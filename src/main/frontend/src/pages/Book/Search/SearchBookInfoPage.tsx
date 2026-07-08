import { message } from "@/app/messages/message";
import { useLocation, useNavigate } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import { NaverApiResultType } from "@/features/Book/types/book.type";
import * as styles from "@/pages/Book/Info/BookInfoPage.css";

function stripTags(value?: string) {
  return value?.replace(/<[^>]*>/g, "") ?? "";
}

function SearchBookInfoPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const book = location.state?.book as NaverApiResultType | undefined;

  if (!book) {
    return <h3>{message("frontend.common.noBookInfo")}</h3>;
  }

  const title = stripTags(book.title);
  const author = stripTags(book.author);
  const publisher = stripTags(book.publisher);
  const description = stripTags(book.description);
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
          <p className={styles.meta}>{author}</p>
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
              <span className={styles.infoLabel}>ISBN</span>
              <p className={styles.infoValue}>{book.isbn || "-"}</p>
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
