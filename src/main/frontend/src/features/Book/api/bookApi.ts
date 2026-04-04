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

import api from "@/app/api/axios";
import { BookFormType } from "../types/book.type";

export const addBookReport = (data: BookFormType) => {
  return api.post("/book/addBookReport", data);
};
