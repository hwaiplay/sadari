/**
 * 완료된 모임 독서 회차의 DONE 독후감 목록 서버 상태를 관리한다.
 *
 * @author HanWon.Jang
 */
import { useInfiniteQuery } from "@tanstack/react-query";
import type { PublicReportSortType } from "@/features/Book/api/bookApi";
import { getRoundReportsApi } from "@/features/ReadingClub/api/readingClubApi";

/**
 * 현재 활성 모임원에게 허용된 완료 회차 독후감 페이지를 조회한다.
 *
 * @author HanWon.Jang
 * @param clubNumb 조회할 모임 번호
 * @param rondNumb 조회할 회차 번호
 * @param sortType 독후감 정렬 코드
 * @param enabled 조회 활성화 여부
 * @return 완료 회차 독후감 무한 조회 상태
 */
export const useReadingRoundReports = (
  clubNumb: number,
  rondNumb: number,
  sortType: PublicReportSortType,
  enabled: boolean,
) => {
  // 모임과 회차 및 정렬별 완료 독후감 페이지를 하나의 무한 조회 캐시로 관리한다
  return useInfiniteQuery({
    queryKey: ["readingClub", clubNumb, "readingRound", rondNumb, "reports", sortType],
    /**
     * 현재 조건의 완료 회차 독후감 한 페이지를 조회한다.
     *
     * @author HanWon.Jang
     * @param context React Query가 전달한 현재 페이지 번호
     * @return 회차 도서 정보와 완료 독후감 페이지 응답
     */
    queryFn: async ({ pageParam }) => {
      // 현재 페이지와 서버 검증 대상 회차 및 정렬 조건을 함께 전달한다
      return await getRoundReportsApi(clubNumb, rondNumb, sortType, pageParam);
    },
    initialPageParam: 1,
    /**
     * 마지막 응답에서 다음 완료 독후감 페이지 번호를 계산한다.
     *
     * @author HanWon.Jang
     * @param lastPage 마지막으로 조회한 완료 회차 독후감 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 페이지 존재를 확인한 경우에만 다음 번호를 반환한다
      return lastPage.data?.reportPage.hasNext
        ? lastPage.data.reportPage.page + 1
        : undefined;
    },
    enabled: enabled && clubNumb > 0 && rondNumb > 0,
  });
};
