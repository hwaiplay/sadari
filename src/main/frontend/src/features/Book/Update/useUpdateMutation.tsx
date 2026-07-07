/**
 * fileName       : useSetReport
 * author         : Hanwon.Jang
 * date           : 2026-05-03
 * description    : 독후감 수정 Mutation
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       Hanwon.Jang       최초 생성
 */

import { message } from "@/app/messages/message";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { uptReportApi } from "../api/bookApi";
import { AxiosError } from "axios";

type ErrorResponse = {
  message?: string;
};

export const useUpdateMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: uptReportApi,
    onSuccess: (data) => {
      navigate(`/book/detail/${data.data}`);
    },
    onError: (error: AxiosError<ErrorResponse>) => {
      void sweetError(
        message("frontend.alert.updateFailedTitle"), // frontend.alert.updateFailedTitle = 수정에 실패했습니다
        error.response?.data?.message ?? message("frontend.report.updateFailed"), // frontend.report.updateFailed = 독후감 수정에 실패했어요.
      );
    },
  });
};
