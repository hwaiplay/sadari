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
  pageTransitionBack,
  pageTransitionBase,
  pageTransitionForward,
  pageTransitionViewport,
} from "./Layout.css";

type LayoutProps = {
  isMainLayout?: boolean;
};

/**
 * 레이아웃 영역을 렌더링하고 라우터 이동 방향에 맞는 화면 진입 효과를 적용합니다.
 *
 * @author HanWon.Jang
 * @param props 레이아웃 표시 옵션
 * @return 공통 레이아웃 컴포넌트
 */
function Layout({ isMainLayout = true }: LayoutProps) {

  const location = useLocation();
  const navigationType = useNavigationType();
  const hasMountedRef = useRef(false);
  const layoutRef = useRef<HTMLDivElement | null>(null);
  const shouldAnimate = hasMountedRef.current;
  const transitionClassName =
    navigationType === "POP" ? pageTransitionBack : pageTransitionForward;

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
}

export default Layout;
