import { message } from "@/app/messages/message";
import HomeLink from "@/components/Button/HomeLink/HomeLink";
import { getReadingTimerSummaryApi } from "@/features/Timer/api/readingTimerApi";
import {
  isReadingTimerRunningChangeEvent,
  READING_TIMER_RUNNING_CHANGED_EVENT,
} from "@/features/Timer/lib/readingTimerEvents";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import {
  isUserProfileUpdatedEvent,
  USER_PROFILE_UPDATED_EVENT,
} from "@/features/User/lib/profileEvents";
import { clsx } from "clsx";
import { useCallback, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import * as styles from "./Navigation.css";
import { BOTTOM_NAV_PATH } from "@/app/navigation/bottomNavigation";

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
  const [isTimerRunning, setIsTimerRunning] = useState(false);
  const { pathname } = useLocation();
  const isHomeActive = pathname === BOTTOM_NAV_PATH.home;
  const isPeedActive =
    pathname === BOTTOM_NAV_PATH.feed
    || pathname.startsWith(`${BOTTOM_NAV_PATH.feed}/`);
  const isTimerActive =
    pathname === BOTTOM_NAV_PATH.timer
    || pathname.startsWith(`${BOTTOM_NAV_PATH.timer}/`);
  const isMyPageActive = pathname === "/mypage" || pathname.startsWith("/mypage/");

  /**
   * 서버의 활성 타이머 상태를 조회해 네비게이션 실행 표시를 갱신한다
   *
   * @author SeungHyeon.Kang
   * @return 상태 조회 완료 Promise
   */
  const refreshTimerRunning = useCallback(async (): Promise<void> => {

    try {
      const response = await getReadingTimerSummaryApi();
      setIsTimerRunning(response.data?.activeTimer?.tmrxStat === "RUNNING");
    } catch {
      // 보조 상태 표시 조회 실패는 화면 접근을 막지 않고 기존 표시를 유지한다
    }
  }, []);

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

  useEffect(() => {

    void refreshTimerRunning();

    /**
     * 창이 다시 활성화되면 다른 탭이나 기기에서 변경된 서버 상태를 확인한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const handleWindowFocus = (): void => {

      void refreshTimerRunning();
    };

    /**
     * 현재 앱에서 변경된 독서 타이머 실행 여부를 네비게이션에 즉시 반영한다
     *
     * @author SeungHyeon.Kang
     * @param event 독서 타이머 실행 상태 변경 이벤트
     * @return 반환값이 없다
     */
    const handleTimerRunningChange = (event: Event): void => {

      // 검증된 공통 이벤트의 실행 여부만 네비게이션 상태에 반영한다
      if (isReadingTimerRunningChangeEvent(event)) {
        setIsTimerRunning(event.detail);
      }
    };

    window.addEventListener("focus", handleWindowFocus);
    window.addEventListener(READING_TIMER_RUNNING_CHANGED_EVENT, handleTimerRunningChange);

    // 네비게이션이 해제되면 창과 타이머 상태 이벤트 구독을 함께 정리한다
    return () => {

      window.removeEventListener("focus", handleWindowFocus);
      window.removeEventListener(READING_TIMER_RUNNING_CHANGED_EVENT, handleTimerRunningChange);
    };
  }, [refreshTimerRunning]);

  return (
    <>
      <div className={clsx(styles.navContainer, isMain && styles.whiteBg)}>
        <nav className={styles.navigation}>
          {/* 홈 */}
          <HomeLink
            className={clsx(styles.navLink, isHomeActive && styles.navLinkActive)}
            resetHomeSearch
          >
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 23.3714V11.5429C4 11.1267 4.09319 10.7324 4.27956 10.36C4.46594 9.98762 4.72275 9.68095 5.05 9.44L12.925 3.52571C13.3844 3.17524 13.9094 3 14.5 3C15.0906 3 15.6156 3.17524 16.075 3.52571L23.95 9.44C24.2781 9.68095 24.5354 9.98762 24.7217 10.36C24.9081 10.7324 25.0009 11.1267 25 11.5429V23.3714C25 24.0943 24.7427 24.7133 24.2282 25.2285C23.7137 25.7437 23.096 26.0009 22.375 26H18.4375C18.0656 26 17.7541 25.8738 17.503 25.6215C17.2519 25.3691 17.1259 25.0572 17.125 24.6857V18.1143C17.125 17.7419 16.999 17.43 16.747 17.1785C16.495 16.927 16.1835 16.8009 15.8125 16.8H13.1875C12.8156 16.8 12.5041 16.9262 12.253 17.1785C12.0019 17.4309 11.8759 17.7428 11.875 18.1143V24.6857C11.875 25.0581 11.749 25.3705 11.497 25.6228C11.245 25.8751 10.9335 26.0009 10.5625 26H6.625C5.90312 26 5.28537 25.7428 4.77175 25.2285C4.25812 24.7142 4.00087 24.0952 4 23.3714Z" fill="#C1C1C1"/>
            </svg>
            <p className={styles.navLinkText}>{message("frontend.common.home")}</p>
          </HomeLink>

          {/* 피드 */}
          <Link
            className={clsx(styles.navLink, isPeedActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.feed}
            aria-label={message("frontend.common.peed")}
            aria-current={isPeedActive ? "page" : undefined}
          >
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M25.6667 9.94016V4.6435C25.6667 2.9985 24.92 2.3335 23.065 2.3335H18.3517C16.4967 2.3335 15.75 2.9985 15.75 4.6435V9.92849C15.75 11.5852 16.4967 12.2385 18.3517 12.2385H23.065C24.92 12.2502 25.6667 11.5852 25.6667 9.94016Z" fill="#C1C1C1"/>
              <path d="M25.6667 23.065V18.3517C25.6667 16.4967 24.92 15.75 23.065 15.75H18.3517C16.4967 15.75 15.75 16.4967 15.75 18.3517V23.065C15.75 24.92 16.4967 25.6667 18.3517 25.6667H23.065C24.92 25.6667 25.6667 24.92 25.6667 23.065Z" fill="#C1C1C1"/>
              <path d="M12.25 9.94016V4.6435C12.25 2.9985 11.5033 2.3335 9.64835 2.3335H4.93501C3.08001 2.3335 2.33334 2.9985 2.33334 4.6435V9.92849C2.33334 11.5852 3.08001 12.2385 4.93501 12.2385H9.64835C11.5033 12.2502 12.25 11.5852 12.25 9.94016Z" fill="#C1C1C1"/>
              <path d="M12.25 23.065V18.3517C12.25 16.4967 11.5033 15.75 9.64835 15.75H4.93501C3.08001 15.75 2.33334 16.4967 2.33334 18.3517V23.065C2.33334 24.92 3.08001 25.6667 4.93501 25.6667H9.64835C11.5033 25.6667 12.25 24.92 12.25 23.065Z" fill="#C1C1C1"/>
            </svg>
            <p className={styles.navLinkText}>{message("frontend.common.peed")}</p>
          </Link>

           {/* 타이머 */}
          <Link
            className={clsx(styles.navLink, isTimerActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.timer}
            aria-label={isTimerRunning
              ? `${message("frontend.common.timer")} (${message("frontend.timer.status.running")})`
              : message("frontend.common.timer")}
            aria-current={isTimerActive ? "page" : undefined}
          >
             <span className={styles.navIconWrap}>
               <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
                 <path d="M14 2.3335C7.57168 2.3335 2.33334 7.57183 2.33334 14.0002C2.33334 20.4285 7.57168 25.6668 14 25.6668C20.4283 25.6668 25.6667 20.4285 25.6667 14.0002C25.6667 7.57183 20.4283 2.3335 14 2.3335ZM19.075 18.1652C18.9117 18.4452 18.62 18.5968 18.3167 18.5968C18.165 18.5968 18.0133 18.5618 17.8733 18.4685L14.2567 16.3102C13.3583 15.7735 12.6933 14.5952 12.6933 13.5568V8.7735C12.6933 8.29516 13.09 7.8985 13.5683 7.8985C14.0467 7.8985 14.4433 8.29516 14.4433 8.7735V13.5568C14.4433 13.9768 14.7933 14.5952 15.155 14.8052L18.7717 16.9635C19.1917 17.2085 19.3317 17.7452 19.075 18.1652Z" fill="#C1C1C1"/>
               </svg>
               {isTimerRunning ? <span className={styles.timerRunningBadge} aria-hidden="true" /> : null}
             </span>
             <p className={styles.navLinkText}>{message("frontend.common.timer")}</p>
          </Link>

          {/* 마이페이지 */}
          <Link
            className={clsx(styles.navLink, isMyPageActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.myPage}
            aria-label={message("frontend.common.myPageIconAlt")}
            aria-current={isMyPageActive ? "page" : undefined}
          >
            <ProfileImage
              className={styles.navProfileImage}
              src={profile?.porfPath}
              alt={message("frontend.common.myPageIconAlt")}
            />
            <p className={styles.navLinkText}>{message("frontend.common.mypage")}</p>
          </Link>
        </nav>
      </div>
    </>
  );
}

export default Navigation;
