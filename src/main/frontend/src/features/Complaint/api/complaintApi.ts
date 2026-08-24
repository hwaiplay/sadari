import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type ComplaintTargetType =
  | "CMPL_USER"
  | "CMPL_BOOK_REPORT"
  | "CMPL_REPLY"
  | "CMPL_PROF_IMAGE"
  | "CMPL_BG_IMAGE"
  | "CMPL_INTRO";

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

export type ComplaintPendingResult = {
  rsltCntt: number;
  lastRsltNumb: number | null;
  resultList: ComplaintResultItem[];
};

export type ComplaintResultItem = {
  rsltNumb: number;
  rcvrType: "REPORTER" | "TARGET";
  cmplNumb: number | null;
  tagtName: string;
  rsonName: string;
  rsltCntn: string;
  cmplDate: string | null;
  procDate: string;
};

/**
 * 인증 사용자의 콘텐츠 신고를 대상 원문 스냅샷과 함께 접수한다
 *
 * @author SeungHyeon.Kang
 * @param complaint 신고 대상과 사유
 * @return 접수된 신고 번호
 */
export const setComplaintApi = async (complaint: ComplaintCreate): Promise<number> => {

  // 서버가 대상 원문을 직접 확인해 저장하도록 대상 식별값과 사유만 전달한다
  const response = await api.post<ResultData<number>>("/complaints", complaint);
  const result = assertResultDataSuccess(response.data);
  if (!result.data) {
    // 성공 응답에 신고 번호가 없으면 완료 화면으로 이동하지 않는다
    throw new Error("신고 번호를 확인할 수 없습니다.");
  }
  // 접수된 신고 번호를 반환한다
  return result.data;
};

/**
 * 활성 사용자가 아직 확인하지 않은 신고 조치 결과 상세를 조회한다
 *
 * @author HanWon.Jang
 * @return 미확인 결과 목록과 조회 시점의 마지막 결과 번호
 */
export const getPendingResultApi = async (): Promise<ComplaintPendingResult> => {
  // 인증 사용자의 미확인 신고 조치 결과를 조회한다
  const response = await api.get<ResultData<ComplaintPendingResult>>("/complaints/results/pending");
  const result = assertResultDataSuccess(response.data);

  // 서버 응답에 결과 요약이 없으면 확인 처리 경계를 만들지 않는다
  if (!result.data) {
    // API 계약 누락을 호출 화면의 조회 실패 경로로 전달한다
    throw new Error("신고 조치 결과를 확인할 수 없습니다.");
  }

  // 팝업 표시와 확인 요청에 사용할 미확인 결과 요약을 반환한다
  return result.data;
};

/**
 * 팝업 조회 시점의 마지막 번호까지 신고 조치 결과를 확인 처리한다
 *
 * @author HanWon.Jang
 * @param resultNumb 조회 시점의 마지막 신고 조치 결과 번호
 * @return 반환값이 없다
 */
export const uptResultConfirmApi = async (resultNumb: number): Promise<void> => {
  // 조회 이후 생성된 결과를 제외하도록 마지막 결과 번호를 경로에 전달한다
  const response = await api.patch<ResultData>(`/complaints/results/${resultNumb}`);
  assertResultDataSuccess(response.data);
};
