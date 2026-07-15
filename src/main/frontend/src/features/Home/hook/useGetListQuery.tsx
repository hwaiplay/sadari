import { getListApi } from "@/features/Book/api/bookApi";
import { useQuery } from "@tanstack/react-query";

type GetListQueryParams = {
  bookKeyword: string;
  sortType: string;
};

/**
 * 메인 화면 독후감 목록을 검색어와 정렬 조건으로 조회합니다.
 *
 * @author Hanwon.Jang
 * @param params 책 제목/작가 검색어와 정렬 조건
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
        console.log("목록 조회 중 오류 발생: " + error);
        throw error;
      }
    },
  });
};
