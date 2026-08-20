import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import CustomSelect, {
  type CustomSelectOption,
} from "@/components/Select/CustomSelect";
import BookRatingSummary from "@/features/Book/components/BookRatingSummary/BookRatingSummary";
import { PopularKeywordSlider } from "@/features/Book/Search/components/PopularKeywordSlider";
import { useSearchBookPage } from "@/features/Book/Search/hook/useSearchBookPage";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { PopularBookPeriodType } from "@/features/Book/types/book.type";
import * as styles from "./SearchBookPage.css";

const DESCRIPTION_PREVIEW_LENGTH = 90;
const POPULAR_PERIOD_OPTIONS: readonly CustomSelectOption<PopularBookPeriodType>[] = [
  {
    value: "weekly",
    label: /* "주간" */ message("frontend.book.search.popularWeeklyOption"),
  },
  {
    value: "monthly",
    label: /* "월간" */ message("frontend.book.search.popularMonthlyOption"),
  },
  {
    value: "yearly",
    label: /* "연간" */ message("frontend.book.search.popularYearlyOption"),
  },
];
const POPULAR_PERIOD_LABELS: Readonly<Record<PopularBookPeriodType, string>> = {
  weekly: /* "주간 인기 도서" */ message("frontend.book.search.popularWeekly"),
  monthly: /* "월간 인기 도서" */ message("frontend.book.search.popularMonthly"),
  yearly: /* "연간 인기 도서" */ message("frontend.book.search.popularYearly"),
};

/**
 * 책 검색 입력과 결과 목록을 표시하고 사용자 동작을 검색 훅에 전달한다.
 *
 * @author HanWon.Jang
 * @return 책 검색 페이지 컴포넌트
 */
