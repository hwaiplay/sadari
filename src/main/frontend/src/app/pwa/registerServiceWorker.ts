/**
 * PWA service worker를 등록한다.
 * 푸시 알림은 service worker가 있어야 토큰 발급과 백그라운드 수신이 가능하므로,
 * 운영 빌드뿐 아니라 localhost와 Tailnet HTTPS 개발 환경에서도 등록되도록 허용한다.
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 */
export function registerServiceWorker(): void {

  // 브라우저가 개발 서버를 직접 여는 기본 로컬 호스트인지 판정한다
  const isLocalhost = ["localhost", "127.0.0.1"].includes(window.location.hostname);
  // 공개 인증서가 적용된 Tailnet 개발 주소인지 판정해 안전한 HTTPS 설치 범위만 허용한다
  const isTailnetHttps = window.location.protocol === "https:" && window.location.hostname.endsWith(".ts.net");

  // 서비스워커를 지원하지 않는 브라우저에서는 기본 웹 화면만 사용한다
  if (!("serviceWorker" in navigator)) {
    // 지원되지 않는 브라우저에서 서비스워커 등록을 시도하지 않도록 종료한다
    return;
  }

  // 신뢰할 수 있는 로컬 또는 Tailnet 개발 주소가 아니면 개발 캐시가 다른 호스트에 남지 않게 차단한다
  if (!import.meta.env.PROD && !isLocalhost && !isTailnetHttps) {
    // 허용되지 않은 개발 호스트에서는 기본 웹 화면만 사용하도록 종료한다
    return;
  }

  // 이미지와 폰트의 지연 여부와 관계없이 서비스워커를 등록해 모바일 브라우저의 설치 판정을 준비한다
  setServiceWorker();
}

/**
 * 전체 웹앱 경로를 제어할 루트 서비스워커를 등록한다
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 */
function setServiceWorker(): void {

  // 서비스워커 원본 캐시를 우회해 앱을 열 때마다 최신 업데이트 스크립트를 확인한다
  navigator.serviceWorker.register("/service-worker.js", { updateViaCache: "none" })
    .then(handleSwRegistration)
    .catch(handleSwRegisterFailure);
}

/**
 * 등록된 서비스워커가 서버의 최신 앱 셸을 즉시 확인하도록 요청한다
 *
 * @author HanWon.Jang
 * @param registration 업데이트를 확인할 서비스워커 등록 정보
 * @return 반환값이 없다
 */
function handleSwRegistration(registration: ServiceWorkerRegistration): void {

  // 브라우저의 기본 확인 주기와 관계없이 현재 접속 시점에 최신 서비스워커를 조회한다
  registration.update().catch(handleSwRegisterFailure);
}

/**
 * 서비스워커 등록 실패가 기본 웹 화면 사용을 중단시키지 않도록 오류를 격리한다
 *
 * @author HanWon.Jang
 * @return 반환값이 없다
 */
function handleSwRegisterFailure(): void {

  // PWA 등록은 보조 기능이므로 실패해도 기본 화면 사용을 유지한다
}
