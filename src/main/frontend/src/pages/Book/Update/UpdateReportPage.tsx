/**
 * src/main/frontend/src/pages/Book/Update/UpdateReportPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { sweetConfirm, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { useEffect, useState } from "react";
import type { CSSProperties, FormEvent } from "react";
import { useParams } from "react-router-dom";
import Loading from "@/components/Loading/Loading";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import { useUpdateMutation } from "@/features/Book/Update/useUpdateMutation";
import { useDeleteMutation } from "@/features/Book/Delete/useDeleteMutation";
import * as styles from "../Set/SetReportPage.css";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ColorCodeField from "@/features/Book/Set/components/form/colorCodeField/ColorCodeField";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import {
  MAX_REPORT_CONTENT_BYTES,
  REPORT_GRADE_OPTIONS,
  REPORT_STATUS_DONE,
  REPORT_STATUS_READ,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  sanitizeText,
  truncateUtf8Bytes,
  validateReportForm,
} from "@/features/Book/utils/reportValidation";
import { useCodeList } from "@/features/Common/utils/codeUtil";
import { formatDateValue } from "@/app/utils/dateUtil";

const UpdateReportPage = () => {
  const { id } = useParams();
  const idNum = Number(id);

  const [status, setStatus] = useState<ReadingStatusType>("");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState("");
  const [pubcYsno, setPubcYsno] = useState<"Y" | "N">("N");
  const [initialStatus, setInitialStatus] = useState<ReadingStatusType>("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);

  const { data, isPending } = useBookDetail(idNum);
  const { data: statusCodes = [] } = useCodeList("READ_STAT");
  const { data: colorCodes = [] } = useCodeList("BOOK_COLR");
  const { mutate } = useUpdateMutation();
  const { mutate: deleteReport } = useDeleteMutation();
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
    setInitialStatus(bookData.reportStat ?? "");
    setStartDate(bookData.reportStdt ?? "");
    setEndDate(bookData.reportEndt ?? "");
    setGrade(Number(bookData.reportGrde) || 0);
    setReportColr(bookData.reportColr ?? "");
    setPubcYsno(bookData.pubcYsno === "Y" ? "Y" : "N");
    setContentByteLength(
      getReportContentStorageByteLength(bookData.reportCntn ?? ""),
    );
  }, [bookData]);

  const isReadingStatus = status === REPORT_STATUS_READ;
  const startDateLabel = isReadingStatus
    ? message("frontend.report.field.targetStartDate")
    : message("frontend.report.field.startDate");
  const endDateLabel = isReadingStatus
    ? message("frontend.report.field.targetEndDate")
    : message("frontend.report.field.endDate");

  /**
   * 사용자가 달력에서 날짜를 선택하는 즉시 시작일과 종료일의 역전 여부를 검증합니다.
   *
   * @author Hanwon.Jang
   * @param nextStartDate 선택 또는 유지될 시작일
   * @param nextEndDate 선택 또는 유지될 종료일
   * @return 날짜 범위가 정상인 경우 true, 시작일이 종료일보다 늦은 경우 false
   */
  const validateDateRangeOnSelect = (nextStartDate: string, nextEndDate: string) => {
    // 한쪽 날짜가 아직 선택되지 않은 상태에서는 최종 저장 검증에서 필수값을 판단합니다.
    if (!nextStartDate || !nextEndDate) {
      return true;
    }

    // 시작일이 종료일과 같거나 앞선 경우 정상 범위이므로 선택 값을 반영합니다.
    if (new Date(nextStartDate) <= new Date(nextEndDate)) {
      return true;
    }

    void sweetWarning(
      message("frontend.alert.inputRequired"),
      message("frontend.validation.invalidDateRange"),
    );
    return false;
  };

  /**
   * 독서 상태 변경을 반영하고 읽는 중에서 완료 상태로 바뀌는 경우 종료일 보정 여부를 확인합니다.
   *
   * @author Hanwon.Jang
   * @param nextStatus 사용자가 선택한 다음 독서 상태 코드
   * @return
   */
  const handleStatusChange = async (nextStatus: ReadingStatusType) => {
    setStatus(nextStatus);

    // 최초 상태가 읽는 중이 아니거나 완료 상태로 진입한 상황이 아니면 날짜 보정 확인이 필요 없습니다.
    if (
      initialStatus !== REPORT_STATUS_READ ||
      nextStatus !== REPORT_STATUS_DONE ||
      status === REPORT_STATUS_DONE
    ) {
      return;
    }

    // 읽는 중인 독후감을 완료 처리할 때만 종료일을 오늘로 자동 변경할지 사용자의 의사를 확인합니다.
    const confirmed = await sweetConfirm({
      title: message("frontend.report.doneDateConfirmTitle"),
      text: message("frontend.report.doneDateConfirmText"),
      confirmButtonText: message("frontend.common.confirm"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (confirmed.isConfirmed) {
      setEndDate(formatDateValue(new Date()));
    }
  };

  if (!id || isNaN(idNum)) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
  }

  const setFormAction = async (e: FormEvent<HTMLFormElement>) => {
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

    const pubcYsno: "Y" | "N" = formData.get("pubcYsno") === "Y" ? "Y" : "N";
    const gradeValue = formData.get("grade");
    const data = {
      reportNumb: idNum,
      reportStat: formData.get("status") as ReadingStatusType,
      reportStdt: formData.get("startDate") as string,
      reportEndt: formData.get("endDate") as string,
      reportGrde: gradeValue ? String(gradeValue) : "0",
      reportColr: formData.get("reportColr") as string,
      pubcYsno,
      reportCntn: sanitizeText(formData.get("content")),
    };

    const confirmed = await sweetConfirm({
      title: message("frontend.alert.saveConfirmTitle"),
      text: message("frontend.report.saveConfirmText"),
      confirmButtonText: message("frontend.report.save"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (!confirmed.isConfirmed) {
      return;
    }

    mutate({ reportNumb, data });
  };

  const deleteOnClick = async () => {
    const confirmed = await sweetConfirm({
      icon: "warning",
      title: message("frontend.alert.deleteConfirmTitle"),
      text: message("frontend.report.deleteConfirmText"),
      confirmButtonText: message("frontend.report.delete"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (confirmed.isConfirmed) {
      deleteReport(idNum);
    }
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
                      void handleStatusChange(
                        item.comdCode as ReadingStatusType,
                      )
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
                label={startDateLabel}
                value={startDate}
                placeholder={message("frontend.report.placeholder.startDate")}
                onChange={setStartDate}
                onBeforeChange={(nextDate) =>
                  validateDateRangeOnSelect(nextDate, endDate)
                }
              />
              <CalendarDatePicker
                name="endDate"
                label={endDateLabel}
                value={endDate}
                placeholder={message("frontend.report.placeholder.endDate")}
                onChange={setEndDate}
                onBeforeChange={(nextDate) =>
                  validateDateRangeOnSelect(startDate, nextDate)
                }
              />
            </div>
          </FormField>

          <FormField title={message("frontend.report.field.grade")}>
            <div
              className={styles.starGroup}
              aria-label={message("frontend.report.gradeAria")}
            >
              {REPORT_GRADE_OPTIONS.map((value) => (
                <label
                  key={value}
                  className={`${styles.starLabel} ${
                    value <= grade ? styles.starActive : ""
                  }`}
                  htmlFor={`grade${value}`}
                >
                  {"\u2605"}
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

          <div className={styles.formActions}>
            <button
              className={styles.deleteButton}
              type="button"
              onClick={deleteOnClick}
            >
              <svg
                className={styles.buttonIcon}
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  d="M6 7h12M10 7V5.5h4V7M8 10v8M12 10v8M16 10v8"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <path
                  d="M8 7l.8 13h6.4L16 7"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinejoin="round"
                />
              </svg>
              {message("frontend.report.delete")}
            </button>
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
        </div>
      </form>
    </main>
  ) : (
    <h3>{data?.message}</h3>
  );
};

export default UpdateReportPage;
