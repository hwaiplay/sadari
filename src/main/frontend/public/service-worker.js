const CACHE_PREFIX = "sadari-pwa-";
const CACHE_NAME = `${CACHE_PREFIX}v4`;
const APP_SHELL = [
  "/",
  "/favicon/site.webmanifest",
  "/favicon/favicon.ico?v=20260802",
  "/favicon/android-chrome-192x192.png?v=20260802",
  "/favicon/android-chrome-512x512.png?v=20260802",
  "/favicon/apple-touch-icon.png?v=20260802",
];
const IMMUTABLE_CACHE_PATH_PREFIX_LIST = ["/assets/"];
const REFRESHABLE_CACHE_PATH_PREFIX_LIST = ["/favicon/", "/fonts/", "/img/"];
const AUTH_RETRY_RESULT_CODES = new Set([1001, 1002, 1003]);
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";

let csrfRequest = null;
let csrfToken = null;

/**
 * Spring Security가 현재 브라우저에 발급한 CSRF Token을 조회한다.
 *
 * @return {Promise<string>} 상태 변경 요청 Header에 사용할 CSRF Token
 * @throws CSRF Token API가 실패하거나 Token 데이터가 없을 때 발생
 * @author SeungHyeon.Kang
 */
async function requestCsrfToken() {
  // Service Worker도 화면과 같은 인증 Cookie를 사용해 CSRF Token을 조회한다
  const response = await fetch("/api/oauth/csrf", {
    method: "GET",
    credentials: "include",
  });
  // 공통 응답의 상태 코드와 Token 값을 함께 검증하기 위해 JSON 데이터를 조회한다
  const result = await response.json();

  // 상태 코드 또는 Token 값이 유효하지 않으면 보호되지 않은 요청을 전송하지 않는다
  if (!response.ok || Number(result?.code) !== 200
          || typeof result?.data !== "string" || result.data.length === 0) {
    // CSRF Token 누락을 알림 읽음 처리의 실패 경로로 전달한다
    throw new Error("CSRF_TOKEN_MISSING");
  }

  // 검증된 CSRF Token을 반환한다
  return result.data;
}

/**
 * 동시에 시작된 Service Worker 요청이 하나의 CSRF Token 조회 Promise를 공유하도록 Token을 준비한다.
 *
 * @param {boolean} forceRefresh 기존 Token을 버리고 다시 조회할지 여부
 * @return {Promise<string>} 현재 브라우저 Cookie와 연결된 CSRF Token
 * @throws CSRF Token 조회 요청이 실패할 때 발생
 * @author SeungHyeon.Kang
 */
async function getCsrfToken(forceRefresh = false) {
  // 서버가 기존 Token을 거부한 경우 Cache를 비우고 새 Token을 조회한다
  if (forceRefresh) {
    // 이후 요청이 거부된 Token을 재사용하지 않도록 Cache를 초기화한다
    csrfToken = null;
  }

  // 이미 검증한 Token이 있으면 추가 네트워크 요청 없이 재사용한다
  if (csrfToken) {
    // Service Worker 상태 변경 요청에 사용할 CSRF Token을 반환한다
    return csrfToken;
  }

  // 진행 중인 조회가 없을 때만 CSRF Token API를 한 번 호출한다
  if (!csrfRequest) {
    // 동시에 발생한 푸시 클릭이 같은 Token 조회 결과를 기다리게 한다
    csrfRequest = requestCsrfToken();
  }

  // 성공과 실패 모두 진행 중 Promise를 정리해 이후 재시도를 허용한다
  try {
    // CSRF Token 조회 결과를 Service Worker Cache에 저장한다
    csrfToken = await csrfRequest;
    // 상태 변경 요청 Header에 사용할 CSRF Token을 반환한다
    return csrfToken;
  }

  // Token 조회의 성공 여부와 관계없이 완료된 Promise를 정리한다
  finally {
    // 완료된 Promise를 제거해 필요할 때 새 Token을 조회할 수 있게 한다
    csrfRequest = null;
  }
}

