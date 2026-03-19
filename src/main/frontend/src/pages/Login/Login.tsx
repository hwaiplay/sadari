/**
 * fileName       : Login
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 로그인 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

import React from "react";
import { Link } from "react-router-dom";

function Login() {
  const REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;
  const REDIRECT_URI = "http://localhost:8080/api/oauth/callback/kakao";
  const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=profile_nickname,profile_image`;

  return (
    <main style={{ marginTop: "100px" }}>
      <img src={"/img/common/logo-b.svg"} alt="사다리 로고" />
      <h1>로그인</h1>
      <Link to={KAKAO_AUTH_URL}>카카오로 3초만에 시작하기</Link>
    </main>
  );
}

export default Login;
