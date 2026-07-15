import { useQuery } from "@tanstack/react-query";
import api from "../../../../app/api/axios";

/**
 * 검색어로 책 검색 결과를 React Query로 조회합니다.
 *
 * @author Hanwon.Jang
 * @param searchKeyword 책 검색어
 * @return 책 검색 Query 객체
 */
export const useSearchQuery = (searchKeyword: string) => {
  return useQuery({
    queryKey: ["search", searchKeyword],
    queryFn: async () => {
      try {
        const response = await api.get(
          `/book/search?query=${encodeURIComponent(searchKeyword)}`,
        );

        return response.data;
      } catch (err) {
        console.log("책 검색 중 오류 발생:", err);
        throw err;
      }
    },
  });
};
