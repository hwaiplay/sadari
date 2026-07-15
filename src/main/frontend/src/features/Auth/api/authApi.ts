import api from "../../../app/api/axios";


/**
 * ?꾩옱 釉뚮씪?곗? 荑좏궎???몄쬆 ?좏겙 ?좏슚?깆쓣 ?뺤씤?쒕떎.
 * @author Hanwon.Jang
 * @return ?몄쬆 ?곹깭 ?뺤씤 API ?묐떟
 */
export const checkAuthApi = async () => {
  return api.get("/oauth/tokenCheck");
};

// refreshToken 湲곕컲 accessToken ?щ컻湲?API
/**
 * refreshToken 荑좏궎瑜??댁슜??accessToken ?щ컻湲됱쓣 ?붿껌?쒕떎.
 * @author Hanwon.Jang
 * @return ?좏겙 ?щ컻湲?API ?묐떟
 */
export const refreshTokenApi = async () => {
  return api.post("/oauth/refresh");
};

export const logoutApi = async () => {
  return api.post("/oauth/logout");
};