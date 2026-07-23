import { isAxiosError } from "axios";

export const RESULT_SUCCESS_CODE = 200;
export const DB_CONNECTION_FAILED_CODE = 2014;

export type ResultData<T = unknown> = {
  code?: number;
  message?: string;
  data?: T;
};

export class ResultDataError extends Error {
  result: ResultData;

  constructor(result: ResultData) {
    super(result.message);
    this.name = "ResultDataError";
    this.result = result;
  }
}

export function assertResultDataSuccess<T extends ResultData>(result: T): T {
  if (Number(result?.code) !== RESULT_SUCCESS_CODE) {
    throw new ResultDataError(result);
  }

  return result;
}

export function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ResultDataError) {
    return error.message || fallbackMessage;
  }

  if (isAxiosError<{ message?: string }>(error)) {
    const resultCode = Number(error.response?.data?.code);

    /*
     * 서버가 DB 연결 실패를 ResultData로 내려준 경우와 브라우저가 1분 timeout으로 요청을 끊은 경우 모두
     * 사용자에게 같은 원인 메시지를 보여준다. timeout은 서버가 응답하지 못하는 대표 케이스라 DB 장애 화면과 같은 문구로 안내한다.
     */
    if (resultCode === DB_CONNECTION_FAILED_CODE || error.code === "ECONNABORTED") {
      return "데이터베이스에 연결할 수 없어요.\n잠시 후 다시 시도해주세요.";
    }

    return error.response?.data?.message ?? fallbackMessage;
  }

  return fallbackMessage;
}
