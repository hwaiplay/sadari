import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { queryClient } from "@/app/query/queryClient";
import { queryKeys } from "@/app/query/queryKeys";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import { ActionButton } from "@/components/Button/ActionButton";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import { SearchBookButtonContent } from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import {
  setReadingTimerApi,
  uptReadingTimerApi,
  type ReadingTimer,
  type ReadingTimerBookTime,
  type ReadingTimerSummary,
  type TimerStatus,
} from "@/features/Timer/api/readingTimerApi";
import { getTimerSummaryOptions } from "@/features/Timer/hooks/useTimerSummaryQuery";
import { useBookTimeQuery } from "@/features/Timer/hooks/useBookTimeQuery";
import { notifyReadingTimerRunningChange } from "@/features/Timer/lib/readingTimerEvents";
import { getReadingHeatmapApi, type ReadingHeatmap } from "@/features/User/api/userApi";
import { ReadingHeatmapChart } from "@/pages/My/ReadingStatisticsSection";
import clsx from "clsx";
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
  type MouseEvent as ReactMouseEvent,
} from "react";
import { createPortal } from "react-dom";
import { Link } from "react-router-dom";
import * as styles from "./ReadingTimerPage.css";

const DAY_MESSAGE_KEYS = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"] as const;
const TIMER_PRESET_EIGHT_HOURS_SECONDS = 8 * 60 * 60;
const TIMER_PRESET_FOUR_HOURS_SECONDS = 4 * 60 * 60;
const TIMER_PRESET_TWO_HOURS_SECONDS = 2 * 60 * 60;

/**
 * 초 단위 시간을 시:분:초 형식으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param totalSeconds 표시할 전체 시간 초
 * @return 시:분:초 문자열
 */
function formatSeconds(totalSeconds: number) {

  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(safeSeconds / 3600).toString().padStart(2, "0");
  const minutes = Math.floor((safeSeconds % 3600) / 60).toString().padStart(2, "0");
  const seconds = (safeSeconds % 60).toString().padStart(2, "0");
  // 화면 타이머 문자열을 반환한다
  return `${hours}:${minutes}:${seconds}`;
}

/**
 * 초 단위 독서시간을 초를 생략한 시분 형식으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param totalSeconds 표시할 전체 독서시간 초
 * @return 시간 유무에 따른 시분 또는 분 문자열
 */
function formatBookTime(totalSeconds: number): string {

  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(safeSeconds / 3600);
  const minutes = Math.floor((safeSeconds % 3600) / 60).toString().padStart(2, "0");

  // 한 시간 미만의 누적 독서시간에는 불필요한 00시간을 표시하지 않는다
  if (hours === 0) {
    // "{0}분"
    return message("frontend.timer.setting.minutes", [minutes]);
  }

  const displayHours = hours.toString().padStart(2, "0");
  // "{0}시간 {1}분"
  return message("frontend.timer.setting.hoursMinutes", [displayHours, minutes]);
}

/**
 * 도서 표지와 제목, 작가 및 누적 타이머 시간을 목록 항목으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param bookTime 도서별 누적 타이머 기록
 * @return 도서별 누적 독서 시간 항목
 */
function renderBookTimeItem(bookTime: ReadingTimerBookTime) {

  // 비어 있거나 잘못된 표지 주소를 공통 대체 이미지로 보정한다
  const coverSource = getBookCoverImageSource(bookTime.bookCvim);
  // 누적 독서시간을 초를 생략한 시분 형식으로 변환한다
  const readingTime = formatBookTime(bookTime.readSecs);
  // "저자 정보 없음"
  const bookAuthor = bookTime.bookAthr?.trim() || message("frontend.common.unknownAuthor");
  // 최근 기록순 도서별 누적시간 항목을 반환한다
  return (
    <Link
      key={bookTime.bookNumb}
      className={styles.bookTimeItem}
      to={`/report/detail/${bookTime.reptNumb}`}
    >
      {/* 누적 타이머 기록의 도서 표지 영역 */}
      <img
        className={styles.bookTimeCover}
        src={coverSource}
        onError={handleBookCoverImageError}
        alt={bookTime.bookTitl}
      />
      {/* 누적 타이머 기록의 도서 제목과 작가 영역 */}
      <div className={styles.bookTimeInfo}>
        <strong className={styles.bookTimeBookTitle}>{bookTime.bookTitl}</strong>
        <span className={styles.bookTimeBookAuthor}>{bookAuthor}</span>
      </div>
      {/* 도서별 누적 독서 시간 영역 */}
      <strong className={styles.bookTimeValue}>{readingTime}</strong>
    </Link>
  );
}

/**
 * 목표시간 설정 여부에 따라 타이머 영역에 표시할 시간을 계산한다
 *
 * @author SeungHyeon.Kang
 * @param displaySeconds 현재 세션의 누적 독서 시간 초
 * @param activeTimer 현재 실행 또는 일시정지 중인 타이머
 * @param targetSeconds 시작 전에 설정한 목표 독서 시간 초
 * @return 목표 타이머의 남은 시간 또는 일반 타이머의 누적 시간 초
 */
function getTimerClockSecs(displaySeconds: number, activeTimer: ReadingTimer | undefined, targetSeconds: number | undefined) {

  // 목표시간이 설정된 세션은 경과시간을 차감한 남은 시간을 표시한다
  if (activeTimer && typeof activeTimer.targSecs === "number") {
    // 목표시간이 지나도 화면 시간이 음수가 되지 않게 제한한다
    const remainingSeconds = Math.max(0, activeTimer.targSecs - displaySeconds);
    // 계산한 목표 독서시간의 잔여 초를 반환한다
    return remainingSeconds;
  }

  // 목표시간 없이 시작한 세션은 기존 누적 독서시간을 표시한다
  if (activeTimer) {
    // 현재 세션에서 읽은 누적 초를 반환한다
    return displaySeconds;
  }

  // 시작 전에는 사용자가 설정한 목표시간 또는 초기 시간을 반환한다
  return targetSeconds ?? 0;
}

/**
 * 현재 읽는 도서 목록에서 사용자가 선택한 독후감의 도서를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param bookList 현재 읽는 도서 목록
 * @param selectedReport 선택한 독후감 번호 문자열
 * @return 선택한 도서 또는 선택되지 않은 경우 undefined
 */
function getSelectedBook(bookList: ReadingTimer[] | undefined, selectedReport: string): ReadingTimer | undefined {

  // 선택값이나 현재 읽는 도서가 없으면 연결할 도서가 없는 상태를 반환한다
  if (!selectedReport || !bookList?.length) {
    // 도서가 선택되지 않은 상태를 반환한다
    return undefined;
  }

  const reportNumber = Number(selectedReport);
  // 현재 읽는 도서를 순서대로 확인하여 선택 번호와 일치하는 항목을 찾는다
  for (const bookItem of bookList) {
    // 독후감 번호가 일치하면 타이머에 표시할 도서를 반환한다
    if (bookItem.reptNumb === reportNumber) {
      // 선택한 현재 읽는 도서를 반환한다
      return bookItem;
    }
  }

  // 목록에서 선택 번호를 찾지 못하면 도서가 없는 상태를 반환한다
  return undefined;
}

