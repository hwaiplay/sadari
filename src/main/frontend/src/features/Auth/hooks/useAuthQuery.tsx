import { useQuery } from "@tanstack/react-query";
import api from "../../../app/api/axios";

/**
 * fileName       : useAuthQuery
 * author         : hanwon.Jang
 * date           : 2026-03-25
 * description    : 인증 상태를 조회하는 커스텀 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-24       hanwon.Jang       최초 생성
 */

export const useAuthQuery = () => {
  return useQuery({
    queryKey: ["auth"],
    queryFn: async () => {
      try {
        const res = await api.get("/oauth/tokenCheck");
        return res.data;
      } catch (err) {
        console.log("에러 발생:", err);
        throw err; // 중요 (Query가 에러 인식하도록)
      }
    },
  });
};
