import { message } from "@/app/messages/message";
import { sweetAlert } from "@/app/lib/sweetAlert/sweetAlert";
import { useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./ErrorPage.css";

/**
 * 등록되지 않은 URL에 접근한 사용자에게 홈 이동 안내를 제공한다
 *
 * @author HanWon.Jang
 * @return 공통 경고 알럿만 표시하는 빈 라우트 화면
 */
const ErrorPage = () => {

  // 등록되지 않은 URL 안내를 닫은 뒤 이동할 홈 경로를 준비한다
  const navigate = useNavigate();

  /**
   * 등록되지 않은 URL 안내가 종료되면 사용자를 홈 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleAlertClosed = useCallback((): void => {
    // 경고 알럿이 닫힌 뒤 빈 라우트 화면에 머물지 않도록 홈으로 이동한다
    navigate("/home", { replace: true });
  }, [navigate]);

  /**
   * 등록되지 않은 URL을 공통 SweetAlert 경고와 홈 이동 버튼으로 안내한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const showNotFoundAlert = useCallback((): void => {
    // 등록되지 않은 URL 안내를 공통 경고 알럿 디자인으로 표시한다
    const alertPromise = sweetAlert({
      icon: "warning",
      customClass: styles.notFoundAlert,
      title: /* "페이지를 찾을 수 없어요" */ message("frontend.error.notFound.title"),
      text: /* "주소가 잘못되었거나 페이지가 이동되었어요. 홈에서 다시 시작해 주세요." */ message(
        "frontend.error.notFound.description",
      ),
      confirmButtonText: /* "홈으로 가기" */ message("frontend.error.notFound.home"),
    });

    // 확인 버튼이나 알럿 바깥 클릭으로 안내가 닫히면 홈 화면으로 이동한다
    void alertPromise.then(handleAlertClosed);
  }, [handleAlertClosed]);

  // 등록되지 않은 URL 화면이 열리면 공통 경고 알럿을 한 번 표시한다
  useEffect(showNotFoundAlert, [showNotFoundAlert]);

  // 공통 SweetAlert 외의 헤더와 내비게이션 및 페이지 콘텐츠를 표시하지 않는다
  return null;
};

export default ErrorPage;
