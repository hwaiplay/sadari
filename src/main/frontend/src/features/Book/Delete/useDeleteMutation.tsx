/**
 * src/main/frontend/src/features/Book/Delete/useDeleteMutation.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { useHomeNavigation } from "@/app/navigation/HomeNavigationProvider";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { delReportApi } from "../api/bookApi";

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
   * 독후감 삭제 성공 즉시 홈 루트로 이동한 뒤 완료 결과를 안내한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleDeleteSuccess = (): void => {

    // 삭제한 독후감 상세 화면이 남지 않도록 성공 응답 즉시 홈 루트로 이동한다
    moveHome();
    // "삭제되었습니다."
    const deleteSuccessTitle = message("frontend.alert.deleteSuccessTitle");
    // "삭제되었습니다."
    void sweetSuccess(deleteSuccessTitle);
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
    mutationFn: delReportApi,
    onSuccess: handleDeleteSuccess,
    onError: handleDeleteError,
  });
};
