/**
 * fileName       : useAddBookMutation
 * author         : hanwon.Jang
 * date           : 2026-04-07
 * description    : 독후감 기록 백엔드 통신
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07       hanwon.Jang       최초 생성
 */

import { message } from "@/app/messages/message";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { setReportApi } from "../../api/bookApi";
import { useNavigate } from "react-router-dom";
import { AxiosError } from "axios";

type ErrorResponse = {
  message?: string;
};

/**
 * 독후감 등록 API mutation을 생성하고 성공 시 상세 화면으로 이동한다.
 * @Author Hanwon.Jang
 * @return 독후감 등록 mutation 객체
 */
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
    onError: (error: AxiosError<ErrorResponse>) => {
      void sweetError(
        message("frontend.alert.createFailedTitle"), // frontend.alert.createFailedTitle = 등록에 실패했습니다
        error.response?.data?.message ?? message("frontend.report.createFailed"), // frontend.report.createFailed = 독후감 등록에 실패했어요.
      );
    },
  });
};
