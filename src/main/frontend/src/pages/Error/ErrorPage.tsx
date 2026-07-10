import { Container } from "@/components/Layout/Container/Container";
import { Link } from "react-router-dom";

/**
 * fileName       : Error
 * author         : Hanwon.Jang
 * date           : 2026-05-03
 * description    : 에러 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       Hanwon.Jang       최초 생성
 */

interface ErrorProps {}

/**
 * 잘못된 접근 또는 예외 라우팅 시 표시할 기본 오류 화면을 렌더링한다.
 * @Author Hanwon.Jang
 * @param props 오류 페이지 확장용 props
 * @return 오류 페이지 컴포넌트
 */
const ErrorPage = (props: ErrorProps) => {
  return (
    <Container>
      <h3>잘못된 접근입니다</h3>
      <Link to="/home">메인 페이지로 이동</Link>
    </Container>
  );
};

export default ErrorPage;
