import { useCallback, useEffect, useSyncExternalStore } from "react";
import { message } from "@/app/messages/message";
import {
  getConnectionStatus,
  publishConnectionError,
  subscribeConnection,
} from "@/app/connection/connectionStatus";
import { ActionButton } from "@/components/Button/ActionButton";
import * as styles from "./ConnectionErrorScreen.css";

/**
 * JDBC 또는 인터넷 연결 장애가 감지되면 앱 전체를 연결 안내 화면으로 전환한다
 *
 * @author HanWon.Jang
 * @return 연결 장애 안내 화면 또는 정상 연결 상태의 빈 화면
 */
export const ConnectionErrorScreen = () => {
  // Axios와 브라우저 이벤트가 공유하는 전역 연결 장애 상태를 구독한다
  const isUnstable = useSyncExternalStore(
    subscribeConnection,
    getConnectionStatus,
    getConnectionStatus,
  );

  /**
   * 브라우저 인터넷 연결이 끊기면 전역 연결 장애 상태를 설정한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleOffline = useCallback((): void => {
    // API 요청이 없는 화면에서도 인터넷 단절을 즉시 안내한다
    publishConnectionError();
  }, []);

  /**
   * 현재 페이지를 다시 불러와 인터넷과 서버 연결 상태를 새로 확인한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleRetry = useCallback((): void => {
    // 앱 초기 요청부터 다시 실행해 JDBC와 인터넷 연결 복구 여부를 확인한다
    window.location.reload();
  }, []);

  /**
   * 앱이 열린 동안 브라우저 오프라인 이벤트를 연결하고 화면 해제 시 정리한다
   *
   * @author HanWon.Jang
   * @return 브라우저 오프라인 이벤트 정리 함수
   */
  const syncBrowserEvents = useCallback((): (() => void) => {
    // API 호출이 없더라도 인터넷 단절을 감지하도록 브라우저 이벤트를 등록한다
    window.addEventListener("offline", handleOffline);

    /**
     * 연결 장애 화면 감지에 사용한 브라우저 이벤트를 제거한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const clearBrowserEvents = (): void => {
      // 컴포넌트 해제 이후 오프라인 이벤트가 남지 않도록 정리한다
      window.removeEventListener("offline", handleOffline);
    };

    // 현재 컴포넌트에 등록한 브라우저 이벤트 정리 함수를 반환한다
    return clearBrowserEvents;
  }, [handleOffline]);

  // 앱 생명주기와 브라우저 인터넷 단절 감지를 연결한다
  useEffect(syncBrowserEvents, [syncBrowserEvents]);

  // 연결 장애가 없으면 기존 앱 화면과 조작을 그대로 유지한다
  if (!isUnstable) {
    // 정상 연결 상태에서는 전역 안내 화면을 렌더링하지 않는다
    return null;
  }

  // 앱 전체를 가리는 연결 장애 안내와 재시도 동작을 반환한다
  return (
    <section className={styles.screen} role="alert" aria-live="assertive">
      {/* 연결 장애 상태와 복구 동작 안내 영역 */}
      <div className={styles.content}>
        {/* 연결 장애를 나타내는 장식 아이콘 영역 */}
        <div className={styles.iconWrap} aria-hidden="true">
          <svg className={styles.icon} viewBox="0 0 64 64" fill="none">
            <path d="M13 27.5C22.8 17.8 41.2 17.8 51 27.5" />
            <path d="M21 36C27.1 30 36.9 30 43 36" />
            <path d="M29 44.5C30.7 42.8 33.3 42.8 35 44.5" />
            <path d="M14 50L50 14" />
          </svg>
        </div>

        {/* 연결 장애 제목과 확인 방법 안내 영역 */}
        <div className={styles.textGroup}>
          <h1 className={styles.title}>
            {/* "연결이 불안정합니다." */}
            {message("frontend.error.connection.title")}
          </h1>
          <p className={styles.description}>
            {/* "인터넷 또는 서버 연결 상태를 확인한 후 다시 시도해 주세요." */}
            {message("frontend.error.connection.description")}
          </p>
        </div>

        {/* 연결 상태를 다시 확인하는 버튼 영역 */}
        <div className={styles.action}>
          <ActionButton size="lg" width="full" onClick={handleRetry}>
            {/* "다시 시도" */}
            {message("frontend.common.retry")}
          </ActionButton>
        </div>
      </div>
    </section>
  );
};
