import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
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

/**
 * 독후감 종료일을 기준으로 홈 화면 월 그룹 정보를 만든다.
 * @Author Hanwon.Jang
 * @param book 월 그룹을 계산할 독후감 카드 데이터
 * @return 그룹 식별자와 화면 표시 월 라벨
 */
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
    label: `${year.slice(2)}.${month}`,
  };
}

/**
 * 독후감 목록을 종료월 단위로 묶는다.
 * @Author Hanwon.Jang
 * @param bookList 월별로 묶을 독후감 목록
 * @return 월 그룹별 독후감 목록
 */
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

/**
 * 한 줄에 표시할 책 개수만큼 목록을 나눈다.
 * @Author Hanwon.Jang
 * @param bookList 나눌 독후감 목록
 * @param size 한 묶음에 포함할 책 개수
 * @return 지정한 크기로 분리된 독후감 목록
 */
function chunkBooks(bookList: HomeBookType[], size: number) {
  return Array.from({ length: Math.ceil(bookList.length / size) }, (_, index) =>
    bookList.slice(index * size, index * size + size),
  );
}

/**
 * 로그인 사용자의 독후감 책장을 월별 그룹으로 렌더링한다.
 * @Author Hanwon.Jang
 * @return 홈 화면 컴포넌트
 */
function Home() {
  const { data, isPending } = useGetListQuery();

  if (isPending) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  const bookList = data?.data ?? [];
  const monthlyBookGroups = groupBooksByMonth(bookList);

  return data?.code === 200 && bookList.length > 0 ? (
    <div className={styles.homeContainer}>
      <div className={styles.monthGroupStack}>
        {monthlyBookGroups.map((group) => (
          <section className={styles.monthGroup} key={group.key}>
            <div className={styles.monthGroup__inner}>
              <div className={styles.monthLabel}>{group.label}</div>
              <div className={styles.bookGrid}>
                {chunkBooks(group.books, 3).map((rowBooks, rowIndex) => (
                  <div className={styles.bookRow} key={`${group.key}-${rowIndex}`}>
                    {rowBooks.map((book: HomeBookType) => (
                      <Book key={book.reportNumb} {...book} />
                    ))}
                  </div>
                ))}
              </div>
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
