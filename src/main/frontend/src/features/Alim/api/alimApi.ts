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
  alimIconMimeType?: string;
  alimIconData?: string;
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

export type AlimTargetData = {
  linkUrlx: string;
};

/**
 * 로그인 사용자의 알림 목록을 조회합니다.
 * 읽음 상태는 변경하지 않고 삭제되지 않은 알림을 읽음 여부와 함께 화면으로 전달합니다.
 *
 * @author HanWon.Jang
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
 * 목록 전체를 조회하지 않고 배지 숫자만 갱신할 때 사용합니다.
 *
 * @author HanWon.Jang
 * @return 미읽음 알림 수 API 응답
 */
export const getUnreadAlimCntApi = async () => {

  const res = await api.get<{ data: AlimUnreadCntData }>("/alim/unread-count");
  return assertResultDataSuccess(res.data);
};

/**
 * 사용자별 알림번호와 클릭 시점의 접근 상태로 계산된 내부 이동 주소를 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param alimNumb 이동할 사용자별 알림 번호
 * @return 현재 접근 권한이 반영된 알림 이동 주소 API 응답
 */
export const getAlimTargetApi = async (alimNumb: number) => {

  const res = await api.get<{ data: AlimTargetData }>(
    `/alim/notification-target/${alimNumb}`,
  );
  // 공통 응답 검증이 끝난 알림 이동 주소를 반환한다
  return assertResultDataSuccess(res.data);
};

/**
 * 알림센터 항목 또는 푸시 알림을 클릭한 경우 사용자별 알림 한 건을 읽음 처리합니다.
 *
 * @author HanWon.Jang
 * @param alimNumb 읽음 처리할 사용자별 알림 번호
 * @return 읽음 처리 후 남은 미읽음 알림 수 API 응답
 */
export const uptAlimReadApi = async (alimNumb: number) => {

  const res = await api.put<{ data: AlimUnreadCntData }>("/alim/read-status", {
    alimNumb,
  });
  return assertResultDataSuccess(res.data);
};

/**
 * 화면에 아직 불러오지 않은 알림까지 모두 삭제 상태로 변경합니다.
 * 알림 페이지의 모두 지우기 버튼에서만 사용합니다.
 *
 * @author HanWon.Jang
 * @return 모두 지우기 처리 API 응답
 */
export const delAllAlimApi = async () => {

  const res = await api.post<{ data: AlimUnreadCntData }>("/alim/delete-all");
  return assertResultDataSuccess(res.data);
};
