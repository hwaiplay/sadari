/**
 * 독후감 수정 요청과 상세 조회 캐시 갱신 및 성공 이동을 처리함
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { uptReportApi } from "../api/bookApi";

/**
 * use Update Mutation 상태와 처리 함수를 제공함
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useUpdateMutation = () => {

  const navigate = useNavigate();
  const queryClient = useQueryClient();

  /**
   * 독후감을 수정하고 상세 캐시를 갱신한 뒤 같은 알림에서 완료를 안내함
   *
   * @author SeungHyeon.Kang
   * @param params 독후감 수정 요청 값
   * @return 독후감 수정 응답 Promise
   * @throws 독후감 수정 또는 상세 캐시 갱신에 실패하면 발생함
   */
  const updateReport = async (
    params: Parameters<typeof uptReportApi>[0],
  ): ReturnType<typeof uptReportApi> => {
    /**
     * 독후감 수정과 상세 캐시 무효화를 하나의 차단 작업으로 실행함
     *
     * @author SeungHyeon.Kang
     * @return 독후감 수정 응답 Promise
     * @throws 독후감 수정 또는 상세 캐시 갱신에 실패하면 발생함
     */
    const updateReportAndInvalidate = async (): ReturnType<typeof uptReportApi> => {
      // 서버에 독후감 수정 내용을 저장함
      const response = await uptReportApi(params);
      // 상세 화면에서 직접 수정한 경우 기존 상세 조회 캐시를 갱신함
      await queryClient.invalidateQueries({
        queryKey: ["detail", response.data],
      });
      // 화면 이동에 사용할 수정 응답을 반환함
      return response;
    };

    // 수정과 캐시 갱신 완료 후 처리 중 알림을 저장 성공 알림으로 전환함
    return runBlockingOperation(updateReportAndInvalidate, {
      success: {
        // "저장되었습니다."
        title: message("frontend.alert.saveSuccessTitle"),
        // "독후감이 저장되었어요."
        text: message("frontend.report.saved"),
      },
    });
  };

  return useMutation({
    mutationFn: updateReport,
    onSuccess: (data) => {
      // 수정 저장 후 상세 화면은 새 히스토리로 쌓지 않고 현재 수정 화면을 교체함
      // 그래야 상세에서 뒤로가기를 눌렀을 때 방금 저장한 수정 화면으로 되돌아가지 않음
      navigate(`/report/detail/${data.data}`, { replace: true });
    },
    onError: (error: unknown) => {

      void sweetError(
        // "수정에 실패했습니다."
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.report.updateFailed")),
      );
    },
  });
};
