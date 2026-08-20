import { isAxiosError } from "axios";
import { message } from "@/app/messages/message";

export const RESULT_SUCCESS_CODE = 200;
export const DB_CONNECTION_FAILED_CODE = 2014;
export const BAD_WORD_INCLUDED_CODE = 2015;

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
   * 공통 응답 오류 객체를 초기화한다
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
 * assert Result Data Success 기능을 처리한다
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
 * get Api Error Message 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param error error 입력값
 * @param fallbackMessage fallback Message 입력값
 * @return 처리 결과
 */
export function getApiErrorMessage(error: unknown, fallbackMessage: string) {

  if (error instanceof ResultDataError) {
    return error.message || fallbackMessage;
  }

  if (isAxiosError<ResultData>(error)) {
    const resultCode = Number(error.response?.data?.code);

    /*
     * 서버가 DB 연결 실패를 ResultData로 내려준 경우와 브라우저가 1분 timeout으로 요청을 끊은 경우 모두
     * 사용자에게 같은 원인 메시지를 보여준다. timeout은 서버가 응답하지 못하는 대표 케이스라 DB 장애 화면과 같은 문구로 안내한다.
     */
    if (resultCode === DB_CONNECTION_FAILED_CODE || error.code === "ECONNABORTED") {
      // "데이터베이스에 연결할 수 없어요. 잠시 후 다시 시도해주세요."
      return message("frontend.common.databaseConnectionFailed");
    }

    return error.response?.data?.message ?? fallbackMessage;
  }

  return fallbackMessage;
}
