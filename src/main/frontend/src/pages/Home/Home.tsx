/**
 * src/main/frontend/src/pages/Home/Home.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { Container } from "@/components/Layout/Container/Container";
import Book from "@/features/Home/components/Book";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

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

function getMonthGroup(book: HomeBookType, sortType: HomeSortType) {
  const targetDate =
    sortType === "START_DATE_DESC" ? book.reptStdt : book.reptEndt;
  const match = targetDate?.match(/^(\d{4})-(\d{2})/);

  if (!match) {
    return {
      key: "unknown",
      label: "?좎쭨 ?놁쓬",
    };
  }

  const [, year, month] = match;
  return {
    key: `${year}-${month}`,
    label: `${year.slice(2)}.${month}`,
  };
}

function getGradeGroup(book: HomeBookType) {
  const rawGrade = Number(book.reptGrde);
  const grade = Number.isFinite(rawGrade) ? Math.max(0, Math.min(5, rawGrade)) : 0;
  const starCount = Math.floor(grade);
  const gradeLabel =
    grade === 0
      ? "0"
      : String.fromCharCode(9733).repeat(starCount);

  return {
    key: String(starCount),
    label: gradeLabel,
  };
}

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

function chunkBooks(bookList: HomeBookType[], size: number) {
  return Array.from({ length: Math.ceil(bookList.length / size) }, (_, index) =>
    bookList.slice(index * size, index * size + size),
  );
}

function getHomeErrorMessage(error: unknown) {
  return getApiErrorMessage(error, message("frontend.common.tryAgain"));
}

function Home() {
  const location = useLocation();
  const navigate = useNavigate();
  const [sortType, setSortType] = useState<HomeSortType>("END_DATE_DESC");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [appliedSearchKeyword, setAppliedSearchKeyword] = useState("");
  const [isSortOpen, setIsSortOpen] = useState(false);
  const { data, isPending, isError, error } = useGetListQuery({
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

  if (isError) {
    return (
      <Container className={styles.emptyHomeContainer}>
        <h1 className={styles.emptyTitle}>{getHomeErrorMessage(error)}</h1>
      </Container>
    );
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
                        <Book key={book.reptNumb} {...book} />
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
          <p className={styles.emptySearchText}>
            {message("frontend.home.search.empty")}
          </p>
          <button
            className={styles.emptySearchButton}
            type="button"
            onClick={() => {
              // 독후감 검색 결과가 없을 때 같은 검색어를 도서 검색 화면에 전달해 즉시 도서 API 검색을 실행한다.
              navigate("/book/search", {
                state: { initialSearchKeyword: appliedSearchKeyword.trim() },
              });
            }}
          >
            {/* 화면표시: "{0}"으로 도서검색하기 */}
            <span>
              {message("frontend.home.search.goBookSearch", [
                appliedSearchKeyword.trim(),
              ])}
            </span>
            <svg
              className={styles.emptySearchButtonIcon}
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                d="M9 6l6 6-6 6"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
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
