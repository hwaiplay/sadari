import { lockBodyScroll, unlockBodyScroll } from "@/app/utils/modalUtil";

type SweetAlertIcon = "success" | "error" | "warning" | "info" | "question";

type SweetAlertOptions = {
  title: string;
  text?: string;
  html?: string;
  icon?: SweetAlertIcon;
  confirmButtonText?: string;
  cancelButtonText?: string;
  showCancelButton?: boolean;
};

type SweetAlertResult = {
  isConfirmed: boolean;
};

const STYLE_ID = "sadari-sweet-alert-style";

const ICON_LABEL: Record<SweetAlertIcon, string> = {
  success: "✓",
  error: "X",
  warning: "!",
  info: "i",
  question: "?",
};

const ICON_CLASS: Record<SweetAlertIcon, string> = {
  success: "sadari-swal-icon-success",
  error: "sadari-swal-icon-error",
  warning: "sadari-swal-icon-warning",
  info: "sadari-swal-icon-info",
  question: "sadari-swal-icon-question",
};

/**
 * SweetAlert 모달에 필요한 스타일 태그를 한 번만 주입합니다.
 *
 * @author Hanwon.Jang
 * @return
 */
function ensureSweetAlertStyle() {
  if (document.getElementById(STYLE_ID)) {
    return;
  }

  const style = document.createElement("style");
  style.id = STYLE_ID;
  style.textContent = `
    .sadari-swal-overlay {
      position: fixed;
      inset: 0;
      width: 100vw;
      height: 100dvh;
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(0, 0, 0, 0.34);
      box-sizing: border-box;
      overflow: hidden;
      overscroll-behavior: contain;
    }

    .sadari-swal-modal {
      width: min(360px, 100%);
      max-height: calc(100dvh - 48px);
      overflow-y: auto;
      border: 1px solid #e3e3e3;
      border-radius: 18px;
      background: #ffffff;
      padding: 26px 24px 22px;
      box-shadow: 0 24px 60px rgba(0, 0, 0, 0.22);
      text-align: center;
      box-sizing: border-box;
      animation: sadari-swal-open 150ms ease-out;
    }

    .sadari-swal-icon {
      width: 58px;
      height: 58px;
      margin: 0 auto 16px;
      border: 2px solid currentColor;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30px;
      font-weight: 700;
      line-height: 1;
      box-sizing: border-box;
      transform-origin: center;
      animation: sadari-swal-icon-show 300ms ease-out both;
    }

    .sadari-swal-icon-label {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      line-height: 1;
      transform-origin: center;
    }

    .sadari-swal-icon-success {
      color: #2f8f64;
      animation: sadari-swal-icon-show 300ms ease-out both, sadari-swal-success-ring 700ms ease-out 120ms both;
    }

    .sadari-swal-icon-success .sadari-swal-icon-label {
      font-size: 38px;
      font-weight: 600;
      animation: sadari-swal-success-mark 520ms ease-out 150ms both;
    }

    .sadari-swal-icon-error {
      color: #c94b4b;
      animation: sadari-swal-icon-show 300ms ease-out both, sadari-swal-error-shake 650ms ease-out 120ms both;
    }

    .sadari-swal-icon-error .sadari-swal-icon-label {
      font-size: 42px;
      font-weight: 300;
      animation: sadari-swal-error-mark 420ms ease-out 150ms both;
    }

    .sadari-swal-icon-warning {
      color: #c78722;
      animation: sadari-swal-icon-show 300ms ease-out both, sadari-swal-warning-pulse 900ms ease-out 130ms both;
    }

    .sadari-swal-icon-warning .sadari-swal-icon-label {
      animation: sadari-swal-pop-mark 450ms ease-out 160ms both;
    }

    .sadari-swal-icon-info {
      color: #3fc3ee;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 35px;
      font-style: normal;
      font-weight: 400;
      line-height: 58px;
      animation: sadari-swal-icon-show 300ms ease-out both, sadari-swal-info-pulse 700ms ease-out 120ms both;
    }

    .sadari-swal-icon-info .sadari-swal-icon-label {
      animation: sadari-swal-info-mark 500ms ease-out 160ms both;
    }

    .sadari-swal-icon-question {
      color: #4b6fbd;
      animation: sadari-swal-icon-show 300ms ease-out both, sadari-swal-question-bounce 680ms ease-out 120ms both;
    }

    .sadari-swal-icon-question .sadari-swal-icon-label {
      animation: sadari-swal-pop-mark 500ms ease-out 160ms both;
    }

    .sadari-swal-title {
      margin: 0;
      color: #1f1f1f;
      font-size: 19px;
      font-weight: 700;
      line-height: 1.35;
      letter-spacing: 0;
    }

    .sadari-swal-text {
      margin: 12px 0 0;
      color: #5f6368;
      font-size: 14px;
      line-height: 1.6;
      white-space: pre-line;
    }

    .sadari-swal-html {
      margin: 12px 0 0;
      color: #5f6368;
      font-size: 14px;
      line-height: 1.6;
      text-align: left;
    }

    .sadari-swal-actions {
      display: flex;
      justify-content: center;
      gap: 10px;
      margin-top: 22px;
    }

    .sadari-swal-button {
      min-width: 86px;
      height: 38px;
      border-radius: 999px;
      border: 1px solid #1f1f1f;
      padding: 0 16px;
      background: #1f1f1f;
      color: #ffffff;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
    }

    .sadari-swal-cancel {
      border-color: #cfd4d9;
      background: #ffffff;
      color: #1f1f1f;
    }

    @keyframes sadari-swal-open {
      from {
        opacity: 0;
        transform: translateY(8px) scale(0.98);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    @keyframes sadari-swal-icon-show {
      0% {
        opacity: 0;
        transform: scale(0.48);
      }
      45% {
        opacity: 1;
        transform: scale(1.08);
      }
      80% {
        transform: scale(0.96);
      }
      100% {
        opacity: 1;
        transform: scale(1);
      }
    }

    @keyframes sadari-swal-success-ring {
      0% {
        box-shadow: 0 0 0 0 rgba(47, 143, 100, 0.28);
      }
      55% {
        box-shadow: 0 0 0 12px rgba(47, 143, 100, 0);
      }
      100% {
        box-shadow: 0 0 0 0 rgba(47, 143, 100, 0);
      }
    }

    @keyframes sadari-swal-success-mark {
      0% {
        opacity: 0;
        transform: rotate(-35deg) scale(0.35);
      }
      55% {
        opacity: 1;
        transform: rotate(8deg) scale(1.16);
      }
      100% {
        opacity: 1;
        transform: rotate(0deg) scale(1);
      }
    }

    @keyframes sadari-swal-error-shake {
      0%, 100% {
        transform: scale(1) translateX(0);
      }
      20% {
        transform: scale(1) translateX(-7px);
      }
      40% {
        transform: scale(1) translateX(7px);
      }
      60% {
        transform: scale(1) translateX(-4px);
      }
      80% {
        transform: scale(1) translateX(4px);
      }
    }

    @keyframes sadari-swal-error-mark {
      0% {
        opacity: 0;
        transform: rotate(45deg) scale(0.45);
      }
      70% {
        opacity: 1;
        transform: rotate(0deg) scale(1.12);
      }
      100% {
        opacity: 1;
        transform: rotate(0deg) scale(1);
      }
    }

    @keyframes sadari-swal-warning-pulse {
      0% {
        box-shadow: 0 0 0 0 rgba(199, 135, 34, 0.32);
      }
      45% {
        transform: scale(1.04);
        box-shadow: 0 0 0 10px rgba(199, 135, 34, 0);
      }
      100% {
        transform: scale(1);
        box-shadow: 0 0 0 0 rgba(199, 135, 34, 0);
      }
    }

    @keyframes sadari-swal-info-pulse {
      0% {
        box-shadow: 0 0 0 0 rgba(63, 195, 238, 0.28);
      }
      55% {
        box-shadow: 0 0 0 11px rgba(63, 195, 238, 0);
      }
      100% {
        box-shadow: 0 0 0 0 rgba(63, 195, 238, 0);
      }
    }

    @keyframes sadari-swal-info-mark {
      0% {
        opacity: 0;
        transform: translateY(-18px);
      }
      60% {
        opacity: 1;
        transform: translateY(4px);
      }
      100% {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes sadari-swal-question-bounce {
      0%, 100% {
        transform: scale(1) rotateY(0deg);
      }
      45% {
        transform: scale(1.04) rotateY(-14deg);
      }
      70% {
        transform: scale(0.98) rotateY(10deg);
      }
    }

    @keyframes sadari-swal-pop-mark {
      0% {
        opacity: 0;
        transform: scale(0.25);
      }
      60% {
        opacity: 1;
        transform: scale(1.18);
      }
      100% {
        opacity: 1;
        transform: scale(1);
      }
    }
  `;

  document.head.appendChild(style);
}

