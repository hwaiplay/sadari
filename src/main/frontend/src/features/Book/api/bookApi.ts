/**
 * src/main/frontend/src/features/Book/api/bookApi.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import {
  AddBookResponse,
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
  const res = await api.get(`/book/getBookdetail/${bookNumb}`);
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

export const setPublicReportLikeApi = (reportNumb: number) => {
  return api.post(`/book/publicReports/${reportNumb}/like`).then((res) => {
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
  reportNumb,
  data,
}: uptReportType): Promise<AddBookResponse> => {
  const res = await api.put(`/book/uptReport/${reportNumb}`, data);
  return assertResultDataSuccess(res.data);
};

export type UptReportStatusGradeParams = {
  reportNumb: number;
  data: {
    reportStat: string;
    reportGrde: string;
    reportEndt?: string;
  };
};

export const uptReportStatusGradeApi = async ({
  reportNumb,
  data,
}: UptReportStatusGradeParams): Promise<AddBookResponse> => {
  const res = await api.put(`/book/uptReport/status-grade/${reportNumb}`, data);
  return assertResultDataSuccess(res.data);
};

export const delReportApi = async (reportNumb: number) => {
  const res = await api.delete(`/book/delReport/${reportNumb}`);
  return assertResultDataSuccess(res.data);
};
