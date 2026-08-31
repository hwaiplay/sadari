/**
 * src/main/frontend/src/features/Book/Delete/useDeleteMutation.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { useHomeNavigation } from "@/app/navigation/HomeNavigationProvider";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { queryClient } from "@/app/query/queryClient";
import { queryKeys } from "@/app/query/queryKeys";
import type { ReadingTimer, ReadingTimerSummary } from "@/features/Timer/api/readingTimerApi";
import { useMutation } from "@tanstack/react-query";
import { delReportApi } from "../api/bookApi";

/**
 * 삭제한 독후감을 타이머 요약의 도서 연결 정보에서 제거한다
 *
 * @author SeungHyeon.Kang
 * @param summary 삭제 전 타이머 요약 캐시
 * @param reptNumb 삭제한 독후감 번호
 * @return 삭제한 도서가 제외된 타이머 요약
 */
const removeDeletedTimerBook = (summary: ReadingTimerSummary, reptNumb: number): ReadingTimerSummary => {
  // 삭제되지 않은 읽는 중 도서만 담을 새 목록을 생성한다
  const currentReadingList: ReadingTimer[] = [];

  // 삭제 성공 직후 도서 선택 모달에서 제거할 항목을 구분한다
  for (const readingBook of summary.currentReadingList) {
    // 삭제한 독후감과 다른 도서만 타이머 선택 목록에 유지한다
    if (readingBook.reptNumb !== reptNumb) {
      // 유지 대상 도서를 새 목록에 추가한다
      currentReadingList.push(readingBook);
    }
  }

  // 기존 실행 세션을 삭제 결과에 맞게 보정할 값으로 사용한다
  let activeTimer = summary.activeTimer;

  // 삭제한 독후감에 연결된 실행 세션이면 캐시에서도 도서 연결 표시를 비운다
  if (activeTimer?.reptNumb === reptNumb) {
    // 외래키의 삭제 시 연결 해제 결과를 서버 재조회 전에 먼저 반영한다
    activeTimer = {
      ...activeTimer,
      reptNumb: undefined,
      bookTitl: undefined,
      bookCvim: undefined,
    };
  }

  // 삭제 결과가 즉시 반영된 현재 도서 목록과 실행 세션을 반환한다
  return {
    ...summary,
    activeTimer,
    currentReadingList,
  };
};

/**
 * use Delete Mutation 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useDeleteMutation = () => {

  // 삭제 완료 후 이전 상세 이력을 남기지 않도록 홈 루트 이동 함수를 조회한다
  const moveHome = useHomeNavigation();

  /**
   * 독후감 삭제 성공 즉시 관련 캐시를 정리한다
   *
   * @author SeungHyeon.Kang
   * @param _response 검증이 끝난 독후감 삭제 응답
   * @param reptNumb 삭제한 독후감 번호
   * @return 반환값이 없다
   */
  const handleDeleteSuccess = (_response: unknown, reptNumb: number): void => {

    // 삭제 전 타이머 요약 캐시가 있으면 화면 이동 전에 즉시 정리한다
    const timerSummary = queryClient.getQueryData<ReadingTimerSummary>(queryKeys.readingTimerSummary);

    // 캐시된 목록이 있을 때 삭제한 도서가 다시 표시되지 않도록 갱신한다
    if (timerSummary) {
      // 삭제한 독후감의 도서 연결을 타이머 요약에서 제거한다
      const nextTimerSummary = removeDeletedTimerBook(timerSummary, reptNumb);
      // 홈 플레이어와 타이머 화면이 함께 사용하는 요약 캐시에 삭제 결과를 반영한다
      queryClient.setQueryData(queryKeys.readingTimerSummary, nextTimerSummary);
    }

    // 서버의 외래키 정리 결과까지 반영하도록 다음 사용 시 타이머 요약을 다시 조회한다
    void queryClient.invalidateQueries({ queryKey: queryKeys.readingTimerSummary });
    // 삭제한 독후감으로 집계된 도서별 누적 독서시간 페이지가 다시 표시되지 않도록 캐시를 제거한다
    queryClient.removeQueries({ queryKey: queryKeys.readingTimerBookTimes });
  };

  /**
   * 독후감 삭제와 성공 후처리를 실행하고 같은 알림에서 완료를 안내한다
   *
   * @author SeungHyeon.Kang
   * @param reptNumb 삭제할 독후감 번호
   * @return 독후감 삭제 응답 Promise
   * @throws 독후감 삭제 또는 성공 후처리에 실패하면 발생한다
   */
  const deleteReport = async (
    reptNumb: Parameters<typeof delReportApi>[0],
  ): ReturnType<typeof delReportApi> => {
    /**
     * 서버 삭제 요청과 캐시 및 화면 후처리를 하나의 차단 작업으로 실행한다
     *
     * @author SeungHyeon.Kang
     * @return 독후감 삭제 응답 Promise
     * @throws 독후감 삭제 또는 성공 후처리에 실패하면 발생한다
     */
    const deleteReportAndRefresh = async (): ReturnType<typeof delReportApi> => {
      // 서버에 독후감 삭제를 요청한다
      const response = await delReportApi(reptNumb);
      // 성공 응답 직후 캐시를 정리하고 삭제된 상세 화면을 제거한다
      handleDeleteSuccess(response, reptNumb);
      // Mutation 결과로 사용할 삭제 응답을 반환한다
      return response;
    };

    // 삭제 후처리가 끝나면 처리 중 알림을 삭제 성공 알림으로 전환한다
    return runBlockingOperation(deleteReportAndRefresh, {
      success: {
        // "삭제되었습니다."
        title: message("frontend.alert.deleteSuccessTitle"),
      },
    });
  };

  /**
   * 독후감 삭제 실패 원인을 공통 오류 문구로 안내한다
   *
   * @author SeungHyeon.Kang
   * @param error 독후감 삭제 요청에서 발생한 오류
   * @return 반환값이 없다
   */
  const handleDeleteError = (error: unknown): void => {

    // "수정에 실패했습니다."
    const deleteFailedTitle = message("frontend.alert.updateFailedTitle");
    // "다시 시도해주세요."
    const retryMessage = message("frontend.common.tryAgain");
    // 서버 메시지가 없으면 공통 재시도 문구로 삭제 실패 내용을 구성한다
    const deleteErrorMessage = getApiErrorMessage(error, retryMessage);
    // "수정에 실패했습니다."
    // "다시 시도해주세요."
    void sweetError(deleteFailedTitle, deleteErrorMessage);
  };

  // 독후감 삭제 요청과 성공 및 실패 화면 처리를 결합한 Mutation을 반환한다
  return useMutation({
    mutationFn: deleteReport,
    onSuccess: () => {
      // 처리 중 이동 가드가 해제된 뒤 삭제한 독후감 상세 화면을 홈 루트로 교체한다
      moveHome();
    },
    onError: handleDeleteError,
  });
};
