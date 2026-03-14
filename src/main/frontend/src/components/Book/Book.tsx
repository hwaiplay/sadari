import React from "react";
import { BookProps } from "../../types/Book";
import * as styles from "./book.css";

function Book({
  title,
  author,
  color,
  className,
}: BookProps & { className?: string }) {
  return (
    <div
      className={`${styles.book} ${className ?? ""}`}
      style={{ backgroundColor: `#${color}` }}
    >
      <div className={styles.title}>{title}</div>
      {/* <div className={styles.author}>{author}</div> */}
    </div>
  );
}

export default Book;
