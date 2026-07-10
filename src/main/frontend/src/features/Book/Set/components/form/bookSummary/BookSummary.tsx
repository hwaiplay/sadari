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

/**
 * 선택한 책의 표지, 제목, 저자와 책 관련 보조 동작 버튼을 표시한다.
 * @Author Hanwon.Jang
 * @param image 책 표지 이미지 URL
 * @param title 책 제목
 * @param author 책 저자
 * @param onChangeBook 책 변경 버튼 클릭 콜백
 * @param onShowBookInfo 책 정보 더보기 버튼 클릭 콜백
 * @return 책 요약 정보 컴포넌트
 */
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
