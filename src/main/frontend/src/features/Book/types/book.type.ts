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
 * 책 검색 후 선택한 책 타입
 */
export interface SelectedBookType {
  title: string;
  author: string;
  publisher: string;
  isbn: string;
  image: string;
  description: string;
}

/**
 * 독후감 기록하기
 */
// 독서 진행 상태
export type ReadingStatusType = "done" | "reading" | "stopped";

// 기록하기 데이터 타입
export interface AddBookReportRequest {
  bookDto: SelectedBookType;
  bookReportDto: {
    status: ReadingStatusType;
    startDate: string;
    endDate: string;
    grade: string;
    content: string;
  };
}

// 기록 후 백엔드 응답
export interface AddBookResponse {
  success: boolean;
  data: number; // bookId
}
