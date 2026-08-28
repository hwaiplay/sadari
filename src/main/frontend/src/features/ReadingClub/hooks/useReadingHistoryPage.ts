import type { ClubReadingHistory } from "@/features/ReadingClub/api/readingClubApi";
import { useReadingHistory } from "@/features/ReadingClub/hooks/useReadingHistory";
import { useNavigate, useParams } from "react-router-dom";

type ReadingHistoryPage = {
  list: ClubReadingHistory[];
};

/**
 * 이전 독서 기록 응답 페이지에서 화면에 표시할 회차 목록을 추출한다.
 *
 * @author HanWon.Jang
 * @param page 이전 독서 기록 응답 페이지
 * @return 응답 페이지에 포함된 종료 회차 목록
 */
const getHistoryPageList = (page: ReadingHistoryPage): ClubReadingHistory[] => {
  // 무한 조회 응답을 단일 목록으로 연결할 현재 페이지 목록을 반환한다
  return page.list;
};

/**
 * 이전 독서 기록 페이지의 경로, 목록 조회와 선택 회차 결과 상태를 관리한다.
 *
 * @author HanWon.Jang
 * @return 이전 독서 기록 페이지 표시와 이벤트 처리 상태
 */
export const useReadingHistoryPage = () => {
  // 서버 접근 검증에 사용할 모임 번호를 경로에서 조회한다
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const isValidRoute = Number.isFinite(clubNumb) && clubNumb > 0;
  // 선택한 독서 목표 결과 페이지로 이동할 라우터 함수를 조회한다
  const navigate = useNavigate();
  // 현재 활성 모임원에게 허용된 모든 종료 회차를 페이지 단위로 조회한다
  const historyQuery = useReadingHistory(clubNumb, isValidRoute);

  // 조회된 서버 페이지를 최신 회차 순서의 단일 목록으로 연결한다
  const historyList = historyQuery.data?.pages.flatMap(getHistoryPageList) ?? [];

  /**
   * 이전 독서 기록 카드에서 확인할 회차를 선택한다.
   *
   * @author HanWon.Jang
   * @param rondNumb 목표 결과를 조회할 완료 회차 번호
   * @return 반환값이 없다
   */
  const handleSelectReading = (rondNumb: number): void => {
    // 모임과 회차 번호를 포함한 독서 목표 결과 페이지로 이동한다
    navigate(`/reading-clubs/history/detail/${clubNumb}/${rondNumb}`);
  };

  /**
   * 이전 독서 기록 목록의 다음 페이지를 이어서 조회한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLoadMore = (): void => {
    // 하단 감지 시 다음 종료 회차 페이지를 비동기로 조회한다
    void historyQuery.fetchNextPage();
  };

  // 페이지가 화면 상태별 표시를 결정할 조회와 이벤트 처리값을 반환한다
  return {
    historyList,
    historyQuery,
    isValidRoute,
    handleLoadMore,
    handleSelectReading,
  };
};
