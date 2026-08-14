export const READING_TIMER_RUNNING_CHANGED_EVENT = "sadari:reading-timer-running-changed";

type ReadingTimerRunningChangedEvent = CustomEvent<boolean>;

/**
 * 독서 타이머 실행 여부가 변경됐음을 공통 네비게이션에 알린다
 *
 * @author SeungHyeon.Kang
 * @param isRunning 독서 타이머 실행 여부
 * @return 반환값이 없다
 */
export function notifyReadingTimerRunningChange(isRunning: boolean): void {

  window.dispatchEvent(
    new CustomEvent<boolean>(READING_TIMER_RUNNING_CHANGED_EVENT, {
      detail: isRunning,
    }),
  );
}

/**
 * 전달된 이벤트가 독서 타이머 실행 상태 변경 이벤트인지 판정한다
 *
 * @author SeungHyeon.Kang
 * @param event 판정할 브라우저 이벤트
 * @return 독서 타이머 실행 상태 변경 이벤트 여부
 */
export function isReadingTimerRunningChangeEvent(
  event: Event,
): event is ReadingTimerRunningChangedEvent {

  // 이벤트 이름과 상세값 형식을 함께 검증한 판정 결과를 반환한다
  return event.type === READING_TIMER_RUNNING_CHANGED_EVENT
    && "detail" in event
    && typeof event.detail === "boolean";
}
