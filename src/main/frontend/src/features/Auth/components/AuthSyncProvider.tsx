import { queryClient } from "@/app/query/queryClient";
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
      queryClient.removeQueries({ queryKey: ["auth"] });

      if (window.location.pathname !== "/login") {
        window.location.replace("/login");
      }
    });
  }, []);

  return children;
}
