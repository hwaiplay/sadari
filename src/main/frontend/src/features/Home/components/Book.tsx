import * as styles from "./Book.css";
import { Link } from "react-router-dom";
import { HomeBookType } from "@/features/Book/types/book.type";

/**
 * 홈 책장에 표시할 개별 책 표지 카드를 렌더링한다.
 * @Author Hanwon.Jang
 * @param reportNumb 상세 이동에 사용할 독후감 번호
 * @param bookTitl 책 제목
 * @param bookCvim 책 표지 이미지 URL
 * @param readingYn 읽는 중 배지 표시 여부
 * @param className 외부에서 전달하는 추가 스타일 클래스
 * @return 홈 책장 책 카드 컴포넌트
 */
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
