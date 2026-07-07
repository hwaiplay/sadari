import { message } from "@/app/messages/message";
import { useParams } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import { useBookInfo } from "@/features/Book/Detail/hook/useBookInfo";
import * as styles from "./BookInfoPage.css";

function BookInfoPage() {
  const { id } = useParams();
  const reportNumb = Number(id);
  const { data, isPending } = useBookInfo(reportNumb);

  if (!id || isNaN(reportNumb)) {
    return <div>{message("frontend.common.invalidAccess")}</div>; // frontend.common.invalidAccess = 잘못된 접근입니다
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.bookInfo")} />; // frontend.report.loading.bookInfo = 도서 정보를 불러오는 중
  }

  const bookInfo = data?.data;

  if (data?.code !== 200 || !bookInfo) {
    return <h3>{data?.message}</h3>;
  }

  return (
    <main className={styles.page}>
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
          <p className={styles.meta}>
            {bookInfo.bookAthr} · {bookInfo.bookPubl}
          </p>
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.common.bookInfo")}</h2> {/* frontend.common.bookInfo = 도서 정보 */}
          <div className={styles.infoGrid}>
            <span className={styles.infoLabel}>{message("frontend.common.author")}</span> {/* frontend.common.author = 저자 */}
            <p className={styles.infoValue}>{bookInfo.bookAthr || "-"}</p>
            <span className={styles.infoLabel}>{message("frontend.common.publisher")}</span> {/* frontend.common.publisher = 출판사 */}
            <p className={styles.infoValue}>{bookInfo.bookPubl || "-"}</p>
            <span className={styles.infoLabel}>ISBN</span>
            <p className={styles.infoValue}>{bookInfo.bookIsbn || "-"}</p>
          </div>
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>{message("frontend.common.bookDescription")}</h2> {/* frontend.common.bookDescription = 책 소개 */}
          <p className={styles.description}>
            {bookInfo.bookDesc || message("frontend.common.noBookDescription") /* frontend.common.noBookDescription = 등록된 책 소개가 없습니다. */}
          </p>
        </section>
      </Container>
    </main>
  );
}

export default BookInfoPage;
