/**
 * fileName       : useSearchQuery
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

import { useQuery } from "@tanstack/react-query";
import api from "../../../../app/api/axios";

/**
 * 검색어로 네이버 책 검색 API 결과를 조회한다.
 * @Author Hanwon.Jang
 * @param searchKeyword 사용자가 입력한 책 검색어
 * @return 책 검색 결과 Query 객체
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
        console.log("에러 발생:", err);
        throw err;
      }
    },
  });
};
