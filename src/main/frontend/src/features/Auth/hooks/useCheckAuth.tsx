import { useEffect, useState } from "react";
import { useAuthQuery } from "./useAuthQuery";
import { refreshToken } from "../api/authApi";

export const useCheckAuth = () => {
  const { data, isLoading, isError, refetch } = useAuthQuery();
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (data?.code === 1001 && !refreshing) {
      setRefreshing(true);
      (async () => {
        try {
          await refreshToken();
          await refetch(); // 토큰 갱신 후 재조회
        } catch {
          window.location.href = "/login"; // refresh 실패 → 로그인
        } finally {
          setRefreshing(false);
        }
      })();
    }
  }, [data, refreshing, refetch]);

  if (isLoading || refreshing)
    return { isLoading: true, navigateTo: null, showChildren: false };
  if (isError)
    return { isLoading: false, navigateTo: "/login", showChildren: false };

  if (data) {
    const code = data.code;
    if (code === 200)
      return { isLoading: false, navigateTo: null, showChildren: true };
    if (code === 1002 || code === 1003)
      return { isLoading: false, navigateTo: "/login", showChildren: false };
  }

  return { isLoading: false, navigateTo: "/login", showChildren: false };
};
