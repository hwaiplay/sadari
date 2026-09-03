import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/app/query/queryClient";
import { HomeNavigationProvider } from "@/app/navigation/HomeNavigationProvider";
import Router from "./router/Router";
import AuthSyncProvider from "@/features/Auth/components/AuthSyncProvider";
import { ImageViewerProvider } from "@/components/ImageViewer/FullscreenImageViewer";
import { ConnectionErrorScreen } from "@/components/ConnectionError/ConnectionErrorScreen";

/**
 * React Query Provider와 애플리케이션 Router를 연결하는 최상위 컴포넌트임
 *
 * @author HanWon.Jang
 * @return 애플리케이션 루트 컴포넌트
 */
export default function App() {

  return (
    <QueryClientProvider client={queryClient}>
      {/* JDBC 또는 인터넷 연결 장애를 앱 전체에 안내하는 화면 */}
      <ConnectionErrorScreen />
      <AuthSyncProvider>
        <ImageViewerProvider>
          <HomeNavigationProvider>
            <Router />
          </HomeNavigationProvider>
        </ImageViewerProvider>
      </AuthSyncProvider>
    </QueryClientProvider>
  );
}
