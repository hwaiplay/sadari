import { queryKeys } from "@/app/query/queryKeys";
import { getMyProfileApi, type UserProfile } from "@/features/User/api/userApi";
import { queryOptions, useQuery } from "@tanstack/react-query";

/**
 * 헤더와 내비게이션 및 프로필 화면이 공유할 내 프로필 조회 옵션을 생성함
 *
 * @author SeungHyeon.Kang
 * @return 내 프로필 React Query 옵션
 */
export function getMyProfileOptions() {
  // 같은 사용자 프로필 요청이 하나의 Query Key와 캐시를 사용하도록 옵션을 반환함
  return queryOptions({
    queryKey: queryKeys.myProfile,
    /**
     * 로그인 사용자의 최신 프로필을 조회함
     *
     * @author SeungHyeon.Kang
     * @return 로그인 사용자 프로필
     * @throws 프로필 API 요청 또는 공통 응답 검증 실패 시 발생
     */
    queryFn: async (): Promise<UserProfile> => {
      // 공통 응답에서 검증된 로그인 사용자 프로필만 반환함
      return (await getMyProfileApi()).data;
    },
    staleTime: 60_000,
  });
}

/**
 * 로그인 사용자 프로필의 공유 서버 상태를 제공함
 *
 * @author SeungHyeon.Kang
 * @return 내 프로필 조회 데이터와 요청 상태
 */
export function useMyProfileQuery() {
  // 공통 프로필 Query Key로 중복 요청을 합친 조회 상태를 반환함
  return useQuery(getMyProfileOptions());
}
