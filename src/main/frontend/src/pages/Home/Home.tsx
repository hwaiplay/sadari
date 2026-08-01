/**
 * src/main/frontend/src/pages/Home/Home.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { Container } from "@/components/Layout/Container/Container";
import Book from "@/features/Home/components/Book";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import { HomeBookType } from "@/features/Book/types/book.type";
import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {searchBar} from "./Home.css";

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
  const gradeLabel =
    grade === 0
      ? "0"
      : String.fromCharCode(9733).repeat(starCount);

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
  const [isSortOpen, setIsSortOpen] = useState(false);
  const sortDropdownRef = useRef<HTMLDivElement>(null);
  const { data, isPending, isError, error } = useGetListQuery({
    bookKeyword: appliedSearchKeyword,
    sortType,
  });
  const bookList = data?.data ?? [];
  const monthlyBookGroups = useMemo(
    () => groupBooksBySort(bookList, sortType),
    [bookList, sortType],
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

  // 독후감 정렬 팝업이 열린 동안 화면 바깥 클릭으로 닫을 수 있게 감시한다
  useEffect(() => {
    // 정렬 팝업이 닫혀 있으면 문서 클릭 감시를 등록하지 않는다
    if (!isSortOpen) {
      // 정렬 팝업 외부 클릭 감시 없이 종료한다
      return;
    }

    /**
     * 독후감 정렬 팝업 바깥을 누르면 열린 옵션 목록을 닫는다
     *
     * @author HanWon.Jang
     * @param event 문서 포인터 입력 이벤트
     * @return 반환값이 없다
     */
    const handleSortOutsidePointerDown = (event: PointerEvent): void => {
      const target = event.target;

      // 정렬 버튼이나 옵션 목록 내부 입력은 현재 팝업 상태를 유지한다
      if (target instanceof Node && sortDropdownRef.current?.contains(target)) {
        // 정렬 팝업 내부 입력의 기본 동작을 계속 처리한다
        return;
      }

      // 정렬 영역 바깥을 누르면 옵션 목록을 닫는다
      setIsSortOpen(false);
    };

    // 정렬 팝업보다 먼저 바깥 입력을 확인하도록 문서 포인터 이벤트를 등록한다
    document.addEventListener("pointerdown", handleSortOutsidePointerDown);

    // 정렬 팝업이 닫히거나 화면이 해제될 때 문서 이벤트를 정리하는 함수를 반환한다
    return () => {
      // 더 이상 필요하지 않은 정렬 팝업 바깥 입력 감시를 해제한다
      document.removeEventListener("pointerdown", handleSortOutsidePointerDown);
    };
  }, [isSortOpen]);

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

      {/* 독후감 정렬 드롭다운 영역 */}
      <div className={styles.sortDropdown} ref={sortDropdownRef}>
        {/* "독후감 정렬" */}
        <button
            className={styles.sortTrigger}
            type="button"
            aria-expanded={isSortOpen}
            aria-haspopup="menu"
            aria-label={message("frontend.home.sort.label")}
            onClick={() => setIsSortOpen((prev) => !prev)}
        >
          <img
              className={styles.sortIcon}
              src="/img/icons/arrow-sorting.svg"
              alt=""
          />
        </button>

        {isSortOpen && (
            /* 독후감 정렬 옵션 영역 */
            <div className={styles.sortMenu} role="menu">
              {SORT_OPTIONS.map((option) => (
                  <button
                      className={`${styles.sortMenuItem} ${
                          sortType === option.value
                              ? styles.sortMenuItemActive
                              : ""
                      }`}
                      key={option.value}
                      type="button"
                      role="menuitem"
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
