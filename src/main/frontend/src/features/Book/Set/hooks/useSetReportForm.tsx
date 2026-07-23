/**
 * src/main/frontend/src/features/Book/Set/hooks/useSetReportForm.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */

import { message } from "@/app/messages/message";
import { sweetConfirm, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import type {
  NaverApiResultType,
  ReadingStatusType,
} from "../../types/book.type";
import {
  sanitizeText,
  normalizeBookAuthor,
  stripHtmlTags,
  validateReportForm,
  validateSelectedBook,
} from "@/features/Book/utils/reportValidation";
import { useSetReport } from "./useSetReport";

export function useSetReportForm(
  selectedBook: NaverApiResultType | undefined,
  validStatusCodes: string[],
  validReportColors: string[],
) {
  const { mutate, isPending } = useSetReport();

  const handleSubmit = async (form: HTMLFormElement) => {
    const bookValidationMessage = validateSelectedBook(selectedBook);

    if (bookValidationMessage) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        bookValidationMessage,
      );
      return;
    }

    if (!selectedBook) {
      return;
    }

    const formData = new FormData(form);
    const status = formData.get("status");
    const startDate = formData.get("startDate");
    const endDate = formData.get("endDate");
    const grade = formData.get("grade");
    const reptColr = formData.get("reptColr");
    const pubcYsno = formData.get("pubcYsno");
    const content = formData.get("content");

    const validationMessage = validateReportForm({
      status,
      startDate,
      endDate,
      grade,
      reptColr,
      content,
      validStatusCodes,
      validReportColors,
    });

    if (validationMessage) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        validationMessage,
      );
      return;
    }

    const normalizedPubcYsno: "Y" | "N" = pubcYsno === "Y" ? "Y" : "N";
    const normalizedGrade = grade ? String(grade) : "0";
    const data = {
      reptStat: status as ReadingStatusType,
      reptStdt: startDate as string,
      reptEndt: endDate as string,
      reptGrde: normalizedGrade,
      reptColr: reptColr as string,
      pubcYsno: normalizedPubcYsno,
      reptCntn: sanitizeText(content),
      bookTitl: stripHtmlTags(selectedBook.title),
      bookAthr: normalizeBookAuthor(selectedBook.author),
      bookPubl: stripHtmlTags(selectedBook.publisher),
      bookIsbn: sanitizeText(selectedBook.isbn),
      bookCvim: sanitizeText(selectedBook.image),
      bookDesc: stripHtmlTags(selectedBook.description),
      publDate: stripHtmlTags(selectedBook.pubdate),
    };

    const confirmed = await sweetConfirm({
      title: message("frontend.alert.saveConfirmTitle"),
      text: message("frontend.report.saveConfirmText"),
      confirmButtonText: message("frontend.report.save"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (!confirmed.isConfirmed) {
      return;
    }

    mutate(data);
  };

  return { isPending, handleSubmit };
}
