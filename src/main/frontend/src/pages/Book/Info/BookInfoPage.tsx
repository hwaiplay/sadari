import { message } from "@/app/messages/message";
import { useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import { useBookInfo } from "@/features/Book/Detail/hook/useBookInfo";
import * as styles from "./BookInfoPage.css";

/**
 * 저장된 독후감에 연결된 책 상세 정보를 조회해 표시한다.
 * @Author Hanwon.Jang
 * @return 책 정보 상세 페이지 컴포넌트
 */
function BookInfoPage() {
  const { id } = useParams();
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
          <p className={styles.meta}>{bookInfo.bookAthr}</p>
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
              <span className={styles.infoLabel}>ISBN</span>
              <p className={styles.infoValue}>{bookInfo.bookIsbn || "-"}</p>
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
