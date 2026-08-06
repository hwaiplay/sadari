import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

export type ClubCategory = {
  intrCode: string;
  intrName?: string;
  intrCnam?: string;
  sortOrdr?: number;
};

export type ReadingClub = {
  clubNumb: number;
  ownrNumb?: number;
  ownrNick?: string;
  clubName: string;
  clubCntn: string;
  clubVisb: "PUBLIC" | "PRIVATE";
  joinType: "OPEN" | "APPROVAL" | "INVITE";
  clubStat: string;
  maxxMemb: number;
  memberCnt: number;
  invitedCnt: number;
  membStat?: "INVITED" | "ACTIVE";
  membRole?: "OWNER" | "MEMBER";
  joinStat?: "PENDING";
  matchCnt?: number;
  categoryList?: ClubCategory[];
  questionList?: string[];
  regiDate?: string;
};

export type ClubCreateParams = {
  clubName: string;
  clubCntn: string;
  clubVisb: "PUBLIC" | "PRIVATE";
  joinType: "OPEN" | "APPROVAL" | "INVITE";
  maxxMemb: number;
  categoryList: string[];
  questionList: string[];
};

export type InviteCandidate = {
  userNumb: number;
  userNick?: string;
  porfPath?: string;
  intrCntn?: string;
};

export type ClubInvitation = {
  clubNumb: number;
  clubName: string;
  senderNick?: string;
  invtDate: string;
  exprDate: string;
};

export type ClubApplication = {
  clubNumb: number;
  applNumb: number;
  userNumb: number;
  userNick?: string;
  porfPath?: string;
  questionList: string[];
  answerList: string[];
  joinStat: string;
  applDate: string;
};

/** 로그인 사용자의 활성 모임을 조회한다. @author SeungHyeon.Kang @return 내 모임 목록 */
export const getMyClubListApi = async (): Promise<ReadingClub[]> => {
  // 내 모임 목록을 서버에 요청한다
  const response = await api.get("/reading-clubs/mine");
  // 공통 성공 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ReadingClub[] | undefined) ?? [];
};

/** 공개 모임을 검색한다. @author SeungHyeon.Kang @param keyword 모임 검색어 @return 공개 모임 목록 */
export const getFindClubListApi = async (keyword: string): Promise<ReadingClub[]> => {
  // 검색어를 Query Parameter로 전달한다
  const response = await api.get("/reading-clubs", { params: { keyword } });
  // 공통 성공 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ReadingClub[] | undefined) ?? [];
};

/** 모임 상세를 조회한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 모임 상세 */
export const getClubDtlApi = async (clubNumb: number): Promise<ReadingClub> => {
  // 선택한 모임 상세를 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}`);
  // 공통 성공 검증을 통과한 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 새 모임을 생성한다. @author SeungHyeon.Kang @param params 모임 생성 입력값 @return 생성된 모임 상세 */
export const createClubApi = async (params: ClubCreateParams): Promise<ReadingClub> => {
  // 모임 생성 입력값을 서버에 전달한다
  const response = await api.post("/reading-clubs", params);
  // 생성된 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 공개 모임에 가입하거나 승인 신청한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @param answerList 승인 질문 답변 @return 처리 후 모임 상세 */
export const joinClubApi = async (clubNumb: number, answerList: string[]): Promise<ReadingClub> => {
  // 모임 가입 정책에 맞춘 요청을 전달한다
  const response = await api.post(`/reading-clubs/${clubNumb}/memberships`, { answerList });
  // 처리 후 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 모임장의 맞팔 초대 후보를 조회한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 초대 후보 목록 */
export const getInviteCandidateListApi = async (clubNumb: number): Promise<InviteCandidate[]> => {
  // 모임별 맞팔 초대 후보를 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/invitation-candidates`);
  // 후보 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as InviteCandidate[] | undefined) ?? [];
};

/** 선택한 맞팔 사용자에게 모임 초대를 발송한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @param userNumbList 초대 대상 사용자 번호 @return 처리 응답 */
export const inviteClubUsersApi = async (clubNumb: number, userNumbList: number[]) => {
  // 좌석 예약을 포함한 초대 요청을 전달한다
  const response = await api.post(`/reading-clubs/${clubNumb}/invitations`, { userNumbList });
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 받은 모임 초대를 조회한다. @author SeungHyeon.Kang @return 유효한 받은 초대 목록 */
export const getClubInvitationListApi = async (): Promise<ClubInvitation[]> => {
  // 유효한 받은 초대 목록을 요청한다
  const response = await api.get("/reading-clubs/invitations/received");
  // 받은 초대 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ClubInvitation[] | undefined) ?? [];
};

/** 받은 초대를 수락한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 가입된 모임 상세 */
export const acceptClubInvitationApi = async (clubNumb: number): Promise<ReadingClub> => {
  // 예약석을 활성 회원으로 전환한다
  const response = await api.put(`/reading-clubs/${clubNumb}/invitations/received`);
  // 가입된 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 받은 초대를 거절한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 처리 응답 */
export const declineClubInvitationApi = async (clubNumb: number) => {
  // 받은 초대 예약석 삭제를 요청한다
  const response = await api.delete(`/reading-clubs/${clubNumb}/invitations/received`);
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 모임의 승인 대기 신청을 조회한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 승인 대기 신청 목록 */
export const getClubApplicationListApi = async (clubNumb: number): Promise<ClubApplication[]> => {
  // 모임장용 승인 대기 목록을 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/applications`);
  // 신청 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ClubApplication[] | undefined) ?? [];
};

/** 가입 신청을 승인 또는 거절한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @param applNumb 신청 번호 @param joinStat 처리 상태 @return 처리 응답 */
export const decideClubApplicationApi = async (
  clubNumb: number,
  applNumb: number,
  joinStat: "APPROVED" | "REJECTED",
) => {
  // 처리 상태를 모임장 승인 API에 전달한다
  const response = await api.put(`/reading-clubs/${clubNumb}/applications/${applNumb}`, { joinStat });
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};
