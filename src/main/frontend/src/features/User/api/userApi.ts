import api from "@/app/api/axios";

export type UserProfile = {
  userNick?: string;
  porfPath?: string;
  bgimPath?: string;
  intrCntn?: string;
};

export type ReadingSummaryReport = {
  reportNumb: number;
  bookTitl?: string;
  bookAthr?: string;
  bookCvim?: string;
  reportEndt?: string;
  reportGrde?: string;
};

export type MonthlyReadingSummary = {
  monthCode?: string;
  currentMonthCount: number;
  previousMonthCount: number;
  countDiff: number;
  yearCode?: string;
  currentYearCount: number;
  previousYearCount: number;
  yearCountDiff: number;
  currentMonthReports?: ReadingSummaryReport[];
  currentYearReports?: ReadingSummaryReport[];
};

export type UpdateUserProfileParams = {
  userNick: string;
  intrCntn: string;
  profileImage?: File | null;
  backgroundImage?: File | null;
};

/**
 * 로그인 사용자의 프로필 정보를 조회한다.
 * @Author Hanwon.Jang
 * @return 프로필 조회 API 응답
 */
export const getMyProfileApi = () => {
  return api.get("/user/me");
};

/**
 * 마이페이지에 표시할 이번 달 완료 독서 권수와 지난달 대비 변화량을 조회한다.
 * @Author Hanwon.Jang
 * @return 월간 완료 독서 요약 API 응답
 */
export const getMonthlyReadingSummaryApi = () => {
  return api.get("/user/monthly-reading-summary");
};

/**
 * 로그인 사용자의 프로필 사진, 닉네임, 한줄 소개를 수정한다.
 * @Author Hanwon.Jang
 * @param params 수정할 프로필 정보
 * @return 프로필 수정 API 응답
 */
export const updateMyProfileApi = (params: UpdateUserProfileParams) => {
  const formData = new FormData();
  formData.append("userNick", params.userNick);
  formData.append("intrCntn", params.intrCntn);

  if (params.profileImage) {
    formData.append("profileImage", params.profileImage);
  }

  if (params.backgroundImage) {
    formData.append("backgroundImage", params.backgroundImage);
  }

  return api.put("/user/me", formData);
};
