import { isAxiosError } from "axios";
import {
  DB_CONNECTION_FAILED_CODE,
  type ResultData,
} from "@/app/api/resultData";

type ConnectionListener = () => void;
type ConnectionFailure = "database" | "offline";

let connectionFailure: ConnectionFailure | null = null;
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
  return connectionFailure !== null;
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
 * @param failure 서버가 확인한 JDBC 장애 또는 브라우저 오프라인 상태
 * @return 반환값이 없다
 */
export const publishConnectionError = (failure: ConnectionFailure): void => {
  // 이미 장애 화면이 열린 상태에서는 구독자에게 같은 상태를 반복 전달하지 않는다
  if (connectionFailure !== null) {
    // 현재 연결 장애 화면을 그대로 유지한다
    return;
  }

  // 새 API 요청과 페이지 조작을 가릴 전역 연결 장애 상태를 설정한다
  connectionFailure = failure;
  // 등록된 모든 화면 구독자에게 연결 장애 상태를 전달한다
  connectionListeners.forEach(notifyConnection);
};

/**
 * 확인된 연결 복구 원인과 일치하는 전역 장애 상태를 해제한다
 *
 * @author HanWon.Jang
 * @param failure 복구가 확인된 JDBC 또는 브라우저 오프라인 장애 원인
 * @return 반환값이 없다
 */
export const publishConnectionRestore = (failure: ConnectionFailure): void => {
  // 다른 종류의 장애는 현재 복구 근거만으로 정상화할 수 없으므로 유지한다
  if (connectionFailure !== failure) {
    // 확인되지 않은 장애 원인을 유지한 채 복구 처리를 종료한다
    return;
  }

  // 연결 복구 뒤 기존 앱 화면을 다시 사용할 수 있도록 장애 상태를 해제한다
  connectionFailure = null;
  // 등록된 모든 화면 구독자에게 연결 복구 상태를 전달한다
  connectionListeners.forEach(notifyConnection);
};

/**
 * API 실패가 서버가 확인한 JDBC 장애 또는 브라우저 오프라인에 해당하는지 판정한다
 *
 * @author HanWon.Jang
 * @param error API 요청에서 발생한 오류
 * @return 전역 서비스 장애 화면에 전달할 연결 장애 원인
 */
export const getConnectionFailure = (error: unknown): ConnectionFailure | null => {
  // Axios 오류가 아니면 서버의 JDBC 장애 코드를 확인할 수 없으므로 일반 오류로 유지한다
  if (!isAxiosError<ResultData>(error)) {
    // 전역 서비스 장애 화면을 표시하지 않도록 장애 원인이 없음을 반환한다
    return null;
  }

  const resultCode = Number(error.response?.data?.code);
  // 서버가 JDBC 연결 실패로 확정한 공통 응답 코드만 데이터베이스 장애로 판정한다
  if (resultCode === DB_CONNECTION_FAILED_CODE) {
    // 서버가 확인한 데이터베이스 연결 장애 원인을 반환한다
    return "database";
  }

  // 서버 응답이 없고 브라우저도 오프라인일 때만 실제 인터넷 단절로 판정한다
  if (error.response === undefined && !navigator.onLine) {
    // API 실패와 브라우저 상태가 함께 확인한 인터넷 단절 원인을 반환한다
    return "offline";
  }

  // 일반 5xx와 타임아웃 및 원인이 불명확한 네트워크 오류는 개별 API 실패로 유지한다
  return null;
};
