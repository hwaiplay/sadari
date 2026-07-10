import { useMutation, useQuery } from "@tanstack/react-query";
import { getDetailApi } from "../../api/bookApi";

/**
 * fileName       : useBookDetailMutation
 * author         : hanwon.Jang
 * date           : 2026-04-08
 * description    : 독후감 상세보기 조회 통신
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-08       hanwon.Jang       최초 생성
 */

/**
 * 독후감 상세 정보를 React Query로 조회한다.
 * @Author Hanwon.Jang
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
        console.log("에러발생: " + error);
        throw error;
      }
    },
  });
};
