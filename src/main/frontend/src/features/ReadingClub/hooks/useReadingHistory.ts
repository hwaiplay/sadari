import { useInfiniteQuery } from "@tanstack/react-query";
import { getClubReadingHistoryApi } from "@/features/ReadingClub/api/readingClubApi";

/**
 * 현재 활성 모임원에게 가입 이전을 포함한 종료 회차 페이지를 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param clubNumb 조회할 모임 번호
 * @param enabled 조회 활성화 여부
 * @return 이전 독서 기록 무한 조회 상태
 */
export function useReadingHistory(clubNumb: number, enabled: boolean) {
  // 모임별 종료 회차를 페이지 순서대로 누적하는 조회 상태를 반환한다
  return useInfiniteQuery({
    queryKey: ["readingClub", clubNumb, "readingHistory"],
    /**
     * 현재 모임의 이전 독서 기록 한 페이지를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param context React Query가 전달한 현재 페이지 번호
     * @return 종료 회차 도서와 목표 달성 집계 페이지
     */
    queryFn: async ({ pageParam }) => {
      // 가입 시점과 관계없이 접근 가능한 종료 회차 페이지를 반환한다
      return await getClubReadingHistoryApi(clubNumb, pageParam);
    },
    initialPageParam: 1,
    /**
     * 마지막 응답에서 다음 이전 독서 기록 페이지 번호를 계산한다.
     *
     * @author SeungHyeon.Kang
     * @param lastPage 마지막으로 조회한 이전 독서 기록 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 페이지 존재를 확인한 경우에만 다음 번호를 반환한다
      return lastPage.hasNext ? lastPage.page + 1 : undefined;
    },
    enabled: enabled && clubNumb > 0,
  });
}
