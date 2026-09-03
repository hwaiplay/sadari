/**
 * 독후감별 좋아요와 댓글 알림 설정 변경 상태를 제공함
 *
 * @author SeungHyeon.Kang
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  uptReportAlimApi,
  type UptReportAlimParams,
} from "../../api/bookApi";

/**
 * 독후감별 알림 설정 변경과 상세 캐시 갱신을 처리함
 *
 * @author SeungHyeon.Kang
 * @param reptNumb 변경할 독후감 번호
 * @return 화면에서 사용할 알림 설정 변경 Mutation 객체
 */
export const useReportAlimSetting = (reptNumb: number) => {
  const queryClient = useQueryClient();

  /**
   * 변경할 알림 유형과 사용 여부에 맞는 성공 문구를 처리 중 모달 전환 정보로 전달함
   *
   * @author SeungHyeon.Kang
   * @param params 독후감 번호, 알림 유형과 변경할 사용 여부
   * @return 독후감 알림 설정 변경 요청 결과
   * @throws 독후감 알림 설정 변경 또는 응답 검증에 실패하면 발생함
   */
  const requestReportAlimSetting = (
    params: UptReportAlimParams,
  ): ReturnType<typeof uptReportAlimApi> => {
    // "좋아요 알림이 켜졌습니다."
    // "좋아요 알림이 꺼졌습니다."
    // "댓글 알림이 켜졌습니다."
    // "댓글 알림이 꺼졌습니다."
    const successTitle = params.alimType === "like"
      ? message(
          params.useYsno === "Y"
            ? "frontend.report.alim.like.enable.successTitle"
            : "frontend.report.alim.like.disable.successTitle",
        )
      : message(
          params.useYsno === "Y"
            ? "frontend.report.alim.reply.enable.successTitle"
            : "frontend.report.alim.reply.disable.successTitle",
        );

    /**
     * 독후감별 좋아요 또는 댓글 알림 사용 여부를 서버에 저장함
     *
     * @author SeungHyeon.Kang
     * @return 독후감 알림 설정 변경 요청 결과
     * @throws 독후감 알림 설정 변경 또는 응답 검증에 실패하면 발생함
     */
    const updateReportAlimSetting = (): ReturnType<typeof uptReportAlimApi> => {
      // 사용자 소유 독후감의 선택한 알림 설정을 변경함
      return uptReportAlimApi(params);
    };

    // 공통 로딩 모달을 닫지 않고 성공 상태로 전환할 제목과 함께 변경 요청을 반환함
    return runBlockingOperation(updateReportAlimSetting, {
      success: { title: successTitle },
    });
  };

  /**
   * 독후감 알림 설정 변경 후 현재 상세 조회 캐시를 갱신함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleSuccess = (): void => {
    // 변경된 설정 문구가 즉시 반영되도록 현재 독후감 상세를 다시 조회함
    void queryClient.invalidateQueries({ queryKey: ["detail", reptNumb] });
  };

  /**
   * 독후감 알림 설정 변경 실패 사유를 공통 오류 알림으로 표시함
   *
   * @author SeungHyeon.Kang
   * @param error API 요청 또는 응답 검증 중 발생한 오류
   * @return 반환값이 없음
   */
  const handleError = (error: unknown): void => {
    void sweetError(
      // "수정에 실패했어요"
      message("frontend.alert.updateFailedTitle"),
      // "다시 시도해주세요."
      getApiErrorMessage(error, message("frontend.common.tryAgain")),
    );
  };

  // 독후감별 알림 설정 변경과 상세 캐시 갱신 Mutation 객체를 반환함
  return useMutation({
    mutationFn: requestReportAlimSetting,
    onSuccess: handleSuccess,
    onError: handleError,
  });
};
