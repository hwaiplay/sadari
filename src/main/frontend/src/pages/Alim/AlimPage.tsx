import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  notifyFirebasePushEnabled,
  requestFirebaseMessagingToken,
  requestPushNotificationPermission,
} from "@/app/pwa/firebaseMessaging";
import Loading from "@/components/Loading/Loading";
import {
  getMyAlimListApi,
  delAllAlimApi,
  uptAlimReadApi,
  type AlimItem,
} from "@/features/Alim/api/alimApi";
import { notifyUnreadAlimCntChanged } from "@/features/Alim/lib/alimEvents";
import {
  delPushSubApi,
  getPushConfigApi,
  setPushSubApi,
} from "@/features/Push/api/pushApi";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./AlimPage.css";

const PUSH_ENABLED_STORAGE_KEY = "sadari:push-enabled";
const ALIM_DISMISS_ANIMATION_MS = 360;
const ALIM_DISMISS_STAGGER_MS = 30;
const ALIM_DISMISS_MAX_STAGGER_COUNT = 10;

/**
 * 별도 상태 조회 API 없이 버튼 상태를 유지하기 위해 현재 브라우저에 마지막 토글 결과를 저장합니다.
 * 저장값이 아직 없는 기존 사용자는 브라우저 알림 권한이 허용돼 있으면 켜짐 상태로 시작합니다.
 *
 * @author Hanwon.Jang
 * @return 현재 브라우저에서 기억한 푸시 알림 활성 여부
 */
function getInitialPushEnabled() {
  if (!("Notification" in window) || Notification.permission !== "granted") {
    return false;
  }

  const storedStatus = window.localStorage.getItem(PUSH_ENABLED_STORAGE_KEY);
  return storedStatus === null ? true : storedStatus === "Y";
}

/**
 * 서버의 USEE_YSNO 변경이 성공한 뒤 버튼 상태를 현재 브라우저에 보관합니다.
 *
 * @author Hanwon.Jang
 * @param enabled 푸시 알림 활성 여부
 */
function setStoredPushEnabled(enabled: boolean) {
  window.localStorage.setItem(PUSH_ENABLED_STORAGE_KEY, enabled ? "Y" : "N");
}

/**
 * 로그인 사용자의 알림 목록을 보여주는 페이지입니다.
 * 삭제되지 않은 알림을 모두 보여주며, 개별 링크 클릭으로 읽음 처리하고 모두 지우기로 목록에서 제거합니다.
 *
 * @author Hanwon.Jang
 * @return 알림 목록 화면
 */
