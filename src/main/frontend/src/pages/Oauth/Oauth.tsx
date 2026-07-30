import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Loading from "../../components/Loading/Loading";
import { useCheckAuth } from "../../features/Auth/hooks/useCheckAuth";

/**
 * Kakao OAuth 인증 결과를 확인하고 인증 상태에 맞는 화면으로 이동합니다.
 *
 * @author HanWon.Jang
 * @return OAuth 인증 처리 중 표시할 로딩 화면
 */
const Oauth = () => {

  const navigate = useNavigate();
  const {
    isLoading,
    isAuthenticated,
    isDeletePending,
    isOnboardingRequired,
  } = useCheckAuth();

  useEffect(() => {

    if (isLoading) {
      return;
    }

    if (isAuthenticated) {
      // 회원 상태와 최초 로그인 여부에 맞는 첫 화면을 선택한다
      const destination = isDeletePending
        ? "/withdrawal/pending"
        : isOnboardingRequired
          ? "/welcome"
          : "/home";
      // 인증 직후 선택한 사용자 전용 화면으로 이동한다
      navigate(destination, { replace: true });
      return;
    }

    void sweetError(
      message("frontend.alert.authFailedTitle"),
      message("frontend.auth.failedRedirect"),
    ).then(() => {

      navigate("/login", { replace: true });
    });
  }, [isAuthenticated, isDeletePending, isLoading, isOnboardingRequired, navigate]);

  return <Loading title={message("frontend.common.loginLoading")} />;
};

export default Oauth;
