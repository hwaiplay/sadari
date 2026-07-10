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

/**
 * 로그인, OAuth 등 공개 페이지 접근을 제어하고 이미 인증된 사용자는 홈으로 이동시킨다.
 * @Author Hanwon.Jang
 * @param children 미인증 상태에서 렌더링할 공개 페이지 컴포넌트
 * @return 인증 상태에 따른 공개 라우트 결과
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
