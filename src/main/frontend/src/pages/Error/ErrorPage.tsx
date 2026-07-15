import { Container } from "@/components/Layout/Container/Container";
import { Link } from "react-router-dom";

interface ErrorProps {}

/**
 * 잘못된 접근 또는 예외 라우팅 시 표시할 기본 오류 화면을 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @param props 오류 페이지 확장용 props
 * @return 오류 페이지 컴포넌트
 */
const ErrorPage = (props: ErrorProps) => {
  return (
    <Container>
      <h3>잘못된 접근입니다.</h3>
      <Link to="/home">메인 페이지로 이동</Link>
    </Container>
  );
};

export default ErrorPage;
