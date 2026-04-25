import api from "@/app/api/axios";
import { AddBookResponse, AddBookReportRequest } from "../types/book.type";

/**
 * fileName       : bookApi
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : 독후감 관련 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 * 2026-04-25       hanwon.Jang       주석 수정 및 독후감 리스트 조회 API 추가
 */

/**
 * 독후감 등록 API
 * @param data 선책한 책&독후감
 * @returns 독후감 id
 */
export const addBookReport = async (
  data: AddBookReportRequest,
): Promise<AddBookResponse> => {
  const res = await api.post("/book/addBookReport", data);
  return res.data;
};

/**
 * 독후감 상세보기 API
 * @param 독후감 id
 * @returns 독후감 상세내용
 */
export const getBookDetail = (id: number) => {
  return api.get(`/book/getBookdetail/${id}`);
};

/**
 * 독후감 리스트 조회 API
 * @param userNumb 유저넘버
 * @returns 독후감 리스트
 */
export const getBooklist = (userNumb: string) => {
  return api.get(`/book/getBookList/${userNumb}`);
};
