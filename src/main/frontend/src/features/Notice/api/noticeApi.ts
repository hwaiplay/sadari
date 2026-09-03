import api, { type SadariRequestConfig } from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type Notice = {
  notiNumb: number;
  versNumb: number;
  cateCode: string;
  cateName: string;
  notiTitl: string;
  notiCntn?: string;
  topxYsno: "Y" | "N";
  readYsno: "Y" | "N";
  dplyDate: string;
};

export type NoticePage = {
  list: Notice[];
  page: number;
  hasNext: boolean;
};

export type UnreadNotice = Pick<Notice, "notiNumb" | "cateName" | "notiTitl">;

/**
 * 현재 배포 중인 공지사항 목록 페이지를 조회함
 *
 * @author SeungHyeon.Kang
 * @param page 조회할 페이지 번호
 * @return 배포된 공지사항 목록과 다음 페이지 여부
 * @throws 공통 응답 실패 또는 목록 데이터가 없을 때 발생
 */
export const getNoticeListApi = async (page: number): Promise<NoticePage> => {

  // 인증 사용자의 공지사항 목록 API를 페이지 번호와 함께 호출함
  const response = await api.get<ResultData<NoticePage>>("/notices", { params: { page } });
  const result = assertResultDataSuccess(response.data);

  // 정상 응답에 목록 데이터가 없으면 화면에서 오류 상태로 처리할 수 있도록 예외를 발생시킴
  if (!result.data) {
    throw new Error("공지사항 목록이 없습니다.");
  }

  // 검증된 공지사항 목록 응답을 반환함
  return result.data;
};

/**
 * 홈 화면에 표시할 로그인 사용자의 미읽음 공지 카테고리와 제목 목록을 조회함
 *
 * @author SeungHyeon.Kang
 * @return 현재 배포 중인 미읽음 공지 번호와 카테고리명 및 제목 목록
 * @throws 공통 응답 실패 시 발생
 */
export const getUnreadNoticeListApi = async (): Promise<UnreadNotice[]> => {
  // 로그인 사용자의 읽음 이력이 없는 현재 배포 공지 목록 API를 호출함
  const response = await api.get<ResultData<UnreadNotice[]>>("/notices/unread");
  // 공통 성공 코드가 확인된 미읽음 공지 목록 응답을 추출함
  const result = assertResultDataSuccess(response.data);

  // 응답 데이터가 없으면 홈 미읽음 공지 안내를 숨길 수 있도록 빈 목록을 반환함
  return result.data ?? [];
};

/**
 * 주키에 해당하는 현재 배포 공지사항 상세를 조회함
 *
 * @author SeungHyeon.Kang
 * @param notiNumb 조회할 공지사항 주키
 * @return 현재 배포 중인 공지사항 상세
 * @throws 공통 응답 실패 또는 현재 배포 상세가 없을 때 발생
 */
export const getNoticeDetailApi = async (notiNumb: number): Promise<Notice> => {

  // 인증 사용자의 공지사항 상세 API를 공지사항 주키로 호출함
  const response = await api.get<ResultData<Notice>>(`/notices/${notiNumb}`);
  const result = assertResultDataSuccess(response.data);

  // 현재 배포 중인 상세 데이터가 없으면 사용자 화면에서 오류 상태로 처리함
  if (!result.data) {
    throw new Error("공지사항을 찾을 수 없습니다.");
  }

  // 검증된 공지사항 상세 응답을 반환함
  return result.data;
};

/**
 * 주키에 해당하는 현재 배포 공지사항의 읽음 이력을 저장함
 *
 * @author SeungHyeon.Kang
 * @param notiNumb 읽은 공지사항 주키
 * @return 반환값이 없음
 * @throws 공통 응답 실패 시 발생
 */
export const setNoticeViewApi = async (notiNumb: number): Promise<void> => {
  // 화면 조회에 수반되는 이력 저장은 공통 처리 중 화면에서 제외함
  const requestConfig: SadariRequestConfig = { skipBlockingOperation: true };
  // CSRF 보호 POST 요청으로 현재 배포 공지의 읽음 이력을 저장함
  const response = await api.post<ResultData<null>>(`/notices/${notiNumb}/views`, null, requestConfig);
  // 공통 성공 코드가 확인되지 않으면 상세 화면의 실패 경로로 전달함
  assertResultDataSuccess(response.data);
};
