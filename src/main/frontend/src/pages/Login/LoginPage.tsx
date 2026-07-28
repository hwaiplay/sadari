import { message } from "@/app/messages/message";
import {
    background, background_img,
    background_img_column,
    background_img_container,
    background_img_overlay,
    background_img_track_down,
    background_img_track_up,
    background_img_track_up_delayed,
    content,
    kakaoLoginBtn,
    loginContainer,
    title
} from "./Login.css";

/**
 * Kakao OAuth 시작 링크를 제공하는 로그인 화면을 렌더링합니다.
 *
 * @author HanWon.Jang
 * @return 로그인 페이지 컴포넌트
 */
function LoginPage() {

  return (
    <main className={loginContainer}>
      <div className={background}>
          <div className={background_img_overlay}></div>
          <div className={background_img_container}>
              <div className={background_img_column}>
                  <div className={background_img_track_up}>
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                  </div>
              </div>
              <div className={background_img_column}>
                  <div className={background_img_track_down}>
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                  </div>
              </div>
              <div className={background_img_column}>
                  <div className={background_img_track_up_delayed}>
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                      <img src={"/img/login-bg-books.png"} className={background_img} alt="" />
                  </div>
              </div>
          </div>
      </div>
      <div className={content}>
        <img
          src={"/img/common/logo-upper.svg"}
          alt={message("frontend.common.logoAlt")}
          width={110}
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
