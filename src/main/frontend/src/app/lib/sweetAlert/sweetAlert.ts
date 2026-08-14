import { lockBodyScroll, unlockBodyScroll } from "@/app/utils/modalUtil";

type SweetAlertIcon = "success" | "error" | "warning" | "info" | "question" | "loading";

type SweetAlertOptions = {
  title: string;
  text?: string;
  texts?: readonly string[];
  html?: string;
  content?: HTMLElement;
  customClass?: string;
  icon?: SweetAlertIcon;
  confirmButtonText?: string;
  denyButtonText?: string;
  cancelButtonText?: string;
  closeButtonLabel?: string;
  showCancelButton?: boolean;
  showCloseButton?: boolean;
  showConfirmButton?: boolean;
  showDenyButton?: boolean;
  allowOutsideClick?: boolean;
  closeSignal?: AbortSignal;
};

type SweetAlertResult = {
  isConfirmed: boolean;
  isDenied: boolean;
  isSecondaryAction: boolean;
  isDismissed: boolean;
};

const STYLE_ID = "sadari-sweet-alert-style";

const ICON_LABEL: Record<SweetAlertIcon, string> = {
  success: "✓",
  error: "X",
  warning: "!",
  info: "i",
  question: "?",
  loading: "",
};

const ICON_CLASS: Record<SweetAlertIcon, string> = {
  success: "sadari-swal-icon-success",
  error: "sadari-swal-icon-error",
  warning: "sadari-swal-icon-warning",
  info: "sadari-swal-icon-info",
  question: "sadari-swal-icon-question",
  loading: "sadari-swal-icon-loading",
};

/**
 * SweetAlert 모달에 필요한 스타일 태그를 한 번만 주입합니다.
 *
 * @author HanWon.Jang
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
      position: relative;
      width: min(360px, 100%);
      max-height: calc(100dvh - 48px);
      overflow-y: auto;
      border: 1px solid #e3e3e3;
      border-radius: 22px;
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

    .sadari-swal-icon-loading {
      border-color: #e8ddd0;
      border-top-color: #c99545;
      animation: sadari-swal-loading-spin 850ms linear infinite;
    }

    .sadari-swal-title {
      margin: 0;
      color: #293038;
      font-size: 18px;
      font-weight: 600;
      line-height: 1.35;
      letter-spacing: 0;
    }

    .sadari-swal-text {
      margin: 4px 0 0;
      color: #293038;
      font-size: 14px;
      line-height: 1.6;
      white-space: pre-line;
    }

    .sadari-swal-html {
      margin: 12px 0 0;
      color: #293038;
      font-size: 14px;
      line-height: 1.6;
      text-align: left;
    }

    .sadari-swal-edit-guide {
      position: relative;
      min-height: 108px;
      margin-top: 18px;
      border: 1px solid #e4ecea;
      border-radius: 16px;
      padding: 20px 24px;
      background: #f7f9f8;
      overflow: hidden;
      box-sizing: border-box;
    }

    .sadari-swal-edit-guide-target {
      position: relative;
      display: inline-flex;
      flex-direction: column;
      align-items: flex-start;
      isolation: isolate;
    }

    .sadari-swal-edit-guide-label,
    .sadari-swal-edit-guide-value {
      position: relative;
      z-index: 1;
    }

    .sadari-swal-edit-guide-label {
      color: #8a8f8d;
      font-size: 14px;
      font-weight: 600;
      line-height: 1.4;
    }

    .sadari-swal-edit-guide-value {
      margin-top: 8px;
      color: #1f1f1f;
      font-size: 19px;
      font-weight: 700;
      line-height: 1.4;
    }

    .sadari-swal-edit-guide-pulse {
      position: absolute;
      z-index: 0;
      left: 72%;
      bottom: 10px;
      width: 58px;
      height: 58px;
      border-radius: 50%;
      background: rgba(82, 193, 188, 0.6);
      transform: translate(-50%, 50%) scale(0.72);
      animation: sadari-swal-edit-guide-tap 1.55s ease-in-out infinite;
      pointer-events: none;
    }

    .sadari-swal-actions {
      display: flex;
      justify-content: center;
      gap: 10px;
      margin-top: 22px;
    }

    .sadari-swal-actions:empty {
      display: none;
    }

    .sadari-swal-logout .sadari-swal-actions {
      flex-direction: column-reverse;
    }

    .sadari-swal-logout .sadari-swal-button {
      width: 100%;
    }

    .sadari-swal-close {
      position: absolute;
      top: 16px;
      right: 16px;
      display: inline-flex;
      width: 36px;
      height: 36px;
      align-items: center;
      justify-content: center;
      border: 0;
      border-radius: 50%;
      padding: 0;
      background: transparent;
      cursor: pointer;
    }

    .sadari-swal-close:hover {
      background: #f3f4f5;
    }

    .sadari-swal-close:focus-visible {
      outline: 2px solid #78b991;
      outline-offset: 2px;
    }

    .sadari-swal-close img {
      display: block;
      width: 14px;
      height: 14px;
    }

    .sadari-swal-button {
      min-width: 86px;
      height: 42px;
      border-radius: 8px;
      border: 1px solid #293038;
      padding: 0 16px;
      background: #293038;
      color: #ffffff;
      font-size: 14px;
      font-weight: 500;
      line-height: 1;
      cursor: pointer;
      transition: background 160ms ease
    }

    .sadari-swal-button:hover {
      background: #171A1F;
    }

    .sadari-swal-user-block .sadari-swal-title {
      font-family: "PretendardSemiBold", system-ui, sans-serif;
      font-size: 20px;
      font-weight: 600;
      white-space: pre-line;
      margin-bottom: 24px !important;
    }

    .sadari-swal-user-block .sadari-swal-text {
      font-family: "PretendardRegular", system-ui, sans-serif;
      font-size: 14px;
      font-weight: 400;
      text-align: left !important;
      margin-top: 14px !important;
    }

    .sadari-swal-user-block .sadari-swal-button {
      font-family: "PretendardMedium", system-ui, sans-serif;
      font-size: 14px;
      font-weight: 500;
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

    @keyframes sadari-swal-edit-guide-tap {
      0%, 100% {
        opacity: 0.28;
        transform: translate(-50%, 50%) scale(0.72);
        box-shadow: 0 0 0 0 rgba(82, 193, 188, 0.26);
      }
      45% {
        opacity: 0.72;
        transform: translate(-50%, 50%) scale(1);
        box-shadow: 0 0 0 8px rgba(82, 193, 188, 0);
      }
      72% {
        opacity: 0.52;
        transform: translate(-50%, 50%) scale(0.88);
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

    .sadari-swal-deny {
      border-color: #b43f3f;
      background: #ffffff;
      color: #b43f3f;
      transition: background 160ms ease
    }

    .sadari-swal-deny:hover {
      background: #FFF1F3;
    }

    @keyframes sadari-swal-loading-spin {
      to {
        transform: rotate(360deg);
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .sadari-swal-edit-guide-pulse {
        animation: none;
        opacity: 0.58;
        transform: translate(-50%, 50%) scale(0.88);
      }
    }
  `;

  document.head.appendChild(style);
}

/**
 * SweetAlert 모달을 닫고 body 스크롤 상태를 복구합니다.
 *
 * @author HanWon.Jang
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
 * @author HanWon.Jang
 * @param options 알림 모달 표시 옵션
 * @return 사용자의 확인, 보조 선택 또는 바깥 클릭 취소 결과 Promise
 */
