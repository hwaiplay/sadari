/**
 * src/main/frontend/src/features/User/lib/profileEvents.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import type { UserProfile } from "@/features/User/api/userApi";
import { queryClient } from "@/app/query/queryClient";
import { queryKeys } from "@/app/query/queryKeys";

/**
 * notify User Profile Updated 사용자 동작을 처리한다
 *
 * @author HanWon.Jang
 * @param profile profile 입력값
 * @return 반환값이 없다
 */
export function notifyUserProfileUpdated(profile: UserProfile) {
  // 프로필 저장 결과를 공통 Query 캐시에 반영해 모든 화면을 함께 갱신한다
  queryClient.setQueryData(queryKeys.myProfile, profile);
}
