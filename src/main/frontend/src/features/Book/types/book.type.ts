import { title } from "./../../../pages/Login/Login.css";
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
 * "네이버 검색 결과 타입"
 */
export interface NaverBookResultType {
  // 책 제목
  title: string;
  // 저자
  author: string;
  // 출판사
  publisher: string;
  // 책 isbn
  isbn: string;
  // 책 표지 이미지
  image: string;
  // 책 소개 내용
  description: string;
}

/**
 * "책 타입"
 */
export interface BookType {
  // 책 제목
  bookTitl: string;
  // 저자
  bookAthr: string;
  // 출판사
  bookPubl: string;
  // 책 isbn
  bookIsbn: string;
  // 책 표지 이미지
  bookCvim: string;
  // 책 소개 내용
  bookDesc: string;
}

/**
 * 독서 진행 상태 타입
 * (완독/읽는중/중단)
 */
export type ReadingStatusType = "done" | "reading" | "stopped";

/**
 * "독후감" 타입
 */
export interface ReportType {
  // 독서 진행 상태
  bookStat: ReadingStatusType;
  // 독서 시작일
  bookStdt: string;
  // 독서 종료일
  bookEndt: string;
  // 별점
  bookGrde: string;
  // 독후감 내용
  bookCntn: string;
}

// 기록하기 데이터 타입
// export interface AddBookReportRequest {
//   bookTitl: string;
//   bookAthr: string;
//   bookPubl: string;
//   bookIsbn: string;
//   bookCvim: string;
//   bookDesc: string;
//   bookStat: ReadingStatusType;
//   bookStdt: string;
//   bookEndt: string;
//   bookGrde: string;
//   bookCntn: string;
// }

// 기록 후 백엔드 응답
export interface AddBookResponse {
  success: boolean;
  data: number; // bookId
}

// 독후감 상세보기 타입
export interface ReportDetail extends ReportType {
  image: string;
  title: string;
}

// 홈화면에 보이는 독후감 타입
export interface HomeBookType {
  bookNumb: number;
  bookTitl: string;
}
