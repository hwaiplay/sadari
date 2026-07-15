/**
 * src/main/frontend/src/pages/Book/Info/BookInfoPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { useNavigate, useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import { useBookInfo } from "@/features/Book/Detail/hook/useBookInfo";
import * as styles from "./BookInfoPage.css";

function BookInfoPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const reportNumb = Number(id);
  const { data, isPending } = useBookInfo(reportNumb);

  if (!id || isNaN(reportNumb)) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.bookInfo")} />;
  }

  const bookInfo = data?.data;

  if (data?.code !== 200 || !bookInfo) {
    return <h3>{data?.message}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${bookInfo.bookCvim}")`,
  } as CSSProperties;

  return (
    <main className={styles.page} style={pageStyle}>
      <Container className={styles.content}>
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookInfo.bookCvim}
              alt={bookInfo.bookTitl}
            />
          </div>
          <h1 className={styles.title}>{bookInfo.bookTitl}</h1>
          <div className={styles.authorRatingLine}>
            <p className={styles.meta}>{bookInfo.bookAthr}</p>
            {bookInfo.bookAvgGrde && (
              <span className={styles.metaSeparator}>|</span>
            )}
            {bookInfo.bookAvgGrde && (
              <span
                className={styles.ratingSummary}
                aria-label={message("frontend.report.gradeValue", [
                  bookInfo.bookAvgGrde,
                ])}
              >
                <span className={styles.ratingStar}>{"\u2605"}</span>
                <span className={styles.ratingValue}>
                  {bookInfo.bookAvgGrde}
                </span>
              </span>
            )}
          </div>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={() =>
              navigate(`/book/public-reports/report/${reportNumb}`, {
                state: {
                  title: bookInfo.bookTitl,
                  author: bookInfo.bookAthr,
                  cover: bookInfo.bookCvim,
                  ratingAverage: bookInfo.bookAvgGrde,
                },
              })
            }
          >
            {message("frontend.book.publicReports.button")}
          </button>
        </section>

        <div className={styles.contentPanel}>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.common.bookInfo")}
            </h2>
            <div className={styles.infoGrid}>
              <span className={styles.infoLabel}>
                {message("frontend.common.author")}
              </span>
              <p className={styles.infoValue}>{bookInfo.bookAthr || "-"}</p>
              <span className={styles.infoLabel}>
                {message("frontend.common.publisher")}
              </span>
              <p className={styles.infoValue}>{bookInfo.bookPubl || "-"}</p>
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.common.bookDescription")}
            </h2>
            <p className={styles.description}>
              {bookInfo.bookDesc || message("frontend.common.noBookDescription")}
            </p>
          </section>
        </div>
      </Container>
    </main>
  );
}

export default BookInfoPage;