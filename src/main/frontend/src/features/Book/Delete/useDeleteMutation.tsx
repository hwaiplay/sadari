/**
 * src/main/frontend/src/features/Book/Delete/useDeleteMutation.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { delReportApi } from "../api/bookApi";

/**
 * use Delete Mutation 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useDeleteMutation = () => {

  const navigate = useNavigate();

  return useMutation({
    mutationFn: delReportApi,
    onSuccess: () => {

      void sweetSuccess(
        message("frontend.alert.deleteSuccessTitle"),
      ).then(() => navigate("/home"));
    },
    onError: (error: unknown) => {

      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    },
  });
};
