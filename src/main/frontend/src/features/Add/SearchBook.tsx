/**
 * fileName       : SearchBook
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

import { searchBtn, searchBtnText } from "../../pages/Add/Add.css";

interface SearchBookProps {}

const SearchBook = (props: SearchBookProps) => {
  return (
    <button type="button" className={searchBtn}>
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
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M1.5 13.2736L25.5 13.2736"
          stroke="#C1C1C1"
          stroke-width="3"
          stroke-linecap="round"
        />
      </svg>
      <p className={searchBtnText}>책 검색하기</p>
    </button>
  );
};

export default SearchBook;
