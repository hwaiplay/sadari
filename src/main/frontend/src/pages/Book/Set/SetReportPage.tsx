import FormField from "@/features/Book/Set/components/form/field/FormField";
import { statusContainer } from "./SetReportPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { useLocation } from "react-router-dom";
import { useState } from "react";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import Loading from "@/components/Loading/Loading";
import { useAddBookMutation } from "@/features/Book/Set/hooks/useAddBookMutation";
import { useSetReportForm } from "@/features/Book/Set/hooks/useSetReportForm";

/**
 * fileName       : SetReport
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

function SetReportPage() {
  // 책 검색 페이지에서 결과 가져옴
  const location = useLocation();
  const selectedBook = location.state?.selectedBook;

  // 독서 상태
  const [status, setStatus] = useState<ReadingStatusType>("done");

  const { isPending, handleSubmit } = useSetReportForm(selectedBook);

  const formAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    handleSubmit(e.currentTarget);
  };

  return isPending ? (
    <Loading title={"등록중"} />
  ) : (
    <form onSubmit={formAction}>
      {selectedBook ? (
        selectedBook.image && (
          <div style={{ width: "300px" }}>
            <img
              src={selectedBook.image}
              alt={selectedBook.title}
              style={{ width: "100%" }}
            />
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
            <label htmlFor={`status-${item.value}`} key={item.value}>
              <div>{item.label}</div>
              <input
                type="radio"
                name="status"
                id={`status-${item.value}`}
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
          <input type="date" name="startDate" id="startDate" />
        </div>
        <div>
          <label htmlFor="endDate">종료일</label>
          <input type="date" name="endDate" id="endDate" />
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

export default SetReportPage;
