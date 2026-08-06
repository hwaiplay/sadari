import { sweetBlockingOperation } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";

type BlockingOperationOptions = {
  title?: string;
  text?: string;
};

type BlockingOperationTask<T> = () => Promise<T>;

const NAVIGATION_GUARD_STATE_KEY = "sadariBlockingOperation";
const NAVIGATION_RELEASE_TIMEOUT_MILLISECONDS = 1_000;

const activeOperationIds = new Set<number>();
let nextOperationId = 0;
let modalAbortController: AbortController | null = null;
let isNavigationGuardActive = false;
let isNavigationGuardReleasing = false;
let navigationReleaseTimer: number | null = null;
let navigationReleaseResolver: (() => void) | null = null;

/**
 * 현재 History State를 보존하면서 이동 차단 표식을 추가할 객체로 변환한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 History State의 복사본
 */
function getHistoryState(): Record<string, unknown> {
  // React Router의 현재 History State가 객체이면 기존 위치 정보를 유지한다
  if (typeof window.history.state === "object" && window.history.state !== null) {
    // 기존 History State를 복사하여 이동 차단 표식의 기반값으로 반환한다
    return { ...window.history.state } as Record<string, unknown>;
  }

  // 저장된 History State가 없으면 이동 차단 표식만 담을 빈 객체를 반환한다
  return {};
}

/**
 * 처리 중 새로고침과 창 닫기를 브라우저가 지원하는 기본 확인 절차로 차단한다
 *
 * @author SeungHyeon.Kang
 * @param event 브라우저 화면 이탈 이벤트
 * @return 반환값이 없다
 */
function handleBeforeUnload(event: BeforeUnloadEvent): void {
  // 서버 처리 결과가 확정되기 전에 문서가 종료되지 않도록 기본 이탈 동작을 취소한다
  event.preventDefault();
  // 브라우저별 기본 이탈 확인 절차가 실행되도록 반환 값을 설정한다
  event.returnValue = "";
}

/**
 * 이동 차단 History 항목 제거가 끝난 뒤 이벤트와 대기 상태를 정리한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function completeNavigationGuardRelease(): void {
  // 해제 대기 제한 시간이 남아 있으면 이후 중복 정리를 막기 위해 제거한다
  if (navigationReleaseTimer !== null) {
    // 현재 이동 가드 해제용 제한 시간만 제거한다
    window.clearTimeout(navigationReleaseTimer);
    // 다음 이동 가드가 새 제한 시간을 등록할 수 있도록 참조를 비운다
    navigationReleaseTimer = null;
  }

  // 처리 중에만 필요한 브라우저 이탈 이벤트를 제거한다
  window.removeEventListener("beforeunload", handleBeforeUnload);
  // 현재 이동 가드의 뒤로가기 감시 이벤트를 제거한다
  window.removeEventListener("popstate", handlePopState);
  isNavigationGuardActive = false;
  isNavigationGuardReleasing = false;

  const resolveRelease = navigationReleaseResolver;
  navigationReleaseResolver = null;

  // 이동 가드 해제를 기다리는 요청이 있으면 History 정리가 끝났음을 전달한다
  if (resolveRelease) {
    // 상태 변경 API 응답이 호출 화면으로 돌아갈 수 있도록 해제 Promise를 완료한다
    resolveRelease();
  }

  // 기존 가드를 해제하는 사이 새 작업이 시작됐으면 현재 위치에 새 가드를 즉시 설치한다
  if (activeOperationIds.size > 0) {
    // 새 상태 변경 작업이 끝날 때까지 현재 화면 이동을 다시 차단한다
    activateNavigationGuard();
  }
}

/**
 * PWA 스와이프와 브라우저 뒤로가기가 처리 중 화면을 벗어나지 못하게 현재 위치를 복원한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function handlePopState(): void {
  // 정상적인 가드 해제 이동이면 현재 위치를 다시 쌓지 않고 정리를 완료한다
  if (isNavigationGuardReleasing) {
    // 같은 URL의 원래 History 항목으로 돌아온 뒤 이동 차단 이벤트를 해제한다
    completeNavigationGuardRelease();
    // 가드 제거용 뒤로가기가 사용자 이동으로 처리되지 않도록 종료한다
    return;
  }

  // 상태 변경 작업이 남아 있으면 뒤로가기가 도착한 현재 URL에 차단 항목을 다시 추가한다
  if (activeOperationIds.size > 0) {
    // 연속 스와이프에도 현재 화면을 유지하도록 동일 URL의 가드 항목을 복원한다
    pushNavigationGuardEntry();
  }
}

/**
 * 현재 URL과 React Router State를 유지한 이동 차단용 History 항목을 추가한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function pushNavigationGuardEntry(): void {
  const guardState = {
    ...getHistoryState(),
    [NAVIGATION_GUARD_STATE_KEY]: true,
  };

  // 뒤로가기가 먼저 같은 URL의 원래 항목에 도착하도록 차단 항목을 현재 위치에 쌓는다
  window.history.pushState(guardState, "", window.location.href);
}

/**
 * 상태 변경 작업 동안 현재 화면을 유지하도록 브라우저 이동 가드를 활성화한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function activateNavigationGuard(): void {
  // 이미 활성화됐거나 기존 가드를 해제 중이면 중복 History 항목을 만들지 않는다
  if (isNavigationGuardActive || isNavigationGuardReleasing) {
    // 현재 이동 가드 상태를 유지하고 활성화 처리를 종료한다
    return;
  }

  isNavigationGuardActive = true;
  // PWA 스와이프와 브라우저 뒤로가기 결과를 현재 화면으로 복원하도록 감시한다
  window.addEventListener("popstate", handlePopState);
  // 새로고침과 창 닫기는 브라우저가 제공하는 기본 이탈 확인 절차로 보호한다
  window.addEventListener("beforeunload", handleBeforeUnload);
  // 사용자의 뒤로가기가 실제 이전 화면 대신 동일 URL에 도착하도록 가드 항목을 추가한다
  pushNavigationGuardEntry();
}

/**
 * 처리 완료 후 동일 URL의 이동 차단 History 항목을 제거한다
 *
 * @author SeungHyeon.Kang
 * @return 이동 차단 History 항목 제거 완료 Promise
 */
