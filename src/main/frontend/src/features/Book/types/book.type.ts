/**
 * fileName       : book.type
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    : 책 관련 타입 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

/**
 * 책 검색
 */
export interface BookSearchType {
  title: string;
  author: string;
  publisher: string;
  isbn: string;
  image: string;
  description: string;
}

export interface SelectBookType {
  title: string;
  isbn: string;
  image: string;
}

/**
 * 기록하기 폼
 */
// 독서 진행 상태
export type ReadingStatusType = "done" | "reading" | "stopped";

export interface BookFormType {
  coverImage: string;
  readingStatus: string;
  readStartDate: string;
  readEndDate: string;
  grade: string;
  content: string;
}
