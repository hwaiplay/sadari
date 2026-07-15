/**
 * src/main/frontend/src/components/Layout/ScrollTop.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useEffect } from "react";
import { useLocation } from "react-router-dom";


const ScrollToTop = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]); // pathname??蹂寃쎈맆 ?뚮쭏???ㅽ뻾??

  return null;
};

export default ScrollToTop;