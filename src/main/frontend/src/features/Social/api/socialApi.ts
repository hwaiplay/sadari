import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import type {
  MonthlyReadingSummary,
  UserProfile,
} from "@/features/User/api/userApi";

/**
 * 다른 사용자의 공개 프로필 정보를 조회합니다.
 * 공개 독후감 작성자 프로필 화면에서 마이페이지와 같은 프로필 영역을 구성할 때 사용합니다.
 *
 * @author Hanwon.Jang
 * @param userNumb 조회할 사용자 번호
 * @return 공개 프로필 조회 API 응답
 */
export const getSocialProfileApi = async (userNumb: number) => {
  const res = await api.get<{ data: UserProfile }>(`/social/profile/${userNumb}`);
  return assertResultDataSuccess(res.data);
};

/**
 * 다른 사용자의 독서 활동 요약 정보를 조회합니다.
 * 주간, 월간, 연간 완료 독후감과 목표 달성 현황을 마이페이지와 동일한 데이터 구조로 받습니다.
 *
 * @author Hanwon.Jang
 * @param userNumb 조회할 사용자 번호
 * @return 독서 활동 요약 조회 API 응답
 */
export const getSocialReadingSummaryApi = async (userNumb: number) => {
  const res = await api.get<{ data: MonthlyReadingSummary }>(
    `/social/profile/${userNumb}/reading-summary`,
  );
  return assertResultDataSuccess(res.data);
};
