import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  requestFirebaseToken,
  requestPushPermission,
} from "@/app/pwa/firebaseMessaging";
import { notifyFirebasePushEnabled } from "@/app/pwa/pushEvents";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import {
  delPushSubApi,
  getPushConfigApi,
  setPushSubApi,
} from "@/features/Push/api/pushApi";
import {
  getUserSettingApi,
  uptUserAlimSettingApi,
  uptUserPrivacyApi,
  type UserSetting,
} from "@/features/User/api/userApi";
import { useEffect, useRef, useState } from "react";
import * as styles from "./UserSettingsPage.css";

const PUSH_ENABLED_STORAGE_KEY = "sadari:push-enabled";

type UserSettingsPageProps = {
  section: "notifications" | "privacy";
};

type SettingField = keyof UserSetting;

const NOTIFICATION_FIELDS: SettingField[] = [
  "likeAlimYsno",
  "replyAlimYsno",
  "followAlimYsno",
  "clubAlimYsno",
  "reportDueAlimYsno",
  "reportLikeDefaultYsno",
  "reportReplyDefaultYsno",
];

const PRIVACY_FIELDS: SettingField[] = [
  "readingStatisticsYsno",
  "readingGoalYsno",
  "imageFeedYsno",
  "reportPublicDefaultYsno",
];

/** 현재 브라우저 권한과 마지막 서버 변경 결과로 기기 푸시 상태를 초기화한다. */
function getInitialPushEnabled(): boolean {
  if (!("Notification" in window) || Notification.permission !== "granted") {
    return false;
  }

  return window.localStorage.getItem(PUSH_ENABLED_STORAGE_KEY) !== "N";
}

