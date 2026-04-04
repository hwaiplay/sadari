import { useEffect } from "react";
import { Navigate } from "react-router-dom";
import Loading from "../../components/Loading/Loading";
import { useCheckAuth } from "../../features/Auth/hooks/useCheckAuth";

/**
 * fileName       : KakaoOAuth
 * author         : hanwon.Jang
 * date           : 2026-03-19
 * description    : 카카오 로그인 검증 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        hanwon.Jang       주석 추가
 * 2026-03-23        hanwon.Jang       로그인 성공 로직 수정
 * 2026-03-25        hanwon.Jang       useAuthQuery 훅으로 인증 상태 조회
 * 2026-04-01        hanwon.Jang       페이지 리디렉션 수정
 */

const Oauth = () => {
  const { isLoading } = useCheckAuth();

  useEffect(() => {
    if (!isLoading) {
      <Navigate to={"/home"} replace />;
    } else {
      alert("인증에 실패했습니다. 로그인 페이지로 이동합니다.");
      <Navigate to={"/login"} replace />;
    }
  }, [isLoading]);

  return <Loading />;
};

export default Oauth;
