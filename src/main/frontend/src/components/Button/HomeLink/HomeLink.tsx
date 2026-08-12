import type { ReactNode } from "react";
import { Link } from "react-router-dom";

type HomeLinkProps = {
  children: ReactNode;
  className?: string;
  resetHomeSearch?: boolean;
};

/**
 * 현재 화면 이력을 홈 루트로 교체하는 링크를 표시한다
 *
 * @author SeungHyeon.Kang
 * @param children 링크에 표시할 텍스트 또는 아이콘
 * @param className 외부에서 전달한 추가 스타일 클래스
 * @param resetHomeSearch 홈에 저장된 검색 조건 초기화 여부
 * @return 홈 루트 이동 링크
 */
const HomeLink = ({ children, className, resetHomeSearch = false }: HomeLinkProps) => {
  // 링크 기본 동작으로 홈에 이동하고 현재 화면이 뒤로가기 이력에 남지 않게 교체한다
  return (
    <Link
      to="/home"
      className={className}
      replace
      state={resetHomeSearch ? { resetHomeSearch: true } : null}
    >
      {children}
    </Link>
  );
};

export default HomeLink;
