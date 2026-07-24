import { initializeApp, getApp, getApps, type FirebaseOptions } from "firebase/app";
import { getMessaging, getToken, isSupported } from "firebase/messaging";
import type { FirebaseWebConfig } from "@/features/Push/api/pushApi";

const SERVICE_WORKER_READY_TIMEOUT_MS = 10000;

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
 * 버튼 클릭 직후 브라우저 알림 권한을 먼저 요청합니다.
 * Chrome 계열 브라우저는 사용자 액션과 멀어진 비동기 흐름에서 권한 팝업을 제한할 수 있으므로,
 * Firebase 설정 조회 같은 API 호출보다 먼저 실행해야 컨펌창이 안정적으로 표시됩니다.
 *
 * @author Hanwon.Jang
 */
export async function requestPushNotificationPermission() {
  /*
   * Chrome/Edge/Safari의 알림 권한 팝업은 사용자 클릭 이벤트와 최대한 가까운 시점에 호출해야 한다.
   * 여기서 Firebase isSupported() 같은 비동기 검사를 먼저 await하면 브라우저가 사용자 제스처로 인정하지 않아
   * "알림을 허용하시겠습니까?" 시스템 팝업이 표시되지 않을 수 있다.
   */
  if (!("Notification" in window) || !("serviceWorker" in navigator)) {
    throw new Error("PUSH_NOT_SUPPORTED");
  }

  if (!window.isSecureContext) {
    throw new Error("PUSH_INSECURE_CONTEXT");
  }

  if (Notification.permission === "granted") {
    return;
  }

  if (Notification.permission === "denied") {
    throw new Error("PUSH_PERMISSION_DENIED");
  }

  const permission = await Notification.requestPermission();

  if (permission !== "granted") {
    throw new Error("PUSH_PERMISSION_DENIED");
  }

  /*
   * 권한 팝업을 먼저 처리한 뒤 Firebase Messaging 지원 여부를 확인한다.
   * 이 검사는 비동기여도 더 이상 권한 팝업 표시 여부에 영향을 주지 않는다.
   */
  const supported = await isSupported();

  if (!supported) {
    throw new Error("PUSH_NOT_SUPPORTED");
  }
}

/**
 * service worker 등록이 완료될 때까지 기다립니다.
 * 등록 실패나 scope 충돌이 있으면 navigator.serviceWorker.ready가 오래 대기할 수 있어,
 * 사용자가 아무 반응이 없다고 느끼지 않도록 제한 시간 이후 명확한 오류로 전환합니다.
 *
 * @author Hanwon.Jang
 * @return 활성화된 service worker registration
 */
async function waitServiceWorkerReady() {
  return Promise.race([
    navigator.serviceWorker.ready,
    new Promise<never>((_, reject) => {
      window.setTimeout(() => reject(new Error("PUSH_SERVICE_WORKER_NOT_READY")), SERVICE_WORKER_READY_TIMEOUT_MS);
    }),
  ]);
}
