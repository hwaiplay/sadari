import { useEffect, useState } from "react";
import { useAuthQuery } from "./useAuthQuery";
import { refreshTokenApi } from "../api/authApi";

/**
 * 인증 상태를 확인하고 만료된 access token은 한 번 refresh 후 재조회한다.
 * @Author Hanwon.Jang
 * @return 로딩 여부와 최종 인증 성공 여부
 */
export const useCheckAuth = () => {
  const { data, isLoading, isError, refetch } = useAuthQuery();
  const [refreshing, setRefreshing] = useState(false);
  const [refreshAttempted, setRefreshAttempted] = useState(false);

  useEffect(() => {
    if (data?.code === 200 && refreshAttempted) {
      setRefreshAttempted(false);
    }
  }, [data?.code, refreshAttempted]);

  useEffect(() => {
    if (data?.code === 1001 && !refreshing && !refreshAttempted) {
      setRefreshing(true);
      setRefreshAttempted(true);

      (async () => {
        try {
          await refreshTokenApi();
          await refetch();
        } catch {
          console.log("token refresh failed");
        } finally {
          setRefreshing(false);
        }
      })();
    }
  }, [data?.code, refreshing, refreshAttempted, refetch]);

  if (isLoading || refreshing) {
    return { isLoading: true, isAuthenticated: false };
  }

  if (isError) {
    return { isLoading: false, isAuthenticated: false };
  }

  if (data) {
    const code = data.code;

    if (code === 200) {
      return { isLoading: false, isAuthenticated: true };
    }

    if (code === 1002 || code === 1003 || refreshAttempted) {
      return { isLoading: false, isAuthenticated: false };
    }
  }

  return { isLoading: false, isAuthenticated: false };
};
