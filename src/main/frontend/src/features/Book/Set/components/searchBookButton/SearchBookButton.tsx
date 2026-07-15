
import { message } from "@/app/messages/message";
import { Link } from "react-router-dom";
import { searchBtn, searchBtnText } from "./SearchBookButton.css";

/**
 * 梨?寃???붾㈃?쇰줈 ?대룞?섎뒗 踰꾪듉???쒖떆?쒕떎.
 * @author Hanwon.Jang
 * @return 梨?寃???대룞 踰꾪듉 而댄룷?뚰듃
 */
const SearchBookButton = () => {
  return (
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
      <p className={searchBtnText}>
        {message("frontend.book.search.open") /* frontend.book.search.open = 梨?寃?됲븯湲?*/}
      </p>
    </Link>
  );
};

export default SearchBookButton;