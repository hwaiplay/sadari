/**
 * src/main/frontend/src/features/Book/Set/hooks/useSetReport.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */

import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { setReportApi } from "../../api/bookApi";
import { useNavigate } from "react-router-dom";

export const useSetReport = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: setReportApi,
    onSuccess: (data) => {
      void sweetSuccess(
        message("frontend.alert.saveSuccessTitle"),
        message("frontend.report.saved"),
      ).then(() => {
        navigate(`/book/detail/${data.data}`);
      });
    },
    onError: (error: unknown) => {
      void sweetError(
        message("frontend.alert.createFailedTitle"), // frontend.alert.createFailedTitle = ?깅줉???ㅽ뙣?덉뒿?덈떎
        getApiErrorMessage(error, message("frontend.report.createFailed")),
      );
    },
  });
};
