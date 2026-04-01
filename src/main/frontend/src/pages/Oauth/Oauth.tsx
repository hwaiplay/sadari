/**
 * fileName       : KakaoOAuth
 * author         : hanwon.Jang
 * date           : 2026-03-19
 * description    : 카카오 로그인 토큰 인증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        hanwon.Jang       주석 추가
 * 2026-03-23        hanwon.Jang       로그인 성공 로직 수정
 * 2026-03-25        hanwon.Jang       useAuthQuery 훅으로 인증 상태 조회
 */
import { useEffect } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuthQuery } from "../../features/Auth/hooks/useAuthQuery";
import { useAuthStore } from "../../features/Auth/store/authStore";
import Loading from "../../components/Loading/Loading";
import { useCheckAuth } from "../../features/Auth/hooks/useCheckAuth";

const Oauth = () => {
  const { isLoading, navigateTo, showChildren } = useCheckAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && navigateTo) {
      navigate("/home");

      // <Navigate to={navigateTo} replace />;
      // if (showChildren) navigate("/home");
      // else if (navigateTo) navigate(navigateTo);
    } else {
      alert("인증에 실패했습니다. 로그인 페이지로 이동합니다.");
      navigate("/login");
    }
  }, [isLoading, navigateTo]);

  return <Loading />;
};

export default Oauth;
