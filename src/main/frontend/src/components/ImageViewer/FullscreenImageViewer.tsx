import { message } from "@/app/messages/message";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import { clsx } from "clsx";
import {
  createContext,
  type ButtonHTMLAttributes,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useId,
  useLayoutEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { createPortal } from "react-dom";
import * as styles from "./FullscreenImageViewer.css";

export type FullscreenImageRequest = {
  source: string;
  fallbackSource?: string;
  alt: string;
  actions?: ReactNode;
};

type ActiveImageRequest = FullscreenImageRequest & {
  triggerId: string;
};

type FullscreenImageViewerContextValue = {
  openImageViewer: (request: FullscreenImageRequest, triggerId: string) => void;
  updateImageViewer: (request: FullscreenImageRequest, triggerId: string) => void;
};

type ImageViewerProviderProps = {
  children: ReactNode;
};

type FullscreenImageButtonProps = Omit<
  ButtonHTMLAttributes<HTMLButtonElement>,
  "aria-label" | "children" | "onClick" | "type"
> & FullscreenImageRequest & {
  children: ReactNode;
  ariaLabel?: string;
  initiallyOpen?: boolean;
};

const FullscreenImageViewerContext = createContext<
  FullscreenImageViewerContextValue | undefined
>(undefined);

/**
 * 앱 전체에서 공통으로 사용할 전체 화면 원본 이미지 뷰어를 제공한다.
 *
 * @author SeungHyeon.Kang
 * @param props 하위 화면 구성 요소
 * @return 이미지 뷰어 컨텍스트와 하위 화면
 */
export function ImageViewerProvider({
  children,
}: ImageViewerProviderProps) {

  const [imageRequest, setImageRequest] = useState<ActiveImageRequest | null>(null);
  const [activeSource, setActiveSource] = useState("");
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const returnFocusRef = useRef<HTMLElement | null>(null);
  const isOpen = imageRequest !== null;

  // 전체 화면 이미지가 열려 있는 동안 배경 문서의 스크롤을 잠근다.
  useBodyScrollLock(isOpen);

  useEffect(
    /**
     * 전체 화면이 열린 동안 기존 앱 화면을 조작 및 접근성 대상에서 제외한다.
     *
     * @author SeungHyeon.Kang
     * @return 배경 화면 상태를 복원하는 정리 함수 또는 반환값 없음
     */
    function manageBackgroundLock() {

      // 뷰어가 닫혀 있으면 기존 앱 화면의 접근성과 조작 상태를 변경하지 않는다.
      if (!isOpen) {
        // 등록할 정리 함수 없이 종료한다.
        return undefined;
      }

      const appRoot = document.getElementById("root");

      // 앱 루트가 없으면 전체 화면 표시 외의 배경 잠금 처리를 생략한다.
      if (!appRoot) {
        // 등록할 정리 함수 없이 종료한다.
        return undefined;
      }

      const lockedAppRoot = appRoot;
      const previousAriaHidden = lockedAppRoot.getAttribute("aria-hidden");
      const hadInertAttribute = lockedAppRoot.hasAttribute("inert");

      // Portal 바깥의 헤더와 내비게이션을 보조기기와 키보드 조작 대상에서도 제외한다.
      lockedAppRoot.setAttribute("inert", "");
      // 전체 화면 뷰어가 열린 동안 기존 앱 화면을 접근성 트리에서 숨긴다.
      lockedAppRoot.setAttribute("aria-hidden", "true");

      /**
       * 이미지 뷰어가 닫힐 때 앱 루트의 기존 접근성과 조작 상태를 복원한다.
       *
       * @author SeungHyeon.Kang
       * @return 반환값이 없다
       */
      function restoreBackgroundLock(): void {

        // 앱 루트에 원래 inert 속성이 없었다면 뷰어가 추가한 조작 잠금을 제거한다.
        if (!hadInertAttribute) {
          // 기존 화면의 포인터와 키보드 조작을 다시 허용한다.
          lockedAppRoot.removeAttribute("inert");
        }

        // 기존 aria-hidden 값이 없었다면 임시 접근성 속성을 제거한다.
        if (previousAriaHidden === null) {
          // 뷰어가 추가한 접근성 숨김 속성만 제거한다.
          lockedAppRoot.removeAttribute("aria-hidden");
          // 별도의 기존 값 복원 없이 종료한다.
          return;
        }

        // 앱 루트가 원래 가지고 있던 접근성 숨김 값을 되돌린다.
        lockedAppRoot.setAttribute("aria-hidden", previousAriaHidden);
      }

      // Effect 해제 시 기존 앱 화면 상태를 되돌릴 정리 함수를 반환한다.
      return restoreBackgroundLock;
    },
    [isOpen],
  );

  /**
   * 선택한 이미지를 전체 화면 원본 보기 상태로 연다.
   *
   * @author SeungHyeon.Kang
   * @param request 원본 및 대체 이미지 정보
   * @param triggerId 전체 화면 뷰어를 연 이미지 버튼 식별값
   * @return 반환값이 없다
   */
  const openImageViewer = useCallback((request: FullscreenImageRequest, triggerId: string): void => {

    // 뷰어를 닫은 뒤 사용자가 이미지를 열었던 요소로 초점을 되돌릴 수 있게 보관한다.
    returnFocusRef.current = document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;
    // 새 이미지의 원본 경로를 즉시 표시 경로로 사용한다.
    setActiveSource(request.source);
    // 헤더와 내비게이션 위에 전체 화면 이미지 뷰어를 표시한다.
    setImageRequest({ ...request, triggerId });
  }, []);

  /**
   * 열려 있는 이미지 버튼의 최신 반응 버튼 상태를 전체 화면 뷰어에 동기화한다.
   *
   * @author HanWon.Jang
   * @param request 최신 이미지와 반응 버튼 정보
   * @param triggerId 전체 화면 뷰어를 연 이미지 버튼 식별값
   * @return 반환값이 없다
   */
  const updateImageViewer = useCallback((request: FullscreenImageRequest, triggerId: string): void => {
    // 현재 뷰어를 연 버튼만 좋아요와 댓글 집계 변경을 반영한다
    setImageRequest(
      /**
       * 현재 열린 이미지와 같은 버튼의 최신 요청만 반영한다
       *
       * @author HanWon.Jang
       * @param currentRequest 현재 전체 화면 이미지 요청
       * @return 최신 반응 버튼을 반영한 전체 화면 이미지 요청
       */
      (currentRequest) => {
        // 닫힌 뷰어이거나 다른 이미지 버튼의 변경이면 현재 뷰어 상태를 유지한다
        if (!currentRequest || currentRequest.triggerId !== triggerId) {
          // 다른 이미지의 전체 화면 표시 내용을 그대로 반환한다
          return currentRequest;
        }

        // 현재 사진의 최신 반응 버튼을 포함한 요청으로 뷰어 내용을 갱신한다
        return { ...request, triggerId };
      },
    );
  }, []);

  /**
   * 전체 화면 이미지 뷰어를 닫고 기존 화면으로 돌아간다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const closeImageViewer = useCallback((): void => {

    // 전체 화면 이미지와 블러 배경을 함께 제거한다.
    setImageRequest(null);
  }, []);

  /**
   * 원본 이미지 로드 실패 시 지정된 공통 대체 이미지로 한 번 교체한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function handleImageError(): void {

    const fallbackSource = imageRequest?.fallbackSource?.trim();

    // 대체 경로가 없거나 이미 대체 이미지를 표시 중이면 반복 요청 없이 종료한다.
    if (!fallbackSource || activeSource === fallbackSource) {
      // 추가 이미지 상태 변경 없이 종료한다.
      return;
    }

    // 원본 대신 화면 유형에 맞는 공통 대체 이미지를 전경에 적용한다.
    setActiveSource(fallbackSource);
  }

  useLayoutEffect(
    /**
     * 전체 화면이 열린 직후 닫기 버튼으로 키보드 초점을 이동한다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function focusViewerCloseButton(): void {

      // 뷰어가 닫혀 있으면 닫기 버튼으로 초점을 이동하지 않는다.
      if (!isOpen) {
        // 불필요한 초점 처리를 생략한다.
        return;
      }

      // 전체 화면이 열린 직후 유일한 명령인 닫기 버튼에 키보드 초점을 둔다.
      closeButtonRef.current?.focus();
    },
    [isOpen],
  );

  useEffect(
    /**
     * 전체 화면이 열린 동안 Escape 키 닫기 동작을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @return 키보드 감시를 제거하는 정리 함수 또는 반환값 없음
     */
    function manageEscapeKey() {

      // 뷰어가 열려 있지 않으면 Escape 입력 감시를 등록하지 않는다.
      if (!isOpen) {
        // 등록할 정리 함수 없이 종료한다.
        return undefined;
      }

    /**
     * Escape 키로 전체 화면 이미지 보기를 닫는다.
     *
     * @author SeungHyeon.Kang
     * @param event 문서 키보드 입력 이벤트
     * @return 반환값이 없다
     */
      function handleDocumentKeyDown(event: KeyboardEvent): void {

        // Escape 이외의 키 입력은 현재 화면 동작에 맡긴다.
        if (event.key !== "Escape") {
          // 이미지 뷰어 닫기 없이 종료한다.
          return;
        }

        // 좋아요 사용자 목록이나 댓글 바텀시트가 위에 열려 있으면 해당 모달이 Escape 입력을 처리한다
        if (document.querySelector("[data-image-viewer-overlay='true']")) {
          // 이미지 뷰어는 하위 모달이 닫힐 때까지 현재 상태를 유지한다
          return;
        }

        // 브라우저의 다른 Escape 기본 동작보다 이미지 뷰어 닫기를 우선한다.
        event.preventDefault();
        // 전체 화면 이미지 뷰어를 닫는다.
        closeImageViewer();
      }

      // 전체 화면이 열린 동안에만 Escape 키 입력을 감시한다.
      document.addEventListener("keydown", handleDocumentKeyDown);

      /**
       * 이미지 뷰어가 닫힐 때 문서의 Escape 키 감시를 제거한다.
       *
       * @author SeungHyeon.Kang
       * @return 반환값이 없다
       */
      function removeEscapeKeyListener(): void {

        // 뷰어가 닫히거나 컴포넌트가 해제되면 키 입력 감시를 제거한다.
        document.removeEventListener("keydown", handleDocumentKeyDown);
      }

      // Effect 해제 시 문서 키보드 감시를 제거할 정리 함수를 반환한다.
      return removeEscapeKeyListener;
    },
    [closeImageViewer, isOpen],
  );

  useEffect(
    /**
     * 이미지 뷰어가 닫힌 뒤 사용자가 열었던 버튼으로 초점을 되돌린다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function restoreImageTriggerFocus(): void {

      // 뷰어가 열려 있는 동안에는 원래 요소로 초점을 돌리지 않는다.
      if (isOpen) {
        // 닫힐 때까지 초점 복원을 보류한다.
        return;
      }

      // 뷰어를 연 버튼이 아직 화면에 있으면 키보드 탐색 위치를 복원한다.
      returnFocusRef.current?.focus();
      // 다음 열기 동작과 섞이지 않도록 복원 대상 참조를 비운다.
      returnFocusRef.current = null;
    },
    [isOpen],
  );

  // 뷰어 상태 변경이 이미지 버튼 전체의 불필요한 재렌더링으로 이어지지 않도록 컨텍스트 값을 고정한다
  const contextValue = useMemo<FullscreenImageViewerContextValue>(
    /**
     * 전체 화면 이미지 버튼이 공유할 안정적인 뷰어 명령을 구성한다
     *
     * @author HanWon.Jang
     * @return 전체 화면 이미지 열기와 갱신 명령
     */
    () => ({
      openImageViewer,
      updateImageViewer,
    }),
    [openImageViewer, updateImageViewer],
  );

  return (
    <FullscreenImageViewerContext.Provider value={contextValue}>
      {children}
      {imageRequest && createPortal(
        /* 원본 이미지를 헤더와 내비게이션 위에 표시하는 전체 화면 영역 */
        <section
          className={styles.viewer}
          role="dialog"
          aria-modal="true"
          aria-label={/* "원본 이미지 전체 화면 보기" */ message("frontend.imageViewer.dialogLabel")}
        >
          {imageRequest.actions ? (
            /* 사진과 겹치지 않는 우하단 반응 버튼을 포함한 원본 이미지 영역 */
            <div className={styles.imageViewport}>
              <figure className={styles.imageFrame}>
                {/* 반응 버튼 높이를 제외한 영역에 비율을 유지하며 표시하는 원본 이미지 */}
                <img
                  className={styles.originalImageWithActions}
                  src={activeSource}
                  onError={handleImageError}
                  alt={imageRequest.alt}
                  draggable="false"
                />
                {/* 현재 프로필 또는 배경사진의 좋아요와 댓글 영역 */}
                <div className={styles.viewerActions}>{imageRequest.actions}</div>
              </figure>
            </div>
          ) : (
            /* 반응 버튼이 없는 원본 이미지를 기존 전체 화면 크기로 표시하는 영역 */
            <img
              className={styles.originalImage}
              src={activeSource}
              onError={handleImageError}
              alt={imageRequest.alt}
              draggable="false"
            />
          )}

          {/* 전체 화면 원본 이미지 닫기 버튼 */}
          <button
            ref={closeButtonRef}
            className={styles.closeButton}
            type="button"
            aria-label={/* "닫기" */ message("frontend.common.close")}
            title={/* "닫기" */ message("frontend.common.close")}
            onClick={closeImageViewer}
          >
            <img
              className={styles.closeIcon}
              src="/img/icons/icon-close.svg"
              alt=""
              aria-hidden="true"
            />
          </button>
        </section>,
        document.body,
      )}
    </FullscreenImageViewerContext.Provider>
  );
}

