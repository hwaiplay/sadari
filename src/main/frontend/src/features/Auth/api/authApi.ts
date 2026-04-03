import api from "../../../app/api/axios";

/**
 * fileName       : authApi
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    : 로그인 검증 관련 API 통합 파일
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       auth 관련 api 통합
 */

// 로그인 상태 검증 API
export const checkAuthApi = async () => {
  return api.get("/oauth/tokenCheck");
};

// 리프레시 토큰 발급 API
export const refreshTokenApi = async () => {
  return api.post("/oauth/refresh");
};
