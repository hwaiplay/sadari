import Layout from "@/components/Layout/Layout";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { useCheckAuth } from "@/features/Auth/hooks/useCheckAuth";
import { Outlet } from "react-router-dom";
import * as styles from "./InquiryLayout.css";

/**
 * 일반 회원에게는 서비스 공통 레이아웃을, 정지 회원에게는 제한 레이아웃을 제공합니다.
 *
 * @author SeungHyeon.Kang
 * @return 회원 상태에 맞는 고객문의 레이아웃
 */
function InquiryLayout() {

  const { isLoading, isSuspended } = useCheckAuth();

  if (isLoading) {
    return <Loading />;
  }

  if (!isSuspended) {
    return <Layout />;
  }

  return (
    <main className={styles.restrictedMain}>
      <Container>
        <Outlet />
      </Container>
    </main>
  );
}

export default InquiryLayout;
