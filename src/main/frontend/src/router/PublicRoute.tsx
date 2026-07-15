import { message } from "@/app/messages/message";
import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import Loading from "../components/Loading/Loading";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth";

/**
 * 로그인 전용 화면의 접근 권한을 확인합니다.
 * 인증 확인 중에는 로딩 화면을 보여주고, 이미 로그인한 사용자는 홈 화면으로 이동시킵니다.
 *
 * @author Hanwon.Jang
 * @param children 로그인하지 않은 사용자에게 렌더링할 공개 화면
 * @return 인증 상태에 맞는 라우트 화면
 */
export default function PublicRoute({ children }: { children: ReactNode }) {
  const { isLoading, isAuthenticated } = useCheckAuth();

  if (isLoading) {
    return <Loading title={message("frontend.common.loginLoading")} />;
  }

  if (isAuthenticated) {
    return <Navigate to="/home" replace />;
  }

  return children;
}
