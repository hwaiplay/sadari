/**
 * fileName       : book.type
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    : 독후감 관련 타입 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

/**
 * "네이버 검색 결과 타입"
 */
export interface NaverApiResultType {
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
  // 출간일
  pubdate: string;
}

/**
 * "책 타입"
 */
export interface BookDtoType {
  bookDto: {
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
    // 공개 독후감 평균 별점
    bookAvgGrde?: number | string | null;
  };
}

/**
 * 독서 진행 상태 타입
 * (완독/읽는중/중단)
 */
export type ReadingStatusType = string;

/**
 * "독후감" 타입
 */
export interface ReportDtoType {
  // 독서 진행 상태
  reportStat: ReadingStatusType;
  reportStatName?: string;
  // 독서 시작일
  reportStdt: string;
  // 독서 종료일
  reportEndt: string;
  // 별점
  reportGrde: string;
  // 책장 색상
  reportColr: string;
  reportColrName?: string;
  pubcYsno?: "Y" | "N";
  pubcYsnoName?: string;
  likeCnt?: number;
  likeYsno?: "Y" | "N";
  // 독후감 내용
  reportCntn: string;

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
  // 출간일
  publDate: string;
  // 평균 별점
  bookAvgGrde?: number | string | null;
}

/**
 * 독후감 수정 타입
 */
export interface uptReportType {
  reportNumb: number;

  data: {
    // 독서 진행 상태
    reportStat: ReadingStatusType;
    // 독서 시작일
    reportStdt: string;
    // 독서 종료일
    reportEndt: string;
    // 별점
    reportGrde: string;
    // 책장 색상
    reportColr: string;
    pubcYsno: "Y" | "N";
    // 독후감 내용
    reportCntn: string;
  };
}

// 기록 후 백엔드 응답
export interface AddBookResponse {
  code: number;
  message?: string;
  data: number; // reportNumb
}

export interface PublicReportType {
  reportNumb: number;
  userNumb: number;
  userNick: string;
  porfPath?: string;
  bookNumb: number;
  reportGrde: string;
  reportCntn: string;
  pubcYsno: "Y";
  likeCnt?: number;
  likeYsno?: "Y" | "N";
}

// 독후감 상세보기 타입
export interface ReportDetailType extends ReportDtoType {
  image: string;
  title: string;
}

// 홈화면에 보이는 독후감 타입
export interface HomeBookType {
  reportNumb: number;
  bookNumb: number;
  bookTitl: string;
  bookCvim?: string;
  reportStdt?: string;
  reportEndt?: string;
  reportGrde?: string;
  reportColr?: string;
  reportColrName?: string;
  readingYn?: "Y" | "N";
}

// 독후감 수정 시 파라미터 타입
export type SetReportParamsType = {
  // reportNumb: number; // 독후감 번호
  data: uptReportType; // 수정 데이터
};