/**
 * Service Worker의 상태 변경 요청에 CSRF Token Header를 설정해 전송한다.
 *
 * @param {string} url 호출할 API 경로
 * @param {RequestInit} options 상태 변경 요청 설정
 * @param {boolean} retry CSRF 오류 재시도를 허용할지 여부
 * @return {Promise<Response>} API 응답
 * @throws CSRF Token 조회 또는 API 통신이 실패할 때 발생
 * @author SeungHyeon.Kang
 */
async function requestWithCsrf(url, options, retry = true) {
  // 현재 브라우저 Cookie와 연결된 CSRF Token을 조회한다
  const token = await getCsrfToken();
  // 기존 요청 Header를 유지하면서 CSRF Token을 추가할 Header 객체를 생성한다
  const headers = new Headers(options.headers);
  // 브라우저가 자동으로 추가하지 않는 요청 Header에 CSRF Token을 설정한다
  headers.set(CSRF_HEADER_NAME, token);
  // 인증 Cookie와 CSRF Header를 함께 포함해 상태 변경 API를 호출한다
  const response = await fetch(url, {
    ...options,
    credentials: "include",
    headers,
  });

  // Cookie와 Header Token이 달라졌으면 새 Token으로 원 요청을 한 번만 복구한다
  if (response.status === 403 && retry) {
    // 서버 Cookie와 일치하는 최신 CSRF Token을 다시 조회한다
    await getCsrfToken(true);
    // 같은 상태 변경 요청을 최신 Token으로 한 번만 다시 전송한다
    return requestWithCsrf(url, options, false);
  }

  // CSRF 검증을 거친 API 응답을 호출부에 반환한다
  return response;
}

/**
 * 시스템 푸시 알림 클릭 시 인증 사용자의 해당 알림 한 건을 읽음 처리한다.
 * access token이 만료된 경우 refresh API를 한 번 호출한 뒤 읽음 요청을 재시도한다.
 *
 * @param {number} alimNumb 사용자별 알림 번호
 * @return {Promise<void>} 읽음 처리 완료 Promise
 * @author HanWon.Jang
 */
async function uptAlimRead(alimNumb) {

  /**
   * 알림 읽음 처리 API를 호출한다
   *
   * @author HanWon.Jang
   * @return {Promise<{response: Response, result: object | null}>} API 응답과 공통 응답 데이터
   * @throws 알림 읽음 처리 요청에 실패하면 발생
   */
  const requestRead = async () => {

    const response = await requestWithCsrf("/api/alim/read-status", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ alimNumb }),
    });
    const result = await response.json().catch(() => null);
    return { response, result };
  };

  let readResult = await requestRead();
  const resultCode = Number(readResult.result?.code);

  // 푸시 클릭은 Axios 인증 인터셉터를 거치지 않으므로 access token 만료 시 서비스워커가 refresh를 직접 한 번 수행한다.
  if (
    readResult.response.status === 401
    || AUTH_RETRY_RESULT_CODES.has(resultCode)
  ) {
    await requestWithCsrf("/api/oauth/refresh", {
      method: "POST",
    });
    readResult = await requestRead();
  }

  if (!readResult.response.ok || Number(readResult.result?.code) !== 200) {
    throw new Error("ALIM_READ_FAILED");
  }

  // 이미 열려 있는 화면에는 읽음 변경 사실을 알려 헤더의 미읽음 배지를 즉시 동기화한다.
  const clientList = await self.clients.matchAll({
    type: "window",
    includeUncontrolled: true,
  });
  clientList.forEach((client) => {
    client.postMessage({ type: "SADARI_ALIM_READ" });
  });
}

// 서비스워커 설치 시 오프라인 실행에 필요한 최소 앱 셸을 준비한다
self.addEventListener("install", handleSwInstall);
// 새 서비스워커 활성화 시 이전 앱 셸 캐시를 정리한다
self.addEventListener("activate", handleSwActivate);
// 화면 요청별 변경 가능성에 맞는 캐시 정책을 적용한다
self.addEventListener("fetch", handleServiceWorkerFetch);

/**
 * 서비스워커 설치가 끝나기 전에 최신 앱 셸을 캐시에 저장한다
 *
 * @author HanWon.Jang
 * @param {ExtendableEvent} event 서비스워커 설치 이벤트
 * @return {void} 반환값이 없다
 */
