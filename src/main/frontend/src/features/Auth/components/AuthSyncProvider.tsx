import { queryClient } from "@/app/query/queryClient";
import { queryKeys, sessionQueryKeys } from "@/app/query/queryKeys";
import { subscribeAuthEvents, type AuthEvent } from "@/features/Auth/lib/authEvents";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { type PropsWithChildren, useEffect } from "react";

/**
 * 브라우저 탭 사이의 인증 종료 이벤트를 애플리케이션 상태에 반영함
 *
 * @author SeungHyeon.Kang
 * @param children 애플리케이션 하위 화면
 * @return 인증 탭 동기화가 적용된 하위 화면
 */
export default function AuthSyncProvider({ children }: PropsWithChildren) {

  useEffect(() => {

    /**
     * 브라우저 캐시에서 앱 문서가 복원되면 현재 쿠키 기준으로 인증 상태를 다시 확인함
     *
     * @author HanWon.Jang
     * @param event 브라우저 문서 복원 이벤트
     * @return 반환값이 없음
     */
    const handlePageShow = (event: PageTransitionEvent): void => {

      // 일반 문서 진입은 인증 Query의 최초 조회가 처리하므로 캐시 복원만 다시 확인함
      if (!event.persisted) {
        return;
      }

      // 로그인 전후의 오래된 인증 화면이 복원되지 않도록 활성 인증 Query를 즉시 갱신함
      void queryClient.refetchQueries({ queryKey: queryKeys.auth, type: "active" });
    };

    /**
     * 같은 브라우저의 로그아웃 이벤트를 현재 탭의 인증 상태와 화면에 반영함
     *
     * @author HanWon.Jang
     * @param authEvent 탭 사이에 전달된 인증 이벤트
     * @return 반환값이 없음
     */
    const handleAuthEvent = (authEvent: AuthEvent): void => {

      // 로그아웃 외의 인증 이벤트가 추가되더라도 현재 정리 정책을 적용하지 않음
      if (authEvent.type !== "LOGOUT") {
        return;
      }

      // 현재 탭의 메모리 인증 상태를 제거함
      useAuthStore.getState().clearAuth();
      // 다른 탭에서 끝난 계정의 인증과 사용자 서버 상태를 현재 탭에서도 제거함
      for (const queryKey of sessionQueryKeys) {
        // 현재 세션에 속한 공통 Query Cache를 제거함
        queryClient.removeQueries({ queryKey });
      }

      // 로그인 화면이 아닌 경우에만 현재 문서를 로그인 경로로 교체함
      if (window.location.pathname !== "/login") {
        // 로그아웃 전 보호 화면이 뒤로가기에 남지 않도록 현재 이력을 로그인 화면으로 교체함
        window.location.replace("/login");
      }
    };

    // 뒤로가기와 앞으로가기로 캐시 문서가 복원되는 시점을 구독함
    window.addEventListener("pageshow", handlePageShow);
    // 다른 탭과 현재 탭에서 발생한 로그아웃을 같은 인증 상태로 반영함
    const unsubscribeAuthEvents = subscribeAuthEvents(handleAuthEvent);

    /**
     * 인증 동기화 Provider가 해제될 때 브라우저와 탭 간 이벤트 구독을 정리함
     *
     * @author HanWon.Jang
     * @return 반환값이 없음
     */
    const cleanupAuthSync = (): void => {

      // 브라우저 캐시 복원 인증 재조회를 해제함
      window.removeEventListener("pageshow", handlePageShow);
      // 탭 간 로그아웃 동기화 구독을 해제함
      unsubscribeAuthEvents();
    };

    // 인증 동기화 Provider가 해제되면 브라우저와 탭 간 이벤트 구독을 함께 정리함
    return cleanupAuthSync;
  }, []);

  return children;
}
