import { message } from "@/app/messages/message";
import { Link } from "react-router-dom";
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
    loginActions,
    loginContainer,
    privacyPolicyLink,
    title
} from "./Login.css";

/**
 * Kakao OAuth 시작 링크와 개인정보처리방침 경로를 제공하는 로그인 화면을 렌더링함
 *
 * @author HanWon.Jang
 * @return 로그인 페이지 컴포넌트
 */
const LoginPage = () => {

  return (
    /* 서비스 로고와 소셜 로그인 영역 */
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
          {/* "간편하게 로그인하고\n독서의 즐거움에 올라보세요." */}
          {message("frontend.auth.loginCopy")}
        </h1>
        {/* 로그인과 개인정보처리방침 이동 영역 */}
        <div className={loginActions}>
          <a href="/api/oauth/kakao" className={kakaoLoginBtn}>
            {/* "카카오로 3초만에 시작하기" */}
            {message("frontend.auth.kakaoStart")}
          </a>
          <Link to="/privacy-policy" className={privacyPolicyLink}>
            {/* "개인정보처리방침" */}
            {message("frontend.auth.privacyPolicy")}
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
              <path d="M5.19751 11.62L9.00083 7.81668C9.44999 7.36752 9.44999 6.63252 9.00083 6.18335L5.19751 2.38" stroke="currentColor" strokeWidth="1.5" strokeMiterlimit="10" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </Link>
        </div>
      </div>
    </main>
  );
};

export default LoginPage;
