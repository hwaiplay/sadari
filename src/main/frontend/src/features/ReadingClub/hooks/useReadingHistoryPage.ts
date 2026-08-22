import {
  getClubReadingGoalResultApi,
  type ClubReadingGoalResult,
  type ClubReadingHistory,
} from "@/features/ReadingClub/api/readingClubApi";
import { useReadingHistory } from "@/features/ReadingClub/hooks/useReadingHistory";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";

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
  // 사용자가 결과를 확인할 이전 독서 회차를 관리한다
  const [selectedRondNumb, setSelectedRondNumb] = useState<number | null>(null);
  // 현재 활성 모임원에게 허용된 모든 종료 회차를 페이지 단위로 조회한다
  const historyQuery = useReadingHistory(clubNumb, isValidRoute);

  /**
   * 사용자가 선택한 완료 회차의 목표 결과를 조회한다.
   *
   * @author HanWon.Jang
   * @return 선택한 완료 회차의 목표 결과 또는 선택 전 Null
   * @throws 회차 결과 API 조회가 실패하면 발생한다
   */
  const getReadingGoalResult = async (): Promise<ClubReadingGoalResult | null> => {
    // 회차를 선택하기 전에는 비활성 Query가 실행되어도 결과를 만들지 않는다
    if (selectedRondNumb === null) {
      // 선택 전 목표 결과가 없음을 반환한다
      return null;
    }

    // 선택한 모임과 회차에 고정된 목표 결과를 반환한다
    return await getClubReadingGoalResultApi(clubNumb, selectedRondNumb);
  };

  // 선택한 완료 회차의 전체 목표 결과를 기존 결과 레이어 형식으로 조회한다
  const readingGoalResultQuery = useQuery({
    queryKey: ["readingClub", clubNumb, "readingGoalResult", selectedRondNumb],
    queryFn: getReadingGoalResult,
    enabled: isValidRoute && selectedRondNumb !== null,
  });
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
    // 선택한 회차 결과 Query가 활성화되도록 회차 번호를 저장한다
    setSelectedRondNumb(rondNumb);
  };

  /**
   * 목표 결과 오버레이를 닫고 선택한 회차 상태를 초기화한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleCloseResult = (): void => {
    // 같은 회차를 다시 선택해도 오버레이가 새로 열리도록 선택 상태를 비운다
    setSelectedRondNumb(null);
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
    readingGoalResultQuery,
    selectedRondNumb,
    handleCloseResult,
    handleLoadMore,
    handleSelectReading,
  };
};
