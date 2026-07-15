import { Link, useNavigate } from "react-router-dom";
import { backpageBtn, header } from "./Header.css";
import { Container } from "../Container/Container";
import clsx from "clsx";

/**
 * 서브 페이지에서 뒤로가기 버튼과 로고를 표시하는 전용 헤더를 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @return 서브 페이지 헤더 컴포넌트
 */
function SubPageHeader() {
  const navigate = useNavigate();

  /**
   * 브라우저 이전 페이지로 이동합니다.
   *
   * @author Hanwon.Jang
   * @return
   */
  const backPrev = () => {
    navigate(-1);
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
