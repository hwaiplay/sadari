import { message } from "@/app/messages/message";
import HomeLink from "@/components/Button/HomeLink/HomeLink";
import {
  isReadingTimerRunningChangeEvent,
  READING_TIMER_RUNNING_CHANGED_EVENT,
} from "@/features/Timer/lib/readingTimerEvents";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useMyProfileQuery } from "@/features/User/hooks/useMyProfileQuery";
import { useTimerSummaryQuery } from "@/features/Timer/hooks/useTimerSummaryQuery";
import { clsx } from "clsx";
import { useCallback, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import * as styles from "./Navigation.css";
import {
  BOTTOM_NAV_PATH,
  getBottomNavState,
} from "@/app/navigation/bottomNavigation";

type NavigationProps = {
  isMain: boolean;
};

/**
 * fileName       : Navigation
 * author         : Hanwon.Jang
 * date           : 2026-08-26
 * description    : 하단 네비게이션 메뉴
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        Hanwon.Jang    주석 추가
 * 2026-08-26        Hanwon.Jang    모임 추가
 */

const Navigation = ({ isMain }: NavigationProps) => {

  // 헤더와 프로필 화면의 사용자 조회
  const myProfileQuery = useMyProfileQuery();
  // 타이머 페이지와 같은 요약 요청 및 캐시
  const timerSummaryQuery = useTimerSummaryQuery();
  const refetchTimerSummary = timerSummaryQuery.refetch;
  const profile = myProfileQuery.data ?? null;
  const [timerRunningOverride, setTimerRunningOverride] = useState<boolean | null>(null);
  const isTimerRunning = timerRunningOverride
    ?? (timerSummaryQuery.data?.activeTimer?.tmrxStat === "RUNNING");
  const { pathname } = useLocation();

  // 현재 페이지에 대한 active 표시
  const isHomeActive = pathname === BOTTOM_NAV_PATH.home;
  const isFeedActive =
    pathname === BOTTOM_NAV_PATH.feed
    || pathname.startsWith(`${BOTTOM_NAV_PATH.feed}/`);
  const isTimerActive =
    pathname === BOTTOM_NAV_PATH.timer
    || pathname.startsWith(`${BOTTOM_NAV_PATH.timer}/`);
  const isMyPageActive = pathname === "/mypage" || pathname.startsWith("/mypage/");
  const isClubPageActive = pathname === BOTTOM_NAV_PATH.club
    || pathname.startsWith(`/reading-clubs`);

  /**
   * 서버의 활성 타이머 상태를 조회해 네비게이션 실행 표시를 갱신한다
   *
   * @author SeungHyeon.Kang
   * @return 상태 조회 완료 Promise
   */
  const refreshTimerRunning = useCallback(async (): Promise<void> => {
    // 창 포커스 시 공통 Query를 갱신해 타이머 페이지와 최신 서버 상태를 공유한다
    await refetchTimerSummary();
    // 서버 응답이 다시 계산되도록 일시적 이벤트 덮어쓰기를 해제한다
    setTimerRunningOverride(null);
  }, [refetchTimerSummary]);

  useEffect(() => {
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
        setTimerRunningOverride(event.detail);
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
            navigationState={getBottomNavState(pathname, BOTTOM_NAV_PATH.home)}
          >
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 23.3714V11.5429C4 11.1267 4.09319 10.7324 4.27956 10.36C4.46594 9.98762 4.72275 9.68095 5.05 9.44L12.925 3.52571C13.3844 3.17524 13.9094 3 14.5 3C15.0906 3 15.6156 3.17524 16.075 3.52571L23.95 9.44C24.2781 9.68095 24.5354 9.98762 24.7217 10.36C24.9081 10.7324 25.0009 11.1267 25 11.5429V23.3714C25 24.0943 24.7427 24.7133 24.2282 25.2285C23.7137 25.7437 23.096 26.0009 22.375 26H18.4375C18.0656 26 17.7541 25.8738 17.503 25.6215C17.2519 25.3691 17.1259 25.0572 17.125 24.6857V18.1143C17.125 17.7419 16.999 17.43 16.747 17.1785C16.495 16.927 16.1835 16.8009 15.8125 16.8H13.1875C12.8156 16.8 12.5041 16.9262 12.253 17.1785C12.0019 17.4309 11.8759 17.7428 11.875 18.1143V24.6857C11.875 25.0581 11.749 25.3705 11.497 25.6228C11.245 25.8751 10.9335 26.0009 10.5625 26H6.625C5.90312 26 5.28537 25.7428 4.77175 25.2285C4.25812 24.7142 4.00087 24.0952 4 23.3714Z" fill="#C1C1C1"/>
            </svg>
            <p className={styles.navLinkText}>{message("frontend.common.home")}</p>
          </HomeLink>

          {/* 피드 */}
          <Link
            className={clsx(styles.navLink, isFeedActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.feed}
            state={getBottomNavState(pathname, BOTTOM_NAV_PATH.feed)}
            aria-label={message("frontend.common.feed")}
            aria-current={isFeedActive ? "page" : undefined}
          >
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M25.6667 9.94016V4.6435C25.6667 2.9985 24.92 2.3335 23.065 2.3335H18.3517C16.4967 2.3335 15.75 2.9985 15.75 4.6435V9.92849C15.75 11.5852 16.4967 12.2385 18.3517 12.2385H23.065C24.92 12.2502 25.6667 11.5852 25.6667 9.94016Z" fill="#C1C1C1"/>
              <path d="M25.6667 23.065V18.3517C25.6667 16.4967 24.92 15.75 23.065 15.75H18.3517C16.4967 15.75 15.75 16.4967 15.75 18.3517V23.065C15.75 24.92 16.4967 25.6667 18.3517 25.6667H23.065C24.92 25.6667 25.6667 24.92 25.6667 23.065Z" fill="#C1C1C1"/>
              <path d="M12.25 9.94016V4.6435C12.25 2.9985 11.5033 2.3335 9.64835 2.3335H4.93501C3.08001 2.3335 2.33334 2.9985 2.33334 4.6435V9.92849C2.33334 11.5852 3.08001 12.2385 4.93501 12.2385H9.64835C11.5033 12.2502 12.25 11.5852 12.25 9.94016Z" fill="#C1C1C1"/>
              <path d="M12.25 23.065V18.3517C12.25 16.4967 11.5033 15.75 9.64835 15.75H4.93501C3.08001 15.75 2.33334 16.4967 2.33334 18.3517V23.065C2.33334 24.92 3.08001 25.6667 4.93501 25.6667H9.64835C11.5033 25.6667 12.25 24.92 12.25 23.065Z" fill="#C1C1C1"/>
            </svg>
            <p className={styles.navLinkText}>{message("frontend.common.feed")}</p>
          </Link>

          {/* 모임 */}
          <Link
            className={clsx(styles.navLink, isClubPageActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.club}
            state={getBottomNavState(pathname, BOTTOM_NAV_PATH.club)}
            aria-label={message("frontend.common.club")}
            aria-current={isClubPageActive ? "page" : undefined}
          >
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20.4517 9.06492C20.37 9.05325 20.2884 9.05325 20.2067 9.06492C18.3984 9.00659 16.9634 7.52492 16.9634 5.70492C16.9634 3.84992 18.4684 2.33325 20.335 2.33325C22.19 2.33325 23.7067 3.83825 23.7067 5.70492C23.695 7.52492 22.26 9.00659 20.4517 9.06492Z" fill="#C1C1C1"/>
              <path d="M24.255 17.15C22.9484 18.025 21.1167 18.3516 19.425 18.13C19.8684 17.1733 20.1017 16.1117 20.1134 14.9916C20.1134 13.825 19.8567 12.7166 19.3667 11.7483C21.0934 11.515 22.925 11.8416 24.2434 12.7166C26.0867 13.93 26.0867 15.925 24.255 17.15Z" fill="#C1C1C1"/>
              <path d="M7.51335 9.06492C7.59502 9.05325 7.67669 9.05325 7.75835 9.06492C9.56669 9.00659 11.0017 7.52492 11.0017 5.70492C11.0017 3.83825 9.49669 2.33325 7.63002 2.33325C5.77502 2.33325 4.27002 3.83825 4.27002 5.70492C4.27002 7.52492 5.70502 9.00659 7.51335 9.06492Z" fill="#C1C1C1"/>
              <path d="M7.64156 14.9916C7.64156 16.1233 7.88656 17.1966 8.32989 18.165C6.68489 18.34 4.96989 17.99 3.70989 17.1616C1.86656 15.9366 1.86656 13.9416 3.70989 12.7166C4.95823 11.8766 6.71989 11.5383 8.37656 11.725C7.89823 12.705 7.64156 13.8133 7.64156 14.9916Z" fill="#C1C1C1"/>
              <path d="M14.1401 18.515C14.0468 18.5033 13.9418 18.5033 13.8368 18.515C11.6901 18.445 9.9751 16.6833 9.9751 14.5133C9.98676 12.2967 11.7718 10.5 14.0001 10.5C16.2168 10.5 18.0134 12.2967 18.0134 14.5133C18.0018 16.6833 16.2984 18.445 14.1401 18.515Z" fill="#C1C1C1"/>
              <path d="M10.3483 20.9301C8.58668 22.1084 8.58668 24.0451 10.3483 25.2117C12.355 26.5534 15.645 26.5534 17.6517 25.2117C19.4133 24.0334 19.4133 22.0967 17.6517 20.9301C15.6567 19.5884 12.3667 19.5884 10.3483 20.9301Z" fill="#C1C1C1"/>
            </svg>
            <p className={styles.navLinkText}>{message("frontend.common.club")}</p>
          </Link>

           {/* 타이머 */}
          <Link
            className={clsx(styles.navLink, isTimerActive && styles.navLinkActive)}
            to={BOTTOM_NAV_PATH.timer}
            state={getBottomNavState(pathname, BOTTOM_NAV_PATH.timer)}
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
            state={getBottomNavState(pathname, BOTTOM_NAV_PATH.myPage)}
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
};

export default Navigation;
