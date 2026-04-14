import { useMutation, useQuery } from "@tanstack/react-query";
import { getBookDetail } from "../../api/bookApi";

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

// export const useBookDetailMutation = () => {
//   return useMutation({
//     mutationFn: getBookDetail,
//   });
// };

export const useBookDetail = (id: number) => {
  return useQuery({
    queryKey: ["detail"],
    queryFn: async () => {
      try {
        const res = await getBookDetail(id);
        return res.data;
      } catch (error) {
        console.log("에러발생: " + error);
        throw error;
      }
    },
  });
};
