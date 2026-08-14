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

const HOME_PATH = "/home";

type HomeNavigationOptions = {
  resetHomeSearch?: boolean;
};

type HomeNavigation = (options?: HomeNavigationOptions) => void;

type HomeNavigationProviderProps = {
  children: ReactNode;
};

type RouterHistoryState = {
  idx?: unknown;
  sadariHomeExitGuard?: unknown;
};

type PendingHomeNavigation = {
  options?: HomeNavigationOptions;
};

type StandaloneNavigator = Navigator & {
  standalone?: boolean;
};

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

  // 검색 초기화 요청이 있을 때만 홈 화면에 일회성 상태를 전달한다
  if (options?.resetHomeSearch) {
    // 홈 검색어와 적용 조건을 초기화할 위치 상태를 반환한다
    return { resetHomeSearch: true };
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
  // 종료 확인 뒤 가드 이력을 벗어나는 POP을 일반 홈 뒤로가기로 처리하지 않도록 표시한다
  const exitConfirmedRef = useRef(false);

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
      // 앱 진입 이후 쌓인 화면을 한 번에 제거하도록 첫 이력으로 이동한다
      navigate(-currentHistoryIndex);
    }
  }, [location.pathname, location.state, navigate]);

  /**
   * PWA 홈 뒤로가기 확인 결과에 따라 현재 앱 창을 종료한다
   *
   * @author SeungHyeon.Kang
   * @return 종료 확인 처리 완료 Promise
   */
  const confirmPwaExit = useCallback(async (): Promise<void> => {

    // PWA 종료 여부와 확인 시 동작을 사용자에게 안내한다
    const result = await sweetConfirm({
      // "앱을 종료할까요?"
      title: message("frontend.pwa.exitConfirmTitle"),
      // "확인을 누르면 앱이 종료돼요."
      text: message("frontend.pwa.exitConfirmText"),
      // "확인"
      confirmButtonText: message("frontend.common.confirm"),
      // "취소"
      cancelButtonText: message("frontend.common.cancel"),
      allowOutsideClick: false,
    });

    // 완료된 모달 뒤에 다음 뒤로가기 확인을 받을 수 있도록 표시 상태를 해제한다
    exitPromptOpenRef.current = false;

    // 취소한 경우 동일 URL의 종료 가드가 다음 뒤로가기를 계속 받게 한다
    if (!result.isConfirmed) {
      // 홈 화면을 유지하고 종료 처리를 완료한다
      return;
    }

    // 설치형 PWA 창을 사용자 확인 동작 안에서 종료한다
    window.close();

    // 브라우저가 직접 창 닫기를 허용하지 않으면 루트 이력 뒤로 이동해 운영체제 종료 동작에 위임한다
    if (!window.closed) {
      // 다음 POP은 종료용 이동이므로 확인 모달을 다시 표시하지 않도록 기록한다
      exitConfirmedRef.current = true;
      // 동일 URL의 종료 가드에서 앱의 첫 이력으로 이동한다
      window.history.back();
    }
  }, []);

  /**
   * PWA 홈에 동일 URL의 종료 가드를 설치하고 뒤로가기 종료 확인을 처리한다
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

    // 현재 홈 항목이 가드가 아니면 동일 URL 항목을 한 개만 추가해 이전 화면 노출을 차단한다
    if (historyState?.sadariHomeExitGuard !== true) {
      // React Router 상태를 보존한 종료 가드를 생성한다
      const guardState = getHomeExitGuardState();
      // PWA 뒤로가기가 앱 외부나 이전 화면보다 동일 URL 가드를 먼저 만나도록 추가한다
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

      // 종료 확인 뒤 가드 아래의 첫 이력에 도착하면 더 이전 이동을 운영체제에 위임한다
      if (exitConfirmedRef.current) {
        // 종료용 POP이 다시 확인 흐름에 들어오지 않도록 표시를 해제한다
        exitConfirmedRef.current = false;
        // PWA 첫 이력의 기본 뒤로가기 동작으로 앱을 종료한다
        window.history.back();
        // 종료 이동 이후 동일 URL 가드를 복원하지 않도록 처리를 종료한다
        return;
      }

      // 앞으로가기로 종료 가드에 도착한 경우에는 별도 확인 없이 홈을 유지한다
      if (poppedState?.sadariHomeExitGuard === true) {
        // 가드 자체의 POP을 종료 요청으로 중복 처리하지 않는다
        return;
      }

      // 이탈한 가드 항목을 즉시 복원해 확인 중 추가 뒤로가기가 이전 화면을 노출하지 않게 한다
      const guardState = getHomeExitGuardState();
      // 사용자가 확인하거나 취소할 때까지 현재 홈 URL을 유지한다
      window.history.pushState(guardState, "", window.location.href);

      // 종료 확인이 이미 표시 중이면 같은 제스처의 중복 이벤트를 무시한다
      if (exitPromptOpenRef.current) {
        // 현재 종료 확인 모달이 사용자 선택을 계속 받도록 처리를 종료한다
        return;
      }

      // 종료 확인 모달이 한 번만 표시되도록 진행 상태를 기록한다
      exitPromptOpenRef.current = true;
      // PWA 종료 여부를 사용자에게 확인하고 선택 결과를 비동기로 처리한다
      void confirmPwaExit();
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
  }, [confirmPwaExit, location.pathname]);

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
