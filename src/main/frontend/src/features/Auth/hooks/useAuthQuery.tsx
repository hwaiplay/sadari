import { useQuery } from "@tanstack/react-query";
import { ResultDataError } from "@/app/api/resultData";
import { checkAuthApi, refreshTokenApi } from "../api/authApi";
import { getUserSettingApi } from "@/features/User/api/userApi";
import { setMessageLocale } from "@/app/messages/message";

const REFRESHABLE_AUTH_CODES = new Set([1001, 1002, 1003]);

/** 인증 완료 뒤 계정 언어를 조회하여 현재 브라우저 메시지 언어와 동기화함 */
const getAuthenticatedState = async () => {
  const authState = await checkAuthApi();
  const setting = await getUserSettingApi();
  setMessageLocale(setting.englishYsno);
  return authState;
};

/**
 * 인증 실패가 Refresh Token으로 한 번 복구할 수 있는 상태인지 판정함
 *
 * @author HanWon.Jang
 * @param error 인증 상태 조회 실패 원인
 * @return Access Token 재발급 대상 여부
 */
const isRefreshableError = (error: unknown): boolean => {

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
    await refreshTokenApi();
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
  });
};
