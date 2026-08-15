import { sweetBlockingOperation } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";

type BlockingOperationOptions = {
  title?: string;
  text?: string;
};

type BlockingOperationTask<T> = () => Promise<T>;

type NavigationGuardEntry = "base" | "sentinel";

type NavigationGuardMarker = {
  id: string;
  entry: NavigationGuardEntry;
  isHistoryIndexAdjusted: boolean;
};

const NAVIGATION_GUARD_STATE_KEY = "sadariBlockingOperation";

const activeOperationIds = new Set<number>();
let nextOperationId = 0;
let nextNavigationGuardId = 0;
let modalAbortController: AbortController | null = null;
let activeNavigationGuardId: string | null = null;
let isNavigationGuardActive = false;
let isPopStateListenerRegistered = false;
let lastObservedNavigationGuardMarker: NavigationGuardMarker | null = null;

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
 * History State에서 Sadari 이동 차단 표식을 타입 안전하게 조회한다
 *
 * @author SeungHyeon.Kang
 * @param state 확인할 History State
 * @return 유효한 이동 차단 표식 또는 표식이 없을 때 null
 */
function getNavigationGuardMarker(state: unknown): NavigationGuardMarker | null {
  // History State가 객체가 아니면 이동 차단 표식을 읽지 않는다
  if (typeof state !== "object" || state === null) {
    // 유효한 이동 차단 표식이 없음을 반환한다
    return null;
  }

  const marker = (state as Record<string, unknown>)[NAVIGATION_GUARD_STATE_KEY];

  // 이동 차단 표식이 객체가 아니면 이전 버전의 값이나 잘못된 값을 사용하지 않는다
  if (typeof marker !== "object" || marker === null) {
    // 유효한 이동 차단 표식이 없음을 반환한다
    return null;
  }

  const markerRecord = marker as Record<string, unknown>;
  const markerId = markerRecord.id;
  const markerEntry = markerRecord.entry;
  const isHistoryIndexAdjusted = markerRecord.isHistoryIndexAdjusted === true;

  // 식별자와 History 항목 구분값이 모두 유효할 때만 이동 차단 표식으로 인정한다
  if (typeof markerId !== "string"
      || (markerEntry !== "base" && markerEntry !== "sentinel")) {
    // 유효하지 않은 이동 차단 표식을 무시하도록 null을 반환한다
    return null;
  }

  // 검증된 이동 차단 식별자와 History 항목 구분값을 반환한다
  return {
    id: markerId,
    entry: markerEntry,
    isHistoryIndexAdjusted,
  };
}

/**
 * 새로운 이동 차단 History 쌍을 구분할 식별자를 생성한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 브라우저 세션에서 사용할 이동 차단 식별자
 */
function createNavigationGuardId(): string {
  nextNavigationGuardId += 1;

  // 페이지 재시작 이후에도 기존 History 표식과 충돌하지 않도록 시간과 순번을 조합한다
  return `${Date.now()}-${nextNavigationGuardId}`;
}

/**
 * 현재 History 항목을 이동 차단 기준 항목으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param guardId 이동 차단 History 쌍의 식별자
 * @return 반환값이 없다
 */
function replaceNavGuardBase(guardId: string): void {
  const guardState = {
    ...getHistoryState(),
    [NAVIGATION_GUARD_STATE_KEY]: {
      id: guardId,
      entry: "base" satisfies NavigationGuardEntry,
      isHistoryIndexAdjusted: false,
    },
  };

  // 저장 중 뒤로가기가 현재 화면과 같은 URL의 기준 항목에서 멈추도록 현재 항목을 표시한다
  window.history.replaceState(guardState, "", window.location.href);
  // 이후 popstate 방향을 판별하도록 현재 기준 항목을 마지막 관찰값으로 기록한다
  lastObservedNavigationGuardMarker = getNavigationGuardMarker(guardState);
}

/**
 * 현재 URL에 이동 차단용 최상단 History 항목을 추가한다
 *
 * @author SeungHyeon.Kang
 * @param guardId 이동 차단 History 쌍의 식별자
 * @return 반환값이 없다
 */
function pushNavGuardSentinel(guardId: string): void {
  const guardState = {
    ...getHistoryState(),
    [NAVIGATION_GUARD_STATE_KEY]: {
      id: guardId,
      entry: "sentinel" satisfies NavigationGuardEntry,
      isHistoryIndexAdjusted: false,
    },
  };

  // 저장 중 뒤로가기가 실제 이전 화면에 도달하기 전에 같은 URL의 차단 항목을 만나도록 추가한다
  window.history.pushState(guardState, "", window.location.href);
  // pushState는 popstate를 발생시키지 않으므로 현재 차단 항목을 직접 관찰값으로 기록한다
  lastObservedNavigationGuardMarker = getNavigationGuardMarker(guardState);
}

