import type { BookSearchResultType } from "@/features/Book/types/book.type";

// 독서 타이머에서 공용 도서 검색 화면으로 진입한 경로를 구분하는 값이다.
export const READING_TIMER_SEARCH_SOURCE = "READING_TIMER" as const;

// 타이머 복귀 시 현재 읽는 도서 모달을 다시 열도록 전달하는 URL 파라미터이다.
export const READING_TIMER_MODAL_PARAM = "timerBookModal" as const;

// 타이머 복귀 시 방금 등록한 독후감을 선택하도록 전달하는 URL 파라미터이다.
export const READING_TIMER_REPORT_PARAM = "timerReport" as const;

// 현재 읽는 도서 모달 재실행 URL 파라미터의 허용값이다.
export const READING_TIMER_MODAL_OPEN = "open" as const;

/**
 * 공용 도서 검색 화면으로 전달하는 화면 이동 상태이다.
 */
export type SearchBookPageState = {
  initialSearchKeyword?: string;
  keepSearchResult?: boolean;
  entrySource?: typeof READING_TIMER_SEARCH_SOURCE;
  timerBook?: BookSearchResultType;
};

/**
 * 도서 검색에서 독서 타이머로 돌아갈 때 전달하는 화면 이동 상태이다.
 */
export type ReadingTimerPageState = {
  reopenBookModal?: boolean;
  selectedReport?: string;
};

/**
 * 타이머에서 시작한 도서 등록을 마친 뒤 현재 읽는 도서 모달로 복귀할 경로를 생성한다.
 *
 * @author SeungHyeon.Kang
 * @param selectedReport 새로 등록해 선택 상태로 표시할 독후감 번호
 * @return 모달 재실행 정보가 포함된 타이머 경로
 */
export function getTimerReturnPath(selectedReport?: number): string {

  // PWA의 History State가 교체되어도 복귀 의도가 유지되도록 URL 파라미터를 생성한다.
  const searchParams = new URLSearchParams();
  // 타이머가 현재 읽는 도서 모달을 한 번 열 수 있도록 허용된 값을 설정한다.
  searchParams.set(READING_TIMER_MODAL_PARAM, READING_TIMER_MODAL_OPEN);

  // 등록 응답의 유효한 독후감 번호만 타이머 선택값으로 전달한다.
  if (Number.isSafeInteger(selectedReport) && Number(selectedReport) > 0) {
    // 방금 등록한 독후감을 모달에서 선택하도록 번호를 설정한다.
    searchParams.set(READING_TIMER_REPORT_PARAM, String(selectedReport));
  }

  // 타이머에서 한 번 소비할 복귀 정보를 포함한 경로를 반환한다.
  return `/timer?${searchParams.toString()}`;
}
