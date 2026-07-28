/**
 * src/main/frontend/src/features/User/lib/profileEvents.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import type { UserProfile } from "@/features/User/api/userApi";

export const USER_PROFILE_UPDATED_EVENT = "sadari:user-profile-updated";

type UserProfileUpdatedEvent = CustomEvent<UserProfile>;

/**
 * notify User Profile Updated 사용자 동작을 처리한다
 *
 * @author HanWon.Jang
 * @param profile profile 입력값
 * @return 반환값이 없다
 */
export function notifyUserProfileUpdated(profile: UserProfile) {

  window.dispatchEvent(
    new CustomEvent<UserProfile>(USER_PROFILE_UPDATED_EVENT, {
      detail: profile,
    }),
  );
}

/**
 * is User Profile Updated Event 여부를 판정한다
 *
 * @author HanWon.Jang
 * @param event event 입력값
 * @return 판정 결과
 */
export function isUserProfileUpdatedEvent(
  event: Event,
): event is UserProfileUpdatedEvent {

  return event.type === USER_PROFILE_UPDATED_EVENT && "detail" in event;
}