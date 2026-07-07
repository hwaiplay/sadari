/**
 * fileName       : useDeleteMutation
 * author         : hanwon.Jang
 * date           : 2026-05-24
 * description    : 독후감 삭제 mutation
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-24       hanwon.Jang       최초 생성
 */

import { message } from "@/app/messages/message";
import { sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { delReportApi } from "../api/bookApi";

export const useDeleteMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: delReportApi,
    onSuccess: () => {
      void sweetSuccess(
        message("frontend.alert.deleteSuccessTitle"), // frontend.alert.deleteSuccessTitle = 삭제되었습니다
        message("frontend.report.deleted"), // frontend.report.deleted = 독후감이 삭제되었습니다.
      ).then(() => navigate("/home"));
    },
  });
};
