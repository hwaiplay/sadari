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
import { useAuthQuery } from "../hooks/useAuthQuery.tsx";

export default function ProtectedRoute({ children }: any) {
  const { data, isLoading, isError } = useAuthQuery();
  // data?.code === 1001 && console.log("인증 실패 코드 1001");

  if (isLoading) {
    return <div>로딩중...!!</div>;
  }

  // if (isError || code === 1001) {
  //   return <Navigate to="/login" replace />;
  // }

  return children;
}
