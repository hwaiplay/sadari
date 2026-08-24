import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetAlert, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  getPendingResultApi,
  type ComplaintResultItem,
  uptResultConfirmApi,
} from "@/features/Complaint/api/complaintApi";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useRef } from "react";

// 화면 전환과 앱 재진입에서 같은 미확인 결과 조회 캐시를 공유한다
const COMPLAINT_RESULT_QUERY_KEY = ["complaint", "results", "pending"] as const;

/** 사용자 로케일로 신고 결과 일시를 표시한다. */
const formatResultDate = (dateValue: string | null): string => {
  // 접수일이 공개되지 않는 피신고자 결과는 빈 문자열로 유지한다
  if (!dateValue) {
    return "";
  }
  // 브라우저 로케일의 읽기 쉬운 날짜와 시간으로 변환한다
  return new Intl.DateTimeFormat(navigator.language, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(dateValue));
};

/** 수신자 유형에 맞춰 신고 조치 결과 한 건의 상세 문구를 생성한다. */
const createResultText = (item: ComplaintResultItem): string => {
  // 피신고자에게는 신고번호와 접수일 및 정확한 누적 건수를 표시하지 않는다
  if (item.rcvrType === "TARGET") {
    return [
      message("frontend.complaint.result.targetHeading"),
      `${message("frontend.complaint.result.targetLabel")}: ${item.tagtName}`,
      `${message("frontend.complaint.result.reasonLabel")}: ${item.rsonName}`,
      `${message("frontend.complaint.result.actionLabel")}: ${item.rsltCntn}`,
      `${message("frontend.complaint.result.processDateLabel")}: ${formatResultDate(item.procDate)}`,
    ].join("\n");
  }
  // 신고자에게는 자신이 접수한 건을 구분할 신고번호와 사유 및 처리 결과를 표시한다
  return [
    message("frontend.complaint.result.reporterHeading"),
    `${message("frontend.complaint.result.numberLabel")}: #${item.cmplNumb}`,
    `${message("frontend.complaint.result.targetLabel")}: ${item.tagtName}`,
    `${message("frontend.complaint.result.reasonLabel")}: ${item.rsonName}`,
    `${message("frontend.complaint.result.actionLabel")}: ${item.rsltCntn}`,
    `${message("frontend.complaint.result.receivedDateLabel")}: ${formatResultDate(item.cmplDate)}`,
    `${message("frontend.complaint.result.processDateLabel")}: ${formatResultDate(item.procDate)}`,
  ].join("\n");
};

/**
 * 활성 사용자가 아직 확인하지 않은 신고 조치 결과를 전용 팝업으로 안내한다
 *
 * @author HanWon.Jang
 * @return 별도 화면 요소 없이 전역 알림만 표시한다
 */
const ComplaintResultPopup = () => {
  // 같은 조회 결과가 렌더링 과정에서 중복 팝업으로 열리지 않도록 마지막 표시 번호를 보관한다
  const shownResultNumbRef = useRef<number | null>(null);
  // 앱 재진입과 창 포커스 시 현재 활성 사용자의 미확인 결과를 다시 조회한다
  const pendingResultQuery = useQuery({
    queryKey: COMPLAINT_RESULT_QUERY_KEY,
    queryFn: getPendingResultApi,
    refetchOnWindowFocus: "always",
    retry: false,
    staleTime: 0,
  });

  // 새 미확인 결과가 조회되면 사용자가 확인할 때까지 닫히지 않는 안내를 표시한다
  useEffect(() => {
    const pendingResult = pendingResultQuery.data;
    const lastRsltNumb = pendingResult?.lastRsltNumb;

    // 결과가 없거나 같은 조회 경계를 이미 표시했다면 팝업을 다시 열지 않는다
    if (!pendingResult || pendingResult.rsltCntt < 1
        || !lastRsltNumb
        || shownResultNumbRef.current === lastRsltNumb) {
      // 현재 렌더링에서는 신고 조치 결과 안내를 생략한다
      return;
    }

    // 현재 조회 경계를 먼저 기록해 Effect 재실행에 따른 중복 팝업을 차단한다
    shownResultNumbRef.current = lastRsltNumb;

    /**
     * 사용자 확인 뒤 조회 시점까지의 신고 조치 결과만 확인 처리한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const showComplaintResult = async (): Promise<void> => {
      // "신고 처리 결과를 안내드려요."
      const title = message("frontend.complaint.result.title");
      // 서버가 수신자 정책에 맞춰 제공한 상세 결과를 팝업 문단으로 변환한다
      const texts = pendingResult.resultList.map(createResultText);
      // 확인 버튼으로 읽은 경우에만 서버 확인 일시를 저장하도록 전용 안내를 표시한다
      const alertResult = await sweetAlert({
        title,
        texts,
        icon: "info",
        allowOutsideClick: false,
      });

      // 확인 버튼 이외의 사유로 알림이 종료되면 미확인 상태를 유지한다
      if (!alertResult.isConfirmed) {
        // 다음 앱 접근에서 다시 표시할 수 있도록 현재 처리를 종료한다
        return;
      }

      // 확인 요청 실패 시 결과를 남겨 다음 접근에서 다시 안내한다
      try {
        await uptResultConfirmApi(lastRsltNumb);
        // 확인 처리 중 새로 생성된 결과가 있으면 새 팝업으로 이어지도록 최신 상태를 조회한다
        await pendingResultQuery.refetch();
      }

      catch (error) {
        // 실패한 조회 경계를 다음 포커스 재조회에서 다시 표시할 수 있도록 해제한다
        shownResultNumbRef.current = null;
        // "신고 처리 결과를 확인 처리하지 못했어요."
        const errorTitle = message("frontend.complaint.result.confirmFailedTitle");
        // "다시 시도해주세요."
        const fallbackMessage = message("frontend.common.tryAgain");
        // 서버의 안전한 업무 메시지 또는 공통 재시도 문구를 표시한다
        await sweetError(errorTitle, getApiErrorMessage(error, fallbackMessage));
      }
    };

    // React Effect에서 비동기 신고 결과 안내를 시작한다
    void showComplaintResult();
  }, [pendingResultQuery.data, pendingResultQuery.dataUpdatedAt, pendingResultQuery.refetch]);

  // 별도 화면 영역 없이 공통 알림 모달만 사용한다
  return null;
};

export default ComplaintResultPopup;
