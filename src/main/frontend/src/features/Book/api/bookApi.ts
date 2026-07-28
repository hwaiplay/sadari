/**
 * src/main/frontend/src/features/Book/api/bookApi.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import api from "@/app/api/axios";
import {
  assertResultDataSuccess,
  type ResultData,
} from "@/app/api/resultData";
import {
  AddBookResponse,
  ReportDetailType,
  ReportDtoType,
  uptReportType,
} from "../types/book.type";


/**
 * set Report 정보를 설정하거나 등록한다
 *
 * @author HanWon.Jang
 * @param data data 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const setReportApi = async (
  data: ReportDtoType,
): Promise<AddBookResponse> => {

  const res = await api.post("/book/setReport", data);
  return assertResultDataSuccess(res.data);
};

/**
 * get Detail 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param bookNumb book Numb 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getDetailApi = async (bookNumb: number) => {

  const res = await api.get<ResultData<ReportDetailType>>(
    `/book/getBookdetail/${bookNumb}`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * get Public Reports By Isbn 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getPublicReportsByIsbnApi = async (isbn: string) => {

  const res = await api.get(
    `/book/publicReports/by-isbn?isbn=${encodeURIComponent(isbn)}`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * get Book Rating Average By Isbn 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getBookRatingAverageByIsbnApi = async (isbn: string) => {

  const res = await api.get(
    `/book/ratingAverage/by-isbn?isbn=${encodeURIComponent(isbn)}`,
  );
  return assertResultDataSuccess(res.data);
};

export type LikeTargetParams = {
  tagtType: string;
  tagtNumb: number;
  // 알림 대상 DB 재조회를 없애기 위해 화면이 이미 조회한 독후감 작성자 번호를 함께 전송합니다.
  targetUserNumb: number;
};

/**
 * set Public Report Like 정보를 설정하거나 등록한다
 *
 * @author HanWon.Jang
 * @param data data 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const setPublicReportLikeApi = (data: LikeTargetParams) => {

  return api.post("/social/like", data).then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

export type BookListParams = {
  bookKeyword?: string;
  sortType?: string;
};

/**
 * get List 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param params params 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getListApi = async (params: BookListParams = {}) => {

  const res = await api.get(`/book/getBookList`, { params });
  return assertResultDataSuccess(res.data);
};

/**
 * upt Report 정보를 수정한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const uptReportApi = async ({
  reptNumb,
  data,
}: uptReportType): Promise<AddBookResponse> => {

  const res = await api.put(`/book/uptReport/${reptNumb}`, data);
  return assertResultDataSuccess(res.data);
};

export type UptReptStatusGradeParams = {
  reptNumb: number;
  data: {
    reptStat: string;
    reptGrde: string;
    reptEndt?: string;
  };
};

/**
 * upt Rept Status Grade 정보를 수정한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const uptReptStatusGradeApi = async ({
  reptNumb,
  data,
}: UptReptStatusGradeParams): Promise<AddBookResponse> => {

  const res = await api.put(`/book/uptReport/status-grade/${reptNumb}`, data);
  return assertResultDataSuccess(res.data);
};

/**
 * del Report 정보를 삭제한다
 *
 * @author HanWon.Jang
 * @param reptNumb rept Numb 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const delReportApi = async (reptNumb: number) => {

  const res = await api.delete(`/book/delReport/${reptNumb}`);
  return assertResultDataSuccess(res.data);
};
