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
    if (!isLoading) {
      if (showChildren) navigate("/home");
      else if (navigateTo) navigate(navigateTo);
    }
  }, [isLoading, showChildren, navigateTo]);

  return <Loading />;
};

export default Oauth;
