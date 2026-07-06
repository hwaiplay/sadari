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

const DEFAULT_BOOK_COLOR = "#ac8a8a";

function normalizeColor(color?: string) {
  if (!color) {
    return DEFAULT_BOOK_COLOR;
  }

  const normalized = color.startsWith("#") ? color : `#${color}`;
  return /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(normalized)
    ? normalized
    : DEFAULT_BOOK_COLOR;
}

function getReadableTextColor(backgroundColor: string) {
  const hex = normalizeColor(backgroundColor).replace("#", "");
  const normalized =
    hex.length === 3
      ? hex
          .split("")
          .map((char) => char + char)
          .join("")
      : hex;

  const red = parseInt(normalized.slice(0, 2), 16);
  const green = parseInt(normalized.slice(2, 4), 16);
  const blue = parseInt(normalized.slice(4, 6), 16);
  const luminance = (red * 299 + green * 587 + blue * 114) / 1000;

  return luminance > 150 ? "#151515" : "#ffffff";
}

function Book({
  reportNumb,
  bookTitl,
  reportColr,
  className,
}: HomeBookType & { className?: string }) {
  let sliceTitle = bookTitl;
  const backgroundColor = normalizeColor(reportColr);

  if (bookTitl?.length > 14) {
    sliceTitle = bookTitl?.slice(0, 14) + "•••";
  }

  return (
    <Link
      to={`/book/detail/${reportNumb}`}
      className={`${styles.book} ${className ?? ""}`}
      style={{
        backgroundColor,
        color: getReadableTextColor(backgroundColor),
      }}
    >
      <div className={styles.title}>{sliceTitle}</div>
    </Link>
  );
}

export default Book;
