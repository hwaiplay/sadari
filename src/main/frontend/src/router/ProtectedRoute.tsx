import { message } from "@/app/messages/message";
import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import Loading from "../components/Loading/Loading.tsx";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth.tsx";

/**
 * 로그인 인증이 필요한 화면의 접근 권한을 확인합니다.
 * 인증 확인 중에는 로딩 화면을 보여주고, 인증되지 않은 사용자는 로그인 화면으로 이동시킵니다.
 *
 * @author HanWon.Jang
 * @param children 인증이 완료된 뒤 렌더링할 보호 대상 화면
 * @return 인증 상태에 맞는 라우트 화면
 */
export default function ProtectedRoute({ children }: { children: ReactNode }) {

  const location = useLocation();
  const {
    isLoading,
    isAuthenticated,
    isDeletePending,
    isSuspended,
    isOnboardingRequired,
  } = useCheckAuth();

  if (isLoading) {
    return <Loading title={message("frontend.common.loginLoading")} />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // 영구 삭제 대기 회원은 일반 서비스 화면 대신 취소 전용 화면으로 이동합니다
  if (isDeletePending) {
    if (location.pathname !== "/withdrawal/pending") {
      // 영구 삭제 예정일과 취소 버튼만 제공하는 화면으로 이동합니다
      return <Navigate to="/withdrawal/pending" replace />;
    }

    // 영구 삭제 대기 상태는 온보딩 여부보다 우선해 전용 화면만 반환합니다
    return children;
  }

  // 정지 회원은 정지 안내와 영구 탈퇴 화면 외의 서비스 화면에 접근할 수 없습니다.
  if (isSuspended) {
    if (
      location.pathname !== "/suspension"
      && location.pathname !== "/suspension/withdrawal"
    ) {
      return <Navigate to="/suspension" replace />;
    }

    // 관리자 정지 상태는 온보딩 여부보다 우선해 제한된 전용 화면만 반환합니다
    return children;
  }

  // 온보딩을 완료하지 않은 신규 회원은 일반 서비스 화면보다 웰컴 화면을 먼저 확인한다
  if (isOnboardingRequired && location.pathname !== "/welcome") {
    // 닉네임을 확정할 수 있는 최초 로그인 화면으로 이동한다
    return <Navigate to="/welcome" replace />;
  }

  // 온보딩을 이미 완료한 사용자가 웰컴 경로를 직접 열면 홈으로 이동한다
  if (!isOnboardingRequired && location.pathname === "/welcome") {
    // 완료된 웰컴 화면이 다시 노출되지 않도록 홈 화면으로 이동한다
    return <Navigate to="/home" replace />;
  }

  // 인증과 사용자 상태에 맞는 보호 화면을 반환한다
  return children;
}
