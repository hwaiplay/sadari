import {
  completeSweetBlockingOperation,
  sweetBlockingOperation,
  type SweetBlockingCompletionOptions,
} from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";

type BlockingOperationOptions = {
  title?: string;
  text?: string;
  success?: BlockingOperationCompletion;
};

export type BlockingOperationCompletion = SweetBlockingCompletionOptions;

type BlockingOperationTask<T> = () => Promise<T>;

type NavigationGuardEntry = "base" | "sentinel";

type NavigationGuardMarker = {
  id: string;
  entry: NavigationGuardEntry;
};

type NavigationGuardCleanup = {
  id: string;
  resolve: () => void;
};

const NAVIGATION_GUARD_STATE_KEY = "sadariBlockingOperation";

const activeOperationIds = new Set<number>();
let nextOperationId = 0;
let nextNavigationGuardId = 0;
let modalAbortController: AbortController | null = null;
let modalResultPromise: Promise<unknown> | null = null;
let activeNavigationGuardId: string | null = null;
let isNavigationGuardActive = false;
let isPopStateListenerRegistered = false;
let lastObservedNavigationGuardMarker: NavigationGuardMarker | null = null;
let pendingNavigationGuardCleanup: NavigationGuardCleanup | null = null;
let isHomeHistoryResetPending = false;

/**
 * 현재 History State를 보존하면서 이동 차단 표식을 추가할 객체로 변환함
 *
 * @author SeungHyeon.Kang
 * @return 현재 History State의 복사본
 */
function getHistoryState(): Record<string, unknown> {
  // React Router의 현재 History State가 객체이면 기존 위치 정보를 유지함
  if (typeof window.history.state === "object" && window.history.state !== null) {
    // 기존 History State를 복사하여 이동 차단 표식의 기반값으로 반환함
    return { ...window.history.state } as Record<string, unknown>;
  }

  // 저장된 History State가 없으면 이동 차단 표식만 담을 빈 객체를 반환함
  return {};
}

/**
 * History State에서 Sadari 이동 차단 표식을 타입 안전하게 조회함
 *
 * @author SeungHyeon.Kang
 * @param state 확인할 History State
 * @return 유효한 이동 차단 표식 또는 표식이 없을 때 null
 */
function getNavigationGuardMarker(state: unknown): NavigationGuardMarker | null {
  // History State가 객체가 아니면 이동 차단 표식을 읽지 않음
  if (typeof state !== "object" || state === null) {
    // 유효한 이동 차단 표식이 없음을 반환함
    return null;
  }

  const marker = (state as Record<string, unknown>)[NAVIGATION_GUARD_STATE_KEY];

  // 이동 차단 표식이 객체가 아니면 이전 버전의 값이나 잘못된 값을 사용하지 않음
  if (typeof marker !== "object" || marker === null) {
    // 유효한 이동 차단 표식이 없음을 반환함
    return null;
  }

  const markerRecord = marker as Record<string, unknown>;
  const markerId = markerRecord.id;
  const markerEntry = markerRecord.entry;

  // 식별자와 History 항목 구분값이 모두 유효할 때만 이동 차단 표식으로 인정함
  if (typeof markerId !== "string"
      || (markerEntry !== "base" && markerEntry !== "sentinel")) {
    // 유효하지 않은 이동 차단 표식을 무시하도록 null을 반환함
    return null;
  }

  // 검증된 이동 차단 식별자와 History 항목 구분값을 반환함
  return {
    id: markerId,
    entry: markerEntry,
  };
}

/**
 * 새로운 이동 차단 History 쌍을 구분할 식별자를 생성함
 *
 * @author SeungHyeon.Kang
 * @return 현재 브라우저 세션에서 사용할 이동 차단 식별자
 */
function createNavigationGuardId(): string {
  nextNavigationGuardId += 1;

  // 페이지 재시작 이후에도 기존 History 표식과 충돌하지 않도록 시간과 순번을 조합함
  return `${Date.now()}-${nextNavigationGuardId}`;
}

/**
 * 현재 History 항목을 이동 차단 기준 항목으로 표시함
 *
 * @author SeungHyeon.Kang
 * @param guardId 이동 차단 History 쌍의 식별자
 * @return 반환값이 없음
 */
