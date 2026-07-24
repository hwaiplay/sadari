const CACHE_NAME = "sadari-pwa-v1";
const APP_SHELL = [
  "/",
  "/favicon/site.webmanifest",
  "/favicon/favicon.ico",
  "/favicon/android-chrome-192x192.png",
  "/favicon/android-chrome-512x512.png",
  "/favicon/apple-touch-icon.png",
];

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
    } catch (e) {
      payload = { notification: { title: "알림", body: event.data.text() } };
    }
  }

  const notification = payload.notification || {};
  const data = payload.data || {};
  const title = notification.title || data.title || "알림";
  const body = notification.body || data.body || "";
  const linkUrlx = data.linkUrlx || "/alim";

  // FCM에서 받은 payload를 브라우저 알림으로 표시한다.
  // 링크는 notificationclick에서 사용해야 하므로 notification data에 함께 저장한다.
  event.waitUntil(
    self.registration.showNotification(title, {
      body,
      icon: "/favicon/android-chrome-192x192.png",
      badge: "/favicon/favicon-32x32.png",
      data: { linkUrlx },
    }),
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();

  const linkUrlx = event.notification.data?.linkUrlx || "/alim";
  const targetUrl = new URL(linkUrlx, self.location.origin).href;

  event.waitUntil(
    clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if ("focus" in client) {
          client.navigate(targetUrl);
          return client.focus();
        }
      }

      return clients.openWindow(targetUrl);
    }),
  );
});
