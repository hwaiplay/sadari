/**
 * fileName       : useCheckAuth
 * author         : hanwon.Jang
 * date           : 2026-03-28
 * description    : 로그인 상태 확인 후 필요 시 토큰 갱신 및 페이지 이동을 결정하는 커스텀 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-28       hanwon.Jang       주석 추가 및 리팩토링
 */

import { useEffect, useState } from "react";
import { useAuthQuery } from "./useAuthQuery";
import { refreshToken } from "../api/authApi";

export const useCheckAuth = () => {
  const { data, isLoading, isError, refetch } = useAuthQuery(); // 로그인 상태 확인 (/api/oauth/tokenCheck)
  const [refreshing, setRefreshing] = useState(false); // 토큰 갱신 중인지 여부 상태

  useEffect(() => {
    // 토큰 만료 코드 (1001) + 현재 토큰 갱신 중이 아닐 때 → 토큰 갱신 시도
    if (data?.code === 1001 && !refreshing) {
      setRefreshing(true);
      (async () => {
        try {
          await refreshToken(); // 토큰 갱신 API 호출
          await refetch(); // 재인증
        } catch {
          alert("세션이 만료되었습니다. 다시 로그인해주세요.");
          return { status: "unauthenticated" };
        } finally {
          setRefreshing(false);
        }
      })();
    }
  }, [data?.code, refreshing]);

  if (isLoading || refreshing)
    return { isLoading: true, navigateTo: null, showChildren: false };

  if (isError)
    return { isLoading: false, navigateTo: "/login", showChildren: false };

  if (data) {
    const code = data.code; // 응답 코드

    if (code === 200)
      return { isLoading: false, navigateTo: null, showChildren: true };
    if (code === 1002 || code === 1003)
      return { isLoading: false, navigateTo: "/login", showChildren: false };
  }

  return { isLoading: false, navigateTo: "/login", showChildren: false };
};
