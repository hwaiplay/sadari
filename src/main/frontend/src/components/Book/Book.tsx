import React from "react";
import { BookProps } from "../../types/Book";
import * as styles from "./book.css";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

function Book({
  id,
  title,
  author,
  color,
  className,
}: BookProps & { className?: string }) {
  const navigate = useNavigate();
  return (
    <Link
      to={`/detail/${id}`}
      className={`${styles.book} ${className ?? ""}`}
      style={{ backgroundColor: `#${color}` }}
    >
      <div className={styles.title}>{title}</div>
    </Link>
    // <div
    //   className={`${styles.book} ${className ?? ""}`}
    //   style={{ backgroundColor: `#${color}` }}
    //   onClick={handleClick}
    // >
    //   <div className={styles.title}>{title}</div>
    //   {/* <div className={styles.author}>{author}</div> */}
    // </div>
  );
}

export default Book;
