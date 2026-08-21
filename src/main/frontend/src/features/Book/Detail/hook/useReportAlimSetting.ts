/**
 * 독후감별 좋아요와 댓글 알림 설정 변경 상태를 제공한다
 *
 * @author SeungHyeon.Kang
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { uptReportAlimApi } from "../../api/bookApi";

/**
 * 독후감별 알림 설정 변경과 상세 캐시 갱신을 처리한다
 *
 * @author SeungHyeon.Kang
 * @param reptNumb 변경할 독후감 번호
 * @return 화면에서 사용할 알림 설정 변경 Mutation 객체
 */
export const useReportAlimSetting = (reptNumb: number) => {
  const queryClient = useQueryClient();

  /**
   * 독후감 알림 설정 변경 후 현재 상세 조회 캐시를 갱신한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleSuccess = (): void => {
    // 변경된 설정 문구가 즉시 반영되도록 현재 독후감 상세를 다시 조회한다
    void queryClient.invalidateQueries({ queryKey: ["detail", reptNumb] });
  };

  /**
   * 독후감 알림 설정 변경 실패 사유를 공통 오류 알림으로 표시한다
   *
   * @author SeungHyeon.Kang
   * @param error API 요청 또는 응답 검증 중 발생한 오류
   * @return 반환값이 없다
   */
  const handleError = (error: unknown): void => {
    void sweetError(
      // "수정에 실패했어요"
      message("frontend.alert.updateFailedTitle"),
      // "다시 시도해주세요."
      getApiErrorMessage(error, message("frontend.common.tryAgain")),
    );
  };

  // 독후감별 알림 설정 변경과 상세 캐시 갱신 Mutation 객체를 반환한다
  return useMutation({
    mutationFn: uptReportAlimApi,
    onSuccess: handleSuccess,
    onError: handleError,
  });
};
