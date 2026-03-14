import { useEffect, useState } from "react";
import axios from "axios";
import { Container } from "../../components/Layout/Container/Container";
import Book from "../../components/Book/Book";
import * as styles from "./Home.css";
import { homeDummyData } from "../../assets/dummy";
import { BookProps } from "../../types/Book";
import { tilt } from "../../components/Book/book.css";
import clsx from "clsx";

function chunkArray<T>(array: T[], size: number) {
  const result = [];

  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }

  return result;
}

function Home() {
  const dummy = homeDummyData;
  const [booksData, setBooksData] = useState<BookProps[]>(homeDummyData);

  const firstRow = booksData.slice(0, 5);
  const rows = chunkArray(booksData.slice(5), 6);
  const rowCount = Math.max(3, rows.length);

  booksData.length;
  //   useEffect(() => {
  //   setBooksData(dummy);
  // }, []);
  return (
    <>
      {/* 첫 줄 */}
      <Container className={clsx(styles.row5, styles.rowContainer)}>
        {firstRow.map((book, index) => (
          <Book
            key={book.id}
            {...book}
            className={index === firstRow.length - 5 ? tilt : ""}
          />
        ))}
      </Container>

      {/* 나머지 */}
      {Array.from({ length: rowCount }).map((_, rowIndex) => (
        <Container className={clsx(styles.row6, styles.rowContainer)}>
          {rows[rowIndex]?.map((book) => (
            <Book key={book.id} {...book} />
          ))}
        </Container>
      ))}
    </>
  );
}

export default Home;
