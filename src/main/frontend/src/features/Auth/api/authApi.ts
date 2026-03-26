// features/auth/api/authApi.ts
import api from "../../../app/api/axios";

export const refreshToken = async () => {
  try {
    const res = await api.post("/oauth/refresh");
    console.log("refresh 토큰 갱신 응답:", res.data);
    return res.data; // ResultData 형태
  } catch (err) {
    throw err;
  }
};
