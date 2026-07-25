import { initializeApp, getApp, getApps, type FirebaseOptions } from "firebase/app";
import { getMessaging, getToken, isSupported, onMessage } from "firebase/messaging";
import type { FirebaseWebConfig } from "@/features/Push/api/pushApi";

export const FIREBASE_PUSH_ENABLED_EVENT = "sadari:firebase-push-enabled";

export function notifyFirebasePushEnabled() {
  window.dispatchEvent(new Event(FIREBASE_PUSH_ENABLED_EVENT));
}

/**
 * 서버 설정 DTO를 Firebase Web SDK 초기화 옵션으로 변환합니다.
 * service account는 백엔드 전용이므로 여기에는 공개 가능한 Web 설정만 들어옵니다.
 *
 * @author Hanwon.Jang
 * @param config 서버에서 받은 Firebase Web 설정
 * @return Firebase 초기화 옵션
 */
function createFirebaseOptions(config: FirebaseWebConfig): FirebaseOptions {
  return {
    apiKey: config.apiKey,
    authDomain: config.authDomain,
    projectId: config.projectId,
    storageBucket: config.storageBucket,
    messagingSenderId: config.messagingSenderId,
    appId: config.appId,
  };
}

/**
 * Firebase app은 한 번만 초기화해야 하므로 이미 초기화된 app이 있으면 재사용합니다.
 *
 * @author Hanwon.Jang
 * @param config 서버에서 받은 Firebase Web 설정
 * @return Firebase app
 */
function getFirebaseApp(config: FirebaseWebConfig) {
  return getApps().length > 0 ? getApp() : initializeApp(createFirebaseOptions(config));
}

/**
 * 브라우저 알림 권한을 요청하고 FCM registration token을 발급합니다.
 * 권한 거부, 미지원 브라우저, service worker 미준비 상태는 호출부에서 사용자에게 안내할 수 있도록 Error로 반환합니다.
 *
 * @author Hanwon.Jang
 * @param config 서버에서 받은 Firebase Web 설정
 * @return FCM registration token
 */
export async function requestFirebaseMessagingToken(config: FirebaseWebConfig) {
  const supported = await isSupported();

  if (!supported || !("Notification" in window) || !("serviceWorker" in navigator)) {
    throw new Error("PUSH_NOT_SUPPORTED");
  }

  if (Notification.permission !== "granted") {
    throw new Error("PUSH_PERMISSION_REQUIRED");
  }

  const registration = await waitServiceWorkerReady();
  const messaging = getMessaging(getFirebaseApp(config));
  const token = await getToken(messaging, {
    vapidKey: config.vapidPublicKey,
    serviceWorkerRegistration: registration,
  });

  if (!token) {
    throw new Error("PUSH_TOKEN_EMPTY");
  }

  return token;
}

/**
 * 앱이 열린 상태에서 도착한 FCM 메시지를 구독합니다.
 *
 * @param config 서버에서 받은 Firebase Web 설정
 * @param listener 포그라운드 메시지 수신 콜백
 * @return 구독 해제 함수. 미지원 환경에서는 아무 작업도 하지 않는 함수를 반환합니다.
 */
export async function subscribeFirebaseForegroundMessages(
  config: FirebaseWebConfig,
  listener: () => void,
) {
  const supported = await isSupported();

  if (
    !supported
    || !("Notification" in window)
    || Notification.permission !== "granted"
  ) {
    return () => undefined;
  }

  const messaging = getMessaging(getFirebaseApp(config));
  return onMessage(messaging, listener);
}
