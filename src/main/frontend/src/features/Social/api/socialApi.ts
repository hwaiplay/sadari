import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import type {
  MonthlyReadingSummary,
  UserProfile,
} from "@/features/User/api/userApi";

export type FollowStatus = {
  followStatName?: string;
};

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

/**
 * 로그인 사용자와 다른 사용자 사이의 팔로우 버튼명을 조회합니다.
 * 서버는 Oracle 함수 FN_GET_FOLW_STAT 결과를 반환하므로 화면은 별도 관계 계산 없이 버튼명만 표시합니다.
 *
 * @author Hanwon.Jang
 * @param userNumb 조회할 상대 사용자 번호
 * @return 팔로우 버튼 상태 조회 API 응답
 */
export const getSocialFollowStatusApi = async (userNumb: number) => {
  const res = await api.get<{ data: FollowStatus }>(
    `/social/profile/${userNumb}/follow-status`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * 로그인 사용자가 다른 사용자를 팔로우하도록 요청합니다.
 * 저장 후 서버에서 갱신된 버튼명을 반환받아 화면 상태를 즉시 맞춥니다.
 *
 * @author Hanwon.Jang
 * @param userNumb 팔로우할 상대 사용자 번호
 * @return 팔로우 저장 후 상태 조회 API 응답
 */
export const setSocialFollowApi = async (userNumb: number) => {
  const res = await api.post<{ data: FollowStatus }>(
    `/social/profile/${userNumb}/follow`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * 로그인 사용자가 다른 사용자를 팔로우 중인 관계를 삭제합니다.
 * 삭제 후 서버에서 갱신된 버튼명을 반환받아 맞팔로우 또는 팔로우 상태로 갱신합니다.
 *
 * @author Hanwon.Jang
 * @param userNumb 언팔로우할 상대 사용자 번호
 * @return 팔로우 삭제 후 상태 조회 API 응답
 */
export const delSocialFollowApi = async (userNumb: number) => {
  const res = await api.delete<{ data: FollowStatus }>(
    `/social/profile/${userNumb}/follow`,
  );
  return assertResultDataSuccess(res.data);
};
