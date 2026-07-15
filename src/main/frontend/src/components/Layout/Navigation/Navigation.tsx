import { message } from "@/app/messages/message";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { logoutApi } from "@/features/Auth/api/authApi";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import {
  isUserProfileUpdatedEvent,
  USER_PROFILE_UPDATED_EVENT,
} from "@/features/User/lib/profileEvents";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import { clsx } from "clsx";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container } from "../Container/Container";
import * as styles from "./Navigation.css";

type NavigationProps = {
  isMain: boolean;
};

const MENU_ITEMS = [
  { label: "독서 캘린더", disabled: false },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
];

/**
 * 하단 네비게이션과 마이페이지 drawer 메뉴를 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @param isMain 메인 레이아웃 여부
 * @return 하단 네비게이션 컴포넌트
 */
function Navigation({ isMain }: NavigationProps) {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const navigate = useNavigate();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const profileImage = profile?.porfPath || "/img/common/icon-user.svg";
  const profileName = profile?.userNick || "사용자";
  const profileIntro =
    profile?.intrCntn || message("frontend.profile.intro.empty");

  /**
   * 로그아웃 확인 후 서버 로그아웃 API를 호출하고 인증 상태를 초기화합니다.
   *
   * @author Hanwon.Jang
   * @return
   */
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
      setIsDrawerOpen(false);
      navigate("/login");
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
        if (!ignore && response.data?.code === 200) {
          setProfile(response.data.data);
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

  return (
    <>
      <Container className={clsx(styles.navContainer, isMain && styles.whiteBg)}>
        <nav className={styles.navigation}>
          <LinkButton
            link="/home"
            className={styles.navLink}
            state={{ resetHomeSearch: true }}
          >
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" aria-hidden="true">
              <path d="M5.3 25.3v-12c0-.8.4-1.6 1.1-2.1l8-6c1-.7 2.2-.7 3.2 0l8 6c.7.5 1.1 1.3 1.1 2.1v12c0 1.5-1.2 2.7-2.7 2.7h-4c-.7 0-1.3-.6-1.3-1.3V20c0-.7-.6-1.3-1.3-1.3h-2.7c-.7 0-1.3.6-1.3 1.3v6.7c0 .7-.6 1.3-1.3 1.3h-4c-1.5 0-2.7-1.2-2.7-2.7Z" fill="#333333" />
            </svg>
          </LinkButton>
          <LinkButton link="/set" className={clsx(styles.navLink, styles.navLink__set)}>
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
              <path d="M36 26H26v10a2 2 0 0 1-4 0V26H12a2 2 0 0 1 0-4h10V12a2 2 0 0 1 4 0v10h10a2 2 0 0 1 0 4Z" fill="#333333" />
            </svg>
          </LinkButton>
          <button
            className={styles.navProfileButton}
            type="button"
            aria-label={message("frontend.common.myPageIconAlt")}
            aria-expanded={isDrawerOpen}
            onClick={() => setIsDrawerOpen(true)}
          >
            <img
              className={styles.navProfileImage}
              src={profileImage}
              alt={message("frontend.common.myPageIconAlt")}
            />
          </button>
        </nav>
      </Container>

      <div
        className={clsx(
          styles.drawerOverlay,
          isDrawerOpen && styles.drawerOverlayVisible,
        )}
        aria-hidden={!isDrawerOpen}
      >
        <button
          className={clsx(
            styles.drawerBackdrop,
            isDrawerOpen && styles.drawerBackdropVisible,
          )}
          type="button"
          aria-label={message("frontend.common.close")}
          onClick={() => setIsDrawerOpen(false)}
        />
        <aside
          className={clsx(styles.drawer, isDrawerOpen && styles.drawerOpen)}
          aria-label="마이페이지 메뉴"
        >
          <section className={styles.drawerHeader}>
            <button
              className={styles.drawerProfileSummaryButton}
              type="button"
              onClick={() => {
                setIsDrawerOpen(false);
                navigate("/mypage/profile");
              }}
            >
              <img className={styles.drawerProfileImage} src={profileImage} alt="" />
              <div className={styles.drawerProfileMeta}>
                <strong className={styles.drawerProfileName}>{profileName}</strong>
                <span className={styles.drawerProfileSub}>{profileIntro}</span>
              </div>
            </button>
            <div className={styles.drawerActionGroup}>
              <button
                className={styles.drawerLogoutButton}
                type="button"
                onClick={handleLogout}
              >
                {message("frontend.auth.logout")}
              </button>
            </div>
          </section>
          <div className={styles.drawerMenu}>
            {MENU_ITEMS.map((item, index) => (
              <button
                className={clsx(
                  styles.drawerMenuButton,
                  item.disabled && styles.drawerMenuDisabled,
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
    </>
  );
}

export default Navigation;
