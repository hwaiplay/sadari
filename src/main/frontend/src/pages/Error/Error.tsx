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

import { Container } from "@/components/Layout/Container/Container";

interface ErrorProps {}

const Error = (props: ErrorProps) => {
  return <Container>잘못된 접근입니다</Container>;
};

export default Error;
