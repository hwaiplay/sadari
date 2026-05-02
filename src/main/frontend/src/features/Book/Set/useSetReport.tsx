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

import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { setReportApi } from "../api/bookApi";

export const useSetReport = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: setReportApi,
    onSuccess: (data) => {
      navigate(`/book/detail/${data.data}`);
    },
  });
};
