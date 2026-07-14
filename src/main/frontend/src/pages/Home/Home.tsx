import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Book from "@/features/Home/components/Book";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";
import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";

type HomeSortType = "END_DATE_DESC" | "START_DATE_DESC" | "GRADE_DESC";

type MonthlyBookGroup = {
  key: string;
  label: string;
  books: HomeBookType[];
};

const SORT_OPTIONS: Array<{ value: HomeSortType; labelKey: string }> = [
  {
    value: "END_DATE_DESC",
    labelKey: "frontend.home.sort.endDateDesc",
  },
  {
    value: "START_DATE_DESC",
    labelKey: "frontend.home.sort.startDateDesc",
  },
  {
    value: "GRADE_DESC",
    labelKey: "frontend.home.sort.gradeDesc",
  },
];

/**
 * 독후감 종료일을 기준으로 홈 화면 월 그룹 정보를 만든다.
 * @Author Hanwon.Jang
 * @param book 월 그룹을 계산할 독후감 카드 데이터
 * @return 그룹 식별자와 화면 표시 월 라벨
 */
function getMonthGroup(book: HomeBookType, sortType: HomeSortType) {
  const targetDate =
    sortType === "START_DATE_DESC" ? book.reportStdt : book.reportEndt;
  const match = targetDate?.match(/^(\d{4})-(\d{2})/);

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
 * 별점 정렬에서 사용할 그룹 라벨을 실제 별 개수로 만든다.
 * @Author Hanwon.Jang
 * @param book 별점 그룹을 계산할 독후감 카드 데이터
 * @return 별점 그룹 식별자와 화면 표시 라벨
 */
function getGradeGroup(book: HomeBookType) {
  const grade = Math.max(1, Math.min(5, Number(book.reportGrde) || 1));

  return {
    key: String(grade),
    label: String.fromCharCode(9733).repeat(grade),
  };
}

/**
 * 독후감 목록을 종료월 단위로 묶는다.
 * @Author Hanwon.Jang
 * @param bookList 월별로 묶을 독후감 목록
 * @return 월 그룹별 독후감 목록
 */
function groupBooksBySort(bookList: HomeBookType[], sortType: HomeSortType) {
  return bookList.reduce<MonthlyBookGroup[]>((groups, book) => {
    const monthGroup =
      sortType === "GRADE_DESC"
        ? getGradeGroup(book)
        : getMonthGroup(book, sortType);
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
  const location = useLocation();
  const [sortType, setSortType] = useState<HomeSortType>("END_DATE_DESC");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [appliedSearchKeyword, setAppliedSearchKeyword] = useState("");
  const [isSortOpen, setIsSortOpen] = useState(false);
  const { data, isPending } = useGetListQuery({
    bookKeyword: appliedSearchKeyword,
    sortType,
  });
  const bookList = data?.data ?? [];
  const monthlyBookGroups = useMemo(
    () => groupBooksBySort(bookList, sortType),
    [bookList, sortType],
  );
  const selectedSortOption = SORT_OPTIONS.find(
    (option) => option.value === sortType,
  );
  const hasSearchCondition = appliedSearchKeyword.trim().length > 0;

  useEffect(() => {
    const state = location.state as { resetHomeSearch?: boolean } | null;

    if (!state?.resetHomeSearch) {
      return;
    }

    setSearchKeyword("");
    setAppliedSearchKeyword("");
  }, [location.key, location.state]);

  const handleSearchSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAppliedSearchKeyword(searchKeyword.trim());
  };

  if (isPending) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  return data?.code === 200 && (bookList.length > 0 || hasSearchCondition) ? (
    <div className={styles.homeContainer}>
      <form className={styles.sortBar} onSubmit={handleSearchSubmit}>
        <label className={styles.searchLabel}>
          <span className={styles.hiddenLabel}>
            {message("frontend.home.search.label")}
          </span>
          <input
            className={styles.searchInput}
            type="search"
            value={searchKeyword}
            placeholder={message("frontend.home.search.label")}
            onChange={(event) => setSearchKeyword(event.target.value)}
          />
          <button
            className={styles.searchButton}
            type="submit"
            aria-label={message("frontend.home.search.button")}
          >
            <svg
              className={styles.searchIcon}
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                d="M10.8 5.2a5.6 5.6 0 1 1 0 11.2 5.6 5.6 0 0 1 0-11.2Z"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
              />
              <path
                d="m15 15 4 4"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </label>

        <div className={styles.sortDropdown}>
          <button
            className={styles.sortTrigger}
            type="button"
            aria-expanded={isSortOpen}
            aria-label={message("frontend.home.sort.label")}
            onClick={() => setIsSortOpen((prev) => !prev)}
          >
            <span>
              {message(selectedSortOption?.labelKey ?? SORT_OPTIONS[0].labelKey)}
            </span>
            <span className={styles.sortArrow}>
              {String.fromCharCode(isSortOpen ? 9650 : 9660)}
            </span>
          </button>

          {isSortOpen && (
            <div className={styles.sortMenu}>
              {SORT_OPTIONS.map((option) => (
                <button
                  className={`${styles.sortMenuItem} ${
                    sortType === option.value ? styles.sortMenuItemActive : ""
                  }`}
                  key={option.value}
                  type="button"
                  onClick={() => {
                    setSortType(option.value);
                    setIsSortOpen(false);
                  }}
                >
                  {message(option.labelKey)}
                </button>
              ))}
            </div>
          )}
        </div>
      </form>
      {bookList.length > 0 ? (
        <div className={styles.monthGroupStack}>
          {monthlyBookGroups.map((group) => (
            <section className={styles.monthGroup} key={group.key}>
              <div className={styles.monthGroup__inner}>
                <div
                  className={`${styles.monthLabel} ${
                    sortType === "GRADE_DESC" ? styles.gradeLabel : ""
                  }`}
                >
                  {group.label}
                </div>
                <div className={styles.bookGrid}>
                  {chunkBooks(group.books, 3).map((rowBooks, rowIndex) => (
                    <div
                      className={styles.bookRow}
                      key={`${group.key}-${rowIndex}`}
                    >
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
      ) : (
        <div className={styles.emptySearchResult}>
          {message("frontend.home.search.empty")}
        </div>
      )}
    </div>
  ) : (
    <Container className={styles.emptyHomeContainer}>
      <h1 className={styles.emptyTitle}>{message("frontend.home.empty")}</h1>
    </Container>
  );
}

export default Home;
