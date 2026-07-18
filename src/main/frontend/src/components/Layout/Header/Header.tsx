/**
 * src/main/frontend/src/components/Layout/Header/Header.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { backpageBtn, header, headerHidden, headerShell, logo } from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";
import { useEffect, useRef, useState } from "react";
import HeaderMenuDrawer from "./HeaderMenuDrawer";

const HEADER_SCROLL_DELTA = 4;

function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const isSubPage = location.pathname !== "/home";
  const lastScrollYRef = useRef(0);
  const isHiddenRef = useRef(false);
  const [isHidden, setIsHidden] = useState(false);

  const backPrev = () => {
    navigate(-1);
  };

  useEffect(() => {
    lastScrollYRef.current = window.scrollY;

    /**
     * 작은 스크롤 이동에도 헤더 전체가 반응하도록 스크롤 방향을 기준으로 표시 상태를 전환합니다.
     * 화면 최상단에서는 이전 화면에서 숨김 상태였더라도 헤더를 다시 노출합니다.
     *
     * @author Hanwon.Jang
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

  return (
    <header className={clsx(headerShell, isHidden && headerHidden)}>
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
        <Link to="/" className={logo}>
          <img
            src={"/img/common/logo-upper.svg"}
            alt={message("frontend.common.logoAlt")}
            width={100}
          />
        </Link>
        <HeaderMenuDrawer />
      </Container>
    </header>
  );
}

export default Header;
