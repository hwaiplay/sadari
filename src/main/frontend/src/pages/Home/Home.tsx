import { message } from "@/app/messages/message";
import clsx from "clsx";
import { Container } from "@/components/Layout/Container/Container";
import { container } from "@/components/Layout/Container/container.css";
import Book from "@/features/Home/components/Book";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";

type MonthlyBookGroup = {
  key: string;
  label: string;
  books: HomeBookType[];
};

function getMonthGroup(book: HomeBookType) {
  const match = book.reportEndt?.match(/^(\d{4})-(\d{2})/);

  if (!match) {
    return {
      key: "unknown",
      label: "날짜 없음",
    };
  }

  const [, year, month] = match;
  return {
    key: `${year}-${month}`,
    label: `${year}년 ${Number(month)}월`,
  };
}

function groupBooksByMonth(bookList: HomeBookType[]) {
  return bookList.reduce<MonthlyBookGroup[]>((groups, book) => {
    const monthGroup = getMonthGroup(book);
    const currentGroup = groups[groups.length - 1];

    if (currentGroup?.key === monthGroup.key) {
      currentGroup.books.push(book);
      return groups;
    }

    groups.push({
      ...monthGroup,
      books: [book],
    });
    return groups;
  }, []);
}

function Home() {
  const { data, isPending } = useGetListQuery();

  if (isPending) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  const bookList = data?.data ?? [];
  const monthlyBookGroups = groupBooksByMonth(bookList);

  return data?.code === 200 && bookList.length > 0 ? (
    <div className={clsx(styles.homeContainer, container)}>
      <div className={styles.monthGroupStack}>
        {monthlyBookGroups.map((group) => (
          <section className={styles.monthGroup} key={group.key}>
            <div className={styles.monthLabel}>{group.label}</div>
            <div className={styles.bookGrid}>
              {group.books.map((book: HomeBookType) => (
                <Book key={book.reportNumb} {...book} />
              ))}
            </div>
          </section>
        ))}
      </div>
    </div>
  ) : (
    <Container className={styles.emptyHomeContainer}>
      <h1 className={styles.emptyTitle}>{message("frontend.home.empty")}</h1>
    </Container>
  );
}

export default Home;
