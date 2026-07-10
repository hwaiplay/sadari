import { message } from "@/app/messages/message";
import { MAX_REPORT_CONTENT_BYTES } from "@/features/Book/constants/reportForm";

const textEncoder = new TextEncoder();

/**
 * 문자열의 UTF-8 저장 바이트 길이를 계산한다.
 * @Author Hanwon.Jang
 * @param value 바이트 길이를 계산할 문자열
 * @return UTF-8 인코딩 기준 바이트 수
 */
export function getUtf8ByteLength(value: string) {
  return textEncoder.encode(value).length;
}

/**
 * DB 저장 전에 HTML 특수문자가 escape 되었을 때의 문자열을 만든다.
 * @Author Hanwon.Jang
 * @param value escape 대상 문자열
 * @return HTML 엔티티로 변환된 문자열
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
 * 독후감 본문이 XSS 필터와 HTML escape를 거친 뒤 실제로 차지할 저장 바이트를 계산한다.
 * @Author Hanwon.Jang
 * @param value 독후감 본문 입력값
 * @return DB 저장 기준 UTF-8 바이트 수
 */
export function getReportContentStorageByteLength(value: string) {
  return getUtf8ByteLength(escapeHtmlForStorage(sanitizeText(value)));
}

/**
 * 독후감 본문을 지정된 최대 바이트 이하로 잘라낸다.
 * @Author Hanwon.Jang
 * @param value 잘라낼 원본 문자열
 * @param maxBytes 허용할 최대 저장 바이트 수
 * @return 최대 바이트를 넘지 않는 문자열
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

// 저장 전 프론트에서 위험한 HTML/script 패턴을 1차 제거한다.
/**
 * 사용자 입력값에서 script, HTML 태그, 이벤트 속성, javascript 스킴을 제거한다.
 * @Author Hanwon.Jang
 * @param value 정제할 폼 값 또는 문자열
 * @return 위험 패턴이 제거된 문자열
 */
export function sanitizeText(value: FormDataEntryValue | string | null) {
  return String(value ?? "")
    .trim()
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<[^>]*>/g, "")
    .replace(/\s(on\w+)=["'][^"']*["']/gi, "")
    .replace(/javascript:/gi, "");
}

// 네이버 도서 검색 응답에는 HTML 강조 태그가 올 수 있어 표시/저장 전에 제거한다.
/**
 * 외부 책 검색 응답에 포함될 수 있는 HTML 태그를 제거한다.
 * @Author Hanwon.Jang
 * @param value HTML 태그 제거 대상 문자열
 * @return 태그가 제거된 일반 텍스트
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

// 등록/수정 공통 필수값을 제출 직전에 검사하고, 누락된 항목을 한 번에 알려준다.
/**
 * 독후감 등록과 수정 폼의 필수값, 코드값, 본문 바이트 제한을 검증한다.
 * @Author Hanwon.Jang
 * @param values 폼에서 읽은 독후감 입력값과 DB 공통코드 기반 허용 목록
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

  if (
    !status ||
    (values.validStatusCodes && !values.validStatusCodes.includes(status))
  ) {
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

  if (
    !reportColr ||
    (values.validReportColors &&
      !values.validReportColors.some(
        (color) => color.toLowerCase() === reportColr.toLowerCase(),
      ))
  ) {
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
/**
 * 독후감 등록 시 함께 저장할 선택 도서 정보가 모두 존재하는지 검증한다.
 * @Author Hanwon.Jang
 * @param book 검색 화면에서 선택한 도서 객체
 * @return 검증 실패 메시지, 검증 성공 시 null
 */
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
