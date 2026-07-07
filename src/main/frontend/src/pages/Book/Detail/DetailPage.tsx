import { message } from "@/app/messages/message";
import { useNavigate, useParams } from "react-router-dom";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { useDeleteMutation } from "@/features/Book/Delete/useDeleteMutation";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import * as styles from "./DetailPage.css";

const STATUS_LABEL = {
  done: message("frontend.report.status.done"), // frontend.report.status.done = 다 읽었어요
  reading: message("frontend.report.status.reading"), // frontend.report.status.reading = 읽고 있어요
  stopped: message("frontend.report.status.stopped"), // frontend.report.status.stopped = 중단했어요
} as const;

function getStatusLabel(status: string) {
  return status in STATUS_LABEL
    ? STATUS_LABEL[status as keyof typeof STATUS_LABEL]
    : status;
}

function RatingStars({ grade }: { grade: string }) {
  const rating = Math.max(0, Math.min(5, Number(grade) || 0));

  return (
    <div
      className={styles.stars}
      aria-label={message("frontend.report.gradeValue", [rating])} // frontend.report.gradeValue = 평점 {0}점
    >
      {[1, 2, 3, 4, 5].map((value) => (
        <span
          key={value}
          className={value <= rating ? styles.starFilled : undefined}
        >
          ★
        </span>
      ))}
    </div>
  );
}

function DetailPage() {
  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();
  const { mutate } = useDeleteMutation();
  const { data, isPending } = useBookDetail(idNum);

  const goUpdatePage = (reportNumb: number) => {
    navigate(`/book/upt/${reportNumb}`);
  };

  const goBookInfoPage = (reportNumb: number) => {
    navigate(`/book/info/${reportNumb}`);
  };

  const deleteOnClick = async (reportNumb: number) => {
    const confirmed = await sweetConfirm({
      title: message("frontend.alert.deleteConfirmTitle"), // frontend.alert.deleteConfirmTitle = 독후감을 삭제할까요?
      text: message("frontend.report.deleteConfirmText"), // frontend.report.deleteConfirmText = 삭제하면 복구할 수 없습니다.
      confirmButtonText: message("frontend.report.delete"), // frontend.report.delete = 삭제
      cancelButtonText: message("frontend.common.cancel"), // frontend.common.cancel = 취소
    });

    if (confirmed.isConfirmed) {
      mutate(reportNumb);
    }
  };

  if (data?.code === 2004) {
    return <div>{data.message}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />; // frontend.report.loading.detail = 독후감을 불러오는 중
  }

  const bookData = data?.data;

  if (data?.code !== 200 || !bookData) {
    return <h3>{data?.message}</h3>;
  }

  return (
    <main className={styles.page}>
      <Container className={styles.detail}>
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookData.bookCvim}
              alt={bookData.bookTitl}
            />
          </div>
          <h1 className={styles.title}>{bookData.bookTitl}</h1>
          <p className={styles.meta}>{bookData.bookAthr}</p>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={() => goBookInfoPage(idNum)}
          >
            {message("frontend.report.bookInfoMore") /* frontend.report.bookInfoMore = 도서 정보 자세히보기 */}
          </button>
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.report.field.status")}</h2> {/* frontend.report.field.status = 독서 상태 */}
          <div className={styles.statusPill}>
            {getStatusLabel(bookData.reportStat)}
          </div>
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.report.field.period")}</h2> {/* frontend.report.field.period = 독서 기간 */}
          <div className={styles.dateStack}>
            <div className={styles.dateRow}>
              <span className={styles.dateLabel}>{message("frontend.report.field.startDate")}</span> {/* frontend.report.field.startDate = 시작일 */}
              <span className={styles.dateValue}>{bookData.reportStdt || "-"}</span>
            </div>
            <div className={styles.dateRow}>
              <span className={styles.dateLabel}>{message("frontend.report.field.endDate")}</span> {/* frontend.report.field.endDate = 종료일 */}
              <span className={styles.dateValue}>{bookData.reportEndt || "-"}</span>
            </div>
          </div>
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.report.field.grade")}</h2> {/* frontend.report.field.grade = 평점 */}
          <RatingStars grade={bookData.reportGrde} />
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.report.field.content")}</h2> {/* frontend.report.field.content = 기록 */}
          <p className={styles.contentBox}>
            {bookData.reportCntn || message("frontend.common.noWrittenReport") /* frontend.common.noWrittenReport = 작성된 기록이 없습니다. */}
          </p>
        </section>

        <div className={styles.actions}>
          <button
            className={styles.actionButton}
            type="button"
            onClick={() => goUpdatePage(idNum)}
          >
            {message("frontend.report.update") /* frontend.report.update = 수정 */}
          </button>
          <button
            className={styles.deleteButton}
            type="button"
            onClick={() => deleteOnClick(idNum)}
          >
            {message("frontend.report.delete") /* frontend.report.delete = 삭제 */}
          </button>
        </div>
      </Container>
    </main>
  );
}

export default DetailPage;
