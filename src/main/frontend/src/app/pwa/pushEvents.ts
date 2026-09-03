export const FIREBASE_PUSH_ENABLED_EVENT = "sadari:firebase-push-enabled";

/**
 * 사용자가 푸시 알림을 활성화한 사실을 현재 화면의 수신 구독에 알림
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
export function notifyFirebasePushEnabled(): void {
  // Firebase SDK와 무관한 가벼운 브라우저 이벤트만 전파함
  window.dispatchEvent(new Event(FIREBASE_PUSH_ENABLED_EVENT));
}
