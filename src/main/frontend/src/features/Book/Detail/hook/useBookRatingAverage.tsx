import { useQuery } from "@tanstack/react-query";
import { getBookRatingAverageByIsbnApi } from "../../api/bookApi";

/**
 * ISBN 기준으로 전체 독후감 평균 별점을 조회한다.
 * @Author Hanwon.Jang
 * @param isbn 평균 별점을 조회할 도서 ISBN
 * @param enabled 평균 별점 조회 실행 여부
 * @return 전체 독후감 평균 별점 조회 Query 객체
 */
export const useBookRatingAverageByIsbn = (
  isbn: string,
  enabled: boolean,
) => {
  return useQuery({
    queryKey: ["bookRatingAverage", isbn],
    queryFn: async () => {
      const res = await getBookRatingAverageByIsbnApi(isbn);
      return res.data;
    },
    enabled: enabled && isbn.trim().length > 0,
  });
};