const SearchBookPage = () => {

  const {
    bookResult,
    handleLoadMore,
    handleMoreInfo,
    handlePopularPeriodChange,
    handlePopularKeywordSelect,
    handleSearchClick,
    handleSelectBook,
    hasMore,
    isInitialLoading,
    isLoadingMore,
    isPopularMode,
    isSearching,
    popularPeriod,
    popularKeywordList,
    searchKeyword,
    selectingBookIsbn,
    setSearchKeyword,
  } = useSearchBookPage();

  // 책 검색 입력과 조회 결과 목록 화면을 반환한다.
  return (
    <main className={styles.page}>
      <Container className={styles.content}>
        {/* 책 검색 입력과 인기 검색어 및 인기 도서 기간 선택 영역 */}
        <section className={styles.searchSection}>
          {/* 책 검색어 입력과 검색 실행 영역 */}
          <form className={styles.searchBar} onSubmit={handleSearchClick}>
            <label className={styles.searchLabel}>
              <span className={styles.hiddenLabel}>
                {/* "책 제목, 저자를 입력하세요" */}
                {message("frontend.book.search.placeholder")}
              </span>
              <input
                className={styles.searchInput}
                type="text"
                name="searchKeyword"
                id="searchKeyword"
                placeholder={message("frontend.book.search.placeholder")}
                value={searchKeyword}
                onChange={(event) => setSearchKeyword(event.target.value)}
              />
              {/* "검색" */}
              <button
                className={styles.searchButton}
                type="submit"
                disabled={isSearching}
                aria-label={/* "검색" */ message("frontend.common.search")}
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

          {/* 인기 도서 모드에서만 인기 검색어와 기간 선택을 같은 높이의 한 행으로 제공한다 */}
          {isPopularMode && (
            <div className={styles.popularControlBar}>
              {/* 최근 인기 검색어 한 건씩 왼쪽 영역에서 세로로 교체하고 즉시 검색하는 영역 */}
              <PopularKeywordSlider
                keywordList={popularKeywordList}
                isDisabled={isSearching}
                onSelect={handlePopularKeywordSelect}
              />

              {/* 같은 행 오른쪽의 기간별 인기 도서 선택 영역 */}
              <div className={styles.popularPeriodBar}>
                <CustomSelect
                  value={popularPeriod}
                  options={POPULAR_PERIOD_OPTIONS}
                  ariaLabel={
                    /* "인기 도서 기간" */ message(
                      "frontend.book.search.popularPeriodLabel",
                    )
                  }
                  triggerContent={POPULAR_PERIOD_LABELS[popularPeriod]}
                  className={styles.popularPeriodSelect}
                  triggerClassName={styles.popularPeriodSelectTrigger}
                  optionListClassName={styles.popularPeriodOptionList}
                  optionClassName={styles.popularPeriodOption}
                  onChange={handlePopularPeriodChange}
                />
              </div>
            </div>
          )}
        </section>

        {isInitialLoading ? (
          /* 선택 기간의 인기 도서 조회 상태 영역 */
          <Loading isFullScreen={false} />
        ) : bookResult &&
          (bookResult.length > 0 ? (
            <div className={styles.resultList}>
              {bookResult.map((book, index) => {

                const title = stripHtmlTags(book.title);
                const author = normalizeBookAuthor(book.author);
                const publisher = stripHtmlTags(book.publisher);
                const description = stripHtmlTags(book.description);
                const preview =
                  description.length > DESCRIPTION_PREVIEW_LENGTH
                    ? `${description.slice(0, DESCRIPTION_PREVIEW_LENGTH)}...`
                    : description;

                // 검색된 책의 요약 정보와 동작 버튼을 반환한다.
                return (
                  <article
                    className={styles.resultCard}
                    key={`${book.isbn}-${index}`}
                  >
                    {/* 검색된 책의 표지 영역 */}
                    <div className={styles.coverArea}>
                      <div className={styles.coverFrame}>
                        <img
                          className={styles.coverImage}
                          src={getBookCoverImageSource(book.image)}
                          data-fallback-image={book.thumbnailImage}
                          onError={handleBookCoverImageError}
                          alt={message("frontend.book.search.coverAlt", [
                            title,
                          ])}
                        />
                      </div>
                    </div>

                    {/* 인기 도서는 표지 아래와 제목 위에 순위 및 작성자 수를 표시한다. */}
                    {isPopularMode &&
                      book.rank !== undefined &&
                      book.reportCount !== undefined && (
                        <p className={styles.popularRank}>
                          {/* "{순위}위 · {작성자 수}명" */}
                          {message("frontend.book.search.popularRank", [
                            book.rank,
                            book.reportCount,
                          ])}
                        </p>
                      )}

                    {/* 검색된 책의 제목과 저자 및 출판사 또는 평균 평점 영역 */}
                    <div className={styles.bookMeta}>
                      <h2 className={styles.bookTitle}>{title}</h2>
                      {isPopularMode ? (
                        /* 인기 도서는 출판사 대신 도서 정보와 같은 평균 별점을 표시한다. */
                        <div className={styles.authorRatingLine}>
                          <p className={styles.meta}>{author}</p>
                          {/* 평균 평점이 있으면 저자 옆에 구분선과 도서 평균 별점을 표시한다. */}
                          {book.ratingAverage !== null &&
                            book.ratingAverage !== undefined &&
                            book.ratingAverage !== "" && (
                              <>
                                <span className={styles.metaSeparator}>|</span>
                                <BookRatingSummary rating={book.ratingAverage} />
                              </>
                            )}
                        </div>
                      ) : (
                        /* 직접 검색 결과는 기존 저자와 출판사 정보를 유지한다. */
                        <p className={styles.meta}>
                          {author} / {publisher}
                        </p>
                      )}
                    </div>

                    {!isPopularMode && (
                      /* 직접 검색 결과에서만 도서 소개를 세 줄까지 표시한다. */
                      <p className={styles.description}>
                        {preview || message("frontend.common.noBookDescription")}
                      </p>
                    )}

                    {/* 검색된 책의 상세보기와 선택 버튼 영역 */}
                    <div className={styles.actions}>
                      <button
                        className={styles.actionButton}
                        type="button"
                        onClick={() => handleMoreInfo(book)}
                      >
                        {/* "더보기" */}
                        {message("frontend.book.search.more")}
                      </button>
                      <button
                        className={styles.primaryButton}
                        type="button"
                        onClick={() => void handleSelectBook(book)}
                        disabled={selectingBookIsbn !== null}
                      >
                        {/* "선택" */}
                        {/* "선택" */ message("frontend.common.select")}
                      </button>
                    </div>
                  </article>
                );
              })}

              <InfiniteScrollTrigger
                hasNext={hasMore}
                isLoading={isLoadingMore}
                onLoadMore={() => void handleLoadMore()}
              >
                {message("frontend.book.search.loadingMore")}
              </InfiniteScrollTrigger>
            </div>
          ) : (
            <p className={styles.emptyMessage}>
              {isPopularMode ? (
                <>
                  {/* "선택한 기간의 인기 도서가 아직 없습니다." */}
                  {message("frontend.book.search.popularEmpty")}
                </>
              ) : (
                <>
                  {/* "검색 결과가 없습니다." */}
                  {message("frontend.book.search.noResult")}
                </>
              )}
            </p>
          ))}
      </Container>
    </main>
  );
};

export default SearchBookPage;
