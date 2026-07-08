import * as styles from "./Book.css";
import { Link } from "react-router-dom";
import { HomeBookType } from "@/features/Book/types/book.type";

function Book({
  reportNumb,
  bookTitl,
  bookCvim,
  readingYn,
  className,
}: HomeBookType & { className?: string }) {
  return (
    <Link
      to={`/book/detail/${reportNumb}`}
      className={`${styles.book} ${className ?? ""}`}
      aria-label={bookTitl}
    >
      <div className={styles.coverWrap}>
        {bookCvim ? (
          <img className={styles.cover} src={bookCvim} alt={bookTitl} />
        ) : (
          <div className={styles.coverFallback}>{bookTitl}</div>
        )}
        {readingYn === "Y" && <span className={styles.readingBadge}>읽는 중</span>}
      </div>
    </Link>
  );
}

export default Book;
