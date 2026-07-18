import { isAxiosError } from "axios";

export const RESULT_SUCCESS_CODE = 200;

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
    return error.response?.data?.message ?? fallbackMessage;
  }

  return fallbackMessage;
}
