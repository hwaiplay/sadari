import React from "react";
import * as styles from "./Book.css";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

interface BookProps {
  id: number;
  title: string;
  color: string;
}

function Book({
  id,
  title,
  color,
  className,
}: BookProps & { className?: string }) {
  const navigate = useNavigate();
  return (
    <Link
      to={`/book/detail/${id}`}
      className={`${styles.book} ${className ?? ""}`}
      style={{ backgroundColor: `#${color}` }}
    >
      <div className={styles.title}>{title}</div>
    </Link>
  );
}

export default Book;
