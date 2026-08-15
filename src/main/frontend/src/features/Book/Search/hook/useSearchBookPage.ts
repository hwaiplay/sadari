import api from "@/app/api/axios";
import {
  assertResultDataSuccess,
  getApiErrorMessage,
} from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import type {
  BookSearchPageType,
  BookSearchResultType,
  PopularBookPeriodType,
} from "@/features/Book/types/book.type";
import { moveToReportEntry } from "@/features/Book/utils/reportEntry";
import type { FormEvent } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  useLocation,
  useNavigate,
  useNavigationType,
  useParams,
} from "react-router-dom";

const SEARCH_STORAGE_KEY = "sadari:book-search:v2";
const SEARCH_PAGE_SIZE = 10;

type SearchBookPageState = {
  initialSearchKeyword?: string;
  keepSearchResult?: boolean;
};

type SearchBookCache = {
  searchKeyword?: string;
  bookResult?: BookSearchResultType[];
  visibleCount?: number;
  nextStart?: number | null;
  end?: boolean;
};

/**
 * 입력한 검색어와 시작 위치를 사용해 책 검색 결과를 조회한다.
 *
 * @author HanWon.Jang
 * @param keyword 검색할 책 제목 또는 저자명
 * @param start 검색을 시작할 결과 위치
 * @return 최대 50권의 검색 결과와 다음 카카오 페이지 상태
 * @throws 책 검색 API 요청 또는 공통 응답 검증에 실패하면 발생한다
 */
async function fetchBooks(
  keyword: string,
  start: number,
): Promise<BookSearchPageType> {

  // 검색어와 조회 시작 위치를 책 검색 API에 전달한다.
  const response = await api.get(
    `/book/search?query=${encodeURIComponent(keyword)}&start=${start}`,
  );

  // 공통 응답 검증을 통과한 50권 검색 페이지를 반환한다.
  return assertResultDataSuccess(response.data).data as BookSearchPageType;
}

/**
 * 선택 기간의 독후감 고유 작성자 수 기준 인기 도서를 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param period 주간과 월간 및 연간 중 조회할 인기 도서 기간
 * @return 순위와 독후감 작성자 수 및 평균 평점을 포함한 인기 도서 최대 10권
 * @throws 인기 도서 API 요청 또는 공통 응답 검증에 실패하면 발생한다
 */
async function fetchPopularBooks(
  period: PopularBookPeriodType,
): Promise<BookSearchResultType[]> {

  // 로그인 회원의 검색 화면에 표시할 선택 기간의 인기 도서를 요청한다.
  const response = await api.get(
    `/book/popular?period=${encodeURIComponent(period)}`,
  );

  // 공통 응답 검증을 통과한 선택 기간의 인기 도서 목록을 반환한다.
  return (assertResultDataSuccess(response.data).data ?? []) as BookSearchResultType[];
}

/**
 * 책 검색 화면 복원에 필요한 상태를 세션 저장소에 보관한다.
 *
 * @author HanWon.Jang
 * @param cache 마지막 검색어와 50권 결과 및 화면 노출 위치
 * @return 반환값이 없다
 */
function saveSearchCache(cache: SearchBookCache): void {

  // 상세 화면에서 돌아올 때 검색 결과를 복원할 수 있도록 저장한다.
  sessionStorage.setItem(SEARCH_STORAGE_KEY, JSON.stringify(cache));
}

/**
 * 책 검색 화면의 조회, 캐시, 더보기와 선택 이동 상태를 관리한다.
 *
 * @author HanWon.Jang
 * @return 책 검색 화면에서 사용하는 상태와 이벤트 처리 함수
 */
