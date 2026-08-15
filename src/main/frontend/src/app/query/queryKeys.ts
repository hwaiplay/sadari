export const queryKeys = {
  auth: ["auth"] as const,
  myProfile: ["user", "me"] as const,
  readingTimerSummary: ["readingTimer", "summary"] as const,
};

// 인증이 끝날 때 이전 계정 데이터와 함께 제거할 서버 상태 키 목록을 제공한다
export const sessionQueryKeys = [
  queryKeys.auth,
  queryKeys.myProfile,
  queryKeys.readingTimerSummary,
] as const;
