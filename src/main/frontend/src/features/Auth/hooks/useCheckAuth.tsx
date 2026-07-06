import { useEffect, useState } from "react";
import { useAuthQuery } from "./useAuthQuery";
import { refreshTokenApi } from "../api/authApi";

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
