import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
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
  const [searchParams] = useSearchParams();
  const isAccountReactivated = searchParams.get("reactivated") === "Y";
  const reactivationNoticeShownRef = useRef(false);
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

      // 비활성화 계정 복귀 로그인은 정책을 다시 확인한 뒤 정상 서비스 화면으로 이동한다
      if (isAccountReactivated) {
        // 개발 모드의 Effect 재실행과 인증 상태 갱신에도 복귀 안내를 한 번만 표시한다
        if (reactivationNoticeShownRef.current) {
          // 이미 열린 복귀 안내가 닫힐 때까지 현재 OAuth 화면을 유지한다
          return;
        }

        // 복귀 안내를 열기 전에 표시 상태를 기록해 중복 팝업을 방지한다
        reactivationNoticeShownRef.current = true;
        // "다시 돌아와서 반가워요"
        // "계정이 다시 활성화됐어요. 비활성화하면서 비공개·삭제·중지된 독후감 공개 설정, 댓글, 알림과 푸시 구독은 자동 복원되지 않아요."
        void sweetSuccess(
          "다시 돌아와서 반가워요",
          "계정이 다시 활성화됐어요. 비활성화하면서 비공개·삭제·중지된 독후감 공개 설정, 댓글, 알림과 푸시 구독은 자동 복원되지 않아요.",
        ).then(() => {

          // 복귀 정책을 확인한 사용자를 선택한 정상 서비스 화면으로 이동한다
          navigate(destination, { replace: true });
        });
        // 복귀 안내가 닫히기 전에 OAuth 화면을 벗어나지 않도록 처리를 종료한다
        return;
      }

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
  }, [isAccountReactivated, isAuthenticated, isDeletePending, isLoading, isOnboardingRequired, navigate]);

  return <Loading title={message("frontend.common.loginLoading")} />;
};

export default Oauth;
