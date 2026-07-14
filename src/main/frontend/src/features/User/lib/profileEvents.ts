import type { UserProfile } from "@/features/User/api/userApi";

export const USER_PROFILE_UPDATED_EVENT = "sadari:user-profile-updated";

type UserProfileUpdatedEvent = CustomEvent<UserProfile>;

/**
 * 프로필 수정 완료 후 현재 화면에 떠 있는 다른 컴포넌트들이 최신 프로필을 즉시 반영하도록 이벤트를 발행한다.
 * Navigation처럼 페이지 이동 없이 유지되는 컴포넌트는 이 이벤트를 받아 별도 새로고침 없이 상태를 갱신한다.
 * @Author Hanwon.Jang
 * @param profile 서버 저장 후 반환된 최신 사용자 프로필 정보
 * @return
 */
export function notifyUserProfileUpdated(profile: UserProfile) {
  window.dispatchEvent(
    new CustomEvent<UserProfile>(USER_PROFILE_UPDATED_EVENT, {
      detail: profile,
    }),
  );
}

/**
 * 브라우저 이벤트가 프로필 갱신 이벤트인지 확인해 detail 타입을 안전하게 좁힌다.
 * @Author Hanwon.Jang
 * @param event 브라우저에서 전달된 이벤트 객체
 * @return 프로필 갱신 이벤트 여부
 */
export function isUserProfileUpdatedEvent(
  event: Event,
): event is UserProfileUpdatedEvent {
  return event.type === USER_PROFILE_UPDATED_EVENT && "detail" in event;
}
