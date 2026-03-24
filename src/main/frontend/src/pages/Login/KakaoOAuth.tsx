/**
 * fileName       : KakaoOAuth
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 카카오 로그인 토큰 인증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 * 2026-03-23        Hanwon.Jang       로그인 성공 로직 수정
 */
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthQuery } from "../../hooks/useAuthQuery";

const KakaoOAuth = () => {
  const { data, isLoading, isError } = useAuthQuery();
  const navigate = useNavigate();
  useEffect(() => {
    // navigate("/home");
  }, []);

  if (isLoading) {
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
  }

  return null;
};

export default KakaoOAuth;
