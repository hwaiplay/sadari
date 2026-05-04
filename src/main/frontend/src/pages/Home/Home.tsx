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

import clsx from "clsx";
import { Container } from "@/components/Layout/Container/Container";
import { container } from "@/components/Layout/Container/container.css";
import Book from "@/features/Home/components/Book";
import { tilt } from "@/features/Home/components/Book.css";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";

function chunkArray<T>(array: T[], size: number) {
  const result = [];

  for (let i = 0; i < array.length; i += size) {
    result?.push(array.slice(i, i + size));
  }

  return result;
}

function Home() {
  const { data, isPending } = useGetListQuery();

  // 로딩 중
  if (isPending) {
    return <Loading title={"목록 조회중"} />;
  }

  // 독후감 리스트
  const bookList = data?.data;

  // 독후감 리스트에서 첫줄(5권) 뽑음
  const firstRow = bookList?.slice(0, 5);

  const rows: HomeBookType[][] = chunkArray(bookList.slice(5), 6); // 두번째 줄~끝
  const rowCount = Math.max(2, rows.length); // 최소 3개의 컨테이너 UI를 위함

  return bookList && data?.code === 200 ? (
    <div className={clsx(styles.homeContainer, container)}>
      <div className={styles.row5Container}>
        <div className={clsx(styles.row5, styles.row)}>
          {firstRow.map((book: HomeBookType, index: number) => (
            <Book
              key={book.reportNumb}
              {...book}
              className={index === firstRow.length - 5 ? tilt : ""}
            />
          ))}
        </div>
      </div>

      {Array.from({ length: rowCount }).map((_, rowIndex) => (
        <div className={clsx(styles.row6, styles.row)} key={rowIndex}>
          {rows[rowIndex]?.map((book) => (
            <Book key={book.reportNumb} {...book} />
          ))}
        </div>
      ))}
    </div>
  ) : (
    <Container className={styles.emptyHomeContainer}>
      <h1 className={styles.emptyTitle}>첫 책을 꽂아 책장을 채워보세요.</h1>
    </Container>
  );
}

export default Home;
