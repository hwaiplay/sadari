// main.tsx
import "./app/styles/global.css";
import { BrowserRouter } from "react-router-dom";
import ReactDOM from "react-dom/client";
import { registerServiceWorker } from "./app/pwa/registerServiceWorker";
import App from "./App";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);

registerServiceWorker();
