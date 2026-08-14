import { getApiErrorMessage } from "@/app/api/resultData.ts";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert.ts";
import { message } from "@/app/messages/message.ts";
import {
  acceptClubInvitationApi,
  declineClubInvitationApi,
  getClubInvitationListApi,
  getMyClubListApi,
  type ClubInvitation,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi.ts";
import { type KeyboardEvent, type MouseEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

/**
 * 모임의 첫 번째 관심분야명을 카드 상단에 표시할 문구로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param club 표시할 모임
 * @return 첫 번째 관심분야명 또는 기본 문구
 */
export const getClubCategory = (club: ReadingClub): string => {
  // 관심분야명이 있으면 카드 분류로 사용한다
  if (club.categoryList?.[0]?.intrName) {
    // 첫 번째 관심분야명을 반환한다
    return club.categoryList[0].intrName;
  }

  // 관심분야가 없는 모임의 기본 분류를 반환한다
  return message("frontend.readingClub.common.defaultCategory");
};

/**
 * 모임 공개 범위와 현재 참여 인원을 카드 설명으로 구성한다
 *
 * @author SeungHyeon.Kang
 * @param club 표시할 모임
 * @return 공개 범위와 참여 인원 문구
 */
export const getClubMeta = (club: ReadingClub): string => {
  // 공개 코드에 맞는 다국어 공개 범위를 결정한다
  const visibility = club.clubVisb === "PUBLIC"
    ? message("frontend.readingClub.common.visibility.public")
    : message("frontend.readingClub.common.visibility.private");

  // 공개 범위와 현재 참여 인원을 함께 반환한다
  return `${visibility} · ${message("frontend.readingClub.common.memberCount", [club.memberCnt])}`;
};

/**
 * 현재 참여 인원을 정원 대비 백분율로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param club 표시할 모임
 * @return 0부터 100 사이의 참여율
 */
export const getMemberProgress = (club: ReadingClub): number => {
  // 정원이 없으면 0으로 나누지 않도록 빈 진행률을 반환한다
  if (club.maxxMemb <= 0) {
    // 빈 진행률을 반환한다
    return 0;
  }

  // 카드 너비를 넘지 않는 참여율을 반환한다
  return Math.min(100, Math.round((club.memberCnt / club.maxxMemb) * 100));
};

/**
 * 내 모임 화면의 조회 상태와 초대 처리 및 상세 이동을 관리한다
 *
 * @author SeungHyeon.Kang
 * @return 내 모임 화면 상태와 이벤트 처리 함수
 */
export const useMyClubPage = () => {
  // 모임 상세 이동에 사용할 라우터 함수를 조회한다
  const navigate = useNavigate();
  const [clubs, setClubs] = useState<ReadingClub[]>([]);
  const [invitations, setInvitations] = useState<ClubInvitation[]>([]);
  const [isInvitationOpen, setIsInvitationOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * 내 모임과 받은 초대를 동시에 새로 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   * @throws 모임 또는 초대 목록 조회에 실패하면 발생한다
   */
  const loadPage = useCallback(async (): Promise<void> => {
    // 두 목록을 동시에 조회해 화면 대기 시간을 줄인다
    const [clubList, invitationList] = await Promise.all([getMyClubListApi(), getClubInvitationListApi()]);
    // 내 모임 목록을 화면 상태에 반영한다
    setClubs(clubList);
    // 받은 초대 목록을 화면 상태에 반영한다
    setInvitations(invitationList);
  }, []);

  /**
   * 최초 모임 목록 조회 실패를 사용자 공통 알림으로 처리한다
   *
   * @author SeungHyeon.Kang
   * @param error 모임 목록 조회 오류
   * @return 반환값이 없다
   */
  const handleLoadError = useCallback((error: unknown): void => {
    // "조회하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
    );
  }, []);

  /**
   * 최초 모임 목록 조회 완료 후 로딩 화면을 해제한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleLoadComplete = useCallback((): void => {
    // 목록 화면을 표시할 수 있도록 로딩 상태를 해제한다
    setIsLoading(false);
  }, []);

  /**
   * 페이지 최초 진입 시 모임과 초대 목록 조회를 시작한다
   *
   * @author SeungHyeon.Kang
   * @return Effect 정리 함수가 없다
   */
  const initializePage = useCallback((): void => {
    // 최초 진입 데이터를 조회한다
    void loadPage()
      // 조회 실패 원인을 사용자용 공통 문구로 변환한다
      .catch(handleLoadError)
      // 조회 완료 후 로딩 화면을 해제한다
      .finally(handleLoadComplete);
  }, [handleLoadComplete, handleLoadError, loadPage]);

  // 페이지 최초 진입에서만 목록 조회 함수를 실행한다
  useEffect(initializePage, [initializePage]);

  /**
   * 받은 초대를 수락하고 최신 모임 목록을 반영한다
   *
   * @author SeungHyeon.Kang
   * @param clubNumb 수락할 모임 번호
   * @return 반환값이 없다
   * @throws 초대 수락 또는 목록 갱신에 실패하면 발생한다
   */
  const acceptInvitation = async (clubNumb: number): Promise<void> => {
    // 예약석을 활성 회원으로 전환한다
    await acceptClubInvitationApi(clubNumb);
    // 변경된 내 모임과 초대 목록을 다시 조회한다
    await loadPage();
  };

  /**
   * 받은 초대를 거절하고 최신 초대 목록을 반영한다
   *
   * @author SeungHyeon.Kang
   * @param clubNumb 거절할 모임 번호
   * @return 반환값이 없다
   * @throws 초대 거절 또는 목록 갱신에 실패하면 발생한다
   */
  const declineInvitation = async (clubNumb: number): Promise<void> => {
    // 초대 예약석을 삭제한다
    await declineClubInvitationApi(clubNumb);
    // 변경된 내 모임과 초대 목록을 다시 조회한다
    await loadPage();
  };

  /**
   * 받은 초대 상세 목록의 표시 상태를 전환한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleInvitationToggle = (): void => {
    // 현재 상태의 반대로 초대 상세 목록을 전환한다
    setIsInvitationOpen(!isInvitationOpen);
  };

  /**
   * 선택한 모임 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param clubNumb 이동할 모임 번호
   * @return 반환값이 없다
   */
  const handleClubMove = (clubNumb: number): void => {
    // 선택한 모임 번호를 상세 경로에 포함한다
    navigate(`/reading-clubs/${clubNumb}`);
  };

  /**
   * 키보드 Enter 입력으로 선택한 모임 상세 화면에 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 카드 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleClubKeyDown = (event: KeyboardEvent<HTMLElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // Enter 입력에서만 카드의 상세 이동을 실행한다
    if (event.key === "Enter" && Number.isFinite(clubNumb)) {
      // 키보드로 선택한 모임 상세 화면으로 이동한다
      handleClubMove(clubNumb);
    }
  };

  /**
   * 포인터로 선택한 모임 카드의 상세 화면에 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 모임 카드 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleClubClick = (event: MouseEvent<HTMLElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 카드만 상세 화면으로 이동한다
    if (Number.isFinite(clubNumb)) {
      // 포인터로 선택한 모임 상세 화면으로 이동한다
      handleClubMove(clubNumb);
    }
  };

  /**
   * 초대 수락 실패를 사용자 공통 알림으로 처리한다
   *
   * @author SeungHyeon.Kang
   * @param error 초대 수락 오류
   * @return 반환값이 없다
   */
  const handleAcceptError = (error: unknown): void => {
    // "초대를 수락하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.acceptInvitationTitle"),
      getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
    );
  };

  /**
   * 초대 거절 실패를 사용자 공통 알림으로 처리한다
   *
   * @author SeungHyeon.Kang
   * @param error 초대 거절 오류
   * @return 반환값이 없다
   */
  const handleDeclineError = (error: unknown): void => {
    // "초대를 거절하지 못했어요"
    void sweetError(
      message("frontend.readingClub.error.declineInvitationTitle"),
      getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
    );
  };

  /**
   * 선택한 받은 초대를 수락한다
   *
   * @author SeungHyeon.Kang
   * @param event 초대 수락 버튼 이벤트
   * @return 반환값이 없다
   */
  const handleAcceptInvitation = (event: MouseEvent<HTMLButtonElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 초대만 수락한다
    if (Number.isFinite(clubNumb)) {
      // 선택한 초대를 수락하고 실패 시 사용자에게 안내한다
      void acceptInvitation(clubNumb).catch(handleAcceptError);
    }
  };

  /**
   * 선택한 받은 초대를 거절한다
   *
   * @author SeungHyeon.Kang
   * @param event 초대 거절 버튼 이벤트
   * @return 반환값이 없다
   */
  const handleDeclineInvitation = (event: MouseEvent<HTMLButtonElement>): void => {
    const clubNumb = Number(event.currentTarget.dataset.clubNumb);
    // 유효한 모임 번호가 있는 초대만 거절한다
    if (Number.isFinite(clubNumb)) {
      // 선택한 초대를 거절하고 실패 시 사용자에게 안내한다
      void declineInvitation(clubNumb).catch(handleDeclineError);
    }
  };

  // 화면 렌더링에 필요한 상태와 이벤트 처리 함수를 반환한다
  return {
    clubs,
    invitations,
    isInvitationOpen,
    isLoading,
    handleInvitationToggle,
    handleClubKeyDown,
    handleClubClick,
    handleAcceptInvitation,
    handleDeclineInvitation,
  };
};
