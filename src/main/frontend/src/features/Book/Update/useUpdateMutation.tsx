/**
 * 독후감 수정 요청과 상세 조회 캐시 갱신 및 성공 이동을 처리한다
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { uptReportApi } from "../api/bookApi";

/**
 * use Update Mutation 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useUpdateMutation = () => {

  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: uptReportApi,
    onSuccess: (data) => {

      // 상세 화면에서 직접 수정한 경우 같은 경로를 유지하므로 기존 상세 조회 캐시를 즉시 갱신한다
      void queryClient.invalidateQueries({
        queryKey: ["detail", data.data],
      });

      void sweetSuccess(
        message("frontend.alert.saveSuccessTitle"),
        message("frontend.report.saved"),
      ).then(() => {
        // 수정 저장 후 상세 화면은 새 히스토리로 쌓지 않고 현재 수정 화면을 교체한다.
        // 그래야 상세에서 뒤로가기를 눌렀을 때 방금 저장한 수정 화면으로 되돌아가지 않는다.
        navigate(`/report/detail/${data.data}`, { replace: true });
      });
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
