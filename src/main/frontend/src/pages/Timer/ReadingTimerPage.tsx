import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import CustomSelect, { type CustomSelectOption } from "@/components/Select/CustomSelect";
import {
  getReadingTimerSummaryApi,
  setReadingTimerApi,
  uptReadingTimerApi,
  type ReadingTimerSummary,
  type TimerStatus,
} from "@/features/Timer/api/readingTimerApi";
import { notifyReadingTimerRunningChange } from "@/features/Timer/lib/readingTimerEvents";
import clsx from "clsx";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import * as styles from "./ReadingTimerPage.css";

const DAY_MESSAGE_KEYS = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"] as const;

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
 * 타이머 상태에 맞는 사용자 안내 문구를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param status 현재 타이머 상태
 * @return 상태 안내 문구
 */
function getStatusLabel(status?: TimerStatus) {

  // 진행 중과 일시정지 상태에 맞는 번역 문구를 반환한다
  return status === "RUNNING"
    ? message("frontend.timer.status.running")
    : message("frontend.timer.status.paused");
}

/**
 * 독서 타이머 실행과 주간 출석 현황을 한 화면에서 제공한다
 *
 * @author SeungHyeon.Kang
 * @return 독서 타이머 화면
 */
export default function ReadingTimerPage() {

  const [summary, setSummary] = useState<ReadingTimerSummary>();
  const [selectedReport, setSelectedReport] = useState("");
  const [displaySeconds, setDisplaySeconds] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isChanging, setIsChanging] = useState(false);

  /**
   * API 응답을 화면 상태와 카운터에 함께 반영한다
   *
   * @author SeungHyeon.Kang
   * @param nextSummary 서버가 계산한 최신 타이머 요약
   */
  const applySummary = useCallback((nextSummary: ReadingTimerSummary) => {

    // 서버 요약을 화면 상태에 설정한다
    setSummary(nextSummary);
    // 서버 기준 현재 세션 누적 시간을 카운터에 설정한다
    setDisplaySeconds(nextSummary.activeTimer?.readSecs ?? 0);
    // 네비게이션 표시가 상태 변경 응답과 즉시 일치하도록 실행 여부를 알린다
    notifyReadingTimerRunningChange(nextSummary.activeTimer?.tmrxStat === "RUNNING");
  }, []);

  useEffect(() => {

    let ignore = false;
    // 화면 진입 시 서버 기준 타이머 요약을 조회한다
    getReadingTimerSummaryApi()
      .then((response) => {

        // 언마운트된 화면에는 응답을 반영하지 않는다
        if (!ignore && response.data) {
          // 조회한 타이머 요약을 화면에 반영한다
          applySummary(response.data);
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
      const response = await setReadingTimerApi(selectedReport ? Number(selectedReport) : undefined);
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
      }
    } catch (error) {
      // 상태 변경 실패 원인을 사용자에게 표시한다
      void sweetError(message("frontend.timer.error.change"), getApiErrorMessage(error, message("frontend.common.tryAgain")));
    } finally {
      // 상태 변경 처리 상태를 종료한다
      setIsChanging(false);
    }
  };

  const activeTimer = summary?.activeTimer;
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
  const bookOptions = useMemo<readonly CustomSelectOption<string>[]>(() => [
    { value: "", label: message("frontend.timer.book.without") },
    ...(summary?.currentReadingList.map((bookItem) => ({
      value: String(bookItem.reptNumb),
      label: bookItem.bookTitl ?? message("frontend.timer.book.none"),
    })) ?? []),
  ], [summary?.currentReadingList]);

  // 최초 조회 중에는 공통 로딩 화면을 반환한다
  if (isLoading) {
    // 독서 타이머 로딩 화면을 표시한다
    return <Loading title={message("frontend.timer.loading")} />;
  }

  return (
    <main className={styles.page}>
      {/* 독서 습관 안내 영역 */}
      <section className={styles.intro} aria-label="독서 습관 안내">
        <img className={styles.introIcon} src="/img/icons/icon-megaphone.svg" alt="" aria-hidden="true" />
        <p className={styles.description}>
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
              <span className={styles.dayName}>{message(`frontend.timer.day.${DAY_MESSAGE_KEYS[index]}`)}</span>
              <span className={styles.dayMark} aria-label={day.attended || (day.today && liveAttendanceAchieved) ? message("frontend.timer.attended") : message("frontend.timer.notAttended")}>
                {day.attended || (day.today && liveAttendanceAchieved) ? "✓" : "·"}
              </span>
              <span className={styles.dayMinutes}>{Math.floor(day.readSecs / 60)}m</span>
            </div>
          ))}
        </div>
      </section>

      {/* 현재 독서 타이머 */}
      <section className={styles.timerCard} aria-labelledby="timer-current-title">
        <h2 id="timer-current-title" className={styles.cardTitle}>{message("frontend.timer.current.title")}</h2>
        <span className={styles.status}>{activeTimer ? getStatusLabel(activeTimer.tmrxStat) : message("frontend.timer.status.ready")}</span>
        <p className={styles.clock} aria-live="off">{formatSeconds(activeTimer ? displaySeconds : todaySeconds)}</p>
        <p className={styles.book}>{activeTimer?.bookTitl ?? message("frontend.timer.book.suggest")}</p>
        {!activeTimer && Boolean(summary?.currentReadingList.length) && (
          <CustomSelect<string>
            value={selectedReport}
            options={bookOptions}
            ariaLabel={message("frontend.timer.book.select")}
            className={styles.bookSelect}
            triggerClassName={styles.bookSelectTrigger}
            optionListClassName={styles.bookOptionList}
            optionClassName={styles.bookOption}
            onChange={setSelectedReport}
          />
        )}
        <div className={styles.actions}>
          {!activeTimer && (
            <ActionButton variant="primary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={startTimer}>
              {/* "독서 시작" */}
              {message("frontend.timer.start")}
            </ActionButton>
          )}
          {activeTimer?.tmrxStat === "RUNNING" && (
            <ActionButton variant="secondary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={() => changeTimer("PAUSED")}>
              {/* "일시정지" */}
              {message("frontend.timer.pause")}
            </ActionButton>
          )}
          {activeTimer?.tmrxStat === "PAUSED" && (
            <ActionButton variant="secondary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={() => changeTimer("RUNNING")}>
              {/* "이어 읽기" */}
              {message("frontend.timer.resume")}
            </ActionButton>
          )}
          {activeTimer && (
            <ActionButton variant="primary" size="lg" className={styles.actionButton} disabled={isChanging} onClick={() => changeTimer("COMPLETED")}>
              {/* "완료" */}
              {message("frontend.timer.complete")}
            </ActionButton>
          )}
        </div>
      </section>

      {/* 오늘 누적 및 출석 기준 */}
      <section className={styles.card} aria-labelledby="timer-today-title">
        <h2 id="timer-today-title" className={styles.cardTitle}>{message("frontend.timer.today.title")}</h2>
        <p className={styles.empty}>{message("frontend.timer.today.summary", [Math.floor(todaySeconds / 60), Math.floor((summary?.attendanceMinSecs ?? 600) / 60)])}</p>
      </section>

      {/* 오늘 완료된 타이머 */}
      <section className={styles.card} aria-labelledby="timer-recent-title">
        <h2 id="timer-recent-title" className={styles.cardTitle}>{message("frontend.timer.recent.title")}</h2>
        {summary?.recentSessionList.length ? (
          <ul className={styles.recentList}>
            {summary.recentSessionList.map((session) => (
              <li key={session.tmrxNumb} className={styles.recentItem}>
                <span className={styles.recentBook}>
                  {session.reptNumb && session.bookTitl ? (
                    <Link className={styles.recentBookLink} to={`/report/detail/${session.reptNumb}`}>
                      {session.bookTitl}
                    </Link>
                  ) : message("frontend.timer.book.none")}
                </span>
                <span className={styles.recentTime}>{formatSeconds(session.readSecs)}</span>
              </li>
            ))}
          </ul>
        ) : <p className={styles.empty}>{message("frontend.timer.recent.empty")}</p>}
      </section>
    </main>
  );
}
