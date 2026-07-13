import { message } from "@/app/messages/message";
import { useLocation, useNavigate } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import { NaverApiResultType } from "@/features/Book/types/book.type";
import { useBookRatingAverageByIsbn } from "@/features/Book/Detail/hook/useBookRatingAverage";
import { stripHtmlTags } from "@/app/utils/htmlUtil";
import * as styles from "@/pages/Book/Info/BookInfoPage.css";

/**
 * 검색 결과에서 선택한 책의 상세 정보를 표시하고 독후감 작성 화면으로 연결한다.
 * @Author Hanwon.Jang
 * @return 검색 책 정보 상세 페이지 컴포넌트
 */
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
  const author = stripHtmlTags(book.author);
  const publisher = stripHtmlTags(book.publisher);
  const description = stripHtmlTags(book.description);
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
                <span className={styles.ratingStar}>★</span>
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
