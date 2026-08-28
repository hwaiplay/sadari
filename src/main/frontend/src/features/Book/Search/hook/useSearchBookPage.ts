import api from "@/app/api/axios";
import {
  assertResultDataSuccess,
  getApiErrorMessage,
} from "@/app/api/resultData";
import {
  sweetError,
  sweetWarning,
} from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { queryClient } from "@/app/query/queryClient";
import { setReportApi } from "@/features/Book/api/bookApi";
import { REPORT_STATUS_READ } from "@/features/Book/constants/reportForm";
import {
  getTimerReturnPath,
  READING_TIMER_SEARCH_SOURCE,
  type SearchBookPageState,
} from "@/features/Book/Search/lib/bookSearchNavigation";
import type {
  BookSearchPageType,
  BookSearchResultType,
  PopularBookPeriodType,
  PopularSearchKeywordType,
  ReportDtoType,
} from "@/features/Book/types/book.type";
import { moveToReportEntry } from "@/features/Book/utils/reportEntry";
import { getTimerSummaryOptions } from "@/features/Timer/hooks/useTimerSummaryQuery";
import {
  normalizeBookAuthor,
  sanitizeText,
  stripHtmlTags,
} from "@/features/Book/utils/reportValidation";
import type { ChangeEvent, FormEvent } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  useLocation,
  useNavigate,
  useNavigationType,
  useParams,
} from "react-router-dom";

const SEARCH_STORAGE_KEY = "sadari:book-search:v2";
const SEARCH_PAGE_SIZE = 10;
const AUTO_SEARCH_MIN_LENGTH = 2;
const AUTO_SEARCH_DELAY_MS = 100;

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
 * 최근 고유 회원 검색 수를 기준으로 안전한 도서 인기 검색어를 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 순위와 정규화된 검색어를 포함한 인기 검색어 최대 10건
 * @throws 인기 검색어 API 요청 또는 공통 응답 검증에 실패하면 발생한다
 */
