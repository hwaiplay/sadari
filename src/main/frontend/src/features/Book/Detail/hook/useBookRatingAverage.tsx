/**
 * src/main/frontend/src/features/Book/Detail/hook/useBookRatingAverage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { useQuery } from "@tanstack/react-query";
import { getBookRatingAvgApi } from "../../api/bookApi";

/**
 * use Book Rating Average By Isbn 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @param enabled enabled 입력값
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useBookRatingAvg = (
  isbn: string,
  enabled: boolean,
) => {

  return useQuery({
    queryKey: ["bookRatingAverage", isbn],
    queryFn: async () => {

      return await getBookRatingAvgApi(isbn);
    },
    enabled: enabled && isbn.trim().length > 0,
  });
};
