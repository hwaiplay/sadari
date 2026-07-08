import { message } from "@/app/messages/message";
import * as styles from "./BookSummary.css";

type BookSummaryProps = {
  image: string;
  title: string;
  author?: string;
  publisher?: string;
  onChangeBook?: () => void;
  onShowBookInfo?: () => void;
};

function BookSummary({
  image,
  title,
  author,
  onChangeBook,
  onShowBookInfo,
}: BookSummaryProps) {
  return (
    <div className={styles.coverArea}>
      <div className={styles.coverFrame}>
        <img className={styles.coverImage} src={image} alt={title} />
      </div>
      <div className={styles.bookMeta}>
        <h1 className={styles.bookTitle}>{title}</h1>
        {author && <p className={styles.bookSubInfo}>{author}</p>}
      </div>
      {(onShowBookInfo || onChangeBook) && (
        <div className={styles.buttonGroup}>
          {onShowBookInfo && (
            <button
              className={styles.bookInfoButton}
              type="button"
              onClick={onShowBookInfo}
            >
              {message("frontend.report.bookInfoMore")}
            </button>
          )}
          {onChangeBook && (
            <button
              className={styles.changeButton}
              type="button"
              onClick={onChangeBook}
            >
              {message("frontend.report.bookChange")}
            </button>
          )}
        </div>
      )}
    </div>
  );
}

export default BookSummary;
