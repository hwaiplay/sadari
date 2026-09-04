import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type ImageReaction = {
  tagtType: "PROFILE_IMAGE" | "BACKGROUND_IMAGE";
  tagtNumb: number;
  likeCnt: number;
  likeYsno: "Y" | "N";
  replCnt: number;
};

export type UserProfile = {
  userStat?: "ACTIVE" | "WITHDRAWN" | "SUSPENDED" | "DELETE_PENDING";
  userStatName?: string;
  onbdYsno?: "Y" | "N";
  userNick?: string;
  porfPath?: string;
  bgimPath?: string;
  bgimDisplayPath?: string;
  intrCntn?: string;
  profileImageReaction?: ImageReaction | null;
  backgroundImageReaction?: ImageReaction | null;
};

export type UserSetting = {
  readingStatisticsYsno: "Y" | "N";
  readingGoalYsno: "Y" | "N";
  imageFeedYsno: "Y" | "N";
  reportPublicDefaultYsno: "Y" | "N";
  likeAlimYsno: "Y" | "N";
  replyAlimYsno: "Y" | "N";
  followAlimYsno: "Y" | "N";
  clubAlimYsno: "Y" | "N";
  chatAlimYsno: "Y" | "N";
  reportDueAlimYsno: "Y" | "N";
  reportLikeDefaultYsno: "Y" | "N";
  reportReplyDefaultYsno: "Y" | "N";
};

export type UserNotificationSettingParams = Pick<
  UserSetting,
  | "likeAlimYsno"
  | "replyAlimYsno"
  | "followAlimYsno"
  | "clubAlimYsno"
  | "chatAlimYsno"
  | "reportDueAlimYsno"
  | "reportLikeDefaultYsno"
  | "reportReplyDefaultYsno"
>;

export type UserPrivacySettingParams = Pick<
  UserSetting,
  | "readingStatisticsYsno"
  | "readingGoalYsno"
  | "imageFeedYsno"
  | "reportPublicDefaultYsno"
>;

export type ReadingSummaryReport = {
  reptNumb: number;
  bookTitl?: string;
  bookAthr?: string;
  bookCvim?: string;
  bookIsbn?: string;
  reptStdt?: string;
  reptEndt?: string;
  reptGrde?: string;
  pubcYsno?: "Y" | "N";
};

export type MonthlyReadingSummary = {
  goalPublicYsno?: "Y" | "N";
  weekCode?: string;
  currentWeekCount: number;
  previousWeekCount: number;
  weekCountDiff: number;
  monthCode?: string;
  currentMonthCount: number;
  previousMonthCount: number;
  countDiff: number;
  yearCode?: string;
  currentYearCount: number;
  previousYearCount: number;
  yearCountDiff: number;
  weekGoalCnt?: number | null;
  monthGoalCnt?: number | null;
  yearGoalCnt?: number | null;
  previousWeekGoalCnt?: number | null;
  previousMonthGoalCnt?: number | null;
  previousYearGoalCnt?: number | null;
  weekGoalRate: number;
  monthGoalRate: number;
  yearGoalRate: number;
  weekGoalSet: boolean;
  monthGoalSet: boolean;
  yearGoalSet: boolean;
  weekGoalRemainUpdateCnt: number;
  monthGoalRemainUpdateCnt: number;
  yearGoalRemainUpdateCnt: number;
  weekGoalEditableRemainDays: number;
  monthGoalEditableRemainDays: number;
  yearGoalEditableRemainDays: number;
  weekGoalUpdateLocked: boolean;
  monthGoalUpdateLocked: boolean;
  yearGoalUpdateLocked: boolean;
  weekGoalAchvCnt: number;
  monthGoalAchvCnt: number;
  yearGoalAchvCnt: number;
  totalGoalAchvCnt: number;
  totalReadBookCnt: number;
  followingCnt: number;
  followerCnt: number;
  receivedLikeCnt: number;
  currentReadingReports?: ReadingSummaryReport[];
  currentWeekReports?: ReadingSummaryReport[];
  currentMonthReports?: ReadingSummaryReport[];
  currentYearReports?: ReadingSummaryReport[];
};

export type ReadingGoalParams = {
  weekGoalCnt: number;
  monthGoalCnt: number;
  yearGoalCnt: number;
};

