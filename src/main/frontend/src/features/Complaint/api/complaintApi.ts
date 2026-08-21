import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type ComplaintTargetType = "CMPL_USER" | "CMPL_BOOK_REPORT" | "CMPL_REPLY";

export type ComplaintReason =
  | "CMPL_SPAM"
  | "CMPL_ABUSE"
  | "CMPL_SEXUAL"
  | "CMPL_PRIVACY"
  | "CMPL_ILLEGAL"
  | "CMPL_OTHER";

export type ComplaintCreate = {
  tagtType: ComplaintTargetType;
  tagtNumb: number;
  cmplRson: ComplaintReason;
  cmplCntn: string | null;
};

/**
 * 인증 사용자의 콘텐츠 신고를 대상 원문 스냅샷과 함께 접수한다
 *
 * @author SeungHyeon.Kang
 * @param complaint 신고 대상과 사유
 * @return 접수된 신고 번호
 */
export async function setComplaintApi(complaint: ComplaintCreate): Promise<number> {

  // 서버가 대상 원문을 직접 확인해 저장하도록 대상 식별값과 사유만 전달한다
  const response = await api.post<ResultData<number>>("/complaints", complaint);
  const result = assertResultDataSuccess(response.data);
  if (!result.data) {
    // 성공 응답에 신고 번호가 없으면 완료 화면으로 이동하지 않는다
    throw new Error("신고 번호를 확인할 수 없습니다.");
  }
  // 접수된 신고 번호를 반환한다
  return result.data;
}
