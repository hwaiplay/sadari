/**
 * src/main/frontend/src/features/Book/api/bookApi.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import api from "@/app/api/axios";
import {
  AddBookResponse,
  ReportDtoType,
  uptReportType,
} from "../types/book.type";


export const setReportApi = async (
  data: ReportDtoType,
): Promise<AddBookResponse> => {
  const res = await api.post("/book/setReport", data);
  return res.data;
};

export const getDetailApi = (bookNumb: number) => {
  return api.get(`/book/getBookdetail/${bookNumb}`);
};

export const getPublicReportsByIsbnApi = (isbn: string) => {
  return api.get(`/book/publicReports/by-isbn?isbn=${encodeURIComponent(isbn)}`);
};

export const getBookRatingAverageByIsbnApi = (isbn: string) => {
  return api.get(`/book/ratingAverage/by-isbn?isbn=${encodeURIComponent(isbn)}`);
};

export const setPublicReportLikeApi = (reportNumb: number) => {
  return api.post(`/book/publicReports/${reportNumb}/like`);
};

export type BookListParams = {
  bookKeyword?: string;
  sortType?: string;
};

export const getListApi = (params: BookListParams = {}) => {
  return api.get(`/book/getBookList`, { params });
};

export const uptReportApi = async ({
  reportNumb,
  data,
}: uptReportType): Promise<AddBookResponse> => {
  const res = await api.put(`/book/uptReport/${reportNumb}`, data);
  return res.data;
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
  return res.data;
};

export const delReportApi = async (reportNumb: number) => {
  const res = await api.delete(`/book/delReport/${reportNumb}`);
  return res.data;
};
