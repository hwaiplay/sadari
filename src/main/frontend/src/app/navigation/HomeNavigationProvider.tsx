import type { ReactNode } from "react";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import type { BottomNavTransition } from "@/app/navigation/bottomNavigation";
import { beginHomeHistoryReset } from "@/app/navigation/blockingOperation";

const HOME_PATH = "/home";

type HomeNavigationOptions = {
  resetHomeSearch?: boolean;
  bottomNavTransition?: BottomNavTransition;
};

type HomeNavigation = (options?: HomeNavigationOptions) => void;

type HomeNavigationProviderProps = {
  children: ReactNode;
};

type RouterHistoryState = {
  idx?: unknown;
  sadariHomeExitGuard?: unknown;
  sadariBlockingOperation?: unknown;
};

type PendingHomeNavigation = {
  options?: HomeNavigationOptions;
};

type StandaloneNavigator = Navigator & {
  standalone?: boolean;
};

type CloseWatcherInstance = EventTarget & {
  destroy: () => void;
};

type CloseWatcherConstructor = new () => CloseWatcherInstance;

type WindowWithCloseWatcher = Window & {
  CloseWatcher?: CloseWatcherConstructor;
};

type RestoreExitGuard = () => void;

// 앱 전체에서 동일한 홈 루트 복귀 정책을 사용하도록 이동 함수를 공유한다
const HomeNavigationContext = createContext<HomeNavigation | null>(null);

/**
 * 현재 화면이 브라우저 탭이 아닌 설치형 PWA로 실행 중인지 확인한다
 *
 * @author SeungHyeon.Kang
 * @return 설치형 PWA 실행 여부
 */
const isStandalonePwa = (): boolean => {

  // Android와 데스크톱 설치형 PWA의 display mode를 확인한다
  const isStandaloneDisplay = window.matchMedia("(display-mode: standalone)").matches;
  const standaloneNavigator = navigator as StandaloneNavigator;

  // 표준 display mode 또는 iOS 전용 상태 중 하나가 설치 실행을 나타내면 PWA로 판정한다
  return isStandaloneDisplay || standaloneNavigator.standalone === true;
};

/**
 * 현재 브라우저가 기기 고유의 뒤로가기 요청을 전달하는 CloseWatcher를 제공하는지 확인한다
 *
 * @author SeungHyeon.Kang
 * @return CloseWatcher 생성자 또는 미지원 환경일 때 null
 */
const getCloseWatcherClass = (): CloseWatcherConstructor | null => {

  const closeWatcherConstructor = (window as WindowWithCloseWatcher).CloseWatcher;

  // CloseWatcher를 지원하지 않는 브라우저는 History 가드 방식으로 처리한다
  if (typeof closeWatcherConstructor !== "function") {
    // 지원 생성자가 없어 폴백 처리를 위한 null을 반환한다
    return null;
  }

  // 모바일 기기의 닫기 요청을 직접 받을 생성자를 반환한다
  return closeWatcherConstructor;
};

/**
 * 홈 뒤로가기를 받을 동일 URL의 종료 확인용 History 상태를 생성한다
 *
 * @author SeungHyeon.Kang
 * @return React Router 상태를 보존한 홈 종료 가드 상태
 */
const getHomeExitGuardState = (): RouterHistoryState => {

  const historyState = window.history.state as RouterHistoryState | null;

  // React Router 인덱스와 위치 상태를 유지하면서 종료 가드 표식만 추가한 상태를 반환한다
  return {
    ...historyState,
    sadariHomeExitGuard: true,
  };
};

/**
 * React Router가 현재 세션 이력에 기록한 위치 인덱스를 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 앱 이력 인덱스 또는 확인할 수 없을 때 null
 */
