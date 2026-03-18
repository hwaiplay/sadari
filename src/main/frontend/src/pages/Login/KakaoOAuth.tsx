// /oauth 페이지
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
