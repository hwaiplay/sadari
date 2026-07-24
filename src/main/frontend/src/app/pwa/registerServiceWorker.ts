/**
 * PWA 서비스워커를 등록합니다.
 * 개발 서버에서는 서비스워커 캐시가 화면 수정 확인을 방해할 수 있으므로 production 빌드에서만 등록합니다.
 *
 * @author Seunghyeon.Kang
 */
export function registerServiceWorker() {
  if (!("serviceWorker" in navigator) || !import.meta.env.PROD) {
    return;
  }

  window.addEventListener("load", () => {
    // 서비스워커 파일은 Vite public 디렉터리에서 정적 파일로 배포되며, 루트 scope로 앱 전체 라우트를 제어한다.
    navigator.serviceWorker.register("/service-worker.js").catch(() => {
      // PWA 등록은 보조 기능이므로 실패해도 앱 사용을 막지 않는다.
    });
  });
}