/**
 * 초 단위 타이머 설정값을 사용자에게 표시할 시·분 문자열로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param totalSeconds 알림 목표 독서 시간 초
 * @return 타이머 설정 표시 문자열
 */
function formatTimerSetting(totalSeconds: number): string {

  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  // 시간 없이 분만 있는 설정값을 반환한다
  if (hours === 0) {
    // "{0}분"
    return message("frontend.timer.setting.minutes", [minutes]);
  }
  // 분 없이 시간만 있는 설정값을 반환한다
  if (minutes === 0) {
    // "{0}시간"
    return message("frontend.timer.setting.hours", [hours]);
  }
  // "{0}시간 {1}분"
  return message("frontend.timer.setting.hoursMinutes", [hours, minutes]);
}

type ReadingBookModalProps = {
  books: ReadingTimer[];
  selectedReport: string;
  onSelect: (reportNumber: string) => void;
  onClose: () => void;
};

/**
 * 현재 읽고 있는 도서를 표지 목록으로 제공하는 타이머 도서 선택 모달을 표시한다
 *
 * @author SeungHyeon.Kang
 * @param books 현재 읽고 있는 도서 목록
 * @param selectedReport 현재 선택된 독후감 번호
 * @param onSelect 도서 선택 시 실행할 함수
 * @param onClose 모달 닫기 시 실행할 함수
 * @return 타이머 도서 선택 모달
 */
