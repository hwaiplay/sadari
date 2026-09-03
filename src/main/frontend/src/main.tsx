// main.tsx
import "./app/styles/global.css";
import { BrowserRouter } from "react-router-dom";
import { createRoot } from "react-dom/client";
import { registerServiceWorker } from "./app/pwa/registerServiceWorker";
import App from "./App";

// 애플리케이션의 최상위 라우터와 화면을 루트 요소에 렌더링함
createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);

// PWA 캐시와 푸시 알림을 처리할 서비스 워커를 등록함
registerServiceWorker();