function handleSwInstall(event) {

  // 최신 앱 셸 저장과 대기 상태 해제가 끝날 때까지 설치 완료를 보류한다
  event.waitUntil(setAppShellCache());
}

/**
 * 오프라인 실행에 필요한 현재 버전의 기본 화면과 아이콘을 저장한다
 *
 * @author HanWon.Jang
 * @return {Promise<void>} 앱 셸 저장 완료 Promise
 */
async function setAppShellCache() {

  // 현재 앱 버전 전용 캐시를 연다
  const cache = await caches.open(CACHE_NAME);
  // 같은 버전에서 함께 사용해야 하는 기본 화면과 아이콘을 원자적으로 저장한다
  await cache.addAll(APP_SHELL);
  // 새 서비스워커가 기존 대기 버전을 건너뛰고 즉시 활성화될 수 있게 한다
  await self.skipWaiting();
}

/**
 * 서비스워커 활성화가 끝나기 전에 이전 사다리 앱 셸 캐시를 제거한다
 *
 * @author HanWon.Jang
 * @param {ExtendableEvent} event 서비스워커 활성화 이벤트
 * @return {void} 반환값이 없다
 */
function handleSwActivate(event) {

  // 이전 캐시 제거와 현재 화면 제어가 끝날 때까지 활성화 완료를 보류한다
  event.waitUntil(activateLatestSw());
}

/**
 * 이전 앱 셸 캐시를 제거하고 다음 앱 실행에 최신 서비스워커를 적용한다
 *
 * @author HanWon.Jang
 * @return {Promise<void>} 최신 서비스워커 활성화 완료 Promise
 */
async function activateLatestSw() {

  // 현재 출처에 저장된 캐시 이름을 조회한다
  const cacheNameList = await caches.keys();
  const deleteCachePromiseList = [];

  // 사다리 앱이 만든 이전 버전 캐시만 골라 제거한다
  for (const cacheName of cacheNameList) {
    // 현재 버전이 아니면서 사다리 앱 접두사를 가진 캐시만 제거한다
    if (cacheName.startsWith(CACHE_PREFIX) && cacheName !== CACHE_NAME) {
      // 이전 앱 셸 캐시 제거 작업을 활성화 완료 조건에 추가한다
      deleteCachePromiseList.push(caches.delete(cacheName));
    }

  }

  // 열린 화면의 편집 상태는 유지하면서 모든 이전 버전 캐시가 제거될 때까지 기다린다
  await Promise.all(deleteCachePromiseList);
}

/**
 * 요청 대상의 변경 가능성과 인증 범위에 따라 적합한 캐시 응답을 선택한다
 *
 * @author HanWon.Jang
 * @param {FetchEvent} event 서비스워커 네트워크 요청 이벤트
 * @return {void} 반환값이 없다
 */
function handleServiceWorkerFetch(event) {

  const request = event.request;

  // 읽기 요청이 아니면 브라우저가 인증과 본문을 그대로 처리하도록 서비스워커에서 제외한다
  if (request.method !== "GET") {
    // 변경 요청을 가로채지 않도록 종료한다
    return;
  }

  // 동일 출처와 캐시 대상 경로를 판정할 요청 주소를 생성한다
  const requestUrl = new URL(request.url);

  // 외부 출처 자원은 외부 서버의 캐시 정책을 존중하도록 서비스워커에서 제외한다
  if (requestUrl.origin !== self.location.origin) {
    // 외부 자원 요청을 가로채지 않도록 종료한다
    return;
  }

  // API와 업로드 파일은 사용자별 최신 데이터와 인증 상태가 중요하므로 서비스워커 캐시에서 제외한다
  if (requestUrl.pathname.startsWith("/api") || requestUrl.pathname.startsWith("/uploads")) {
    // 사용자 데이터 요청을 브라우저와 서버가 직접 처리하도록 종료한다
    return;
  }

  // 화면 이동은 새 배포 화면을 우선하고 네트워크 장애 때만 저장된 앱 셸을 사용한다
  if (request.mode === "navigate") {
    // 최신 화면 우선 응답을 브라우저에 전달한다
    event.respondWith(getNavigationResponse(request));
    // 하나의 화면 요청에 캐시 전략이 중복 적용되지 않도록 종료한다
    return;
  }

  // Vite 해시 파일은 내용이 바뀌면 주소도 바뀌므로 저장된 응답을 우선 사용한다
  if (hasCachePathPrefix(requestUrl.pathname, IMMUTABLE_CACHE_PATH_PREFIX_LIST)) {
    // 변경 불가능한 빌드 자원에 캐시 우선 응답을 적용한다
    event.respondWith(getCacheFirstResponse(request));
    // 하나의 정적 자원 요청에 캐시 전략이 중복 적용되지 않도록 종료한다
    return;
  }

  // 같은 주소로 교체될 수 있는 아이콘과 화면 이미지는 서버의 최신 파일을 우선 사용한다
  if (hasCachePathPrefix(requestUrl.pathname, REFRESHABLE_CACHE_PATH_PREFIX_LIST)) {
    // 변경 가능한 정적 자원에 네트워크 우선 응답을 적용한다
    event.respondWith(getNetworkFirstResponse(request));
  }
}

