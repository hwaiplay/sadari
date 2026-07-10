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
/**
 * 현재 브라우저 쿠키의 인증 토큰 유효성을 확인한다.
 * @Author Hanwon.Jang
 * @return 인증 상태 확인 API 응답
 */
export const checkAuthApi = async () => {
  return api.get("/oauth/tokenCheck");
};

// 리프레시 토큰 발급 API
/**
 * refresh token으로 access token 재발급을 요청한다.
 * @Author Hanwon.Jang
 * @return 토큰 재발급 API 응답
 */
export const refreshTokenApi = async () => {
  return api.post("/oauth/refresh");
};