function releaseNavigationGuard(): Promise<void> {
  // 활성화된 이동 가드가 없으면 추가 History 조작 없이 완료한다
  if (!isNavigationGuardActive) {
    // 제거할 이동 차단 항목이 없는 완료 Promise를 반환한다
    return Promise.resolve();
  }

  // 이미 해제 중이면 최초 해제 작업이 완료될 때까지 같은 Promise를 공유한다
  if (isNavigationGuardReleasing) {
    // 진행 중인 이동 가드 해제 완료를 기다리는 Promise를 반환한다
    return new Promise((resolve) => {
      const previousResolver = navigationReleaseResolver;

      /**
       * 기존 해제 대기자와 추가 대기자를 함께 완료한다
       *
       * @author SeungHyeon.Kang
       * @return 반환값이 없다
       */
      navigationReleaseResolver = () => {
        // 먼저 등록된 상태 변경 요청의 해제 대기를 완료한다
        previousResolver?.();
        // 추가로 등록된 상태 변경 요청의 해제 대기를 완료한다
        resolve();
      };
    });
  }

  isNavigationGuardReleasing = true;

  // 동일 URL로 쌓은 가드 항목을 제거한 뒤 API 호출 화면에 응답을 전달한다
  return new Promise((resolve) => {
    navigationReleaseResolver = resolve;

    /**
     * 일부 브라우저에서 popstate가 오지 않아도 처리 중 화면이 영구 잠기지 않게 가드를 정리한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const handleReleaseTimeout = (): void => {
      // 브라우저가 History 이동 이벤트를 전달하지 않은 경우 남은 이벤트와 대기 상태를 해제한다
      completeNavigationGuardRelease();
    };

    // History 이동 이벤트가 누락되는 예외 상황의 최대 대기 시간을 설정한다
    navigationReleaseTimer = window.setTimeout(handleReleaseTimeout, NAVIGATION_RELEASE_TIMEOUT_MILLISECONDS);
    // 동일 URL의 직전 원본 History 항목으로 돌아가 차단용 항목을 제거한다
    window.history.back();
  });
}

/**
 * 상태 변경 작업의 처리 중 모달과 화면 이동 차단을 시작한다
 *
 * @author SeungHyeon.Kang
 * @param options 작업별 처리 중 문구
 * @return 완료 시 해제할 상태 변경 작업 식별값
 */
export function beginBlockingOperation(options: BlockingOperationOptions = {}): number {
  nextOperationId += 1;
  const operationId = nextOperationId;
  const isFirstOperation = activeOperationIds.size === 0;
  // 동시에 실행되는 상태 변경 요청을 개별적으로 완료 처리할 수 있도록 식별값을 등록한다
  activeOperationIds.add(operationId);

  // 최초 작업에서만 모달과 History 가드를 생성하여 중첩 요청의 화면 중복을 막는다
  if (isFirstOperation) {
    // 작업 완료 신호로만 닫히는 처리 중 모달의 제어 객체를 생성한다
    modalAbortController = new AbortController();
    // 상태 변경 응답이 확정될 때까지 현재 화면의 이동을 차단한다
    activateNavigationGuard();
    // "처리 중입니다."
    void sweetBlockingOperation({
      title: options.title ?? message("frontend.common.processing"),
      // "처리가 완료될 때까지 잠시만 기다려주세요."
      text: options.text ?? message("frontend.common.processingWait"),
      closeSignal: modalAbortController.signal,
    });
  }

  // 호출부가 작업 완료 시 정확한 요청을 해제할 수 있도록 식별값을 반환한다
  return operationId;
}

/**
 * 상태 변경 작업을 완료하고 마지막 요청이면 처리 중 모달과 이동 가드를 해제한다
 *
 * @author SeungHyeon.Kang
 * @param operationId 완료한 상태 변경 작업 식별값
 * @return 처리 중 화면과 이동 가드 정리 완료 Promise
 */
export async function endBlockingOperation(operationId: number): Promise<void> {
  // 이미 완료된 요청은 다른 상태 변경 작업의 진행 상태에 영향을 주지 않는다
  if (!activeOperationIds.delete(operationId)) {
    // 중복 해제 요청을 별도 화면 변경 없이 종료한다
    return;
  }

  // 다른 상태 변경 작업이 남아 있으면 공통 모달과 이동 가드를 유지한다
  if (activeOperationIds.size > 0) {
    // 마지막 요청이 완료될 때까지 현재 처리 중 화면을 유지한다
    return;
  }

  // 호출 화면이 후속 성공 또는 실패 알림을 열기 전에 차단용 History 항목을 제거한다
  await releaseNavigationGuard();

  // 가드 해제 중 새 작업이 시작되지 않았을 때만 현재 처리 중 모달을 닫는다
  if (activeOperationIds.size === 0) {
    // 작업 완료 신호로 버튼 없는 처리 중 모달을 자동으로 닫는다
    modalAbortController?.abort();
    // 다음 최초 작업이 새 모달 제어 객체를 만들도록 참조를 비운다
    modalAbortController = null;
  }
}

/**
 * API 호출 전 준비 과정까지 포함한 상태 변경 작업을 공통 이동 차단 범위에서 실행한다
 *
 * @author SeungHyeon.Kang
 * @param task 처리 중 화면을 유지할 비동기 상태 변경 작업
 * @param options 작업별 처리 중 문구
 * @return 상태 변경 작업의 완료 결과
 * @throws 전달받은 상태 변경 작업이 실패할 때 발생
 */
export async function runBlockingOperation<T>(
  task: BlockingOperationTask<T>,
  options: BlockingOperationOptions = {},
): Promise<T> {
  // API 요청 전에 필요한 파일 처리와 권한 요청부터 화면 이동을 차단한다
  const operationId = beginBlockingOperation(options);

  // 성공과 실패 모두 공통 이동 가드가 해제되도록 상태 변경 작업을 격리한다
  try {
    // 호출 화면이 처리 결과를 이어서 사용할 수 있도록 비동기 작업 결과를 반환한다
    return await task();
  }

  // 상태 변경 결과와 관계없이 처리 중 화면과 이동 가드를 해제한다
  finally {
    // 후속 성공 또는 실패 알림이 열리기 전에 버튼 없는 처리 중 모달을 닫는다
    await endBlockingOperation(operationId);
  }
}
