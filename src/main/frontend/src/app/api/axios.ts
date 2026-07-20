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

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

let refreshRequest: Promise<unknown> | null = null;
let logoutRequest: Promise<unknown> | null = null;

const AUTH_FAILURE_CODES = new Set([1001, 1002, 1003, 2004, 2009]);

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
  (response) => {
    // 서버가 HTTP 200으로 응답하더라도 본문 code가 인증 실패라면 보호 화면을 유지하면 안 된다.
    if (
      response.config.url === "/user/me" &&
      AUTH_FAILURE_CODES.has(Number(response.data?.code))
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
      AUTH_FAILURE_CODES.has(Number((error.response?.data as { code?: unknown } | undefined)?.code))
    ) {
      void resetSessionAndRedirectToLogin();
    }

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url === "/oauth/logout" ||
      originalRequest.url === "/oauth/refresh"
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshRequest) {
        refreshRequest = api
          .post("/oauth/refresh")
          .then((response) => assertResultDataSuccess(response.data))
          .finally(() => {
            refreshRequest = null;
          });
      }

      await refreshRequest;
      return api(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  },
);

export default api;
