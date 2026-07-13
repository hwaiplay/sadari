/**
 * fileName       : useGetListQuery
 * author         : hanwon.Jang
 * date           : 2026-04-25
 * description    : 독후감 리스트 조회 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-25       hanwon.Jang       최초 생성
 */

import { getListApi } from "@/features/Book/api/bookApi";
import { useQuery } from "@tanstack/react-query";

type GetListQueryParams = {
  bookKeyword: string;
  sortType: string;
};

/**
 * 로그인 사용자의 독후감 목록을 React Query로 조회한다.
 * @Author Hanwon.Jang
 * @return 독후감 목록 조회 Query 객체
 */
export const useGetListQuery = (params: GetListQueryParams) => {
  return useQuery({
    queryKey: ["list", params.bookKeyword, params.sortType],
    queryFn: async () => {
      try {
        const res = await getListApi(params);
        return res.data;
      } catch (error) {
        console.log("리스트 조회중 에러발생: " + error);
        throw error;
      }
    },
  });
};
