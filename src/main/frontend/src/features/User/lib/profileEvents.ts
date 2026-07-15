/**
 * src/main/frontend/src/features/User/lib/profileEvents.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import type { UserProfile } from "@/features/User/api/userApi";

export const USER_PROFILE_UPDATED_EVENT = "sadari:user-profile-updated";

type UserProfileUpdatedEvent = CustomEvent<UserProfile>;

export function notifyUserProfileUpdated(profile: UserProfile) {
  window.dispatchEvent(
    new CustomEvent<UserProfile>(USER_PROFILE_UPDATED_EVENT, {
      detail: profile,
    }),
  );
}

export function isUserProfileUpdatedEvent(
  event: Event,
): event is UserProfileUpdatedEvent {
  return event.type === USER_PROFILE_UPDATED_EVENT && "detail" in event;
}