export type UpdateUserProfileParams = {
  userNick: string;
  intrCntn: string;
  profileImageDraftToken?: string | null;
  backgroundImageDraftToken?: string | null;
};

export type ReadingTimeDaily = {
  readDate: string;
  readSecs: number;
};

export type ReadingStatusCount = {
  reptStat: "READ" | "DONE" | "STOP";
  reptCnt: number;
};

export type ReadingStreak = {
  currentStreakDays: number;
  longestStreakDays: number;
};

export type ReadingBookTime = {
  reptNumb?: number | null;
  bookNumb: number;
  bookTitl?: string;
  bookAthr?: string;
  bookCvim?: string;
  readSecs: number;
};

export type ReadingRatingCount = {
  reptGrde: number;
  reptCnt: number;
};

export type ReadingYearComparison = {
  currentYear: number;
  previousYear: number;
  currentReadSecs: number;
  previousReadSecs: number;
  currentReadDays: number;
  previousReadDays: number;
  currentDoneBooks: number;
  previousDoneBooks: number;
};

export type ReadingStatistics = {
  heatmapStart: string;
  heatmapEnd: string;
  heatmapList: ReadingTimeDaily[];
  statusList: ReadingStatusCount[];
  streak: ReadingStreak;
  topBookList: ReadingBookTime[];
  ratingList: ReadingRatingCount[];
  yearComparison: ReadingYearComparison;
  selectedYear: number;
  availableYears: number[];
  publicYsno: "Y" | "N";
};

export type ReadingHeatmap = Pick<
  ReadingStatistics,
  "heatmapStart" | "heatmapEnd" | "heatmapList" | "selectedYear" | "availableYears"
>;

export type ReadingStatisticsSettingParams = {
  publicYsno: "Y" | "N";
};

export type ProfileImageType = "PROFILE" | "BACKGROUND";

export type ProfileImageDraft = {
  imageType: ProfileImageType;
  draftToken: string;
  previewDataUrl: string;
  expiresAt: string;
};

export type UpdateOnboardingParams = {
  userNick: string;
};

export type UserInterest = {
  intrCnam: string;
  intrCode: string;
  intrName: string;
  cgrpOrdr?: number;
  codeOrdr?: number;
};

export type UpdateUserInterestsParams = {
  interestList: Array<Pick<UserInterest, "intrCode">>;
};

/**
 * get My Profile 정보를 조회함
 *
 * @author HanWon.Jang
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getMyProfileApi = async (): Promise<ResultData<UserProfile> & { data: UserProfile }> => {

  const res = await api.get<ResultData<UserProfile> & { data: UserProfile }>("/user/me");
  // 검증된 로그인 사용자 프로필 응답을 반환함
  return assertResultDataSuccess(res.data);
};

/** 로그인 사용자의 알림과 공개 범위 설정을 조회함 */
export const getUserSettingApi = async (): Promise<UserSetting> => {
  const res = await api.get("/user/settings");
  return assertResultDataSuccess(res.data).data as UserSetting;
};

/** 선택형 알림 범주와 신규 독후감 알림 기본값을 저장함 */
export const uptUserAlimSettingApi = async (
  params: UserNotificationSettingParams,
): Promise<UserSetting> => {
  const res = await api.put("/user/settings/notifications", params);
  return assertResultDataSuccess(res.data).data as UserSetting;
};

/** 공개 범위와 신규 독후감 공개 기본값을 저장함 */
export const uptUserPrivacyApi = async (
  params: UserPrivacySettingParams,
): Promise<UserSetting> => {
  const res = await api.put("/user/settings/privacy", params);
  return assertResultDataSuccess(res.data).data as UserSetting;
};

/**
 * 마이페이지에 표시할 이번 달/올해 완료 독서 권수와 목표 달성 정보를 조회함
 *
 * @author HanWon.Jang
 * @return 월간/연간 완료 독서 요약 API 응답
 */
export const getMonthlyReadingApi = async () => {

  const res = await api.get("/user/monthly-reading-summary");
  return assertResultDataSuccess(res.data);
};

