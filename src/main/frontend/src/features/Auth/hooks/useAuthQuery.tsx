import { useQuery } from "@tanstack/react-query";
import { checkAuthApi } from "../api/authApi";

/**
 * 현재 브라우저의 로그인 상태를 React Query로 조회합니다.
 *
 * @author Hanwon.Jang
 * @return 로그인 상태 조회 Query 객체
 */
export const useAuthQuery = () => {
  return useQuery({
    queryKey: ["auth"],
    queryFn: async () => {
      try {
        return await checkAuthApi();
      } catch (err) {
        console.log("인증 상태 조회 중 오류 발생:", err);
        throw err;
      }
    },
    retry: false,
  });
};
