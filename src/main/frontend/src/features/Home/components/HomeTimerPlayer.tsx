import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { queryClient } from "@/app/query/queryClient";
import { queryKeys } from "@/app/query/queryKeys";
import { ActionButton } from "@/components/Button/ActionButton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import {
  type ReadingTimer,
  type ReadingTimerSummary,
  type TimerStatus,
  uptReadingTimerApi,
} from "@/features/Timer/api/readingTimerApi";
import { getTimerSummaryOptions, useTimerSummaryQuery } from "@/features/Timer/hooks/useTimerSummaryQuery";
import { getLiveTimerSecs, syncTimerSummary } from "@/features/Timer/lib/readingTimerClock";
import { notifyReadingTimerRunningChange } from "@/features/Timer/lib/readingTimerEvents";
import { clsx } from "clsx";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { Link } from "react-router-dom";
import * as styles from "./HomeTimerPlayer.css";

const PLAYER_EXIT_DURATION_MS = 300;

/**
 * 초 단위 타이머 시간을 시분초 형식으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param totalSeconds 표시할 타이머 시간 초
 * @return 두 자리 시분초 문자열
 */
function formatPlayerTime(totalSeconds: number): string {

  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(safeSeconds / 3600).toString().padStart(2, "0");
  const minutes = Math.floor((safeSeconds % 3600) / 60).toString().padStart(2, "0");
  const seconds = (safeSeconds % 60).toString().padStart(2, "0");
  // 홈 플레이어에 표시할 시분초 문자열을 반환한다
  return `${hours}:${minutes}:${seconds}`;
}

/**
 * 현재 세션의 목표시간 여부에 맞는 홈 플레이어 표시 초를 계산한다
 *
 * @author SeungHyeon.Kang
 * @param timer 홈에 표시하는 활성 타이머
 * @param elapsedSeconds 현재 세션의 누적 독서 시간 초
 * @return 목표시간의 남은 초 또는 누적 독서 시간 초
 */
function getPlayerTimeSeconds(timer: ReadingTimer, elapsedSeconds: number): number {

  // 목표시간이 설정된 세션은 타이머 화면과 동일하게 남은 시간을 표시한다
  if (typeof timer.targSecs === "number") {
    // 목표시간을 넘긴 경우에도 음수 시간이 표시되지 않게 제한해 반환한다
    return Math.max(0, timer.targSecs - elapsedSeconds);
  }

  // 일반 세션은 현재까지 누적한 독서 시간을 반환한다
  return elapsedSeconds;
}

/**
 * 홈의 독후감 등록 버튼 왼쪽에 활성 독서 타이머 제어 플레이어를 표시한다
 *
 * @author SeungHyeon.Kang
 * @return 활성 세션의 홈 타이머 플레이어 또는 비활성 상태의 null
 */
