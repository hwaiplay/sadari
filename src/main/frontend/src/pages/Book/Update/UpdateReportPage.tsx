import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Loading from "@/components/Loading/Loading";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import { useUpdateMutation } from "@/features/Book/Update/useUpdateMutation";
import * as styles from "../Set/SetReportPage.css";

const BOOK_COLORS = [
  "#ac8a8a",
  "#8fd1df",
  "#efc36e",
  "#cbb7da",
  "#b4d09b",
  "#2f3437",
];

const UpdateReportPage = () => {
  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();

  const [status, setStatus] = useState<ReadingStatusType>("done");
  const [grade, setGrade] = useState(0);
  const [reportColr, setReportColr] = useState(BOOK_COLORS[0]);
  const [contentLength, setContentLength] = useState(0);

  const { data, isPending } = useBookDetail(idNum);
  const { mutate } = useUpdateMutation();

  const bookData = data?.data;

  useEffect(() => {
    if (!bookData) {
      return;
    }

    setStatus(bookData.reportStat ?? "done");
    setGrade(Number(bookData.reportGrde) || 0);
    setReportColr(bookData.reportColr || BOOK_COLORS[0]);
    setContentLength(bookData.reportCntn?.length ?? 0);
  }, [bookData]);

  if (!id || isNaN(idNum)) {
    return <div>잘못된 접근입니다</div>;
  }

  if (isPending) {
    return <Loading title={"독후감을 불러오는 중"} />;
  }

  const setFormAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const formData = new FormData(e.currentTarget);
    const reportNumb = idNum;

    const data = {
      reportNumb: idNum,
      reportStat: formData.get("status") as ReadingStatusType,
      reportStdt: formData.get("startDate") as string,
      reportEndt: formData.get("endDate") as string,
      reportGrde: formData.get("grade") as string,
      reportColr: formData.get("reportColr") as string,
      reportCntn: formData.get("content") as string,
    };

    mutate({ reportNumb, data });
  };

  return bookData ? (
    <main className={styles.page}>
      <form className={styles.form} onSubmit={setFormAction}>
        <div className={styles.topBar}>
          <button
            className={styles.backButton}
            type="button"
            aria-label="뒤로가기"
            onClick={() => navigate(-1)}
          >
            ‹
          </button>
          <h1 className={styles.brand}>sadari</h1>
          <button className={styles.saveButton} type="submit">
            저장
          </button>
        </div>

        <div className={styles.coverArea}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookData.bookCvim}
              alt={bookData.bookTitl}
            />
          </div>
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
                defaultValue={bookData.reportStdt}
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
                defaultValue={bookData.reportEndt}
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
              defaultValue={bookData.reportCntn}
              onChange={(e) => setContentLength(e.currentTarget.value.length)}
            />
          </div>
        </FormField>
      </form>
    </main>
  ) : (
    <h3>{data?.message}</h3>
  );
};

export default UpdateReportPage;
