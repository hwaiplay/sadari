import { Link, useNavigate } from "react-router-dom";
import { backpageBtn, header, logo } from "./Header.css";
import { Container } from "../Container/Container";
import { useLocation } from "react-router-dom";
import clsx from "clsx";

/**
 * fileName       : Header
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : 헤더 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       기록 페이지 감지 추가
 */

function Header() {
  const location = useLocation();
  const navigate = useNavigate();

  // 기록 페이지인지 여부
  const isSetPage = location.pathname === "/add";

  // 이전페이지로 이동 클릭 함수
  const backPrev = () => {
    navigate(-1);
  };

  return (
    <header>
      <Container className={clsx(header, isSetPage && "_form")}>
        {isSetPage && (
          <button
            className={backpageBtn}
            type="button"
            aria-label="이전 페이지로 돌아가기"
            onClick={backPrev}
          >
            <img
              src={"/img/common/icon-backpage.svg"}
              alt="뒤로가기 화살표 아이콘"
            />
          </button>
        )}
        <Link to="/" className={logo}>
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
      </Container>
    </header>
  );
}

export default Header;