/**
 * 타이머 화면에 표시할 선택 연도의 독서 시간 잔디만 조회함
 *
 * @author SeungHyeon.Kang
 * @param readYear 조회할 연도, 없으면 현재 연도
 * @param signal 화면 이탈 시 조회를 취소할 요청 신호
 * @return 조회 가능한 연도와 날짜별 독서 시간 잔디
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getReadingHeatmapApi = async (
  readYear?: number,
  signal?: AbortSignal,
): Promise<ReadingHeatmap> => {

  // 전체 독서 통계 없이 선택 연도의 잔디 데이터만 요청함
  const res = await api.get("/user/reading-heatmap", {
    params: readYear === undefined ? undefined : { readYear },
    signal,
  });
  // 공통 응답 코드가 검증된 독서 잔디 데이터만 반환함
  return assertResultDataSuccess(res.data).data as ReadingHeatmap;
};

/**
 * 스크롤로 마이페이지 통계 영역에 진입한 사용자의 독서 시간과 상태 분포를 조회함
 *
 * @author SeungHyeon.Kang
 * @param readYear 조회할 연도, 없으면 현재 연도
 * @param signal 화면 이탈 시 조회를 취소할 요청 신호
 * @return 선택 연도의 잔디와 독서 상태 통계 API 응답
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getReadingStatsApi = async (
  readYear?: number,
  signal?: AbortSignal,
): Promise<ReadingStatistics> => {

  // 본인 전용 독서 통계를 화면 이탈 시 취소할 수 있는 조회 요청으로 전달함
  const res = await api.get("/user/reading-statistics", {
    params: readYear === undefined ? undefined : { readYear },
    signal,
  });
  // 공통 응답 코드가 검증된 독서 통계 데이터만 반환함
  return assertResultDataSuccess(res.data).data as ReadingStatistics;
};

/**
 * 마이페이지에서 선택한 독서 통계 공개 여부를 저장함
 *
 * @author SeungHyeon.Kang
 * @param params 다른 사용자 공개 여부
 * @return 저장된 공개 여부 코드
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const uptReadingStatsSettingApi = async (
  params: ReadingStatisticsSettingParams,
): Promise<"Y" | "N"> => {
  // 로그인 회원의 범용 설정에 독서 통계 공개 여부를 저장함
  const res = await api.put("/user/reading-statistics/settings", params);
  // 공통 성공 응답 검증을 통과한 공개 여부 코드를 반환함
  return assertResultDataSuccess(res.data).data as "Y" | "N";
};

/**
 * 마이페이지에서 설정한 이번 달/올해 독서 목표 권수를 저장함
 *
 * @author HanWon.Jang
 * @param params 월간/연간 목표 권수
 * @return 저장 후 갱신된 월간/연간 완료 독서 요약 API 응답
 */
