import { isAxiosError } from "axios";
import {
  DB_CONNECTION_FAILED_CODE,
  type ResultData,
} from "@/app/api/resultData";

type ConnectionListener = () => void;

let isConnectionUnstable = !navigator.onLine;
const connectionListeners = new Set<ConnectionListener>();

/**
 * 연결 장애 상태를 구독한 단일 화면에 변경 사실을 전달한다
 *
 * @author HanWon.Jang
 * @param listener 연결 상태가 변경될 때 실행할 구독 함수
 * @return 반환값이 없다
 */
const notifyConnection = (listener: ConnectionListener): void => {
  // 연결 장애 화면이 즉시 다시 렌더링되도록 구독 함수를 실행한다
  listener();
};

/**
 * 전역 연결 장애 화면이 구독할 현재 연결 상태를 조회한다
 *
 * @author HanWon.Jang
 * @return JDBC 또는 인터넷 연결이 불안정한지 여부
 */
export const getConnectionStatus = (): boolean => {
  // 마지막으로 감지한 전역 연결 장애 상태를 반환한다
  return isConnectionUnstable;
};

/**
 * 전역 연결 장애 상태 변경을 화면 구독자에게 전달한다
 *
 * @author HanWon.Jang
 * @param listener 연결 상태가 변경될 때 실행할 구독 함수
 * @return 연결 상태 구독 해제 함수
 */
export const subscribeConnection = (listener: ConnectionListener): (() => void) => {
  // 연결 장애 상태가 바뀔 때 화면을 갱신할 구독 함수를 등록한다
  connectionListeners.add(listener);

  /**
   * 현재 화면의 연결 상태 구독을 해제한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const unsubscribeConnection = (): void => {
    // 컴포넌트가 해제된 뒤 연결 상태 변경을 전달하지 않도록 구독을 제거한다
    connectionListeners.delete(listener);
  };

  // 현재 화면에서 사용할 연결 상태 구독 해제 함수를 반환한다
  return unsubscribeConnection;
};

/**
 * JDBC 또는 인터넷 연결 장애를 전역 화면 상태에 반영한다
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 */
export const publishConnectionError = (): void => {
  // 이미 장애 화면이 열린 상태에서는 구독자에게 같은 상태를 반복 전달하지 않는다
  if (isConnectionUnstable) {
    // 현재 연결 장애 화면을 그대로 유지한다
    return;
  }

  // 새 API 요청과 페이지 조작을 가릴 전역 연결 장애 상태를 설정한다
  isConnectionUnstable = true;
  // 등록된 모든 화면 구독자에게 연결 장애 상태를 전달한다
  connectionListeners.forEach(notifyConnection);
};

/**
 * API 실패가 JDBC, 인터넷 또는 서버 실행 장애에 해당하는지 판정한다
 *
 * @author HanWon.Jang
 * @param error API 요청에서 발생한 오류
 * @return 전역 서비스 장애 화면을 표시해야 하는지 여부
 */
export const isConnectionError = (error: unknown): boolean => {
  // 브라우저가 오프라인이면 서버 응답 유무와 관계없이 인터넷 연결 장애로 판정한다
  if (!navigator.onLine) {
    // 인터넷 연결이 끊긴 상태를 반환한다
    return true;
  }

  // Axios 오류가 아니면 서버 응답 상태를 판정할 수 없으므로 일반 오류로 유지한다
  if (!isAxiosError<ResultData>(error)) {
    // 전역 서비스 장애 화면을 표시하지 않도록 일반 오류 판정값을 반환한다
    return false;
  }

  const resultCode = Number(error.response?.data?.code);
  const responseStatus = error.response?.status;
  // 서버가 반환한 모든 5xx와 JDBC 실패 코드 및 응답 없는 연결 오류를 서비스 장애로 판정한다
  return (responseStatus !== undefined && responseStatus >= 500 && responseStatus < 600)
    || resultCode === DB_CONNECTION_FAILED_CODE
    || error.code === "ERR_NETWORK"
    || error.code === "ECONNABORTED";
};
