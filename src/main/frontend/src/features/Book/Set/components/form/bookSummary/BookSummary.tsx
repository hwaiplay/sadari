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
 * ?좏깮??梨낆쓽 ?쒖?, ?쒕ぉ, ??먯? 梨?愿??蹂댁“ ?숈옉 踰꾪듉???쒖떆?쒕떎.
 * @author Hanwon.Jang
 * @param image 梨??쒖? ?대?吏 URL
 * @param title 梨??쒕ぉ
 * @param author 梨???? * @param onChangeBook 梨?蹂寃?踰꾪듉 ?대┃ 肄쒕갚
 * @param onShowBookInfo 梨??뺣낫 ?붾낫湲?踰꾪듉 ?대┃ 肄쒕갚
 * @return 梨??붿빟 ?뺣낫 而댄룷?뚰듃
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
