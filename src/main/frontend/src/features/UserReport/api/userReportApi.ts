import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";
import type { SafetyReportTargetType } from "@/components/UserActionMenu/userActionMenu.types";

export type UserReportCreate = {
  targetType: SafetyReportTargetType;
  targetNumb: number;
  reason: string;
  detailReason?: string;
};

const REPORT_TARGET_CODE = "CMPL_BOOK_REPORT";
const REPLY_TARGET_CODE = "CMPL_REPLY";

/**
 * 인증 사용자의 독후감 또는 댓글 신고를 접수한다.
 *
 * @author Hanwon.Jang
 * @param report 신고 대상과 사유 입력값
 * @return 접수된 신고 번호
 * @throws 신고 접수 실패 또는 신고 번호 누락 시 오류가 발생한다
 */
export async function setUserReportApi(report: UserReportCreate): Promise<number> {

  const tagtType = report.targetType === "REPORT" ? REPORT_TARGET_CODE : REPLY_TARGET_CODE;
  // 인증 쿠키와 CSRF 토큰을 사용하는 공통 API 클라이언트로 신고를 접수한다
  const response = await api.post<ResultData<number>>("/complaints", {
    tagtType,
    tagtNumb: report.targetNumb,
    cmplRson: report.reason,
    cmplCntn: report.detailReason?.trim() || null,
  });
  // HTTP 성공 여부와 별도로 공통 업무 성공 코드를 검증한다
  const result = assertResultDataSuccess(response.data);
  // 서버가 접수 번호를 반환하지 않으면 성공 화면으로 이동하지 않도록 실패 처리한다
  if (!result.data) {
    throw new Error("신고 번호를 확인할 수 없습니다.");
  }
  // 검증된 신고 접수 번호를 화면에 반환한다
  return result.data;
}
