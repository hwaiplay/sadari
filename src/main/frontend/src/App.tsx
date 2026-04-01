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
import Router from "./app/router/Router";

const queryClient = new QueryClient();

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router />
    </QueryClientProvider>
  );
}
