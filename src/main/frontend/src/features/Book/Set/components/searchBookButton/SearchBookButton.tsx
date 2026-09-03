
import { message } from "@/app/messages/message";
import { Link, type To } from "react-router-dom";
import type { ReactNode } from "react";
import { searchBtn, searchBtnText } from "./SearchBookButton.css";

type SearchBookButtonProps = {
  to?: To;
};

type SearchBookButtonContentProps = {
  children?: ReactNode;
};

/**
 * 도서 검색 진입 요소의 공통 아이콘과 문구를 표시함
 *
 * @author SeungHyeon.Kang
 * @param props 화면 맥락에 맞는 선택 문구
 * @return 도서 검색 진입 요소의 공통 내용
 */
export function SearchBookButtonContent({ children }: SearchBookButtonContentProps) {

  // 도서 검색 진입 요소에서 공유하는 아이콘과 문구를 반환함
  return (
    <>
      <svg
        width="27"
        height="27"
        viewBox="0 0 27 27"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <path
          d="M13.2736 1.5V25.5"
          stroke="#C1C1C1"
          strokeWidth="3"
          strokeLinecap="round"
        />
        <path
          d="M1.5 13.2736L25.5 13.2736"
          stroke="#C1C1C1"
          strokeWidth="3"
          strokeLinecap="round"
        />
      </svg>
      <p className={searchBtnText}>
        {children ?? (
          <>
            {/* "책 검색하기" */}
            {message("frontend.book.search.open")}
          </>
        )}
      </p>
    </>
  );
}

/**
 * 도서 검색 화면으로 이동하는 버튼을 표시함
 * @author SeungHyeon.Kang
 * @param props 이동할 도서 검색 경로
 * @return 도서 검색 화면 이동 버튼
 */
const SearchBookButton = ({ to = "/book/search" }: SearchBookButtonProps) => {

  // 개인 또는 모임 흐름에서 지정한 도서 검색 화면 이동 버튼을 반환함
  return (
    <Link to={to} className={searchBtn}>
      {/* 도서 검색 진입 아이콘과 안내 문구 영역 */}
      <SearchBookButtonContent />
    </Link>
  );
};

export default SearchBookButton;
