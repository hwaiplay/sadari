import React from "react";
import { useCheckAuth } from "../features/Auth/hooks/useCheckAuth";
import Loading from "../components/Loading/Loading";
import { Navigate } from "react-router-dom";

/**
 * fileName       : PublicRoute
 * author         : hanwon.Jang
 * date           : 2026-04-01
 * description    : 인증이 필요 없는 라우트를 보호하는 컴포넌트.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-01       hanwon.Jang       최초 생성
 */

export default function PublicRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isLoading, isAuthenticated } = useCheckAuth();

  if (isLoading) return <Loading title="로딩중" />;

  if (isAuthenticated) {
    return <Navigate to="/home" replace />;
  }

  return children;
}
