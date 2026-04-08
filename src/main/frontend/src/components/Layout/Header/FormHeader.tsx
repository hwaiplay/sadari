import { Link, useNavigate } from "react-router-dom";
import { backpageBtn, header } from "./Header.css";
import { Container } from "../Container/Container";
import clsx from "clsx";

/**
 * fileName       : FormHeader
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : form 전용 헤더
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       레이아웃 변경
 */

function FormHeader() {
  const navigate = useNavigate();

  const backPrev = () => {
    navigate(-1); // 이전 페이지로 이동
  };

  return (
    <header>
      <Container className={clsx(header, "_form")}>
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
        <Link to="/">
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
      </Container>
    </header>
  );
}

export default FormHeader;
