import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type UserSuspension = {
  spndNumb: number;
  spndType: "PERIOD" | "INDEFINITE";
  spndTypeName: string;
  spndRson: string;
  spndRsonName: string;
  spndStat: "ACTIVE" | "EXPIRED";
  spndStatName: string;
  strtDate: string;
  endxDate?: string | null;
  rlesDate?: string | null;
};

/**
 * 로그인 회원에게 공개할 현재 이용 정지 정보를 조회합니다.
 *
 * @author HanWon.Jang
 * @return 내부 관리자 메모를 제외한 이용 정지 정보
 */
export const getUserSuspensionApi = async () => {

  const response = await api.get("/user/suspension");
  // 공통 성공 코드 검증이 끝난 현재 이용 정지 정보를 반환합니다
  return assertResultDataSuccess(response.data) as ResultData<UserSuspension | null>;
};
