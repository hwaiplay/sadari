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

/**
 * 독후감 삭제 API mutation을 생성하고 성공 시 홈 화면으로 이동한다.
 * @Author Hanwon.Jang
 * @return 독후감 삭제 mutation 객체
 */
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
