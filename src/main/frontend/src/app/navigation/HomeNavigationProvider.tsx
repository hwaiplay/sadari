import type { ReactNode } from "react";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";

type HomeNavigationOptions = {
  resetHomeSearch?: boolean;
};

type HomeNavigation = (options?: HomeNavigationOptions) => void;

type HomeNavigationProviderProps = {
  children: ReactNode;
};

type RouterHistoryState = {
  idx?: unknown;
};

type PendingHomeNavigation = {
  options?: HomeNavigationOptions;
};

// 앱 전체에서 동일한 홈 루트 복귀 정책을 사용하도록 이동 함수를 공유한다
const HomeNavigationContext = createContext<HomeNavigation | null>(null);

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
  function completePendingHomeNavigation(): void {

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
  }

  // POP 위치 변경이 끝나면 앱 진입 항목을 홈으로 교체한다
  useEffect(completePendingHomeNavigation, [location.key, navigate]);

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
