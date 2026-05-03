/**
 * fileName       : useGetListQuery
 * author         : hanwon.Jang
 * date           : 2026-04-25
 * description    : 독후감 리스트 조회 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-25       hanwon.Jang       최초 생성
 */

import { getBooklist } from "@/features/Book/api/bookApi";
import { useQuery } from "@tanstack/react-query";

export const useGetListQuery = () => {
  return useQuery({
    queryKey: ["list"],
    queryFn: async () => {
      try {
        const res = await getBooklist();
        return res.data;
      } catch (error) {
        console.log("리스트 조회중 에러발생: " + error);
        throw error;
      }
    },
  });
};
