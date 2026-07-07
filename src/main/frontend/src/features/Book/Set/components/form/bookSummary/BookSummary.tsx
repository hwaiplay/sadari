import { message } from "@/app/messages/message";
import * as styles from "./BookSummary.css";

type BookSummaryProps = {
  image: string;
  title: string;
  author?: string;
  publisher?: string;
  onChangeBook?: () => void;
};

// 등록/수정 폼 상단에서 선택된 책의 핵심 정보를 보여준다.
function BookSummary({
  image,
  title,
  author,
  publisher,
  onChangeBook,
}: BookSummaryProps) {
  const subInfo = [author, publisher].filter(Boolean).join(" · ");

  return (
    <div className={styles.coverArea}>
      <div className={styles.coverFrame}>
        <img className={styles.coverImage} src={image} alt={title} />
      </div>
      <div className={styles.bookMeta}>
        <h1 className={styles.bookTitle}>{title}</h1>
        {subInfo && <p className={styles.bookSubInfo}>{subInfo}</p>}
      </div>
      {onChangeBook && (
        <>
          {/* 등록 화면에서만 책 변경 버튼을 노출해 다시 검색 화면으로 이동할 수 있게 한다. */}
          <button className={styles.changeButton} type="button" onClick={onChangeBook}>
            {message("frontend.report.bookChange") /* frontend.report.bookChange = 책 변경 */}
          </button>
        </>
      )}
    </div>
  );
}

export default BookSummary;
