/**
 * src/main/frontend/src/app/api/axios.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
// src/api/axios.ts
import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { queryClient } from "@/app/query/queryClient";
import { assertResultDataSuccess } from "./resultData";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const API_TIMEOUT_MILLISECONDS = 60_000;

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
  // DB 연결 장애처럼 서버 응답이 멈춘 요청은 사용자가 무기한 로딩에 갇히지 않도록 1분 후 실패 처리한다.
  timeout: API_TIMEOUT_MILLISECONDS,
});

let refreshRequest: Promise<unknown> | null = null;
let logoutRequest: Promise<unknown> | null = null;

const AUTH_FAILURE_CODES = new Set([1001, 1002, 1003, 2004, 2009]);
const REFRESHABLE_AUTH_CODES = new Set([1001, 1002, 1003]);

function getResultCode(data: unknown) {
  return Number((data as { code?: unknown } | undefined)?.code);
}

function isRefreshableAuthCode(code: number) {
  return REFRESHABLE_AUTH_CODES.has(code);
}

function isAuthEndpoint(url?: string) {
  return url === "/oauth/refresh" || url === "/oauth/logout";
}

function refreshSession() {
  if (!refreshRequest) {
    refreshRequest = api
      .post("/oauth/refresh")
      .then((response) => assertResultDataSuccess(response.data))
      .finally(() => {
        refreshRequest = null;
      });
  }

  return refreshRequest;
}

async function resetSessionAndRedirectToLogin() {
  // /user/me는 로그인 후 화면에서 현재 세션의 사용자 정보를 확정하는 API다.
  // 인증성 실패 코드가 오면 토큰과 사용자 데이터가 불일치한 상태이므로 세션을 비우고 로그인부터 다시 시킨다.
  useAuthStore.getState().clearAuth();
  // Remove stale successful tokenCheck data so /login does not redirect back to /home after logout.
  queryClient.removeQueries({ queryKey: ["auth"] });

  // accessToken/refreshToken은 HttpOnly 쿠키라 브라우저 코드에서 직접 삭제할 수 없다.
  // 서버 logout API로 쿠키를 만료시킨 뒤 이동해야 /login과 /home 사이의 반복 이동을 막을 수 있다.
  if (!logoutRequest) {
    logoutRequest = axios
      .post("/api/oauth/logout", undefined, { withCredentials: true })
      .catch(() => undefined)
      .finally(() => {
        logoutRequest = null;
      });
  }

  await logoutRequest;

  if (window.location.pathname !== "/login") {
    window.location.replace("/login");
  }
}

api.interceptors.response.use(
  async (response) => {
    const originalRequest = response.config as RetryableRequestConfig;
    const resultCode = getResultCode(response.data);

    if (
      originalRequest.url === "/user/me" &&
      isRefreshableAuthCode(resultCode) &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        // accessToken 문제는 refreshToken으로 복구될 수 있으므로 세션 삭제보다 재발급을 먼저 시도한다.
        await refreshSession();
        return api(originalRequest);
      } catch {
        await resetSessionAndRedirectToLogin();
        return Promise.reject(response);
      }
    }
    // 서버가 HTTP 200으로 응답하더라도 본문 code가 인증 실패라면 보호 화면을 유지하면 안 된다.
    if (
      originalRequest.url === "/user/me" &&
      AUTH_FAILURE_CODES.has(resultCode)
    ) {
      void resetSessionAndRedirectToLogin();
    }

    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    // /user/me가 400/2009처럼 에러 응답으로 내려와도 세션 불일치로 보고 재로그인 처리한다.
    if (
      originalRequest?.url === "/user/me" &&
      AUTH_FAILURE_CODES.has(getResultCode(error.response?.data))
    ) {
      void resetSessionAndRedirectToLogin();
    }

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      await refreshSession();
      return api(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  },
);

export default api;
