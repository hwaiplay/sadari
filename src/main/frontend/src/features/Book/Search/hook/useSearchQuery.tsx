import { useQuery } from "@tanstack/react-query";
import api from "../../../../app/api/axios";
import { assertResultDataSuccess } from "../../../../app/api/resultData";
import type { BookSearchPageType } from "../../types/book.type";

/**
 * 검색어로 책 검색 결과를 React Query로 조회함
 *
 * @author HanWon.Jang
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

        const searchPage = assertResultDataSuccess(response.data)
          .data as BookSearchPageType;

        // 기존 훅의 반환 계약은 유지하고 50권 검색 페이지에서 도서 목록만 전달함
        return searchPage.bookList;
      } catch (err) {
        console.log("책 검색 중 오류 발생:", err);
        throw err;
      }
    },
  });
};