async function getPopularKeywords(): Promise<PopularSearchKeywordType[]> {

  // 검색 화면의 한 줄 세로 슬라이더에 표시할 최근 인기 검색어를 요청한다
  const response = await api.get("/book/popular-search-keywords");

  // 공통 응답 검증을 통과한 안전한 인기 검색어 목록을 반환한다
  return (assertResultDataSuccess(response.data).data ?? []) as PopularSearchKeywordType[];
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
  const [popularKeywordList, setPopularKeywordList] = useState<
    PopularSearchKeywordType[]
  >([]);
  const [selectingBookIsbn, setSelectingBookIsbn] = useState<string | null>(null);
  const popularLoadingPeriodRef = useRef<PopularBookPeriodType | null>(null);
  const resultRequestIdRef = useRef(0);
  const autoSearchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastSearchedKeywordRef = useRef("");
  const previousKeywordRef = useRef("");
  const navigate = useNavigate();
  const location = useLocation();
  const navigationType = useNavigationType();
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const isClubBookSearch = clubNumbParam !== undefined;
  const clubNumb = Number(clubNumbParam);
  const hasValidClubNumb = Number.isSafeInteger(clubNumb) && clubNumb > 0;
  const pageState = (location.state ?? {}) as SearchBookPageState;
  const isTimerBookSearch =
    !isClubBookSearch &&
    pageState.entrySource === READING_TIMER_SEARCH_SOURCE;
  const [timerPeriodBook, setTimerPeriodBook] =
    useState<BookSearchResultType | null>(pageState.timerBook ?? null);
  const [isTimerReportSaving, setIsTimerReportSaving] = useState(false);

  /**
   * 검색 화면의 부가 영역에 표시할 안전한 인기 검색어를 독립적으로 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 인기 검색어 조회가 끝나면 완료되는 Promise
   */
  const loadPopularKeywords = useCallback(async (): Promise<void> => {

    // 인기 검색어 장애는 도서 검색과 인기 도서 목록을 막지 않도록 별도 실패 경로로 처리한다
    try {
      // 최근 고유 회원 검색 수 기준의 안전한 인기 검색어를 조회한다
      const popularKeywords = await getPopularKeywords();
      // 조회된 인기 검색어를 세로 슬라이더 표시 순서로 설정한다
      setPopularKeywordList(popularKeywords);
    }

    // 인기 검색어 부가 조회 실패는 사용자 오류 알림 없이 영역만 숨긴다
    catch {
      // 도서 검색과 인기 도서 목록을 유지하고 인기 검색어만 빈 상태로 전환한다
      setPopularKeywordList([]);
    }
  }, []);

  /**
   * 검색 화면 진입 시 인기 검색어 부가 조회를 시작한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const startPopularKeywordLoad = useCallback((): void => {
    // 인기 도서 최초 조회와 독립적으로 인기 검색어를 불러온다
    void loadPopularKeywords();
  }, [loadPopularKeywords]);

  // 화면 진입 시 한 번 인기 검색어 부가 데이터를 조회한다
  useEffect(startPopularKeywordLoad, [startPopularKeywordLoad]);

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

    // 같은 검색어의 자동 검색과 수동 제출이 겹치면 기존 결과를 유지한다.
    if (lastSearchedKeywordRef.current === keyword) {
      // 이미 조회했거나 조회 중인 검색어의 중복 API 호출을 종료한다.
      return;
    }

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

    // 직접 검색이 진행되면 이전 목록 응답이 결과를 덮어쓰지 못하도록 요청 순번을 갱신한다.
    const requestId = ++resultRequestIdRef.current;
    // 응답 대기 중인 검색어도 중복 요청 대상에서 제외한다.
    lastSearchedKeywordRef.current = keyword;

    try {
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
      // 더 늦게 시작한 검색이 있으면 이전 요청의 오류를 사용자에게 표시하지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 검색 흐름을 유지하고 오래된 요청의 오류 처리를 종료한다.
        return;
      }

      // 실패한 검색어는 검색 버튼이나 다음 입력에서 다시 시도할 수 있도록 해제한다.
      lastSearchedKeywordRef.current = "";
      console.error("도서 검색 중 오류 발생: ", error);
      // 검색 실패 원인을 공통 오류 알림으로 표시한다.
      await sweetError(
        message("frontend.alert.searchFailedTitle"),
        getApiErrorMessage(error, message("frontend.book.search.failed")),
      );
    } finally {
      // 최신 요청인 경우에만 첫 페이지 조회 상태를 해제한다.
      if (requestId === resultRequestIdRef.current) {
        // 성공과 실패에 관계없이 최신 검색의 진행 상태를 해제한다.
        setIsSearching(false);
      }
    }
  }, []);

  /**
   * 예약된 자동 검색 타이머를 해제한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const clearAutoSearch = useCallback((): void => {

    // 예약된 자동 검색이 없으면 해제 작업을 생략한다.
    if (autoSearchTimerRef.current === null) {
      // 해제할 타이머가 없는 처리를 종료한다.
      return;
    }

    // 이전 입력에서 예약한 자동 검색 실행을 취소한다.
    clearTimeout(autoSearchTimerRef.current);
    // 다음 자동 검색을 예약할 수 있도록 타이머 참조를 비운다.
    autoSearchTimerRef.current = null;
  }, []);

  /**
   * 검색 입력값을 화면 상태에 반영한다.
   *
   * @author HanWon.Jang
   * @param event 검색 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleKeywordChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 입력 중인 검색어를 검색창과 자동 검색 조건에 반영한다.
    setSearchKeyword(event.target.value);
  };

  /**
   * 두 글자 이상 입력이 멈추면 자동 검색을 예약한다.
   *
   * @author HanWon.Jang
   * @return 예약된 자동 검색을 해제하는 함수
   */
  const startAutoSearch = useCallback((): (() => void) => {

    const keyword = searchKeyword.trim();
    const previousKeyword = previousKeywordRef.current;
    // 현재 입력을 다음 변경 시 빈 검색어 전환 여부 판단에 사용한다.
    previousKeywordRef.current = keyword;
    // 입력이 바뀔 때 이전 검색 예약부터 취소한다.
    clearAutoSearch();

    // 검색어를 모두 지우면 기본 인기 도서 화면으로 돌아간다.
    if (keyword === "") {
      // 빈 초기 화면에서는 이미 실행 중인 인기 도서 조회를 유지한다.
      if (previousKeyword === "") {
        // 검색어 변화가 없는 초기 처리를 종료한다.
        return clearAutoSearch;
      }

      // 진행 중인 직접 검색 응답이 인기 도서 화면을 덮어쓰지 못하게 무효화한다.
      resultRequestIdRef.current += 1;
      // 같은 검색어를 다시 입력하면 새로 조회할 수 있도록 중복 기준을 초기화한다.
      lastSearchedKeywordRef.current = "";
      // 무효화한 직접 검색의 진행 표시를 즉시 해제한다.
      setIsSearching(false);
      // 비운 검색어에 이전 직접 검색 결과가 복원되지 않도록 캐시를 제거한다.
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      // 현재 선택된 기간의 인기 도서와 인기 검색어 화면을 복원한다.
      void loadPopularBooks(popularPeriod);
      void loadPopularKeywords();
      // 다음 입력 변경 시 예약된 타이머를 정리할 함수를 반환한다.
      return clearAutoSearch;
    }

    // 두 글자 미만이면 자동 검색 없이 검색 버튼과 Enter만 허용한다.
    if (
      keyword.length < AUTO_SEARCH_MIN_LENGTH
      || lastSearchedKeywordRef.current === keyword
    ) {
      // 자동 검색 조건을 충족하지 않은 처리를 종료한다.
      return clearAutoSearch;
    }

    /**
     * 입력 대기 시간이 지난 검색어로 첫 페이지 자동 검색을 실행한다.
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const executeAutoSearch = (): void => {
      // 실행된 타이머 참조를 비우고 현재 검색어 조회를 시작한다.
      autoSearchTimerRef.current = null;
      void executeBookSearch(keyword);
    };

    // 연속 입력마다 대기 시간을 다시 시작해 마지막 문자열만 조회한다.
    autoSearchTimerRef.current = setTimeout(
      executeAutoSearch,
      AUTO_SEARCH_DELAY_MS,
    );

    // 입력 변경이나 화면 해제 시 남은 자동 검색을 취소한다.
    return clearAutoSearch;
  }, [
    clearAutoSearch,
    executeBookSearch,
    loadPopularBooks,
    loadPopularKeywords,
    popularPeriod,
    searchKeyword,
  ]);

  // 검색어와 입력기 조합 상태가 바뀔 때 자동 검색 예약을 갱신한다.
  useEffect(startAutoSearch, [startAutoSearch]);

  /**
   * 클릭하거나 터치한 인기 검색어를 입력창에 반영하고 즉시 첫 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param keyword 사용자가 선택한 현재 인기 검색어
   * @return 인기 검색어 조회 처리가 끝나면 완료되는 Promise
   */
  async function selectPopularKeyword(keyword: string): Promise<void> {
    // 사용자가 선택한 검색어를 입력창에도 동일하게 표시한다
    setSearchKeyword(keyword);
    // 별도 제출 동작 없이 선택한 검색어로 도서 첫 페이지를 즉시 조회한다
    await executeBookSearch(keyword);
  }

  /**
   * 클릭하거나 키보드로 선택한 작가명을 입력창에 반영하고 즉시 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param author 사용자가 선택한 도서의 작가명
   * @return 작가명 조회 처리가 끝나면 완료되는 Promise
   */
  async function handleAuthorSelect(author: string): Promise<void> {

    const keyword = author.trim();

    // 선택한 작가명을 검색 입력창에 동일하게 표시한다.
    setSearchKeyword(keyword);
    // 별도 검색 버튼 조작 없이 선택한 작가명으로 도서 첫 페이지를 즉시 조회한다.
    await executeBookSearch(keyword);
  }

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
      const cachedKeyword = parsed.searchKeyword ?? "";

      // 저장된 검색어와 결과 페이지 상태를 화면에 복원한다.
      lastSearchedKeywordRef.current = cachedKeyword;
      previousKeywordRef.current = cachedKeyword.trim();
      setSearchKeyword(cachedKeyword);
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
  const handleSearchClick = async (
    event?: FormEvent<HTMLFormElement>,
  ): Promise<void> => {

    // 브라우저의 기본 폼 제출 동작을 중지한다.
    event?.preventDefault();
    // 검색 버튼과 Enter가 실행되면 대기 중인 같은 입력의 자동 검색을 취소한다.
    clearAutoSearch();
    // 앞뒤 공백을 제거한 검색어로 첫 페이지를 조회한다.
    await executeBookSearch(searchKeyword.trim());
  };

  /**
   * 현재 검색 결과 다음 페이지를 조회해 기존 목록 뒤에 추가한다.
   *
   * @author HanWon.Jang
   * @return 추가 조회 처리가 끝나면 완료되는 Promise
   */
  const handleLoadMore = async (): Promise<void> => {

    // 입력 중인 다음 검색어가 아니라 현재 결과를 만든 검색어로 다음 페이지를 조회한다.
    const keyword = lastSearchedKeywordRef.current;

    // 검색어가 없거나 첫 페이지 및 추가 노출 중이면 중복 요청을 차단한다.
    if (!keyword || isSearching || isLoadingMore) {
      // 실행할 수 없는 추가 조회를 종료한다.
      return;
    }

    // 추가 조회 뒤 새 첫 페이지 검색이 시작되면 오래된 응답을 버릴 요청 순번을 보관한다.
    const requestId = resultRequestIdRef.current;

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

      // 추가 조회 중 새 첫 페이지 검색이 시작됐으면 이전 결과를 합치지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 첫 페이지 검색이 목록을 갱신하도록 오래된 추가 응답 처리를 종료한다.
        return;
      }

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
      // 새 첫 페이지 검색 뒤 도착한 이전 추가 조회 오류는 사용자에게 표시하지 않는다.
      if (requestId !== resultRequestIdRef.current) {
        // 최신 검색 흐름을 유지하고 오래된 오류 처리를 종료한다.
        return;
      }

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
  };

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
  const handleSelectBook = async (book: BookSearchResultType): Promise<void> => {
    // 독서 타이머에서 진입한 검색은 독후감 등록 화면 대신 목표기간 달력을 바로 연다.
    if (isTimerBookSearch) {
      // 선택한 도서를 타이머 전용 목표 독서기간 모달에 전달한다.
      setTimerPeriodBook(book);
      // 기존 독후감 확인과 일반 등록 화면 이동을 실행하지 않는다.
      return;
    }

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

      // 다음 도서 추천에서 진입한 검색은 선택 도서를 투표 화면으로 바로 전달한다.
      if (pageState.clubBookVoteReturnPath) {
        navigate(pageState.clubBookVoteReturnPath, {
          state: {
            recommendedBook: book,
            candidates: pageState.clubBookVoteCandidates,
          },
        });
        // 추천 도서 전달 뒤 현재 독서 등록 흐름을 실행하지 않는다.
        return;
      }

      // 독서 수정에서 진입했다면 같은 회차의 수정 화면으로 선택 도서를 돌려보낸다.
      const editRondNumb = Number(pageState.clubReadingEditRondNumb);
      const readingEntryPath = Number.isSafeInteger(editRondNumb) && editRondNumb > 0
        ? `/reading-clubs/update/book/${clubNumb}/${editRondNumb}`
        : `/reading-clubs/set/book/${clubNumb}`;
      // 선택한 책 정보와 ISBN을 등록 또는 수정 화면에 전달한다.
      navigate(
        `${readingEntryPath}?isbn=${encodeURIComponent(book.isbn)}`,
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
  };

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
      navigate(`/reading-clubs/books/search/info/${clubNumb}`, {
        state: {
          book,
          clubReadingEditRondNumb: pageState.clubReadingEditRondNumb,
          clubBookVoteReturnPath: pageState.clubBookVoteReturnPath,
          clubBookVoteCandidates: pageState.clubBookVoteCandidates,
        },
      });
      // 모임 책 상세 이동 뒤 개인 상세 흐름을 실행하지 않는다.
      return;
    }

    // 선택한 책 정보를 개인 책 상세 화면으로 전달한다.
    navigate("/book/search/info", {
      state: {
        book,
        entrySource: isTimerBookSearch
          ? READING_TIMER_SEARCH_SOURCE
          : undefined,
      },
    });
  }

  /**
   * 타이머에서 시작한 도서 등록 흐름을 현재 읽는 도서 모달로 돌려보낸다.
   *
   * @author SeungHyeon.Kang
   * @param selectedReport 새로 등록해 선택 상태로 표시할 독후감 번호
   * @return 반환값이 없다
   */
  function returnToTimer(selectedReport?: number): void {

    // PWA History State 교체 후에도 모달 재실행 정보가 남도록 타이머 복귀 경로를 생성한다.
    const timerReturnPath = getTimerReturnPath(selectedReport);
    // 검색 화면 이력을 타이머로 교체하고 현재 읽는 도서 모달 및 신규 도서 선택 정보를 전달한다.
    navigate(timerReturnPath, { replace: true });
  }

  /**
   * 타이머 전용 목표 독서기간 모달을 닫고 현재 읽는 도서 모달로 돌아간다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function closeTimerPeriod(): void {

    // 독후감 저장 중에는 화면을 이탈해 처리 결과를 잃지 않게 한다.
    if (isTimerReportSaving) {
      // 진행 중인 저장 요청을 유지하고 닫기 처리를 종료한다.
      return;
    }

    // 선택한 검색 도서 임시 상태를 비운다.
    setTimerPeriodBook(null);
    // 독서 타이머의 현재 읽는 도서 모달로 돌아간다.
    returnToTimer();
  }

  /**
   * 선택한 도서와 목표 독서기간으로 읽는 중 독후감을 등록한다.
   *
   * @author SeungHyeon.Kang
   * @param startDate 목표 독서 시작일
   * @param endDate 목표 독서 종료일
   * @return 독후감 등록과 타이머 복귀가 끝나면 완료되는 Promise
   */
  async function saveTimerReport(
    startDate: string,
    endDate: string,
  ): Promise<void> {

    // 선택 도서가 없거나 저장 중이면 중복 등록 요청을 보내지 않는다.
    if (!timerPeriodBook || isTimerReportSaving) {
      // 등록할 수 없는 현재 상태의 처리를 종료한다.
      return;
    }

    // 타이머에서 생성하는 독후감은 기존 읽는 중 정책의 비공개와 미평점 값을 사용한다.
    const reportData: ReportDtoType = {
      reptStat: REPORT_STATUS_READ,
      reptStdt: startDate,
      reptEndt: endDate,
      reptGrde: "0",
      reptColr: "",
      pubcYsno: "N" as const,
      reptCntn: "",
      bookTitl: stripHtmlTags(timerPeriodBook.title),
      bookAthr: normalizeBookAuthor(timerPeriodBook.author),
      bookPubl: stripHtmlTags(timerPeriodBook.publisher),
      bookIsbn: sanitizeText(timerPeriodBook.isbn),
      bookCvim: sanitizeText(timerPeriodBook.image),
      bookDesc: stripHtmlTags(timerPeriodBook.description),
      publDate: stripHtmlTags(timerPeriodBook.pubdate),
    };
    let savedReportNumber: number | undefined;

    try {
      // 모달 안에 공통 소형 회전 링을 표시하도록 저장 상태를 시작한다.
      setIsTimerReportSaving(true);

      /**
       * 읽는 중 독후감을 등록하고 타이머 공용 요약 캐시를 최신 상태로 준비한다.
       *
       * @author SeungHyeon.Kang
       * @return 새로 등록한 독후감 번호 Promise
       * @throws 독후감 등록 또는 타이머 요약 캐시 갱신에 실패하면 발생한다
       */
      const persistTimerReport = async (): Promise<number> => {
        // 기존 독후감 등록 API로 읽는 중 독후감과 도서 정보를 함께 저장한다.
        const reportResponse = await setReportApi(reportData);
        const timerSummaryOptions = getTimerSummaryOptions();
        // 등록 전 캐시가 타이머 복귀 화면에 남지 않도록 공용 요약을 만료 처리한다.
        await queryClient.invalidateQueries({
          queryKey: timerSummaryOptions.queryKey,
          refetchType: "none",
        });
        try {
          // 타이머 복귀 전에 최신 현재 읽는 도서 목록을 강제로 조회해 공용 캐시에 저장한다.
          await queryClient.fetchQuery({
            ...timerSummaryOptions,
            staleTime: 0,
          });
        } catch {
          // 사전 조회 실패는 등록 성공을 취소하지 않고 타이머 화면의 진입 조회에서 다시 시도하도록 만료 상태를 유지한다.
          await queryClient.invalidateQueries({
            queryKey: timerSummaryOptions.queryKey,
            refetchType: "none",
          });
        }
        // 타이머 복귀 화면에서 선택 상태로 사용할 신규 독후감 번호를 반환한다.
        return reportResponse.data;
      };

      // 등록과 캐시 준비가 끝나면 처리 중 알림을 같은 저장 성공 알림으로 전환한다.
      savedReportNumber = await runBlockingOperation(persistTimerReport, {
        success: {
          // "저장되었습니다."
          title: message("frontend.alert.saveSuccessTitle"),
          // "독후감이 저장되었어요."
          text: message("frontend.report.saved"),
        },
      });
    } catch (error) {
      // 기존 독후감 등록 실패 메시지와 서버 오류 원인을 사용자에게 표시한다.
      await sweetError(
        message("frontend.alert.createFailedTitle"),
        getApiErrorMessage(error, message("frontend.report.createFailed")),
      );
    } finally {
      // 성공과 실패에 관계없이 저장 진행 상태를 해제한다.
      setIsTimerReportSaving(false);
    }

    // 독후감 저장과 성공 안내가 완료된 경우에만 새 도서를 다시 조회할 타이머로 돌아간다.
    if (savedReportNumber !== undefined) {
      // 현재 검색 화면을 타이머로 교체하고 신규 도서가 선택된 도서 선택 모달을 다시 연다.
      returnToTimer(savedReportNumber);
    }
  }

  // 책 검색 화면이 렌더링과 이벤트 연결에 사용할 값을 반환한다.
  return {
    bookResult: visibleBookResult,
    handleAuthorSelect,
    handleKeywordChange,
    handleLoadMore,
    handleMoreInfo,
    handlePopularPeriodChange,
    selectPopularKeyword,
    handleSearchClick,
    handleSelectBook,
    hasMore,
    isInitialLoading,
    isLoadingMore,
    isPopularMode,
    isSearching,
    isTimerBookSearch,
    isTimerReportSaving,
    popularPeriod,
    popularKeywordList,
    searchKeyword,
    selectingBookIsbn,
    timerPeriodBook,
    closeTimerPeriod,
    saveTimerReport,
  };
}
