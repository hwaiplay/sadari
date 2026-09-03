import { useHomeNavigation } from "@/app/navigation/HomeNavigationProvider";
import type { BottomNavState } from "@/app/navigation/bottomNavigation";
import type { MouseEvent, ReactNode } from "react";
import { Link } from "react-router-dom";

type HomeLinkProps = {
  children: ReactNode;
  className?: string;
  resetHomeSearch?: boolean;
  navigationState?: BottomNavState | null;
};

/**
 * 기존 앱 이력을 남기지 않고 홈 루트로 이동하는 링크를 표시함
 *
 * @author SeungHyeon.Kang
 * @param children 링크에 표시할 텍스트 또는 아이콘
 * @param className 외부에서 전달한 추가 스타일 클래스
 * @param resetHomeSearch 홈에 저장된 검색 조건 초기화 여부
 * @param navigationState 하단 탭 순서에 따른 홈 진입 상태
 * @return 홈 루트 이동 링크
 */
const HomeLink = ({
  children,
  className,
  resetHomeSearch = false,
  navigationState = null,
}: HomeLinkProps) => {

  // 홈 링크가 모든 중간 이력을 제거하도록 공통 루트 이동 함수를 조회함
  const moveHome = useHomeNavigation();

  /**
   * 일반 클릭은 홈 루트 이동으로 처리하고 새 탭 클릭은 링크 기본 동작을 유지함
   *
   * @author SeungHyeon.Kang
   * @param event 홈 링크 클릭 이벤트
   * @return 반환값이 없음
   */
  const handleClick = (event: MouseEvent<HTMLAnchorElement>): void => {

    // 새 탭 또는 새 창을 요청한 클릭은 브라우저의 링크 동작을 유지함
    if (event.button !== 0 || event.metaKey
        || event.ctrlKey || event.shiftKey
        || event.altKey) {
      // 수정 키가 포함된 클릭을 SPA 루트 이동으로 가로채지 않고 종료함
      return;
    }

    // 일반 링크가 홈 이력을 새로 추가하지 않도록 기본 이동을 차단함
    event.preventDefault();
    // 현재 앱 이력을 정리한 뒤 홈 검색 초기화 정책과 함께 루트로 이동함
    moveHome({
      resetHomeSearch,
      bottomNavTransition: navigationState?.bottomNavTransition,
    });
  };

  // 새 탭 접근성을 유지하면서 일반 클릭만 홈 루트 복귀로 처리하는 링크를 반환함
  return (
    <Link to="/home" className={className} onClick={handleClick}>
      {children}
    </Link>
  );
};

export default HomeLink;
