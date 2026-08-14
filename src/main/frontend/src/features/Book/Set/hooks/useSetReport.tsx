/**
 * fileName       : useSetReport
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 화면에서 사용할 상태와 처리 함수 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang    주석 추가
 */


import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { useMutation } from "@tanstack/react-query";
import { setReportApi } from "../../api/bookApi";
import { useNavigate } from "react-router-dom";

/**
 * 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
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

        // 등록 화면 아래에 남아 있는 도서 검색 이력 대신 홈을 뒤로가기 목적지로 지정한다
        navigate("/home", { replace: true });
        // 홈 위에 등록된 독후감 상세 화면을 새 이력으로 추가한다
        navigate(`/report/detail/${data.data}`);
      });
    },
    onError: (error: unknown) => {

      void sweetError(
        // "등록에 실패했습니다."
        message("frontend.alert.createFailedTitle"),
        getApiErrorMessage(error, message("frontend.report.createFailed")),
      );
    },
  });
};
