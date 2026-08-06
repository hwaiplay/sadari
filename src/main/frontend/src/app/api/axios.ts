/**
 * src/main/frontend/src/app/api/axios.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
// src/api/axios.ts
import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { queryClient } from "@/app/query/queryClient";
import {
  beginBlockingOperation,
  endBlockingOperation,
} from "@/app/navigation/blockingOperation";
import { assertResultDataSuccess, type ResultData } from "./resultData";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
  _csrfRetry?: boolean;
  _blockingOperationId?: number;
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
 * 요청 경로가 저장 없이 계산 결과만 조회하는 POST API인지 판정한다
 *
 * @author SeungHyeon.Kang
 * @param url Axios 요청 경로
 * @return 상태 변경 처리 중 모달에서 제외할 조회성 POST 여부
 */
function isNonSavingPostEndpoint(url?: string): boolean {
  // 도서 표지색 분석은 서버 데이터를 변경하지 않으므로 저장 작업으로 분류하지 않는다
  return url === "/book/cover-color";
}

/**
 * Axios 요청이 사용자 데이터 또는 외부 설정을 변경하는 작업인지 판정한다
 *
 * @author SeungHyeon.Kang
 * @param config 전송할 Axios 요청 설정
 * @return 처리 중 모달과 화면 이동 차단이 필요한 요청 여부
 */
function isBlockingOperationRequest(config: InternalAxiosRequestConfig): boolean {
  // 조회 요청과 인증 유지 요청 및 조회성 POST는 사용자 저장 작업에서 제외한다
  return isCsrfProtectedMethod(config.method)
    && !isAuthEndpoint(config.url)
    && !isNonSavingPostEndpoint(config.url);
}

/**
 * HTTP Method가 CSRF 검증 대상인 상태 변경 요청인지 판정한다
 *
 * @author SeungHyeon.Kang
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
 * @author SeungHyeon.Kang
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
 * @author SeungHyeon.Kang
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
 * @author SeungHyeon.Kang
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
 * @author SeungHyeon.Kang
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

/**
 * 상태 변경 요청의 이동 차단을 시작한 뒤 CSRF Token Header를 준비한다
 *
 * @author SeungHyeon.Kang
 * @param config 전송 직전 Axios 요청 설정
 * @return 이동 차단 식별값과 CSRF Token Header가 반영된 요청 설정
 * @throws CSRF Token 조회 요청이 실패할 때 발생
 */
async function prepareRequest(config: InternalAxiosRequestConfig): Promise<InternalAxiosRequestConfig> {
  const blockingConfig = config as RetryableRequestConfig;

  // 재시도가 아닌 최초 상태 변경 요청이면 공통 처리 중 모달과 이동 가드를 시작한다
  if (isBlockingOperationRequest(config) && blockingConfig._blockingOperationId === undefined) {
    // 요청 완료 시 같은 작업만 해제할 수 있도록 이동 차단 식별값을 설정한다
    blockingConfig._blockingOperationId = beginBlockingOperation();
  }

  // CSRF Token 준비 실패도 처리 중 화면을 정리하도록 요청 전 단계를 격리한다
  try {
    // 상태 변경 요청에 현재 인증 Cookie와 연결된 CSRF Token Header를 설정한다
    return await setCsrfHeader(blockingConfig);
  }

  // CSRF Token을 준비하지 못하면 서버 전송 없이 종료되는 요청의 이동 가드를 해제한다
  catch (error) {
    // 요청 전 단계에서 시작한 처리 중 모달과 이동 가드를 정리한다
    await finishBlockingRequest(blockingConfig);
    // 호출 화면의 기존 실패 경로가 원인을 처리할 수 있도록 오류를 다시 전달한다
    throw error;
  }
}

/**
 * Axios 상태 변경 요청에 연결된 처리 중 모달과 이동 가드를 해제한다
 *
 * @author SeungHyeon.Kang
 * @param config 완료되거나 실패한 Axios 요청 설정
 * @return 요청별 이동 차단 정리 완료 Promise
 */
async function finishBlockingRequest(config?: RetryableRequestConfig): Promise<void> {
  const operationId = config?._blockingOperationId;

  // 조회 요청 또는 이미 정리된 상태 변경 요청은 추가 화면 변경 없이 종료한다
  if (!config || operationId === undefined) {
    // 해제할 공통 처리 중 작업이 없는 상태로 완료한다
    return;
  }

  // 재시도 응답 체인에서 같은 요청이 다시 해제되지 않도록 식별값을 먼저 제거한다
  delete config._blockingOperationId;
  // 후속 화면 처리 전에 버튼 없는 모달과 동일 URL History 가드를 정리한다
  await endBlockingOperation(operationId);
}

// 모든 상태 변경 요청이 화면별 구현 없이 동일한 이동 차단과 CSRF 검증을 적용받도록 요청 Interceptor를 등록한다
api.interceptors.request.use(prepareRequest);

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

// 상태 변경 요청의 최종 성공 또는 실패 응답이 확정되면 공통 처리 중 화면을 해제한다
api.interceptors.response.use(
  async (response) => {
    // 호출 화면의 성공 처리 전에 현재 요청의 이동 차단 History 항목을 제거한다
    await finishBlockingRequest(response.config as RetryableRequestConfig);
    // 정리가 끝난 Axios 응답을 기존 호출부에 반환한다
    return response;
  },
  async (error: AxiosError) => {
    // 호출 화면의 오류 처리 전에 실패한 요청의 처리 중 모달과 이동 가드를 해제한다
    await finishBlockingRequest(error.config as RetryableRequestConfig | undefined);
    // 기존 API 실패 경로가 사용자 메시지를 표시할 수 있도록 Axios 오류를 반환한다
    return Promise.reject(error);
  },
);

export default api;