export function sweetAlert(options: SweetAlertOptions) {

  ensureSweetAlertStyle();

  return new Promise<SweetAlertResult>((resolve) => {

    // 호출 전에 작업이 끝난 안내 모달은 화면에 추가하지 않고 닫힘 결과로 완료한다
    if (options.closeSignal?.aborted) {
      // 이미 종료된 작업의 모달 Promise를 닫힘 상태로 완료한다
      resolve({
        isConfirmed: false,
        isDenied: false,
        isSecondaryAction: false,
        isDismissed: true,
      });
      // 종료된 작업의 DOM 생성을 차단하도록 모달 처리를 종료한다
      return;
    }

    const overlay = document.createElement("div");
    const modal = document.createElement("div");
    const iconType = options.icon ?? "info";
    let isClosed = false;
    let closeSignalHandler: (() => void) | null = null;

    /**
     * close 사용자 동작을 처리한다
     *
     * @author HanWon.Jang
     * @param result result 입력값
     * @return 반환값이 없다
     */
    const close = (result: SweetAlertResult) => {

      if (isClosed) {
        return;
      }

      isClosed = true;

      // 외부 작업 종료 신호 구독이 남아 다른 알림까지 닫지 않도록 현재 모달의 구독을 제거한다
      if (closeSignalHandler && options.closeSignal) {
        // 현재 모달에 연결한 작업 종료 이벤트만 해제한다
        options.closeSignal.removeEventListener("abort", closeSignalHandler);
      }

      resolve(closeSweetAlert(overlay, result));
    };

    /**
     * 비동기 작업이 끝나면 처리 중 모달을 사용자 입력 없이 닫는다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    closeSignalHandler = () => {

      // 완료된 작업의 안내 모달을 닫힘 상태로 정리한다
      close({
        isConfirmed: false,
        isDenied: false,
        isSecondaryAction: false,
        isDismissed: true,
      });
    };

    // 작업 종료 신호가 제공되면 현재 모달만 자동으로 닫을 수 있도록 구독한다
    if (options.closeSignal) {
      // 작업이 끝나는 최초 신호만 처리하도록 일회성 이벤트를 등록한다
      options.closeSignal.addEventListener("abort", closeSignalHandler, { once: true });
    }

    lockBodyScroll();
    overlay.className = "sadari-swal-overlay";
    modal.className = ["sadari-swal-modal", options.customClass]
      .filter(Boolean)
      .join(" ");
    modal.setAttribute("role", "alertdialog");
    modal.setAttribute("aria-modal", "true");
    modal.setAttribute("aria-busy", iconType === "loading" ? "true" : "false");
    modal.tabIndex = -1;

    /**
     * 우측 상단 닫기 버튼을 기존 취소 선택과 같은 결과로 처리한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const handleCloseButtonClick = (): void => {
      // 사용자의 닫기 선택으로 현재 알림을 취소 완료한다
      close({
        isConfirmed: false,
        isDenied: false,
        isSecondaryAction: true,
        isDismissed: false,
      });
    };

    // 하단 취소 버튼 대신 우측 상단 닫기 버튼을 요청한 모달에만 제공한다
    if (options.showCloseButton) {
      const closeButton = document.createElement("button");
      closeButton.className = "sadari-swal-close";
      closeButton.type = "button";
      closeButton.setAttribute("aria-label", options.closeButtonLabel ?? "취소");

      const closeIcon = document.createElement("img");
      closeIcon.src = "/img/icons/icon-close.svg";
      closeIcon.alt = "";
      closeIcon.setAttribute("aria-hidden", "true");

      closeButton.appendChild(closeIcon);
      closeButton.addEventListener("click", handleCloseButtonClick);
      modal.appendChild(closeButton);
    }

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

    if (options.content) {
      modal.appendChild(options.content);
    } else if (options.html) {
      const content = document.createElement("div");
      content.className = "sadari-swal-html";
      content.innerHTML = options.html;
      modal.appendChild(content);
    } else if (options.texts) {
      // 여러 안내 문구를 각각 독립된 문단으로 제공해 항목별 스타일과 접근성을 유지한다.
      options.texts.forEach((textContent) => {
        const text = document.createElement("p");
        text.className = "sadari-swal-text";
        text.textContent = textContent;
        modal.appendChild(text);
      });
    } else if (options.text) {
      const text = document.createElement("p");
      text.className = "sadari-swal-text";
      text.textContent = options.text;
      modal.appendChild(text);
    }

    const actions = document.createElement("div");
    actions.className = "sadari-swal-actions";
    let cancelButton: HTMLButtonElement | null = null;

    // 취소 또는 보조 동작이 필요한 모달에만 취소 버튼을 제공한다
    if (options.showCancelButton) {
      cancelButton = document.createElement("button");
      cancelButton.className = "sadari-swal-button sadari-swal-cancel";
      cancelButton.type = "button";
      cancelButton.textContent = options.cancelButtonText ?? "취소";
      cancelButton.addEventListener("click", () => {

        close({
          isConfirmed: false,
          isDenied: false,
          isSecondaryAction: true,
          isDismissed: false,
        });
      });
      actions.appendChild(cancelButton);
    }

    // 현재 동작과 구분되는 두 번째 확정 선택이 필요할 때 거부 버튼을 제공한다
    if (options.showDenyButton) {
      const denyButton = document.createElement("button");
      denyButton.className = "sadari-swal-button sadari-swal-deny";
      denyButton.type = "button";
      denyButton.textContent = options.denyButtonText ?? "다른 선택";
      denyButton.addEventListener("click", () => {

        // 사용자의 두 번째 확정 선택으로 현재 알림을 완료한다
        close({
          isConfirmed: false,
          isDenied: true,
          isSecondaryAction: false,
          isDismissed: false,
        });
      });
      actions.appendChild(denyButton);
    }

    let confirmButton: HTMLButtonElement | null = null;

    // 뒤로가기 취소 안내처럼 보조 버튼만 필요한 모달에는 확인 버튼을 만들지 않는다
    if (options.showConfirmButton !== false) {
      confirmButton = document.createElement("button");
      confirmButton.className = "sadari-swal-button";
      confirmButton.type = "button";
      confirmButton.textContent = options.confirmButtonText ?? "확인";
      confirmButton.addEventListener("click", () => {

        // 사용자의 확인 선택으로 현재 알림을 완료한다
        close({
          isConfirmed: true,
          isDenied: false,
          isSecondaryAction: false,
          isDismissed: false,
        });
      });
      actions.appendChild(confirmButton);
    }

    overlay.addEventListener("click", (event) => {

      // 작업 중 뒤로가기 차단 모달은 바깥 영역을 눌러도 유지하고 일반 알림만 닫는다
      if (event.target === overlay && options.allowOutsideClick !== false) {
        close({
          isConfirmed: false,
          isDenied: false,
          isSecondaryAction: false,
          isDismissed: true,
        });
      }
    });

    modal.addEventListener("click", (event) => {

      event.stopPropagation();
    });

    // 버튼이 없는 처리 중 모달에는 빈 버튼 영역을 추가하지 않는다
    if (actions.childElementCount > 0) {
      // 사용자가 선택할 수 있는 알림 동작을 모달 하단에 추가한다
      modal.appendChild(actions);
    }

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    // 사용 가능한 기본 동작이 있으면 해당 버튼에 포커스를 두고 버튼이 없으면 모달 본문을 선택한다
    if (confirmButton) {
      // 일반 알림의 기본 확인 동작에 포커스를 설정한다
      confirmButton.focus();
    } else {
      // 버튼 없는 처리 중 모달은 모달 본문에 포커스를 두어 배경 화면 조작을 차단한다
      modal.focus();
    }
  });
}

type SweetBlockingOperationOptions = {
  title: string;
  text?: string;
  closeSignal: AbortSignal;
};

/**
 * 상태 변경 작업이 끝날 때까지 닫을 수 없는 처리 중 모달을 표시한다
 *
 * @author SeungHyeon.Kang
 * @param options 처리 중 문구와 작업 종료 신호
 * @return 작업 완료 신호에 따라 자동으로 닫히는 알림 처리 결과 Promise
 */
export function sweetBlockingOperation(options: SweetBlockingOperationOptions) {

  // 사용자가 서버 처리 도중 화면을 이탈하지 않도록 모든 닫기 버튼을 제거한 모달을 반환한다
  return sweetAlert({
    title: options.title,
    text: options.text,
    icon: "loading",
    showCancelButton: false,
    showConfirmButton: false,
    allowOutsideClick: false,
    closeSignal: options.closeSignal,
  });
}

/**
 * 취소 버튼을 포함한 확인 모달을 표시합니다.
 *
 * @author HanWon.Jang
 * @param options 확인 모달 표시 옵션
 * @return 사용자의 확인 또는 취소 선택 결과 Promise
 */
export function sweetConfirm(options: SweetAlertOptions) {

  // 호출 화면이 취소 버튼 노출 여부를 지정하면 해당 설정을 유지한다
  return sweetAlert({
    icon: "question",
    confirmButtonText: "확인",
    cancelButtonText: "취소",
    ...options,
    showCancelButton: options.showCancelButton ?? true,
  });
}

/**
 * 성공 알림 모달을 표시합니다.
 *
 * @author HanWon.Jang
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
 * @author HanWon.Jang
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
 * @author HanWon.Jang
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
 * @author HanWon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetInfo(title: string, text?: string) {

  return sweetAlert({ title, text, icon: "info" });
}

/**
 * 수정 가능한 예시 요소와 클릭 강조 애니메이션을 포함한 안내 모달을 표시합니다.
 *
 * @author HanWon.Jang
 * @param title 안내 모달 제목
 * @param fieldLabel 수정 가능한 예시 요소의 항목명
 * @param fieldValue 수정 가능한 예시 요소의 현재값
 * @return 사용자 확인 결과 Promise
 */
export function sweetEditGuide(
  title: string,
  fieldLabel: string,
  fieldValue: string,
) {

  // 번역된 문구를 HTML 문자열로 해석하지 않도록 안내 콘텐츠를 DOM 엘리먼트로 구성한다
  const content = document.createElement("div");
  content.className = "sadari-swal-html sadari-swal-edit-guide";
  content.setAttribute("aria-hidden", "true");

  const target = document.createElement("div");
  target.className = "sadari-swal-edit-guide-target";

  const label = document.createElement("span");
  label.className = "sadari-swal-edit-guide-label";
  label.textContent = fieldLabel;

  const value = document.createElement("strong");
  value.className = "sadari-swal-edit-guide-value";
  value.textContent = fieldValue;

  const pulse = document.createElement("span");
  pulse.className = "sadari-swal-edit-guide-pulse";

  // target.append(label, value, pulse);
  // content.appendChild(target);

  // 클릭 가능한 요소의 시각적 예시를 포함한 안내 모달을 반환한다
  return sweetAlert({
    title,
    icon: "info",
  });
}

/**
 * 질문 알림 모달을 표시합니다.
 *
 * @author HanWon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetQuestion(title: string, text?: string) {

  return sweetAlert({ title, text, icon: "question" });
}
