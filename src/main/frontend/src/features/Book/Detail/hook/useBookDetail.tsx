import { useMutation, useQuery } from "@tanstack/react-query";
import { getDetailApi } from "../../api/bookApi";

/**
 * 독후감 상세 정보를 React Query로 조회합니다.
 *
 * @author Hanwon.Jang
 * @param bookNumb 조회할 독후감 번호
 * @return 독후감 상세 조회 Query 객체
 */
export const useBookDetail = (bookNumb: number) => {
  return useQuery({
    queryKey: ["detail", bookNumb],
    queryFn: async () => {
      try {
        const res = await getDetailApi(bookNumb);
        return res.data;
      } catch (error) {
        console.log("독후감 상세 조회 중 오류 발생: " + error);
        throw error;
      }
    },
  });
};