function replaceNavGuardBase(guardId: string): void {
  const guardState = {
    ...getHistoryState(),
    [NAVIGATION_GUARD_STATE_KEY]: {
      id: guardId,
      entry: "base" satisfies NavigationGuardEntry,
    },
  };

  // 저장 중 뒤로가기가 현재 화면과 같은 URL의 기준 항목에서 멈추도록 현재 항목을 표시함
  window.history.replaceState(guardState, "", window.location.href);
  // 이후 popstate 방향을 판별하도록 현재 기준 항목을 마지막 관찰값으로 기록함
  lastObservedNavigationGuardMarker = getNavigationGuardMarker(guardState);
}

/**
 * 현재 URL에 이동 차단용 최상단 History 항목을 추가함
 *
 * @author SeungHyeon.Kang
 * @param guardId 이동 차단 History 쌍의 식별자
 * @return 반환값이 없음
 */
function pushNavGuardSentinel(guardId: string): void {
  const guardState = {
    ...getHistoryState(),
    [NAVIGATION_GUARD_STATE_KEY]: {
      id: guardId,
      entry: "sentinel" satisfies NavigationGuardEntry,
    },
  };

  // 저장 중 뒤로가기가 실제 이전 화면에 도달하기 전에 같은 URL의 차단 항목을 만나도록 추가함
  window.history.pushState(guardState, "", window.location.href);
  // pushState는 popstate를 발생시키지 않으므로 현재 차단 항목을 직접 관찰값으로 기록함
  lastObservedNavigationGuardMarker = getNavigationGuardMarker(guardState);
}

/**
 * 현재 History State에서 이동 차단 표식만 제거함
 *
 * @author SeungHyeon.Kang
 * @param state 정리할 History State
 * @return React Router와 다른 가드 상태를 유지한 History State
 */
const removeNavGuardMarker = (state: Record<string, unknown>): Record<string, unknown> => {

  const cleanedState = { ...state };
  // 저장 가드만 제거해 홈 종료 가드와 React Router 인덱스는 원래 값으로 유지함
  delete cleanedState[NAVIGATION_GUARD_STATE_KEY];
  // 임시 저장 가드가 제거된 History State를 반환함
  return cleanedState;
};

/**
 * 홈 루트 정리가 저장 가드의 기준 항목에 도착해도 앱 외부로 추가 이동하지 않게 표시함
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
export const beginHomeHistoryReset = (): void => {

  // 이전 버전이 남긴 최상단 가드에서 홈 루트로 이동할 때만 자동 건너뛰기를 일회 중단함
  isHomeHistoryResetPending = !isNavigationGuardActive
    && lastObservedNavigationGuardMarker?.entry === "sentinel";
};

/**
 * 처리 중 새로고침과 창 닫기를 브라우저가 지원하는 기본 확인 절차로 차단함
 *
 * @author SeungHyeon.Kang
 * @param event 브라우저 화면 이탈 이벤트
 * @return 반환값이 없음
 */
function handleBeforeUnload(event: BeforeUnloadEvent): void {
  // 서버 처리 결과가 확정되기 전에 문서가 종료되지 않도록 기본 이탈 동작을 취소함
  event.preventDefault();
  // 브라우저별 기본 이탈 확인 절차가 실행되도록 반환값을 설정함
  event.returnValue = "";
}

/**
 * History 이동 방향과 저장 진행 상태에 따라 실제 화면 이탈이나 내부 중복 항목을 처리함
 *
 * @author SeungHyeon.Kang
 * @param event 이동한 대상 History State를 포함한 이벤트
 * @return 반환값이 없음
 */
