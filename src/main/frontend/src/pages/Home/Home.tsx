import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import CustomSelect from "@/components/Select/CustomSelect";
import Book from "@/features/Home/components/Book";
import * as styles from "./Home.css";
import Loading from "@/components/Loading/Loading";
import type { HomeBookType } from "@/features/Book/types/book.type";
import { createPortal } from "react-dom";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import { useHome } from "../../features/Home/hook/useHome.tsx";

/**
 * fileName       : Home
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 메인 홈 화면
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang    주석 추가
 */

function Home() {

  // 홈 화면 렌더링에 필요한 조회 상태와 사용자 동작을 조회한다
  const {
    data,
    isPending,
    isError,
    errorMessage,
    bookList,
    bookGroups,
    sortType,
    sortOptions,
    searchKeyword,
    appliedSearchKeyword,
    hasSearchCondition,
    handleSearchChange,
    handleSearchSubmit,
    handleSortChange,
    handleBookSearch,
  } = useHome();

  // 독후감 목록을 조회하는 동안 공통 로딩 화면을 표시한다
  if (isPending) {
    // 홈 독후감 목록 로딩 화면을 반환한다
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  // 독후감 목록 조회에 실패하면 정제된 오류 문구를 표시한다
  if (isError) {
    // 홈 독후감 목록 오류 화면을 반환한다
    return (
      <Container className={styles.emptyHomeContainer}>
        <h1 className={styles.emptyTitle}>{errorMessage}</h1>
      </Container>
    );
  }

  // 조회 성공 여부와 검색 상태에 맞는 홈 화면을 반환한다
  return data?.code === 200 && (bookList.length > 0 || hasSearchCondition) ? (
      <>
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
            onChange={handleSearchChange}
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
          onChange={handleSortChange}
        />
      </div>

      {bookList.length > 0 ? (
        <div className={styles.monthGroupStack}>
          {bookGroups.map((group) => (
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
                  {group.rows.map((rowBooks, rowIndex) => (
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
            onClick={handleBookSearch}
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

  {/* 홈에서만 표시하면서 페이지 전환 transform의 영향을 받지 않는 독후감 등록 링크 */}
  {createPortal(
    <LinkButton link="/report/set" className={styles.reportSetButton}>
      <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
        <path d="M36 26H26v10a2 2 0 0 1-4 0V26H12a2 2 0 0 1 0-4h10V12a2 2 0 0 1 4 0v10h10a2 2 0 0 1 0 4Z" fill="currentColor" />
      </svg>
    </LinkButton>,
    document.body,
  )}

  </>
  ) : (
    <Container className={styles.emptyHomeContainer}>
      <LinkButton link="/report/set" className={styles.emptySetReportButton}>
        <div className={styles.emptyPlusCircle}>
          <svg width="21" height="21" viewBox="0 0 21 21" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15.4286 11.1413H11.1429V15.427C11.1429 15.6544 11.0526 15.8724 10.8919 16.0331C10.7311 16.1939 10.5131 16.2842 10.2858 16.2842C10.0584 16.2842 9.84042 16.1939 9.67968 16.0331C9.51893 15.8724 9.42862 15.6544 9.42862 15.427V11.1413H5.14291C4.91558 11.1413 4.69756 11.051 4.53682 10.8903C4.37607 10.7295 4.28577 10.5115 4.28577 10.2842C4.28577 10.0569 4.37607 9.83883 4.53682 9.67809C4.69756 9.51734 4.91558 9.42704 5.14291 9.42704H9.42862V5.14132C9.42862 4.91399 9.51893 4.69598 9.67968 4.53523C9.84042 4.37449 10.0584 4.28418 10.2858 4.28418C10.5131 4.28418 10.7311 4.37449 10.8919 4.53523C11.0526 4.69598 11.1429 4.91399 11.1429 5.14132V9.42704H15.4286C15.656 9.42704 15.874 9.51734 16.0347 9.67809C16.1955 9.83883 16.2858 10.0569 16.2858 10.2842C16.2858 10.5115 16.1955 10.7295 16.0347 10.8903C15.874 11.051 15.656 11.1413 15.4286 11.1413Z" fill="#333333"/>
          </svg>
        </div>
        <h1 className={styles.emptyTitle}>{message("frontend.home.empty")}</h1>
        <p className={styles.emptyDescription}>{message("frontend.home.emptyDescription")}</p>
      </LinkButton>
    </Container>
  );
}

export default Home;
