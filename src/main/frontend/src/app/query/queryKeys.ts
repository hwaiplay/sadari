export const queryKeys = {
  auth: ["auth"] as const,
  myProfile: ["user", "me"] as const,
  readingTimerSummary: ["readingTimer", "summary"] as const,
  readingTimerBookTimes: ["readingTimer", "bookTimes"] as const,
};

// 인증이 끝날 때 이전 계정 데이터와 함께 제거할 서버 상태 키 목록을 제공함
export const sessionQueryKeys = [
  queryKeys.auth,
  queryKeys.myProfile,
  queryKeys.readingTimerSummary,
  queryKeys.readingTimerBookTimes,
] as const;
