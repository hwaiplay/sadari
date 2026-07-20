/**
 * src/main/frontend/src/features/Auth/hooks/useCheckAuth.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useEffect, useState } from "react";
import { useAuthQuery } from "./useAuthQuery";
import { refreshTokenApi } from "../api/authApi";
import { ResultDataError } from "@/app/api/resultData";

const AUTH_FAIL_CODE = 1001;
const TOKEN_INVALID_CODE = 1002;
const TOKEN_EXPIRED_CODE = 1003;
const REFRESHABLE_AUTH_CODES = new Set([
  AUTH_FAIL_CODE,
  TOKEN_INVALID_CODE,
  TOKEN_EXPIRED_CODE,
]);

export const useCheckAuth = () => {
  const { data, error, isLoading, isError, refetch } = useAuthQuery();
  const [refreshing, setRefreshing] = useState(false);
  const [refreshAttempted, setRefreshAttempted] = useState(false);
  const errorCode = error instanceof ResultDataError
    ? Number(error.result.code)
    : undefined;

  useEffect(() => {
    if (data?.code === 200 && refreshAttempted) {
      setRefreshAttempted(false);
    }
  }, [data?.code, refreshAttempted]);

  useEffect(() => {
    if (
      errorCode &&
      REFRESHABLE_AUTH_CODES.has(errorCode) &&
      !refreshing &&
      !refreshAttempted
    ) {
      setRefreshing(true);
      setRefreshAttempted(true);

      (async () => {
        try {
          // accessToken 만료/누락/검증 실패는 refreshToken으로 복구 가능한 상태일 수 있어 먼저 재발급을 시도한다.
          await refreshTokenApi();
          await refetch();
        } catch {
          console.log("token refresh failed");
        } finally {
          setRefreshing(false);
        }
      })();
    }
  }, [errorCode, refreshing, refreshAttempted, refetch]);

  if (isLoading || refreshing) {
    return { isLoading: true, isAuthenticated: false };
  }

  if (isError) {
    if (
      errorCode === TOKEN_INVALID_CODE ||
      errorCode === TOKEN_EXPIRED_CODE ||
      refreshAttempted
    ) {
      return { isLoading: false, isAuthenticated: false };
    }

    return { isLoading: false, isAuthenticated: false };
  }

  if (data) {
    const code = data.code;

    if (code === 200) {
      return { isLoading: false, isAuthenticated: true };
    }

    return { isLoading: false, isAuthenticated: false };
  }

  return { isLoading: false, isAuthenticated: false };
};
