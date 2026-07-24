import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

export type AlimItem = {
  userNumb: number;
  alimNumb: number;
  alimSitu?: string;
  tempCode?: string;
  alimTitl?: string;
  alimCont?: string;
  linkUrlx?: string;
  readYsno?: "Y" | "N";
  readDate?: string;
  sendDate?: string;
  deltYsno?: "Y" | "N";
};

/**
 * 로그인 사용자의 알림 목록을 조회합니다.
 * 서버의 ResultData 공통 응답 검증을 통과한 데이터만 화면으로 전달합니다.
 *
 * @author Hanwon.Jang
 * @return 내 알림 목록 API 응답
 */
export const getMyAlimListApi = async () => {
  const res = await api.get<{ data: AlimItem[] }>("/alim/list");
  return assertResultDataSuccess(res.data);
};