/**
 * 화면 이동 시 서버의 최신 HTML을 사용하고 연결 장애 때 저장된 앱 셸을 반환한다
 *
 * @author HanWon.Jang
 * @param {Request} request 화면 이동 요청
 * @return {Promise<Response>} 최신 화면 또는 오프라인 앱 셸 응답
 */
async function getNavigationResponse(request) {

  // 네트워크가 연결된 동안 최신 배포 화면을 조회한다
  try {
    const response = await fetch(request);

    // 정상 응답만 오프라인 앱 셸로 교체해 오류 화면이 장기간 남지 않게 한다
    if (response.ok) {
      // 다음 오프라인 실행에 사용할 최신 기본 화면을 저장한다
      await setRuntimeCacheResponse("/", response);
    }

    // 서버에서 받은 최신 화면 응답을 반환한다
    return response;
  }

  catch {
    // 네트워크 장애를 대신할 기본 화면을 캐시에서 조회한다
    const cachedResponse = await caches.match("/");

    // 저장된 앱 셸이 있으면 오프라인 화면을 제공한다
    if (cachedResponse) {
      // 마지막으로 저장된 기본 화면을 반환한다
      return cachedResponse;
    }

    // 앱 셸도 없으면 브라우저가 연결 실패로 처리할 오류 응답을 반환한다
    return Response.error();
  }
}

/**
 * 내용 해시가 있는 빌드 자원은 저장된 응답을 우선하고 없을 때 서버에서 내려받는다
 *
 * @author HanWon.Jang
 * @param {Request} 빌드 정적 자원 요청
 * @return {Promise<Response>} 캐시 또는 네트워크 정적 자원 응답
 */
async function getCacheFirstResponse(request) {

  // 동일한 빌드 자원이 이미 저장되어 있는지 확인한다
  const cachedResponse = await caches.match(request);

  // 내용 해시가 같은 파일은 변경되지 않으므로 저장된 응답을 재사용한다
  if (cachedResponse) {
    // 저장된 빌드 자원 응답을 반환한다
    return cachedResponse;
  }

  // 처음 요청된 빌드 자원을 서버에서 내려받는다
  const response = await fetch(request);

  // 정상 응답만 저장해 일시적인 오류가 캐시에 남지 않게 한다
  if (response.ok) {
    // 이후 요청에서 재사용할 빌드 자원을 저장한다
    await setRuntimeCacheResponse(request, response);
  }

  // 서버에서 받은 빌드 자원 응답을 반환한다
  return response;
}

/**
 * 같은 주소로 교체될 수 있는 정적 자원은 서버를 우선하고 연결 장애 때 캐시를 사용한다
 *
 * @author HanWon.Jang
 * @param {Request} 변경 가능한 정적 자원 요청
 * @return {Promise<Response>} 네트워크 또는 캐시 정적 자원 응답
 */
