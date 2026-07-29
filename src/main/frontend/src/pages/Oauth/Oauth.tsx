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
  const { isLoading, isAuthenticated, isDeletePending } = useCheckAuth();

  useEffect(() => {

    if (isLoading) {
      return;
    }

    if (isAuthenticated) {
      // 영구 삭제 대기 회원은 일반 홈 대신 탈퇴 취소 전용 화면으로 이동합니다
      navigate(isDeletePending ? "/withdrawal/pending" : "/home", { replace: true });
      return;
    }

    void sweetError(
      message("frontend.alert.authFailedTitle"),
      message("frontend.auth.failedRedirect"),
    ).then(() => {

      navigate("/login", { replace: true });
    });
  }, [isAuthenticated, isDeletePending, isLoading, navigate]);

  return <Loading title={message("frontend.common.loginLoading")} />;
};

export default Oauth;
