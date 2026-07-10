import { Link, useNavigate } from "react-router-dom";
import { backpageBtn, header } from "./Header.css";
import { Container } from "../Container/Container";
import clsx from "clsx";

/**
 * fileName       : SubPageHeader
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : 서브페이지용 헤더
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       레이아웃 변경
 * 2026-05-06       hanwon.Jang       서브페이지용으로 수정
 */

/**
 * 서브 페이지에서 뒤로가기 버튼과 로고를 표시하는 전용 헤더를 렌더링한다.
 * @Author Hanwon.Jang
 * @return 서브 페이지 헤더 컴포넌트
 */
function SubPageHeader() {
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

export default SubPageHeader;
