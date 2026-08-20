/**
 * src/main/frontend/src/components/Layout/Header/Header.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author SeungHyeon.Kang
 */
import { message } from "@/app/messages/message";
import {
  useLocation,
  useNavigate,
  useNavigationType,
} from "react-router-dom";
import HomeLink from "@/components/Button/HomeLink/HomeLink";
import {
  backpageBtn,
  header,
  headerCenter,
  headerContentSlideBack,
  headerContentSlideForward,
  headerHidden,
  headerRouteTitle,
  headerRouteTitleWithBack,
  headerShell,
  logo,
  routeTitle,
} from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";
import { useEffect, useRef, useState } from "react";
import HeaderMenuDrawer from "./HeaderMenuDrawer";
import type { UserMenuItem } from "@/features/Menu/api/userMenuApi";
import { useUserMenuQuery } from "@/features/Menu/hooks/useUserMenuQuery";
import { BOTTOM_NAV_PATH } from "@/app/navigation/bottomNavigation";

const HEADER_SCROLL_DELTA = 4;

type HeaderMenuTransitionDirection = "forward" | "back";

type ResolvedHeaderMenu = {
  pathname: string;
  currentMenu: UserMenuItem | null;
  menuList: UserMenuItem[];
  transitionDirection: HeaderMenuTransitionDirection;
};

type HeaderProps = {
  menuEnabled?: boolean;
  onHiddenChange?: (isHidden: boolean) => void;
};

/**
 * Header 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @param props 메뉴 사용 여부와 헤더 숨김 상태 전달 함수
 * @return 구성된 화면 요소
 */
