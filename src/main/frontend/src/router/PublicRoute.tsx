import { message } from "@/app/messages/message";
import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import Loading from "../components/Loading/Loading";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth";

/**
 * 로그인 전용 화면의 접근 권한을 확인함
 * 인증 확인 중에는 로딩 화면을 보여주고, 이미 로그인한 사용자는 홈 화면으로 이동시킴
 *
 * @author HanWon.Jang
 * @param children 로그인하지 않은 사용자에게 렌더링할 공개 화면
 * @return 인증 상태에 맞는 라우트 화면
 */
export default function PublicRoute({ children }: { children: ReactNode }) {

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

  if (isAuthenticated) {
    if (isDeletePending) {
      return <Navigate to="/withdrawal/pending" replace />;
    }

    if (isSuspended) {
      return <Navigate to="/suspension" replace />;
    }

    // 최초 로그인 상태면 공개 화면보다 웰컴 화면을 먼저 제공함
    return <Navigate to={isOnboardingRequired ? "/welcome" : "/home"} replace />;
  }

  // 로그인하지 않은 사용자가 요청한 공개 화면을 반환함
  return children;
}
