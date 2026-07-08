import { message } from "@/app/messages/message";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as styles from "./SetReportPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { useLocation, useNavigate } from "react-router-dom";
import type { CSSProperties } from "react";
import { useState } from "react";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import Loading from "@/components/Loading/Loading";
import { useSetReportForm } from "@/features/Book/Set/hooks/useSetReportForm";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ColorPickerField from "@/features/Book/Set/components/form/colorPickerField/ColorPickerField";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import {
  DEFAULT_REPORT_COLOR,
  MAX_REPORT_CONTENT_BYTES,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  truncateUtf8Bytes,
} from "@/features/Book/utils/reportValidation";

function SetReportPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const selectedBook = location.state?.selectedBook;

  const [status, setStatus] = useState<ReadingStatusType>("done");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState(DEFAULT_REPORT_COLOR);
  const [contentByteLength, setContentByteLength] = useState(0);

  const { isPending, handleSubmit } = useSetReportForm(selectedBook);
  const pageStyle = selectedBook?.image
    ? ({
        "--book-bg-image": `url("${selectedBook.image}")`,
      } as CSSProperties)
    : undefined;

  const formAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    handleSubmit(e.currentTarget);
  };

  return isPending ? (
    <Loading title={message("frontend.report.loading.create")} />
  ) : (
    <main className={styles.page} style={pageStyle}>
      <form className={styles.form} onSubmit={formAction}>
        {selectedBook?.image ? (
          <BookSummary
            image={selectedBook.image}
            title={selectedBook.title}
            author={selectedBook.author}
            publisher={selectedBook.publisher}
            onShowBookInfo={() =>
              navigate("/book/search/info", { state: { book: selectedBook } })
            }
            onChangeBook={() =>
              navigate("/book/search", { state: { keepSearchResult: true } })
            }
          />
        ) : (
          <div className={styles.searchBookArea}>
            <SearchBookButton />
          </div>
        )}

        <div className={styles.contentPanel}>
          <FormField title={message("frontend.report.field.status")}>
            <div className={styles.statusContainer}>
              {[
                { label: message("frontend.report.status.done"), value: "done" },
                {
                  label: message("frontend.report.status.reading"),
                  value: "reading",
                },
                {
                  label: message("frontend.report.status.stopped"),
                  value: "stopped",
                },
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

          <FormField title={message("frontend.report.field.period")}>
            <div className={styles.fieldStack}>
              <CalendarDatePicker
                name="startDate"
                label={message("frontend.report.field.startDate")}
                placeholder={message("frontend.report.placeholder.startDate")}
              />
              <CalendarDatePicker
                name="endDate"
                label={message("frontend.report.field.endDate")}
                placeholder={message("frontend.report.placeholder.endDate")}
              />
            </div>
          </FormField>

          <FormField title={message("frontend.report.field.grade")}>
            <div
              className={styles.starGroup}
              aria-label={message("frontend.report.gradeAria")}
            >
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

          <FormField title={message("frontend.report.field.color")}>
            <ColorPickerField value={reportColr} onChange={setReportColr} />
          </FormField>

          <FormField title={message("frontend.report.field.content")}>
            <div className={styles.textAreaWrap}>
              <span className={styles.counter}>
                ({contentByteLength}/{MAX_REPORT_CONTENT_BYTES} byte)
              </span>
              <textarea
                className={styles.textArea}
                name="content"
                id="content"
                placeholder={message("frontend.report.placeholder.content")}
                onChange={(e) => {
                  const nextValue = truncateUtf8Bytes(e.currentTarget.value);
                  e.currentTarget.value = nextValue;
                  setContentByteLength(
                    getReportContentStorageByteLength(nextValue),
                  );
                }}
              />
            </div>
          </FormField>

          <button className={styles.saveButton} type="submit">
            {message("frontend.report.save")}
          </button>
        </div>
      </form>
    </main>
  );
}

export default SetReportPage;
