/**
 * fileName       : useAddBook
 * author         : hanwon.Jang
 * date           : 2026-04-07
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07       hanwon.Jang       최초 생성
 */

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { BookFormType } from "../../types/book.type";
import { addBookReport } from "../../api/bookApi";

const useAddBook = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const submitBook = async (data: BookFormType) => {
    try {
      setLoading(true);

      const res = await addBookReport(data);
      const code = res.data.code;
      const bookId = res.data.data;
      console.log(res.data);

      if (code === 200) {
        alert("등록 완료!");
        navigate(`/book/detail/${bookId}`);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return { submitBook, loading };
};

export default useAddBook;
