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
import { useAuthQuery } from "../../hooks/useAuthQuery.tsx";
import { Navigate } from "react-router-dom";
import Loading from "../../components/Loading/Loading.tsx";

export default function ProtectedRoute({ children }: any) {
  const { data, isLoading, isError } = useAuthQuery();

  if (isLoading) {
    return <Loading />;
  }

  // console.log("인증 결과:", data, "에러 여부:", isError);

  if (isError) {
    alert("에러발생");

    return <Navigate to="/login" replace />;
  }

  if (data?.code === 1001 || data?.code === 1002) {
    alert(`카카오 로그인 필요 ${data.code}`);

    return <Navigate to="/login" replace />;
  }

  if (data?.code === 1003) {
    alert(`토큰 만료 ${data.code}`);

    // refreshToken으로 토큰 재발급 시도
    return <Navigate to="/api/oauth/refresh" replace />;
  }

  if (data?.code === 1004) {
    alert(`관리자 권한 ${data.code}`);

    return <Navigate to="/api/oauth/refresh" replace />;
  }

  return children;
}
