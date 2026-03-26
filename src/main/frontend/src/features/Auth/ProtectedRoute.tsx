import { ReactNode, useEffect } from "react";
/**
 * fileName       : ProtectRoute
 * author         : hanwon.Jang
 * date           : 2026-03-24
 * description    : 인증이 필요한 라우트를 보호하는 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-24       hanwon.Jang       최초 생성
 */
import { Navigate } from "react-router-dom";
import Loading from "../../components/Loading/Loading.tsx";
import { useCheckAuth } from "./hooks/useCheckAuth.tsx";

export default function ProtectedRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isLoading, navigateTo, showChildren } = useCheckAuth();

  if (isLoading) return <Loading />;

  if (navigateTo) return <Navigate to={navigateTo} replace />;

  if (showChildren) return children;

  return <Navigate to="/login" replace />; // fallback
}
