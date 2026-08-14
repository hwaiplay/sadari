import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import { useSearchBookPage } from "@/features/Book/Search/hook/useSearchBookPage";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import * as styles from "./SearchBookPage.css";

const DESCRIPTION_PREVIEW_LENGTH = 90;

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
    handleSearchClick,
    handleSelectBook,
    hasMore,
    isLoadingMore,
    isSearching,
    searchKeyword,
    selectingBookIsbn,
    setSearchKeyword,
  } = useSearchBookPage();

  // 책 검색 입력과 조회 결과 목록 화면을 반환한다.
  return (
    <main className={styles.page}>
      <Container className={styles.content}>
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
              aria-label={message("frontend.book.search.button")}
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

        {bookResult &&
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

                    {/* 검색된 책의 제목과 저자 및 출판사 영역 */}
                    <div className={styles.bookMeta}>
                      <h2 className={styles.bookTitle}>{title}</h2>
                      <p className={styles.meta}>
                        {author} / {publisher}
                      </p>
                    </div>

                    {/* 검색된 책의 소개 영역 */}
                    <p className={styles.description}>
                      {preview || message("frontend.common.noBookDescription")}
                    </p>

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
                        {message("frontend.book.search.select")}
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
              {/* "검색 결과가 없습니다." */}
              {message("frontend.book.search.noResult")}
            </p>
          ))}
      </Container>
    </main>
  );
};

export default SearchBookPage;