export const updateReadingGoalApi = (params: ReadingGoalParams) => {

  return api.put("/user/reading-goal", params).then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

/**
 * copy Previous Reading Goal 기능을 처리함
 *
 * @author HanWon.Jang
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const copyPrevReadingGoalApi = () => {

  return api.post("/user/reading-goal/previous").then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

/**
 * update My Profile 정보를 수정함
 *
 * @author HanWon.Jang
 * @param params params 입력값
 * @return 반환값이 없음
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const updateMyProfileApi = (params: UpdateUserProfileParams) => {

  const formData = new FormData();
  formData.append("userNick", params.userNick);
  formData.append("intrCntn", params.intrCntn);

  if (params.profileImageDraftToken) {
    formData.append("profileImageDraftToken", params.profileImageDraftToken);
  }

  if (params.backgroundImageDraftToken) {
    formData.append("backgroundImageDraftToken", params.backgroundImageDraftToken);
  }

  return api.put("/user/uptProfile", formData).then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

/**
 * 최초 로그인 사용자의 닉네임을 저장하고 웰컴 화면을 완료함
 *
 * @author HanWon.Jang
 * @param params 사용자가 확정한 닉네임
 * @return 온보딩 완료 후 최신 사용자 프로필 응답
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const updateOnboardingApi = (params: UpdateOnboardingParams) => {

  // 닉네임 저장과 온보딩 완료를 같은 백엔드 트랜잭션으로 요청함
  return api.put("/user/onboarding", params).then((res) => {

    // 공통 응답 코드가 성공인 경우에만 최신 프로필을 반환함
    return assertResultDataSuccess(res.data);
  });
};

/**
 * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회함
 *
 * @author SeungHyeon.Kang
 * @return 대분류와 세부코드가 포함된 관심분야 목록
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const getUserInterestCatalogApi = async (): Promise<UserInterest[]> => {
  // 사용자 도메인의 활성 독서 관심분야 목록을 요청함
  const res = await api.get("/user/interests/catalog");
  // 공통 응답 검증을 통과한 관심분야 목록을 반환함
  return (assertResultDataSuccess(res.data).data as UserInterest[] | undefined) ?? [];
};

/**
 * 앨범에서 선택한 프로필 또는 배경 이미지를 사용자 전용 임시 저장소에 보관함
 *
 * @author SeungHyeon.Kang
 * @param imageFile 사용자가 선택한 원본 이미지
 * @param imageType 프로필 또는 배경 이미지 구분값
 * @return 서버가 방향 보정과 축소를 완료한 미리보기와 임시 식별값
 * @throws API 요청 또는 이미지 검증 실패 시 발생
 */
export const setProfileImageDraftApi = (
  imageFile: File,
  imageType: ProfileImageType,
): Promise<ProfileImageDraft> => {
  const formData = new FormData();
  // 서버에서 파일 시그니처와 방향을 검증할 원본 이미지를 전달함
  formData.append("imageFile", imageFile);
  // 임시 저장 경로와 미리보기 제한 크기를 결정할 이미지 유형을 전달함
  formData.append("imageType", imageType);

  return api.post("/user/profile-image-drafts", formData).then((res) => {
    // 공통 성공 응답에서 로그인 사용자의 임시 이미지 정보만 반환함
    return assertResultDataSuccess(res.data).data as ProfileImageDraft;
  });
};

/**
 * 앱 재시작 뒤에도 만료되지 않은 프로필 이미지 임시 선택본을 복원함
 *
 * @author SeungHyeon.Kang
 * @return 같은 로그인 사용자의 복원 가능한 임시 이미지 목록
 * @throws API 요청 실패 시 발생
 */
export const getProfileDraftListApi = async (): Promise<ProfileImageDraft[]> => {
  // 공개 파일 URL 없이 인증 응답 본문으로 작은 서버 미리보기를 조회함
  const res = await api.get("/user/profile-image-drafts");
  return (assertResultDataSuccess(res.data).data as ProfileImageDraft[] | undefined) ?? [];
};

/**
 * 프로필 편집을 취소한 이미지 유형의 임시 원본과 미리보기를 삭제함
 *
 * @author SeungHyeon.Kang
 * @param imageType 삭제할 프로필 또는 배경 이미지 구분값
 * @return 삭제 처리 응답
 * @throws API 요청 실패 시 발생
 */
export const delProfileImageDraftApi = (imageType: ProfileImageType) => {
  // 쿼리 파라미터로 고정 유형만 전달해 사용자 전용 임시 파일을 삭제함
  return api.delete("/user/profile-image-drafts", { params: { imageType } }).then((res) => {
    return assertResultDataSuccess(res.data);
  });
};

/**
 * 로그인 사용자가 현재 선택한 독서 관심분야를 조회함
 *
 * @author SeungHyeon.Kang
 * @return 현재 저장된 관심분야 목록
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const getUserInterestListApi = async (): Promise<UserInterest[]> => {
  // 로그인 사용자의 관심분야 목록을 요청함
  const res = await api.get("/user/interests");
  // 공통 응답 검증을 통과한 목록을 반환함
  return (assertResultDataSuccess(res.data).data as UserInterest[] | undefined) ?? [];
};

/**
 * 최초 로그인 사용자가 선택한 독서 관심분야를 전체 교체함
 *
 * @author SeungHyeon.Kang
 * @param params 선택한 관심분야 목록
 * @return 관심분야 저장 응답
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const updateUserInterestsApi = (params: UpdateUserInterestsParams) => {
  // 선택하지 않은 경우에도 빈 목록으로 기존 관심분야를 정리할 수 있도록 요청함
  return api.put("/user/interests", params).then((res) => {
    // 공통 성공 코드가 확인된 저장 응답을 반환함
    return assertResultDataSuccess(res.data);
  });
};
