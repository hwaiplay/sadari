/**
 * fileName       : KakaoOAuth
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 카카오 로그인 토큰 인증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const KakaoOAuth = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    // accessToken을 URL에서 가져와 localStorage에 저장
    const accessToken = params.get("accessToken");
    localStorage.setItem("token", accessToken || "");

    if (accessToken) {
      navigate("/home");
    } else {
      alert("로그인에 실패했습니다. 다시 시도해주세요.");
      navigate("/login");
    }
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

export default KakaoOAuth;
