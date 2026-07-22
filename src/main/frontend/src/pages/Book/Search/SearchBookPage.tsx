import { message } from "@/app/messages/message";
import {
  assertResultDataSuccess,
  getApiErrorMessage,
} from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { FormEvent, useEffect, useState } from "react";
import api from "../../../app/api/axios";
import { NaverApiResultType } from "@/features/Book/types/book.type";
import { useLocation, useNavigate, useNavigationType } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import { stripHtmlTags } from "@/app/utils/htmlUtil";
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
 * @author Hanwon.Jang
 * @return 책 검색 페이지 컴포넌트
 */
const SearchBookPage = () => {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [bookResult, setBookResult] = useState<NaverApiResultType[] | null>(
    null,
  );
  const [nextStart, setNextStart] = useState(1);
  const [hasMore, setHasMore] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const navigationType = useNavigationType();

  const fetchBooks = async (keyword: string, start: number) => {
    const response = await api.get(
      `/book/search?query=${encodeURIComponent(keyword)}&start=${start}`,
    );

    return (assertResultDataSuccess(response.data).data ?? []) as NaverApiResultType[];
  };

  const saveSearchCache = (
    keyword: string,
    result: NaverApiResultType[],
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
   * @author Hanwon.Jang
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
      // 화면표시: 검색에 실패했습니다.
      // 화면표시: 책 검색에 실패했습니다. 다시 시도해주세요.
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
        bookResult?: NaverApiResultType[];
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

  const handleSearchClick = async (e?: FormEvent<HTMLFormElement>) => {
    e?.preventDefault();
    await executeBookSearch(searchKeyword.trim());
  };

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

  const handleSelectBook = (book: NaverApiResultType) => {
    navigate("/set", {
      state: { selectedBook: book },
    });
  };

  const handleMoreInfo = (book: NaverApiResultType) => {
    navigate("/book/search/info", {
      state: { book },
    });
  };

  return (
    <main className={styles.page}>
      <Container className={styles.content}>
        <form className={styles.searchForm} onSubmit={handleSearchClick}>
          <input
            className={styles.searchInput}
            type="text"
            name="searchKeyword"
            id="searchKeyword"
            placeholder={message("frontend.book.search.placeholder")}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
          />
          <button
            className={styles.searchButton}
            type="submit"
            disabled={isSearching}
          >
            {message("frontend.book.search.button")}
          </button>
        </form>

        {bookResult &&
          (bookResult.length > 0 ? (
            <div className={styles.resultList}>
              {bookResult.map((book, index) => {
                const title = stripHtmlTags(book.title);
                const author = stripHtmlTags(book.author);
                const publisher = stripHtmlTags(book.publisher);
                const description = stripHtmlTags(book.description);
                const preview =
                  description.length > DESCRIPTION_PREVIEW_LENGTH
                    ? `${description.slice(0, DESCRIPTION_PREVIEW_LENGTH)}...`
                    : description;

                return (
                  <article
                    className={styles.resultCard}
                    key={`${book.isbn}-${index}`}
                  >
                    <div className={styles.coverArea}>
                      <div className={styles.coverFrame}>
                        <img
                          className={styles.coverImage}
                          src={book.image}
                          alt={message("frontend.book.search.coverAlt", [
                            title,
                          ])}
                        />
                      </div>
                    </div>
                    <div>
                      <h2 className={styles.bookTitle}>{title}</h2>
                      <p className={styles.meta}>
                        {author} / {publisher}
                      </p>
                    </div>
                    <p className={styles.description}>
                      {preview || message("frontend.common.noBookDescription")}
                    </p>
                    <div className={styles.actions}>
                      <button
                        className={styles.actionButton}
                        type="button"
                        onClick={() => handleMoreInfo(book)}
                      >
                        {message("frontend.book.search.more")}
                      </button>
                      <button
                        className={styles.primaryButton}
                        type="button"
                        onClick={() => handleSelectBook(book)}
                      >
                        {message("frontend.book.search.select")}
                      </button>
                    </div>
                  </article>
                );
              })}
              {hasMore && (
                <button
                  className={styles.loadMoreButton}
                  type="button"
                  onClick={handleLoadMore}
                  disabled={isLoadingMore}
                >
                  {isLoadingMore
                    ? message("frontend.book.search.loadingMore")
                    : message("frontend.book.search.loadMore")}
                </button>
              )}
            </div>
          ) : (
            <p className={styles.emptyMessage}>
              {message("frontend.book.search.noResult")}
            </p>
          ))}
      </Container>
    </main>
  );
};

export default SearchBookPage;
