import Header from "@/components/Layout/Header/Header";
import Layout from "@/components/Layout/Layout";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { useCheckAuth } from "@/features/Auth/hooks/useCheckAuth";
import { clsx } from "clsx";
import { Link, Outlet, useLocation } from "react-router-dom";
import * as styles from "./InquiryLayout.css";

/**
 * 정지 회원이 허용된 화면 사이에서만 이동할 수 있는 하단 내비게이션을 표시합니다.
 *
 * @author SeungHyeon.Kang
 * @return 정지 회원용 문의 내비게이션
 */
function SuspendedInquiryNavigation() {

  const { pathname } = useLocation();
  const isSuspensionActive = pathname === "/suspension";
  const isListActive = pathname === "/inquiry/list"
    || pathname.startsWith("/inquiry/detail/");
  const isWriteActive = pathname === "/inquiry/write";

  return (
    <div className={styles.navShell}>
      <nav className={styles.navigation} aria-label="정지 회원 메뉴">
        <Link
          className={clsx(styles.navLink, isSuspensionActive && styles.navLinkActive)}
          to="/suspension"
          aria-current={isSuspensionActive ? "page" : undefined}
        >
          <svg className={styles.navIcon} viewBox="0 0 28 28" aria-hidden="true">
            <path d="M14 3.5 24 7.7v6.6c0 5.1-3.5 8.9-10 10.7-6.5-1.8-10-5.6-10-10.7V7.7L14 3.5Z" />
            <path d="M14 9v6.2M14 19h.01" />
          </svg>
          <p className={styles.navText}>정지 안내</p>
        </Link>
        <Link
          className={clsx(styles.navLink, isListActive && styles.navLinkActive)}
          to="/inquiry/list"
          aria-current={isListActive ? "page" : undefined}
        >
          <svg className={styles.navIcon} viewBox="0 0 28 28" aria-hidden="true">
            <path d="M6 4.5h16a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-9l-5.5 4v-4H6a2 2 0 0 1-2-2v-11a2 2 0 0 1 2-2Z" />
            <path d="M9 10h10M9 14h7" />
          </svg>
          <p className={styles.navText}>문의 내역</p>
        </Link>
        <Link
          className={clsx(styles.navLink, isWriteActive && styles.navLinkActive)}
          to="/inquiry/write?category=SUSPENSION_APPEAL"
          aria-current={isWriteActive ? "page" : undefined}
        >
          <svg className={styles.navIcon} viewBox="0 0 28 28" aria-hidden="true">
            <path d="M18.8 5.2a2.4 2.4 0 0 1 3.4 3.4L10.4 20.4 5.5 22.5l2.1-4.9L18.8 5.2Z" />
            <path d="m16.8 7.2 4 4" />
          </svg>
          <p className={styles.navText}>문의하기</p>
        </Link>
      </nav>
    </div>
  );
}

/**
 * 일반 회원에게는 서비스 공통 레이아웃을, 정지 회원에게는 제한 레이아웃을 제공합니다.
 *
 * @author SeungHyeon.Kang
 * @return 회원 상태에 맞는 고객문의 레이아웃
 */
function InquiryLayout() {

  const { isLoading, isSuspended } = useCheckAuth();

  if (isLoading) {
    return <Loading title="고객문의 화면을 준비하고 있습니다" />;
  }

  if (!isSuspended) {
    return <Layout />;
  }

  return (
    <div>
      <Header menuEnabled={false} />
      <main className={styles.restrictedMain}>
        <Container>
          <Outlet />
        </Container>
      </main>
      <SuspendedInquiryNavigation />
    </div>
  );
}

export default InquiryLayout;
