/**
 * src/main/frontend/src/app/api/axios.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
// src/api/axios.ts
import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { queryClient } from "@/app/query/queryClient";
import { assertResultDataSuccess, type ResultData } from "./resultData";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
  _csrfRetry?: boolean;
};

const API_TIMEOUT_MILLISECONDS = 60_000;
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const SAFE_HTTP_METHODS = new Set(["get", "head", "options", "trace"]);

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
  // DB 연결 장애처럼 서버 응답이 멈춘 요청은 사용자가 무기한 로딩에 갇히지 않도록 1분 후 실패 처리한다.
  timeout: API_TIMEOUT_MILLISECONDS,
});

let refreshRequest: Promise<unknown> | null = null;
let logoutRequest: Promise<unknown> | null = null;
let csrfRequest: Promise<string> | null = null;
let csrfToken: string | null = null;

const AUTH_FAILURE_CODES = new Set([1001, 1002, 1003, 2004, 2009]);
const REFRESHABLE_AUTH_CODES = new Set([1001, 1002, 1003]);

/**
 * get Result Code 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param data data 입력값
 * @return 처리 결과
 */
function getResultCode(data: unknown) {

  return Number((data as { code?: unknown } | undefined)?.code);
}

/**
 * is Refreshable Auth Code 여부를 판정한다
 *
 * @author HanWon.Jang
 * @param code code 입력값
 * @return 판정 결과
 */
function isRefreshableAuthCode(code: number) {

  return REFRESHABLE_AUTH_CODES.has(code);
}

/**
 * is Auth Endpoint 여부를 판정한다
 *
 * @author HanWon.Jang
 * @param url url 입력값
 * @return 판정 결과
 */
function isAuthEndpoint(url?: string) {

  return url === "/oauth/refresh" || url === "/oauth/logout";
}

/**
 * HTTP Method가 CSRF 검증 대상인 상태 변경 요청인지 판정한다
 *
 * @author OpenAI.Codex
 * @param method Axios 요청의 HTTP Method
 * @return CSRF Token Header가 필요한 요청 여부
 */
function isCsrfProtectedMethod(method?: string) {
  // Method가 생략된 Axios 요청은 기본 GET으로 판단한다
  const normalizedMethod = method?.toLowerCase() ?? "get";
  // 안전한 조회 Method가 아니면 CSRF 검증 대상 요청으로 판정한다
  return !SAFE_HTTP_METHODS.has(normalizedMethod);
}

/**
 * 요청 경로가 CSRF Token 자체를 조회하는 API인지 판정한다
 *
 * @author OpenAI.Codex
 * @param url Axios 요청 경로
 * @return CSRF Token 조회 API 여부
 */
function isCsrfEndpoint(url?: string) {
  // CSRF Token 조회 요청이 다시 Token 조회를 시도하지 않도록 경로를 구분한다
  return url === "/oauth/csrf" || url === "/api/oauth/csrf";
}

/**
 * Spring Security가 현재 브라우저에 발급한 CSRF Token을 조회한다
 *
 * @author OpenAI.Codex
 * @return 상태 변경 요청 Header에 사용할 CSRF Token
 * @throws CSRF Token API가 실패하거나 Token 데이터가 없을 때 발생
 */
async function requestCsrfToken() {
  // 공통 Axios Interceptor의 재귀 호출을 피하려고 기본 Axios로 Token 조회 API를 호출한다
  const response = await axios.get<ResultData<string>>("/api/oauth/csrf", {
    withCredentials: true,
    timeout: API_TIMEOUT_MILLISECONDS,
  });
  // HTTP 성공뿐 아니라 공통 응답 코드까지 검증한다
  const result = assertResultDataSuccess(response.data);

  // 빈 Token은 상태 변경 요청을 보호할 수 없으므로 요청 전에 중단한다
  if (typeof result.data !== "string" || result.data.length === 0) {
    // CSRF Token 누락을 호출부의 공통 오류 경로로 전달한다
    throw new Error("CSRF_TOKEN_MISSING");
  }

  // 검증된 CSRF Token을 반환한다
  return result.data;
}

/**
 * 동시에 시작된 상태 변경 요청이 하나의 CSRF Token 조회 Promise를 공유하도록 Token을 준비한다
 *
 * @author OpenAI.Codex
 * @param forceRefresh 기존 Token을 버리고 다시 조회할지 여부
 * @return 현재 브라우저 Cookie와 연결된 CSRF Token
 * @throws CSRF Token 조회 요청이 실패할 때 발생
 */
