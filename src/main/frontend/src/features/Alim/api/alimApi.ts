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
  alimIconCode?: string;
  alimIconName?: "HEART" | "FOLLOW" | string;
};

export type AlimListData = {
  list: AlimItem[];
  hasNext: boolean;
  nextPage: number;
  unreadCnt: number;
};

export type AlimUnreadCntData = {
  unreadCnt: number;
};

/**
 * 로그인 사용자의 알림 목록을 조회합니다.
 * 서버의 ResultData 공통 응답 검증을 통과한 데이터만 화면으로 전달합니다.
 *
 * @author Hanwon.Jang
 * @return 내 알림 목록 API 응답
 */
export const getMyAlimListApi = async (page = 1) => {
  const res = await api.get<{ data: AlimListData }>("/alim/list", {
    params: { page },
  });
  return assertResultDataSuccess(res.data);
};

/**
 * 햄버거 메뉴 알림 버튼에 표시할 미읽음 알림 수만 조회합니다.
 * 목록 API는 읽음 처리를 동반하므로 배지 갱신에는 이 API를 따로 사용합니다.
 *
 * @author Hanwon.Jang
 * @return 미읽음 알림 수 API 응답
 */
export const getUnreadAlimCntApi = async () => {
  const res = await api.get<{ data: AlimUnreadCntData }>("/alim/unread-count");
  return assertResultDataSuccess(res.data);
};

/**
 * 화면에 아직 불러오지 않은 알림까지 모두 읽음 처리합니다.
 * 알림 페이지의 모두 읽음 버튼에서만 사용합니다.
 *
 * @author Hanwon.Jang
 * @return 모두 읽음 처리 API 응답
 */
export const readAllAlimApi = async () => {
  const res = await api.post<{ data: AlimUnreadCntData }>("/alim/read-all");
  return assertResultDataSuccess(res.data);
};
