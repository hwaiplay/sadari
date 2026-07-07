import { message } from "@/app/messages/message";
import { sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Loading from "@/components/Loading/Loading";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import { useUpdateMutation } from "@/features/Book/Update/useUpdateMutation";
import * as styles from "../Set/SetReportPage.css";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ColorPickerField from "@/features/Book/Set/components/form/colorPickerField/ColorPickerField";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import {
  DEFAULT_REPORT_COLOR,
  MAX_REPORT_CONTENT_BYTES,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  sanitizeText,
  truncateUtf8Bytes,
  validateReportForm,
} from "@/features/Book/utils/reportValidation";

const UpdateReportPage = () => {
  const { id } = useParams();
  const idNum = Number(id);

  const [status, setStatus] = useState<ReadingStatusType>("done");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState(DEFAULT_REPORT_COLOR);
  const [contentByteLength, setContentByteLength] = useState(0);

  const { data, isPending } = useBookDetail(idNum);
  const { mutate } = useUpdateMutation();

  const bookData = data?.data;

  useEffect(() => {
    if (!bookData) {
      return;
    }

    setStatus(bookData.reportStat ?? "done");
    setGrade(Number(bookData.reportGrde) || 0);
    setReportColr(bookData.reportColr || DEFAULT_REPORT_COLOR);
    setContentByteLength(
      getReportContentStorageByteLength(bookData.reportCntn ?? ""),
    );
  }, [bookData]);

  if (!id || isNaN(idNum)) {
    return <div>{message("frontend.common.invalidAccess")}</div>; // frontend.common.invalidAccess = 잘못된 접근입니다
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />; // frontend.report.loading.detail = 독후감을 불러오는 중
  }

  const setFormAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const formData = new FormData(e.currentTarget);
    const reportNumb = idNum;

    const validationMessage = validateReportForm({
      status: formData.get("status"),
      startDate: formData.get("startDate"),
      endDate: formData.get("endDate"),
      grade: formData.get("grade"),
      reportColr: formData.get("reportColr"),
      content: formData.get("content"),
    });

    if (validationMessage) {
      void sweetWarning(
        message("frontend.alert.inputRequired"), // frontend.alert.inputRequired = 입력이 필요합니다
        validationMessage,
      );
      return;
    }

    const data = {
      reportNumb: idNum,
      reportStat: formData.get("status") as ReadingStatusType,
      reportStdt: formData.get("startDate") as string,
      reportEndt: formData.get("endDate") as string,
      reportGrde: formData.get("grade") as string,
      reportColr: formData.get("reportColr") as string,
      reportCntn: sanitizeText(formData.get("content")),
    };

    mutate({ reportNumb, data });
  };

  return bookData ? (
    <main className={styles.page}>
      <form className={styles.form} onSubmit={setFormAction}>
        <BookSummary
          image={bookData.bookCvim}
          title={bookData.bookTitl}
          author={bookData.bookAthr}
          publisher={bookData.bookPubl}
        />

        <FormField title={message("frontend.report.field.status")}> {/* frontend.report.field.status = 독서 상태 */}
          <div className={styles.statusContainer}>
            {[
              { label: message("frontend.report.status.done"), value: "done" }, // frontend.report.status.done = 다 읽었어요
              { label: message("frontend.report.status.reading"), value: "reading" }, // frontend.report.status.reading = 읽고 있어요
              { label: message("frontend.report.status.stopped"), value: "stopped" }, // frontend.report.status.stopped = 중단했어요
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

        <FormField title={message("frontend.report.field.period")}> {/* frontend.report.field.period = 독서 기간 */}
          <div className={styles.fieldStack}>
            <CalendarDatePicker
              name="startDate"
              label={message("frontend.report.field.startDate")} // frontend.report.field.startDate = 시작일
              defaultValue={bookData.reportStdt}
              placeholder={message("frontend.report.placeholder.startDate")} // frontend.report.placeholder.startDate = 시작일 선택
            />
            <CalendarDatePicker
              name="endDate"
              label={message("frontend.report.field.endDate")} // frontend.report.field.endDate = 종료일
              defaultValue={bookData.reportEndt}
              placeholder={message("frontend.report.placeholder.endDate")} // frontend.report.placeholder.endDate = 종료일 선택
            />
          </div>
        </FormField>

        <FormField title={message("frontend.report.field.grade")}> {/* frontend.report.field.grade = 평점 */}
          <div className={styles.starGroup} aria-label={message("frontend.report.gradeAria")}> {/* frontend.report.gradeAria = 평점 선택 */}
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

        <FormField title={message("frontend.report.field.color")}> {/* frontend.report.field.color = 책장 색상 */}
          <ColorPickerField value={reportColr} onChange={setReportColr} />
        </FormField>

        <FormField title={message("frontend.report.field.content")}> {/* frontend.report.field.content = 기록 */}
          <div className={styles.textAreaWrap}>
            <span className={styles.counter}>
              ({contentByteLength}/{MAX_REPORT_CONTENT_BYTES} byte)
            </span>
            <textarea
              className={styles.textArea}
              name="content"
              id="content"
              placeholder={message("frontend.report.placeholder.content")} // frontend.report.placeholder.content = 독후감을 남겨보세요
              defaultValue={bookData.reportCntn}
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
          {message("frontend.report.save") /* frontend.report.save = 저장 */}
        </button>
      </form>
    </main>
  ) : (
    <h3>{data?.message}</h3>
  );
};

export default UpdateReportPage;
