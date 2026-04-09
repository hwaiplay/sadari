import { ReactNode, useEffect } from "react";
/**
 * fileName       : ProtectRoute
 * author         : hanwon.Jang
 * date           : 2026-03-24
 * description    : 인증이 필요한 라우트를 보호하는 컴포넌트. 로그인 인증된 사용자만 페이지 접근 허용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-24       hanwon.Jang       최초 생성
 */
import { Navigate } from "react-router-dom";
import Loading from "../components/Loading/Loading.tsx";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth.tsx";

export default function ProtectedRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isLoading, isAuthenticated } = useCheckAuth();

  if (isLoading) return <Loading title="로딩중" />;

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
