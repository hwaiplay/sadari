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

import { useMutation } from "@tanstack/react-query";
import { addBookReport } from "../../api/bookApi";
import { useNavigate } from "react-router-dom";

export const useAddBookMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: addBookReport,
    onSuccess: (data) => {
      // 서버에서 새 글 ID 받아서 이동
      navigate(`/book/detail/${data.data}`);
    },
  });
};