function Header({ menuEnabled = true, onHiddenChange }: HeaderProps) {

  const location = useLocation();
  const navigate = useNavigate();
  const navigationType = useNavigationType();
  const isHomeRoute = location.pathname === BOTTOM_NAV_PATH.home;
  const isBottomNavRoot =
    isHomeRoute
    || location.pathname === BOTTOM_NAV_PATH.feed
    || location.pathname === BOTTOM_NAV_PATH.timer
    || location.pathname === BOTTOM_NAV_PATH.myPage;
  const hasBackButton = !isBottomNavRoot;
  const lastScrollYRef = useRef(0);
  const isHiddenRef = useRef(false);
  const hasResolvedMenuRef = useRef(false);
  const [isHidden, setIsHidden] = useState(false);
  const [resolvedMenu, setResolvedMenu] = useState<ResolvedHeaderMenu | null>(
    null,
  );
  // 로딩 화면과 같은 Query Key를 사용하여 경로별 메뉴 조회 요청을 공유한다
  const {
    data: userMenuData,
    isError: isUserMenuError,
  } = useUserMenuQuery(location.pathname, menuEnabled);
  const isMenuResolved = resolvedMenu?.pathname === location.pathname;
  const currentMenu = isMenuResolved ? resolvedMenu.currentMenu : null;
  const menuList = isMenuResolved ? resolvedMenu.menuList : [];
  const headerContentSlide =
    resolvedMenu?.transitionDirection === "back"
      ? headerContentSlideBack
      : headerContentSlideForward;

  /**
   * back Prev 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const backPrev = () => {

    navigate(-1);
  };

  useEffect(() => {

    lastScrollYRef.current = window.scrollY;

    /**
     * 작은 스크롤 이동에도 헤더 전체가 반응하도록 스크롤 방향을 기준으로 표시 상태를 전환합니다.
     * 화면 최상단에서는 이전 화면에서 숨김 상태였더라도 헤더를 다시 노출합니다.
     *
     * @author HanWon.Jang
     * @return
     */
    const handleScroll = () => {

      const currentScrollY = window.scrollY;
      const scrollDiff = currentScrollY - lastScrollYRef.current;

      // 최상단에서는 사용자가 길을 잃지 않도록 헤더를 항상 보여줍니다.
      if (currentScrollY <= 0) {
        if (isHiddenRef.current) {
          isHiddenRef.current = false;
          // 헤더를 화면에 다시 표시한다
          setIsHidden(false);
          // 현재 페이지의 고정 영역이 헤더 아래로 이동하도록 노출 상태를 전달한다
          onHiddenChange?.(false);
        }

        lastScrollYRef.current = currentScrollY;
        return;
      }

      // 아주 미세한 흔들림은 무시하고, 의도된 스크롤 방향 변화에만 헤더를 움직입니다.
      if (Math.abs(scrollDiff) < HEADER_SCROLL_DELTA) {
        return;
      }

      const shouldHide = scrollDiff > 0;

      if (isHiddenRef.current !== shouldHide) {
        isHiddenRef.current = shouldHide;
        // 스크롤 방향에 맞는 헤더 표시 상태를 적용한다
        setIsHidden(shouldHide);
        // 현재 페이지의 고정 영역이 헤더 위치 변화에 맞춰 이동하도록 상태를 전달한다
        onHiddenChange?.(shouldHide);
      }

      lastScrollYRef.current = currentScrollY;
    };

    window.addEventListener("scroll", handleScroll, { passive: true });

    return () => {

      window.removeEventListener("scroll", handleScroll);
    };
  }, [location.pathname, onHiddenChange]);

  useEffect(() => {

    const transitionDirection =
      navigationType === "POP" && hasResolvedMenuRef.current
        ? "back"
        : "forward";

    // 메뉴가 비활성화된 레이아웃은 조회 결과 없이 헤더 표시 상태만 확정한다
    if (!menuEnabled) {
      // 메뉴를 사용하지 않는 경로도 헤더 콘텐츠 표시를 시작할 수 있게 확정한다
      hasResolvedMenuRef.current = true;
      // 현재 경로에 메뉴가 없는 헤더 상태를 저장한다
      setResolvedMenu({
        pathname: location.pathname,
        currentMenu: null,
        menuList: [],
        transitionDirection,
      });
      // 비활성화된 메뉴의 추가 처리를 중단한다
      return;
    }

    // 메뉴 API가 아직 진행 중이면 이전 경로의 메뉴를 현재 경로에 표시하지 않는다
    if (!userMenuData && !isUserMenuError) {
      // 현재 경로의 메뉴 조회가 확정될 때까지 상태 반영을 보류한다
      return;
    }

    // 현재 경로의 메뉴명 유무가 확정된 뒤 헤더 중앙 콘텐츠를 한 번에 표시한다
    hasResolvedMenuRef.current = true;
    // 메뉴 조회 실패도 빈 메뉴 상태로 확정하여 화면 진입을 막지 않는다
    setResolvedMenu({
      pathname: location.pathname,
      currentMenu: userMenuData?.currentMenu ?? null,
      menuList: userMenuData?.menuList ?? [],
      transitionDirection,
    });
  }, [
    isUserMenuError,
    location.pathname,
    menuEnabled,
    navigationType,
    userMenuData,
  ]);

  return (
    /* 사용자 화면의 이전 이동과 현재 메뉴 표시 영역 */
    <header
      className={clsx(
        headerShell,
        isHidden && headerHidden,
      )}
    >
      <Container className={clsx(header, hasBackButton && "_sub")}>
        {hasBackButton && (
          <button
            className={backpageBtn}
            type="button"
            aria-label={message("frontend.common.back")}
            onClick={backPrev}
          >
            <img
              src={"/img/common/icon-backpage.svg"}
              alt={message("frontend.common.backIconAlt")}
            />
          </button>
        )}
        {/* 홈 로고와 메뉴명은 왼쪽에 표시하고 다른 경로의 대체 로고는 중앙에 표시하는 영역 */}
        <div
          className={clsx(
            headerCenter,
            (isHomeRoute || currentMenu?.menuName) && headerRouteTitle,
            currentMenu?.menuName && hasBackButton && headerRouteTitleWithBack,
          )}
        >
          {/* 홈 화면은 메뉴 조회 결과와 관계없이 왼쪽에 서비스 로고를 표시하는 영역 */}
          {isMenuResolved &&
            (isHomeRoute || !currentMenu?.menuName ? (
              <HomeLink className={clsx(logo, headerContentSlide)}>
                <img
                  src={"/img/common/logo-upper.svg"}
                  alt={message("frontend.common.logoAlt")}
                  width={100}
                />
              </HomeLink>
            ) : (
              <h1 className={clsx(routeTitle, headerContentSlide)}>
                {currentMenu.menuName}
              </h1>
            ))}
        </div>
        {menuEnabled && <HeaderMenuDrawer menuList={menuList} />}
      </Container>
    </header>
  );
}

export default Header;
