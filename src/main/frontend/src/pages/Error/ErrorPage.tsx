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

const ErrorPage = (props: ErrorProps) => {
  return (
    <Container>
      <h3>잘못된 접근입니다</h3>
      <Link to="/home">메인 페이지로 이동</Link>
    </Container>
  );
};

export default ErrorPage;
