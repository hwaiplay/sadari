import api, { type SadariRequestConfig } from "@/app/api/axios";
import {
  assertResultDataSuccess,
  type PageData,
  type ResultData,
} from "@/app/api/resultData";

export type TimerStatus = "RUNNING" | "PAUSED" | "COMPLETED";

export type ReadingTimer = {
  tmrxNumb: number;
  reptNumb?: number;
  bookTitl?: string;
  bookCvim?: string;
  tmrxStat: TimerStatus;
  targSecs?: number;
  alrmDate?: string;
  sendDate?: string;
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

export type ReadingTimerBookTime = {
  reptNumb: number;
  bookNumb: number;
  bookTitl: string;
  bookAthr?: string;
  bookCvim?: string;
  readSecs: number;
  lastReadDate: string;
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
export async function getReadingTimerSummaryApi(): Promise<
  ResultData<ReadingTimerSummary> & { data: ReadingTimerSummary }
> {

  // 서버가 계산한 타이머와 출석 현황을 조회한다
  const response = await api.get<ResultData<ReadingTimerSummary> & { data: ReadingTimerSummary }>(
    "/reading-timer/summary",
  );
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}

/**
 * 도서별 누적 독서 시간을 최근 완료 기록순으로 20권씩 조회한다
 *
 * @author SeungHyeon.Kang
 * @param page 조회할 페이지 번호
 * @return 도서별 누적 독서 시간 페이지 응답
 */
export async function getBookTimePageApi(page: number): Promise<
  ResultData<PageData<ReadingTimerBookTime>> & { data: PageData<ReadingTimerBookTime> }
> {

  // 서버가 제한한 도서별 누적 독서 시간 페이지를 조회한다
  const response = await api.get<
    ResultData<PageData<ReadingTimerBookTime>> & { data: PageData<ReadingTimerBookTime> }
  >("/reading-timer/book-times", { params: { page } });
  // 공통 응답 성공 여부를 검증한 페이지 데이터를 반환한다
  return assertResultDataSuccess(response.data);
}

/**
 * 선택한 읽는 중 도서와 연결하거나 도서 없이 타이머를 시작한다
 *
 * @author SeungHyeon.Kang
 * @param reptNumb 연결할 독후감 번호
 * @param targSecs 알림 목표 독서 시간 초
 * @return 시작 후 독서 타이머 화면 요약
 */
export async function setReadingTimerApi(reptNumb?: number, targSecs?: number) {

  // 선택한 독후감 번호와 알림 목표시간을 시작 요청에 전달한다
  const response = await api.post<ResultData<ReadingTimerSummary>>("/reading-timer/sessions", { reptNumb, targSecs });
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}

/**
 * 독서 타이머를 재개, 일시정지 또는 완료 처리한다
 *
 * @author SeungHyeon.Kang
 * @param tmrxNumb 변경할 타이머 세션 번호
 * @param tmrxStat 변경할 타이머 상태
 * @param skipBlockingOperation 공통 처리 중 알림과 이동 차단 제외 여부
 * @return 상태 변경 후 독서 타이머 화면 요약
 */
export async function uptReadingTimerApi(tmrxNumb: number, tmrxStat: TimerStatus, skipBlockingOperation = false) {

  // 호출 화면의 즉시 제어 정책을 공통 Axios 처리 중 화면 설정에 전달한다
  const requestConfig: SadariRequestConfig = { skipBlockingOperation };
  // 사용자 소유 세션의 목표 상태를 서버에 전달한다
  const response = await api.patch<ResultData<ReadingTimerSummary>>(
    `/reading-timer/sessions/${tmrxNumb}`,
    { tmrxStat },
    requestConfig,
  );
  // 공통 응답 성공 여부를 검증한 결과를 반환한다
  return assertResultDataSuccess(response.data);
}
