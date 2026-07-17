import { message } from "@/app/messages/message";
import {
  REPORT_GRADE_OPTIONS,
  MAX_REPORT_CONTENT_BYTES,
  REPORT_STATUS_READ,
} from "@/features/Book/constants/reportForm";
import type { NaverApiResultType } from "@/features/Book/types/book.type";

const textEncoder = new TextEncoder();

/**
 * 문자열의 UTF-8 byte 길이를 계산합니다.
 *
 * @author Hanwon.Jang
 * @param value byte 길이를 계산할 문자열
 * @return UTF-8 기준 byte 길이
 */
export function getUtf8ByteLength(value: string) {
  return textEncoder.encode(value).length;
}

/**
 * DB 저장 기준 byte 계산을 위해 HTML 특수문자를 entity로 변환합니다.
 *
 * @author Hanwon.Jang
 * @param value escape 처리할 문자열
 * @return HTML entity가 적용된 문자열
 */
function escapeHtmlForStorage(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

/**
 * 독후감 내용이 DB에 저장될 때의 UTF-8 byte 길이를 계산합니다.
 * 화면 입력값은 저장 전에 sanitize와 HTML escape를 거치므로 같은 기준으로 길이를 계산합니다.
 *
 * @author Hanwon.Jang
 * @param value 독후감 내용 입력값
 * @return DB 저장 기준 UTF-8 byte 길이
 */
export function getReportContentStorageByteLength(value: string) {
  return getUtf8ByteLength(escapeHtmlForStorage(sanitizeText(value)));
}

/**
 * 문자열을 UTF-8 byte 제한 안에서 자릅니다.
 *
 * @author Hanwon.Jang
 * @param value 자를 원본 문자열
 * @param maxBytes 허용 가능한 최대 byte 길이
 * @return 최대 byte 길이를 넘지 않는 문자열
 */
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

/**
 * 사용자 입력값에서 script, HTML tag, inline event, javascript scheme을 제거합니다.
 *
 * @author Hanwon.Jang
 * @param value 정리할 입력값
 * @return 위험한 패턴이 제거된 문자열
 */
export function sanitizeText(value: FormDataEntryValue | string | null) {
  return String(value ?? "")
    .trim()
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<[^>]*>/g, "")
    .replace(/\s(on\w+)=["'][^"']*["']/gi, "")
    .replace(/javascript:/gi, "");
}

/**
 * 문자열에서 HTML tag를 제거합니다.
 *
 * @author Hanwon.Jang
 * @param value HTML tag를 제거할 문자열
 * @return HTML tag가 제거된 문자열
 */
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
  validStatusCodes?: string[];
  validReportColors?: string[];
};

/**
 * 독후감 등록과 수정 폼의 필수값, 공통코드, 내용 byte 제한을 검증합니다.
 *
 * @author Hanwon.Jang
 * @param values 화면에서 입력한 독후감 폼 값과 DB 코드 목록
 * @return 검증 실패 메시지, 검증 성공 시 null
 */
export function validateReportForm(values: ReportFormValues) {
  const status = String(values.status ?? "");
  const startDate = String(values.startDate ?? "");
  const endDate = String(values.endDate ?? "");
  const grade = String(values.grade ?? "");
  const reportColr = String(values.reportColr ?? "");
  const content = String(values.content ?? "").trim();
  const missingFields: string[] = [];
  // 완료 상태에서는 화면 라벨이 목표 날짜로 바뀌므로 필수값 안내 문구도 같은 명칭을 사용합니다.
  const startDateFieldName =
    status === REPORT_STATUS_READ
      ? message("frontend.report.field.targetStartDate")
      : message("frontend.report.field.startDate");
  const endDateFieldName =
    status === REPORT_STATUS_READ
      ? message("frontend.report.field.targetEndDate")
      : message("frontend.report.field.endDate");
  const isReadingStatus = status === REPORT_STATUS_READ;
  const gradeNumber = Number(grade);
  const hasGrade = grade !== "";

  if (
    !status ||
    (values.validStatusCodes && !values.validStatusCodes.includes(status))
  ) {
    missingFields.push(message("frontend.report.field.status"));
  }

  if (!startDate) {
    missingFields.push(startDateFieldName);
  }

  if (!endDate) {
    missingFields.push(endDateFieldName);
  }

  if (
    (!isReadingStatus && !hasGrade) ||
    (hasGrade && !(REPORT_GRADE_OPTIONS as readonly number[]).includes(gradeNumber))
  ) {
    missingFields.push(message("frontend.report.field.grade"));
  }

  if (
    !reportColr ||
    (values.validReportColors &&
      !values.validReportColors.some(
        (color) => color.toLowerCase() === reportColr.toLowerCase(),
      ))
  ) {
    missingFields.push(message("frontend.report.field.color"));
  }

  if (!content) {
    missingFields.push(message("frontend.report.field.content"));
  }

  if (missingFields.length > 0) {
    return `${message("frontend.validation.missingPrefix")}\n${missingFields
      .map((field) => `- ${field}`)
      .join("\n")}`;
  }

  if (new Date(startDate) > new Date(endDate)) {
    return message("frontend.validation.invalidDateRange");
  }

  if (getReportContentStorageByteLength(content) > MAX_REPORT_CONTENT_BYTES) {
    return message("frontend.validation.contentByteLimit", [
      MAX_REPORT_CONTENT_BYTES,
    ]);
  }

  return null;
}

/**
 * 독후감 등록 시 선택된 도서 정보가 저장 가능한 형태인지 검증합니다.
 *
 * @author Hanwon.Jang
 * @param book 검증할 도서 검색 결과 객체
 * @return 검증 실패 메시지, 검증 성공 시 null
 */
export function validateSelectedBook(book?: Partial<NaverApiResultType> | null) {
  if (!book) {
    return message("frontend.validation.bookRequired");
  }

  if (
    !String(book.title ?? "").trim() ||
    !String(book.author ?? "").trim() ||
    !String(book.publisher ?? "").trim() ||
    !String(book.isbn ?? "").trim() ||
    !String(book.image ?? "").trim() ||
    !String(book.description ?? "").trim()
  ) {
    return message("frontend.validation.invalidBook");
  }

  return null;
}
