/**
 * fileName       : book.type
 * author         : HanWon.Jang
 * date           : 2026-04-02
 * description    : 독후감 관련 타입 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       HanWon.Jang       최초 생성
 */

/**
 * 카카오 도서 검색 결과를 화면 계약으로 변환한 타입
 */
export interface BookSearchResultType {
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
  // 조회 응답에 포함되는 독후감 작성자 사용자 번호이며 등록 요청에서는 생략합니다.
  userNumb?: number;
  // 독서 진행 상태
  reptStat: ReadingStatusType;
  reptStatName?: string;
  // 독서 시작일
  reptStdt: string;
  // 독서 종료일
  reptEndt: string;
  // 별점
  reptGrde: string;
  // 책장 색상
  reptColr: string;
  reptColrName?: string;
  pubcYsno?: "Y" | "N";
  pubcYsnoName?: string;
  likeCnt?: number;
  likeYsno?: "Y" | "N";

  // 댓글 수
  replCnt?: number
  // 독후감 내용
  reptCntn: string;

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
  reptNumb: number;

  data: {
    // 독서 진행 상태
    reptStat: ReadingStatusType;
    // 독서 시작일
    reptStdt: string;
    // 독서 종료일
    reptEndt: string;
    // 별점
    reptGrde: string;
    // 책장 색상
    reptColr: string;
    pubcYsno: "Y" | "N";
    // 독후감 내용
    reptCntn: string;
  };
}

// 기록 후 백엔드 응답
export interface AddBookResponse {
  code: number;
  message?: string;
  data: number; // reptNumb
}

// 공개된 독후감 타입
export interface PublicReportType {
  reptNumb: number;
  userNumb: number;
  userNick: string;
  porfPath?: string;
  bookNumb: number;
  reptStat: "READ" | "DONE" | "STOP";
  reptStatName?: string;
  reptGrde: string;
  reptCntn: string;
  pubcYsno: "Y";
  likeCnt?: number;
  replCnt?: number;
  likeYsno?: "Y" | "N";
  commentCnt?: number;
}

// 독후감 상세보기 타입
export interface ReportDetailType extends ReportDtoType {
  // 상세 조회에서는 좋아요 알림 수신자를 요청에 포함해야 하므로 작성자 번호가 항상 필요합니다.
  userNumb: number;
}

// 홈화면에 보이는 독후감 타입
export interface HomeBookType {
  reptNumb: number;
  bookNumb: number;
  bookTitl: string;
  bookCvim?: string;
  reptStdt?: string;
  reptEndt?: string;
  reptGrde?: string;
  reptColr?: string;
  reptColrName?: string;
  readingYn?: "Y" | "N";
}

// 독후감 수정 시 파라미터 타입
export type SetReportParamsType = {
  // reptNumb: number; // 독후감 번호
  data: uptReportType; // 수정 데이터
};
