import { message } from "@/app/messages/message";
import { MAX_REPORT_CONTENT_BYTES } from "@/features/Book/constants/reportForm";
import { ReadingStatusType } from "@/features/Book/types/book.type";

const VALID_STATUSES: ReadingStatusType[] = ["done", "reading", "stopped"];
const HEX_COLOR_PATTERN = /^#([0-9a-fA-F]{6})$/;
const textEncoder = new TextEncoder();

export function getUtf8ByteLength(value: string) {
  return textEncoder.encode(value).length;
}

function escapeHtmlForStorage(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

export function getReportContentStorageByteLength(value: string) {
  return getUtf8ByteLength(escapeHtmlForStorage(sanitizeText(value)));
}

export function truncateUtf8Bytes(
  value: string,
  maxBytes = MAX_REPORT_CONTENT_BYTES,
) {
  let bytes = 0;
  let result = "";

  for (const char of value) {
    const charBytes = getReportContentStorageByteLength(char);

    if (bytes + charBytes > maxBytes) {
      break;
    }

    bytes += charBytes;
    result += char;
  }

  return result;
}

// 저장 전 프론트에서 위험한 HTML/script 패턴을 1차 제거한다.
export function sanitizeText(value: FormDataEntryValue | string | null) {
  return String(value ?? "")
    .trim()
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<[^>]*>/g, "")
    .replace(/\s(on\w+)=["'][^"']*["']/gi, "")
    .replace(/javascript:/gi, "");
}

// 네이버 도서 검색 응답에는 HTML 강조 태그가 올 수 있어 표시/저장 전에 제거한다.
export function stripHtmlTags(value?: string) {
  return sanitizeText(value ?? "");
}

type ReportFormValues = {
  status: FormDataEntryValue | null;
  startDate: FormDataEntryValue | null;
  endDate: FormDataEntryValue | null;
  grade: FormDataEntryValue | null;
  reportColr: FormDataEntryValue | null;
  content: FormDataEntryValue | null;
};

// 등록/수정 공통 필수값을 제출 직전에 검사하고, 누락된 항목을 한 번에 알려준다.
export function validateReportForm(values: ReportFormValues) {
  const status = String(values.status ?? "");
  const startDate = String(values.startDate ?? "");
  const endDate = String(values.endDate ?? "");
  const grade = String(values.grade ?? "");
  const reportColr = String(values.reportColr ?? "");
  const content = String(values.content ?? "").trim();
  const missingFields: string[] = [];

  if (!VALID_STATUSES.includes(status as ReadingStatusType)) {
    missingFields.push(message("frontend.report.field.status")); // frontend.report.field.status = 독서 상태
  }

  if (!startDate) {
    missingFields.push(message("frontend.report.field.startDate")); // frontend.report.field.startDate = 시작일
  }

  if (!endDate) {
    missingFields.push(message("frontend.report.field.endDate")); // frontend.report.field.endDate = 종료일
  }

  if (!grade || Number(grade) < 1 || Number(grade) > 5) {
    missingFields.push(message("frontend.report.field.grade")); // frontend.report.field.grade = 평점
  }

  if (!HEX_COLOR_PATTERN.test(reportColr)) {
    missingFields.push(message("frontend.report.field.color")); // frontend.report.field.color = 책장 색상
  }

  if (!content) {
    missingFields.push(message("frontend.report.field.content")); // frontend.report.field.content = 기록
  }

  if (missingFields.length > 0) {
    return `${message("frontend.validation.missingPrefix")}\n${missingFields // frontend.validation.missingPrefix = 다음 항목을 입력해주세요.
      .map((field) => `- ${field}`)
      .join("\n")}`;
  }

  if (getReportContentStorageByteLength(content) > MAX_REPORT_CONTENT_BYTES) {
    return message("frontend.validation.contentByteLimit", [
      MAX_REPORT_CONTENT_BYTES,
    ]); // frontend.validation.contentByteLimit = 독후감 내용은 {0}byte 이하로 입력해주세요.
  }

  return null;
}

// 독후감 등록은 선택한 책 정보를 함께 저장하므로 책 필수값을 별도로 확인한다.
export function validateSelectedBook(book: any) {
  if (!book) {
    return message("frontend.validation.bookRequired"); // frontend.validation.bookRequired = 책을 선택해주세요.
  }

  if (
    !String(book.title ?? "").trim() ||
    !String(book.author ?? "").trim() ||
    !String(book.publisher ?? "").trim() ||
    !String(book.isbn ?? "").trim() ||
    !String(book.image ?? "").trim() ||
    !String(book.description ?? "").trim()
  ) {
    return message("frontend.validation.invalidBook"); // frontend.validation.invalidBook = 선택한 책 정보가 올바르지 않습니다. 다른 책을 선택해주세요.
  }

  return null;
}
