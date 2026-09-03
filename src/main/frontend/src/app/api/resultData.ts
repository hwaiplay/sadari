import { isAxiosError } from "axios";
import { message } from "@/app/messages/message";

export const RESULT_SUCCESS_CODE = 200;
export const DB_CONNECTION_FAILED_CODE = 2014;
export const BAD_WORD_INCLUDED_CODE = 2015;
export const COMPLAINT_DUPLICATED_CODE = 2027;

export type ResultData<T = unknown> = {
  code?: number;
  message?: string;
  data?: T;
};

export type PageData<T> = {
  list: T[];
  page: number;
  hasNext: boolean;
};

export class ResultDataError extends Error {
  result: ResultData;

  /**
   * 공통 응답 오류 객체를 초기화함
   *
   * @author HanWon.Jang
   * @param result result 입력값
   * @return 처리 결과
   */
  constructor(result: ResultData) {

    super(result.message);
    this.name = "ResultDataError";
    this.result = result;
  }
}

/**
 * assert Result Data Success 기능을 처리함
 *
 * @author HanWon.Jang
 * @param result result 입력값
 * @return 처리 결과
 */
export function assertResultDataSuccess<T extends ResultData>(result: T): T {

  if (Number(result?.code) !== RESULT_SUCCESS_CODE) {
    throw new ResultDataError(result);
  }

  return result;
}

/**
 * get Api Error Message 정보를 조회함
 *
 * @author HanWon.Jang
 * @param error error 입력값
 * @param fallbackMessage fallback Message 입력값
 * @return 처리 결과
 */
export const getApiErrorMessage = (error: unknown, fallbackMessage: string): string => {
  // 공통 응답 검증에서 확인한 업무 실패는 서버가 제공한 메시지를 우선 사용함
  if (error instanceof ResultDataError) {
    // 비어 있는 서버 메시지는 화면별 공통 오류 문구로 보정함
    return error.message || fallbackMessage;
  }

  // Axios 응답은 서버가 명시한 공통 실패 코드와 메시지를 기준으로 안내함
  if (isAxiosError<ResultData>(error)) {
    const resultCode = Number(error.response?.data?.code);

    // 서버가 JDBC 연결 실패로 확정한 경우에만 데이터베이스 전용 문구를 사용함
    if (resultCode === DB_CONNECTION_FAILED_CODE) {
      // "데이터베이스에 연결할 수 없어요. 잠시 후 다시 시도해주세요."
      return message("frontend.common.databaseConnectionFailed");
    }

    // 일반 5xx와 타임아웃 및 네트워크 오류에는 서버 메시지나 화면별 대체 문구를 사용함
    return error.response?.data?.message ?? fallbackMessage;
  }

  // API 응답으로 분류할 수 없는 오류에는 화면별 공통 오류 문구를 사용함
  return fallbackMessage;
};
