/**
 * 공통 헤더, 본문, 하단 네비게이션을 배치하고 페이지 전환 애니메이션을 적용합니다.
 *
 * @author Hanwon.Jang
 */
import Header from "./Header/Header";
import { Outlet, useLocation, useNavigationType } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { vars } from "@/app/styles/tokens.css";
import { Container } from "./Container/Container";
import { clsx } from "clsx";
import { useEffect, useRef } from "react";
import {
  pageTransitionBack,
  pageTransitionBase,
  pageTransitionForward,
  pageTransitionViewport,
} from "./Layout.css";

/**
 * 레이아웃 영역을 렌더링하고 라우터 이동 방향에 맞는 화면 진입 효과를 적용합니다.
 *
 * @author Hanwon.Jang
 * @param isMainLayout 메인 컨테이너와 헤더 여백 적용 여부
 * @return 공통 레이아웃 컴포넌트
 */
function Layout({ isMainLayout = true }: { isMainLayout?: boolean }) {
  const location = useLocation();
  const navigationType = useNavigationType();
  const hasMountedRef = useRef(false);
  const shouldAnimate = hasMountedRef.current;
  const transitionClassName =
    navigationType === "POP" ? pageTransitionBack : pageTransitionForward;

  useEffect(() => {
    hasMountedRef.current = true;
  }, []);

  return (
    <div>
      <Header />
      <main
        style={{
          paddingTop: isMainLayout ? vars.headerHeight : 0,
          paddingBottom: vars.headerHeight,
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
