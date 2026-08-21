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
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
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

  /**
   * 독후감을 등록하고 처리 중 알림을 같은 성공 알림으로 전환한다
   *
   * @author SeungHyeon.Kang
   * @param params 독후감 등록 요청 값
   * @return 독후감 등록 응답 Promise
   * @throws 독후감 등록 또는 응답 검증에 실패하면 발생한다
   */
  const setReport = (
    params: Parameters<typeof setReportApi>[0],
  ): ReturnType<typeof setReportApi> => {
    /**
     * 현재 입력한 독후감 정보를 서버에 등록한다
     *
     * @author SeungHyeon.Kang
     * @return 독후감 등록 응답 Promise
     * @throws 독후감 등록 또는 응답 검증에 실패하면 발생한다
     */
    const requestSetReport = (): ReturnType<typeof setReportApi> => {
      // 검증을 마친 독후감 입력값을 등록 API에 전달한다
      return setReportApi(params);
    };

    // 등록 완료 시 처리 중 알림을 닫지 않고 저장 성공 알림으로 전환한다
    return runBlockingOperation(requestSetReport, {
      success: {
        // "저장되었습니다."
        title: message("frontend.alert.saveSuccessTitle"),
        // "독후감이 저장되었어요."
        text: message("frontend.report.saved"),
      },
    });
  };

  return useMutation({
    mutationFn: setReport,
    onSuccess: (data) => {
      // 등록 화면 아래에 남아 있는 도서 검색 이력 대신 홈을 뒤로가기 목적지로 지정한다
      navigate("/home", { replace: true });
      // 홈 위에 등록된 독후감 상세 화면을 새 이력으로 추가한다
      navigate(`/report/detail/${data.data}`);
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