function ReadingBookModal({ books, selectedReport, onSelect, onClose }: ReadingBookModalProps) {

  // 모달 안에서 고른 도서를 선택 버튼으로 확정하기 전까지 임시로 보관한다
  const [pendingReport, setPendingReport] = useState(selectedReport);

  // 도서 선택 모달이 열린 동안 배경 화면 스크롤을 잠근다
  useBodyScrollLock(true);

  useEffect(() => {
    /**
     * Escape 키로 도서 선택 모달을 닫는다
     *
     * @author SeungHyeon.Kang
     * @param event 키보드 입력 이벤트
     * @return 반환값이 없다
     */
    const handleKeyDown = (event: KeyboardEvent): void => {
      // Escape 키 입력에서만 도서 선택 모달을 닫는다
      if (event.key !== "Escape") {
        // 다른 키 입력은 별도 처리 없이 종료한다
        return;
      }

      // 키보드 사용자가 원래 타이머 화면으로 돌아가도록 모달을 닫는다
      onClose();
    };

    // 모달이 열린 동안 Escape 키 입력을 감지한다
    document.addEventListener("keydown", handleKeyDown);
    // 모달이 닫히면 전역 키보드 이벤트를 정리한다
    return () => {
      // 닫힌 모달의 키보드 처리가 남지 않도록 이벤트를 해제한다
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [onClose]);

  /**
   * 모달 바깥 배경을 직접 누른 경우에만 도서 선택 모달을 닫는다
   *
   * @author SeungHyeon.Kang
   * @param event 모달 배경 마우스 이벤트
   * @return 반환값이 없다
   */
  const handleOverlayMouseDown = (event: ReactMouseEvent<HTMLDivElement>): void => {
    // 모달 본문에서 시작된 클릭은 선택 동작을 유지한다
    if (event.target !== event.currentTarget) {
      // 모달 내부 클릭 처리를 종료한다
      return;
    }

    // 배경을 누르면 도서 선택을 변경하지 않고 모달을 닫는다
    onClose();
  };

  /**
   * 현재 읽는 도서 버튼의 독후감 번호를 모달의 임시 선택값으로 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 선택한 도서 버튼 이벤트
   * @return 반환값이 없다
   */
  const handleBookSelect = (event: ReactMouseEvent<HTMLButtonElement>): void => {

    const reportNumber = event.currentTarget.value;
    // 유효한 독후감 번호가 있는 도서만 타이머에 연결한다
    if (!reportNumber) {
      // 연결할 독후감 번호가 없으면 선택 처리를 종료한다
      return;
    }

    // 선택 버튼으로 확정할 수 있도록 독후감 번호를 임시 선택값에 저장한다
    setPendingReport(reportNumber);
  };

  /**
   * 모달에서 고른 도서를 타이머 선택값으로 확정한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleBookConfirm = (): void => {

    // 선택한 독후감 번호가 없으면 도서 확정 처리를 종료한다
    if (!pendingReport) {
      // 도서 없이 기록하기는 별도 버튼에서 처리한다
      return;
    }

    // 임시로 고른 독후감 번호를 타이머 화면에 반영한다
    onSelect(pendingReport);
    // 확정한 도서를 화면에서 확인할 수 있도록 모달을 닫는다
    onClose();
  };

  /**
   * 도서 연결 없이 타이머를 시작할 수 있도록 선택값을 비운다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleWithoutBook = (): void => {

    // 기존 도서 연결값을 제거한다
    onSelect("");
    // 도서 없음 선택을 반영한 뒤 모달을 닫는다
    onClose();
  };

  /**
   * 현재 읽는 도서 한 권을 표지와 제목이 있는 선택 버튼으로 구성한다
   *
   * @author SeungHyeon.Kang
   * @param bookItem 현재 읽는 도서 항목
   * @return 도서 선택 버튼 또는 독후감 번호가 없을 때 null
   */
  const renderBookOption = (bookItem: ReadingTimer) => {

    // 타이머와 연결할 독후감 번호가 없는 항목은 선택 목록에서 제외한다
    if (!bookItem.reptNumb) {
      // 선택할 수 없는 도서는 표시하지 않는다
      return null;
    }

    // 현재 읽는 도서의 표지와 제목을 포함한 선택 버튼을 반환한다
    return (
      <button
        className={styles.modalBookOption}
        type="button"
        value={String(bookItem.reptNumb)}
        data-selected={pendingReport === String(bookItem.reptNumb)}
        onClick={handleBookSelect}
        key={bookItem.reptNumb}
      >
        {/* 현재 읽는 도서 표지 영역 */}
        <span className={styles.modalBookCover}>
          <img
            className={styles.coverImage}
            src={getBookCoverImageSource(bookItem.bookCvim)}
            onError={handleBookCoverImageError}
            alt=""
            aria-hidden="true"
          />
        </span>
        {/* 현재 읽는 도서 제목과 선택 상태 영역 */}
        <span className={styles.modalBookText}>
          <span className={styles.modalBookTitle}>
            {bookItem.bookTitl ?? /* "연결된 도서 없음" */ message("frontend.timer.book.none")}
          </span>
          <span className={styles.modalBookState}>
            {/* "읽는 중" */}
            {message("frontend.common.reading")}
          </span>
        </span>
      </button>
    );
  };

  // 페이지의 stacking context와 분리된 최상위 도서 선택 모달을 반환한다
  return createPortal(
    /* 현재 읽는 도서 선택 모달 배경 영역 */
    <div className={styles.modalOverlay} role="presentation" onMouseDown={handleOverlayMouseDown}>
      {/* 현재 읽는 도서 선택 모달 본문 영역 */}
      <section className={styles.bookModal} role="dialog" aria-modal="true" aria-labelledby="timer-book-modal-title">
        {/* 도서 선택 모달 제목과 닫기 영역 */}
        <header className={styles.modalHeader}>
          <div>
            <h2 id="timer-book-modal-title" className={styles.modalTitle}>
              {/* "현재 읽고 있는 도서" */}
              {message("frontend.timer.book.modal.title")}
            </h2>
            <p className={styles.modalDescription}>
              {/* "타이머에 연결할 도서를 선택해 주세요." */}
              {message("frontend.timer.book.modal.description")}
            </p>
          </div>
          <button
            className={styles.modalClose}
            type="button"
            aria-label={message("frontend.timer.book.modal.close")}
            onClick={onClose}
          >
            <img src="/img/icons/icon-close.svg" alt="" aria-hidden="true" />
          </button>
        </header>
        {/* 현재 읽는 도서 선택 목록 영역 */}
        <div className={styles.modalBody}>
          {books.length ? books.map(renderBookOption) : (
            <p className={styles.modalEmpty}>
              {/* "현재 읽고 있는 도서가 없습니다." */}
              {message("frontend.timer.book.modal.empty")}
            </p>
          )}
        </div>
        {/* 도서 연결 해제와 선택 확정 버튼 영역 */}
        <footer className={styles.modalFooter}>
          <ActionButton
            variant="secondary"
            size="lg"
            className={styles.modalFooterButton}
            onClick={handleWithoutBook}
          >
            {/* "도서 없이 기록하기" */}
            {message("frontend.timer.book.without")}
          </ActionButton>
          <ActionButton
            variant="primary"
            size="lg"
            className={styles.modalFooterButton}
            disabled={!pendingReport}
            onClick={handleBookConfirm}
          >
            {/* "선택" */}
            {message("frontend.common.select")}
          </ActionButton>
        </footer>
      </section>
    </div>,
    document.body,
  );
}

type TimerSettingModalProps = {
  targetSeconds?: number;
  maxSeconds: number;
  onSave: (targetSeconds: number) => void;
  onClose: () => void;
};

/**
 * 마이페이지 독서 목표 설정 모달 형식으로 알림 목표 독서시간을 입력받는다
 *
 * @author SeungHyeon.Kang
 * @param targetSeconds 현재 선택한 목표 독서 시간 초
 * @param maxSeconds 설정 가능한 최대 독서 시간 초
 * @param onSave 목표시간 저장 시 실행할 함수
 * @param onClose 모달 닫기 시 실행할 함수
 * @return 타이머 설정 모달
 */
function TimerSettingModal({ targetSeconds, maxSeconds, onSave, onClose }: TimerSettingModalProps) {

  const initialSeconds = targetSeconds ?? 1800;
  const maxHours = Math.floor(maxSeconds / 3600);
  // 모달에 표시할 시간 단위 목표값을 설정한다
  const [hours, setHours] = useState(Math.floor(initialSeconds / 3600));
  // 모달에 표시할 분 단위 목표값을 설정한다
  const [minutes, setMinutes] = useState(Math.floor((initialSeconds % 3600) / 60));
  const selectedSeconds = (hours * 3600) + (minutes * 60);
  const isValid = selectedSeconds > 0 && selectedSeconds <= maxSeconds;

  // 타이머 설정 모달이 열린 동안 배경 화면 스크롤을 잠근다
  useBodyScrollLock(true);

  // 타이머 설정 모달의 Escape 키 닫기 동작을 등록한다
  useEffect(() => {
    /**
     * Escape 키로 타이머 설정 모달을 닫는다
     *
     * @author SeungHyeon.Kang
     * @param event 키보드 입력 이벤트
     * @return 반환값이 없다
     */
    const handleKeyDown = (event: KeyboardEvent): void => {
      // Escape 키가 아니면 다른 입력 처리를 유지한다
      if (event.key !== "Escape") {
        // 타이머 설정 모달 닫기 처리를 생략한다
        return;
      }
      // 타이머 설정 모달을 닫는다
      onClose();
    };

    window.addEventListener("keydown", handleKeyDown);
    // 모달이 닫힐 때 키보드 이벤트를 정리한다
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [onClose]);

  /**
   * 시간 입력값을 0부터 최대시간 사이로 보정한다
   *
   * @author SeungHyeon.Kang
   * @param nextHours 적용할 시간
   * @return 반환값이 없다
   */
  const updateHours = (nextHours: number): void => {

    const safeHours = Math.min(maxHours, Math.max(0, nextHours));
    // 최대시간을 선택하면 분을 0으로 제한한다
    if (safeHours === maxHours) {
      // 8시간 초과 설정을 막도록 분을 비운다
      setMinutes(0);
    }
    // 보정한 시간 값을 화면에 설정한다
    setHours(safeHours);
  };

  /**
   * 분 입력값을 0부터 59 사이로 보정한다
   *
   * @author SeungHyeon.Kang
   * @param nextMinutes 적용할 분
   * @return 반환값이 없다
   */
  const updateMinutes = (nextMinutes: number): void => {

    // 최대시간에서는 분을 추가하지 않는다
    if (hours === maxHours) {
      // 8시간 설정을 유지한다
      setMinutes(0);
      // 분 입력 처리를 종료한다
      return;
    }
    // 보정한 분 값을 화면에 설정한다
    setMinutes(Math.min(59, Math.max(0, nextMinutes)));
  };

  /**
   * 타이머 설정 모달 바깥 영역을 누르면 모달을 닫는다
   *
   * @author SeungHyeon.Kang
   * @param event 모달 배경 마우스 이벤트
   * @return 반환값이 없다
   */
  const handleSettingOverlay = (event: ReactMouseEvent<HTMLDivElement>): void => {

    // 모달 본문에서 시작된 클릭은 입력 상태를 유지한다
    if (event.currentTarget !== event.target) {
      // 모달 바깥 클릭이 아닌 정상 흐름을 종료한다
      return;
    }
    // 타이머 설정 모달을 닫는다
    onClose();
  };

  /**
   * 시간 감소 버튼으로 목표시간을 한 시간 줄인다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleHourDecrease = (): void => {

    // 현재 시간에서 한 시간을 줄여 허용 범위로 보정한다
    updateHours(hours - 1);
  };

  /**
   * 시간 증가 버튼으로 목표시간을 한 시간 늘린다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleHourIncrease = (): void => {

    // 현재 시간에서 한 시간을 늘려 허용 범위로 보정한다
    updateHours(hours + 1);
  };

  /**
   * 시간 입력값에서 숫자만 추출해 목표시간에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 시간 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleHourChange = (event: ChangeEvent<HTMLInputElement>): void => {

    // 숫자가 아닌 문자를 제거한 시간 값을 허용 범위로 보정한다
    updateHours(Number(event.currentTarget.value.replace(/\D/g, "")) || 0);
  };

  /**
   * 분 감소 버튼으로 목표시간을 오 분 줄인다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleMinuteDecrease = (): void => {

    // 현재 분에서 오 분을 줄여 허용 범위로 보정한다
    updateMinutes(minutes - 5);
  };

  /**
   * 분 증가 버튼으로 목표시간을 오 분 늘린다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleMinuteIncrease = (): void => {

    // 현재 분에서 오 분을 늘려 허용 범위로 보정한다
    updateMinutes(minutes + 5);
  };

  /**
   * 분 입력값에서 숫자만 추출해 목표시간에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 분 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleMinuteChange = (event: ChangeEvent<HTMLInputElement>): void => {

    // 숫자가 아닌 문자를 제거한 분 값을 허용 범위로 보정한다
    updateMinutes(Number(event.currentTarget.value.replace(/\D/g, "")) || 0);
  };

  /**
   * 빠른 선택 radio 버튼의 목표시간을 시간과 분 입력값에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 선택한 목표시간 radio 변경 이벤트
   * @return 반환값이 없다
   */
  const handlePresetChange = (event: ChangeEvent<HTMLInputElement>): void => {

    const nextSeconds = Math.min(maxSeconds, Math.max(0, Number(event.currentTarget.value)));
    // 선택한 빠른 목표시간의 시간 단위를 입력 상태에 설정한다
    setHours(Math.floor(nextSeconds / 3600));
    // 빠른 목표시간은 정시 단위이므로 분 입력을 초기화한다
    setMinutes(0);
  };

  /**
   * 검증된 목표 독서시간을 시작 전 설정값으로 저장한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleTimerSettingSave = (): void => {

    // 모달에서 선택한 초 단위 목표시간을 부모 화면에 저장한다
    onSave(selectedSeconds);
  };

  // 타이머 목표시간을 입력하는 설정 모달을 본문 최상위에 표시한다
  return createPortal((
    /* 타이머 설정 모달 배경 영역 */
    <div
      className={styles.modalOverlay}
      role="presentation"
      onMouseDown={handleSettingOverlay}
    >
      {/* 타이머 설정 모달 본문 영역 */}
      <section
        className={styles.timerSettingModal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="timer-setting-title"
      >
        {/* 타이머 설정 제목과 닫기 영역 */}
        <div className={styles.modalHeader}>
          <div>
            <h2 className={styles.modalTitle} id="timer-setting-title">
              {/* "타이머 설정" */}
              {message("frontend.timer.setting.title")}
            </h2>
            <p className={styles.modalDescription}>
              {/* "읽고 싶은 시간을 정하면 시간이 지난 뒤 알림을 보내드려요." */}
              {message("frontend.timer.setting.description")}
            </p>
          </div>
          {/* "타이머 설정 닫기" */}
          <button
            className={styles.timerSettingClose}
            type="button"
            aria-label={message("frontend.timer.setting.close")}
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {/* 시간과 분 입력 영역 */}
        <div className={styles.timerSettingBody}>
          <div className={styles.timerSettingGrid}>
            <label className={styles.timerSettingField} htmlFor="timer-setting-hours">
              {/* "시간" */}
              <span>{message("frontend.timer.setting.hourLabel")}</span>
              <span className={styles.timerSettingStepper}>
                {/* "시간 늘리기" */}
                <button
                  className={styles.timerSettingStepButton}
                  type="button"
                  aria-label={message("frontend.timer.setting.hourIncrease")}
                  onClick={handleHourIncrease}
                >
                  +
                </button>
                <input
                  id="timer-setting-hours"
                  className={styles.timerSettingInput}
                  inputMode="numeric"
                  value={hours}
                  onChange={handleHourChange}
                />
                {/* "시간 줄이기" */}
                <button
                  className={styles.timerSettingStepButton}
                  type="button"
                  aria-label={message("frontend.timer.setting.hourDecrease")}
                  onClick={handleHourDecrease}
                >
                  -
                </button>
              </span>
            </label>
            <label className={styles.timerSettingField} htmlFor="timer-setting-minutes">
              {/* "분" */}
              <span>{message("frontend.timer.setting.minuteLabel")}</span>
              <span className={styles.timerSettingStepper}>
                {/* "분 늘리기" */}
                <button
                  className={styles.timerSettingStepButton}
                  type="button"
                  aria-label={message("frontend.timer.setting.minuteIncrease")}
                  onClick={handleMinuteIncrease}
                >
                  +
                </button>
                <input
                  id="timer-setting-minutes"
                  className={styles.timerSettingInput}
                  inputMode="numeric"
                  value={minutes}
                  onChange={handleMinuteChange}
                />
                {/* "분 줄이기" */}
                <button
                  className={styles.timerSettingStepButton}
                  type="button"
                  aria-label={message("frontend.timer.setting.minuteDecrease")}
                  onClick={handleMinuteDecrease}
                >
                  -
                </button>
              </span>
            </label>
          </div>

          {/* 빠른 목표시간 radio 선택 영역 */}
          {/* "빠른 시간 선택" */}
          <div
            className={styles.timerSettingPresetList}
            role="radiogroup"
            aria-label={message("frontend.timer.setting.presetLabel")}
          >
            {/* 8시간 빠른 선택 항목 영역 */}
            <label className={styles.timerSettingPreset}>
              <input
                className={styles.timerSettingPresetInput}
                type="radio"
                name="timer-setting-preset"
                value={TIMER_PRESET_EIGHT_HOURS_SECONDS}
                checked={selectedSeconds === TIMER_PRESET_EIGHT_HOURS_SECONDS}
                onChange={handlePresetChange}
              />
              <span>
                {/* "8시간" */}
                {message("frontend.timer.setting.hours", [8])}
              </span>
            </label>
            {/* 4시간 빠른 선택 항목 영역 */}
            <label className={styles.timerSettingPreset}>
              <input
                className={styles.timerSettingPresetInput}
                type="radio"
                name="timer-setting-preset"
                value={TIMER_PRESET_FOUR_HOURS_SECONDS}
                checked={selectedSeconds === TIMER_PRESET_FOUR_HOURS_SECONDS}
                onChange={handlePresetChange}
              />
              <span>
                {/* "4시간" */}
                {message("frontend.timer.setting.hours", [4])}
              </span>
            </label>
            {/* 2시간 빠른 선택 항목 영역 */}
            <label className={styles.timerSettingPreset}>
              <input
                className={styles.timerSettingPresetInput}
                type="radio"
                name="timer-setting-preset"
                value={TIMER_PRESET_TWO_HOURS_SECONDS}
                checked={selectedSeconds === TIMER_PRESET_TWO_HOURS_SECONDS}
                onChange={handlePresetChange}
              />
              <span>
                {/* "2시간" */}
                {message("frontend.timer.setting.hours", [2])}
              </span>
            </label>
          </div>
          <p className={styles.timerSettingGuide}>
            {isValid ? (
              <>
                {/* "{0} 뒤에 알려드려요." */}
                {message("frontend.timer.setting.selected", [formatTimerSetting(selectedSeconds)])}
              </>
            ) : (
              <>
                {/* "1분 이상 8시간 이하로 설정해 주세요." */}
                {message("frontend.timer.setting.required")}
              </>
            )}
          </p>
        </div>

        {/* 타이머 설정 취소와 저장 영역 */}
        <div className={styles.modalActions}>
          <ActionButton variant="secondary" size="lg" className={styles.modalActionButton} onClick={onClose}>
            {/* "취소" */}
            {message("frontend.common.cancel")}
          </ActionButton>
          <ActionButton
            variant="primary"
            size="lg"
            className={styles.modalActionButton}
            disabled={!isValid}
            onClick={handleTimerSettingSave}
          >
            {/* "저장" */}
            {message("frontend.profile.save")}
          </ActionButton>
        </div>
      </section>
    </div>
  ), document.body);
}

type TimerReadingHeatmapProps = {
  refreshKey: number;
};

/**
 * 타이머 화면에 전체 독서 통계 없이 연도별 독서 잔디만 조회해 표시한다
 *
 * @author SeungHyeon.Kang
 * @param props 독서 잔디 갱신 번호
 * @return 독서 잔디 카드
 */
function TimerReadingHeatmap(props: TimerReadingHeatmapProps) {
  const { refreshKey } = props;
  // 화면 이탈과 연도 변경 시 이전 잔디 조회를 취소할 요청 참조를 생성한다
  const abortControllerRef = useRef<AbortController | null>(null);
  // 이미 처리한 타이머 완료 갱신 번호를 저장한다
  const appliedRefreshKeyRef = useRef(refreshKey);
  // 타이머 화면에 표시할 선택 연도의 독서 잔디 상태를 생성한다
  const [heatmap, setHeatmap] = useState<ReadingHeatmap | null>(null);
  // 독서 잔디 조회 진행 상태를 생성한다
  const [isHeatmapLoading, setIsHeatmapLoading] = useState(true);
  // 독서 잔디 조회 실패 상태를 생성한다
  const [isHeatmapError, setIsHeatmapError] = useState(false);

  /**
   * 선택 연도의 독서 잔디 전용 API를 조회해 화면 상태에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param readYear 조회할 연도, 없으면 현재 연도
   * @return 독서 잔디 조회 완료 Promise
   */
  const loadHeatmap = useCallback(async (readYear?: number): Promise<void> => {
    // 연도 변경 전에 남아 있는 이전 잔디 요청을 취소한다
    abortControllerRef.current?.abort();
    // 현재 잔디 요청을 화면 이탈 시 취소할 제어 객체를 생성한다
    const abortController = new AbortController();
    // 최신 독서 잔디 요청 제어 객체를 저장한다
    abortControllerRef.current = abortController;
    // 잔디 조회 진행 상태를 화면에 반영한다
    setIsHeatmapLoading(true);
    // 이전 실패 상태를 초기화한다
    setIsHeatmapError(false);

    // 잔디 전용 API의 성공과 실패 및 완료 상태를 각각 처리한다
    try {
      // 선택한 연도의 날짜별 독서 시간만 조회한다
      const response = await getReadingHeatmapApi(readYear, abortController.signal);

      // 화면이 유지되는 동안 검증된 독서 잔디 응답을 반영한다
      if (!abortController.signal.aborted) {
        // 전체 통계 없이 날짜별 독서 시간과 연도 목록만 저장한다
        setHeatmap(response);
      }

    } catch {
      // 사용자가 화면을 벗어나 취소된 요청은 실패 화면으로 처리하지 않는다
      if (!abortController.signal.aborted) {
        // 잔디 조회를 다시 시도할 수 있도록 실패 상태를 표시한다
        setIsHeatmapError(true);
      }

    } finally {
      // 최신 요청이 유지되는 동안에만 로딩 상태를 종료한다
      if (!abortController.signal.aborted) {
        // 독서 잔디 조회 완료 상태를 반영한다
        setIsHeatmapLoading(false);
      }
    }
  }, []);

  /**
   * 타이머 화면 진입 시 현재 연도 잔디를 조회하고 이탈 시 요청을 정리한다
   *
   * @author SeungHyeon.Kang
   * @return 독서 잔디 요청 취소 함수
   */
  const prepareHeatmap = useCallback((): (() => void) => {
    // 타이머 화면의 현재 연도 독서 잔디 조회를 시작한다
    void loadHeatmap();

    /**
     * 타이머 화면을 벗어날 때 진행 중인 독서 잔디 조회를 취소한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const abortHeatmapRequest = (): void => {
      // 해제된 화면의 상태가 변경되지 않도록 최신 요청을 취소한다
      abortControllerRef.current?.abort();
    };

    // Effect 정리 단계에서 실행할 잔디 요청 취소 함수를 반환한다
    return abortHeatmapRequest;
  }, [loadHeatmap]);

  // 타이머 화면 진입과 이탈에 맞춰 독서 잔디 조회를 관리한다
  useEffect(prepareHeatmap, [prepareHeatmap]);

  /**
   * 독서 타이머 완료 후 현재 선택 연도의 잔디를 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const refreshCompletedHeatmap = useCallback((): void => {
    // 이미 처리한 완료 갱신 번호는 중복 조회하지 않는다
    if (appliedRefreshKeyRef.current === refreshKey) {
      // 추가 잔디 조회 없이 갱신 처리를 종료한다
      return;
    }

    // 현재 완료 갱신 번호를 처리 완료 상태로 저장한다
    appliedRefreshKeyRef.current = refreshKey;
    // 사용자가 보고 있던 연도의 최신 독서 시간을 다시 조회한다
    void loadHeatmap(heatmap?.selectedYear);
  }, [heatmap?.selectedYear, loadHeatmap, refreshKey]);

  // 타이머 완료 갱신 번호가 변경되면 독서 잔디를 다시 조회한다
  useEffect(refreshCompletedHeatmap, [refreshCompletedHeatmap]);

  /**
   * 잔디에서 선택한 조회 연도를 전용 API에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param readYearValue 선택한 연도 문자열
   * @return 반환값이 없다
   */
  const handleYearChange = (readYearValue: string): void => {
    const readYear = Number(readYearValue);

    // 서버가 제공한 조회 가능 연도 중 현재 선택과 다른 연도만 다시 조회한다
    if (!heatmap || heatmap.selectedYear === readYear || !heatmap.availableYears.includes(readYear)) {
      return;
    }

    // 선택한 연도의 독서 시간 잔디만 다시 조회한다
    void loadHeatmap(readYear);
  };

  /**
   * 독서 잔디 조회 실패 후 현재 선택 연도로 다시 요청한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleHeatmapRetry = (): void => {
    // 실패한 연도 또는 현재 연도의 독서 잔디 조회를 다시 시작한다
    void loadHeatmap(heatmap?.selectedYear);
  };

  // 타이머 화면에는 마이페이지 공통 잔디만 포함한 카드를 반환한다
  return (
    /* 연도별 독서 시간 잔디 전체 영역 */
    <section className={styles.heatmapCard} aria-label={message("frontend.profile.readingStats.heatmapTitle")}>
      {/* 독서 잔디 조회 중 상태 영역 */}
      {isHeatmapLoading && !heatmap && (
        <p className={styles.heatmapState} role="status">
          {/* "독서 통계를 불러오는 중입니다." */}
          {message("frontend.profile.readingStats.loading")}
        </p>
      )}
      {/* 독서 잔디 조회 실패와 재시도 영역 */}
      {isHeatmapError && !heatmap && (
        <div className={styles.heatmapError} role="alert">
          <span>
            {/* "독서 통계를 불러오지 못했습니다." */}
            {message("frontend.profile.readingStats.loadFailed")}
          </span>
          <button className={styles.heatmapRetry} type="button" onClick={handleHeatmapRetry}>
            {/* "다시 시도" */}
            {message("frontend.common.retry")}
          </button>
        </div>
      )}
      {/* 조회가 완료된 선택 연도의 독서 잔디 영역 */}
      {heatmap && (
        <ReadingHeatmapChart
          heatmap={heatmap}
          onYearChange={handleYearChange}
          titleClassName={styles.heatmapTitle}
        />
      )}
    </section>
  );
}

/**
 * 독서 타이머 실행과 주간 출석 현황을 한 화면에서 제공한다
 *
 * @author SeungHyeon.Kang
 * @return 독서 타이머 화면
 */
export default function ReadingTimerPage() {

  const bookTimeQuery = useBookTimeQuery();
  const [summary, setSummary] = useState<ReadingTimerSummary>();
  const [selectedReport, setSelectedReport] = useState("");
  const [displaySeconds, setDisplaySeconds] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isChanging, setIsChanging] = useState(false);
  const [isBookModalOpen, setIsBookModalOpen] = useState(false);
  const [isTimerSettingModalOpen, setIsTimerSettingModalOpen] = useState(false);
  const [targetSeconds, setTargetSeconds] = useState<number>();
  const [heatmapRefreshKey, setHeatmapRefreshKey] = useState(0);
  const bookTimeList = useMemo(() => {
    // 조회된 도서별 누적시간 서버 페이지를 최근 기록순으로 연결해 반환한다
    return bookTimeQuery.data?.pages.flatMap((page) => page.data.list) ?? [];
  }, [bookTimeQuery.data]);
  const activeTimer = summary?.activeTimer;
  const selectedBook = getSelectedBook(summary?.currentReadingList, selectedReport);
  const displayedBook = activeTimer ?? selectedBook;
  // 연결 도서가 없는 실행 세션에서는 표지와 도서 안내를 숨긴다
  const isTimerWithoutBook = Boolean(activeTimer && !activeTimer.bookTitl);
  // 목표시간 설정과 현재 세션 상태를 반영한 타이머 표시 초를 계산한다
  const timerClockSeconds = getTimerClockSecs(displaySeconds, activeTimer, targetSeconds);

  /**
   * API 응답을 화면 상태와 카운터에 함께 반영한다
   *
   * @author SeungHyeon.Kang
   * @param nextSummary 서버가 계산한 최신 타이머 요약
   */
  const applySummary = useCallback((nextSummary: ReadingTimerSummary) => {

    // 서버 요약을 화면 상태에 설정한다
    setSummary(nextSummary);
    // 내비게이션과 같은 서버 요약을 사용하도록 공통 Query 캐시를 갱신한다
    queryClient.setQueryData(getTimerSummaryOptions().queryKey, nextSummary);
    // 서버 기준 현재 세션 누적 시간을 카운터에 설정한다
    setDisplaySeconds(nextSummary.activeTimer?.readSecs ?? 0);
    // 네비게이션 표시가 상태 변경 응답과 즉시 일치하도록 실행 여부를 알린다
    notifyReadingTimerRunningChange(nextSummary.activeTimer?.tmrxStat === "RUNNING");
  }, []);

  useEffect(() => {

    let ignore = false;
    // 화면 진입 시 서버 기준 타이머 요약을 조회한다
    queryClient.fetchQuery(getTimerSummaryOptions())
      .then((nextSummary) => {

        // 언마운트된 화면에는 응답을 반영하지 않는다
        if (!ignore) {
          // 조회한 타이머 요약을 화면에 반영한다
          applySummary(nextSummary);
        }
      })
      .catch((error) => {

        // 화면이 유지되는 동안 조회 실패 안내를 표시한다
        if (!ignore) {
          // 서버 또는 네트워크 오류 메시지를 사용자에게 표시한다
          void sweetError(message("frontend.timer.error.load"), getApiErrorMessage(error, message("frontend.common.tryAgain")));
        }
      })
      .finally(() => {

        // 화면이 유지되는 동안 로딩 상태를 종료한다
        if (!ignore) {
          // 최초 조회 로딩 상태를 해제한다
          setIsLoading(false);
        }
      });
    // 언마운트 이후 비동기 응답 반영을 막는다
    return () => {
      ignore = true;
    };
  }, [applySummary]);

  useEffect(() => {

    // 실행 중이 아닌 타이머에는 로컬 초 증가를 적용하지 않는다
    if (summary?.activeTimer?.tmrxStat !== "RUNNING") {
      // 타이머가 정지된 상태에서는 별도 정리 함수가 필요하지 않는다
      return undefined;
    }
    /**
     * 서버가 준 누적 시간에서 화면 표시 초를 1초 증가시킨다
     *
     * @author SeungHyeon.Kang
     */
    const tickTimer = () => {
      // 단일 세션 최대 시간을 넘지 않도록 화면 초를 증가시킨다
      setDisplaySeconds((currentSeconds) => Math.min(summary.maxSessionSecs, currentSeconds + 1));
    };
    // 실행 중 화면 카운터를 1초 간격으로 갱신한다
    const intervalId = window.setInterval(tickTimer, 1000);
    // 화면 상태가 바뀌면 기존 카운터를 정리한다
    return () => {
      window.clearInterval(intervalId);
    };
  }, [summary?.activeTimer?.tmrxStat, summary?.maxSessionSecs]);

  /**
   * 도서 선택값으로 새 독서 타이머를 시작한다
   *
   * @author SeungHyeon.Kang
   */
  const startTimer = async () => {

    // 중복 클릭을 막는 처리 상태를 시작한다
    setIsChanging(true);
    try {
      // 선택한 독후감 번호를 숫자로 변환해 시작 요청을 보낸다
      const response = await setReadingTimerApi(selectedBook?.reptNumb, targetSeconds);
      // 시작 결과가 있으면 화면에 반영한다
      if (response.data) {
        // 서버가 반환한 최신 요약을 화면에 설정한다
        applySummary(response.data);
      }
    } catch (error) {
      // 시작 실패 원인을 사용자에게 표시한다
      void sweetError(message("frontend.timer.error.start"), getApiErrorMessage(error, message("frontend.common.tryAgain")));
    } finally {
      // 시작 요청 처리 상태를 종료한다
      setIsChanging(false);
    }
  };

  /**
   * 현재 독서 타이머를 요청한 상태로 변경한다
   *
   * @author SeungHyeon.Kang
   * @param targetStatus 변경할 타이머 상태
   */
  const changeTimer = async (targetStatus: TimerStatus) => {

    const timerNumber = summary?.activeTimer?.tmrxNumb;
    // 현재 세션이 없으면 상태 변경 요청을 보내지 않는다
    if (!timerNumber) {
      // 변경할 타이머가 없는 정상 흐름을 종료한다
      return;
    }
    // 중복 상태 변경을 막는 처리 상태를 시작한다
    setIsChanging(true);
    try {
      // 사용자 소유 세션의 상태 변경을 요청한다
      const response = await uptReadingTimerApi(timerNumber, targetStatus);
      // 변경 결과가 있으면 화면에 반영한다
      if (response.data) {
        // 서버가 반환한 최신 요약을 화면에 설정한다
        applySummary(response.data);

        // 완료된 독서 시간이 잔디에 즉시 반영되도록 갱신 번호를 변경한다
        if (targetStatus === "COMPLETED") {
          // 세션별 목표시간이 다음 독서에 자동으로 재사용되지 않도록 초기화한다
          setTargetSeconds(undefined);
          // 잔디 컴포넌트가 기존 조회 API를 다시 실행하도록 완료 횟수를 증가시킨다
          setHeatmapRefreshKey((currentKey) => currentKey + 1);
          // 새 완료 기록이 최신 페이지와 기존 도서 누적시간에 즉시 반영되도록 첫 페이지부터 다시 조회한다
          void queryClient.resetQueries({ queryKey: queryKeys.readingTimerBookTimes });
        }
      }
    } catch (error) {
      // 상태 변경 실패 원인을 사용자에게 표시한다
      void sweetError(message("frontend.timer.error.change"), getApiErrorMessage(error, message("frontend.common.tryAgain")));
    } finally {
      // 상태 변경 처리 상태를 종료한다
      setIsChanging(false);
    }
  };

  /**
   * 현재 읽는 도서를 선택할 수 있도록 도서 선택 모달을 연다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const openBookModal = (): void => {

    // 타이머 시작 전 도서 선택 모달을 표시한다
    setIsBookModalOpen(true);
  };

  /**
   * 도서 선택을 변경하지 않고 현재 읽는 도서 모달을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const closeBookModal = (): void => {

    // 타이머 화면으로 돌아가도록 도서 선택 모달을 숨긴다
    setIsBookModalOpen(false);
  };

  /**
   * 독서 시작 전 알림 목표시간 설정 모달을 연다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const openTimerSettingModal = (): void => {

    // 타이머 설정 모달을 표시한다
    setIsTimerSettingModalOpen(true);
  };

  /**
   * 타이머 설정값을 변경하지 않고 모달을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const closeTimerSettingModal = (): void => {

    // 타이머 설정 모달을 숨긴다
    setIsTimerSettingModalOpen(false);
  };

  /**
   * 모달에서 선택한 목표시간을 독서 시작 요청값으로 저장한다
   *
   * @author SeungHyeon.Kang
   * @param nextTargetSeconds 선택한 목표 독서 시간 초
   * @return 반환값이 없다
   */
  const saveTimerSetting = (nextTargetSeconds: number): void => {

    // 선택한 목표 독서 시간을 시작 요청 상태에 저장한다
    setTargetSeconds(nextTargetSeconds);
    // 저장 후 타이머 설정 모달을 닫는다
    setIsTimerSettingModalOpen(false);
  };

  /**
   * 도서 선택 모달에서 전달한 독후감 번호를 타이머 연결값으로 설정한다
   *
   * @author SeungHyeon.Kang
   * @param reportNumber 선택한 독후감 번호 문자열
   * @return 반환값이 없다
   */
  const selectBook = (reportNumber: string): void => {

    // 선택한 현재 읽는 도서를 타이머 시작 화면에 반영한다
    setSelectedReport(reportNumber);
  };

  /**
   * 실행 중인 독서 타이머를 일시정지한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const pauseTimer = (): void => {

    // 실행 중인 세션을 일시정지 상태로 변경한다
    void changeTimer("PAUSED");
  };

  /**
   * 일시정지한 독서 타이머를 다시 실행한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const resumeTimer = (): void => {

    // 일시정지한 세션을 실행 상태로 변경한다
    void changeTimer("RUNNING");
  };

  /**
   * 현재 독서 타이머를 완료 처리한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const completeTimer = (): void => {

    // 현재 세션의 누적 시간을 확정하고 완료 상태로 변경한다
    void changeTimer("COMPLETED");
  };

  const todaySeconds = useMemo(() => {
    // 실행 중 세션의 오늘 표시 시간에는 로컬 카운터 증가분을 반영한다
    if (activeTimer?.tmrxStat === "RUNNING") {
      const liveDifference = Math.max(0, displaySeconds - activeTimer.readSecs);
      // 서버 오늘 누적에 현재 화면 증가분을 더해 반환한다
      return (summary?.todayReadSecs ?? 0) + liveDifference;
    }
    // 정지 상태에서는 서버가 확정한 오늘 누적을 반환한다
    return summary?.todayReadSecs ?? 0;
  }, [activeTimer, displaySeconds, summary?.todayReadSecs]);

  const liveAttendanceAchieved = todaySeconds >= (summary?.attendanceMinSecs ?? 600);
  const displayedAttendanceCount = (summary?.weekAttendanceCount ?? 0)
    + (summary?.weekList.some((day) => day.today && !day.attended) && liveAttendanceAchieved ? 1 : 0);

  // 최초 조회 중에는 공통 로딩 화면을 반환한다
  if (isLoading) {
    // 독서 타이머 로딩 화면을 표시한다
    return <Loading />;
  }

  return (
    <main className={styles.page}>
      {/* 독서 타이머 페이지 설명 영역 */}
      <section className={styles.intro} aria-labelledby="reading-timer-description">
        <p id="reading-timer-description" className={styles.description}>
          {/* "하루 10분을 읽으면 이번 주 출석이 채워져요." */}
          {message("frontend.timer.description")}
        </p>
      </section>

      {/* 이번 주 출석 현황 */}
      <section className={styles.card} aria-labelledby="timer-week-title">
        <div className={styles.weekHeader}>
          <h2 id="timer-week-title" className={styles.cardTitle}>{message("frontend.timer.week.title")}</h2>
          <span className={styles.weekCount}>{message("frontend.timer.week.count", [displayedAttendanceCount])}</span>
        </div>
        <div className={styles.weekGrid}>
          {summary?.weekList.map((day, index) => (
            <div key={day.readDate} className={clsx(styles.day, (day.attended || (day.today && liveAttendanceAchieved)) && styles.attendedDay, day.today && styles.todayDay)}>
              {/* "일"~"토" */}
              <span className={styles.dayName}>{message(`frontend.common.week.${DAY_MESSAGE_KEYS[index]}`)}</span>
              <span className={styles.dayMark} aria-label={day.attended || (day.today && liveAttendanceAchieved) ? message("frontend.timer.attended") : message("frontend.timer.notAttended")}>
                {day.attended || (day.today && liveAttendanceAchieved) ? "✓" : "·"}
              </span>
              <span className={styles.dayMinutes}>{Math.floor(day.readSecs / 60)}m</span>
            </div>
          ))}
        </div>
      </section>

      {/* 도서 표지와 독서 타이머 실행 영역 */}
      <section className={styles.timerCard} aria-label={message("frontend.timer.section.label")}>
        <div className={clsx(styles.timerLayout, isTimerWithoutBook && styles.timerLayoutWithoutBook)}>
          {/* 선택 도서 표지와 도서 선택 진입 영역 */}
          <div className={clsx(styles.bookCoverColumn, isTimerWithoutBook && styles.bookCoverColumnHidden)}>
            {!isTimerWithoutBook && (
              !activeTimer ? (
                <button
                  className={displayedBook
                    ? styles.bookCoverButton
                    : styles.emptyBookCoverButton}
                  type="button"
                  aria-label={displayedBook
                    ? message("frontend.timer.book.change")
                    : message("frontend.timer.book.select")}
                  onClick={openBookModal}
                >
                  {displayedBook ? (
                    <img
                      className={styles.coverImage}
                      src={getBookCoverImageSource(displayedBook.bookCvim)}
                      onError={handleBookCoverImageError}
                      alt={displayedBook.bookTitl ?? message("frontend.timer.book.none")}
                    />
                  ) : (
                    <SearchBookButtonContent>
                      {/* "도서 선택" */}
                      {message("frontend.timer.book.choose")}
                    </SearchBookButtonContent>
                  )}
                  {displayedBook && (
                    <span className={styles.coverActionLabel}>
                      {/* "도서 변경" */}
                      {message("frontend.timer.book.change")}
                    </span>
                  )}
                </button>
              ) : (
                <div className={styles.bookCoverFrame}>
                  <img
                    className={styles.coverImage}
                    src={getBookCoverImageSource(displayedBook?.bookCvim)}
                    onError={handleBookCoverImageError}
                    alt={displayedBook?.bookTitl ?? ""}
                  />
                </div>
              )
            )}
          </div>

          {/* 타이머 시간과 도서 및 실행 버튼 영역 */}
          <div className={clsx(styles.timerPanel, isTimerWithoutBook && styles.timerPanelWithoutBook)}>
            <p className={styles.clock} aria-live="off">
              {formatSeconds(timerClockSeconds)}
            </p>
            {!isTimerWithoutBook && (
              <p className={styles.book}>
                {displayedBook?.bookTitl
                  ?? /* "읽고 있는 도서를 선택해주세요" */ message("frontend.timer.book.suggest")}
              </p>
            )}
            {/* 타이머 시작과 상태 변경 버튼 영역 */}
            <div className={clsx(styles.actions, isTimerWithoutBook && styles.actionsWithoutBook)}>
              {!activeTimer && (
                <ActionButton
                  variant="secondary"
                  size="lg"
                  className={styles.actionButton}
                  disabled={isChanging}
                  onClick={openTimerSettingModal}
                >
                  {targetSeconds ? (
                    <>
                      {/* "타이머 {0}" */}
                      {message("frontend.timer.setting.buttonValue", [formatTimerSetting(targetSeconds)])}
                    </>
                  ) : (
                    <>
                      {/* "타이머 설정" */}
                      {message("frontend.timer.setting.button")}
                    </>
                  )}
                </ActionButton>
              )}
              {!activeTimer && (
                <ActionButton
                  variant="primary"
                  size="lg"
                  className={styles.actionButton}
                  disabled={isChanging}
                  onClick={startTimer}
                >
                  {/* "독서 시작" */}
                  {message("frontend.timer.start")}
                </ActionButton>
              )}
              {activeTimer?.tmrxStat === "RUNNING" && (
                <ActionButton variant="secondary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={pauseTimer}>
                  {/* "일시정지" */}
                  {message("frontend.timer.pause")}
                </ActionButton>
              )}
              {activeTimer?.tmrxStat === "PAUSED" && (
                <ActionButton variant="secondary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={resumeTimer}>
                  {/* "이어 읽기" */}
                  {message("frontend.timer.resume")}
                </ActionButton>
              )}
              {activeTimer && (
                <ActionButton
                  variant="primary"
                  size="lg"
                  className={styles.actionButton}
                  disabled={isChanging}
                  onClick={completeTimer}
                >
                  {/* "완료" */}
                  {message("frontend.timer.complete")}
                </ActionButton>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 오늘 누적 및 출석 기준 */}
      <section className={styles.card} aria-labelledby="timer-today-title">
        <h2 id="timer-today-title" className={styles.cardTitle}>{message("frontend.timer.today.title")}</h2>
        <p className={styles.empty}>{message("frontend.timer.today.summary", [Math.floor(todaySeconds / 60), Math.floor((summary?.attendanceMinSecs ?? 600) / 60)])}</p>
      </section>

      {/* 연도별 독서 시간 잔디 영역 */}
      <TimerReadingHeatmap refreshKey={heatmapRefreshKey} />

      {/* 타이머로 기록한 도서별 누적 독서 시간 영역 */}
      <section className={styles.bookTimeCard} aria-labelledby="timer-book-time-title">
        <h2 id="timer-book-time-title" className={styles.cardTitle}>
          {/* "도서별 누적 독서 시간" */}
          {message("frontend.timer.bookTime.title")}
        </h2>
        {bookTimeQuery.isPending ? (
          /* 도서별 누적시간 최초 페이지 조회 링 영역 */
          <Loading isCompact />
        ) : bookTimeQuery.isError ? (
          /* 도서별 누적시간 조회 실패와 재시도 영역 */
          <div className={styles.heatmapError} role="alert">
            <span>
              {getApiErrorMessage(bookTimeQuery.error, message("frontend.timer.error.load"))}
            </span>
            <button
              className={styles.heatmapRetry}
              type="button"
              onClick={() => {
                // 실패한 도서별 누적시간 첫 페이지를 다시 조회한다
                void bookTimeQuery.refetch();
              }}
            >
              {/* "다시 시도" */}
              {message("frontend.common.retry")}
            </button>
          </div>
        ) : bookTimeList.length > 0 ? (
          /* 최근 타이머 기록순 도서별 누적시간 목록 영역 */
          <div className={styles.bookTimeList}>
            {bookTimeList.map(renderBookTimeItem)}
            <InfiniteScrollTrigger
              hasNext={Boolean(bookTimeQuery.hasNextPage)}
              isLoading={bookTimeQuery.isFetchingNextPage}
              onLoadMore={() => {
                // 목록 하단에 도달하면 다음 도서별 누적시간 20건을 조회한다
                void bookTimeQuery.fetchNextPage();
              }}
            >
              <Loading isCompact />
            </InfiniteScrollTrigger>
          </div>
        ) : (
          <p className={styles.bookTimeEmpty}>
            {/* "아직 도서와 함께 완료한 타이머 기록이 없어요." */}
            {message("frontend.timer.bookTime.empty")}
          </p>
        )}
      </section>

      {/* 현재 읽는 도서 선택 모달 영역 */}
      {isBookModalOpen && !activeTimer && (
        <ReadingBookModal
          books={summary?.currentReadingList ?? []}
          selectedReport={selectedReport}
          onSelect={selectBook}
          onClose={closeBookModal}
        />
      )}

      {/* 알림 목표 독서시간 설정 모달 영역 */}
      {isTimerSettingModalOpen && !activeTimer && (
        <TimerSettingModal
          targetSeconds={targetSeconds}
          maxSeconds={summary?.maxSessionSecs ?? 28800}
          onSave={saveTimerSetting}
          onClose={closeTimerSettingModal}
        />
      )}
    </main>
  );
}