/** 사용자 설정 범주를 저장하고 현재 기기 푸시를 별도로 제어하는 화면이다. */
function UserSettingsPage({ section }: UserSettingsPageProps) {
  const [setting, setSetting] = useState<UserSetting | null>(null);
  const [savedSetting, setSavedSetting] = useState<UserSetting | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isPushEnabled, setIsPushEnabled] = useState(getInitialPushEnabled);
  const [isPushChanging, setIsPushChanging] = useState(false);
  const pushTokenRef = useRef<string | null>(null);
  const fields = section === "notifications" ? NOTIFICATION_FIELDS : PRIVACY_FIELDS;
  const isDirty = Boolean(setting && savedSetting)
    && fields.some((field) => setting?.[field] !== savedSetting?.[field]);

  useEffect(() => {
    let ignore = false;

    getUserSettingApi()
      .then((data) => {
        if (!ignore) {
          setSetting(data);
          setSavedSetting(data);
        }
      })
      .catch((error) => {
        if (!ignore) {
          void sweetError(
            message("frontend.settings.load.failedTitle"),
            getApiErrorMessage(error, message("frontend.common.tryAgain")),
          );
        }
      })
      .finally(() => {
        if (!ignore) {
          setIsLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!isDirty) {
        return;
      }

      event.preventDefault();
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [isDirty]);

  /** 단일 Y/N 설정을 체크박스 상태에 맞춰 변경한다. */
  const handleToggle = (field: SettingField) => {
    setSetting((current) => current
      ? { ...current, [field]: current[field] === "Y" ? "N" : "Y" }
      : current);
  };

  /** 현재 설정 화면의 필드만 서버에 저장한다. */
  const handleSave = async () => {
    if (!setting || !isDirty || isSaving) {
      return;
    }

    setIsSaving(true);
    try {
      const saved = await runBlockingOperation(
        () => section === "notifications"
          ? uptUserAlimSettingApi({
              likeAlimYsno: setting.likeAlimYsno,
              replyAlimYsno: setting.replyAlimYsno,
              followAlimYsno: setting.followAlimYsno,
              clubAlimYsno: setting.clubAlimYsno,
              reportDueAlimYsno: setting.reportDueAlimYsno,
              reportLikeDefaultYsno: setting.reportLikeDefaultYsno,
              reportReplyDefaultYsno: setting.reportReplyDefaultYsno,
            })
          : uptUserPrivacyApi({
              readingStatisticsYsno: setting.readingStatisticsYsno,
              readingGoalYsno: setting.readingGoalYsno,
              imageFeedYsno: setting.imageFeedYsno,
              reportPublicDefaultYsno: setting.reportPublicDefaultYsno,
            }),
        {
          title: message("frontend.settings.saving"),
          success: { title: message("frontend.settings.save.successTitle") },
        },
      );
      setSetting(saved);
      setSavedSetting(saved);
    } catch (error) {
      void sweetError(
        message("frontend.settings.save.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsSaving(false);
    }
  };

  /** 현재 브라우저의 FCM 토큰을 한 번만 조회한다. */
  const getCurrentPushToken = async () => {
    if (pushTokenRef.current) {
      return pushTokenRef.current;
    }

    const config = await getPushConfigApi();
    pushTokenRef.current = await requestFirebaseToken(config.data);
    return pushTokenRef.current;
  };

  /** 계정 알림 설정과 독립적으로 현재 브라우저의 푸시 구독을 즉시 변경한다. */
  const handlePushToggle = async () => {
    if (isPushChanging) {
      return;
    }

    const wasEnabled = isPushEnabled;
    setIsPushChanging(true);
    try {
      await runBlockingOperation(async () => {
        if (wasEnabled) {
          await delPushSubApi({ endpUrlx: await getCurrentPushToken() });
        } else {
          await requestPushPermission();
          await setPushSubApi({ endpUrlx: await getCurrentPushToken() });
          notifyFirebasePushEnabled();
        }
      }, {
        title: message(
          wasEnabled ? "frontend.push.changing.disable" : "frontend.push.changing.enable",
        ),
        success: {
          title: message(
            wasEnabled ? "frontend.push.disable.successTitle" : "frontend.push.enable.successTitle",
          ),
        },
      });
      setIsPushEnabled(!wasEnabled);
      window.localStorage.setItem(PUSH_ENABLED_STORAGE_KEY, wasEnabled ? "N" : "Y");
    } catch (error) {
      const code = error instanceof Error ? error.message : "";
      const detail = code === "PUSH_NOT_SUPPORTED"
        ? message("frontend.push.enable.unsupported")
        : code === "PUSH_INSECURE_CONTEXT"
          ? message("frontend.push.enable.insecureContext")
          : code === "PUSH_PERMISSION_DENIED" || code === "PUSH_PERMISSION_REQUIRED"
            ? message("frontend.push.enable.denied")
            : code === "PUSH_SERVICE_WORKER_NOT_READY"
              ? message("frontend.push.enable.serviceWorkerNotReady")
              : getApiErrorMessage(error, message("frontend.common.tryAgain"));
      void sweetError(
        message(wasEnabled ? "frontend.push.disable.failedTitle" : "frontend.push.enable.failedTitle"),
        detail,
      );
    } finally {
      setIsPushChanging(false);
    }
  };

  /** 접근 가능한 이름과 설명이 포함된 설정 스위치를 표시한다. */
  const renderSwitch = (field: SettingField, titleKey: string, descriptionKey: string) => (
    <label className={styles.settingRow} key={field}>
      <span className={styles.settingText}>
        <strong className={styles.settingTitle}>{message(titleKey)}</strong>
        <span className={styles.settingDescription}>{message(descriptionKey)}</span>
      </span>
      <input
        className={styles.switchInput}
        type="checkbox"
        checked={setting?.[field] === "Y"}
        onChange={() => handleToggle(field)}
      />
      <span className={styles.switchTrack} aria-hidden="true" />
    </label>
  );

  if (isLoading) {
    return <Loading />;
  }

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.pageTitle}>
          {message(section === "notifications"
            ? "frontend.settings.notifications.title"
            : "frontend.settings.privacy.title")}
        </h1>
        <p className={styles.pageDescription}>
          {message(section === "notifications"
            ? "frontend.settings.notifications.description"
            : "frontend.settings.privacy.description")}
        </p>
      </header>

      {section === "notifications" ? (
        <>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>{message("frontend.settings.notifications.category")}</h2>
            {renderSwitch("likeAlimYsno", "frontend.settings.notifications.like", "frontend.settings.notifications.like.description")}
            {renderSwitch("replyAlimYsno", "frontend.settings.notifications.reply", "frontend.settings.notifications.reply.description")}
            {renderSwitch("followAlimYsno", "frontend.settings.notifications.follow", "frontend.settings.notifications.follow.description")}
            {renderSwitch("clubAlimYsno", "frontend.settings.notifications.club", "frontend.settings.notifications.club.description")}
            {renderSwitch("reportDueAlimYsno", "frontend.settings.notifications.due", "frontend.settings.notifications.due.description")}
          </section>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>{message("frontend.settings.notifications.reportDefault")}</h2>
            {renderSwitch("reportLikeDefaultYsno", "frontend.settings.notifications.reportLike", "frontend.settings.notifications.reportLike.description")}
            {renderSwitch("reportReplyDefaultYsno", "frontend.settings.notifications.reportReply", "frontend.settings.notifications.reportReply.description")}
          </section>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>{message("frontend.settings.notifications.device")}</h2>
            <p className={styles.deviceDescription}>{message("frontend.settings.notifications.device.description")}</p>
            <ActionButton
              variant="secondary"
              size="lg"
              width="full"
              aria-pressed={isPushEnabled}
              disabled={isPushChanging}
              onClick={() => void handlePushToggle()}
            >
              {message(isPushEnabled ? "frontend.push.enable" : "frontend.push.disable")}
            </ActionButton>
          </section>
        </>
      ) : (
        <section className={styles.section}>
          {renderSwitch("readingStatisticsYsno", "frontend.settings.privacy.statistics", "frontend.settings.privacy.statistics.description")}
          {renderSwitch("readingGoalYsno", "frontend.settings.privacy.goal", "frontend.settings.privacy.goal.description")}
          {renderSwitch("imageFeedYsno", "frontend.settings.privacy.imageFeed", "frontend.settings.privacy.imageFeed.description")}
          {renderSwitch("reportPublicDefaultYsno", "frontend.settings.privacy.reportDefault", "frontend.settings.privacy.reportDefault.description")}
        </section>
      )}

      <div className={styles.saveArea}>
        <ActionButton
          variant="primary"
          size="lg"
          width="full"
          disabled={!isDirty || isSaving}
          onClick={() => void handleSave()}
        >
          {message("frontend.common.save")}
        </ActionButton>
      </div>
    </main>
  );
}

export default UserSettingsPage;
