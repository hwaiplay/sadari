/**
 * src/main/frontend/src/pages/Home/Home.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { Container } from "@/components/Layout/Container/Container";
import CustomSelect from "@/components/Select/CustomSelect";
import Book from "@/features/Home/components/Book";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";
import type { ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

type HomeSortType = "END_DATE_DESC" | "START_DATE_DESC" | "GRADE_DESC";

type MonthlyBookGroup = {
  key: string;
  label: ReactNode;
  books: HomeBookType[];
};

const SORT_OPTIONS: Array<{
  value: HomeSortType;
  labelKey: string;
}> = [
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
 * get Month Group 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param book book 입력값
 * @param sortType sort Type 입력값
 * @return 처리 결과
 */
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

/**
 * get Grade Group 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param book book 입력값
 * @return 처리 결과
 */
function getGradeGroup(book: HomeBookType) {

  const rawGrade = Number(book.reptGrde);
  const grade = Number.isFinite(rawGrade) ? Math.max(0, Math.min(5, rawGrade)) : 0;
  const starCount = Math.floor(grade);
  const gradeIcons: ReactNode[] = [];

  // 평점이 없을 때도 평점 기준 그룹임을 식별할 수 있도록 빈 별을 표시한다
  if (grade === 0) {
    // 빈 별 아이콘을 평점 그룹 라벨에 추가한다
    gradeIcons.push(
      <img
        key="empty-star"
        src="/img/icons/icon-star-rate-empty.svg"
        alt=""
        aria-hidden="true"
      />,
    );

  } else {
    // 평점 수만큼 채워진 별을 표시한다
    for (let index = 0; index < starCount; index += 1) {
      // 채워진 별 아이콘을 평점 그룹 라벨에 추가한다
      gradeIcons.push(
        <img
          key={`filled-star-${index}`}
          src="/img/icons/icon-star-rate.svg"
          alt=""
          aria-hidden="true"
        />,
      );
    }

  }

  const gradeLabel = (
    <span aria-label={`평점 ${starCount}점`}>
      {gradeIcons}
    </span>
  );

  return {
    key: String(starCount),
    label: gradeLabel,
  };
}

/**
 * group Books By Sort 기능을 처리한다
 *
 * @author HanWon.Jang
 * @param bookList book List 입력값
 * @param sortType sort Type 입력값
 * @return 처리 결과
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
 * chunk Books 기능을 처리한다
 *
 * @author HanWon.Jang
 * @param bookList book List 입력값
 * @param size size 입력값
 * @return 처리 결과
 */
function chunkBooks(bookList: HomeBookType[], size: number) {

  return Array.from({ length: Math.ceil(bookList.length / size) }, (_, index) =>
    bookList.slice(index * size, index * size + size),
  );
}

/**
 * get Home Error Message 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param error error 입력값
 * @return 처리 결과
 */
function getHomeErrorMessage(error: unknown) {

  return getApiErrorMessage(error, message("frontend.common.tryAgain"));
}

/**
 * Home 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function Home() {

  const location = useLocation();
  const navigate = useNavigate();
  const [sortType, setSortType] = useState<HomeSortType>("END_DATE_DESC");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [appliedSearchKeyword, setAppliedSearchKeyword] = useState("");
  const { data, isPending, isError, error } = useGetListQuery({
    bookKeyword: appliedSearchKeyword,
    sortType,
  });
  const bookList = data?.data ?? [];
  const monthlyBookGroups = useMemo(
    () => groupBooksBySort(bookList, sortType),
    [bookList, sortType],
  );
  const sortOptions = useMemo(
    () =>
      SORT_OPTIONS.map((option) => ({
        value: option.value,
        label: message(option.labelKey),
      })),
    [],
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

  /**
   * handle Search Submit 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param event event 입력값
   * @return 반환값이 없다
   */
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
      {/* 독후감 검색과 정렬 영역 */}
      <form className={styles.searchBar} onSubmit={handleSearchSubmit}>
        <label className={styles.searchLabel}>
          <span className={styles.hiddenLabel}>
            {/* "제목, 작가 검색" */}
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
      </form>

      {/* 독후감 정렬 영역 */}
      <div className={styles.sortBar}>
        <CustomSelect
          value={sortType}
          options={sortOptions}
          ariaLabel={message("frontend.home.sort.label")}
          className={styles.sortSelect}
          triggerClassName={styles.sortSelectTrigger}
          optionListClassName={styles.sortOptionList}
          optionClassName={styles.sortSelectOption}
          onChange={setSortType}
        />
      </div>

      {bookList.length > 0 ? (
        <div className={styles.monthGroupStack}>
          {monthlyBookGroups.map((group) => (
            /* 등록 월별 독후감 목록 영역 */
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
            {/* "검색된 독후감이 없습니다." */}
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
            {/* ""{0}"으로 도서검색하기" */}
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
