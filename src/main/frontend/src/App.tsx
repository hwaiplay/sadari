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
import Router from "./app/Router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
const queryClient = new QueryClient();

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router />
    </QueryClientProvider>
  );
}
