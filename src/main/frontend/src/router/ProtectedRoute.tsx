import { message } from "@/app/messages/message";
import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import Loading from "../components/Loading/Loading.tsx";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth.tsx";

/**
 * 로그인 인증이 필요한 화면의 접근 권한을 확인합니다.
 * 인증 확인 중에는 로딩 화면을 보여주고, 인증되지 않은 사용자는 로그인 화면으로 이동시킵니다.
 *
 * @author Hanwon.Jang
 * @param children 인증이 완료된 뒤 렌더링할 보호 대상 화면
 * @return 인증 상태에 맞는 라우트 화면
 */
export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isLoading, isAuthenticated } = useCheckAuth();

  if (isLoading) {
    return <Loading title={message("frontend.common.loginLoading")} />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
