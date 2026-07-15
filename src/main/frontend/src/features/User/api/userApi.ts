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

export const getMyProfileApi = () => {
  return api.get("/user/me");
};

/**
 * 留덉씠?섏씠吏???쒖떆???대쾲 ???꾨즺 ?낆꽌 沅뚯닔? 吏?쒕떖 ?鍮?蹂?붾웾??議고쉶?쒕떎.
 * @author Hanwon.Jang
 * @return ?붽컙 ?꾨즺 ?낆꽌 ?붿빟 API ?묐떟
 */
export const getMonthlyReadingSummaryApi = () => {
  return api.get("/user/monthly-reading-summary");
};

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