function AlimPage() {
  const navigate = useNavigate();
  const [alimList, setAlimList] = useState<AlimItem[]>([]);
  const [nextPage, setNextPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isFetchingMore, setIsFetchingMore] = useState(false);
  const [isDeletingAll, setIsDeletingAll] = useState(false);
  const [isClearingAll, setIsClearingAll] = useState(false);
  const [readingAlimNumb, setReadingAlimNumb] = useState<number | null>(null);
  const [isPushEnabled, setIsPushEnabled] = useState(getInitialPushEnabled);
  const [isPushChanging, setIsPushChanging] = useState(false);
  const observerTargetRef = useRef<HTMLDivElement | null>(null);
  const pushTokenRef = useRef<string | null>(null);
  const dismissTimerRef = useRef<number | null>(null);

  const loadAlimList = useCallback(
    async (page: number) => {
      const isFirstPage = page === 1;

      if (isFirstPage) {
        setIsLoading(true);
      } else {
        setIsFetchingMore(true);
      }

      try {
        const response = await getMyAlimListApi(page);
        const data = response.data;

        // 목록 조회는 상태를 변경하지 않으므로 서버가 반환한 미삭제 알림을 읽음 상태 그대로 페이지 순서대로 병합한다.
        setAlimList((prevList) => (
          isFirstPage ? data.list ?? [] : [...prevList, ...(data.list ?? [])]
        ));
        setHasNext(Boolean(data.hasNext));
        setNextPage(data.nextPage ?? page + 1);
        notifyUnreadAlimCntChanged(data.unreadCnt ?? 0);
      } catch (error) {
        void sweetError(
          message("frontend.alim.list.failedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      } finally {
        if (isFirstPage) {
          setIsLoading(false);
        } else {
          setIsFetchingMore(false);
        }
      }
    },
    [],
  );

  useEffect(() => {
    void loadAlimList(1);
  }, [loadAlimList]);

  useEffect(() => {
    return () => {
      if (dismissTimerRef.current !== null) {
        window.clearTimeout(dismissTimerRef.current);
      }
    };
  }, []);

  useEffect(() => {
    const target = observerTargetRef.current;

    if (!target || !hasNext || isLoading || isFetchingMore) {
      return;
    }

    const observer = new IntersectionObserver((entries) => {
      const [entry] = entries;

      // 하단 감지 영역이 보이는 순간 다음 미읽음 알림 20개를 요청한다.
      if (entry?.isIntersecting) {
        void loadAlimList(nextPage);
      }
    });

    observer.observe(target);

    return () => {
      observer.disconnect();
    };
  }, [hasNext, isFetchingMore, isLoading, loadAlimList, nextPage]);

  const handleDeleteAll = async () => {
    if (isDeletingAll) {
      return;
    }

    let dismissAnimationStarted = false;
    setIsDeletingAll(true);

    try {
      const response = await delAllAlimApi();

      // 서버는 아직 불러오지 않은 알림까지 모두 삭제 처리하므로 추가 페이지 요청을 즉시 중단한다.
      setHasNext(false);
      notifyUnreadAlimCntChanged(response.data?.unreadCnt ?? 0);

      // 현재 화면에 카드가 있으면 순차적으로 오른쪽 퇴장시킨 뒤 목록을 비워 빈 상태 문구로 전환한다.
      if (alimList.length > 0) {
        dismissAnimationStarted = true;
        setIsClearingAll(true);
        const maxStaggerCount = Math.min(
          Math.max(alimList.length - 1, 0),
          ALIM_DISMISS_MAX_STAGGER_COUNT,
        );
        const totalAnimationMs =
          ALIM_DISMISS_ANIMATION_MS
          + maxStaggerCount * ALIM_DISMISS_STAGGER_MS;

        dismissTimerRef.current = window.setTimeout(() => {
          setAlimList([]);
          setIsClearingAll(false);
          setIsDeletingAll(false);
          dismissTimerRef.current = null;
        }, totalAnimationMs);
        return;
      }

      setAlimList([]);
    } catch (error) {
      void sweetError(
        message("frontend.alim.readAll.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      // 퇴장 애니메이션이 시작된 경우에는 타이머 완료 시 버튼 잠금을 해제해 중복 요청을 막는다.
      if (!dismissAnimationStarted) {
        setIsDeletingAll(false);
      }
    }
  };

  const getCurrentPushToken = async () => {
    if (pushTokenRef.current) {
      return pushTokenRef.current;
    }

    const configResponse = await getPushConfigApi();
    const token = pushTokenRef.current
      ?? await requestFirebaseMessagingToken(configResponse.data);

    pushTokenRef.current = token;
    return token;
  };

  const handlePushToggle = async () => {
    if (isPushChanging) {
      return;
    }

    const wasPushEnabled = isPushEnabled;
    setIsPushChanging(true);

    try {
      // 켜짐 상태에서 다시 누르면 현재 브라우저 token만 비활성화하고 버튼을 꺼짐 상태로 전환한다.
      if (wasPushEnabled) {
        const token = await getCurrentPushToken();
        await delPushSubApi({ endpUrlx: token });
        setIsPushEnabled(false);
        setStoredPushEnabled(false);
        // 화면표시: "푸시 알림이 꺼졌습니다."
        void sweetSuccess(message("frontend.push.disable.successTitle"));
        return;
      }

      // 브라우저 권한 요청은 버튼 클릭 직후 실행해야 팝업이 차단되지 않는다.
      // Firebase 설정 API를 기다린 뒤 요청하면 사용자 액션으로 인정되지 않아 컨펌창이 뜨지 않을 수 있다.
      await requestPushNotificationPermission();

      const token = await getCurrentPushToken();

      // TB_PSHSUB.ENDP_URLX는 현재 FCM registration token 저장 위치로 사용한다.
      // 서버는 인증 사용자 번호를 직접 채우므로 프론트에서는 token만 전달한다.
      await setPushSubApi({ endpUrlx: token });
      setIsPushEnabled(true);
      setStoredPushEnabled(true);
      notifyFirebasePushEnabled();
      // 화면표시: "푸시 알림이 켜졌습니다."
      void sweetSuccess(message("frontend.push.enable.successTitle"));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "";
      const detailMessage =
        errorMessage === "PUSH_NOT_SUPPORTED"
          ? message("frontend.push.enable.unsupported")
          : errorMessage === "PUSH_INSECURE_CONTEXT"
            ? message("frontend.push.enable.insecureContext")
          : errorMessage === "PUSH_PERMISSION_DENIED"
            ? message("frontend.push.enable.denied")
            : errorMessage === "PUSH_PERMISSION_REQUIRED"
              ? message("frontend.push.enable.denied")
            : errorMessage === "PUSH_SERVICE_WORKER_NOT_READY"
              ? message("frontend.push.enable.serviceWorkerNotReady")
            : getApiErrorMessage(error, message("frontend.common.tryAgain"));

      // 화면표시: "푸시 알림 설정에 실패했습니다." 또는 "푸시 알림 해제에 실패했습니다."
      void sweetError(
        message(
          wasPushEnabled
            ? "frontend.push.disable.failedTitle"
            : "frontend.push.enable.failedTitle",
        ),
        detailMessage,
      );
    } finally {
      setIsPushChanging(false);
    }
  };

  const handleAlimClick = async (alim: AlimItem) => {
    // 링크가 없는 알림은 단순 안내 알림으로 취급해 현재 화면을 유지합니다.
    if (!alim.linkUrlx || readingAlimNumb !== null || isClearingAll) {
      return;
    }

    setReadingAlimNumb(alim.alimNumb);

    try {
      const response = await uptAlimReadApi(alim.alimNumb);
      notifyUnreadAlimCntChanged(response.data?.unreadCnt ?? 0);
      // 읽은 알림도 알림센터에 유지하므로 제거하지 않고 상태만 바꾸어 어두운 스타일을 즉시 적용한다.
      setAlimList((prevList) => (
        prevList.map((item) => (
          item.alimNumb === alim.alimNumb
            ? { ...item, readYsno: "Y" }
            : item
        ))
      ));
      navigate(alim.linkUrlx);
    } catch (error) {
      void sweetError(
        message("frontend.alim.readAll.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setReadingAlimNumb(null);
    }
  };

  const renderAlimIcon = (alimIconName?: string) => {
    // 알림 상황 공통코드의 OPT1_NAME으로 아이콘을 분기한다.
    // 코드가 아직 등록되지 않은 상황은 기존 종 아이콘을 보여줘 알림 목록 자체는 깨지지 않게 한다.
    if (alimIconName === "HEART") {
      return (
        <svg className={styles.alimHeartIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z" />
        </svg>
      );
    }

    if (alimIconName === "FOLLOW") {
      return (
        <svg className={styles.alimIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M19 8v6" />
          <path d="M22 11h-6" />
        </svg>
      );
    }

    return (
      <svg className={styles.alimIcon} viewBox="0 0 24 24" aria-hidden="true">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
    );
  };

  const getAlimIconWrapClass = (alimIconName?: string) => {
    // DB 공통코드의 OPT1_NAME을 화면 스타일로 매핑한다.
    // LIKE는 HEART, FOLLOW는 FOLLOW로 내려오며, 신규 상황 코드가 추가되면 기본 파란 종 아이콘 스타일을 사용한다.
    if (alimIconName === "HEART") {
      return styles.alimIconWrapLike;
    }

    if (alimIconName === "FOLLOW") {
      return styles.alimIconWrapFollow;
    }

    return styles.alimIconWrap;
  };

  if (isLoading) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.title}>{message("frontend.alim.subtitle")}</p>
        </div>
        <div className={styles.headerActions}>
          <button
            className={isPushEnabled ? styles.pushButton : styles.pushButtonOff}
            type="button"
            aria-pressed={isPushEnabled}
            disabled={isPushChanging}
            onClick={() => void handlePushToggle()}
          >
            {message(
              isPushEnabled
                ? "frontend.push.enable"
                : "frontend.push.disable",
            )}
          </button>
          <button
            className={styles.readAllButton}
            type="button"
            disabled={isDeletingAll || isClearingAll || alimList.length === 0}
            onClick={handleDeleteAll}
          >
            {message("frontend.alim.readAll")}
          </button>
        </div>
      </section>

      {alimList.length === 0 ? (
        <div className={styles.empty}>{message("frontend.alim.empty")}</div>
      ) : (
        <section className={styles.list} aria-label={message("frontend.alim.title")}>
          {alimList.map((alim, index) => (
            <button
              className={[
                styles.itemButton,
                alim.readYsno === "Y" ? styles.itemButtonRead : "",
                isClearingAll ? styles.itemButtonLeaving : "",
              ].filter(Boolean).join(" ")}
              type="button"
              disabled={isClearingAll || readingAlimNumb === alim.alimNumb}
              onClick={() => void handleAlimClick(alim)}
              style={isClearingAll ? {
                animationDelay: `${
                  Math.min(index, ALIM_DISMISS_MAX_STAGGER_COUNT)
                  * ALIM_DISMISS_STAGGER_MS
                }ms`,
              } : undefined}
              key={`${alim.userNumb}-${alim.alimNumb}`}
            >
              <span className={getAlimIconWrapClass(alim.alimIconName)} aria-hidden="true">
                <img src={`/img/icons/noti-${alim.alimSitu}.svg`} alt={"icon"} />
              </span>
              <span className={styles.itemText}>
                <span className={styles.itemContent}>{alim.alimCont}</span>
                <span className={styles.itemDate}>{alim.sendDate}</span>
              </span>
            </button>
          ))}
          <div className={styles.scrollTarget} ref={observerTargetRef}>
            {isFetchingMore ? message("frontend.alim.loadingMore") : null}
          </div>
        </section>
      )}
    </main>
  );
}

export default AlimPage;
