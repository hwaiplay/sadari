import api from "../../../app/api/axios";

// export const refreshTokenApi = async () => {
//   try {
//     const res = await api.post("/oauth/refresh");
//     console.log("토큰 갱신 응답:", res.data);
//     return res.data; // ResultData 형태
//   } catch (err) {
//     throw err;
//   }
// };

// 로그인 상태 검증 API
export const checkAuthApi = async () => {
  return api.get("/oauth/tokenCheck");
};

// 리프레시 토큰 발급 API
export const refreshTokenApi = async () => {
  return api.post("/oauth/refresh");
};
