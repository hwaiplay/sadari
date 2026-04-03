/**
 * fileName       : SearchBook
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 책 검색하기 버튼 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

import { Link } from "react-router-dom";
import { searchBtn, searchBtnText } from "./SearchBookButton.css";

interface SearchBookProps {
  src: string;
  title: string;
}

const SearchBookButton = ({ src, title }: SearchBookProps) => {
  return src ? (
    <div style={{ width: "300px" }}>
      <img src={src} alt={title} style={{ width: "100%" }} />
    </div>
  ) : (
    <Link to="/book/search" className={searchBtn}>
      <svg
        width="27"
        height="27"
        viewBox="0 0 27 27"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
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
      <p className={searchBtnText}>책 검색하기</p>
    </Link>
  );
};

export default SearchBookButton;
