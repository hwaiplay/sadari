import api from "../../../app/api/axios";
import { assertResultDataSuccess } from "../../../app/api/resultData";


/**
 * ?꾩옱 釉뚮씪?곗? 荑좏궎???몄쬆 ?좏겙 ?좏슚?깆쓣 ?뺤씤?쒕떎.
 * @author Hanwon.Jang
 * @return ?몄쬆 ?곹깭 ?뺤씤 API ?묐떟
 */
export const checkAuthApi = async () => {
  const res = await api.get("/oauth/tokenCheck");
  return assertResultDataSuccess(res.data);
};

// refreshToken 湲곕컲 accessToken ?щ컻湲?API
/**
 * refreshToken 荑좏궎瑜??댁슜??accessToken ?щ컻湲됱쓣 ?붿껌?쒕떎.
 * @author Hanwon.Jang
 * @return ?좏겙 ?щ컻湲?API ?묐떟
 */
export const refreshTokenApi = async () => {
  const res = await api.post("/oauth/refresh");
  return assertResultDataSuccess(res.data);
};

export const logoutApi = async () => {
  const res = await api.post("/oauth/logout");
  return assertResultDataSuccess(res.data);
};
