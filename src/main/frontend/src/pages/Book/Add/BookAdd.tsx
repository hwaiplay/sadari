import FormField from "@/features/Book/Add/components/form/field/FormField";
import { Button } from "@/components/Button/Button";
import { statusContainer } from "./BookAdd.css";
import SearchBookButton from "@/features/Book/Add/components/searchBookButton/SearchBookButton";
import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { BookForm } from "@/features/Book/types/book.type";

/**
 * fileName       : Add
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 기록하기 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 * 2026-04-02       Hanwon.Jang       선택 책 반영
 * 2026-04-02       Hanwon.Jang       폼 구성
 */

function BookAdd() {
  const location = useLocation();
  const selectedBook = location.state?.selectedBook;
  const [form, setForm] = useState<BookForm>({
    isbn: "",
    image: "",
    title: "",
  });

  useEffect(() => {
    if (selectedBook) {
      setForm({
        isbn: selectedBook.isbn,
        image: selectedBook.image,
        title: selectedBook.title,
      });
    }
  }, [selectedBook]);

  const [readingStatus, setReadingStatus] = useState("done");
  const [statusBtnVariant, setStatusBtnVariant] = useState("done");

  const handleStatusClick = (state: string) => {
    switch (state) {
      case "done":
        break;

      case "ing":
        break;
      case "stop":
        break;
    }
  };

  return (
    <form action="" method="post">
      <SearchBookButton src={form.image} title={form.title} />
      <FormField title="독서 상태">
        <div className={statusContainer}>
          <Button onClick={() => handleStatusClick("done")}>다 읽었어요</Button>
          <Button onClick={() => handleStatusClick("ing")} variant="disable">
            읽고 있어요
          </Button>
          <Button onClick={() => handleStatusClick("stop")} variant="disable">
            중단 했어요
          </Button>
        </div>
      </FormField>
      <FormField title="독서 기간">
        <div>
          <label htmlFor="startDate">시작일</label>
          <input type="date" name="startDate" id="startDate" />
        </div>
        <div>
          <label htmlFor="EndDate">종료일</label>
          <input type="date" name="EndDate" id="EndDate" />
        </div>
      </FormField>
      <FormField title="평점">
        <input type="radio" name="range" id="range1" value={1} />
        <input type="radio" name="range" id="range2" value={2} />
        <input type="radio" name="range" id="range3" value={3} />
        <input type="radio" name="range" id="range4" value={4} />
        <input type="radio" name="range" id="range5" value={5} />
      </FormField>
      <FormField title="기록">
        <textarea
          name="content"
          id="content"
          placeholder="독후감을 남겨보세요"
        />
      </FormField>
      <button type="submit">기록하기</button>
    </form>
  );
}

export default BookAdd;
