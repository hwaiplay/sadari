/**
 * 로그인 사용자가 상대를 팔로우 중인 버튼 상태인지 판정함
 *
 * @author SeungHyeon.Kang
 * @param followStatName 서버에서 내려준 팔로우 버튼명
 * @return 로그인 사용자의 팔로우 관계 존재 여부
 */
export const isFollowedByMe = (followStatName?: string): boolean => {
  // 팔로잉과 친구 상태에서만 로그인 사용자가 만든 팔로우 관계를 삭제할 수 있음
  return followStatName === "팔로잉" || followStatName === "친구";
};
