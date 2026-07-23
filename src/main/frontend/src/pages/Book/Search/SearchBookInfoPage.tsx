/**
 * src/main/frontend/src/pages/Book/Search/SearchBookInfoPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { useLocation, useNavigate } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import { NaverApiResultType } from "@/features/Book/types/book.type";
import { useBookRatingAverageByIsbn } from "@/features/Book/Detail/hook/useBookRatingAverage";
import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import { formatCompactDate } from "@/app/utils/dateUtil";
import * as styles from "@/pages/Book/Info/BookInfoPage.css";

function SearchBookInfoPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const book = location.state?.book as NaverApiResultType | undefined;
  const { data: ratingAverageData } = useBookRatingAverageByIsbn(
    book?.isbn ?? "",
    Boolean(book?.isbn),
  );

  if (!book) {
    return <h3>{message("frontend.common.noBookInfo")}</h3>;
  }

  const title = stripHtmlTags(book.title);
  const author = normalizeBookAuthor(book.author);
  const publisher = stripHtmlTags(book.publisher);
  const description = stripHtmlTags(book.description);
  const pubdate = formatCompactDate(stripHtmlTags(book.pubdate));
  const ratingAverage = ratingAverageData?.data;
  const pageStyle = {
    "--book-bg-image": `url("${book.image}")`,
  } as CSSProperties;

  return (
    <main className={styles.page} style={pageStyle}>
      <Container className={styles.content}>
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img className={styles.coverImage} src={book.image} alt={title} />
          </div>
          <h1 className={styles.title}>{title}</h1>
          <div className={styles.authorRatingLine}>
            <p className={styles.meta}>{author}</p>
            {ratingAverage && <span className={styles.metaSeparator}>|</span>}
            {ratingAverage && (
              <span
                className={styles.ratingSummary}
                aria-label={message("frontend.report.gradeValue", [
                  ratingAverage,
                ])}
              >
                <span className={styles.ratingStar}>{"\u2605"}</span>
                <span className={styles.ratingValue}>{ratingAverage}</span>
              </span>
            )}
          </div>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={() =>
              navigate(
                `/book/public-reports/isbn?isbn=${encodeURIComponent(
                  book.isbn,
                )}`,
                {
                  state: {
                    title,
                    author,
                    cover: book.image,
                    ratingAverage,
                  },
                },
              )
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
              <p className={styles.infoValue}>{author || "-"}</p>
              <span className={styles.infoLabel}>
                {message("frontend.common.publisher")}
              </span>
              <p className={styles.infoValue}>{publisher || "-"}</p>
              <span className={styles.infoLabel}>
                {message("frontend.common.publDate")}
              </span>
              <p className={styles.infoValue}>{pubdate || "-"}</p>
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.common.bookDescription")}
            </h2>
            <p className={styles.description}>
              {description || message("frontend.common.noBookDescription")}
            </p>
          </section>

          <button
            className={styles.selectButton}
            type="button"
            onClick={() => navigate("/set", { state: { selectedBook: book } })}
          >
            {message("frontend.book.search.writeThisBook")}
          </button>
        </div>
      </Container>
    </main>
  );
}

export default SearchBookInfoPage;
