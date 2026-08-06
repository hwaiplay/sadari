/**
 * src/main/frontend/src/components/Layout/Header/Header.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
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
  headerShell,
  logo,
  routeTitle,
} from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";
import { useEffect, useRef, useState } from "react";
import HeaderMenuDrawer from "./HeaderMenuDrawer";
import {
  getUserMenuApi,
  type UserMenuItem,
} from "@/features/Menu/api/userMenuApi";

const HEADER_SCROLL_DELTA = 4;

type HeaderMenuTransitionDirection = "forward" | "back";

type ResolvedHeaderMenu = {
  pathname: string;
  currentMenu: UserMenuItem | null;
  menuList: UserMenuItem[];
  transitionDirection: HeaderMenuTransitionDirection;
};

/**
 * Header 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function Header() {

  const location = useLocation();
  const navigate = useNavigate();
  const navigationType = useNavigationType();
  const isSubPage = location.pathname !== "/home";
  const lastScrollYRef = useRef(0);
  const isHiddenRef = useRef(false);
  const hasResolvedMenuRef = useRef(false);
  const [isHidden, setIsHidden] = useState(false);
  const [resolvedMenu, setResolvedMenu] = useState<ResolvedHeaderMenu | null>(
    null,
  );
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
          setIsHidden(false);
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
        setIsHidden(shouldHide);
      }

      lastScrollYRef.current = currentScrollY;
    };

    window.addEventListener("scroll", handleScroll, { passive: true });

    return () => {

      window.removeEventListener("scroll", handleScroll);
    };
  }, [location.pathname]);

  useEffect(() => {

    let ignore = false;
    const transitionDirection =
      navigationType === "POP" && hasResolvedMenuRef.current
        ? "back"
        : "forward";

    getUserMenuApi(location.pathname)
      .then((response) => {

        if (ignore) {
          return;
        }

        // 현재 경로의 메뉴명 유무가 확정된 뒤 헤더 중앙 콘텐츠를 한 번에 표시한다
        hasResolvedMenuRef.current = true;
        setResolvedMenu({
          pathname: location.pathname,
          currentMenu: response.data?.currentMenu ?? null,
          menuList: response.data?.menuList ?? [],
          transitionDirection,
        });
      })
      .catch(() => {

        if (!ignore) {
          // 메뉴 조회 실패는 화면 진입을 막지 않고 기존 로고와 빈 햄버거 목록으로 대체한다.
          hasResolvedMenuRef.current = true;
          setResolvedMenu({
            pathname: location.pathname,
            currentMenu: null,
            menuList: [],
            transitionDirection,
          });
        }
      });

    return () => {

      ignore = true;
    };
  }, [location.pathname, navigationType]);

  return (
    /* 사용자 화면의 이전 이동과 현재 메뉴 표시 영역 */
    <header
      className={clsx(
        headerShell,
        isHidden && headerHidden,
      )}
    >
      <Container className={clsx(header, isSubPage && "_sub")}>
        {isSubPage && (
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
        {/* 현재 경로의 메뉴 조회가 끝난 뒤 메뉴명 또는 로고를 표시하는 중앙 영역 */}
        <div className={headerCenter}>
          {isMenuResolved &&
            (currentMenu?.menuName ? (
              <h1 className={clsx(routeTitle, headerContentSlide)}>
                {currentMenu.menuName}
              </h1>
            ) : (
              <HomeLink className={clsx(logo, headerContentSlide)}>
                <img
                  src={"/img/common/logo-upper.svg"}
                  alt={message("frontend.common.logoAlt")}
                  width={100}
                />
              </HomeLink>
            ))}
        </div>
        <HeaderMenuDrawer menuList={menuList} />
      </Container>
    </header>
  );
}

export default Header;