/**
 * SweetAlert 모달을 닫고 body 스크롤 상태를 복구합니다.
 *
 * @author Hanwon.Jang
 * @param overlay 제거할 모달 오버레이 엘리먼트
 * @param result 사용자의 확인 여부 결과
 * @return SweetAlert 처리 결과
 */
function closeSweetAlert(overlay: HTMLDivElement, result: SweetAlertResult) {
  overlay.remove();
  unlockBodyScroll();
  return result;
}

/**
 * 제목, 본문, 아이콘, 확인/취소 버튼 옵션으로 커스텀 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param options 알림 모달 표시 옵션
 * @return 사용자의 확인 또는 취소 선택 결과 Promise
 */
export function sweetAlert(options: SweetAlertOptions) {
  ensureSweetAlertStyle();

  return new Promise<SweetAlertResult>((resolve) => {
    const overlay = document.createElement("div");
    const modal = document.createElement("div");
    const iconType = options.icon ?? "info";
    let isClosed = false;

    const close = (result: SweetAlertResult) => {
      if (isClosed) {
        return;
      }

      isClosed = true;
      resolve(closeSweetAlert(overlay, result));
    };

    lockBodyScroll();
    overlay.className = "sadari-swal-overlay";
    modal.className = "sadari-swal-modal";
    modal.setAttribute("role", "alertdialog");
    modal.setAttribute("aria-modal", "true");

    const icon = document.createElement("div");
    icon.className = `sadari-swal-icon ${ICON_CLASS[iconType]}`;

    const iconLabel = document.createElement("span");
    iconLabel.className = "sadari-swal-icon-label";
    iconLabel.textContent = ICON_LABEL[iconType];
    icon.appendChild(iconLabel);

    const title = document.createElement("h2");
    title.className = "sadari-swal-title";
    title.textContent = options.title;

    modal.append(icon, title);

    if (options.html) {
      const content = document.createElement("div");
      content.className = "sadari-swal-html";
      content.innerHTML = options.html;
      modal.appendChild(content);
    } else if (options.text) {
      const text = document.createElement("p");
      text.className = "sadari-swal-text";
      text.textContent = options.text;
      modal.appendChild(text);
    }

    const actions = document.createElement("div");
    actions.className = "sadari-swal-actions";

    if (options.showCancelButton) {
      const cancelButton = document.createElement("button");
      cancelButton.className = "sadari-swal-button sadari-swal-cancel";
      cancelButton.type = "button";
      cancelButton.textContent = options.cancelButtonText ?? "취소";
      cancelButton.addEventListener("click", () => {
        close({ isConfirmed: false });
      });
      actions.appendChild(cancelButton);
    }

    const confirmButton = document.createElement("button");
    confirmButton.className = "sadari-swal-button";
    confirmButton.type = "button";
    confirmButton.textContent = options.confirmButtonText ?? "확인";
    confirmButton.addEventListener("click", () => {
      close({ isConfirmed: true });
    });
    actions.appendChild(confirmButton);

    overlay.addEventListener("click", (event) => {
      if (event.target === overlay && options.showCancelButton) {
        close({ isConfirmed: false });
      }
    });

    modal.addEventListener("click", (event) => {
      event.stopPropagation();
    });

    modal.appendChild(actions);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    confirmButton.focus();
  });
}

/**
 * 취소 버튼을 포함한 확인 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param options 확인 모달 표시 옵션
 * @return 사용자의 확인 또는 취소 선택 결과 Promise
 */
export function sweetConfirm(options: SweetAlertOptions) {
  return sweetAlert({
    icon: "question",
    confirmButtonText: "확인",
    cancelButtonText: "취소",
    ...options,
    showCancelButton: true,
  });
}

/**
 * 성공 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetSuccess(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "success" });
}

/**
 * 오류 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetError(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "error" });
}

/**
 * 경고 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetWarning(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "warning" });
}

/**
 * 안내 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetInfo(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "info" });
}

/**
 * 질문 알림 모달을 표시합니다.
 *
 * @author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetQuestion(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "question" });
}