const getCurrentHistoryIndex = (): number | null => {

  const historyState = window.history.state as RouterHistoryState | null;

  // 라우터 인덱스가 없으면 임의 이동으로 브라우저 외부 이력을 건드리지 않는다
  if (typeof historyState?.idx !== "number") {
    // 현재 항목만 홈으로 교체할 수 있도록 인덱스 없음 상태를 반환한다
    return null;
  }

  // 앱 진입점까지 이동할 거리를 계산할 수 있는 현재 인덱스를 반환한다
  return historyState.idx;
};

/**
 * 홈 검색 초기화 요청을 React Router 위치 상태로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param options 홈 이동 시 적용할 화면 상태
 * @return 홈 화면에 전달할 위치 상태 또는 상태가 없을 때 null
 */
const getHomeLocationState = (options?: HomeNavigationOptions): HomeNavigationOptions | null => {

  // 검색 초기화 또는 하단 탭 방향 정보가 있을 때 홈 화면에 일회성 상태를 전달한다
  if (options?.resetHomeSearch || options?.bottomNavTransition !== undefined) {
    // 홈 검색 정책과 하단 탭 진입 방향을 함께 보존한 위치 상태를 반환한다
    return {
      resetHomeSearch: options.resetHomeSearch,
      bottomNavTransition: options.bottomNavTransition,
    };
  }

  // 별도 화면 상태 없이 홈으로 이동하도록 null을 반환한다
  return null;
};

/**
 * 앱 진입 이력을 홈 루트로 교체하는 이동 함수를 전체 화면에 제공한다
 *
 * @author SeungHyeon.Kang
 * @param children 홈 이동 정책을 공유할 애플리케이션 화면
 * @return 홈 루트 이동 컨텍스트가 적용된 화면
 */
