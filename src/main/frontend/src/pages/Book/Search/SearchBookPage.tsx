import { message } from "@/app/messages/message";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { FormEvent, useEffect, useState } from "react";
import api from "../../../app/api/axios";
import { NaverApiResultType } from "@/features/Book/types/book.type";
import { useLocation, useNavigate, useNavigationType } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import * as styles from "./SearchBookPage.css";

const SEARCH_STORAGE_KEY = "sadari:book-search";
const DESCRIPTION_PREVIEW_LENGTH = 90;
const SEARCH_PAGE_SIZE = 10;

function stripTags(value?: string) {
  return value?.replace(/<[^>]*>/g, "") ?? "";
}

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

  useEffect(() => {
    const shouldRestoreSearch =
      navigationType === "POP" || location.state?.keepSearchResult === true;

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
  }, [location.state, navigationType]);

  const fetchBooks = async (keyword: string, start: number) => {
    const response = await api.get(
      `/book/search?query=${encodeURIComponent(keyword)}&start=${start}`,
    );

    if (response.data.code !== 200) {
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        message("frontend.book.search.failed"),
      );
      return null;
    }

    return (response.data.data ?? []) as NaverApiResultType[];
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

  const handleSearchClick = async (e?: FormEvent<HTMLFormElement>) => {
    e?.preventDefault();
    const keyword = searchKeyword.trim();

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
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        message("frontend.book.search.failed"),
      );
    } finally {
      setIsSearching(false);
    }
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
        message("frontend.book.search.failed"),
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
                const title = stripTags(book.title);
                const author = stripTags(book.author);
                const publisher = stripTags(book.publisher);
                const description = stripTags(book.description);
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
