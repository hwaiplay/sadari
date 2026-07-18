/**
 * src/main/frontend/src/features/Book/Detail/hook/useBookRatingAverage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useQuery } from "@tanstack/react-query";
import { getBookRatingAverageByIsbnApi } from "../../api/bookApi";

export const useBookRatingAverageByIsbn = (
  isbn: string,
  enabled: boolean,
) => {
  return useQuery({
    queryKey: ["bookRatingAverage", isbn],
    queryFn: async () => {
      return await getBookRatingAverageByIsbnApi(isbn);
    },
    enabled: enabled && isbn.trim().length > 0,
  });
};
