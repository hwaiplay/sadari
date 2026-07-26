/**
 * src/main/frontend/src/features/Book/api/bookApi.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
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


export const setReportApi = async (
  data: ReportDtoType,
): Promise<AddBookResponse> => {
  const res = await api.post("/book/setReport", data);
  return assertResultDataSuccess(res.data);
};

export const getDetailApi = async (bookNumb: number) => {
  const res = await api.get<ResultData<ReportDetailType>>(
    `/book/getBookdetail/${bookNumb}`,
  );
  return assertResultDataSuccess(res.data);
};

export const getPublicReportsByIsbnApi = async (isbn: string) => {
  const res = await api.get(
    `/book/publicReports/by-isbn?isbn=${encodeURIComponent(isbn)}`,
  );
  return assertResultDataSuccess(res.data);
};

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

export const setPublicReportLikeApi = (data: LikeTargetParams) => {
  return api.post("/social/like", data).then((res) => {
    return assertResultDataSuccess(res.data);
  });
};

export type BookListParams = {
  bookKeyword?: string;
  sortType?: string;
};

export const getListApi = async (params: BookListParams = {}) => {
  const res = await api.get(`/book/getBookList`, { params });
  return assertResultDataSuccess(res.data);
};

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

export const uptReptStatusGradeApi = async ({
  reptNumb,
  data,
}: UptReptStatusGradeParams): Promise<AddBookResponse> => {
  const res = await api.put(`/book/uptReport/status-grade/${reptNumb}`, data);
  return assertResultDataSuccess(res.data);
};

export const delReportApi = async (reptNumb: number) => {
  const res = await api.delete(`/book/delReport/${reptNumb}`);
  return assertResultDataSuccess(res.data);
};
