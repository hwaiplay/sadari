/**
 * src/main/frontend/src/features/Auth/hooks/useCheckAuth.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { useAuthQuery } from "./useAuthQuery";
const DELETE_PENDING_STATUS = "DELETE_PENDING";
const SUSPENDED_STATUS = "SUSPENDED";
const ONBOARDING_COMPLETED = "Y";

/**
 * use Check Auth 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const useCheckAuth = () => {

  // 인증 조회와 한 번의 Token 복구 상태를 공통 Query에서 가져온다
  const { data, isLoading, isError } = useAuthQuery();

  // 최초 인증 조회가 끝날 때까지 로그인 판단을 보류한다
  if (isLoading) {
    // 라우트에서 공통 로그인 조회 화면을 표시할 상태를 반환한다
    return {
      isLoading: true,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  // 인증 조회 또는 한 번의 Token 복구가 실패하면 비로그인 상태로 확정한다
  if (isError) {
    // 추가 자동 재시도 없이 로그인 화면에서 사용할 상태를 반환한다
    return {
      isLoading: false,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  // 검증된 인증 응답이 있으면 사용자 상태별 라우팅 정보를 구성한다
  if (data) {
    const code = data.code;

    // 공통 성공 응답일 때만 로그인 사용자 상태를 제공한다
    if (code === 200) {
      // 인증 응답의 회원 상태로 영구 삭제 대기 전용 화면 여부를 판단합니다
      const authData = data.data as { userStat?: string; onbdYsno?: string } | undefined;
      // 인증 성공 여부와 영구 삭제 대기 및 온보딩 진입 여부를 함께 반환합니다
      return {
        isLoading: false,
        isAuthenticated: true,
        isDeletePending: authData?.userStat === DELETE_PENDING_STATUS,
        isSuspended: authData?.userStat === SUSPENDED_STATUS,
        isOnboardingRequired: authData?.onbdYsno !== ONBOARDING_COMPLETED,
      };
    }

    // 성공 코드가 아닌 응답은 보호 화면에 진입하지 못하도록 반환한다
    return {
      isLoading: false,
      isAuthenticated: false,
      isDeletePending: false,
      isSuspended: false,
      isOnboardingRequired: false,
    };
  }

  // 조회 결과가 없는 초기 예외 상태도 비로그인 상태로 안전하게 처리한다
  return {
    isLoading: false,
    isAuthenticated: false,
    isDeletePending: false,
    isSuspended: false,
    isOnboardingRequired: false,
  };
};
