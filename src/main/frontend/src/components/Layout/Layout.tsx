/**
 * 공통 헤더, 본문, 하단 네비게이션을 배치하고 페이지 전환 애니메이션을 적용합니다.
 *
 * @author HanWon.Jang
 */
import Header from "./Header/Header";
import { Outlet, useLocation, useNavigationType } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { vars } from "@/app/styles/tokens.css";
import { Container } from "./Container/Container";
import { clsx } from "clsx";
import { useCallback, useEffect, useRef } from "react";
import {
  getBottomNavDirection,
  type BottomNavDirection,
} from "@/app/navigation/bottomNavigation";
import {
  pageTransitionBack,
  pageTransitionBase,
  pageTransitionForward,
  pageTransitionViewport,
} from "./Layout.css";

type LayoutProps = {
  isMainLayout?: boolean;
};

/**
 * 하단 탭 전용 방향이 없을 때 기존 라우터 이력 기반 화면 전환을 유지한다
 *
 * @author HanWon.Jang
 * @param bottomNavDirection 검증된 하단 탭 진입 방향
 * @param isHistoryPop 브라우저 이력 탐색으로 이동했는지 여부
 * @return 현재 화면에 적용할 기존 페이지 전환 클래스
 */
const getTransitionClass = (bottomNavDirection: BottomNavDirection | null, isHistoryPop: boolean): string => {

  // 왼쪽 탭으로 이동하면 기존 역방향 진입 클래스를 사용한다
  if (bottomNavDirection === "back") {
    // 화면이 왼쪽에서 진입하는 기존 클래스를 반환한다
    return pageTransitionBack;
  }

  // 오른쪽 탭으로 이동하면 기존 정방향 진입 클래스를 사용한다
  if (bottomNavDirection === "forward") {
    // 화면이 오른쪽에서 진입하는 기존 클래스를 반환한다
    return pageTransitionForward;
  }

  // 하단 탭 외의 브라우저 이력 이동은 기존 역방향 정책을 유지한다
  if (isHistoryPop) {
    // 기존 POP 화면 전환 클래스를 반환한다
    return pageTransitionBack;
  }

  // 일반 링크와 프로그램 이동은 기존 정방향 정책을 유지한다
  return pageTransitionForward;
};

/**
 * 레이아웃 영역을 렌더링하고 라우터 이동 방향에 맞는 화면 진입 효과를 적용합니다.
 *
 * @author HanWon.Jang
 * @param props 레이아웃 표시 옵션
 * @return 공통 레이아웃 컴포넌트
 */
const Layout = ({ isMainLayout = true }: LayoutProps) => {

  const location = useLocation();
  const navigationType = useNavigationType();
  const hasMountedRef = useRef(false);
  const layoutRef = useRef<HTMLDivElement | null>(null);
  // 하단 탭 위치 상태가 현재 목적지와 일치할 때만 전용 방향을 조회한다
  const bottomNavDirection = getBottomNavDirection(
    location.state,
    location.pathname,
    navigationType === "POP",
  );
  const shouldAnimate = hasMountedRef.current || bottomNavDirection !== null;
  // 하단 탭 전용 방향이 없으면 기존 PUSH 및 POP 전환 클래스를 선택한다
  const transitionClassName = getTransitionClass(
    bottomNavDirection,
    navigationType === "POP",
  );

  useEffect(() => {

    hasMountedRef.current = true;
  }, []);

  /**
   * 헤더 아래 고정 영역이 스크롤 중 헤더와 같은 속도로 이동하게 거리를 적용한다.
   *
   * @author SeungHyeon.Kang
   * @param headerOffset 헤더가 화면 위로 이동한 거리
   * @return 반환값이 없다
   */
  const handleHeaderOffsetChange = useCallback((headerOffset: number): void => {

    // 레이아웃 요소가 준비되지 않았으면 위치 갱신을 종료한다
    if (!layoutRef.current) {
      // 헤더 아래 영역의 현재 위치를 유지한다
      return;
    }

    // 하위 고정 영역이 공유할 헤더 이동 거리 CSS 변수를 갱신한다
    layoutRef.current.style.setProperty(
      "--header-scroll-offset",
      `${headerOffset}px`,
    );
  }, []);

  return (
    <div ref={layoutRef}>
      <Header onOffsetChange={handleHeaderOffsetChange} />
      {/* 현재 경로에 연결된 페이지 표시 영역 */}
      <main
        style={{
          paddingTop: isMainLayout ? vars.headerHeight : 0,
          paddingBottom: `calc(${vars.navHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
        }}
      >
        <div className={pageTransitionViewport}>
          <div
            key={location.key}
            className={clsx(
              pageTransitionBase,
              shouldAnimate && transitionClassName,
            )}
          >
            {isMainLayout ? (
              <Container>
                <Outlet />
              </Container>
            ) : (
              <Outlet />
            )}
          </div>
        </div>
      </main>
      <Navigation isMain={isMainLayout} />
    </div>
  );
};

export default Layout;
