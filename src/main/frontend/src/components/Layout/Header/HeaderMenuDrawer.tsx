import { message } from "@/app/messages/message";
import {
  FIREBASE_PUSH_ENABLED_EVENT,
  subscribeFirebaseForegroundMessages,
} from "@/app/pwa/firebaseMessaging";
import { queryClient } from "@/app/query/queryClient";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { getUnreadAlimCntApi } from "@/features/Alim/api/alimApi";
import {
  isUnreadAlimCntChangedEvent,
  UNREAD_ALIM_CNT_CHANGED_EVENT,
} from "@/features/Alim/lib/alimEvents";
import { logoutApi } from "@/features/Auth/api/authApi";
import { getPushConfigApi } from "@/features/Push/api/pushApi";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import {
  isUserProfileUpdatedEvent,
  USER_PROFILE_UPDATED_EVENT,
} from "@/features/User/lib/profileEvents";
import { clsx } from "clsx";
import { useCallback, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import {
  headerAlimBadge,
  headerAlimButton,
  headerAlimIcon,
  hamburgerButton,
  hamburgerIcon,
} from "./Header.css";
import * as drawerStyles from "../Navigation/Navigation.css";

const MENU_ITEMS = [
  { label: "독서 캘린더", disabled: false },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
];

function HeaderMenuDrawer() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [unreadAlimCnt, setUnreadAlimCnt] = useState(0);
  const navigate = useNavigate();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const profileImage = profile?.porfPath || "/img/common/icon-user.svg";
  const profileName = profile?.userNick || "사용자";
  const profileIntro =
    profile?.intrCntn || message("frontend.profile.intro.empty");
  const portalTarget = typeof document === "undefined" ? null : document.body;

  const refreshUnreadAlimCnt = useCallback(async () => {
    try {
      const response = await getUnreadAlimCntApi();
      setUnreadAlimCnt(response.data?.unreadCnt ?? 0);
    } catch {
      // 알림 배지는 보조 정보이므로 조회 실패 시 기존 숫자를 유지한다.
    }
  }, []);

  const handleLogout = async () => {
    const confirmed = await sweetConfirm({
      title: message("frontend.auth.logoutConfirmTitle"),
      confirmButtonText: message("frontend.auth.logout"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (!confirmed.isConfirmed) {
      return;
    }

    try {
      await logoutApi();
    } finally {
      clearAuth();
      // Logout changes the auth state immediately; clearing the cached check prevents /login -> /home loops.
      queryClient.removeQueries({ queryKey: ["auth"] });
      setIsDrawerOpen(false);
      navigate("/login", { replace: true });
    }
  };

  useEffect(() => {
    let ignore = false;
    const handleProfileUpdated = (event: Event) => {
      if (isUserProfileUpdatedEvent(event)) {
        setProfile(event.detail);
      }
    };

    getMyProfileApi()
      .then((response) => {
        if (!ignore) {
          setProfile(response.data);
        }
      })
      .catch(() => {
        if (!ignore) {
          setProfile(null);
        }
      });

    window.addEventListener(USER_PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      ignore = true;
      window.removeEventListener(USER_PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, []);

  useEffect(() => {
    void refreshUnreadAlimCnt();

    const handleServiceWorkerMessage = (event: MessageEvent) => {
      // 푸시 수신뿐 아니라 시스템 알림 클릭으로 읽음 상태가 바뀐 경우에도 배지 수를 다시 조회한다.
      if (
        event.data?.type === "SADARI_ALIM_RECEIVED"
        || event.data?.type === "SADARI_ALIM_READ"
      ) {
        void refreshUnreadAlimCnt();
      }
    };

    const handleWindowFocus = () => {
      void refreshUnreadAlimCnt();
    };

    const handleUnreadAlimCntChanged = (event: Event) => {
      if (isUnreadAlimCntChangedEvent(event)) {
        setUnreadAlimCnt(event.detail);
      }
    };

    navigator.serviceWorker?.addEventListener("message", handleServiceWorkerMessage);
    window.addEventListener("focus", handleWindowFocus);
    window.addEventListener(UNREAD_ALIM_CNT_CHANGED_EVENT, handleUnreadAlimCntChanged);

    return () => {
      navigator.serviceWorker?.removeEventListener("message", handleServiceWorkerMessage);
      window.removeEventListener("focus", handleWindowFocus);
      window.removeEventListener(UNREAD_ALIM_CNT_CHANGED_EVENT, handleUnreadAlimCntChanged);
    };
  }, [refreshUnreadAlimCnt]);

  useEffect(() => {
    let unsubscribe: (() => void) | undefined;
    let ignore = false;

    const initializeForegroundMessages = async () => {
      if (!("Notification" in window) || Notification.permission !== "granted") {
        return;
      }

      try {
        const response = await getPushConfigApi();
        const unsubscribeForegroundMessages =
          await subscribeFirebaseForegroundMessages(
            response.data,
            () => void refreshUnreadAlimCnt(),
          );

        if (ignore) {
          unsubscribeForegroundMessages();
          return;
        }

        unsubscribe?.();
        unsubscribe = unsubscribeForegroundMessages;
      } catch {
        // 포그라운드 리스너 초기화 실패는 기본 화면 사용을 막지 않는다.
      }
    };

    const handlePushEnabled = () => {
      void initializeForegroundMessages();
    };

    void initializeForegroundMessages();
    window.addEventListener(FIREBASE_PUSH_ENABLED_EVENT, handlePushEnabled);

    return () => {
      ignore = true;
      unsubscribe?.();
      window.removeEventListener(FIREBASE_PUSH_ENABLED_EVENT, handlePushEnabled);
    };
  }, [refreshUnreadAlimCnt]);

  const drawer = (
    <div
      className={clsx(
        drawerStyles.drawerOverlay,
        isDrawerOpen && drawerStyles.drawerOverlayVisible,
      )}
      aria-hidden={!isDrawerOpen}
    >
      <button
        className={clsx(
          drawerStyles.drawerBackdrop,
          isDrawerOpen && drawerStyles.drawerBackdropVisible,
        )}
        type="button"
        aria-label={message("frontend.common.close")}
        onClick={() => setIsDrawerOpen(false)}
      />
      <aside
        className={clsx(drawerStyles.drawer, isDrawerOpen && drawerStyles.drawerOpen)}
        aria-label="마이페이지 메뉴"
      >
        <section className={drawerStyles.drawerHeader}>
          <button
            className={drawerStyles.drawerProfileSummaryButton}
            type="button"
            onClick={() => {
              setIsDrawerOpen(false);
              navigate("/mypage/profile");
            }}
          >
            <img className={drawerStyles.drawerProfileImage} src={profileImage} alt="" />
            <div className={drawerStyles.drawerProfileMeta}>
              <strong className={drawerStyles.drawerProfileName}>{profileName}</strong>
              <span className={drawerStyles.drawerProfileSub}>{profileIntro}</span>
            </div>
          </button>
          <div className={drawerStyles.drawerActionGroup}>
            <button
              className={drawerStyles.drawerLogoutButton}
              type="button"
              onClick={handleLogout}
            >
              {message("frontend.auth.logout")}
            </button>
          </div>
        </section>
        <div className={drawerStyles.drawerMenu}>
          {MENU_ITEMS.map((item, index) => (
            <button
              className={clsx(
                drawerStyles.drawerMenuButton,
                item.disabled && drawerStyles.drawerMenuDisabled,
              )}
              type="button"
              disabled={item.disabled}
              onClick={() => {
                if (item.disabled) {
                  return;
                }

                setIsDrawerOpen(false);
                navigate("/mypage/reading-calendar");
              }}
              key={`${item.label}-${index}`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </aside>
    </div>
  );

  return (
    <>
      <button
        className={headerAlimButton}
        type="button"
        aria-label={message("frontend.alim.title")}
        onClick={() => navigate("/alim")}
      >
        <svg className={headerAlimIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {unreadAlimCnt > 0 ? (
          <span className={headerAlimBadge}>
            {unreadAlimCnt > 99 ? "99+" : unreadAlimCnt}
          </span>
        ) : null}
      </button>
      <button
        className={hamburgerButton}
        type="button"
        aria-label="메뉴 열기"
        aria-expanded={isDrawerOpen}
        onClick={() => setIsDrawerOpen(true)}
      >
        <svg className={hamburgerIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M4 7h16M4 12h16M4 17h16" />
        </svg>
      </button>
      {portalTarget ? createPortal(drawer, portalTarget) : null}
    </>
  );
}

export default HeaderMenuDrawer;
