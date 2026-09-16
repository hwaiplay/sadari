import { useQuery } from "@tanstack/react-query";
import { ResultDataError } from "@/app/api/resultData";
import { checkAuthApi, refreshTokenApi } from "../api/authApi";
import { getUserSettingApi } from "@/features/User/api/userApi";
import { setMessageLocale } from "@/app/messages/message";

const REFRESHABLE_AUTH_CODES = new Set([1001, 1002, 1003]);

/**
 * 인증된 활성 회원만 사용자 설정을 조회하여 계정 언어 동기화
 *
 * @author HanWon.Jang
 * @return 제한 상태를 포함한 서버 인증 결과
 * @throws 인증 또는 활성 회원 설정 조회 실패 시 발생
 */
const getAuthenticatedState = async () => {
  // 로그인 여부와 계정 접근 제한을 서버 인증 응답으로 확인
  const authState = await checkAuthApi();
  // 제한 상태 회원의 설정 API 차단을 로그인 실패로 오인하지 않도록 활성 회원만 조회
  if (authState.data?.userStat === "ACTIVE") {
    // 일반 서비스 접근이 허용된 회원의 언어 설정 조회
    const setting = await getUserSettingApi();
    // 조회한 계정 언어를 브라우저 표시 언어에 반영
    setMessageLocale(setting.englishYsno);
  }

  // 탈퇴 대기와 정지 상태도 로그인 성공으로 유지할 서버 인증 결과 반환
  return authState;
};

/**
 * 인증 실패가 Refresh Token으로 한 번 복구할 수 있는 상태인지 판정함
 *
 * @author HanWon.Jang
 * @param error 인증 상태 조회 실패 원인
 * @return Access Token 재발급 대상 여부
 */
const isRefreshableError = (error: unknown): error is ResultDataError => {

  // 공통 인증 실패 응답만 Access Token 재발급 대상으로 처리함
  if (!(error instanceof ResultDataError)) {
    // 네트워크와 일반 업무 오류는 인증 재발급 없이 호출부로 전달함
    return false;
  }

  // 서버가 정의한 Access Token 복구 가능 코드인지 반환함
  return REFRESHABLE_AUTH_CODES.has(Number(error.result.code));
};

/**
 * 현재 인증 상태를 조회하고 Access Token 문제일 때 한 번만 재발급 후 다시 확인함
 *
 * @author HanWon.Jang
 * @return 현재 브라우저의 인증 상태 응답
 * @throws 인증 조회 또는 Access Token 재발급이 실패할 때 발생
 */
const getAuthState = async () => {

  // 최초 인증 조회와 한 번의 복구 시도를 하나의 Query 실행 경계로 묶음
  try {
    // 현재 Access Token과 사용자 상태를 조회함
    return await getAuthenticatedState();
  }

  // 인증 실패 종류에 따라 한 번의 Access Token 복구 여부를 결정함
  catch (error) {
    // 복구 대상이 아니면 자동 재시도 없이 원래 오류를 전달함
    if (!isRefreshableError(error)) {
      // 네트워크 또는 일반 업무 오류를 기존 Query 오류 경로로 전달함
      throw error;
    }

    // 같은 Query 실행에서 Access Token을 한 번만 재발급함
    try {
      await refreshTokenApi();
    }

    // 만료되거나 제거된 Refresh Token은 정상적인 로그아웃 상태로 확정함
    catch (refreshError) {
      // 인증 실패를 Query 오류로 남기면 새 인증 화면이 붙을 때 같은 복구 요청이 다시 시작될 수 있음
      if (isRefreshableError(refreshError)) {
        // 서버가 쿠키를 만료시킨 인증 실패 응답을 그대로 반환해 반복 복구를 종료함
        return refreshError.result;
      }

      // 네트워크와 서버 장애는 로그아웃으로 오인하지 않고 기존 오류 화면에서 처리함
      throw refreshError;
    }

    // 재발급 뒤 인증 상태를 한 번 확인하고 실패 시 추가 반복 없이 종료함
    return await getAuthenticatedState();
  }
};

/**
 * 현재 브라우저의 로그인 상태를 React Query로 조회함
 *
 * @author HanWon.Jang
 * @return 로그인 상태 조회 Query 객체
 */
export const useAuthQuery = () => {

  // 여러 인증 화면이 같은 Query와 단일 복구 요청을 공유하도록 반환함
  return useQuery({
    queryKey: ["auth"],
    queryFn: getAuthState,
    retry: false,
    // 실패한 Query에 새 Route 구독자가 붙어도 인증 복구를 다시 시작하지 않음
    retryOnMount: false,
    // PublicRoute와 OAuth 화면이 차례로 붙을 때 같은 인증 상태를 다시 조회하지 않음
    refetchOnMount: false,
  });
};
