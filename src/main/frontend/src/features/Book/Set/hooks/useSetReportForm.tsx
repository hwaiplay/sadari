/**
 * fileName       : useSetReportForm
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : 독후감 등록 form 로직
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       최초 생성
 */

import { message } from "@/app/messages/message";
import { sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { ReadingStatusType } from "../../types/book.type";
import {
  sanitizeText,
  stripHtmlTags,
  validateReportForm,
  validateSelectedBook,
} from "@/features/Book/utils/reportValidation";
import { useSetReport } from "./useSetReport";

/**
 * 독후감 등록 폼 제출에 필요한 도서 검증, 입력값 검증, 데이터 정제를 수행한다.
 * @Author Hanwon.Jang
 * @param selectedBook 검색 화면에서 선택한 도서 정보
 * @param validStatusCodes DB 공통코드에서 조회한 허용 독서 상태 코드 목록
 * @param validReportColors DB 공통코드에서 조회한 허용 책장 색상 코드 목록
 * @return 등록 진행 상태와 submit 처리 함수
 */
export function useSetReportForm(
  selectedBook: any,
  validStatusCodes: string[],
  validReportColors: string[],
) {
  const { mutate, isPending } = useSetReport();

  const handleSubmit = (form: HTMLFormElement) => {
    const bookValidationMessage = validateSelectedBook(selectedBook);

    if (bookValidationMessage) {
      void sweetWarning(
        message("frontend.alert.inputRequired"), // frontend.alert.inputRequired = 입력이 필요합니다
        bookValidationMessage,
      );
      return;
    }

    const formData = new FormData(form);
    const status = formData.get("status");
    const startDate = formData.get("startDate");
    const endDate = formData.get("endDate");
    const grade = formData.get("grade");
    const reportColr = formData.get("reportColr");
    const content = formData.get("content");

    const validationMessage = validateReportForm({
      status,
      startDate,
      endDate,
      grade,
      reportColr,
      content,
      validStatusCodes,
      validReportColors,
    });

    if (validationMessage) {
      void sweetWarning(
        message("frontend.alert.inputRequired"), // frontend.alert.inputRequired = 입력이 필요합니다
        validationMessage,
      );
      return;
    }

    const data = {
      reportStat: status as ReadingStatusType,
      reportStdt: startDate as string,
      reportEndt: endDate as string,
      reportGrde: grade as string,
      reportColr: reportColr as string,
      reportCntn: sanitizeText(content),
      bookTitl: stripHtmlTags(selectedBook.title),
      bookAthr: stripHtmlTags(selectedBook.author),
      bookPubl: stripHtmlTags(selectedBook.publisher),
      bookIsbn: sanitizeText(selectedBook.isbn),
      bookCvim: sanitizeText(selectedBook.image),
      bookDesc: stripHtmlTags(selectedBook.description),
    };

    mutate(data);
  };

  return { isPending, handleSubmit };
}
