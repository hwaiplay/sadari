import { getApiErrorMessage } from "@/app/api/resultData.ts";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert.ts";
import { message } from "@/app/messages/message.ts";
import {
  acceptClubInvitationApi,
  declineClubInvitationApi,
  getClubApplicationListApi,
  getClubInvitationListApi,
  getMyClubListApi,
  type ClubInvitation,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi.ts";
import { type KeyboardEvent, type MouseEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export type PendingClubApplications = {
  clubNumb: number;
  clubName: string;
  applicationCnt: number;
};

/**
 * 모임장 권한을 가진 모임인지 판정함
 *
 * @author HanWon.Jang
 * @param club 판정할 모임
 * @return 모임장 권한 여부
 */
const isOwnerClub = (club: ReadingClub): boolean => {
  // 가입 신청 조회 권한이 있는 모임장 여부를 반환함
  return club.membRole === "OWNER";
};

/**
 * 승인 대기 신청이 있는 모임 요약인지 판정함
 *
 * @author HanWon.Jang
 * @param item 판정할 모임 요약
 * @return 승인 대기 모임 요약 여부
 */
const hasPendingSummary = (
  item: PendingClubApplications | null,
): item is PendingClubApplications => {
  // 빈 조회 결과를 제외할 수 있도록 판정 결과를 반환함
  return item !== null;
};

/**
 * 모임장인 모임 한 곳의 승인 대기 가입 신청 건수를 조회함
 *
 * @author HanWon.Jang
 * @param club 가입 신청을 확인할 모임
 * @return 승인 대기 신청이 있는 모임 요약 또는 빈 값
 * @throws 가입 신청 조회에 실패하면 발생함
 */
const getPendingApplications = async (
  club: ReadingClub,
): Promise<PendingClubApplications | null> => {
  // 모임장 권한이 확인된 모임의 기존 가입 신청 목록을 조회함
  const applications = await getClubApplicationListApi(club.clubNumb);
  // 승인 대기 신청이 없으면 상단 알림 대상에서 제외함
  if (applications.length === 0) {
    // 알림 대상이 아님을 나타내는 빈 값을 반환함
    return null;
  }

  // 모임별 승인 대기 건수와 관리 화면 이동에 필요한 식별값을 반환함
  return {
    clubNumb: club.clubNumb,
    clubName: club.clubName,
    applicationCnt: applications.length,
  };
};

/**
 * 모임의 모든 관심분야명을 카드 상단에 표시할 문구로 변환함
 *
 * @author HanWon.Jang
 * @param club 표시할 모임
 * @return 전체 관심분야명 또는 기본 문구
 */
export const getClubCategory = (club: ReadingClub): string => {
  // 각 카테고리의 세부명부터 대분류명과 코드 순서로 표시값을 보정함
  const categoryNames = club.categoryList
    ?.map((category) => category.intrName ?? category.intrCnam ?? category.intrCode)
    .filter((categoryName): categoryName is string => Boolean(categoryName)) ?? [];
  // 카테고리 표시값이 있으면 모든 분류를 카드에 함께 표시함
  if (categoryNames.length > 0) {
    // 전체 카테고리의 표시 가능한 값을 쉼표로 구분해 반환함
    return categoryNames.join(" · ");
  }

  // 관심분야가 없는 모임의 기본 분류를 반환함
  return message("frontend.readingClub.common.defaultCategory");
};

/**
 * 모임 공개 범위와 현재 참여 인원을 카드 설명으로 구성함
 *
 * @author HanWon.Jang, SeungHyeon.Kang
 * @param club 표시할 모임
 * @return 공개 범위와 참여 인원 문구
 */
export const getClubMeta = (club: ReadingClub): string => {
  // 공개 코드에 맞는 다국어 공개 범위를 결정함
  const visibility = club.clubVisb === "PUBLIC"
    ? /* "공개" */ message("frontend.common.public")
    : /* "비공개" */ message("frontend.common.private");

  // 공개 범위와 현재 참여 인원을 함께 반환함
  return `${visibility} · ${message("frontend.readingClub.common.memberCount", [club.memberCnt])}`;
};

/**
 * 현재 독서 목표를 달성한 인원을 목표 참여 인원 대비 백분율로 변환함
 *
 * @author HanWon.Jang
 * @param club 표시할 모임
 * @return 0부터 100 사이의 목표 달성률
 */
export const getGoalProgress = (club: ReadingClub): number => {
  // 진행 중인 독서 목표가 없으면 빈 진행률을 표시함
  if (!club.currentRondNumb) {
    // 도서 선정 중인 모임의 빈 진행률을 반환함
    return 0;
  }

  // 모임장을 포함한 참여 인원이 항상 한 명 이상 표시되도록 보정함
  const goalMemberCnt = Math.max(1, club.currentGoalMembCnt ?? 0);
  // 비정상 집계값이 진행률 범위를 벗어나지 않도록 달성 인원을 보정함
  const goalAchvCnt = Math.min(goalMemberCnt, Math.max(0, club.currentGoalAchvCnt ?? 0));
  // 카드 너비를 넘지 않는 목표 달성률을 반환함
  return Math.round((goalAchvCnt / goalMemberCnt) * 100);
};

/**
 * 현재 독서 목표의 달성 인원 문구 또는 도서 선정 상태를 구성함
 *
 * @author HanWon.Jang
 * @param club 표시할 모임
 * @return 목표 달성 인원 또는 도서 선정 상태 문구
 */
export const getGoalProgressText = (club: ReadingClub): string => {
  // 진행 중인 독서 목표가 없으면 다음 도서를 정하는 상태로 안내함
  if (!club.currentRondNumb) {
    // "도서 선정 중"
    return message("frontend.readingClub.my.selectingBook");
  }

  // 모임장을 포함한 참여 인원이 항상 한 명 이상 표시되도록 보정함
  const goalMemberCnt = Math.max(1, club.currentGoalMembCnt ?? 0);
  // 비정상 집계값이 참여 인원 범위를 벗어나지 않도록 달성 인원을 보정함
  const goalAchvCnt = Math.min(goalMemberCnt, Math.max(0, club.currentGoalAchvCnt ?? 0));
  // "{달성 인원}/{참여 인원}명 목표 달성"
  return message("frontend.readingClub.my.goalAchievement", [goalAchvCnt, goalMemberCnt]);
};

/**
 * 내 모임 화면의 조회 상태와 초대 처리 및 상세 이동을 관리함
 *
 * @author HanWon.Jang, SeungHyeon.Kang
 * @return 내 모임 화면 상태와 이벤트 처리 함수
 */
export const useMyClubPage = () => {
  // 모임 상세 이동에 사용할 라우터 함수를 조회함
  const navigate = useNavigate();
  const [clubs, setClubs] = useState<ReadingClub[]>([]);
  const [invitations, setInvitations] = useState<ClubInvitation[]>([]);
  const [pendingApplications, setPendingApplications] = useState<PendingClubApplications[]>([]);
  const [isNoticeOpen, setIsNoticeOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * 내 모임과 받은 초대를 동시에 새로 조회함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   * @throws 모임 또는 초대 목록 조회에 실패하면 발생함
   */
  const loadPage = useCallback(async (): Promise<void> => {
    // 내 모임과 받은 초대를 동시에 조회해 화면 대기 시간을 줄임
    const [clubList, invitationList] = await Promise.all([getMyClubListApi(), getClubInvitationListApi()]);
    // 모임장 권한이 있는 모임만 가입 신청 조회 대상으로 선별함
    const ownerClubs = clubList.filter(isOwnerClub);
    // 모임장인 모임의 가입 신청을 동시에 조회함
    const applicationResults = await Promise.all(ownerClubs.map(getPendingApplications));
    // 내 모임 목록을 화면 상태에 반영함
    setClubs(clubList);
    // 받은 초대 목록을 화면 상태에 반영함
    setInvitations(invitationList);
    // 승인 대기 신청이 있는 모임만 상단 알림 상태에 반영함
    setPendingApplications(applicationResults.filter(hasPendingSummary));
  }, []);

  /**
   * 최초 모임 목록 조회 실패를 사용자 공통 알림으로 처리함
   *
   * @author HanWon.Jang
   * @param error 모임 목록 조회 오류
   * @return 반환값이 없음
   */
  const handleLoadError = useCallback((error: unknown): void => {
    // "조회하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
    );
  }, []);

  /**
   * 최초 모임 목록 조회 완료 후 로딩 화면을 해제함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleLoadComplete = useCallback((): void => {
    // 목록 화면을 표시할 수 있도록 로딩 상태를 해제함
    setIsLoading(false);
  }, []);

  /**
   * 페이지 최초 진입 시 모임과 초대 목록 조회를 시작함
   *
   * @author HanWon.Jang
   * @return Effect 정리 함수가 없음
   */
  const initializePage = useCallback((): void => {
    // 최초 진입 데이터를 조회함
    void loadPage()
      // 조회 실패 원인을 사용자용 공통 문구로 변환함
      .catch(handleLoadError)
      // 조회 완료 후 로딩 화면을 해제함
      .finally(handleLoadComplete);
  }, [handleLoadComplete, handleLoadError, loadPage]);

  // 페이지 최초 진입에서만 목록 조회 함수를 실행함
  useEffect(initializePage, [initializePage]);

  /**
   * 받은 초대를 수락하고 최신 모임 목록을 반영함
   *
   * @author HanWon.Jang
   * @param clubNumb 수락할 모임 번호
   * @return 반환값이 없음
   * @throws 초대 수락 또는 목록 갱신에 실패하면 발생함
   */
  const acceptInvitation = async (clubNumb: number): Promise<void> => {
    // 예약석을 활성 회원으로 전환함
    await acceptClubInvitationApi(clubNumb);
    // 변경된 내 모임과 초대 목록을 다시 조회함
    await loadPage();
  };

  /**
   * 받은 초대를 거절하고 최신 초대 목록을 반영함
   *
   * @author HanWon.Jang
   * @param clubNumb 거절할 모임 번호
   * @return 반환값이 없음
   * @throws 초대 거절 또는 목록 갱신에 실패하면 발생함
   */
  const declineInvitation = async (clubNumb: number): Promise<void> => {
    // 초대 예약석을 삭제함
    await declineClubInvitationApi(clubNumb);
    // 변경된 내 모임과 초대 목록을 다시 조회함
    await loadPage();
  };

  /**
   * 받은 초대 상세 목록의 표시 상태를 전환함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleNoticeToggle = (): void => {
    // 현재 상태의 반대로 모임 알림 상세 목록을 전환함
    setIsNoticeOpen(!isNoticeOpen);
  };

  /**
   * 선택한 모임 상세 화면으로 이동함
   *
   * @author HanWon.Jang
   * @param clubNumb 이동할 모임 번호
   * @return 반환값이 없음
   */
  const handleClubMove = (clubNumb: number): void => {
    // 선택한 모임 번호를 상세 경로에 포함함
    navigate(`/reading-clubs/${clubNumb}`);
  };

  /**
   * 키보드 Enter 입력으로 선택한 모임 상세 화면에 이동함
   *
   * @author HanWon.Jang
   * @param event 카드 키보드 이벤트
   * @return 반환값이 없음
   */
  const handleClubKeyDown = (event: KeyboardEvent<HTMLElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // Enter 입력에서만 카드의 상세 이동을 실행함
    if (event.key === "Enter" && Number.isFinite(clubNumb)) {
      // 키보드로 선택한 모임 상세 화면으로 이동함
      handleClubMove(clubNumb);
    }
  };

  /**
   * 포인터로 선택한 모임 카드의 상세 화면에 이동함
   *
   * @author HanWon.Jang
   * @param event 모임 카드 클릭 이벤트
   * @return 반환값이 없음
   */
  const handleClubClick = (event: MouseEvent<HTMLElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 카드만 상세 화면으로 이동함
    if (Number.isFinite(clubNumb)) {
      // 포인터로 선택한 모임 상세 화면으로 이동함
      handleClubMove(clubNumb);
    }
  };

  /**
   * 초대 수락 실패를 사용자 공통 알림으로 처리함
   *
   * @author HanWon.Jang
   * @param error 초대 수락 오류
   * @return 반환값이 없음
   */
  const handleAcceptError = (error: unknown): void => {
    // "초대를 수락하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.acceptInvitationTitle"),
      getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
    );
  };

  /**
   * 초대 거절 실패를 사용자 공통 알림으로 처리함
   *
   * @author HanWon.Jang, SeungHyeon.Kang
   * @param error 초대 거절 오류
   * @return 반환값이 없음
   */
  const handleDeclineError = (error: unknown): void => {
    // "초대를 거절하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.declineInvitationTitle"),
      getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
    );
  };

  /**
   * 선택한 받은 초대를 수락함
   *
   * @author HanWon.Jang
   * @param event 초대 수락 버튼 이벤트
   * @return 반환값이 없음
   */
  const handleAcceptInvitation = (event: MouseEvent<HTMLButtonElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 초대만 수락함
    if (Number.isFinite(clubNumb)) {
      // 선택한 초대를 수락하고 실패 시 사용자에게 안내함
      void acceptInvitation(clubNumb).catch(handleAcceptError);
    }
  };

  /**
   * 선택한 받은 초대를 거절함
   *
   * @author HanWon.Jang
   * @param event 초대 거절 버튼 이벤트
   * @return 반환값이 없음
   */
  const handleDeclineInvitation = (event: MouseEvent<HTMLButtonElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 초대만 거절함
    if (Number.isFinite(clubNumb)) {
      // 선택한 초대를 거절하고 실패 시 사용자에게 안내함
      void declineInvitation(clubNumb).catch(handleDeclineError);
    }
  };

  // 화면 렌더링에 필요한 상태와 이벤트 처리 함수를 반환함
  return {
    clubs,
    invitations,
    pendingApplications,
    isNoticeOpen,
    isLoading,
    handleNoticeToggle,
    handleClubKeyDown,
    handleClubClick,
    handleAcceptInvitation,
    handleDeclineInvitation,
  };
};
