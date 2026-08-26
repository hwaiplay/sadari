import { useCallback, useEffect } from "react";
import { useLocation, useNavigate, useNavigationType } from "react-router-dom";

/**
 * fileName       : useCompletedFormGuard
 * author         : Hanwon.Jang
 * date           : 2026-08-26
 * description    : 저장을 마친 폼이 브라우저 뒤로가기로 다시 노출되지 않도록 현재 탭의 이동 이력을 보호하는 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        Hanwon.Jang    최초 생성
 */

const COMPLETED_FORM_KEY_PREFIX = "sadari:completed-form:";

export const useCompletedFormGuard = () => {
  // 현재 폼 경로와 브라우저 이동 유형을 기준으로 완료된 방문 기록을 구분
  const location = useLocation();
  const navigate = useNavigate();
  const navigationType = useNavigationType();
  const storageKey = `${COMPLETED_FORM_KEY_PREFIX}${location.pathname}`;

  // 뒤로가기로 완료된 폼에 도달하면 저장 결과 화면으로 현재 기록을 교체
  useEffect(() => {
    const completedPath = sessionStorage.getItem(storageKey);

    // 완료 표시가 없는 일반 진입은 현재 폼을 그대로 표시
    if (!completedPath) {
      return;
    }

    // 브라우저 뒤로가기로 완료된 폼을 다시 열면 저장 결과 화면으로 되돌림
    if (navigationType === "POP") {
      navigate(completedPath, { replace: true });
      return;
    }

    // 메뉴와 버튼을 통한 새 폼 진입에서는 이전 완료 표시를 제거
    sessionStorage.removeItem(storageKey);
  }, [navigate, navigationType, storageKey]);

  /**
   * 현재 폼을 완료 처리하고 저장 결과 화면으로 방문 기록을 교체
   *
   * @author HanWon.Jang
   * @param completedPath 저장 완료 후 이동할 화면 경로
   * @return
   */
  const finishForm = useCallback((completedPath: string): void => {
    // 현재 탭의 같은 폼 기록이 다시 열릴 때 사용할 완료 경로를 저장
    sessionStorage.setItem(storageKey, completedPath);
    // 현재 폼 방문 기록을 저장 결과 화면으로 교체
    navigate(completedPath, { replace: true });
  }, [navigate, storageKey]);

  return finishForm;
};
