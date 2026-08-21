import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/app/query/queryClient";
import { HomeNavigationProvider } from "@/app/navigation/HomeNavigationProvider";
import Router from "./router/Router";
import AuthSyncProvider from "@/features/Auth/components/AuthSyncProvider";
import { FullscreenImageViewerProvider } from "@/components/ImageViewer/FullscreenImageViewer";

/**
 * React Query Provider와 애플리케이션 Router를 연결하는 최상위 컴포넌트입니다.
 *
 * @author HanWon.Jang
 * @return 애플리케이션 루트 컴포넌트
 */
export default function App() {

  return (
    <QueryClientProvider client={queryClient}>
      <AuthSyncProvider>
        <FullscreenImageViewerProvider>
          <HomeNavigationProvider>
            <Router />
          </HomeNavigationProvider>
        </FullscreenImageViewerProvider>
      </AuthSyncProvider>
    </QueryClientProvider>
  );
}
