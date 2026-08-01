/**
 * src/main/frontend/src/features/Book/Set/hooks/useSetReportForm.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */

import { message } from "@/app/messages/message";
import { sweetConfirm, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import type {
  BookSearchResultType,
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
import { REPORT_STATUS_READ } from "@/features/Book/constants/reportForm";

/**
 * use Set Report Form 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @param selectedBook selected Book 입력값
 * @param validStatusCodes valid Status Codes 입력값
 * @param validReportColors valid Report Colors 입력값
 * @return 화면에서 사용할 상태와 처리 함수
 */
export function useSetReportForm(
  selectedBook: BookSearchResultType | undefined,
  validStatusCodes: string[],
  validReportColors: string[],
) {

  const { mutate, isPending } = useSetReport();

  /**
   * handle Submit 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param form form 입력값
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
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

    const isReadingStatus = status === REPORT_STATUS_READ;
    let normalizedPubcYsno: "Y" | "N" = "N";
    let normalizedGrade = "0";

    // 완료와 중단 상태에서만 사용자가 선택한 공개 여부와 평점을 저장 요청에 반영한다
    if (!isReadingStatus) {
      // 공개를 명시적으로 선택한 경우에만 공개값을 사용한다
      normalizedPubcYsno = pubcYsno === "Y" ? "Y" : "N";
      // 평점이 비어 있으면 미선택 내부값 0을 사용한다
      normalizedGrade = grade ? String(grade) : "0";
    }
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