async function getNetworkFirstResponse(request) {

  // 연결 가능한 동안 최신 아이콘과 화면 이미지를 조회한다
  try {
    const response = await fetch(request);

    // 정상 응답만 교체해 깨진 파일 응답이 캐시에 남지 않게 한다
    if (response.ok) {
      // 같은 주소의 이전 정적 자원을 최신 응답으로 교체한다
      await setRuntimeCacheResponse(request, response);
    }

    // 서버에서 받은 최신 정적 자원 응답을 반환한다
    return response;
  }

  catch {
    // 네트워크 장애를 대신할 정적 자원을 캐시에서 조회한다
    const cachedResponse = await caches.match(request);

    // 이전에 저장된 자원이 있으면 연결 장애 중에도 화면을 유지한다
    if (cachedResponse) {
      // 마지막으로 저장된 정적 자원 응답을 반환한다
      return cachedResponse;
    }

    // 캐시된 자원도 없으면 브라우저가 로드 실패로 처리할 오류 응답을 반환한다
    return Response.error();
  }
}

/**
 * 최신 네트워크 응답을 현재 앱 버전 캐시에 복제해 저장한다
 *
 * @author HanWon.Jang
 * @param {Request|string} request 저장할 요청 또는 앱 셸 경로
 * @param {Response} response 복제해 저장할 정상 응답
 * @return {Promise<void>} 정적 자원 저장 완료 Promise
 */
async function setRuntimeCacheResponse(request, response) {

  // 현재 앱 버전 전용 캐시를 연다
  const cache = await caches.open(CACHE_NAME);
  // 브라우저에 반환할 원본 응답과 분리된 복제본을 저장한다
  await cache.put(request, response.clone());
}

/**
 * 요청 경로가 지정된 캐시 정책 접두사 중 하나에 포함되는지 판정한다
 *
 * @author HanWon.Jang
 * @param {string} pathname 판정할 동일 출처 요청 경로
 * @param {string[]} pathPrefixList 허용할 캐시 경로 접두사 목록
 * @return {boolean} 캐시 정책 적용 대상 여부
 */
function hasCachePathPrefix(pathname, pathPrefixList) {

  // 요청 경로와 일치하는 캐시 정책 접두사를 순서대로 확인한다
  for (const pathPrefix of pathPrefixList) {
    // 현재 접두사로 시작하면 해당 캐시 정책 적용 대상으로 판정한다
    if (pathname.startsWith(pathPrefix)) {
      // 일치하는 캐시 경로가 있음을 반환한다
      return true;
    }

  }

  // 일치하는 캐시 경로가 없음을 반환한다
  return false;
}

self.addEventListener("push", (event) => {
  let payload = {};

  if (event.data) {
    try {
      payload = event.data.json();
    } catch {
      payload = { notification: { title: "알림", body: event.data.text() } };
    }
  }

  const notification = payload.notification || {};
  const data = payload.data || {};
  const title = notification.title || data.title || "알림";
  const body = notification.body || data.body || "";
  const linkUrlx = data.linkUrlx || "/alim";
  const alimNumb = Number(data.alimNumb);

  // FCM에서 받은 payload를 브라우저 알림으로 표시한다.
  // 링크와 알림 번호는 notificationclick에서 이동 및 개별 읽음 처리에 사용하므로 notification data에 함께 저장한다.
  const showNotification = self.registration.showNotification(title, {
      body,
      icon: "/favicon/android-chrome-192x192.png?v=20260802",
      badge: "/favicon/favicon-32x32.png?v=20260802",
      data: {
        linkUrlx,
        alimNumb: Number.isFinite(alimNumb) ? alimNumb : null,
      },
    });
  const notifyOpenClients = self.clients
    .matchAll({ type: "window", includeUncontrolled: true })
    .then((clientList) => {
      clientList.forEach((client) => {
        client.postMessage({ type: "SADARI_ALIM_RECEIVED" });
      });
    });

  event.waitUntil(
    Promise.all([showNotification, notifyOpenClients]),
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();

  const linkUrlx = event.notification.data?.linkUrlx || "/alim";
  const alimNumb = Number(event.notification.data?.alimNumb);
  const targetUrl = new URL(linkUrlx, self.location.origin).href;

  const readAlim = Number.isFinite(alimNumb) && alimNumb > 0
    ? uptAlimRead(alimNumb).catch(() => undefined)
    : Promise.resolve();

  event.waitUntil(
    readAlim.then(() => (
      self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
        for (const client of clientList) {
          if ("focus" in client) {
            client.navigate(targetUrl);
            return client.focus();
          }
        }

        return self.clients.openWindow(targetUrl);
      })
    )),
  );
});
