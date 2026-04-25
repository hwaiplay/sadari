import React from "react";
import * as styles from "./Book.css";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import { HomeBookType } from "@/features/Book/types/book.type";

function Book({
  reportNumb,
  bookTitle,
  className,
}: HomeBookType & { className?: string }) {
  const navigate = useNavigate();

  return (
    <Link
      to={`/book/detail/${reportNumb}`}
      className={`${styles.book} ${className ?? ""}`}
      style={{ backgroundColor: `#ac8a8a` }}
      // style={{ backgroundColor: `#${color}` }}
    >
      <div className={styles.title}>{bookTitle}</div>
    </Link>
  );
}

export default Book;
