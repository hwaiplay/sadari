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
} from "@/features/Book/types/book.type";
import { moveToReportEntry } from "@/features/Book/utils/reportEntry";
import type { FormEvent } from "react";
import { useCallback, useEffect, useState } from "react";
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
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [selectingBookIsbn, setSelectingBookIsbn] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const navigationType = useNavigationType();
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const isClubBookSearch = clubNumbParam !== undefined;
  const clubNumb = Number(clubNumbParam);
  const hasValidClubNumb = Number.isSafeInteger(clubNumb) && clubNumb > 0;

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

      // 첫 페이지 조회 중임을 화면에 반영한다.
      setIsSearching(true);
      // 새 조회가 끝나기 전에는 이전 추가 노출 상태를 초기화한다.
      setIsEnd(true);

      // 카카오 API에서 검색 결과의 첫 50권 페이지를 조회한다.
      const responseData = await fetchBooks(keyword, 1);
      const firstVisibleCount = Math.min(
        SEARCH_PAGE_SIZE,
        responseData.bookList.length,
      );

      // 조회된 최대 50권 중 첫 10권과 다음 카카오 페이지 상태를 화면에 반영한다.
      setBookResult(responseData.bookList);
      setVisibleCount(firstVisibleCount);
      setNextStart(responseData.nextStart ?? null);
      setIsEnd(responseData.end);
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
      void executeBookSearch(initialSearchKeyword);
      // 전달받은 검색어 처리 뒤 캐시 복원 흐름을 종료한다.
      return;
    }

    const shouldRestoreSearch =
      navigationType === "POP" || state.keepSearchResult === true;

    // 새로 검색 화면에 진입하면 이전 검색 결과를 남기지 않는다.
    if (!shouldRestoreSearch) {
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
      // 신규 진입의 캐시 복원 처리를 종료한다.
      return;
    }

    const cached = sessionStorage.getItem(SEARCH_STORAGE_KEY);

    // 복원할 검색 상태가 없으면 현재 빈 화면을 유지한다.
    if (!cached) {
      // 저장된 검색 상태가 없는 복원 처리를 종료한다.
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
    } catch {
      // 손상된 검색 캐시는 다음 진입에 재사용되지 않도록 제거한다.
      sessionStorage.removeItem(SEARCH_STORAGE_KEY);
    }
  }, [executeBookSearch, location.key, location.state, navigationType]);

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
    handleSearchClick,
    handleSelectBook,
    hasMore,
    isLoadingMore,
    isSearching,
    searchKeyword,
    selectingBookIsbn,
    setSearchKeyword,
  };
}
