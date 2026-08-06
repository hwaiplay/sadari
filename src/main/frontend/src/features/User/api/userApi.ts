import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

export type UserProfile = {
  userStat?: "ACTIVE" | "WITHDRAWN" | "SUSPENDED" | "DELETE_PENDING";
  onbdYsno?: "Y" | "N";
  userNick?: string;
  porfPath?: string;
  bgimPath?: string;
  intrCntn?: string;
};

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
 * get My Profile 정보를 조회한다
 *
 * @author HanWon.Jang
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getMyProfileApi = async () => {

  const res = await api.get("/user/me");
  return assertResultDataSuccess(res.data);
};

/**
 * 마이페이지에 표시할 이번 달/올해 완료 독서 권수와 목표 달성 정보를 조회합니다.
 *
 * @author HanWon.Jang
 * @return 월간/연간 완료 독서 요약 API 응답
 */
export const getMonthlyReadingSummaryApi = async () => {

  const res = await api.get("/user/monthly-reading-summary");
  return assertResultDataSuccess(res.data);
};

/**
 * 마이페이지에서 설정한 이번 달/올해 독서 목표 권수를 저장합니다.
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
 * copy Previous Reading Goal 기능을 처리한다
 *
 * @author HanWon.Jang
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const copyPreviousReadingGoalApi = () => {

  return api.post("/user/reading-goal/previous").then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

/**
 * update My Profile 정보를 수정한다
 *
 * @author HanWon.Jang
 * @param params params 입력값
 * @return 반환값이 없다
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
 * 최초 로그인 사용자의 닉네임을 저장하고 웰컴 화면을 완료한다
 *
 * @author HanWon.Jang
 * @param params 사용자가 확정한 닉네임
 * @return 온보딩 완료 후 최신 사용자 프로필 응답
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const updateOnboardingApi = (params: UpdateOnboardingParams) => {

  // 닉네임 저장과 온보딩 완료를 같은 백엔드 트랜잭션으로 요청한다
  return api.put("/user/onboarding", params).then((res) => {

    // 공통 응답 코드가 성공인 경우에만 최신 프로필을 반환한다
    return assertResultDataSuccess(res.data);
  });
};

/**
 * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 대분류와 세부코드가 포함된 관심분야 목록
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const getUserInterestCatalogApi = async (): Promise<UserInterest[]> => {
  // 사용자 도메인의 활성 독서 관심분야 목록을 요청한다
  const res = await api.get("/user/interests/catalog");
  // 공통 응답 검증을 통과한 관심분야 목록을 반환한다
  return (assertResultDataSuccess(res.data).data as UserInterest[] | undefined) ?? [];
};

/**
 * 앨범에서 선택한 프로필 또는 배경 이미지를 사용자 전용 임시 저장소에 보관한다
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
  // 서버에서 파일 시그니처와 방향을 검증할 원본 이미지를 전달한다
  formData.append("imageFile", imageFile);
  // 임시 저장 경로와 미리보기 제한 크기를 결정할 이미지 유형을 전달한다
  formData.append("imageType", imageType);

  return api.post("/user/profile-image-drafts", formData).then((res) => {
    // 공통 성공 응답에서 로그인 사용자의 임시 이미지 정보만 반환한다
    return assertResultDataSuccess(res.data).data as ProfileImageDraft;
  });
};

/**
 * 앱 재시작 뒤에도 만료되지 않은 프로필 이미지 임시 선택본을 복원한다
 *
 * @author SeungHyeon.Kang
 * @return 같은 로그인 사용자의 복원 가능한 임시 이미지 목록
 * @throws API 요청 실패 시 발생
 */
export const getProfileImageDraftListApi = async (): Promise<ProfileImageDraft[]> => {
  // 공개 파일 URL 없이 인증 응답 본문으로 작은 서버 미리보기를 조회한다
  const res = await api.get("/user/profile-image-drafts");
  return (assertResultDataSuccess(res.data).data as ProfileImageDraft[] | undefined) ?? [];
};

/**
 * 프로필 편집을 취소한 이미지 유형의 임시 원본과 미리보기를 삭제한다
 *
 * @author SeungHyeon.Kang
 * @param imageType 삭제할 프로필 또는 배경 이미지 구분값
 * @return 삭제 처리 응답
 * @throws API 요청 실패 시 발생
 */
export const delProfileImageDraftApi = (imageType: ProfileImageType) => {
  // 쿼리 파라미터로 고정 유형만 전달해 사용자 전용 임시 파일을 삭제한다
  return api.delete("/user/profile-image-drafts", { params: { imageType } }).then((res) => {
    return assertResultDataSuccess(res.data);
  });
};

/**
 * 로그인 사용자가 현재 선택한 독서 관심분야를 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 저장된 관심분야 목록
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const getUserInterestListApi = async (): Promise<UserInterest[]> => {
  // 로그인 사용자의 관심분야 목록을 요청한다
  const res = await api.get("/user/interests");
  // 공통 응답 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(res.data).data as UserInterest[] | undefined) ?? [];
};

/**
 * 최초 로그인 사용자가 선택한 독서 관심분야를 전체 교체한다
 *
 * @author SeungHyeon.Kang
 * @param params 선택한 관심분야 목록
 * @return 관심분야 저장 응답
 * @throws API 요청 또는 업무 검증 실패 시 발생
 */
export const updateUserInterestsApi = (params: UpdateUserInterestsParams) => {
  // 선택하지 않은 경우에도 빈 목록으로 기존 관심분야를 정리할 수 있도록 요청한다
  return api.put("/user/interests", params).then((res) => {
    // 공통 성공 코드가 확인된 저장 응답을 반환한다
    return assertResultDataSuccess(res.data);
  });
};
