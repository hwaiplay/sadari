import api from "../../../app/api/axios";
import { assertResultDataSuccess } from "../../../app/api/resultData";


/**
 * ?꾩옱 釉뚮씪?곗? 荑좏궎???몄쬆 ?좏겙 ?좏슚?깆쓣 ?뺤씤?쒕떎
 * @author HanWon.Jang
 * @return ?몄쬆 ?곹깭 ?뺤씤 API ?묐떟
 */
export const checkAuthApi = async () => {

  const res = await api.get("/oauth/tokenCheck");
  return assertResultDataSuccess(res.data);
};

// refreshToken 湲곕컲 accessToken ?щ컻湲?API
/**
 * refreshToken 荑좏궎瑜??댁슜??accessToken ?щ컻湲됱쓣 ?붿껌?쒕떎
 * @author HanWon.Jang
 * @return ?좏겙 ?щ컻湲?API ?묐떟
 */
export const refreshTokenApi = async () => {

  const res = await api.post("/oauth/refresh");
  return assertResultDataSuccess(res.data);
};

/**
 * logout 기능을 처리함
 *
 * @author HanWon.Jang
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export type LogoutScope = "CURRENT" | "ALL";

export type LogoutRequest = {
  scope: LogoutScope;
  pushToken?: string;
};

/**
 * 선택한 범위의 로그인 세션과 푸시 구독을 종료함
 *
 * @author SeungHyeon.Kang
 * @param data 현재 기기 또는 전체 기기 로그아웃 요청
 * @return 처리 결과
 */
export const logoutApi = async (data: LogoutRequest) => {

  const res = await api.post("/oauth/logout", data);
  return assertResultDataSuccess(res.data);
};
