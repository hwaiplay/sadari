/**
 * fileName       : App
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       Router 분리
 */
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import Router from "./router/Router";

const queryClient = new QueryClient();

/**
 * React Query Provider와 애플리케이션 Router를 연결하는 최상위 컴포넌트이다.
 * @Author Hanwon.Jang
 * @return 애플리케이션 루트 컴포넌트
 */
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router />
    </QueryClientProvider>
  );
}