/**
 * 전체 화면 원본 이미지 뷰어 열기 함수를 반환한다.
 *
 * @author SeungHyeon.Kang
 * @return 전체 화면 이미지 뷰어 컨텍스트
 */
export function useFullscreenImageViewer(): FullscreenImageViewerContextValue {

  const context = useContext(FullscreenImageViewerContext);

  // 최상위 공급자 밖에서 호출된 잘못된 구성은 개발 단계에서 즉시 알린다.
  if (!context) {
    // 필수 공급자가 누락된 상태로 화면을 계속 렌더링하지 않는다.
    throw new Error("ImageViewerProvider is required.");
  }

  // 공통 뷰어 열기 함수를 사용하는 화면에 반환한다.
  return context;
}

/**
 * 전달받은 이미지 영역을 전체 화면 원본 보기 버튼으로 제공한다.
 *
 * @author SeungHyeon.Kang
 * @param props 이미지 정보와 버튼 표시 내용
 * @return 전체 화면 이미지 보기 버튼
 */
export function FullscreenImageButton({
  source,
  fallbackSource,
  alt,
  children,
  ariaLabel,
  initiallyOpen = false,
  actions,
  className,
  ...buttonProps
}: FullscreenImageButtonProps) {

  const { openImageViewer, updateImageViewer } = useFullscreenImageViewer();
  // 같은 화면의 여러 이미지 버튼 중 현재 뷰어를 연 버튼을 구분한다
  const triggerId = useId();
  // 알림 경로로 진입한 이미지가 다시 렌더링되어도 자동 열기를 한 번만 수행한다
  const hasInitiallyOpenedRef = useRef(false);

  useEffect(
    /**
     * 알림 경로가 지정한 현재 사진을 최초 렌더링에서 전체 화면으로 연다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    () => {
      // 일반 프로필 진입이거나 이미 자동으로 연 버튼이면 사용자 클릭을 기다린다
      if (!initiallyOpen || hasInitiallyOpenedRef.current) {
        // 자동 열기 상태를 변경하지 않고 종료한다
        return;
      }

      // 후속 반응 상태 갱신이 전체 화면을 반복해서 열지 않도록 처리 완료를 기록한다
      hasInitiallyOpenedRef.current = true;
      // 알림이 가리킨 현재 사진과 반응 버튼을 공통 전체 화면 뷰어에 전달한다
      openImageViewer({ source, fallbackSource, alt, actions }, triggerId);
    },
    [actions, alt, fallbackSource, initiallyOpen, openImageViewer, source, triggerId],
  );

  useEffect(
    /**
     * 현재 이미지 버튼이 연 뷰어에 최신 사진 반응 상태를 전달한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    () => {
      // 열린 전체 화면 이미지에 최신 좋아요와 댓글 집계를 반영한다
      updateImageViewer({ source, fallbackSource, alt, actions }, triggerId);
    },
    [actions, alt, fallbackSource, source, triggerId, updateImageViewer],
  );

  /**
   * 현재 버튼에 표시된 이미지를 전체 화면 원본 보기로 연다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function handleImageButtonClick(): void {

    // 이미지 경로와 설명 및 실패 대체 경로를 공통 뷰어에 전달한다.
    openImageViewer({ source, fallbackSource, alt, actions }, triggerId);
  }

  return (
    <button
      {...buttonProps}
      className={clsx(styles.trigger, className)}
      type="button"
      aria-label={ariaLabel ?? /* "원본 이미지 보기" */ message("frontend.imageViewer.open")}
      onClick={handleImageButtonClick}
    >
      {children}
    </button>
  );
}
