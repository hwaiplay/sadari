/**
 * fileName       : useSetReportForm
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : form 로직 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       최초 생성
 */

import { ReadingStatusType } from "../../types/book.type";
import { useAddBookMutation } from "./useAddBookMutation";

export function useSetReportForm(selectedBook: any) {
  // 백엔드 응답
  const { mutate, isPending } = useAddBookMutation();

  // 폼 action
  const handleSubmit = (form: HTMLFormElement) => {
    const formData = new FormData(form);
    // 독서상태
    const status = formData.get("status");
    // 독서 시작일
    const startDate = formData.get("startDate");
    // 독서 종료일
    const endDate = formData.get("endDate");
    // 별점
    const grade = formData.get("grade");
    // 독후감 내용
    const content = formData.get("content");

    const data = {
      bookTitl: selectedBook.title,
      bookAthr: selectedBook.author,
      bookPubl: selectedBook.publisher,
      bookIsbn: selectedBook.isbn,
      bookCvim: selectedBook.image,
      bookDesc: selectedBook.description,
      bookStat: status as ReadingStatusType,
      bookStdt: startDate as string,
      bookEndt: endDate as string,
      bookGrde: grade as string,
      bookCntn: content as string,
    };

    mutate(data);
  };

  return { isPending, handleSubmit };
}
