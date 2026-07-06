import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as styles from "./SetReportPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import Loading from "@/components/Loading/Loading";
import { useSetReportForm } from "@/features/Book/Set/hooks/useSetReportForm";

const BOOK_COLORS = [
  "#ac8a8a",
  "#8fd1df",
  "#efc36e",
  "#cbb7da",
  "#b4d09b",
  "#2f3437",
];

function SetReportPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const selectedBook = location.state?.selectedBook;

  const [status, setStatus] = useState<ReadingStatusType>("done");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState(BOOK_COLORS[0]);
  const [contentLength, setContentLength] = useState(0);

  const { isPending, handleSubmit } = useSetReportForm(selectedBook);

  const formAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    handleSubmit(e.currentTarget);
  };

  return isPending ? (
    <Loading title={"등록중"} />
  ) : (
    <main className={styles.page}>
      <form className={styles.form} onSubmit={formAction}>

        <div className={styles.coverArea}>
          {selectedBook?.image ? (
            <div className={styles.coverFrame}>
              <img
                className={styles.coverImage}
                src={selectedBook.image}
                alt={selectedBook.title}
              />
            </div>
          ) : (
            <SearchBookButton />
          )}
        </div>

        <FormField title="독서 상태">
          <div className={styles.statusContainer}>
            {[
              { label: "다 읽었어요", value: "done" },
              { label: "읽고 있어요", value: "reading" },
              { label: "중단했어요", value: "stopped" },
            ].map((item) => (
              <label className={styles.statusOption} key={item.value}>
                <input
                  className={styles.hiddenInput}
                  type="radio"
                  name="status"
                  value={item.value}
                  checked={status === item.value}
                  onChange={() => setStatus(item.value as ReadingStatusType)}
                />
                <span className={styles.statusPill}>{item.label}</span>
              </label>
            ))}
          </div>
        </FormField>

        <FormField title="독서 기간">
          <div className={styles.fieldStack}>
            <div className={styles.dateRow}>
              <label className={styles.inputLabel} htmlFor="startDate">
                시작일
              </label>
              <input
                className={styles.input}
                type="date"
                name="startDate"
                id="startDate"
              />
            </div>
            <div className={styles.dateRow}>
              <label className={styles.inputLabel} htmlFor="endDate">
                종료일
              </label>
              <input
                className={styles.input}
                type="date"
                name="endDate"
                id="endDate"
              />
            </div>
          </div>
        </FormField>

        <FormField title="평점">
          <div className={styles.starGroup} aria-label="평점 선택">
            {[1, 2, 3, 4, 5].map((value) => (
              <label
                key={value}
                className={`${styles.starLabel} ${
                  value <= grade ? styles.starActive : ""
                }`}
                htmlFor={`grade${value}`}
              >
                ★
                <input
                  className={styles.hiddenInput}
                  type="radio"
                  name="grade"
                  id={`grade${value}`}
                  value={value}
                  checked={grade === value}
                  onChange={() => setGrade(value)}
                />
              </label>
            ))}
          </div>
        </FormField>

        <FormField title="책장 색상">
          <div className={styles.colorGrid} aria-label="책장 색상 선택">
            {BOOK_COLORS.map((color) => (
              <label className={styles.colorOption} key={color}>
                <input
                  className={styles.hiddenInput}
                  type="radio"
                  name="reportColr"
                  value={color}
                  checked={reportColr === color}
                  onChange={() => setReportColr(color)}
                />
                <span
                  className={styles.colorSwatch}
                  style={{ backgroundColor: color }}
                />
              </label>
            ))}
          </div>
        </FormField>

        <FormField title="기록">
          <div className={styles.textAreaWrap}>
            <span className={styles.counter}>({contentLength}/500)</span>
            <textarea
              className={styles.textArea}
              name="content"
              id="content"
              maxLength={500}
              placeholder="독후감을 남겨보세요"
              onChange={(e) => setContentLength(e.currentTarget.value.length)}
            />
          </div>
        </FormField>

        <button className={styles.saveButton} type="submit">
          저장
        </button>
      </form>
    </main>
  );
}

export default SetReportPage;