async function getCsrfToken(forceRefresh = false) {
  // 서버가 기존 Token을 거부한 경우 Cache를 비우고 새 Token을 조회한다
  if (forceRefresh) {
    // 다음 상태 변경 요청이 이전 Token을 재사용하지 않도록 Cache를 초기화한다
    csrfToken = null;
  }

  // 이미 검증한 Token이 있으면 추가 네트워크 요청 없이 재사용한다
  if (csrfToken) {
    // 현재 브라우저에 연결된 CSRF Token을 반환한다
    return csrfToken;
  }

  // 진행 중인 조회가 없을 때만 CSRF Token API를 한 번 호출한다
  if (!csrfRequest) {
    // 동시에 시작된 요청이 같은 CSRF Token 조회 결과를 기다리게 한다
    csrfRequest = requestCsrfToken();
  }

  // 성공과 실패 모두 진행 중 Promise를 정리해 이후 재시도를 허용한다
  try {
    // CSRF Token 조회 결과를 공통 Cache에 저장한다
    csrfToken = await csrfRequest;
    // 상태 변경 요청 Header에 사용할 CSRF Token을 반환한다
    return csrfToken;
  }

  // Token 조회의 성공 여부와 관계없이 완료된 Promise를 정리한다
  finally {
    // 완료된 Promise를 제거해 필요할 때 새 Token을 조회할 수 있게 한다
    csrfRequest = null;
  }
}

/**
 * 상태 변경 Axios 요청에 현재 브라우저의 CSRF Token Header를 설정한다
 *
 * @author OpenAI.Codex
 * @param config 전송 직전 Axios 요청 설정
 * @return CSRF Token Header가 반영된 Axios 요청 설정
 * @throws CSRF Token 조회 요청이 실패할 때 발생
 */
async function setCsrfHeader(config: InternalAxiosRequestConfig) {
  // 안전한 조회와 Token 조회 자체에는 CSRF Header를 추가하지 않는다
  if (!isCsrfProtectedMethod(config.method) || isCsrfEndpoint(config.url)) {
    // 원본 조회 요청 설정을 유지한다
    return config;
  }

  // 현재 브라우저 Cookie와 연결된 CSRF Token을 조회한다
  const token = await getCsrfToken();
  // 브라우저가 자동으로 추가하지 않는 요청 Header에 CSRF Token을 설정한다
  config.headers.set(CSRF_HEADER_NAME, token);
  // CSRF Token Header가 반영된 요청 설정을 반환한다
  return config;
}

// 모든 상태 변경 요청이 화면별 구현 없이 동일한 CSRF 검증을 통과하도록 요청 Interceptor를 등록한다
api.interceptors.request.use(setCsrfHeader);

/**
 * refresh Session 기능을 처리한다
 *
 * @author HanWon.Jang
 * @return 처리 결과
 */
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

/**
 * reset Session And Redirect To Login 사용자 동작을 처리한다
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
async function resetSessionAndRedirectToLogin() {
  // /user/me는 로그인 후 화면에서 현재 세션의 사용자 정보를 확정하는 API다.
  // 인증성 실패 코드가 오면 토큰과 사용자 데이터가 불일치한 상태이므로 세션을 비우고 로그인부터 다시 시킨다.
  useAuthStore.getState().clearAuth();
  // Remove stale successful tokenCheck data so /login does not redirect back to /home after logout.
  queryClient.removeQueries({ queryKey: ["auth"] });

  // accessToken/refreshToken은 HttpOnly 쿠키라 브라우저 코드에서 직접 삭제할 수 없다.
  // 서버 logout API로 쿠키를 만료시킨 뒤 이동해야 /login과 /home 사이의 반복 이동을 막을 수 있다.
  if (!logoutRequest) {
    logoutRequest = api
      .post("/oauth/logout")
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

    // Cookie와 Header의 CSRF Token이 달라졌으면 새 Token으로 원 요청을 한 번만 복구한다
    if (error.response?.status === 403 && originalRequest
            && isCsrfProtectedMethod(originalRequest.method) && !originalRequest._csrfRetry
            && !isCsrfEndpoint(originalRequest.url)) {
      // 동일 요청이 CSRF 오류로 무한 반복되지 않도록 재시도 상태를 기록한다
      originalRequest._csrfRetry = true;

      // 새 Token 조회와 원 요청 재전송 실패를 현재 API 오류 경로로 격리한다
      try {
        // 서버 Cookie와 일치하는 최신 CSRF Token을 다시 조회한다
        await getCsrfToken(true);
        // 기존 요청 데이터와 인증 재시도 상태를 유지한 채 한 번만 다시 전송한다
        return api(originalRequest);
      }

      // Token 재조회 또는 원 요청 재전송이 실패하면 최초 거부 응답을 유지한다
      catch {
        // 원래의 CSRF 거부 응답을 호출부에 전달한다
        return Promise.reject(error);
      }
    }

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