function handlePopState(event: PopStateEvent): void {
  const previousMarker = lastObservedNavigationGuardMarker;
  const currentMarker = getNavigationGuardMarker(event.state);

  // 이후 History 이동 방향을 비교할 수 있도록 이동 대상의 표식을 관찰값으로 기록함
  lastObservedNavigationGuardMarker = currentMarker;

  // 저장 완료 정리로 기준 항목에 도착하면 현재 항목의 임시 표식만 제거하고 작업을 완료함
  if (pendingNavigationGuardCleanup !== null) {
    const pendingCleanup = pendingNavigationGuardCleanup;
    pendingNavigationGuardCleanup = null;

    // 예상한 기준 항목이면 Router 인덱스와 PWA 종료 가드를 유지한 채 저장 표식만 제거함
    if (currentMarker?.id === pendingCleanup.id && currentMarker.entry === "base") {
      const cleanedState = removeNavGuardMarker(getHistoryState());
      window.history.replaceState(cleanedState, "", window.location.href);
    }

    // 정리 POP이 일반 사용자 뒤로가기로 이어지지 않도록 관찰 상태를 초기화함
    lastObservedNavigationGuardMarker = null;
    pendingCleanup.resolve();
    // 같은 POP을 완료된 가드의 추가 뒤로가기로 처리하지 않고 종료함
    return;
  }

  // 홈 루트 정리가 이전 버전의 가드 기준 항목에 도착하면 외부 이력으로 추가 이동하지 않음
  if (isHomeHistoryResetPending && !isNavigationGuardActive) {
    isHomeHistoryResetPending = false;
    // 홈 Provider가 도착한 앱 루트 항목을 홈으로 교체하도록 현재 POP 처리를 종료함
    return;
  }

  // 저장이 다시 시작된 경우에는 홈 정리 표시를 폐기하고 활성 저장 가드를 우선함
  isHomeHistoryResetPending = false;

  // 상태 변경 작업 중 차단 쌍의 기준 항목으로 돌아오면 같은 URL의 차단 항목을 즉시 복원함
  if (isNavigationGuardActive && activeNavigationGuardId !== null
      && currentMarker?.id === activeNavigationGuardId && currentMarker.entry === "base") {
    // PWA 스와이프와 하드웨어 뒤로가기가 실제 이전 화면으로 이어지지 않도록 차단 항목을 복원함
    pushNavGuardSentinel(activeNavigationGuardId);
    // 저장 중 뒤로가기 처리를 현재 화면에서 종료함
    return;
  }

  // 상태 변경 작업 중 예상하지 못한 History 위치로 이동하면 가능한 경우 기존 차단 항목으로 복귀함
  if (isNavigationGuardActive) {
    // 사용자가 저장 중인 화면에서 벗어나지 않도록 브라우저의 다음 History 항목으로 복귀함
    window.history.forward();
    // 저장 중 예외 History 이동 처리를 종료함
    return;
  }

  // 저장 완료 후 사용자가 뒤로갈 때 차단 항목에서 기준 항목으로 이동한 경우 내부 중복 항목만 건너뜀
  if (previousMarker?.entry === "sentinel" && currentMarker?.entry === "base"
      && previousMarker.id === currentMarker.id) {
    // 저장 완료 시에는 이동하지 않고 이후 사용자의 뒤로가기 시점에만 실제 이전 화면으로 이동함
    window.history.back();
  }
}

/**
 * 저장 완료 뒤에도 남은 내부 History 쌍을 처리할 애플리케이션 수명 이벤트를 한 번 등록함
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
function ensurePopStateListener(): void {
  // 이미 등록된 애플리케이션 수명 이벤트는 중복 등록하지 않음
  if (isPopStateListenerRegistered) {
    // 기존 이벤트가 이후 History 이동도 처리하도록 등록 절차를 종료함
    return;
  }

  // 저장 완료 후 사용자가 실제 뒤로갈 때 내부 기준 항목을 건너뛸 수 있도록 계속 감시함
  window.addEventListener("popstate", handlePopState);
  isPopStateListenerRegistered = true;
}

/**
 * 상태 변경 작업 동안 현재 화면을 유지할 History 가드를 활성화함
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
function activateNavigationGuard(): void {
  // 이미 활성화된 가드는 중첩 요청이 완료될 때까지 그대로 유지함
  if (isNavigationGuardActive) {
    // 중복 History 항목을 만들지 않고 활성화 처리를 종료함
    return;
  }

  // 저장 완료 후 남은 History 항목도 처리할 공통 감시 이벤트를 준비함
  ensurePopStateListener();

  const currentMarker = getNavigationGuardMarker(window.history.state);

  // 현재 항목이 이전 작업에서 만든 차단 항목이면 새 중복 항목 없이 재사용함
  if (currentMarker?.entry === "sentinel") {
    activeNavigationGuardId = currentMarker.id;
    // 현재 차단 항목부터 이동 방향을 추적하도록 관찰값을 갱신함
    lastObservedNavigationGuardMarker = currentMarker;
  }

  // 현재 항목을 재사용할 수 없으면 기준 항목과 차단 항목을 새로 구성함
  else {
    const guardId = createNavigationGuardId();
    activeNavigationGuardId = guardId;
    // 현재 React Router 위치를 이동 차단 History 쌍의 기준 항목으로 표시함
    replaceNavGuardBase(guardId);
    // 사용자의 뒤로가기를 받을 같은 URL의 차단 항목을 추가함
    pushNavGuardSentinel(guardId);
  }

  isNavigationGuardActive = true;
  // 새로고침과 창 닫기는 브라우저가 제공하는 기본 이탈 확인 절차로 보호함
  window.addEventListener("beforeunload", handleBeforeUnload);
}

/**
 * 상태 변경 완료 후 동일 URL의 임시 History 항목을 제거하고 이동 차단 상태를 해제함
 *
 * @author SeungHyeon.Kang
 * @return 반환값이 없음
 */
