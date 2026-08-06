import { message } from "@/app/messages/message";
import HomeLink from "@/components/Button/HomeLink/HomeLink";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import {
  isUserProfileUpdatedEvent,
  USER_PROFILE_UPDATED_EVENT,
} from "@/features/User/lib/profileEvents";
import { clsx } from "clsx";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./Navigation.css";

type NavigationProps = {
  isMain: boolean;
};

/**
 * Navigation 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
 */
function Navigation({ isMain }: NavigationProps) {

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const navigate = useNavigate();

  useEffect(() => {

    let ignore = false;
    /**
     * handle Profile Updated 사용자 동작을 처리한다
     *
     * @author HanWon.Jang
     * @param event event 입력값
     * @return 반환값이 없다
     */
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

  return (
    <div className={clsx(styles.navContainer, isMain && styles.whiteBg)}>
      {/* 홈과 마이페이지 이동 영역 */}
      <nav className={styles.navigation}>
        <HomeLink
          className={styles.navLink}
          resetHomeSearch
        >
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none" aria-hidden="true">
            <path d="M5.3 25.3v-12c0-.8.4-1.6 1.1-2.1l8-6c1-.7 2.2-.7 3.2 0l8 6c.7.5 1.1 1.3 1.1 2.1v12c0 1.5-1.2 2.7-2.7 2.7h-4c-.7 0-1.3-.6-1.3-1.3V20c0-.7-.6-1.3-1.3-1.3h-2.7c-.7 0-1.3.6-1.3 1.3v6.7c0 .7-.6 1.3-1.3 1.3h-4c-1.5 0-2.7-1.2-2.7-2.7Z" fill="#333333" />
          </svg>
        </HomeLink>
        <LinkButton link="/set" className={clsx(styles.navLink, styles.navLink__set)}>
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
            <path d="M36 26H26v10a2 2 0 0 1-4 0V26H12a2 2 0 0 1 0-4h10V12a2 2 0 0 1 4 0v10h10a2 2 0 0 1 0 4Z" fill="#333333" />
          </svg>
        </LinkButton>
        <button
          className={styles.navProfileButton}
          type="button"
          aria-label={message("frontend.common.myPageIconAlt")}
          onClick={() => navigate("/mypage/profile")}
        >
          <ProfileImage
            className={styles.navProfileImage}
            src={profile?.porfPath}
            alt={message("frontend.common.myPageIconAlt")}
          />
        </button>
      </nav>
    </div>
  );
}

export default Navigation;
