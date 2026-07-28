import { initializeApp, getApp, getApps, type FirebaseOptions } from "firebase/app";
import { getMessaging, getToken, isSupported, onMessage } from "firebase/messaging";
import type { FirebaseWebConfig } from "@/features/Push/api/pushApi";

export const FIREBASE_PUSH_ENABLED_EVENT = "sadari:firebase-push-enabled";

/**
 * notify Firebase Push Enabled 사용자 동작을 처리한다
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 */
export function notifyFirebasePushEnabled() {

  window.dispatchEvent(new Event(FIREBASE_PUSH_ENABLED_EVENT));
}

/**
 * 사용자의 버튼 클릭 흐름 안에서 브라우저 알림 권한을 요청합니다.
 *
 * Notification.requestPermission()은 사용자의 직접 동작과 분리되면 브라우저가
 * 권한 팝업을 차단할 수 있으므로 Firebase 설정 조회보다 먼저 호출해야 합니다.
 *
 * @author HanWon.Jang
 * @return 허용된 알림 권한
 */
export async function requestPushNotificationPermission() {

  if (!window.isSecureContext) {
    throw new Error("PUSH_INSECURE_CONTEXT");
  }

  if (!("Notification" in window) || !("serviceWorker" in navigator)) {
    throw new Error("PUSH_NOT_SUPPORTED");
  }

  // 이미 허용된 브라우저에서는 팝업을 다시 띄우지 않고 다음 토큰 발급 단계로 진행합니다.
  if (Notification.permission === "granted") {
    return Notification.permission;
  }

  // 사용자가 한 번 차단한 권한은 코드로 다시 요청할 수 없으므로 사이트 설정 변경을 안내합니다.
  if (Notification.permission === "denied") {
    throw new Error("PUSH_PERMISSION_DENIED");
  }

  const permission = await Notification.requestPermission();

  if (permission !== "granted") {
    throw new Error("PUSH_PERMISSION_DENIED");
  }

  return permission;
}

/**
 * FCM token 발급에 사용할 service worker가 활성화될 때까지 기다립니다.
 *
 * 개발 서버 로드 직후처럼 등록이 아직 끝나지 않은 경우 navigator.serviceWorker.ready가
 * 대기 상태가 될 수 있으므로 제한 시간을 두어 버튼이 무한 로딩되는 것을 막습니다.
 *
 * @author HanWon.Jang
 * @return 활성화된 service worker 등록 정보
 */
async function waitServiceWorkerReady() {

  const serviceWorkerReadyTimeoutMs = 10_000;
  // 브라우저의 window.setTimeout 반환값은 number입니다.
  // 프로젝트에 @types/node도 포함되어 있어 ReturnType을 쓰면 NodeJS.Timeout으로 잘못 추론될 수 있으므로 명시합니다.
  let timeoutId: number | undefined;

  try {
    return await Promise.race([
      navigator.serviceWorker.ready,
      new Promise<never>((_, reject) => {

        timeoutId = window.setTimeout(() => {

          reject(new Error("PUSH_SERVICE_WORKER_NOT_READY"));
        }, serviceWorkerReadyTimeoutMs);
      }),
    ]);
  } finally {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId);
    }
  }
}

/**
 * 서버 설정 DTO를 Firebase Web SDK 초기화 옵션으로 변환합니다.
 * service account는 백엔드 전용이므로 여기에는 공개 가능한 Web 설정만 들어옵니다.
 *
 * @author HanWon.Jang
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
 * @author HanWon.Jang
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
 * @author HanWon.Jang
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
