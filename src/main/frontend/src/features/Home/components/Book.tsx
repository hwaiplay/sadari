import * as styles from "./Book.css";
import { Link } from "react-router-dom";
import { HomeBookType } from "@/features/Book/types/book.type";

/**
 * fileName       : Book
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 독후감 책 UI 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang       데이터 포맷에 맞춰 수정
 */

function Book({
  reportNumb,
  bookTitle,
  className,
}: HomeBookType & { className?: string }) {
  let sliceTitle = bookTitle;

  if (bookTitle.length > 14) {
    sliceTitle = bookTitle?.slice(0, 14) + "•••";
  }

  return (
    <Link
      to={`/book/detail/${reportNumb}`}
      className={`${styles.book} ${className ?? ""}`}
      style={{ backgroundColor: `#ac8a8a` }}
      // style={{ backgroundColor: `#${color}` }}
    >
      <div className={styles.title}>{sliceTitle}</div>
    </Link>
  );
}

export default Book;
