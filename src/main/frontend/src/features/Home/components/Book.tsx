import { message } from "@/app/messages/message";
import { HomeBookType } from "@/features/Book/types/book.type";
import { Link } from "react-router-dom";
import * as styles from "./Book.css";

/**
 * 홈 책장에 표시할 책 표지와 독서 중 배지를 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @param reptNumb 이동할 독후감 번호
 * @param bookTitl 책 제목
 * @param bookCvim 책 표지 이미지 URL
 * @param readingYn 독서 중 여부
 * @param className 외부에서 전달하는 추가 className
 * @return 책장 책 컴포넌트
 */
function Book({
  reptNumb,
  bookTitl,
  bookCvim,
  readingYn,
  className,
}: HomeBookType & { className?: string }) {
  return (
    <Link
      to={`/book/detail/${reptNumb}`}
      className={`${styles.book} ${className ?? ""}`}
      aria-label={bookTitl}
    >
      <div className={styles.coverWrap}>
        {bookCvim ? (
          <img className={styles.cover} src={bookCvim} alt={bookTitl} />
        ) : (
          <div className={styles.coverFallback}>{bookTitl}</div>
        )}
        {readingYn === "Y" && (
          <span className={styles.readingBadge}>
            {message("frontend.report.status.reading")}
          </span>
        )}
      </div>
    </Link>
  );
}

export default Book;
