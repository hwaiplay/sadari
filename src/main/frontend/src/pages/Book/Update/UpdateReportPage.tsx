import { message } from "@/app/messages/message";
import { sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { useEffect, useState } from "react";
import type { CSSProperties } from "react";
import { useParams } from "react-router-dom";
import Loading from "@/components/Loading/Loading";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import { useUpdateMutation } from "@/features/Book/Update/useUpdateMutation";
import * as styles from "../Set/SetReportPage.css";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ColorCodeField from "@/features/Book/Set/components/form/colorCodeField/ColorCodeField";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import { MAX_REPORT_CONTENT_BYTES } from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  sanitizeText,
  truncateUtf8Bytes,
  validateReportForm,
} from "@/features/Book/utils/reportValidation";
import { useCodeList } from "@/features/Common/utils/codeUtil";

/**
 * 독후감 수정 화면을 렌더링하고 기존 상세 데이터로 폼 상태를 초기화한다.
 * @Author Hanwon.Jang
 * @return 독후감 수정 페이지 컴포넌트
 */
const UpdateReportPage = () => {
  const { id } = useParams();
  const idNum = Number(id);

  const [status, setStatus] = useState<ReadingStatusType>("");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);

  const { data, isPending } = useBookDetail(idNum);
  const { data: statusCodes = [] } = useCodeList("READ_STAT");
  const { data: colorCodes = [] } = useCodeList("BOOK_COLR");
  const { mutate } = useUpdateMutation();
  const bookData = data?.data;
  const pageStyle = bookData?.bookCvim
    ? ({
        "--book-bg-image": `url("${bookData.bookCvim}")`,
      } as CSSProperties)
    : undefined;

  useEffect(() => {
    if (!bookData) {
      return;
    }

    setStatus(bookData.reportStat ?? "");
    setGrade(Number(bookData.reportGrde) || 0);
    setReportColr(bookData.reportColr ?? "");
    setContentByteLength(
      getReportContentStorageByteLength(bookData.reportCntn ?? ""),
    );
  }, [bookData]);

  if (!id || isNaN(idNum)) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
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
      validStatusCodes: statusCodes.map((item) => item.comdCode),
      validReportColors: colorCodes.map((item) => item.comdCode),
    });

    if (validationMessage) {
      void sweetWarning(message("frontend.alert.inputRequired"), validationMessage);
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
    <main className={styles.page} style={pageStyle}>
      <form className={styles.form} onSubmit={setFormAction}>
        <BookSummary
          image={bookData.bookCvim}
          title={bookData.bookTitl}
          author={bookData.bookAthr}
          publisher={bookData.bookPubl}
        />

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

          <FormField title={message("frontend.report.field.period")}>
            <div className={styles.fieldStack}>
              <CalendarDatePicker
                name="startDate"
                label={message("frontend.report.field.startDate")}
                defaultValue={bookData.reportStdt}
                placeholder={message("frontend.report.placeholder.startDate")}
              />
              <CalendarDatePicker
                name="endDate"
                label={message("frontend.report.field.endDate")}
                defaultValue={bookData.reportEndt}
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
            <ColorCodeField
              colors={colorCodes}
              value={reportColr}
              onChange={setReportColr}
            />
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
            {message("frontend.report.save")}
          </button>
        </div>
      </form>
    </main>
  ) : (
    <h3>{data?.message}</h3>
  );
};

export default UpdateReportPage;
