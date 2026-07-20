import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/app/query/queryClient";
import Router from "./router/Router";

/**
 * React Query Provider와 애플리케이션 Router를 연결하는 최상위 컴포넌트입니다.
 *
 * @author Hanwon.Jang
 * @return 애플리케이션 루트 컴포넌트
 */
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router />
    </QueryClientProvider>
  );
}
