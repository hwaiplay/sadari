/**
 * src/main/frontend/src/features/Book/Update/useUpdateMutation.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */

import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { uptReportApi } from "../api/bookApi";

export const useUpdateMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: uptReportApi,
    onSuccess: (data) => {
      void sweetSuccess(
        message("frontend.alert.saveSuccessTitle"),
        message("frontend.report.saved"),
      ).then(() => {
        // 수정 저장 후 상세 화면은 새 히스토리로 쌓지 않고 현재 수정 화면을 교체한다.
        // 그래야 상세에서 뒤로가기를 눌렀을 때 방금 저장한 수정 화면으로 되돌아가지 않는다.
        navigate(`/book/detail/${data.data}`, { replace: true });
      });
    },
    onError: (error: unknown) => {
      void sweetError(
        message("frontend.alert.updateFailedTitle"), // frontend.alert.updateFailedTitle = ?섏젙???ㅽ뙣?덉뒿?덈떎
        getApiErrorMessage(error, message("frontend.report.updateFailed")),
      );
    },
  });
};
