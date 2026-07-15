/**
 * src/main/frontend/src/app/api/axios.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
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