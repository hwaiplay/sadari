import { message } from "@/app/messages/message";
import { useNavigate, useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { useDeleteMutation } from "@/features/Book/Delete/useDeleteMutation";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import * as styles from "./DetailPage.css";

/**
 * 독후감 평점을 별점 UI로 변환해 표시한다.
 * @Author Hanwon.Jang
 * @param grade 문자열로 전달된 1점부터 5점까지의 평점
 * @return 별점 표시 컴포넌트
 */
function RatingStars({ grade }: { grade: string }) {
  const rating = Math.max(0, Math.min(5, Number(grade) || 0));

  return (
    <div
      className={styles.stars}
      aria-label={message("frontend.report.gradeValue", [rating])}
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

/**
 * 독후감 상세 조회 결과를 화면에 표시하고 수정, 삭제, 책 정보 이동 동작을 제공한다.
 * @Author Hanwon.Jang
 * @return 독후감 상세 페이지 컴포넌트
 */
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
      title: message("frontend.alert.deleteConfirmTitle"),
      text: message("frontend.report.deleteConfirmText"),
      confirmButtonText: message("frontend.report.delete"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (confirmed.isConfirmed) {
      mutate(reportNumb);
    }
  };

  if (data?.code === 2004) {
    return <div>{data.message}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
  }

  const bookData = data?.data;

  if (data?.code !== 200 || !bookData) {
    return <h3>{data?.message}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${bookData.bookCvim}")`,
  } as CSSProperties;

  return (
    <main className={styles.page} style={pageStyle}>
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
            {message("frontend.report.bookInfoMore")}
          </button>
        </section>

        <div className={styles.contentPanel}>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.status")}
            </h2>
            <div className={styles.statusPill}>
              {bookData.reportStatName || bookData.reportStat}
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.period")}
            </h2>
            <div className={styles.dateStack}>
              <div className={styles.dateRow}>
                <span className={styles.dateLabel}>
                  {message("frontend.report.field.startDate")}
                </span>
                <span className={styles.dateValue}>
                  {bookData.reportStdt || "-"}
                </span>
              </div>
              <div className={styles.dateRow}>
                <span className={styles.dateLabel}>
                  {message("frontend.report.field.endDate")}
                </span>
                <span className={styles.dateValue}>
                  {bookData.reportEndt || "-"}
                </span>
              </div>
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.grade")}
            </h2>
            <RatingStars grade={bookData.reportGrde} />
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.content")}
            </h2>
            <p className={styles.contentBox}>
              {bookData.reportCntn || message("frontend.common.noWrittenReport")}
            </p>
          </section>

          <div className={styles.actions}>
            <button
              className={styles.actionButton}
              type="button"
              onClick={() => goUpdatePage(idNum)}
            >
              {message("frontend.report.update")}
            </button>
            <button
              className={styles.deleteButton}
              type="button"
              onClick={() => deleteOnClick(idNum)}
            >
              {message("frontend.report.delete")}
            </button>
          </div>
        </div>
      </Container>
    </main>
  );
}

export default DetailPage;