export const HomeNavigationProvider = ({ children }: HomeNavigationProviderProps) => {

  // 홈 이동 완료 시점을 감지하도록 현재 라우터 위치를 조회한다
  const location = useLocation();
  // 앱 이력 이동과 홈 항목 교체에 사용할 라우터 이동 함수를 조회한다
  const navigate = useNavigate();
  // 루트 항목으로 돌아간 직후 홈 교체를 완료할 요청을 보관한다
  const pendingNavigationRef = useRef<PendingHomeNavigation | null>(null);
  // 종료 확인 모달이 중복 표시되지 않도록 현재 표시 여부를 보관한다
  const exitPromptOpenRef = useRef(false);
  // History 이동 없이 기기 뒤로가기를 받을 현재 CloseWatcher를 보관한다
  const closeWatcherRef = useRef<CloseWatcherInstance | null>(null);

  /**
   * 현재 화면 아래의 앱 이력을 제거하고 홈을 세션 루트로 배치한다
   *
   * @author SeungHyeon.Kang
   * @param options 홈 검색 초기화 등 이동 후 적용할 화면 상태
   * @return 반환값이 없다
   */
  // 어느 화면에서 호출해도 같은 루트 복귀 함수를 유지하도록 메모이제이션한다
  const moveHome = useCallback((options?: HomeNavigationOptions): void => {
    // 이미 루트 복귀가 진행 중이면 중복 POP으로 앱 외부 이력까지 이동하지 않도록 차단한다
    if (pendingNavigationRef.current !== null) {
      // 먼저 시작된 홈 이동이 완료될 때까지 추가 요청을 종료한다
      return;
    }

    // 앱 진입점까지 되돌아갈 거리를 계산하기 위해 현재 라우터 인덱스를 조회한다
    const currentHistoryIndex = getCurrentHistoryIndex();

    // 앱 내부 이력이 있으면 최초 항목으로 돌아간 뒤 해당 항목을 홈으로 교체한다
    if (currentHistoryIndex !== null && currentHistoryIndex > 0) {
      // POP 이동이 완료된 뒤 적용할 홈 화면 상태를 보관한다
      pendingNavigationRef.current = { options };
      // 이전 버전의 저장 가드 기준 항목도 앱 루트로 사용하고 외부 이력은 건드리지 않게 표시한다
      beginHomeHistoryReset();
      // 중간 상세 화면을 뒤로가기 대상에서 제외하도록 앱의 첫 이력으로 이동한다
      navigate(-currentHistoryIndex);
      // 비동기 POP 완료 전 중복 홈 이동을 실행하지 않도록 종료한다
      return;
    }

    // 최초 항목이 현재 화면이면 이력을 추가하지 않고 홈으로 교체한다
    navigate("/home", {
      replace: true,
      // 홈 검색 초기화 요청만 일회성 위치 상태로 전달한다
      state: getHomeLocationState(options),
    });
  }, [navigate]);

  /**
   * 앱의 첫 이력에 도착한 POP 이동을 홈 화면 교체로 완료한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const completePendingHomeNav = useCallback((): void => {

    // 루트 복귀를 기다리는 요청이 없으면 일반 라우팅 흐름을 유지한다
    if (pendingNavigationRef.current === null) {
      // 현재 위치 변경에 추가 처리가 필요하지 않아 완료 처리를 종료한다
      return;
    }

    // POP 이동이 앱의 첫 이력에 도착했는지 확인한다
    const currentHistoryIndex = getCurrentHistoryIndex();

    // 아직 첫 이력에 도착하지 않았으면 다음 위치 변경까지 홈 교체를 보류한다
    if (currentHistoryIndex !== 0) {
      // 목표 인덱스가 아닌 중간 위치에서는 현재 라우팅을 유지한다
      return;
    }

    const pendingNavigation = pendingNavigationRef.current;
    // 완료된 요청이 다음 위치 변경에서 다시 실행되지 않도록 제거한다
    pendingNavigationRef.current = null;
    // 앱의 첫 이력을 홈으로 교체해 다음 뒤로가기를 OS가 처리하도록 한다
    navigate("/home", {
      replace: true,
      // 홈 검색 초기화 요청만 일회성 위치 상태로 전달한다
      state: getHomeLocationState(pendingNavigation.options),
    });
  }, [navigate]);

  /**
   * 공통 함수를 거치지 않고 홈에 도착한 경우에도 앱 내부 이력을 첫 항목까지 정리한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const normalizeHomeHistory = useCallback((): void => {

    // 홈 이외의 화면과 이미 루트 복귀가 진행 중인 화면은 현재 이력을 유지한다
    if (location.pathname !== HOME_PATH || pendingNavigationRef.current !== null) {
      // 홈 루트 정리가 필요하지 않아 현재 위치 처리를 종료한다
      return;
    }

    // 직접 홈 이동으로 남은 앱 내부 이력의 깊이를 확인한다
    const currentHistoryIndex = getCurrentHistoryIndex();

    // 앱의 첫 이력이 아니면 중간 화면을 제거한 뒤 홈으로 교체할 요청을 시작한다
    if (currentHistoryIndex !== null && currentHistoryIndex > 0) {
      // POP 완료 후 현재 홈 상태를 첫 항목에 다시 적용하도록 요청을 보관한다
      pendingNavigationRef.current = { options: location.state as HomeNavigationOptions | undefined };
      // 저장 가드가 남긴 기준 항목에서 추가 뒤로가기가 발생하지 않도록 홈 정리 시작을 알린다
      beginHomeHistoryReset();
      // 앱 진입 이후 쌓인 화면을 한 번에 제거하도록 첫 이력으로 이동한다
      navigate(-currentHistoryIndex);
    }
  }, [location.pathname, location.state, navigate]);

  /**
   * PWA 홈의 두 번째 뒤로가기 종료 방법을 안내하고 취소 시 감지 수단을 복원한다
   *
   * @author SeungHyeon.Kang
   * @param restoreExitGuard 취소 뒤 현재 브라우저의 종료 감지 수단을 복원할 함수
   * @return 종료 안내 처리 완료 Promise
   */
  const showPwaExitGuide = useCallback(async (restoreExitGuard: RestoreExitGuard): Promise<void> => {

    // 첫 번째 뒤로가기 뒤 다음 시스템 뒤로가기가 앱 종료 동작임을 안내한다
    await sweetConfirm({
      // "앱 종료 안내"
      title: message("frontend.pwa.exitConfirmTitle"),
      // "종료를 원하시면 뒤로가기 버튼을 한 번 더 눌러주세요."
      text: message("frontend.pwa.exitConfirmText"),
      // "취소"
      cancelButtonText: message("frontend.common.cancel"),
      showConfirmButton: false,
      allowOutsideClick: false,
    });

    // 취소로 닫힌 안내 뒤 다음 첫 번째 뒤로가기를 다시 받을 수 있도록 표시 상태를 해제한다
    exitPromptOpenRef.current = false;
    // 사용자가 취소했을 때만 현재 브라우저에 맞는 다음 종료 감지 수단을 복원한다
    restoreExitGuard();
  }, []);

  /**
   * PWA 홈에 브라우저별 종료 감지 수단을 설치하고 뒤로가기 종료 안내를 처리한다
   *
   * @author SeungHyeon.Kang
   * @return 홈 화면을 벗어날 때 실행할 이벤트 정리 함수 또는 반환값 없음
   */
  const syncHomeExitGuard = useCallback((): (() => void) | undefined => {

    // 브라우저 탭과 설치형 PWA의 뒤로가기 정책을 구분한다
    const isStandalone = isStandalonePwa();

    // 일반 브라우저와 루트 정리 중인 홈에는 설치형 PWA 종료 가드를 적용하지 않는다
    if (location.pathname !== HOME_PATH || !isStandalone
        || pendingNavigationRef.current !== null) {
      // 기존 브라우저 이력 동작을 유지하도록 정리 함수 없이 종료한다
      return undefined;
    }

    const historyState = window.history.state as RouterHistoryState | null;
    const closeWatcherConstructor = getCloseWatcherClass();

    // 지원 브라우저는 History 항목을 추가하지 않고 기기 고유의 닫기 요청을 직접 처리한다
    if (closeWatcherConstructor !== null) {
      // 이전 배포에서 남은 종료 가드가 현재 항목이면 기본 홈 항목으로 먼저 이동한다
      if (historyState?.sadariHomeExitGuard === true) {
        // 기존 가드를 앞으로 보내 다음 닫기 요청이 추가 History를 거치지 않게 한다
        window.history.back();
        // 기본 홈 항목 도착 후 Effect가 다시 실행되도록 현재 설치를 종료한다
        return undefined;
      }

      // 중첩 함수에서도 지원 생성자의 null 가능성이 다시 확장되지 않도록 타입을 확정한다
      const supportedCloseWatcher = closeWatcherConstructor;
      let isCloseWatcherActive = true;

      /**
       * 현재 홈 화면의 다음 기기 뒤로가기를 받을 CloseWatcher를 설치한다
       *
       * @author SeungHyeon.Kang
       * @return 반환값이 없다
       */
      function installCloseWatcher(): void {

        // 홈 Effect가 해제됐거나 이미 감시 중이면 CloseWatcher를 중복 생성하지 않는다
        if (!isCloseWatcherActive || window.location.pathname !== HOME_PATH
            || closeWatcherRef.current !== null) {
          // 현재 화면에는 새 CloseWatcher가 필요하지 않아 처리를 종료한다
          return;
        }

        // 첫 번째 기기 뒤로가기만 소비할 CloseWatcher를 생성한다
        const closeWatcher = new supportedCloseWatcher();
        // Effect 정리와 취소 후 재설치를 위해 현재 감시 객체를 보관한다
        closeWatcherRef.current = closeWatcher;

        /**
         * 첫 번째 기기 뒤로가기를 종료 안내로 전환하고 두 번째 요청은 운영체제에 남긴다
         *
         * @author SeungHyeon.Kang
         * @return 반환값이 없다
         */
        function handleDeviceCloseRequest(): void {

          // 한 번 사용하면 자동 해제되는 CloseWatcher와 현재 참조가 일치할 때만 비운다
          if (closeWatcherRef.current === closeWatcher) {
            // 두 번째 기기 뒤로가기가 운영체제 기본 종료로 전달되도록 참조를 제거한다
            closeWatcherRef.current = null;
          }

          // 홈 Effect 해제와 동시에 도착한 닫기 요청에는 종료 안내를 표시하지 않는다
          if (!isCloseWatcherActive || window.location.pathname !== HOME_PATH) {
            // 현재 홈 종료 흐름이 아니므로 안내 처리를 종료한다
            return;
          }

          // 안내 중에는 새 CloseWatcher를 만들지 않아 두 번째 요청이 앱 종료로 이어지게 한다
          exitPromptOpenRef.current = true;
          // 취소 버튼을 누른 경우에만 다음 첫 번째 뒤로가기를 받을 CloseWatcher를 다시 설치한다
          void showPwaExitGuide(installCloseWatcher);
        }

        // Android 뒤로가기 등 기기 고유 닫기 요청을 한 번만 처리한다
        closeWatcher.addEventListener("close", handleDeviceCloseRequest, { once: true });
      }

      // 현재 홈에서 첫 번째 시스템 뒤로가기를 받을 CloseWatcher를 설치한다
      installCloseWatcher();

      /**
       * 홈 화면을 벗어날 때 현재 CloseWatcher를 해제한다
       *
       * @author SeungHyeon.Kang
       * @return 반환값이 없다
       */
      function removeCloseWatcher(): void {

        // 완료 대기 중인 안내가 CloseWatcher를 다시 설치하지 못하도록 Effect를 비활성화한다
        isCloseWatcherActive = false;
        // 다른 화면의 기기 뒤로가기를 홈 종료 요청으로 소비하지 않도록 감시 객체를 제거한다
        closeWatcherRef.current?.destroy();
        // 다음 홈 진입에서 새 감시 객체를 설치할 수 있도록 참조를 비운다
        closeWatcherRef.current = null;
        // 다음 홈 진입이 이전 안내 표시 상태를 상속하지 않도록 초기화한다
        exitPromptOpenRef.current = false;
      }

      // CloseWatcher 방식의 홈 종료 Effect 정리 함수를 반환한다
      return removeCloseWatcher;
    }

    // 현재 홈 항목이 가드가 아니면 동일 URL 항목을 한 개만 추가해 이전 화면 노출을 차단한다
    if (historyState?.sadariHomeExitGuard !== true) {
      // React Router 상태를 보존한 종료 가드를 생성한다
      const guardState = getHomeExitGuardState();
      // PWA 뒤로가기가 앱 외부나 이전 화면보다 동일 URL 가드를 먼저 만나도록 추가한다
      window.history.pushState(guardState, "", window.location.href);
    }

    /**
     * 미지원 브라우저에서 취소 후 동일 URL History 종료 가드를 복원한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function restoreHistoryGuard(): void {

      // 안내가 닫히기 전에 다른 화면으로 이동했으면 해당 화면의 History를 변경하지 않는다
      if (window.location.pathname !== HOME_PATH) {
        // 홈 종료 가드를 복원할 수 없는 화면이므로 처리를 종료한다
        return;
      }

      const currentState = window.history.state as RouterHistoryState | null;

      // 앞으로가기로 이미 종료 가드에 도착한 경우에는 동일 URL 항목을 중복 추가하지 않는다
      if (currentState?.sadariHomeExitGuard === true) {
        // 현재 가드를 다음 뒤로가기에서 그대로 사용하도록 처리를 종료한다
        return;
      }

      // 취소 뒤 다음 첫 번째 뒤로가기를 다시 감지할 종료 가드 상태를 생성한다
      const guardState = getHomeExitGuardState();
      // 사용자가 취소했을 때만 동일 URL 가드를 복원해 다음 종료 안내를 준비한다
      window.history.pushState(guardState, "", window.location.href);
    }

    /**
     * PWA 홈의 뒤로가기 POP을 종료 확인 흐름으로 전환한다
     *
     * @author SeungHyeon.Kang
     * @param event 이동한 홈 History 상태를 포함한 이벤트
     * @return 반환값이 없다
     */
    function handleHomePopState(event: PopStateEvent): void {

      const poppedState = event.state as RouterHistoryState | null;

      // 저장 완료가 만든 동일 URL 정리 POP은 PWA 종료 요청으로 처리하지 않는다
      if (poppedState?.sadariBlockingOperation !== undefined) {
        // 저장 가드가 원래 홈 종료 항목을 복원하도록 현재 종료 처리를 건너뛴다
        return;
      }

      // 앞으로가기로 종료 가드에 도착한 경우에는 별도 확인 없이 홈을 유지한다
      if (poppedState?.sadariHomeExitGuard === true) {
        // 가드 자체의 POP을 종료 요청으로 중복 처리하지 않는다
        return;
      }

      // 종료 확인이 이미 표시 중이면 같은 제스처의 중복 이벤트를 무시한다
      if (exitPromptOpenRef.current) {
        // 가드를 다시 추가하지 않아 현재 제스처를 운영체제의 기본 종료 흐름으로 유지한다
        return;
      }

      // 종료 확인 모달이 한 번만 표시되도록 진행 상태를 기록한다
      exitPromptOpenRef.current = true;
      // 다음 뒤로가기가 앱 종료 동작임을 안내하고 취소 결과를 비동기로 처리한다
      void showPwaExitGuide(restoreHistoryGuard);
    }

    // 홈 가드 아래 항목으로 이동하는 뒤로가기를 종료 확인 흐름으로 받는다
    window.addEventListener("popstate", handleHomePopState);

    /**
     * 홈 화면을 벗어날 때 종료 확인용 POP 구독을 해제한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function removeHomeExitListener(): void {

      // 다른 화면의 정상 뒤로가기에 홈 종료 정책이 적용되지 않도록 구독을 제거한다
      window.removeEventListener("popstate", handleHomePopState);
    }

    // 홈 화면 Effect가 해제될 때 사용할 이벤트 정리 함수를 반환한다
    return removeHomeExitListener;
  }, [location.pathname, showPwaExitGuide]);

  // POP 위치 변경이 끝나면 앱 진입 항목을 홈으로 교체한다
  useEffect(completePendingHomeNav, [completePendingHomeNav, location.key]);
  // 모든 홈 진입 경로가 앱의 첫 이력으로 정리되도록 현재 위치를 검사한다
  useEffect(normalizeHomeHistory, [location.key, normalizeHomeHistory]);
  // 설치형 PWA 홈에서만 뒤로가기를 종료 확인 흐름으로 전환한다
  useEffect(syncHomeExitGuard, [location.key, syncHomeExitGuard]);

  // 전체 화면에서 홈 루트 이동 함수를 사용할 수 있는 컨텍스트를 반환한다
  return (
    <HomeNavigationContext.Provider value={moveHome}>
      {children}
    </HomeNavigationContext.Provider>
  );
};

/**
 * 현재 화면에서 앱 세션 루트로 복귀하는 홈 이동 함수를 제공한다
 *
 * @author SeungHyeon.Kang
 * @return 홈 루트 이동 함수
 * @throws Error 홈 이동 Provider 밖에서 호출할 때 발생
 */
export const useHomeNavigation = (): HomeNavigation => {

  // 상위 Provider가 공유한 홈 이동 함수를 조회한다
  const moveHome = useContext(HomeNavigationContext);

  // Provider 누락으로 홈 이동 정책을 적용할 수 없으면 구성 오류를 즉시 알린다
  if (moveHome === null) {
    throw new Error("HomeNavigationProvider 안에서 사용해야 합니다.");
  }

  // 현재 화면에서 사용할 홈 루트 이동 함수를 반환한다
  return moveHome;
};
