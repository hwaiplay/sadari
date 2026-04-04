import FormField from "@/features/Book/Add/components/form/field/FormField";
import { Button } from "@/components/Button/Button";
import { statusContainer } from "./BookAdd.css";
import SearchBookButton from "@/features/Book/Add/components/searchBookButton/SearchBookButton";
import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import {
  ReadingStatusType,
  SelectBookType,
} from "@/features/Book/types/book.type";
import { addBookReport } from "@/features/Book/api/bookApi";

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
 * 2026-04-03       Hanwon.Jang       폼 구성
 */

function BookAdd() {
  const location = useLocation();
  const selectedBook = location.state?.selectedBook;
  const [form, setForm] = useState<SelectBookType>({
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

  const [status, setStatus] = useState<ReadingStatusType>("done");

  const addFormAction = (event: any) => {
    const formData = new FormData(event.target);
    const coverImage = form.image; // 책표지
    const status = formData.get("status"); // 독서상태
    const startDate = formData.get("startDate"); // 독서 시작일
    const endDate = formData.get("endDate"); // 독서 종료일
    const grade = formData.get("grade"); // 평점
    const content = formData.get("content"); // 평점

    const data = {
      coverImage: coverImage,
      readingStatus: status as ReadingStatusType,
      readStartDate: startDate as string,
      readEndDate: endDate as string,
      grade: grade as string,
      content: content as string,
    };

    try {
      addBookReport(data);
      alert("등록 완료!");
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <form onSubmit={addFormAction}>
      {selectedBook ? (
        form.image && (
          <div style={{ width: "300px" }}>
            <img src={form.image} alt={form.title} style={{ width: "100%" }} />
          </div>
        )
      ) : (
        <SearchBookButton />
      )}
      <FormField title="독서 상태">
        <div className={statusContainer}>
          {[
            { label: "다 읽었어요", value: "done" },
            { label: "읽고 있어요", value: "reading" },
            { label: "중단했어요", value: "stopped" },
          ].map((item) => (
            <label htmlFor={`readingStatus-${item.value}`} key={item.value}>
              <div>{item.label}</div>
              <input
                type="radio"
                name="readingStatus"
                id={`readingStatus-${item.value}`}
                value={item.value}
                checked={status === item.value}
                onChange={() => setStatus(item.value as ReadingStatusType)}
              />
            </label>
          ))}
        </div>
      </FormField>
      <FormField title="독서 기간">
        <div>
          <label htmlFor="startDate">시작일</label>
          <input type="date" name="readStartDate" id="startDate" />
        </div>
        <div>
          <label htmlFor="endDate">종료일</label>
          <input type="date" name="readEndDate" id="endDate" />
        </div>
      </FormField>
      <FormField title="평점">
        <input type="radio" name="grade" id="grade1" value="1" />
        <input type="radio" name="grade" id="grade2" value="2" />
        <input type="radio" name="grade" id="grade3" value="3" />
        <input type="radio" name="grade" id="grade4" value="4" />
        <input type="radio" name="grade" id="grade5" value="5" />
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
