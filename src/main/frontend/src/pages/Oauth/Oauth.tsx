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
import { useNavigate } from "react-router-dom";
import { useAuthQuery } from "../../features/Auth/hooks/useAuthQuery";
import { useAuthStore } from "../../features/Auth/store/authStore";

const Oauth = () => {
  const navigate = useNavigate();
  // 로그인 인증 상태 조회
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    const init = async () => {
      try {
        await checkAuth();
        navigate("/home");
      } catch {
        navigate("/login");
      }
    };

    init();
  }, []);

  return (
    <div
      style={{
        textAlign: "center",
        height: "100svh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      로그인 처리중...
    </div>
  );
};

export default Oauth;
