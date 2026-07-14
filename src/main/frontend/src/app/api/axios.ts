// src/api/axios.ts
import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

let refreshRequest: Promise<unknown> | null = null;

/**
 * accessToken 만료로 일반 API가 401을 반환하면 refreshToken으로 토큰을 재발급한 뒤 원래 요청을 한 번 재시도한다.
 * refresh API 자체가 실패한 경우에는 재시도 루프를 막기 위해 원래 401을 그대로 호출부로 전달한다.
 * @Author Hanwon.Jang
 * @param error API 응답 처리 중 발생한 axios 오류
 * @return refresh 성공 시 원 요청 재시도 결과, refresh 실패 시 원 오류
 */
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url === "/oauth/refresh"
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshRequest) {
        refreshRequest = api.post("/oauth/refresh").finally(() => {
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
