// main.tsx
import "./app/styles/global.css";
import { BrowserRouter } from "react-router-dom";
import { createRoot } from "react-dom/client";
import { registerServiceWorker } from "./app/pwa/registerServiceWorker";
import App from "./App";
import {
  getMessageLocale,
  MESSAGE_LOCALE_CHANGE_EVENT,
} from "./app/messages/message";

/**
 * 모바일 키보드와 브라우저 도구 모음을 제외한 실제 표시 높이를 CSS에 전달
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
const syncViewportHeight = (): void => {
  const viewportHeight = window.visualViewport?.height ?? window.innerHeight;

  // 채팅과 댓글 화면이 현재 보이는 영역만 사용하도록 높이 변수를 갱신
  document.documentElement.style.setProperty(
    "--app-viewport-height",
    `${Math.round(viewportHeight)}px`,
  );
};

// 애플리케이션 최초 표시 영역 높이를 채팅과 댓글 레이아웃에 전달
syncViewportHeight();
// 일반 창 크기 변경 시 실제 표시 영역 높이를 다시 계산
window.addEventListener("resize", syncViewportHeight);
// 모바일 키보드와 화면 확대가 표시 영역을 바꾸면 높이를 다시 계산
window.visualViewport?.addEventListener("resize", syncViewportHeight);
// iOS가 입력창을 보이게 하려고 표시 영역을 이동할 때 높이를 다시 동기화
window.visualViewport?.addEventListener("scroll", syncViewportHeight);

// 언어 변경 시 공통 헤더와 하단 메뉴까지 새 메시지로 다시 렌더링할 루트를 생성
const root = createRoot(document.getElementById("root")!);

/** 현재 메시지 언어를 키로 사용해 전체 화면을 렌더링 */
const renderApp = (): void => {
  root.render(
    <BrowserRouter>
      <App key={getMessageLocale()} />
    </BrowserRouter>,
  );
};

// 최초 화면을 현재 기기 또는 저장된 계정 언어로 렌더링
renderApp();
// 계정 언어 변경 시 메모된 공통 레이아웃까지 새 언어로 교체
window.addEventListener(MESSAGE_LOCALE_CHANGE_EVENT, renderApp);

// PWA 캐시와 푸시 알림을 처리할 서비스 워커를 등록
registerServiceWorker();
