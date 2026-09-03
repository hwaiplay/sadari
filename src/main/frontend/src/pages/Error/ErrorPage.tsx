import { message } from "@/app/messages/message";
import { sweetAlert } from "@/app/lib/sweetAlert/sweetAlert";
import { useHomeNavigation } from "@/app/navigation/HomeNavigationProvider";
import { useCallback, useEffect } from "react";
import * as styles from "./ErrorPage.css";

/**
 * 등록되지 않은 URL에 접근한 사용자에게 홈 이동 안내를 제공함
 *
 * @author HanWon.Jang
 * @return 공통 경고 알럿만 표시하는 빈 라우트 화면
 */
const ErrorPage = () => {

  // 잘못된 주소와 그 아래의 앱 이력을 제거하도록 홈 루트 이동 함수를 조회함
  const moveHome = useHomeNavigation();

  /**
   * 등록되지 않은 URL 안내가 종료되면 사용자를 홈 화면으로 이동함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleAlertClosed = useCallback((): void => {
    // 경고 알럿이 닫힌 뒤 빈 라우트 화면에 머물지 않도록 홈 루트로 이동함
    moveHome();
  }, [moveHome]);

  /**
   * 등록되지 않은 URL을 공통 SweetAlert 경고와 홈 이동 버튼으로 안내함
   *
   * @author HanWon.Jang
   * @return 화면 이탈 시 현재 알림을 닫는 정리 함수
   */
  const showNotFoundAlert = useCallback((): (() => void) => {
    // 잘못된 주소 화면의 생명주기와 현재 알림의 종료 시점을 연결함
    const closeController = new AbortController();
    // 등록되지 않은 URL 안내를 공통 경고 알럿 디자인으로 표시함
    const alertPromise = sweetAlert({
      icon: "warning",
      customClass: styles.notFoundAlert,
      title: /* "페이지를 찾을 수 없어요" */ message("frontend.error.notFound.title"),
      text: /* "주소가 잘못되었거나 페이지가 이동되었어요. 홈에서 다시 시작해 주세요." */ message(
        "frontend.error.notFound.description",
      ),
      confirmButtonText: /* "홈으로 가기" */ message("frontend.error.notFound.home"),
      closeSignal: closeController.signal,
    });

    // 사용자가 알림을 닫은 경우에만 홈으로 이동하고 뒤로가기 정리에서는 현재 이력을 유지함
    void alertPromise.then(() => {
      // 화면 이탈 신호로 닫힌 알림은 이미 이동한 이전 페이지를 덮어쓰지 않음
      if (closeController.signal.aborted) {
        // 뒤로가기 이후 홈 이동 후속 처리를 중단함
        return;
      }

      handleAlertClosed();
    });

    // 뒤로가기를 포함한 화면 이탈 시 남아 있는 알림과 스크롤 잠금을 함께 해제함
    return () => {
      closeController.abort();
    };
  }, [handleAlertClosed]);

  // 등록되지 않은 URL 화면이 열리면 공통 경고 알럿을 한 번 표시함
  useEffect(showNotFoundAlert, [showNotFoundAlert]);

  // 공통 SweetAlert 외의 헤더와 내비게이션 및 페이지 콘텐츠를 표시하지 않음
  return null;
};

export default ErrorPage;
