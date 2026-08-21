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
  clubCntn: string | null;
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
  currentRondNumb?: number;
  readingOrdr?: number;
  currentBookNumb?: number;
  currentBookTitl?: string;
  currentBookAthr?: string;
  currentBookCvim?: string;
  currentBookPubl?: string;
  currentBookIsbn?: string;
  currentBookDesc?: string;
  currentPublDate?: string;
  currentBookChangeAllowed?: boolean;
  currentGoalStdt?: string;
  currentGoalEndt?: string;
  currentReportStat?: "READ" | "DONE" | "STOP";
  currentReportNumb?: number;
  currentGoalAchvCnt?: number;
  currentGoalMembCnt?: number;
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
  intrText?: string;
};

export type SentClubInvitation = {
  userNumb: number;
  userNick?: string;
  porfPath?: string;
  intrText?: string;
  invtDate: string;
  exprDate: string;
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

export type ClubMemberProfile = {
  userNumb: number;
  userNick?: string;
  porfPath?: string;
  membRole: "OWNER" | "MEMBER";
};

export type ClubReadingCreateParams = {
  bookTitl: string;
  bookAthr: string;
  bookPubl: string;
  bookIsbn: string;
  bookCvim: string;
  bookDesc: string;
  publDate: string;
  goalStdt: string;
  goalEndt: string;
  idemKeyx: string;
};

export type ClubReadingUpdateParams = Omit<ClubReadingCreateParams, "idemKeyx">;

export type ClubReadingCreateResult = {
  rondNumb: number;
};

/**
 * 모임 독서 회차와 활성 멤버별 읽는 중 독후감을 등록한다.
 *
 * @author Hanwon.Jang
 * @param clubNumb 모임 번호
 * @param params 선택 도서와 목표 독서 기간
 * @return 생성된 모임 독서 회차 번호
 */
export const createClubReadingApi = async (
  clubNumb: number,
  params: ClubReadingCreateParams,
): Promise<ClubReadingCreateResult> => {

  // 모임 독서 등록 정보를 서버에 전달한다
  const response = await api.post(`/reading-clubs/${clubNumb}/setBook`, params);
  // 공통 성공 응답에서 생성된 회차 번호를 반환한다
  return assertResultDataSuccess(response.data).data as ClubReadingCreateResult;
};

/**
 * 현재 모임 독서의 도서와 목표 기간을 수정한다.
 *
 * @author Hanwon.Jang
 * @param clubNumb 모임 번호
 * @param rondNumb 수정할 회차 번호
 * @param params 선택 도서와 목표 독서 기간
 * @return 수정된 모임 독서 회차 번호
 * @throws 현재 모임장 권한이 없거나 수정 요청이 거절되면 발생
 */
export const updateClubReadingApi = async (
  clubNumb: number,
  rondNumb: number,
  params: ClubReadingUpdateParams,
): Promise<ClubReadingCreateResult> => {

  // 모임장 권한으로 현재 회차의 도서와 목표 기간 수정을 요청한다
  const response = await api.put(
    `/reading-clubs/${clubNumb}/${rondNumb}/updateClub`,
    params,
  );
  // 공통 성공 검증을 통과한 수정 회차 번호를 반환한다
  return assertResultDataSuccess(response.data).data as ClubReadingCreateResult;
};

/** 로그인 사용자의 활성 모임을 조회한다. @author Hanwon.Jang @return 내 모임 목록 */
export const getMyClubListApi = async (): Promise<ReadingClub[]> => {
  // 내 모임 목록을 서버에 요청한다
  const response = await api.get("/reading-clubs/mine");
  // 공통 성공 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ReadingClub[] | undefined) ?? [];
};

/** 공개 모임을 검색한다. @author Hanwon.Jang @param keyword 모임 검색어 @return 공개 모임 목록 */
export const getFindClubListApi = async (keyword: string): Promise<ReadingClub[]> => {
  // 검색어를 Query Parameter로 전달한다
  const response = await api.get("/reading-clubs", { params: { keyword } });
  // 공통 성공 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ReadingClub[] | undefined) ?? [];
};

/** 모임 상세를 조회한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 모임 상세 */
export const getClubDtlApi = async (clubNumb: number): Promise<ReadingClub> => {
  // 선택한 모임 상세를 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}`);
  // 공통 성공 검증을 통과한 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/**
 * 활성 모임원 프로필을 조회한다
 *
 * @author Hanwon.Jang
 * @param clubNumb 모임 번호
 * @return 활성 모임원과 프로필 이미지 경로 목록
 * @throws 모임 상세 조회 실패 또는 접근 권한이 없을 때 발생
 */
export const getClubMemberListApi = async (clubNumb: number): Promise<ClubMemberProfile[]> => {
  // 같은 모임의 활성 회원에게 허용된 모임원 프로필을 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/members`);
  // 공통 성공 검증을 통과한 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ClubMemberProfile[] | undefined) ?? [];
};

/** 새 모임을 생성한다. @author Hanwon.Jang @param params 모임 생성 입력값 @return 생성된 모임 상세 */
export const createClubApi = async (params: ClubCreateParams): Promise<ReadingClub> => {
  // 모임 생성 입력값을 서버에 전달한다
  const response = await api.post("/reading-clubs", params);
  // 생성된 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 공개 모임에 가입하거나 승인 신청한다. @author Hanwon.Jang @param clubNumb 모임 번호 @param answerList 승인 질문 답변 @return 처리 후 모임 상세 */
export const joinClubApi = async (clubNumb: number, answerList: string[]): Promise<ReadingClub> => {
  // 모임 가입 정책에 맞춘 요청을 전달한다
  const response = await api.post(`/reading-clubs/${clubNumb}/memberships`, { answerList });
  // 처리 후 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 모임장의 맞팔 초대 후보를 조회한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 초대 후보 목록 */
export const getInviteCandidateListApi = async (clubNumb: number): Promise<InviteCandidate[]> => {
  // 모임별 맞팔 초대 후보를 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/invitation-candidates`);
  // 후보 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as InviteCandidate[] | undefined) ?? [];
};

/** 모임 정보를 수정한다. @author Hanwon.Jang @param clubNumb 모임 번호 @param params 모임 수정 입력값 @return 수정된 모임 상세 */
export const uptClubApi = async (clubNumb: number, params: ClubCreateParams): Promise<ReadingClub> => {
  // 모임장 권한으로 수정 입력값을 서버에 전달한다
  const response = await api.put(`/reading-clubs/${clubNumb}`, params);
  // 수정된 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 모임을 삭제한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 처리 응답 */
export const delClubApi = async (clubNumb: number) => {
  // 모임장 권한으로 모임 삭제를 요청한다
  const response = await api.delete(`/reading-clubs/${clubNumb}`);
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 모임장이 활성 회원에게 보낸 유효한 초대를 조회한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 보낸 초대 목록 */
export const getSentClubInvitationListApi = async (clubNumb: number): Promise<SentClubInvitation[]> => {
  // 비활성화 또는 삭제 대기 회원을 제외한 보낸 초대 목록을 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/invitations/sent`);
  // 공통 성공 검증을 통과한 보낸 초대 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as SentClubInvitation[] | undefined) ?? [];
};

/** 선택한 맞팔 사용자에게 모임 초대를 발송한다. @author Hanwon.Jang @param clubNumb 모임 번호 @param userNumbList 초대 대상 사용자 번호 @return 처리 응답 */
export const inviteClubUsersApi = async (clubNumb: number, userNumbList: number[]) => {
  // 좌석 예약을 포함한 초대 요청을 전달한다
  const response = await api.post(`/reading-clubs/${clubNumb}/invitations`, { userNumbList });
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 모임장이 활성 회원에게 보낸 초대를 취소한다. @author Hanwon.Jang @param clubNumb 모임 번호 @param userNumb 초대 대상 사용자 번호 @return 처리 응답 */
export const cancelSentClubInvitationApi = async (clubNumb: number, userNumb: number) => {
  // 선택한 회원에게 보낸 유효한 초대의 취소를 요청한다
  const response = await api.delete(`/reading-clubs/${clubNumb}/invitations/${userNumb}`);
  // 공통 성공 검증을 통과한 취소 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 받은 모임 초대를 조회한다. @author Hanwon.Jang @return 유효한 받은 초대 목록 */
export const getClubInvitationListApi = async (): Promise<ClubInvitation[]> => {
  // 유효한 받은 초대 목록을 요청한다
  const response = await api.get("/reading-clubs/invitations/received");
  // 받은 초대 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ClubInvitation[] | undefined) ?? [];
};

/** 받은 초대를 수락한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 가입된 모임 상세 */
export const acceptClubInvitationApi = async (clubNumb: number): Promise<ReadingClub> => {
  // 예약석을 활성 회원으로 전환한다
  const response = await api.put(`/reading-clubs/${clubNumb}/invitations/received`);
  // 가입된 모임 상세를 반환한다
  return assertResultDataSuccess(response.data).data as ReadingClub;
};

/** 받은 초대를 거절한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 처리 응답 */
export const declineClubInvitationApi = async (clubNumb: number) => {
  // 받은 초대 예약석 삭제를 요청한다
  const response = await api.delete(`/reading-clubs/${clubNumb}/invitations/received`);
  // 공통 성공 응답을 반환한다
  return assertResultDataSuccess(response.data);
};

/** 모임의 승인 대기 신청을 조회한다. @author Hanwon.Jang @param clubNumb 모임 번호 @return 승인 대기 신청 목록 */
export const getClubApplicationListApi = async (clubNumb: number): Promise<ClubApplication[]> => {
  // 모임장용 승인 대기 목록을 요청한다
  const response = await api.get(`/reading-clubs/${clubNumb}/applications`);
  // 신청 목록을 반환한다
  return (assertResultDataSuccess(response.data).data as ClubApplication[] | undefined) ?? [];
};

/** 가입 신청을 승인 또는 거절한다. @author Hanwon.Jang @param clubNumb 모임 번호 @param applNumb 신청 번호 @param joinStat 처리 상태 @return 처리 응답 */
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
