export const MAX_REPORT_CONTENT_BYTES = 4000;
export const REPORT_STATUS_CODE_GROUP = "READ_STAT";
export const REPORT_COLOR_CODE_GROUP = "BOOK_COLR";
// 독후감 등록·수정 화면이 한 번의 공통코드 일괄 API로 조회할 코드 그룹임
export const REPORT_FORM_CODE_GROUPS = [
  REPORT_STATUS_CODE_GROUP,
  REPORT_COLOR_CODE_GROUP,
] as const;
export const REPORT_STATUS_READ = "READ";
export const REPORT_STATUS_DONE = "DONE";
export const REPORT_STATUS_STOP = "STOP";
export const REPORT_GRADE_VALUES = [
  0,
  0.5,
  1,
  1.5,
  2,
  2.5,
  3,
  3.5,
  4,
  4.5,
  5,
] as const;
export const REPORT_GRADE_OPTIONS = [1, 2, 3, 4, 5] as const;
