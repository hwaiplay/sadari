import { queryKeys } from "@/app/query/queryKeys";
import { getBookTimePageApi } from "@/features/Timer/api/readingTimerApi";
import { useInfiniteQuery } from "@tanstack/react-query";

/**
 * 도서별 누적 독서 시간을 최근 기록순 서버 페이지로 조회함
 *
 * @author SeungHyeon.Kang
 * @return 도서별 누적 독서 시간 무한 조회 상태
 */
export function useBookTimeQuery() {

  // 모든 누적시간 페이지가 동일한 타이머 전용 캐시를 사용하도록 조회 상태를 반환함
  return useInfiniteQuery({
    queryKey: queryKeys.readingTimerBookTimes,
    /**
     * 현재 페이지의 도서별 누적 독서 시간 최대 20건을 조회함
     *
     * @author SeungHyeon.Kang
     * @param context React Query가 전달한 현재 페이지 번호
     * @return 도서별 누적 독서 시간 페이지 응답
     * @throws 타이머 API 요청 또는 공통 응답 검증 실패 시 발생
     */
    queryFn: async ({ pageParam }) => {
      // 서버가 검증하는 현재 페이지 번호를 전용 API에 전달함
      return await getBookTimePageApi(pageParam);
    },
    initialPageParam: 1,
    /**
     * 마지막 누적시간 응답에서 이어서 조회할 페이지 번호를 계산함
     *
     * @author SeungHyeon.Kang
     * @param lastPage 마지막으로 조회한 도서별 누적시간 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 페이지 존재를 확인한 경우에만 다음 번호를 반환함
      return lastPage.data.hasNext ? lastPage.data.page + 1 : undefined;
    },
    staleTime: 10_000,
  });
}