export function useSearchBookPage() {

  const [searchKeyword, setSearchKeyword] = useState("");
  const [bookResult, setBookResult] = useState<BookSearchResultType[] | null>(
    null,
  );
  const [visibleCount, setVisibleCount] = useState(SEARCH_PAGE_SIZE);
  const [nextStart, setNextStart] = useState<number | null>(null);
  const [isEnd, setIsEnd] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isPopularMode, setIsPopularMode] = useState(true);
  const [popularPeriod, setPopularPeriod] =
    useState<PopularBookPeriodType>("monthly");
  const [selectingBookIsbn, setSelectingBookIsbn] = useState<string | null>(null);
  const popularLoadingPeriodRef = useRef<PopularBookPeriodType | null>(null);
  const resultRequestIdRef = useRef(0);
  const navigate = useNavigate();
  const location = useLocation();
  const navigationType = useNavigationType();
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const isClubBookSearch = clubNumbParam !== undefined;
  const clubNumb = Number(clubNumbParam);
  const hasValidClubNumb = Number.isSafeInteger(clubNumb) && clubNumb > 0;

  /**
   * 검색 화면 최초 진입과 기간 변경에 표시할 인기 도서를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param period 주간과 월간 및 연간 중 조회할 인기 도서 기간
   * @return 인기 도서 조회가 끝나면 완료되는 Promise
   */
  const loadPopularBooks = useCallback(async (
    period: PopularBookPeriodType,
  ): Promise<void> => {

    // React 개발 모드에서 같은 기간의 인기 도서 요청이 겹치면 기존 요청의 화면 반영을 기다린다.
    if (popularLoadingPeriodRef.current === period) {
      // 이미 실행 중인 같은 기간의 인기 도서 조회를 유지하고 중복 API 호출을 종료한다.
      return;
    }

    // 완료 전까지 동일 기간의 요청은 막고 다른 기간 선택은 최신 요청으로 허용하도록 진행 상태를 기록한다.
    popularLoadingPeriodRef.current = period;
    // 인기 도서 응답보다 늦게 시작한 직접 검색이 우선하도록 요청 순번을 발급한다.
    const requestId = ++resultRequestIdRef.current;

    try {
      // 페이지 최초 인기 도서 조회 상태를 화면에 반영한다.
      setIsInitialLoading(true);
      // 기간 선택 목록과 인기 도서 카드가 표시되는 초기 화면 모드를 유지한다.
      setIsPopularMode(true);
      // 선택 기간의 독후감 작성자 수 기준 상위 도서를 조회한다.
      const popularBookList = await fetchPopularBooks(period);

      // 인기 도서 조회 뒤 직접 검색이 시작됐으면 오래된 인기 응답으로 목록을 덮어쓰지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 직접 검색이 화면을 갱신하도록 이전 인기 응답 처리를 종료한다.
        return;
      }

      // 인기 도서 최대 10권을 검색 결과와 같은 목록 영역에 설정한다.
      setBookResult(popularBookList);
      // 인기 도서는 최대 10권 전체를 한 번에 노출한다.
      setVisibleCount(popularBookList.length);
      // 인기 도서에는 다음 카카오 검색 위치가 없음을 설정한다.
      setNextStart(null);
      // 인기 도서 목록은 추가 페이지가 없는 종료 상태로 설정한다.
      setIsEnd(true);
      // 기간 선택과 순위 및 작성자 수가 표시되도록 초기 화면 모드를 설정한다.
      setIsPopularMode(true);
    } catch (error) {
      // 인기 도서 조회 뒤 직접 검색이 시작됐으면 이전 요청의 오류를 사용자에게 표시하지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 직접 검색 흐름을 유지하고 오래된 인기 조회 오류 처리를 종료한다.
        return;
      }

      console.error("기간별 인기 도서 조회 중 오류 발생: ", error);
      // "책 검색에 실패했습니다. 다시 시도해주세요."
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
      // 조회 실패 뒤 검색 결과 없음 상태를 화면에 표시한다.
      setBookResult([]);
    } finally {
      // 현재 요청이 마지막으로 시작한 기간 조회이면 다음 선택을 받을 수 있도록 진행 상태를 해제한다.
      if (popularLoadingPeriodRef.current === period) {
        popularLoadingPeriodRef.current = null;
      }
      // 최신 요청인 경우에만 페이지 최초 조회 상태를 해제한다.
      if (requestId === resultRequestIdRef.current) {
        // 성공과 실패에 관계없이 최신 인기 도서 최초 조회 상태를 해제한다.
        setIsInitialLoading(false);
      }
    }
  }, []);

  /**
   * 주간과 월간 및 연간 인기 도서 선택을 화면과 조회 결과에 반영한다.
   *
   * @author SeungHyeon.Kang
   * @param period 사용자가 선택한 인기 도서 집계 기간
   * @return 반환값이 없다
   */
  function handlePopularPeriodChange(period: PopularBookPeriodType): void {

    // 선택한 기간을 표시하고 해당 기간의 인기 도서 목록을 새로 조회한다.
    setPopularPeriod(period);
    void loadPopularBooks(period);
  }

  /**
   * 검색어로 첫 페이지를 조회하고 화면과 세션 캐시를 갱신한다.
   *
   * @author HanWon.Jang
   * @param keyword 검색할 책 제목 또는 저자명
   * @return 검색 처리가 끝나면 완료되는 Promise
   */
  const executeBookSearch = useCallback(async (keyword: string): Promise<void> => {

    try {
      // 검색어가 없으면 필수 입력 안내를 표시한다.
      if (keyword === "") {
        // 검색어 입력이 필요하다는 공통 경고를 표시한다.
        await sweetWarning(
          message("frontend.alert.inputRequired"),
          message("frontend.book.search.keywordRequired"),
        );
        // 빈 검색어 조회를 종료한다.
        return;
      }

      // 직접 검색이 진행되면 이전 인기 도서 응답이 목록을 덮어쓰지 못하도록 요청 순번을 갱신한다.
      const requestId = ++resultRequestIdRef.current;

      // 첫 페이지 조회 중임을 화면에 반영한다.
      setIsSearching(true);
      // 사용자가 직접 검색하면 인기 도서 최초 로딩 화면을 종료한다.
      setIsInitialLoading(false);
      // 새 조회가 끝나기 전에는 이전 추가 노출 상태를 초기화한다.
      setIsEnd(true);

      // 카카오 API에서 검색 결과의 첫 50권 페이지를 조회한다.
      const responseData = await fetchBooks(keyword, 1);

      // 더 늦게 시작한 검색이 있으면 오래된 검색 응답으로 목록을 덮어쓰지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 검색 요청이 화면을 갱신하도록 이전 응답 처리를 종료한다.
        return;
      }
      const firstVisibleCount = Math.min(
        SEARCH_PAGE_SIZE,
        responseData.bookList.length,
      );

      // 조회된 최대 50권 중 첫 10권과 다음 카카오 페이지 상태를 화면에 반영한다.
      setBookResult(responseData.bookList);
      setVisibleCount(firstVisibleCount);
      setNextStart(responseData.nextStart ?? null);
      setIsEnd(responseData.end);
      // 직접 검색 결과에서는 인기 안내와 순위 정보를 숨긴다.
      setIsPopularMode(false);
      // 상세 화면 복귀를 위해 최신 검색 상태를 저장한다.
      saveSearchCache({
        searchKeyword: keyword,
        bookResult: responseData.bookList,
        visibleCount: firstVisibleCount,
        nextStart: responseData.nextStart ?? null,
        end: responseData.end,
      });
    } catch (error) {
      console.error("도서 검색 중 오류 발생: ", error);
      // 검색 실패 원인을 공통 오류 알림으로 표시한다.
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
    } finally {
      // 성공과 실패에 관계없이 첫 페이지 조회 상태를 해제한다.
      setIsSearching(false);
    }
  }, []);

  useEffect(() => {

    const state = (location.state ?? {}) as SearchBookPageState;
    const initialSearchKeyword = state.initialSearchKeyword?.trim() ?? "";

    // 외부에서 검색어를 전달받으면 이전 캐시 대신 해당 검색어를 즉시 조회한다.
    if (initialSearchKeyword.length > 0) {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      setSearchKeyword(initialSearchKeyword);
      // 전달받은 검색어 조회는 인기 도서 최초 로딩과 구분한다.
      setIsInitialLoading(false);
      void executeBookSearch(initialSearchKeyword);
      // 전달받은 검색어 처리 뒤 캐시 복원 흐름을 종료한다.
      return;
    }

    const shouldRestoreSearch =
      navigationType === "POP" || state.keepSearchResult === true;

    // 새로 검색 화면에 진입하면 이전 검색 결과를 남기지 않는다.
    if (!shouldRestoreSearch) {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      // 신규 진입의 빈 화면 대신 기본 월간 인기 도서를 조회한다.
      void loadPopularBooks("monthly");
      // 신규 진입의 인기 도서 조회를 시작하고 캐시 복원 처리를 종료한다.
      return;
    }

    const cached = sessionStorage.getItem(SEARCH_STORAGE_KEY);

    // 복원할 검색 상태가 없으면 현재 빈 화면을 유지한다.
    if (!cached) {
      // 저장된 직접 검색이 없으면 기본 월간 인기 도서를 다시 조회한다.
      void loadPopularBooks("monthly");
      // 인기 도서 조회를 시작하고 빈 캐시 복원 처리를 종료한다.
      return;
    }

    try {
      const parsed = JSON.parse(cached) as SearchBookCache;

      // 저장된 검색어와 결과 페이지 상태를 화면에 복원한다.
      setSearchKeyword(parsed.searchKeyword ?? "");
      setBookResult(parsed.bookResult ?? null);
      setVisibleCount(parsed.visibleCount ?? SEARCH_PAGE_SIZE);
      setNextStart(parsed.nextStart ?? null);
      setIsEnd(parsed.end ?? true);
      // 복원한 목록은 사용자가 직접 조회한 검색 결과로 표시한다.
      setIsPopularMode(false);
      // 세션 검색 결과 복원이 끝났으므로 페이지 최초 로딩 상태를 해제한다.
      setIsInitialLoading(false);
    } catch {
      // 손상된 검색 캐시는 다음 진입에 재사용되지 않도록 제거한다.
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      // 손상된 직접 검색 캐시 대신 기본 월간 인기 도서를 조회한다.
      void loadPopularBooks("monthly");
    }
  }, [executeBookSearch, loadPopularBooks, location.key, location.state, navigationType]);

  /**
   * 검색 폼 제출을 막고 현재 입력된 검색어를 조회한다.
   *
   * @author HanWon.Jang
   * @param event 검색 폼 제출 이벤트
   * @return 검색 처리가 끝나면 완료되는 Promise
   */
  async function handleSearchClick(
    event?: FormEvent<HTMLFormElement>,
  ): Promise<void> {

    // 브라우저의 기본 폼 제출 동작을 중지한다.
    event?.preventDefault();
    // 앞뒤 공백을 제거한 검색어로 첫 페이지를 조회한다.
    await executeBookSearch(searchKeyword.trim());
  }

  /**
   * 현재 검색 결과 다음 페이지를 조회해 기존 목록 뒤에 추가한다.
   *
   * @author HanWon.Jang
   * @return 추가 조회 처리가 끝나면 완료되는 Promise
   */
  async function handleLoadMore(): Promise<void> {

    const keyword = searchKeyword.trim();

    // 검색어가 없거나 이미 추가 노출 중이면 중복 요청을 차단한다.
    if (!keyword || isLoadingMore) {
      // 실행할 수 없는 추가 조회를 종료한다.
      return;
    }

    try {
      // 추가 조회 중임을 화면에 반영한다.
      setIsLoadingMore(true);

      // 이미 받아둔 50권 안에 미노출 도서가 있으면 외부 호출 없이 다음 10권만 표시한다.
      if (visibleCount < (bookResult?.length ?? 0)) {
        const nextVisibleCount = Math.min(
          visibleCount + SEARCH_PAGE_SIZE,
          bookResult?.length ?? 0,
        );
        // 공용 50권 결과에서 다음 10권까지 화면 노출 범위를 확장한다.
        setVisibleCount(nextVisibleCount);
        // 상세 화면 복귀 시 같은 노출 위치를 유지하도록 검색 상태를 저장한다.
        saveSearchCache({
          searchKeyword: keyword,
          bookResult: bookResult ?? [],
          visibleCount: nextVisibleCount,
          nextStart,
          end: isEnd,
        });
        // 카카오 API를 호출하지 않은 추가 노출 처리를 종료한다.
        return;
      }

      // 마지막 페이지이거나 다음 위치가 없으면 불필요한 빈 카카오 호출을 차단한다.
      if (isEnd || nextStart === null) {
        // 추가 검색할 카카오 페이지가 없는 처리를 종료한다.
        return;
      }

      // 받아둔 50권을 모두 소진한 경우에만 다음 카카오 검색 페이지를 호출한다.
      const responseData = await fetchBooks(keyword, nextStart);
      const mergedResult = [...(bookResult ?? []), ...responseData.bookList];
      const nextVisibleCount = Math.min(
        visibleCount + SEARCH_PAGE_SIZE,
        mergedResult.length,
      );

      // 기존 목록 뒤에 새 50권 페이지를 추가하고 화면에는 다음 10권까지만 노출한다.
      setBookResult(mergedResult);
      setVisibleCount(nextVisibleCount);
      setNextStart(responseData.nextStart ?? null);
      setIsEnd(responseData.end);
      // 합쳐진 결과와 실제 카카오 페이지 상태를 세션 캐시에 저장한다.
      saveSearchCache({
        searchKeyword: keyword,
        bookResult: mergedResult,
        visibleCount: nextVisibleCount,
        nextStart: responseData.nextStart ?? null,
        end: responseData.end,
      });
    } catch (error) {
      console.error("도서 검색 결과 추가 조회 중 오류 발생: ", error);
      // 추가 조회 실패 원인을 공통 오류 알림으로 표시한다.
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
    } finally {
      // 성공과 실패에 관계없이 추가 조회 상태를 해제한다.
      setIsLoadingMore(false);
    }
  }

  // 서버에서 미리 받은 결과 중 현재 화면에 노출할 범위만 잘라낸다.
  const visibleBookResult = bookResult?.slice(0, visibleCount) ?? null;
  const hasMore =
    visibleCount < (bookResult?.length ?? 0) || (!isEnd && nextStart !== null);

  /**
   * 선택한 책을 개인 독후감 또는 모임 독서 등록 흐름으로 전달한다.
   *
   * @author HanWon.Jang
   * @param book 선택한 책 정보
   * @return 책 선택 이동이 끝나면 완료되는 Promise
   */
  async function handleSelectBook(book: BookSearchResultType): Promise<void> {

    // 다른 책의 개인 독후감 확인이 진행 중이면 중복 선택을 차단한다.
    if (selectingBookIsbn !== null) {
      // 진행 중인 책 선택 요청을 유지하고 새 요청을 종료한다.
      return;
    }

    // 모임 독서용 검색이면 선택한 책을 예정된 독서 등록 URL로 전달한다.
    if (isClubBookSearch) {
      // 올바르지 않은 모임 번호로는 독서 등록 URL을 만들지 않는다.
      if (!hasValidClubNumb) {
        // 모임 목록으로 돌아가 다시 진입하도록 한다.
        navigate("/reading-clubs/mine", { replace: true });
        // 잘못된 모임 번호의 책 선택을 종료한다.
        return;
      }

      // 독서 등록 화면 구현 전까지 책 정보와 ISBN을 예정된 URL로 전달한다.
      navigate(
        `/reading-clubs/${clubNumb}/readings/set?isbn=${encodeURIComponent(book.isbn)}`,
        { state: { book } },
      );
      // 모임 이동 뒤 개인 독후감 선택 흐름을 실행하지 않는다.
      return;
    }

    // 개인 책 선택의 중복 요청을 막기 위해 현재 ISBN을 저장한다.
    setSelectingBookIsbn(book.isbn);

    try {
      // 기존 독후감 확인 결과에 따라 수정 또는 새 작성 화면으로 이동한다.
      await moveToReportEntry(book, navigate);
    } finally {
      // 선택 흐름이 끝나면 다른 책을 선택할 수 있도록 진행 상태를 해제한다.
      setSelectingBookIsbn(null);
    }
  }

  /**
   * 선택한 책 상세 화면을 현재 개인 또는 모임 검색 흐름에 맞게 연다.
   *
   * @author HanWon.Jang
   * @param book 상세 조회할 책 정보
   * @return 반환값이 없다
   */
  function handleMoreInfo(book: BookSearchResultType): void {

    // 모임 검색에서는 상세 화면에서도 동일한 모임 선택 흐름을 유지한다.
    if (isClubBookSearch) {
      // 올바르지 않은 모임 번호이면 모임 목록으로 이동한다.
      if (!hasValidClubNumb) {
        // 모임 목록으로 돌아가 다시 진입하도록 한다.
        navigate("/reading-clubs/mine", { replace: true });
        // 잘못된 모임 번호의 상세 이동을 종료한다.
        return;
      }

      // 선택한 책 정보를 모임 책 상세 화면으로 전달한다.
      navigate(`/reading-clubs/${clubNumb}/books/search/info`, {
        state: { book },
      });
      // 모임 책 상세 이동 뒤 개인 상세 흐름을 실행하지 않는다.
      return;
    }

    // 선택한 책 정보를 개인 책 상세 화면으로 전달한다.
    navigate("/book/search/info", {
      state: { book },
    });
  }

  // 책 검색 화면이 렌더링과 이벤트 연결에 사용할 값을 반환한다.
  return {
    bookResult: visibleBookResult,
    handleLoadMore,
    handleMoreInfo,
    handlePopularPeriodChange,
    handleSearchClick,
    handleSelectBook,
    hasMore,
    isInitialLoading,
    isLoadingMore,
    isPopularMode,
    isSearching,
    popularPeriod,
    searchKeyword,
    selectingBookIsbn,
    setSearchKeyword,
  };
}