export function HomeTimerPlayer() {

  const timerSummaryQuery = useTimerSummaryQuery();
  const activeTimer = timerSummaryQuery.data?.activeTimer;
  const [closingTimer, setClosingTimer] = useState<ReadingTimer>();
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [isChanging, setIsChanging] = useState(false);
  const [isExiting, setIsExiting] = useState(false);
  const exitTimerRef = useRef<number | undefined>(undefined);
  const visibleTimer = activeTimer ?? closingTimer;

  useEffect(() => {
    // 서버에 활성 세션이 있을 때만 닫힘 애니메이션용 이전 상태를 최신값으로 교체한다
    if (!activeTimer) {
      // 활성 세션이 없는 동안에는 완료 애니메이션의 이전 세션을 유지한다
      return;
    }

    // 캐시 수신 시각부터 실제로 흐른 시간을 포함해 홈 플레이어에 반영한다
    setElapsedSeconds(getLiveTimerSecs(timerSummaryQuery.data));
    // 새 활성 세션에는 이전 완료 애니메이션 상태를 적용하지 않는다
    setClosingTimer(undefined);
    // 활성 세션 플레이어를 정상 위치에 표시한다
    setIsExiting(false);
  }, [activeTimer, timerSummaryQuery.data]);

  useEffect(() => {
    // 실행 상태가 아닌 세션에는 화면용 초 증가를 적용하지 않는다
    if (activeTimer?.tmrxStat !== "RUNNING") {
      // 일시정지 또는 완료 상태에는 정리할 반복 작업이 없다
      return undefined;
    }

    /**
     * 서버가 확정한 누적시간을 기준으로 홈 플레이어 표시 초를 증가시킨다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const tickPlayer = (): void => {

      // interval 지연과 백그라운드 정지를 건너뛰도록 동기화 시각부터 실제 경과 초를 다시 계산한다
      setElapsedSeconds(getLiveTimerSecs(timerSummaryQuery.data));
    };

    // 실행 중인 홈 플레이어를 1초 간격으로 갱신한다
    const intervalId = window.setInterval(tickPlayer, 1000);

    /**
     * 홈 플레이어 상태가 바뀌거나 컴포넌트가 해제되면 초 증가 작업을 정리한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const clearPlayerInterval = (): void => {

      // 더 이상 사용하지 않는 반복 작업을 브라우저에서 제거한다
      window.clearInterval(intervalId);
    };

    // 홈 플레이어 상태 변경 시 실행할 반복 작업 정리 함수를 반환한다
    return clearPlayerInterval;
  }, [activeTimer?.tmrxStat, timerSummaryQuery.data]);

  useEffect(() => {
    /**
     * 홈 화면을 벗어날 때 예약된 플레이어 종료 애니메이션 작업을 정리한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const clearPlayerExit = (): void => {

      // 종료 예약이 있는 경우에만 브라우저 타이머를 제거한다
      if (exitTimerRef.current !== undefined) {
        // 언마운트 뒤 닫힘 상태가 변경되지 않도록 예약을 취소한다
        window.clearTimeout(exitTimerRef.current);
      }
    };

    // 홈 타이머 플레이어가 해제될 때 실행할 종료 예약 정리 함수를 반환한다
    return clearPlayerExit;
  }, []);

  /**
   * 서버의 상태 변경 결과를 홈 플레이어와 공통 타이머 캐시에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param nextSummary 서버가 계산한 최신 타이머 요약
   * @return 반환값이 없다
   */
  const applySummary = (nextSummary: ReadingTimerSummary): void => {

    // 상태 변경 응답에 최초 브라우저 수신 시각을 기록한다
    const syncedSummary = syncTimerSummary(nextSummary);
    // 타이머 화면과 내비게이션이 즉시 같은 결과를 사용하도록 공통 캐시를 갱신한다
    queryClient.setQueryData(getTimerSummaryOptions().queryKey, syncedSummary);
    // 하단 내비게이션의 실행 표시를 서버 상태와 즉시 일치시킨다
    notifyReadingTimerRunningChange(syncedSummary.activeTimer?.tmrxStat === "RUNNING");
  };

  /**
   * 완료 저장이 끝난 홈 타이머 플레이어를 화면 아래로 내린 뒤 제거한다
   *
   * @author SeungHyeon.Kang
   * @param completedTimer 완료 처리 직전의 활성 타이머
   * @return 반환값이 없다
   */
  const closeCompletedPlayer = (completedTimer: ReadingTimer): void => {

    // 서버 응답에서 활성 세션이 사라져도 애니메이션 동안 표시할 세션을 보관한다
    setClosingTimer(completedTimer);
    // 플레이어가 아래로 내려가는 종료 전환을 시작한다
    setIsExiting(true);

    /**
     * 종료 전환이 끝난 플레이어의 이전 세션 정보를 제거한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const finishPlayerExit = (): void => {

      // 종료 애니메이션이 끝난 플레이어를 렌더링에서 제거한다
      setClosingTimer(undefined);
      // 다음 세션이 정상 위치에서 표시되도록 종료 상태를 초기화한다
      setIsExiting(false);
      // 소비한 브라우저 타이머 식별값을 초기화한다
      exitTimerRef.current = undefined;
    };

    // CSS 전환 시간 뒤 이전 타이머 플레이어를 완전히 제거한다
    exitTimerRef.current = window.setTimeout(finishPlayerExit, PLAYER_EXIT_DURATION_MS);
  };

  /**
   * 현재 홈 독서 타이머를 요청한 상태로 변경한다
   *
   * @author SeungHyeon.Kang
   * @param targetStatus 변경할 타이머 상태
   * @return 상태 변경 요청이 끝나면 완료되는 Promise
   */
  const changeTimerStatus = async (targetStatus: TimerStatus): Promise<void> => {

    // 활성 세션이 없거나 상태 변경 중이면 중복 요청을 보내지 않는다
    if (!activeTimer || isChanging) {
      // 처리할 수 없는 현재 상태에서 타이머 제어를 종료한다
      return;
    }

    // 연속 제어 입력을 막도록 홈 플레이어를 처리 중 상태로 변경한다
    setIsChanging(true);
    try {
      // 재생과 일시정지는 플레이어 안에서 즉시 처리하므로 화면 전체 처리 중 알림을 생략한다
      const skipBlockingOperation = targetStatus !== "COMPLETED";
      // 사용자 소유 타이머의 일시정지, 재개 또는 완료 저장을 서버에 요청한다
      const response = await uptReadingTimerApi(activeTimer.tmrxNumb, targetStatus, skipBlockingOperation);

      // 서버가 최신 타이머 요약을 반환한 경우에만 공통 화면 상태를 갱신한다
      if (response.data) {
        // 완료 상태는 캐시에서 활성 세션을 제거하기 전에 애니메이션용 세션을 보관한다
        if (targetStatus === "COMPLETED") {
          // 저장이 끝난 플레이어의 아래 방향 종료 전환을 시작한다
          closeCompletedPlayer(activeTimer);
        }

        // 검증된 서버 상태를 홈, 타이머 및 내비게이션에 함께 반영한다
        applySummary(response.data);

        // 완료 저장은 도서별 누적 독서시간의 기존 페이지를 다음 조회 전에 초기화한다
        if (targetStatus === "COMPLETED") {
          // 최신 완료 세션을 첫 페이지부터 다시 조회하도록 도서별 누적 캐시를 제거한다
          void queryClient.resetQueries({ queryKey: queryKeys.readingTimerBookTimes });
        }
      }
    } catch (error) {
      // "독서 타이머 상태를 변경하지 못했습니다."
      const errorTitle = message("frontend.timer.error.change");
      // "다시 시도해주세요."
      const fallbackMessage = message("frontend.common.tryAgain");
      // "독서 타이머 상태를 변경하지 못했습니다."
      void sweetError(
        errorTitle,
        getApiErrorMessage(error, fallbackMessage),
      );
    } finally {
      // 서버 요청이 끝나면 홈 플레이어 제어를 다시 허용한다
      setIsChanging(false);
    }
  };

  /**
   * 실행 중인 홈 독서 타이머를 일시정지한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const pauseTimer = (): void => {

    // 현재 세션의 측정 구간을 서버에 확정하고 일시정지한다
    void changeTimerStatus("PAUSED");
  };

  /**
   * 일시정지한 홈 독서 타이머를 다시 실행한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const resumeTimer = (): void => {

    // 서버 기준 새 측정 구간을 시작하도록 현재 세션을 재개한다
    void changeTimerStatus("RUNNING");
  };

  /**
   * 홈 독서 타이머를 완료하고 확정 독서시간을 저장한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const completeTimer = (): void => {

    // 마지막 측정 구간을 저장하고 세션을 다시 재개할 수 없는 완료 상태로 변경한다
    void changeTimerStatus("COMPLETED");
  };

  // 활성 세션과 완료 애니메이션용 이전 세션이 모두 없으면 홈에 플레이어를 표시하지 않는다
  if (!visibleTimer) {
    // 비활성 타이머의 홈 플레이어 영역을 렌더링하지 않는다
    return null;
  }

  const displayTime = formatPlayerTime(getPlayerTimeSeconds(visibleTimer, elapsedSeconds));
  // "도서 없이 독서 중"
  const bookTitle = visibleTimer.bookTitl?.trim() || message("frontend.home.timer.bookless");
  const isRunning = visibleTimer.tmrxStat === "RUNNING";

  // 최상위 문서에서 독후감 등록 버튼 왼쪽에 고정되는 홈 타이머 플레이어를 반환한다
  return createPortal(
    /* 홈 활성 독서 타이머 플레이어 영역 */
    <section
      className={clsx(styles.player, isExiting && styles.playerExiting)}
      aria-label={/* "독서 타이머 플레이어" */ message("frontend.home.timer.playerLabel")}
    >
      {/* 타이머 페이지로 이동하는 도서와 시간 정보 영역 */}
      <Link
        className={styles.timerPageLink}
        to="/timer"
        aria-label={/* "타이머 페이지로 이동" */ message("frontend.home.timer.open")}
      >
        {/* 선택한 도서의 표지 영역 */}
        {visibleTimer.bookTitl && (
          <img
            className={styles.bookCover}
            src={getBookCoverImageSource(visibleTimer.bookCvim)}
            onError={handleBookCoverImageError}
            alt=""
            aria-hidden="true"
          />
        )}

        {/* 타이머 시간과 선택 도서 제목 영역 */}
        <div className={styles.timerInfo}>
          <p
            className={styles.timerClock}
            aria-label={/* "타이머 시간 {0}" */ message("frontend.home.timer.time", [displayTime])}
          >
            {displayTime}
          </p>
          <p className={styles.bookTitle}>{bookTitle}</p>
        </div>
      </Link>

      {/* 타이머 일시정지 또는 재생과 정지 저장 영역 */}
      <div className={styles.playerActions}>
        {isRunning ? (
          <ActionButton
            variant="secondary"
            size="sm"
            className={styles.iconButton}
            icon={(
              <svg className={styles.controlIcon} viewBox="0 0 16 16" fill="none">
                <path d="M4.5 3.25v9.5M11.5 3.25v9.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
              </svg>
            )}
            disabled={isChanging || isExiting}
            aria-label={/* "일시정지" */ message("frontend.timer.pause")}
            onClick={pauseTimer}
          />
        ) : (
          <ActionButton
            variant="secondary"
            size="sm"
            className={styles.iconButton}
            icon={(
              <svg className={styles.controlIcon} viewBox="0 0 16 16" fill="none">
                <path d="M5 3.15 12.25 8 5 12.85V3.15Z" fill="currentColor" />
              </svg>
            )}
            disabled={isChanging || isExiting}
            aria-label={/* "이어 읽기" */ message("frontend.timer.resume")}
            onClick={resumeTimer}
          />
        )}
        <ActionButton
          variant="secondary"
          size="sm"
          className={styles.iconButton}
          icon={(
            <svg className={styles.controlIcon} viewBox="0 0 16 16" fill="none">
              <rect x="4" y="4" width="8" height="8" rx="1" fill="#D84A5F" />
            </svg>
          )}
          disabled={isChanging || isExiting}
          aria-label={/* "타이머 정지 및 저장" */ message("frontend.home.timer.stop")}
          onClick={completeTimer}
        />
      </div>
    </section>,
    document.body,
  );
}