/**
 * 완료된 이동 차단 항목을 React Router의 정상 이력 인덱스로 승격한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function adjustReleasedGuardIndex(): void {

  // 현재 History 깊이와 React Router 인덱스를 비교할 상태를 조회한다
  const historyState = getHistoryState();
  // 현재 항목이 이동 차단 과정에서 만든 내부 항목인지 확인한다
  const currentMarker = getNavigationGuardMarker(historyState);
  const currentHistoryIndex = historyState.idx;

  // 현재 항목이 미보정 차단 항목이 아니면 React Router 인덱스를 변경하지 않는다
  if (currentMarker?.entry !== "sentinel"
          || currentMarker.isHistoryIndexAdjusted
          || typeof currentHistoryIndex !== "number") {
    // 일반 화면과 이미 보정된 PWA 세션의 History 상태를 유지한다
    return;
  }

  const adjustedState = {
    ...historyState,
    idx: currentHistoryIndex + 1,
    [NAVIGATION_GUARD_STATE_KEY]: {
      id: currentMarker.id,
      entry: currentMarker.entry,
      isHistoryIndexAdjusted: true,
    },
  };

  // 동일 URL 차단 항목을 실제 History 깊이와 일치하는 Router 인덱스로 교체한다
  window.history.replaceState(adjustedState, "", window.location.href);
  // 이후 뒤로가기에서 차단 기준 항목을 한 번에 건너뛸 수 있도록 관찰값을 갱신한다
  lastObservedNavigationGuardMarker = getNavigationGuardMarker(adjustedState);
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
  // 브라우저별 기본 이탈 확인 절차가 실행되도록 반환값을 설정한다
  event.returnValue = "";
}

/**
 * History 이동 방향과 저장 진행 상태에 따라 실제 화면 이탈이나 내부 중복 항목을 처리한다
 *
 * @author SeungHyeon.Kang
 * @param event 이동한 대상 History State를 포함한 이벤트
 * @return 반환값이 없다
 */
function handlePopState(event: PopStateEvent): void {
  const previousMarker = lastObservedNavigationGuardMarker;
  const currentMarker = getNavigationGuardMarker(event.state);

  // 이후 History 이동 방향을 비교할 수 있도록 이동 대상의 표식을 관찰값으로 기록한다
  lastObservedNavigationGuardMarker = currentMarker;

  // 상태 변경 작업 중 차단 쌍의 기준 항목으로 돌아오면 같은 URL의 차단 항목을 즉시 복원한다
  if (isNavigationGuardActive && activeNavigationGuardId !== null
      && currentMarker?.id === activeNavigationGuardId && currentMarker.entry === "base") {
    // PWA 스와이프와 하드웨어 뒤로가기가 실제 이전 화면으로 이어지지 않도록 차단 항목을 복원한다
    pushNavGuardSentinel(activeNavigationGuardId);
    // 저장 중 뒤로가기 처리를 현재 화면에서 종료한다
    return;
  }

  // 상태 변경 작업 중 예상하지 못한 History 위치로 이동하면 가능한 경우 기존 차단 항목으로 복귀한다
  if (isNavigationGuardActive) {
    // 사용자가 저장 중인 화면에서 벗어나지 않도록 브라우저의 다음 History 항목으로 복귀한다
    window.history.forward();
    // 저장 중 예외 History 이동 처리를 종료한다
    return;
  }

  // 저장 완료 후 사용자가 뒤로갈 때 차단 항목에서 기준 항목으로 이동한 경우 내부 중복 항목만 건너뛴다
  if (previousMarker?.entry === "sentinel" && currentMarker?.entry === "base"
      && previousMarker.id === currentMarker.id) {
    // 저장 완료 시에는 이동하지 않고 이후 사용자의 뒤로가기 시점에만 실제 이전 화면으로 이동한다
    window.history.back();
  }
}