async function deactivateNavigationGuard(): Promise<void> {
  // 활성화된 이동 가드가 없으면 브라우저 상태를 변경하지 않음
  if (!isNavigationGuardActive) {
    // 저장 완료 후 중복 해제 요청을 완료함
    return;
  }

  const completedGuardId = activeNavigationGuardId;
  const currentMarker = getNavigationGuardMarker(window.history.state);
  // 저장 결과가 확정되었으므로 정리 POP이 활성 차단으로 다시 복원되지 않게 먼저 비활성화함
  isNavigationGuardActive = false;
  activeNavigationGuardId = null;
  // 저장 결과가 확정되었으므로 새로고침과 창 닫기 보호 이벤트를 해제함
  window.removeEventListener("beforeunload", handleBeforeUnload);

  // 예상한 최상단 가드가 아니면 현재 History를 이동하지 않고 안전하게 해제를 마침
  if (completedGuardId === null || currentMarker?.id !== completedGuardId
      || currentMarker.entry !== "sentinel") {
    // 정리할 동일 URL 쌍이 없으므로 현재 화면을 유지함
    return;
  }

  // 앞으로가기에 남을 임시 항목이 이후 가드로 오인되지 않도록 현재 표식을 먼저 제거함
  const cleanedSentinelState = removeNavGuardMarker(getHistoryState());
  window.history.replaceState(cleanedSentinelState, "", window.location.href);
  // 같은 URL의 기준 항목으로 돌아오는 POP을 완료 신호로 기다림
  await new Promise<void>((resolve) => {
    pendingNavigationGuardCleanup = {
      id: completedGuardId,
      resolve,
    };
    // 저장 가드가 만든 한 항목만 제거해 React Router의 원래 인덱스로 복귀함
    window.history.back();
  });
}

/**
 * 상태 변경 작업의 처리 중 모달과 화면 이동 차단을 시작함
 *
 * @author SeungHyeon.Kang
 * @param options 작업별 처리 중 문구
 * @return 완료 시 해제할 상태 변경 작업 식별값
 */
export function beginBlockingOperation(options: BlockingOperationOptions = {}): number {
  nextOperationId += 1;
  const operationId = nextOperationId;
  const isFirstOperation = activeOperationIds.size === 0;
  // 동시에 실행되는 상태 변경 요청을 개별적으로 완료 처리할 수 있도록 식별값을 등록함
  activeOperationIds.add(operationId);

  // 최초 작업에서만 모달과 History 가드를 생성하여 중첩 요청의 화면 중복을 막음
  if (isFirstOperation) {
    // 작업 완료 신호로만 닫히는 처리 중 모달의 제어 객체를 생성함
    modalAbortController = new AbortController();
    // 상태 변경 응답이 확정될 때까지 현재 화면의 이동을 차단함
    activateNavigationGuard();
    // "처리 중입니다."
    modalResultPromise = sweetBlockingOperation({
      title: options.title ?? message("frontend.common.processing"),
      // "처리가 완료될 때까지 잠시만 기다려주세요."
      text: options.text ?? message("frontend.common.processingWait"),
      closeSignal: modalAbortController.signal,
      completion: options.success,
    });
  }

  // 호출부가 작업 완료 후 정확한 요청만 해제하도록 식별값을 반환함
  return operationId;
}

