type SweetAlertIcon = "success" | "error" | "warning" | "info";

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
  error: "!",
  warning: "!",
  info: "i",
};

const ICON_CLASS: Record<SweetAlertIcon, string> = {
  success: "sadari-swal-icon-success",
  error: "sadari-swal-icon-error",
  warning: "sadari-swal-icon-warning",
  info: "sadari-swal-icon-info",
};

/**
 * SweetAlert 대체 모달에 필요한 공통 스타일 태그를 문서에 한 번만 삽입한다.
 * @Author Hanwon.Jang
 * @return 없음
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
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(0, 0, 0, 0.34);
      box-sizing: border-box;
    }

    .sadari-swal-modal {
      width: min(360px, 100%);
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
    }

    .sadari-swal-icon-success {
      color: #2f8f64;
    }

    .sadari-swal-icon-error {
      color: #c94b4b;
    }

    .sadari-swal-icon-warning {
      color: #c78722;
    }

    .sadari-swal-icon-info {
      color: #3a74a8;
      font-family: Georgia, serif;
      font-style: italic;
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
  `;

  document.head.appendChild(style);
}

/**
 * SweetAlert 모달을 닫고 body 스크롤 상태를 복구한다.
 * @Author Hanwon.Jang
 * @param overlay 제거할 모달 오버레이 엘리먼트
 * @param result 사용자 확인 여부 결과
 * @return SweetAlert 처리 결과
 */
function closeSweetAlert(overlay: HTMLDivElement, result: SweetAlertResult) {
  overlay.remove();
  document.body.style.overflow = "";
  return result;
}

/**
 * 제목, 본문, 아이콘, 확인/취소 버튼 옵션으로 커스텀 알림 모달을 표시한다.
 * @Author Hanwon.Jang
 * @param options 알림 모달 표시 옵션
 * @return 사용자의 확인 또는 취소 선택 결과 Promise
 */
export function sweetAlert(options: SweetAlertOptions) {
  ensureSweetAlertStyle();

  return new Promise<SweetAlertResult>((resolve) => {
    const overlay = document.createElement("div");
    const modal = document.createElement("div");
    const iconType = options.icon ?? "info";

    overlay.className = "sadari-swal-overlay";
    modal.className = "sadari-swal-modal";
    modal.setAttribute("role", "alertdialog");
    modal.setAttribute("aria-modal", "true");

    const icon = document.createElement("div");
    icon.className = `sadari-swal-icon ${ICON_CLASS[iconType]}`;
    icon.textContent = ICON_LABEL[iconType];

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
        resolve(closeSweetAlert(overlay, { isConfirmed: false }));
      });
      actions.appendChild(cancelButton);
    }

    const confirmButton = document.createElement("button");
    confirmButton.className = "sadari-swal-button";
    confirmButton.type = "button";
    confirmButton.textContent = options.confirmButtonText ?? "확인";
    confirmButton.addEventListener("click", () => {
      resolve(closeSweetAlert(overlay, { isConfirmed: true }));
    });
    actions.appendChild(confirmButton);

    overlay.addEventListener("click", (event) => {
      if (event.target === overlay && options.showCancelButton) {
        resolve(closeSweetAlert(overlay, { isConfirmed: false }));
      }
    });

    modal.addEventListener("click", (event) => {
      event.stopPropagation();
    });

    modal.appendChild(actions);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    document.body.style.overflow = "hidden";
    confirmButton.focus();
  });
}

/**
 * 취소 버튼이 포함된 확인 모달을 표시한다.
 * @Author Hanwon.Jang
 * @param options 확인 모달 표시 옵션
 * @return 사용자의 확인 또는 취소 선택 결과 Promise
 */
export function sweetConfirm(options: SweetAlertOptions) {
  return sweetAlert({
    icon: "warning",
    confirmButtonText: "확인",
    cancelButtonText: "취소",
    ...options,
    showCancelButton: true,
  });
}

/**
 * 성공 알림 모달을 표시한다.
 * @Author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetSuccess(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "success" });
}

/**
 * 오류 알림 모달을 표시한다.
 * @Author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetError(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "error" });
}

/**
 * 경고 알림 모달을 표시한다.
 * @Author Hanwon.Jang
 * @param title 알림 제목
 * @param text 알림 본문
 * @return 사용자 확인 결과 Promise
 */
export function sweetWarning(title: string, text?: string) {
  return sweetAlert({ title, text, icon: "warning" });
}
