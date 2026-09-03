import { queryKeys } from "@/app/query/queryKeys";
import {
  getReadingTimerSummaryApi,
  type ReadingTimerSummary,
} from "@/features/Timer/api/readingTimerApi";
import { syncTimerSummary } from "@/features/Timer/lib/readingTimerClock";
import { queryOptions, useQuery } from "@tanstack/react-query";

/**
 * 타이머 페이지와 내비게이션이 공유할 타이머 요약 조회 옵션을 생성함
 *
 * @author SeungHyeon.Kang
 * @return 독서 타이머 요약 React Query 옵션
 */
export function getTimerSummaryOptions() {
  // 같은 타이머 요약 요청이 하나의 Query Key와 캐시를 사용하도록 옵션을 반환함
  return queryOptions({
    queryKey: queryKeys.readingTimerSummary,
    /**
     * 서버 기준 독서 타이머와 주간 출석 요약을 조회함
     *
     * @author SeungHyeon.Kang
     * @return 독서 타이머 요약
     * @throws 타이머 API 요청 또는 공통 응답 검증 실패 시 발생
     */
    queryFn: async (): Promise<ReadingTimerSummary> => {
      // 서버가 계산한 타이머 요약을 공통 응답에서 조회함
      const summary = (await getReadingTimerSummaryApi()).data;
      // 화면 전환 뒤에도 같은 경과시간 기준을 사용하도록 수신 시각을 기록해 반환함
      return syncTimerSummary(summary);
    },
    staleTime: 10_000,
  });
}

/**
 * 독서 타이머 요약의 공유 서버 상태를 제공함
 *
 * @author SeungHyeon.Kang
 * @return 타이머 요약 데이터와 요청 상태
 */
export function useTimerSummaryQuery() {
  // 공통 타이머 Query Key로 중복 요청을 합친 조회 상태를 반환함
  return useQuery(getTimerSummaryOptions());
}
