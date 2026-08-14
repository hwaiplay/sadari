import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type TimerStatus = "RUNNING" | "PAUSED" | "COMPLETED";

export type ReadingTimer = {
  tmrxNumb: number;
  reptNumb?: number;
  bookTitl?: string;
  bookCvim?: string;
  tmrxStat: TimerStatus;
  strtDate: string;
  lastStrt?: string;
  endxDate?: string;
  readSecs: number;
};

export type ReadingTimerDaily = {
  readDate: string;
  readSecs: number;
  attended: boolean;
  today: boolean;
};

export type ReadingTimerSummary = {
  activeTimer?: ReadingTimer;
  weekStart: string;
  weekEnd: string;
  serverDate: string;
  todayReadSecs: number;
  attendanceMinSecs: number;
  maxSessionSecs: number;
  weekAttendanceCount: number;
  weekList: ReadingTimerDaily[];
  currentReadingList: ReadingTimer[];
  recentSessionList: ReadingTimer[];
};

/**
 * 현재 독서 타이머와 이번 주 출석 현황을 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 독서 타이머 화면 요약
 */
export async function getReadingTimerSummaryApi() {

  // 서버가 계산한 타이머와 출석 현황을 조회한다
  const response = await api.get<ResultData<ReadingTimerSummary>>("/reading-timer/summary");
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}

/**
 * 선택한 읽는 중 도서와 연결하거나 도서 없이 타이머를 시작한다
 *
 * @author SeungHyeon.Kang
 * @param reptNumb 연결할 독후감 번호
 * @return 시작 후 독서 타이머 화면 요약
 */
export async function setReadingTimerApi(reptNumb?: number) {

  // 선택한 독후감 번호를 시작 요청에 전달한다
  const response = await api.post<ResultData<ReadingTimerSummary>>("/reading-timer/sessions", { reptNumb });
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}

/**
 * 독서 타이머를 재개, 일시정지 또는 완료 처리한다
 *
 * @author SeungHyeon.Kang
 * @param tmrxNumb 변경할 타이머 세션 번호
 * @param tmrxStat 변경할 타이머 상태
 * @return 상태 변경 후 독서 타이머 화면 요약
 */
export async function uptReadingTimerApi(tmrxNumb: number, tmrxStat: TimerStatus) {

  // 사용자 소유 세션의 목표 상태를 서버에 전달한다
  const response = await api.patch<ResultData<ReadingTimerSummary>>(`/reading-timer/sessions/${tmrxNumb}`, { tmrxStat });
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}
