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

// refreshToken 기반 accessToken 재발급 API
/**
 * refreshToken 쿠키를 이용해 accessToken 재발급을 요청한다.
 * @Author Hanwon.Jang
 * @return 토큰 재발급 API 응답
 */
export const refreshTokenApi = async () => {
  return api.post("/oauth/refresh");
};

/**
 * 서버에 로그아웃을 요청해 Redis 토큰 상태와 브라우저 HttpOnly 쿠키를 함께 정리한다.
 * 프론트에서는 HttpOnly 쿠키를 직접 삭제할 수 없으므로 반드시 이 API를 통해 서버가 만료 쿠키를 내려줘야 한다.
 * 요청이 완료되면 호출부에서 로컬 인증 상태와 화면 이동을 처리한다.
 * @Author Hanwon.Jang
 * @return 로그아웃 API 응답
 */
export const logoutApi = async () => {
  return api.post("/oauth/logout");
};
