import { QueryClient } from "@tanstack/react-query";

/**
 * React Query client singleton used by the app and non-hook modules.
 *
 * @author Hanwon.Jang
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // DB 연결 장애 상태에서 같은 조회 API가 자동 재시도되면 화면은 계속 로딩처럼 보이고 서버 로그도 반복된다.
      retry: false,
      // 사용자가 창을 다시 포커스할 때 실패한 목록 조회가 자동으로 반복되지 않도록 명시적으로 끈다.
      refetchOnWindowFocus: false,
    },
  },
});
