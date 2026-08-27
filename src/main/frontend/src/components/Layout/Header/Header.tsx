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
import { useScrollHeader } from "./useScrollHeader";

/**
 * fileName       : Header
 * author         : SeungHyeon.Kang
 * date           : 2026-08-27
 * description    : 공용 헤더
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 */

type HeaderMenuTransitionDirection = "forward" | "back";

type ResolvedHeaderMenu = {
  pathname: string;
  currentMenu: UserMenuItem | null;
  menuList: UserMenuItem[];
  transitionDirection: HeaderMenuTransitionDirection;
};

type HeaderProps = {
  menuEnabled?: boolean;
  onOffsetChange?: (headerOffset: number) => void;
};

function Header({ menuEnabled = true, onOffsetChange }: HeaderProps) {

  const location = useLocation();
  const navigate = useNavigate();
  const navigationType = useNavigationType();
  const isHomeRoute = location.pathname === BOTTOM_NAV_PATH.home;

  // 현재 페이지가 바텀 네비게이션 메뉴인지 확인
  const isBottomNavRoot =
    isHomeRoute
    || location.pathname === BOTTOM_NAV_PATH.feed
    || location.pathname === BOTTOM_NAV_PATH.timer
    || location.pathname === BOTTOM_NAV_PATH.myPage
    || location.pathname === BOTTOM_NAV_PATH.club;

  // 뒤로가기 버튼 표시 상태
  const hasBackButton = !isBottomNavRoot;

  const hasResolvedMenuRef = useRef(false);

  const [resolvedMenu, setResolvedMenu] = useState<ResolvedHeaderMenu | null>(
    null,
  );

  // 스크롤 이동량과 같은 거리로 움직일 헤더 상태를 구성
  const { headerRef } = useScrollHeader(
    onOffsetChange,
    location.pathname,
  );

  // 로딩 화면과 같은 Query Key를 사용하여 경로별 메뉴 조회 요청을 공유
  const {
    data: userMenuData,
    isError: isUserMenuError,
  } = useUserMenuQuery(location.pathname, menuEnabled);

  const isMenuResolved = resolvedMenu?.pathname === location.pathname;
  const currentMenu = isMenuResolved ? resolvedMenu.currentMenu : null;
  const menuList = isMenuResolved ? resolvedMenu.menuList : [];
  const currentRouteTitle = currentMenu?.menuName;
  const headerContentSlide =
    resolvedMenu?.transitionDirection === "back"
      ? headerContentSlideBack
      : headerContentSlideForward;

  /**
   * back Prev 사용자 동작을 처리
   *
   * @author HanWon.Jang
   * @return
   */
  const backPrev = () => {

    navigate(-1);
  };

  useEffect(() => {

    const transitionDirection =
      navigationType === "POP" && hasResolvedMenuRef.current
        ? "back"
        : "forward";

    // 메뉴가 비활성화된 레이아웃은 조회 결과 없이 헤더 표시 상태만 확정
    if (!menuEnabled) {
      // 메뉴를 사용하지 않는 경로도 헤더 콘텐츠 표시를 시작할 수 있게 확정
      hasResolvedMenuRef.current = true;
      // 현재 경로에 메뉴가 없는 헤더 상태를 저장
      setResolvedMenu({
        pathname: location.pathname,
        currentMenu: null,
        menuList: [],
        transitionDirection,
      });
      // 비활성화된 메뉴의 추가 처리를 중단
      return;
    }

    // 메뉴 API가 아직 진행 중이면 이전 경로의 메뉴를 현재 경로에 표시하지 않음
    if (!userMenuData && !isUserMenuError) {
      // 현재 경로의 메뉴 조회가 확정될 때까지 상태 반영을 보류
      return;
    }

    // 현재 경로의 메뉴명 유무가 확정된 뒤 헤더 중앙 콘텐츠를 한 번에 표시
    hasResolvedMenuRef.current = true;
    // 메뉴 조회 실패도 빈 메뉴 상태로 확정하여 화면 진입을 막지 않음
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
      ref={headerRef}
      className={headerShell}
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
              width={"100%"}
            />
          </button>
        )}
        {/* 홈 로고와 메뉴명은 왼쪽에 표시하고 다른 경로의 대체 로고는 중앙에 표시하는 영역 */}
        <div
          className={clsx(
            headerCenter,
            (isHomeRoute || currentRouteTitle) && headerRouteTitle,
            currentRouteTitle
              && hasBackButton
              && headerRouteTitleWithBack,
          )}
        >
          {/* 홈 화면은 메뉴 조회 결과와 관계없이 왼쪽에 서비스 로고를 표시하는 영역 */}
          {isMenuResolved &&
            (isHomeRoute || !currentRouteTitle ? (
              <HomeLink className={clsx(logo, headerContentSlide)}>
                <img
                  src={"/img/common/logo-upper.svg"}
                  alt={message("frontend.common.logoAlt")}
                  width={100}
                />
              </HomeLink>
            ) : (
              <h1 className={clsx(routeTitle, headerContentSlide)}>
                {currentRouteTitle}
              </h1>
            ))}
        </div>
        {menuEnabled && <HeaderMenuDrawer menuList={menuList} />}
      </Container>
    </header>
  );
}

export default Header;
