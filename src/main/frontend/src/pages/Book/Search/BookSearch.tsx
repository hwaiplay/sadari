/**
 * fileName       : BookSearch
 * author         : hanwon.Jang
 * date           : 2026-04-01
 * description    : 책 검색 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-01       hanwon.Jang       최초 생성
 */

import { useEffect, useState } from "react";
import api from "../../../app/api/axios";
import { BookSearchProps } from "@/features/Book/types/book.type";
import { useNavigate } from "react-router-dom";

const BookSearch = () => {
  const [searchKeyword, setSearchKeyword] = useState(""); // 검색어
  const [bookResult, setBookResult] = useState<BookSearchProps[] | null>(null); // 응답 데이터

  // 검색 로직 구현
  const handleSearchClick = async () => {
    try {
      if (searchKeyword.trim() === "") {
        alert("검색어를 입력해주세요.");
        return;
      }

      const response = await api.get(
        `/book/search?query=${encodeURIComponent(searchKeyword)}`,
      );

      const responsecode = response.data.code;

      if (responsecode !== 200) {
        alert("책 검색에 실패했습니다. 다시 시도해주세요.");
        return;
      }

      const responseData = response.data.data;
      setBookResult(responseData);
    } catch (error) {
      console.error("검색어 처리 중 에러 발생: ", error);
    }
  };

  const navigate = useNavigate();

  const handleSelectBook = (book: BookSearchProps) => {
    navigate("/add", {
      state: { selectedBook: book },
    });
  };

  useEffect(() => {
    console.log(bookResult);
  }, [bookResult]);

  return (
    <>
      <div style={{ marginTop: "90px" }}>
        <input
          type="text"
          name="searchKeyword"
          id="searchKeyword"
          placeholder="책 제목을 입력하세요"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <button onClick={handleSearchClick}>검색</button>
      </div>
      {bookResult &&
        (bookResult.length > 0 ? (
          bookResult.map((book) => (
            <div key={book.isbn} onClick={() => handleSelectBook(book)}>
              <img src={book.image} alt={book.title} />
              <h2>{book.title}</h2>
              <div>
                <p>{book.author}</p>
                <p>{book.publisher}</p>
              </div>
            </div>
          ))
        ) : (
          <p>검색 결과가 없습니다.</p>
        ))}
    </>
  );
};

export default BookSearch;
