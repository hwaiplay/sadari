import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type WithdrawalType = "SOFT" | "HARD";
export type WithdrawalReason = "LOW_USAGE" | "INCONVENIENT" | "PRIVACY" | "OTHER";

export type WithdrawalRequest = {
  wthdType: WithdrawalType;
  wthdRson: WithdrawalReason;
  rsonCntn?: string;
};

export type WithdrawalStatus = {
  wthdNumb: number;
  deltDate: string;
  wthdStat: string;
};

export type WithdrawalCancelStatus = "ACTIVE" | "SUSPENDED";

/**
 * 회원 탈퇴 정책을 저장하고 Kakao 재인증 URL을 요청합니다.
 *
 * @author HanWon.Jang
 * @param request 탈퇴 유형과 사유
 * @return Kakao 재인증 URL을 포함한 API 응답
 */
export const setWithdrawalRequestApi = async (request: WithdrawalRequest) => {

  // 검증된 탈퇴 유형과 사유를 서버에 전달합니다
  const response = await api.post("/user/withdrawal/reauth", request);
  // 공통 성공 코드가 확인된 재인증 URL을 반환합니다
  return assertResultDataSuccess(response.data) as ResultData<{ authUrl: string }>;
};

/**
 * 로그인 회원의 영구 삭제 대기 정보를 조회합니다.
 *
 * @author HanWon.Jang
 * @return 영구 삭제 예정일을 포함한 API 응답
 */
export const getWithdrawalStatusApi = async () => {

  // 영구 삭제 대기 상태와 예정일을 조회합니다
  const response = await api.get("/user/withdrawal/status");
  // 공통 성공 코드가 확인된 삭제 대기 정보를 반환합니다
  return assertResultDataSuccess(response.data) as ResultData<WithdrawalStatus | null>;
};

/**
 * 영구 삭제 대기를 취소하고 회원 상태를 복구합니다.
 *
 * @author HanWon.Jang
 * @return 복구 처리 API 응답
 */
export const uptWithdrawalCancelApi = async () => {

  // 영구 삭제 대기 취소를 서버에 요청합니다
  const response = await api.post("/user/withdrawal/cancel");
  // 공통 성공 코드가 확인된 취소 결과를 반환합니다
  return assertResultDataSuccess(response.data) as ResultData<WithdrawalCancelStatus>;
};
