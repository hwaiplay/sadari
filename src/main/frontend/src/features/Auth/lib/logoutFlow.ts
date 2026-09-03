import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { queryClient } from "@/app/query/queryClient";
import { sessionQueryKeys } from "@/app/query/queryKeys";
import { getPushConfigApi } from "@/features/Push/api/pushApi";
import { logoutApi, type LogoutScope } from "@/features/Auth/api/authApi";
import { useAuthStore } from "@/features/Auth/store/authStore";
import { publishAuthLogout } from "./authEvents";

/**
 * 로그아웃 Alert에서 현재 기기와 전체 기기 중 하나를 선택함
 *
 * @author SeungHyeon.Kang
 * @return 선택한 로그아웃 범위, 취소 시 null
 */
export async function selectLogoutScope(): Promise<LogoutScope | null> {

  const result = await sweetConfirm({
    // "로그아웃 하시겠습니까?"
    title: message("frontend.auth.logoutConfirmTitle"),
    // "로그아웃할 범위를 선택해주세요."
    text: message("frontend.auth.logoutScopeText"),
    // "현재 디바이스 로그아웃"
    confirmButtonText: message("frontend.auth.logoutCurrent"),
    // "전체 디바이스 로그아웃"
    denyButtonText: message("frontend.auth.logoutAll"),
    // "취소"
    closeButtonLabel: message("frontend.common.cancel"),
    showCancelButton: false,
    showCloseButton: true,
    showDenyButton: true,
    customClass: "sadari-swal-logout",
  });

  if (result.isConfirmed) {
    return "CURRENT";
  }

  if (result.isDenied) {
    return "ALL";
  }

  return null;
}

/**
 * 현재 브라우저에서 이미 허용된 FCM token을 사용자 팝업 없이 조회함
 *
 * @author SeungHyeon.Kang
 * @return 현재 브라우저 FCM token, 조회할 수 없으면 undefined
 */
async function getCurrentPushToken(): Promise<string | undefined> {

  if (!("Notification" in window) || Notification.permission !== "granted") {
    return undefined;
  }

  try {
    const response = await getPushConfigApi();
    // 현재 기기 로그아웃에서 푸시 토큰이 실제로 필요할 때만 Firebase SDK를 지연 로드함
    const { requestFirebaseToken } = await import("@/app/pwa/firebaseMessaging");
    return await requestFirebaseToken(response.data);
  } catch {
    // 푸시 token 조회 실패가 인증 세션 로그아웃을 막지 않게 함
    return undefined;
  }
}

/**
 * 선택한 범위의 서버 세션을 종료하고 같은 브라우저의 모든 탭을 로그인 화면 상태로 동기화함
 *
 * @author SeungHyeon.Kang
 * @param scope 현재 기기 또는 전체 기기 로그아웃 범위
 * @return 반환값이 없음
 */
export async function runLogout(scope: LogoutScope): Promise<void> {

  const pushToken = scope === "CURRENT" ? await getCurrentPushToken() : undefined;

  try {
    await logoutApi({ scope, pushToken });
  } finally {
    useAuthStore.getState().clearAuth();
    // 다음 로그인 계정에 이전 인증과 사용자 데이터가 노출되지 않도록 세션 캐시를 제거함
    for (const queryKey of sessionQueryKeys) {
      // 현재 세션에 속한 공통 Query Cache를 제거함
      queryClient.removeQueries({ queryKey });
    }
    publishAuthLogout();
  }
}
