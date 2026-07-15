import { message } from "@/app/messages/message";
import { Link, useNavigate } from "react-router-dom";
import { backpageBtn, header, headerHidden, headerShell } from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";
import { useEffect, useRef, useState } from "react";

const HEADER_SCROLL_DELTA = 4;

/**
 * 서브 페이지에서 뒤로가기 버튼과 로고를 표시하는 전용 헤더를 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @return 서브 페이지 헤더 컴포넌트
 */
function SubPageHeader() {
  const navigate = useNavigate();
  const lastScrollYRef = useRef(0);
  const isHiddenRef = useRef(false);
  const [isHidden, setIsHidden] = useState(false);

  /**
   * 브라우저 히스토리를 기반으로 이전 페이지로 이동합니다.
   *
   * @author Hanwon.Jang
   */
  const backPrev = () => {
    navigate(-1);
  };

  useEffect(() => {
    lastScrollYRef.current = window.scrollY;

    /**
     * 등록/수정 화면의 전용 헤더도 일반 헤더와 동일하게 스크롤 방향에 따라 숨김 상태를 전환합니다.
     * 헤더 배경, 로고, 뒤로가기 버튼이 각각 따로 움직이지 않도록 하나의 래퍼 상태만 변경합니다.
     *
     * @author Hanwon.Jang
     */
    const handleScroll = () => {
      const currentScrollY = window.scrollY;
      const scrollDiff = currentScrollY - lastScrollYRef.current;

      // 모바일 사파리 등의 바운스 백(Bounce-back) 효과로 인해 스크롤 위치가 음수가 되는 경우 헤더 노출을 고정합니다.
      if (currentScrollY <= 0) {
        if (isHiddenRef.current) {
          isHiddenRef.current = false;
          setIsHidden(false);
        }

        lastScrollYRef.current = currentScrollY;
        return;
      }

      // 미세한 스크롤 조작 시 상태가 너무 자주 바뀌어 렌더링 저하가 일어나는 것을 방지하기 위해 임계값(DELTA) 미만은 무시합니다.
      if (Math.abs(scrollDiff) < HEADER_SCROLL_DELTA) {
        return;
      }

      // 스크롤이 아래로 내려가면 헤더를 숨기고(true), 위로 올라가면 다시 헤더를 보여줍니다(false).
      const shouldHide = scrollDiff > 0;

      // 불필요한 상태(State) 업데이트와 렌더링 낭비를 줄이기 위해, 이전 숨김 상태와 실시간 계산된 상태가 다를 때만 갱신합니다.
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
  }, []);

  return (
      <header className={clsx(headerShell, isHidden && headerHidden)}>
        <Container className={clsx(header, "_form")}>
          {/* [주석] 필수 입력 값 누락 시 노출: "이전 페이지로 이동" */}
          <button
              className={backpageBtn}
              type="button"
              aria-label={message("frontend.common.back")}
              onClick={backPrev}
          >
            {/* [주석] 필수 입력 값 누락 시 노출: "뒤로가기 아이콘" */}
            <img
                src={"/img/common/icon-backpage.svg"}
                alt={message("frontend.common.backIconAlt")}
            />
          </button>
          <Link to="/">
            {/* [주석] 필수 입력 값 누락 시 노출: "서비스 로고 이미지" */}
            <img
                src={"/img/common/logo-b.svg"}
                alt={message("frontend.common.logoAlt")}
                width={100}
            />
          </Link>
        </Container>
      </header>
  );
}

export default SubPageHeader;