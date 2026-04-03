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

// 책 검색 타입
export interface BookSearchProps {
  title: string;
  author: string;
  publisher: string;
  isbn: string;
  image: string;
  description: string;
}

// 기록하기 폼 타입
export interface BookForm {
  title: string;
  isbn: string;
  image: string;
}
