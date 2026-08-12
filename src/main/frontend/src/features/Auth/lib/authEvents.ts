const AUTH_CHANNEL_NAME = "sadari-auth";
const AUTH_STORAGE_KEY = "sadari:auth-event";
const AUTH_WINDOW_EVENT = "sadari:auth-event";

export type AuthEvent = {
  type: "LOGOUT";
  eventId: string;
};

/**
 * 같은 브라우저의 다른 탭에 로그아웃 완료를 전달한다.
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
export function publishAuthLogout(): void {

  const authEvent: AuthEvent = {
    type: "LOGOUT",
    eventId: typeof crypto.randomUUID === "function"
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random()}`,
  };

  // BroadcastChannel을 지원하는 브라우저의 열린 탭에 즉시 전달한다
  if ("BroadcastChannel" in window) {
    try {
      const channel = new BroadcastChannel(AUTH_CHANNEL_NAME);
      channel.postMessage(authEvent);
      channel.close();
    } catch {
      // 브라우저가 채널 생성을 제한하면 storage 대체 경로를 계속 시도한다
    }
  }

  // BroadcastChannel 미지원 브라우저도 storage 이벤트로 다른 탭에 전달한다
  try {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authEvent));
  } catch {
    // 저장소가 차단돼도 현재 탭 로그아웃과 BroadcastChannel 전달은 유지한다
  }
  // 로그아웃을 실행한 현재 탭에도 같은 정리 이벤트를 전달한다
  window.dispatchEvent(new CustomEvent<AuthEvent>(AUTH_WINDOW_EVENT, { detail: authEvent }));
}

/**
 * 동일 브라우저 탭에서 발생한 인증 이벤트를 구독한다.
 *
 * @author SeungHyeon.Kang
 * @param listener 인증 이벤트 처리 함수
 * @return 구독 해제 함수
 */
export function subscribeAuthEvents(listener: (event: AuthEvent) => void): () => void {

  let channel: BroadcastChannel | null = null;

  // 브라우저 정책이 채널 생성을 차단하면 storage 이벤트만 구독한다
  if ("BroadcastChannel" in window) {
    try {
      channel = new BroadcastChannel(AUTH_CHANNEL_NAME);
    } catch {
      channel = null;
    }
  }

  const handleChannelMessage = (event: MessageEvent<AuthEvent>) => {
    listener(event.data);
  };

  const handleStorage = (event: StorageEvent) => {
    if (event.key !== AUTH_STORAGE_KEY || !event.newValue) {
      return;
    }

    try {
      listener(JSON.parse(event.newValue) as AuthEvent);
    } catch {
      // 다른 스크립트가 손상시킨 저장값은 인증 상태 변경으로 해석하지 않는다
    }
  };

  const handleWindowEvent = (event: Event) => {
    listener((event as CustomEvent<AuthEvent>).detail);
  };

  channel?.addEventListener("message", handleChannelMessage);
  window.addEventListener("storage", handleStorage);
  window.addEventListener(AUTH_WINDOW_EVENT, handleWindowEvent);

  return () => {
    channel?.removeEventListener("message", handleChannelMessage);
    channel?.close();
    window.removeEventListener("storage", handleStorage);
    window.removeEventListener(AUTH_WINDOW_EVENT, handleWindowEvent);
  };
}
