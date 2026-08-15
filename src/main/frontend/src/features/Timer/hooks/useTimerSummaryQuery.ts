import { queryKeys } from "@/app/query/queryKeys";
import {
  getReadingTimerSummaryApi,
  type ReadingTimerSummary,
} from "@/features/Timer/api/readingTimerApi";
import { queryOptions, useQuery } from "@tanstack/react-query";

/**
 * 타이머 페이지와 내비게이션이 공유할 타이머 요약 조회 옵션을 생성한다
 *
 * @author SeungHyeon.Kang
 * @return 독서 타이머 요약 React Query 옵션
 */
export function getTimerSummaryOptions() {
  // 같은 타이머 요약 요청이 하나의 Query Key와 캐시를 사용하도록 옵션을 반환한다
  return queryOptions({
    queryKey: queryKeys.readingTimerSummary,
    /**
     * 서버 기준 독서 타이머와 주간 출석 요약을 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 독서 타이머 요약
     * @throws 타이머 API 요청 또는 공통 응답 검증 실패 시 발생
     */
    queryFn: async (): Promise<ReadingTimerSummary> => {
      // 공통 응답에서 검증된 타이머 요약만 반환한다
      return (await getReadingTimerSummaryApi()).data;
    },
    staleTime: 10_000,
  });
}

/**
 * 독서 타이머 요약의 공유 서버 상태를 제공한다
 *
 * @author SeungHyeon.Kang
 * @return 타이머 요약 데이터와 요청 상태
 */
export function useTimerSummaryQuery() {
  // 공통 타이머 Query Key로 중복 요청을 합친 조회 상태를 반환한다
  return useQuery(getTimerSummaryOptions());
}
