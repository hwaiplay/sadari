// 하단 내비게이션 이동과 헤더 루트 화면 판정에 공통으로 사용하는 경로
export const BOTTOM_NAV_PATH = {
  home: "/home",
  feed: "/feed",
  club: "/reading-clubs/mine",
  timer: "/timer",
  myPage: "/mypage/profile",
} as const;

export type BottomNavTransition = {
  fromIndex: number;
  toIndex: number;
};

export type BottomNavState = {
  bottomNavTransition: BottomNavTransition;
};

export type BottomNavDirection = "forward" | "back";

// 화면에 표시되는 하단 내비게이션의 좌우 순서를 단일 기준으로 관리함
export const BOTTOM_NAV_ORDER = [
  BOTTOM_NAV_PATH.home,
  BOTTOM_NAV_PATH.feed,
  BOTTOM_NAV_PATH.club,
  BOTTOM_NAV_PATH.timer,
  BOTTOM_NAV_PATH.myPage,
] as const;

const BOTTOM_NAV_COUNT = BOTTOM_NAV_ORDER.length;

/**
 * 현재 경로가 속한 하단 내비게이션 탭의 표시 순서를 조회함
 *
 * @author HanWon.Jang
 * @param pathname 현재 화면 경로
 * @return 하단 탭 순서 또는 하단 탭 화면이 아닐 때 null
 */
export const getBottomNavIndex = (pathname: string): number | null => {

  // 홈 루트 화면은 첫 번째 탭으로 판정함
  if (pathname === BOTTOM_NAV_PATH.home) {
    // 홈 탭의 표시 순서를 반환함
    return BOTTOM_NAV_ORDER.indexOf(BOTTOM_NAV_PATH.home);
  }

  // 피드 루트와 하위 화면은 두 번째 탭으로 판정함
  if (pathname === BOTTOM_NAV_PATH.feed
      || pathname.startsWith(`${BOTTOM_NAV_PATH.feed}/`)) {
    // 피드 탭의 표시 순서를 반환함
    return BOTTOM_NAV_ORDER.indexOf(BOTTOM_NAV_PATH.feed);
  }

  // 독서 모임 전체 화면은 세 번째 탭으로 판정함
  if (pathname.startsWith("/reading-clubs")) {
    // 모임 탭의 표시 순서를 반환함
    return BOTTOM_NAV_ORDER.indexOf(BOTTOM_NAV_PATH.club);
  }

  // 타이머 루트와 하위 화면은 네 번째 탭으로 판정함
  if (pathname === BOTTOM_NAV_PATH.timer
      || pathname.startsWith(`${BOTTOM_NAV_PATH.timer}/`)) {
    // 타이머 탭의 표시 순서를 반환함
    return BOTTOM_NAV_ORDER.indexOf(BOTTOM_NAV_PATH.timer);
  }

  // 마이페이지 루트와 하위 화면은 다섯 번째 탭으로 판정함
  if (pathname === "/mypage" || pathname.startsWith("/mypage/")) {
    // 마이페이지 탭의 표시 순서를 반환함
    return BOTTOM_NAV_ORDER.indexOf(BOTTOM_NAV_PATH.myPage);
  }

  // 하단 탭에 속하지 않는 화면은 기존 라우터 전환을 사용하도록 null을 반환함
  return null;
};

/**
 * 하단 내비게이션 탭 사이의 이동 순서를 위치 상태로 생성함
 *
 * @author HanWon.Jang
 * @param fromPath 이동을 시작하는 현재 화면 경로
 * @param toPath 사용자가 선택한 하단 탭 경로
 * @return 탭 순서 전환 상태 또는 방향을 판정할 수 없을 때 null
 */
export const getBottomNavState = (fromPath: string, toPath: string): BottomNavState | null => {

  // 출발 화면이 속한 하단 탭 순서를 조회함
  const fromIndex = getBottomNavIndex(fromPath);
  // 도착 화면이 속한 하단 탭 순서를 조회함
  const toIndex = getBottomNavIndex(toPath);

  // 탭 외부 화면 이동과 같은 탭 재선택은 기존 페이지 전환을 유지함
  if (fromIndex === null || toIndex === null || fromIndex === toIndex) {
    // 하단 탭 전용 방향을 적용하지 않도록 null을 반환함
    return null;
  }

  // 하단 탭의 출발 및 도착 순서를 포함한 일회성 위치 상태를 반환함
  return {
    bottomNavTransition: {
      fromIndex,
      toIndex,
    },
  };
};

/**
 * 현재 위치 상태를 검증하여 하단 탭 전용 화면 진입 방향을 결정함
 *
 * @author HanWon.Jang
 * @param state React Router가 현재 화면에 전달한 위치 상태
 * @param pathname 전환이 완료된 현재 화면 경로
 * @param isHistoryPop 브라우저 이력 탐색으로 이동했는지 여부
 * @return 하단 탭 전용 진입 방향 또는 기존 전환을 사용해야 할 때 null
 */
export const getBottomNavDirection = (state: unknown, pathname: string, isHistoryPop: boolean): BottomNavDirection | null => {

  // 브라우저 앞뒤 이동에서는 이력에 저장된 과거 탭 전환 상태를 재사용하지 않음
  if (isHistoryPop) {
    // 기존 POP 전환 방향을 유지하도록 null을 반환함
    return null;
  }

  // 객체가 아닌 외부 위치 상태는 하단 탭 이동 정보로 사용하지 않음
  if (typeof state !== "object" || state === null
      || !("bottomNavTransition" in state)) {
    // 유효한 하단 탭 이동 상태가 없어 null을 반환함
    return null;
  }

  const transition = state.bottomNavTransition;

  // 출발 및 도착 순서가 숫자인 위치 상태만 방향 계산에 사용함
  if (typeof transition !== "object" || transition === null
      || !("fromIndex" in transition) || !("toIndex" in transition)
      || typeof transition.fromIndex !== "number"
      || typeof transition.toIndex !== "number") {
    // 손상되거나 임의로 조작된 위치 상태를 적용하지 않도록 null을 반환함
    return null;
  }

  const targetIndex = getBottomNavIndex(pathname);
  const isFromIndexValid = Number.isInteger(transition.fromIndex)
    && transition.fromIndex >= 0 && transition.fromIndex < BOTTOM_NAV_COUNT;
  const isToIndexValid = Number.isInteger(transition.toIndex)
    && transition.toIndex >= 0 && transition.toIndex < BOTTOM_NAV_COUNT;

  // 현재 목적지와 일치하는 서로 다른 유효 탭 사이의 이동만 허용함
  if (!isFromIndexValid || !isToIndexValid || targetIndex === null
      || targetIndex !== transition.toIndex
      || transition.fromIndex === transition.toIndex) {
    // 목적지가 다르거나 범위를 벗어난 상태는 기존 라우터 전환으로 처리함
    return null;
  }

  // 오른쪽에 배치된 탭은 화면 오른쪽에서 진입함
  if (transition.fromIndex < transition.toIndex) {
    // 기존 정방향 진입 애니메이션을 선택함
    return "forward";
  }

  // 왼쪽에 배치된 탭은 화면 왼쪽에서 진입하도록 역방향을 반환함
  return "back";
};
