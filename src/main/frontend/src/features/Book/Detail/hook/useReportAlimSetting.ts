/**
 * 독후감별 좋아요와 댓글 알림 설정 변경 상태를 제공한다
 *
 * @author SeungHyeon.Kang
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  uptReportAlimApi,
  type UptReportAlimParams,
} from "../../api/bookApi";

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
   * 독후감 알림 설정 변경 후 현재 상세 조회 캐시를 갱신하고 변경 결과를 알린다
   *
   * @author SeungHyeon.Kang
   * @param _result 서버가 반환한 독후감 알림 설정 변경 결과
   * @param variables 요청에 사용한 알림 유형과 변경값
   * @return 반환값이 없다
   */
  const handleSuccess = (
    _result: Awaited<ReturnType<typeof uptReportAlimApi>>,
    variables: UptReportAlimParams,
  ): void => {
    // 변경된 설정 문구가 즉시 반영되도록 현재 독후감 상세를 다시 조회한다
    void queryClient.invalidateQueries({ queryKey: ["detail", reptNumb] });

    // 좋아요 알림은 서버에 반영된 사용 여부에 맞는 완료 문구를 표시한다
    if (variables.alimType === "like") {
      // "좋아요 알림이 켜졌습니다."
      // "좋아요 알림이 꺼졌습니다."
      void sweetSuccess(
        message(
          variables.useYsno === "Y"
            ? "frontend.report.alim.like.enable.successTitle"
            : "frontend.report.alim.like.disable.successTitle",
        ),
      );
      // 좋아요 알림 완료 뒤 댓글 알림 문구가 중복 표시되지 않도록 종료한다
      return;
    }

    // "댓글 알림이 켜졌습니다."
    // "댓글 알림이 꺼졌습니다."
    void sweetSuccess(
      message(
        variables.useYsno === "Y"
          ? "frontend.report.alim.reply.enable.successTitle"
          : "frontend.report.alim.reply.disable.successTitle",
      ),
    );
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
