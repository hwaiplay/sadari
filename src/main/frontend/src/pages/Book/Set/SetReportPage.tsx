/**
 * src/main/frontend/src/pages/Book/Set/SetReportPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as styles from "./SetReportPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { useLocation, useNavigate } from "react-router-dom";
import type { CSSProperties, FormEvent } from "react";
import { useEffect, useState } from "react";
import type {
  NaverApiResultType,
  ReadingStatusType,
} from "@/features/Book/types/book.type";
import Loading from "@/components/Loading/Loading";
import { useSetReportForm } from "@/features/Book/Set/hooks/useSetReportForm";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ColorCodeField from "@/features/Book/Set/components/form/colorCodeField/ColorCodeField";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import RatingField from "@/features/Book/Set/components/form/ratingField/RatingField";
import {
  MAX_REPORT_CONTENT_BYTES,
  REPORT_STATUS_READ,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  truncateUtf8Bytes,
} from "@/features/Book/utils/reportValidation";
import { useCodeList } from "@/features/Common/utils/codeUtil";

function SetReportPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const selectedBook = (
    location.state as { selectedBook?: NaverApiResultType } | null
  )?.selectedBook;

  const [status, setStatus] = useState<ReadingStatusType>("");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState("");
  const [pubcYsno, setPubcYsno] = useState<"Y" | "N">("N");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);

  const { data: statusCodes = [] } = useCodeList("READ_STAT");
  const { data: colorCodes = [] } = useCodeList("BOOK_COLR");
  const validStatusCodes = statusCodes.map((item) => item.comdCode);
  const validReportColors = colorCodes.map((item) => item.comdCode);
  const { isPending, handleSubmit } = useSetReportForm(
    selectedBook,
    validStatusCodes,
    validReportColors,
  );
  const pageStyle = selectedBook?.image
    ? ({
        "--book-bg-image": `url("${selectedBook.image}")`,
      } as CSSProperties)
    : undefined;

  const formAction = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    handleSubmit(e.currentTarget);
  };

  const isReadingStatus = status === REPORT_STATUS_READ;
  const periodTitle = isReadingStatus ? "목표 독서 기간" : "독서 기간";

  useEffect(() => {
    if (!status && statusCodes.length > 0) {
      setStatus(statusCodes[0].comdCode);
    }
  }, [status, statusCodes]);

  useEffect(() => {
    if (!reportColr && colorCodes.length > 0) {
      setReportColr(colorCodes[0].comdCode);
    }
  }, [reportColr, colorCodes]);

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
              {statusCodes.map((item) => (
                <label className={styles.statusOption} key={item.comdCode}>
                  <input
                    className={styles.hiddenInput}
                    type="radio"
                    name="status"
                    value={item.comdCode}
                    checked={status === item.comdCode}
                    onChange={() =>
                      setStatus(item.comdCode as ReadingStatusType)
                    }
                  />
                  <span className={styles.statusPill}>{item.comdName}</span>
                </label>
              ))}
            </div>
          </FormField>

          <FormField title={periodTitle}>
            <div className={styles.fieldStack}>
              <CalendarDatePicker
                name="startDate"
                endName="endDate"
                value={startDate}
                endValue={endDate}
                placeholder={message("frontend.report.placeholder.startDate")}
                endPlaceholder={message("frontend.report.placeholder.endDate")}
                onRangeChange={(nextStartDate, nextEndDate) => {
                  setStartDate(nextStartDate);
                  setEndDate(nextEndDate);
                }}
              />
            </div>
          </FormField>

          <FormField title={message("frontend.report.field.grade")}>
            <RatingField value={grade} onChange={setGrade} />
          </FormField>

          <FormField title={message("frontend.report.field.color")}>
            <ColorCodeField
              colors={colorCodes}
              value={reportColr}
              onChange={setReportColr}
            />
          </FormField>

          <FormField title={message("frontend.report.field.public")}>
            <div className={styles.publicToggleRow}>
              <div className={styles.publicToggleText}>
                <span className={styles.publicToggleState}>
                  {pubcYsno === "Y"
                    ? message("frontend.report.public.on")
                    : message("frontend.report.public.off")}
                </span>
                <span className={styles.publicToggleHelp}>
                  {message("frontend.report.public.help")}
                </span>
              </div>
              <label className={styles.publicToggleControl}>
                <input type="hidden" name="pubcYsno" value={pubcYsno} />
                <input
                  className={styles.hiddenInput}
                  type="checkbox"
                  checked={pubcYsno === "Y"}
                  onChange={(e) => setPubcYsno(e.target.checked ? "Y" : "N")}
                />
                <span className={styles.switchTrack}>
                  <span className={styles.switchThumb} />
                </span>
              </label>
            </div>
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
            <svg
              className={styles.buttonIcon}
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                d="M5 4h11l3 3v13H5V4Z"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinejoin="round"
              />
              <path
                d="M8 4v6h8M8 17h8"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            {message("frontend.report.save")}
          </button>
        </div>
      </form>
    </main>
  );
}

export default SetReportPage;
