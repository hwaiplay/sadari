import { queryClient } from "@/app/query/queryClient";
import { sessionQueryKeys } from "@/app/query/queryKeys";
import { subscribeAuthEvents } from "@/features/Auth/lib/authEvents";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { type PropsWithChildren, useEffect } from "react";

/**
 * 브라우저 탭 사이의 인증 종료 이벤트를 애플리케이션 상태에 반영한다.
 *
 * @author SeungHyeon.Kang
 * @param children 애플리케이션 하위 화면
 * @return 인증 탭 동기화가 적용된 하위 화면
 */
export default function AuthSyncProvider({ children }: PropsWithChildren) {

  useEffect(() => {
    return subscribeAuthEvents((authEvent) => {
      if (authEvent.type !== "LOGOUT") {
        return;
      }

      useAuthStore.getState().clearAuth();
      // 다른 탭에서 끝난 계정의 인증과 사용자 서버 상태를 현재 탭에서도 제거한다
      for (const queryKey of sessionQueryKeys) {
        // 현재 세션에 속한 공통 Query Cache를 제거한다
        queryClient.removeQueries({ queryKey });
      }

      if (window.location.pathname !== "/login") {
        window.location.replace("/login");
      }
    });
  }, []);

  return children;
}
