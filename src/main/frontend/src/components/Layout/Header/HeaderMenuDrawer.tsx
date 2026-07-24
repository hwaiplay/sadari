import { message } from "@/app/messages/message";
import { queryClient } from "@/app/query/queryClient";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { logoutApi } from "@/features/Auth/api/authApi";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import {
  isUserProfileUpdatedEvent,
  USER_PROFILE_UPDATED_EVENT,
} from "@/features/User/lib/profileEvents";
import { clsx } from "clsx";
import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import { hamburgerButton, hamburgerIcon } from "./Header.css";
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
  const navigate = useNavigate();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const profileImage = profile?.porfPath || "/img/common/icon-user.svg";
  const profileName = profile?.userNick || "사용자";
  const profileIntro =
    profile?.intrCntn || message("frontend.profile.intro.empty");
  const portalTarget = typeof document === "undefined" ? null : document.body;

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
        <div className={drawerStyles.drawerFooter}>
          <button
            className={drawerStyles.drawerAlimButton}
            type="button"
            aria-label={message("frontend.alim.title")}
            onClick={() => {
              // 알림 아이콘은 드로어 안의 하단 액션이므로 먼저 드로어를 닫고 알림 목록 화면으로 이동합니다.
              setIsDrawerOpen(false);
              navigate("/alim");
            }}
          >
            <svg className={drawerStyles.drawerAlimIcon} viewBox="0 0 24 24" aria-hidden="true">
              <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
          </button>
        </div>
      </aside>
    </div>
  );

  return (
    <>
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
