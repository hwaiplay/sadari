import { useEffect } from "react";
import { useLocation } from "react-router-dom";

/**
 * fileName       : ScrollTop
 * author         : Hanwon.Jang
 * date           : 2026-05-04
 * description    : 페이지 경로가 변경되면 화면 최상단으로 스크롤 해주는 함수
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-04       Hanwon.Jang       최초 생성
 */

/**
 * 라우트 경로가 변경될 때마다 브라우저 스크롤을 최상단으로 이동한다.
 * @Author Hanwon.Jang
 * @return 렌더링 없이 스크롤 부수효과만 수행하므로 null
 */
const ScrollToTop = () => {
  const { pathname } = useLocation(); // 현재 페이지 경로를 가져옴

  useEffect(() => {
    window.scrollTo(0, 0); // 페이지 경로가 변경될 때마다 스크롤을 최상단으로 이동
  }, [pathname]); // pathname이 변경될 때마다 실행됨

  return null; // 이 컴포넌트는 렌더링할 것이 없으므로 null을 반환
};

export default ScrollToTop;