/**
 * 상태 변경 작업을 완료하고 마지막 요청이면 처리 중 모달과 이동 차단을 해제함
 *
 * @author SeungHyeon.Kang
 * @param operationId 완료된 상태 변경 작업 식별값
 * @param completion 같은 모달에 표시할 선택 성공 정보
 * @return 처리 중 화면과 이동 가드 정리 완료 Promise
 */
export async function endBlockingOperation(
  operationId: number,
  completion?: BlockingOperationCompletion,
): Promise<void> {
  // 이미 완료된 요청은 다른 상태 변경 작업의 진행 상태에 영향을 주지 않음
  if (!activeOperationIds.delete(operationId)) {
    // 중복 해제 요청을 추가 화면 변경 없이 종료함
    return;
  }

  // 다른 상태 변경 작업이 남아 있으면 공통 모달과 이동 가드를 유지함
  if (activeOperationIds.size > 0) {
    // 마지막 요청이 완료될 때까지 현재 처리 중 화면을 유지하고 종료함
    return;
  }

  const completedModalPromise = modalResultPromise;
  // 성공 또는 실패 알림이 열리기 전에 동일 URL의 임시 이력과 논리 가드를 함께 해제함
  await deactivateNavigationGuard();
  // 성공 정보를 지정한 작업은 현재 DOM을 유지한 채 로딩 상태를 성공 상태로 전환함
  if (modalAbortController && completion) {
    completeSweetBlockingOperation(modalAbortController, completion);
  } else {
    // 완료 정보가 없는 작업은 기존처럼 버튼 없는 처리 중 모달을 닫음
    modalAbortController?.abort();
  }
  // 다음 최초 작업에서 새 모달 제어 객체를 만들도록 참조를 비움
  modalAbortController = null;
  // 다음 작업이 이전 모달의 확인 완료 Promise를 기다리지 않도록 참조를 비움
  modalResultPromise = null;

  // 성공 전환을 요청한 호출부는 사용자가 같은 모달을 확인한 뒤 후속 처리를 실행하게 함
  if (completedModalPromise) {
    await completedModalPromise;
  }
}

/**
 * API 호출 전 준비 과정까지 포함한 상태 변경 작업을 공통 이동 차단 범위에서 실행함
 *
 * @author SeungHyeon.Kang
 * @param task 처리 중 화면을 유지할 비동기 상태 변경 작업
 * @param options 작업별 처리 중 문구
 * @return 상태 변경 작업의 완료 결과
 * @throws 전달받은 상태 변경 작업에 실패하면 발생함
 */
export async function runBlockingOperation<T>(
  task: BlockingOperationTask<T>,
  options: BlockingOperationOptions = {},
): Promise<T> {
  // API 요청 전에 필요한 파일 처리와 권한 요청부터 화면 이동을 차단함
  const operationId = beginBlockingOperation(options);

  // 성공은 같은 모달의 완료 상태로 전환하고 실패는 처리 중 모달만 닫도록 분리함
  try {
    const taskResult = await task();
    // 성공 문구가 있으면 같은 모달에서 완료 상태로 전환하고 사용자 확인까지 기다림
    await endBlockingOperation(operationId, options.success);
    // 호출 화면이 처리 결과를 이어서 사용할 수 있도록 비동기 작업 결과를 반환함
    return taskResult;
  }

  // 실패한 작업은 성공 상태로 전환하지 않고 기존 오류 경로가 이어지게 함
  catch (error) {
    // 오류 알림이 열리기 전에 버튼 없는 처리 중 모달을 닫음
    await endBlockingOperation(operationId);
    // 호출 화면이 기존 실패 문구를 표시할 수 있도록 원래 오류를 다시 전달함
    throw error;
  }
}

// 이전 실행에서 남은 이동 차단 항목이 있으면 최초 홈 정리와 뒤로가기에서 방향을 판별하도록 보관함
lastObservedNavigationGuardMarker = getNavigationGuardMarker(window.history.state);
// 페이지를 다시 연 뒤에도 기존 이동 차단 History 쌍을 처리하도록 애플리케이션 수명 이벤트를 준비함
ensurePopStateListener();
