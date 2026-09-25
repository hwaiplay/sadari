/**
 * src/main/frontend/src/components/Layout/ScrollTop.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당
 *
 * @author HanWon.Jang
 */
import { useEffect } from "react";
import { useLocation } from "react-router-dom";


/**
 * Scroll To Top 화면 또는 컴포넌트를 구성
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
const ScrollToTop = () => {

  const { pathname } = useLocation();

  useEffect(() => {

    window.scrollTo(0, 0);
  }, [pathname]); // 화면 경로 변경 시마다 실행

  return null;
};

export default ScrollToTop;
