import React from "react";
import Header from "../../components/Layout/Header/Header";
import { Link } from "react-router-dom";

function Login() {
  const REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;
  const REDIRECT_URI = "https://localhost:8080/api/oauth/callback/kakao";
  const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code`;

  return (
    <div>
      <Header />
      <main style={{ marginTop: "100px" }}>
        <h1>로그인</h1>
        <Link to={KAKAO_AUTH_URL}>카카오로 3초만에 시작하기</Link>
      </main>
    </div>
  );
}

export default Login;
