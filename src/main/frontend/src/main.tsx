// main.tsx
import "./app/styles/global.css";
import { BrowserRouter } from "react-router-dom";
import { createRoot } from "react-dom/client";
import { registerServiceWorker } from "./app/pwa/registerServiceWorker";
import App from "./App";

/**
 * 모바일 키보드와 브라우저 도구 모음을 제외한 실제 표시 높이를 CSS에 전달함
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
const syncViewportHeight = (): void => {
  const viewportHeight = window.visualViewport?.height ?? window.innerHeight;

  // 채팅과 댓글 화면이 현재 보이는 영역만 사용하도록 높이 변수를 갱신함
  document.documentElement.style.setProperty(
    "--app-viewport-height",
    `${Math.round(viewportHeight)}px`,
  );
};

// 애플리케이션 최초 표시 영역 높이를 채팅과 댓글 레이아웃에 전달함
syncViewportHeight();
// 일반 창 크기 변경 시 실제 표시 영역 높이를 다시 계산함
window.addEventListener("resize", syncViewportHeight);
// 모바일 키보드와 화면 확대가 표시 영역을 바꾸면 높이를 다시 계산함
window.visualViewport?.addEventListener("resize", syncViewportHeight);
// iOS가 입력창을 보이게 하려고 표시 영역을 이동할 때 높이를 다시 동기화함
window.visualViewport?.addEventListener("scroll", syncViewportHeight);

// 애플리케이션의 최상위 라우터와 화면을 루트 요소에 렌더링함
createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);

// PWA 캐시와 푸시 알림을 처리할 서비스 워커를 등록함
registerServiceWorker();
