import { message } from "@/app/messages/message";
import { content, kakaoLoginBtn, loginContainer, title } from "./Login.css";

/**
 * Kakao OAuth 시작 링크를 제공하는 로그인 화면을 렌더링합니다.
 *
 * @author Hanwon.Jang
 * @return 로그인 페이지 컴포넌트
 */
function LoginPage() {
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
        <a href="/api/oauth/kakao" className={kakaoLoginBtn}>
          {message("frontend.auth.kakaoStart")}
        </a>
      </div>
    </main>
  );
}

export default LoginPage;
