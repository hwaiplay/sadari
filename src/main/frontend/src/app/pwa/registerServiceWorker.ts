/**
 * PWA service worker를 등록한다.
 * 푸시 알림은 service worker가 있어야 토큰 발급과 백그라운드 수신이 가능하므로,
 * 운영 빌드뿐 아니라 localhost 개발 환경에서도 등록되도록 허용한다.
 *
 * @author Seunghyeon.Kang
 */
export function registerServiceWorker() {
  const isLocalhost = ["localhost", "127.0.0.1"].includes(window.location.hostname);

  if (!("serviceWorker" in navigator) || (!import.meta.env.PROD && !isLocalhost)) {
    return;
  }

  window.addEventListener("load", () => {
    // service-worker.js는 Vite public 디렉터리에서 루트 정적 파일로 배포되어 전체 라우트를 제어한다.
    navigator.serviceWorker.register("/service-worker.js").catch(() => {
      // PWA 등록은 보조 기능이므로 실패해도 기본 화면 사용은 막지 않는다.
    });
  });
}
