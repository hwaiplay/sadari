import { message } from "@/app/messages/message";
import { Link } from "react-router-dom";
import { content, kakaoLoginBtn, loginContainer, title } from "./Login.css";

/**
 * Kakao OAuth 시작 링크를 제공하는 로그인 화면을 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @return 로그인 페이지 컴포넌트
 */
function LoginPage() {
  const REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;
  const REDIRECT_URI = "http://localhost:8080/api/oauth/callback/kakao";
  const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=profile_nickname,profile_image`;

  return (
    <main className={loginContainer}>
      <div className={content}>
        <img
          src={"/img/common/logo-b.svg"}
          alt={message("frontend.common.logoAlt")}
        />
        <h1 className={title}>
          {message("frontend.auth.loginCopy")}
        </h1>
        <Link to={KAKAO_AUTH_URL} className={kakaoLoginBtn}>
          {message("frontend.auth.kakaoStart")}
        </Link>
      </div>
    </main>
  );
}

export default LoginPage;
