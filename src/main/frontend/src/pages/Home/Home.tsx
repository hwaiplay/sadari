/**
 * fileName       : HOME
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 메인 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

import { useEffect, useState } from "react";
import clsx from "clsx";
import { homeDummyData } from "../../app/assets/dummy";
import { Container } from "../../components/Layout/Container/Container";
import Book from "../../features/Home/Book";
import * as styles from "./Home.css";
import { container } from "../../components/Layout/Container/container.css";
import { tilt } from "../../features/Home/book.css";

function chunkArray<T>(array: T[], size: number) {
  const result = [];

  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }

  return result;
}

function Home() {
  const [booksData, setBooksData] = useState(homeDummyData);

  const firstRow = booksData.slice(0, 5); // 첫줄
  const rows = chunkArray(booksData.slice(5), 6); // 두번째 줄~끝
  const rowCount = Math.max(2, rows.length); // 최소 3개의 컨테이너 UI를 위함

  //   useEffect(() => {
  //   setBooksData(dummy);
  // }, []);
  return (
    <>
      {booksData.length !== 0 ? (
        <div className={clsx(styles.homeContainer, container)}>
          <div className={styles.row5Container}>
            {/* 첫 줄 */}
            <div className={clsx(styles.row5, styles.row)}>
              {firstRow.map((book, index) => (
                <Book
                  key={book.id}
                  {...book}
                  className={index === firstRow.length - 5 ? tilt : ""}
                />
              ))}
            </div>
          </div>

          {/* 나머지 */}
          {Array.from({ length: rowCount }).map((_, rowIndex) => (
            <div className={clsx(styles.row6, styles.row)} key={rowIndex}>
              {rows[rowIndex]?.map((book) => (
                <Book key={book.id} {...book} />
              ))}
            </div>
          ))}
        </div>
      ) : (
        <Container className={styles.emptyHomeContainer}>
          <h1 className={styles.emptyTitle}>첫 책을 꽂아 책장을 채워보세요.</h1>
        </Container>
      )}
    </>
  );
}

export default Home;
