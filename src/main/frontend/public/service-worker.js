const CACHE_NAME = "sadari-pwa-v1";
const APP_SHELL = [
  "/",
  "/favicon/site.webmanifest",
  "/favicon/favicon.ico",
  "/favicon/android-chrome-192x192.png",
  "/favicon/android-chrome-512x512.png",
  "/favicon/apple-touch-icon.png",
];
const AUTH_RETRY_RESULT_CODES = new Set([1001, 1002, 1003]);

/**
 * 시스템 푸시 알림 클릭 시 인증 사용자의 해당 알림 한 건을 읽음 처리한다.
 * access token이 만료된 경우 refresh API를 한 번 호출한 뒤 읽음 요청을 재시도한다.
 *
 * @param {number} alimNumb 사용자별 알림 번호
 * @return {Promise<void>} 읽음 처리 완료 Promise
 */
async function uptAlimRead(alimNumb) {
  const requestRead = async () => {
    const response = await fetch("/api/alim/read-status", {
      method: "PUT",
      credentials: "include",
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
    await fetch("/api/oauth/refresh", {
      method: "POST",
      credentials: "include",
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

self.addEventListener("install", (event) => {
  // 앱 설치 직후 기본 화면과 아이콘을 캐시해 최초 실행에 필요한 최소 자원을 준비한다.
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(APP_SHELL))
      .then(() => self.skipWaiting()),
  );
});

self.addEventListener("activate", (event) => {
  // 캐시 이름이 바뀌면 이전 버전 캐시를 제거해 오래된 JS/CSS가 계속 남지 않게 한다.
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => Promise.all(
        cacheNames
          .filter((cacheName) => cacheName !== CACHE_NAME)
          .map((cacheName) => caches.delete(cacheName)),
      ))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (event) => {
  const request = event.request;

  if (request.method !== "GET") {
    return;
  }

  const requestUrl = new URL(request.url);

  if (requestUrl.origin !== self.location.origin) {
    return;
  }

  // API와 업로드 파일은 사용자별 최신 데이터와 인증 상태가 중요하므로 서비스워커 캐시를 타지 않는다.
  if (requestUrl.pathname.startsWith("/api") || requestUrl.pathname.startsWith("/uploads")) {
    return;
  }

  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const responseClone = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put("/", responseClone));
          return response;
        })
        .catch(() => caches.match("/")),
    );
    return;
  }

  event.respondWith(
    caches.match(request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }

      return fetch(request).then((response) => {
        const responseClone = response.clone();

        // Vite가 만드는 JS/CSS와 public 정적 자원만 캐시해 반복 방문 시 로딩 비용을 줄인다.
        if (
          requestUrl.pathname.startsWith("/assets/")
          || requestUrl.pathname.startsWith("/favicon/")
          || requestUrl.pathname.startsWith("/fonts/")
          || requestUrl.pathname.startsWith("/img/")
        ) {
          caches.open(CACHE_NAME).then((cache) => cache.put(request, responseClone));
        }

        return response;
      });
    }),
  );
});

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
      icon: "/favicon/android-chrome-192x192.png",
      badge: "/favicon/favicon-32x32.png",
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
