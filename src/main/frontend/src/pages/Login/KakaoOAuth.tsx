/**
 * fileName       : KakaoOAuth
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 토큰 인증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */
import { useEffect } from "react";

const KakaoOAuth = () => {
  useEffect(() => {
    const url = new URL(window.location.href);
    const token = url.searchParams.get("token");

    if (token) {
      localStorage.setItem("token", token); // 저장
      window.location.href = "/home"; // 홈으로 이동

      alert("WELCOME TO SADARI !");
    }
  }, []);

  return <div>로그인 처리중...</div>;
};

export default KakaoOAuth;
