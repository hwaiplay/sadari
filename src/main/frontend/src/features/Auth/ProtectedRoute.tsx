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
import { useAuthQuery } from "./hooks/useAuthQuery.tsx";
import { Navigate } from "react-router-dom";
import Loading from "../../components/Loading/Loading.tsx";

export default function ProtectedRoute({ children }: any) {
  const { isLoading, isError, error } = useAuthQuery();

  if (isLoading) {
    return <Loading />;
  }

  if (isError) {
    const code = (error as Error).message;

    if (code === "1001") {
      alert("인증 실패 code: " + code);
    }

    if (code === "1002") {
      alert("유효하지 않은 토큰 code: " + code);
    }

    if (code === "1003") {
      alert("토큰 만료 code: " + code);
    }

    return <Navigate to="/login" replace />;
  }

  return children;
}
