import { useMutation, useQuery } from "@tanstack/react-query";
import { getDetailApi } from "../../api/bookApi";

/**
 * 독후감 상세 정보를 React Query로 조회합니다.
 *
 * @author Hanwon.Jang
 * @param bookNumb 조회할 독후감 번호
 * @param enabled 상세 조회 API 호출 여부
 * @return 독후감 상세 조회 Query 객체
 */
export const useBookDetail = (bookNumb: number, enabled = true) => {
  return useQuery({
    queryKey: ["detail", bookNumb],
    queryFn: async () => {
      try {
        return await getDetailApi(bookNumb);
      } catch (error) {
        console.log("독후감 상세 조회 중 오류 발생: " + error);
        throw error;
      }
    },
    enabled: enabled && Number.isFinite(bookNumb),
  });
};
