/**
 * fileName       : ClubTypes
 * author         : Hanwon.Jang
 * date           : 2026-09-01
 * description    : 모임 관련 타입 정의 파일
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-01        Hanwon.Jang    최초 생성
 */

// 모임 도서 추천
export type ClubBookRecommendation = {
  recmNumb: number;
  bookNumb: number;
  bookTitl: string;
  bookAthr: string;
  bookPubl: string;
  bookIsbn: string;
  bookCvim?: string;
  bookDesc?: string;
  publDate?: string;
  userNick?: string;
  mineYsno: "Y" | "N";
  voteYsno: "Y" | "N";
  voteCnt: number;
};

// 모임 도서 투표 페이지
export type ClubBookVotePage = {
  candidateList: ClubBookRecommendation[];
  voteDeadline?: string;
  dDay?: number;
  canRecommend: boolean;
  hasRecommended: boolean;
  hasVoted: boolean;
};