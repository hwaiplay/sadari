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
 */

/**
 * 독후감 기록 API
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
 * @returns 독후감 내용
 */
export const getBookDetail = (id: number) => {
  return api.get(`/book/getBookdetail/${id}`);
};
