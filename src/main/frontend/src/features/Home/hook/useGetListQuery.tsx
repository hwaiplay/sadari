import { getListApi } from "@/features/Book/api/bookApi";
import { useInfiniteQuery } from "@tanstack/react-query";

type GetListQueryParams = {
  bookKeyword: string;
  sortType: string;
};

/**
 * 메인 화면 독후감 목록을 검색어와 정렬 조건으로 조회함
 *
 * @author HanWon.Jang
 * @param params 책 제목/작가 검색어와 정렬 조건
 * @return 독후감 목록 조회 Query 객체
 */
export const useGetListQuery = (params: GetListQueryParams) => {

  // 같은 검색어와 정렬 조건의 페이지를 하나의 무한 조회 캐시로 관리함
  return useInfiniteQuery({
    queryKey: ["list", params.bookKeyword, params.sortType],
    /**
     * 서버가 제한한 홈 독후감 한 페이지를 조회함
     *
     * @author SeungHyeon.Kang
     * @param context React Query가 전달한 현재 페이지 번호
     * @return 홈 독후감 페이지 응답
     * @throws 홈 독후감 API 요청 또는 응답 검증 실패 시 발생
     */
    queryFn: async ({ pageParam }) => {
      // 현재 페이지 번호를 검색 및 정렬 조건과 함께 서버에 전달함
      return await getListApi({ ...params, page: pageParam });
    },
    initialPageParam: 1,
    /**
     * 마지막 응답의 다음 페이지 여부로 이어서 조회할 페이지 번호를 계산함
     *
     * @author SeungHyeon.Kang
     * @param lastPage 마지막으로 조회한 홈 독후감 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 페이지 존재를 확인한 경우에만 다음 번호를 반환함
      return lastPage.data?.hasNext ? lastPage.data.page + 1 : undefined;
    },
    retry: false,
  });
};
