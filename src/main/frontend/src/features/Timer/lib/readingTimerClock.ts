import type { ReadingTimerSummary } from "@/features/Timer/api/readingTimerApi";

const MILLIS_PER_SECOND = 1000;

/**
 * 서버 타이머 요약에 브라우저 수신 기준 시각을 한 번만 기록한다
 *
 * @author SeungHyeon.Kang
 * @param summary 서버가 계산한 타이머 요약
 * @return 클라이언트 기준 시각이 포함된 타이머 요약
 */
export function syncTimerSummary(summary: ReadingTimerSummary): ReadingTimerSummary {
  // 캐시에서 다시 사용한 요약은 최초 수신 시각을 유지해 누락된 경과시간을 보존한다
  if (summary.clientSyncMillis !== undefined) {
    // 이미 동기화된 타이머 요약을 그대로 반환한다
    return summary;
  }

  // 서버 응답을 받은 브라우저 기준 시각을 현재 타이머 계산 기준으로 조회한다
  const clientSyncMillis = Date.now();
  // 서버 누적시간과 브라우저 경과시간을 함께 계산할 새 요약을 반환한다
  return { ...summary, clientSyncMillis };
}

/**
 * 서버 누적시간과 실제 브라우저 경과시간으로 실행 중인 타이머 초를 계산한다
 *
 * @author SeungHyeon.Kang
 * @param summary 클라이언트 기준 시각이 포함된 타이머 요약
 * @param currentMillis 계산 기준 브라우저 시각
 * @return 최대 세션 시간을 적용한 현재 타이머 누적 초
 */
export function getLiveTimerSecs(summary: ReadingTimerSummary | undefined, currentMillis?: number): number {
  const activeTimer = summary?.activeTimer;
  // 활성 세션이 없으면 화면에 표시할 누적시간도 없는 상태로 반환한다
  if (!activeTimer) {
    // 타이머 시작 전 표시값을 0초로 반환한다
    return 0;
  }

  // 일시정지 세션이나 동기화 전 응답은 서버가 확정한 누적시간을 그대로 사용한다
  if (activeTimer.tmrxStat !== "RUNNING" || summary.clientSyncMillis === undefined) {
    // 서버에 저장되었거나 응답 시점에 계산된 누적시간을 반환한다
    return activeTimer.readSecs;
  }

  // 별도 계산 시각이 없으면 현재 브라우저 시각을 실제 경과시간 기준으로 조회한다
  const calculationMillis = currentMillis ?? Date.now();
  // 브라우저 시계가 뒤로 조정되어도 타이머가 감소하지 않도록 양수 경과 초만 계산한다
  const elapsedSeconds = Math.max(0, Math.floor((calculationMillis - summary.clientSyncMillis) / MILLIS_PER_SECOND));
  // 서버 누적시간에 화면이 경과한 초를 더해 현재 세션 시간을 계산한다
  const liveSeconds = activeTimer.readSecs + elapsedSeconds;
  // 백그라운드 복귀 후에도 서버와 같은 단일 세션 최대시간을 적용해 반환한다
  return Math.min(summary.maxSessionSecs, liveSeconds);
}
