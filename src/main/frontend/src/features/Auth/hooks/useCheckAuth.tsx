/**
 * src/main/frontend/src/features/Auth/hooks/useCheckAuth.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
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
const DELETE_PENDING_STATUS = "DELETE_PENDING";
const SUSPENDED_STATUS = "SUSPENDED";
const ONBOARDING_COMPLETED = "Y";

/**
 * use Check Auth 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
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
    return {
      isLoading: true,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  if (isError) {
    if (
      errorCode === TOKEN_INVALID_CODE ||
      errorCode === TOKEN_EXPIRED_CODE ||
      refreshAttempted
    ) {
      return {
        isLoading: false,
        isAuthenticated: false,
        isDeletePending: false,
        isSuspended: false,
        isOnboardingRequired: false,
      };
    }

    return {
      isLoading: false,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  if (data) {
    const code = data.code;

    if (code === 200) {
      // 인증 응답의 회원 상태로 영구 삭제 대기 전용 화면 여부를 판단합니다
      const authData = data.data as { userStat?: string; onbdYsno?: string } | undefined;
      // 인증 성공 여부와 영구 삭제 대기 및 온보딩 진입 여부를 함께 반환합니다
      return {
        isLoading: false,
        isAuthenticated: true,
        isDeletePending: authData?.userStat === DELETE_PENDING_STATUS,
        isSuspended: authData?.userStat === SUSPENDED_STATUS,
        isOnboardingRequired: authData?.onbdYsno !== ONBOARDING_COMPLETED,
      };
    }

    return {
      isLoading: false,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  return {
    isLoading: false,
    isAuthenticated: false,
    isDeletePending: false,
    isSuspended: false,
    isOnboardingRequired: false,
  };
};
