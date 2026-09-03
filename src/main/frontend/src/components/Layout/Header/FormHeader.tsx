import { message } from "@/app/messages/message";
import HomeLink from "@/components/Button/HomeLink/HomeLink";
import { useNavigate } from "react-router-dom";
import { backpageBtn, header, headerShell } from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";
import HeaderMenuDrawer from "./HeaderMenuDrawer";
import { useScrollHeader } from "./useScrollHeader";

/**
 * 서브 페이지에서 뒤로가기 버튼과 로고를 표시하는 전용 헤더를 렌더링함
 *
 * @author SeungHyeon.Kang
 * @return 서브 페이지 헤더 컴포넌트
 */
function SubPageHeader() {

  const navigate = useNavigate();
  // 스크롤 이동량과 같은 거리로 움직일 전용 헤더 상태를 구성함
  const { headerRef } = useScrollHeader();

  /**
   * 브라우저 히스토리를 기반으로 이전 페이지로 이동함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const backPrev = (): void => {

    // 브라우저의 직전 화면으로 이동함
    navigate(-1);
  };

  return (
    /* 등록과 수정 화면의 상단 이동 및 제목 영역 */
    <header
      ref={headerRef}
      className={headerShell}
    >
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
          <HomeLink>
            {/* [주석] 필수 입력 값 누락 시 노출: "서비스 로고 이미지" */}
            <img
                src={"/img/common/logo-b.svg"}
                alt={message("frontend.common.logoAlt")}
                width={100}
            />
          </HomeLink>
          <HeaderMenuDrawer />
        </Container>
      </header>
  );
}

export default SubPageHeader;