/**
 * 저장 완료 뒤에도 남은 내부 History 쌍을 처리할 애플리케이션 수명 이벤트를 한 번 등록한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function ensurePopStateListener(): void {
  // 이미 등록된 애플리케이션 수명 이벤트는 중복 등록하지 않는다
  if (isPopStateListenerRegistered) {
    // 기존 이벤트가 이후 History 이동도 처리하도록 등록 절차를 종료한다
    return;
  }

  // 저장 완료 후 사용자가 실제 뒤로갈 때 내부 기준 항목을 건너뛸 수 있도록 계속 감시한다
  window.addEventListener("popstate", handlePopState);
  isPopStateListenerRegistered = true;
}

/**
 * 상태 변경 작업 동안 현재 화면을 유지할 History 가드를 활성화한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function activateNavigationGuard(): void {
  // 이미 활성화된 가드는 중첩 요청이 완료될 때까지 그대로 유지한다
  if (isNavigationGuardActive) {
    // 중복 History 항목을 만들지 않고 활성화 처리를 종료한다
    return;
  }

  // 저장 완료 후 남은 History 항목도 처리할 공통 감시 이벤트를 준비한다
  ensurePopStateListener();

  const currentMarker = getNavigationGuardMarker(window.history.state);

  // 현재 항목이 이전 작업에서 만든 차단 항목이면 새 중복 항목 없이 재사용한다
  if (currentMarker?.entry === "sentinel") {
    activeNavigationGuardId = currentMarker.id;
    // 현재 차단 항목부터 이동 방향을 추적하도록 관찰값을 갱신한다
    lastObservedNavigationGuardMarker = currentMarker;
  }

  // 현재 항목을 재사용할 수 없으면 기준 항목과 차단 항목을 새로 구성한다
  else {
    const guardId = createNavigationGuardId();
    activeNavigationGuardId = guardId;
    // 현재 React Router 위치를 이동 차단 History 쌍의 기준 항목으로 표시한다
    replaceNavGuardBase(guardId);
    // 사용자의 뒤로가기를 받을 같은 URL의 차단 항목을 추가한다
    pushNavGuardSentinel(guardId);
  }

  isNavigationGuardActive = true;
  // 새로고침과 창 닫기는 브라우저가 제공하는 기본 이탈 확인 절차로 보호한다
  window.addEventListener("beforeunload", handleBeforeUnload);
}

/**
 * 상태 변경 완료 후 History 위치를 이동하지 않고 현재 화면의 이동 차단 상태만 해제한다
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없다
 */
function deactivateNavigationGuard(): void {
  // 활성화된 이동 가드가 없으면 브라우저 상태를 변경하지 않는다
  if (!isNavigationGuardActive) {
    // 저장 완료 후 중복 해제 요청을 종료한다
    return;
  }

  // 완료된 동일 URL 항목을 정상 이력으로 계산해 홈 이동 거리가 실제 History 깊이와 일치하게 한다
  adjustReleasedGuardIndex();
  // 저장 완료 직후 popstate가 발생하지 않도록 History 이동 없이 논리 가드만 비활성화한다
  isNavigationGuardActive = false;
  activeNavigationGuardId = null;
  // 저장 결과가 확정되었으므로 새로고침과 창 닫기 보호 이벤트를 해제한다
  window.removeEventListener("beforeunload", handleBeforeUnload);
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

  // 호출부가 작업 완료 후 정확한 요청만 해제하도록 식별값을 반환한다
  return operationId;
}

/**
 * 상태 변경 작업을 완료하고 마지막 요청이면 처리 중 모달과 이동 차단을 해제한다
 *
 * @author SeungHyeon.Kang
 * @param operationId 완료된 상태 변경 작업 식별값
 * @return 처리 중 화면과 이동 가드 정리 완료 Promise
 */
export function endBlockingOperation(operationId: number): Promise<void> {
  // 이미 완료된 요청은 다른 상태 변경 작업의 진행 상태에 영향을 주지 않는다
  if (!activeOperationIds.delete(operationId)) {
    // 중복 해제 요청을 완료된 Promise로 종료한다
    return Promise.resolve();
  }

  // 다른 상태 변경 작업이 남아 있으면 공통 모달과 이동 가드를 유지한다
  if (activeOperationIds.size > 0) {
    // 마지막 요청이 완료될 때까지 현재 처리 중 화면을 유지한다
    return Promise.resolve();
  }

  // 성공 또는 실패 알림이 열리기 전에 History 이동 없이 논리 가드만 해제한다
  deactivateNavigationGuard();
  // 작업 완료 신호로 버튼 없는 처리 중 모달을 닫는다
  modalAbortController?.abort();
  // 다음 최초 작업에서 새 모달 제어 객체를 만들도록 참조를 비운다
  modalAbortController = null;

  // 호출부가 정리 완료 뒤 후속 알림과 라우팅을 실행하도록 완료된 Promise를 반환한다
  return Promise.resolve();
}

/**
 * API 호출 전 준비 과정까지 포함한 상태 변경 작업을 공통 이동 차단 범위에서 실행한다
 *
 * @author SeungHyeon.Kang
 * @param task 처리 중 화면을 유지할 비동기 상태 변경 작업
 * @param options 작업별 처리 중 문구
 * @return 상태 변경 작업의 완료 결과
 * @throws 전달받은 상태 변경 작업에 실패하면 발생한다
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

// 이전 PWA 실행에서 미보정 상태로 남은 동일 URL 항목도 현재 세션의 정상 이력으로 승격한다
adjustReleasedGuardIndex();
// 페이지를 다시 연 뒤에도 기존 이동 차단 History 쌍을 처리하도록 애플리케이션 수명 이벤트를 준비한다
ensurePopStateListener();
