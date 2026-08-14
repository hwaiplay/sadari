import { message } from "@/app/messages/message";
import {
  assertResultDataSuccess,
  getApiErrorMessage,
} from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { FormEvent, useEffect, useState } from "react";
import api from "../../../app/api/axios";
import { BookSearchResultType } from "@/features/Book/types/book.type";
import { useLocation, useNavigate, useNavigationType } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import { moveToReportEntry } from "@/features/Book/utils/reportEntry";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import * as styles from "./SearchBookPage.css";

const SEARCH_STORAGE_KEY = "sadari:book-search";
const DESCRIPTION_PREVIEW_LENGTH = 90;
const SEARCH_PAGE_SIZE = 10;

type SearchBookPageState = {
  initialSearchKeyword?: string;
  keepSearchResult?: boolean;
};

/**
 * 책 검색, 더보기, 선택, 추가 조회 흐름을 처리하는 책 검색 화면을 렌더링합니다.
 *
 * @author HanWon.Jang
 * @return 책 검색 페이지 컴포넌트
 */
const SearchBookPage = () => {

  const [searchKeyword, setSearchKeyword] = useState("");
  const [bookResult, setBookResult] = useState<BookSearchResultType[] | null>(
    null,
  );
  const [nextStart, setNextStart] = useState(1);
  const [hasMore, setHasMore] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [selectingBookIsbn, setSelectingBookIsbn] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const navigationType = useNavigationType();

  /**
   * fetch Books 기능을 처리한다
   *
   * @author HanWon.Jang
   * @param keyword keyword 입력값
   * @param start start 입력값
   * @return 처리 결과
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const fetchBooks = async (keyword: string, start: number) => {

    const response = await api.get(
      `/book/search?query=${encodeURIComponent(keyword)}&start=${start}`,
    );

    return (assertResultDataSuccess(response.data).data ?? []) as BookSearchResultType[];
  };

  /**
   * save Search Cache 기능을 처리한다
   *
   * @author HanWon.Jang
   * @param keyword keyword 입력값
   * @param result result 입력값
   * @param next next 입력값
   * @param more more 입력값
   * @return 처리 결과
   */
  const saveSearchCache = (
    keyword: string,
    result: BookSearchResultType[],
    next: number,
    more: boolean,
  ) => {

    sessionStorage.setItem(
      SEARCH_STORAGE_KEY,
      JSON.stringify({
        searchKeyword: keyword,
        bookResult: result,
        nextStart: next,
        hasMore: more,
      }),
    );
  };

  /**
   * 전달받은 검색어로 도서 검색을 실행하고 첫 페이지 결과를 화면과 세션 캐시에 반영합니다.
   * 홈 독후감 검색 결과 없음 추천과 사용자가 직접 누른 검색 버튼이 같은 흐름을 사용하도록 분리했습니다.
   *
   * @author HanWon.Jang
   * @param keyword 검색할 책 제목 또는 작가 이름
   * @return
   */
  const executeBookSearch = async (keyword: string) => {

    try {
      if (keyword === "") {
        await sweetWarning(
          message("frontend.alert.inputRequired"),
          message("frontend.book.search.keywordRequired"),
        );
        return;
      }

      setIsSearching(true);
      setHasMore(false);

      const responseData = await fetchBooks(keyword, 1);

      if (!responseData) {
        return;
      }

      const next = 1 + SEARCH_PAGE_SIZE;
      const more = responseData.length === SEARCH_PAGE_SIZE;

      setBookResult(responseData);
      setNextStart(next);
      setHasMore(more);
      saveSearchCache(keyword, responseData, next, more);
    } catch (error) {
      console.error("도서 검색 중 오류 발생: ", error);
      // "검색에 실패했습니다."
      // "책 검색에 실패했습니다. 다시 시도해주세요."
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
    } finally {
      setIsSearching(false);
    }
  };

  useEffect(() => {

    const state = (location.state ?? {}) as SearchBookPageState;
    const initialSearchKeyword = state.initialSearchKeyword?.trim() ?? "";

    // 홈 독후감 검색 결과가 없어서 넘어온 경우에는 이전 도서 검색 캐시보다 전달받은 검색어가 우선입니다.
    // 입력창을 먼저 채운 뒤 같은 검색어로 즉시 조회해 사용자가 다시 검색 버튼을 누르지 않게 합니다.
    if (initialSearchKeyword.length > 0) {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      setSearchKeyword(initialSearchKeyword);
      void executeBookSearch(initialSearchKeyword);
      return;
    }

    const shouldRestoreSearch =
      navigationType === "POP" || state.keepSearchResult === true;

    // 새로 도서 검색 화면에 진입한 경우에는 이전 검색 결과가 남지 않도록 세션 캐시를 비웁니다.
    // 뒤로가기나 책 정보 화면에서 돌아온 경우에만 사용자가 보던 검색 결과를 복구합니다.
    if (!shouldRestoreSearch) {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      return;
    }

    const cached = sessionStorage.getItem(SEARCH_STORAGE_KEY);

    if (!cached) {
      return;
    }

    try {
      const parsed = JSON.parse(cached) as {
        searchKeyword?: string;
        bookResult?: BookSearchResultType[];
        nextStart?: number;
        hasMore?: boolean;
      };

      setSearchKeyword(parsed.searchKeyword ?? "");
      setBookResult(parsed.bookResult ?? null);
      setNextStart(parsed.nextStart ?? 1);
      setHasMore(parsed.hasMore ?? false);
    } catch {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
    }
  }, [location.key, location.state, navigationType]);

  /**
   * handle Search Click 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param e e 입력값
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleSearchClick = async (e?: FormEvent<HTMLFormElement>) => {

    e?.preventDefault();
    await executeBookSearch(searchKeyword.trim());
  };

  /**
   * handle Load More 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleLoadMore = async () => {

    const keyword = searchKeyword.trim();

    if (!keyword || isLoadingMore) {
      return;
    }

    try {
      setIsLoadingMore(true);
      const responseData = await fetchBooks(keyword, nextStart);

      if (!responseData) {
        return;
      }

      if (responseData.length === 0) {
        setHasMore(false);
        saveSearchCache(keyword, bookResult ?? [], nextStart, false);
        return;
      }

      const mergedResult = [...(bookResult ?? []), ...responseData];
      const next = nextStart + SEARCH_PAGE_SIZE;
      const more = responseData.length === SEARCH_PAGE_SIZE;

      setBookResult(mergedResult);
      setNextStart(next);
      setHasMore(more);
      saveSearchCache(keyword, mergedResult, next, more);
    } catch (error) {
      console.error("도서 검색 결과 추가 조회 중 오류 발생: ", error);
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
    } finally {
      setIsLoadingMore(false);
    }
  };

  /**
   * handle Select Book 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param book book 입력값
   * @return 기존 독후감 확인과 화면 이동이 끝난 Promise
   */
  const handleSelectBook = async (book: BookSearchResultType): Promise<void> => {
    // 다른 도서의 기존 독후감 확인이 진행 중이면 중복 요청을 차단한다
    if (selectingBookIsbn !== null) {
      // 진행 중인 도서 선택 요청을 유지한다
      return;
    }

    // 선택 버튼의 중복 요청을 막기 위해 현재 ISBN을 진행 상태로 설정한다
    setSelectingBookIsbn(book.isbn);
    // 선택 흐름이 예외로 끝나도 버튼 진행 상태를 복원한다
    try {
      // 기존 독후감 수정과 추가 작성 선택 흐름으로 이동한다
      await moveToReportEntry(book, navigate);
    }

    finally {
      // 선택 안내가 끝난 뒤 다른 도서를 선택할 수 있도록 진행 상태를 해제한다
      setSelectingBookIsbn(null);
    }
  };

  /**
   * handle More Info 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param book book 입력값
   * @return 반환값이 없다
   */
  const handleMoreInfo = (book: BookSearchResultType) => {

    navigate("/book/search/info", {
      state: { book },
    });
  };

  return (
    /* 외부 도서 검색과 결과 목록 전체 영역 */
    <main className={styles.page}>
      <Container className={styles.content}>
        {/* 도서 검색어 입력과 검색 실행 영역 */}
        <form className={styles.searchBar} onSubmit={handleSearchClick}>
          <label className={styles.searchLabel}>
            <span className={styles.hiddenLabel}>
              {/* "책 제목, 작가를 입력하세요" */}
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

                return (
                  /* 검색된 도서 개별 항목 영역 */
                  <article
                    className={styles.resultCard}
                    key={`${book.isbn}-${index}`}
                  >
                    {/* 검색된 도서의 표지 영역 */}
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

                    {/* 검색된 도서의 제목과 저자 및 출판사 영역 */}
                    <div className={styles.bookMeta}>
                      <h2 className={styles.bookTitle}>{title}</h2>
                      <p className={styles.meta}>
                        {author} / {publisher}
                      </p>
                    </div>

                    {/* 검색된 도서의 소개 영역 */}
                    <p className={styles.description}>
                      {preview || message("frontend.common.noBookDescription")}
                    </p>

                    {/* 검색된 도서의 상세보기와 선택 버튼 영역 */}
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
