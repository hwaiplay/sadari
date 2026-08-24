/**
 * fileName       : useClubMemberManage
 * author         : HanWon.Jang
 * date           : 2026-08-14
 * description    : 멤버와 가입 신청 관리 화면의 조회 및 처리 상태를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 * 2026-08-24        HanWon.Jang        모임원 퇴장 처리 추가
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  cancelSentClubInvitationApi,
  decideClubApplicationApi,
  exitClubMemberApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getClubMemberListApi,
  getInviteCandidateListApi,
  getSentClubInvitationListApi,
  inviteClubUsersApi,
  type ClubApplication,
  type ClubMemberProfile,
  type InviteCandidate,
  type ReadingClub,
  type SentClubInvitation,
} from "@/features/ReadingClub/api/readingClubApi";
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

type ApplicationDecision = "APPROVED" | "REJECTED";

/**
 * 멤버와 가입 신청 관리 화면의 서버 상태와 사용자 동작을 제공한다
 *
 * @author Hanwon.Jang
 * @return 멤버와 가입 신청 관리 화면 상태 및 이벤트 처리 함수
 */
export const useClubMemberManage = () => {
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const [club, setClub] = useState<ReadingClub | null>(null);
  const [applications, setApplications] = useState<ClubApplication[]>([]);
  const [members, setMembers] = useState<ClubMemberProfile[]>([]);
  const [candidates, setCandidates] = useState<InviteCandidate[]>([]);
  const [sentInvitations, setSentInvitations] = useState<SentClubInvitation[]>([]);
  const [selectedApplication, setSelectedApplication] = useState<ClubApplication | null>(null);
  const [selectedMember, setSelectedMember] = useState<ClubMemberProfile | null>(null);
  const [exitReason, setExitReason] = useState("");
  const [isInviteOpen, setIsInviteOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * 모임장 권한과 멤버 및 가입 관리 데이터를 함께 조회한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   * @throws 모임 상세 또는 관리 데이터 조회에 실패할 때 발생
   */
  const getPageData = useCallback(async (): Promise<void> => {
    // 모임장 권한을 먼저 확인하여 관리 데이터의 불필요한 조회를 막는다
    const nextClub = await getClubDtlApi(clubNumb);

    // 모임장이 아닌 사용자는 관리 화면을 계속 볼 수 없게 모임 상세로 돌려보낸다
    if (nextClub.membRole !== "OWNER") {
      // "모임장만 멤버 관리 화면을 이용할 수 있어요."
      await sweetError(message("frontend.readingClub.memberManage.accessTitle"));
      // 권한이 있는 범위인 현재 모임 상세 화면으로 이동한다
      navigate(`/reading-clubs/${clubNumb}`, { replace: true });
      // 관리 데이터 요청을 중단한다
      return;
    }

    // 모임장에게 필요한 멤버와 신청 및 초대 후보를 중복 대기 없이 조회한다
    const [nextApplications, nextMembers, nextCandidates, nextSentInvitations] = await Promise.all([
      getClubApplicationListApi(clubNumb),
      getClubMemberListApi(clubNumb),
      getInviteCandidateListApi(clubNumb),
      getSentClubInvitationListApi(clubNumb),
    ]);

    // 조회한 모임 정보를 화면 정원 표시에 반영한다
    setClub(nextClub);
    // 활성 계정의 처리 대기 신청만 가입 신청 목록에 반영한다
    setApplications(nextApplications);
    // 활성 계정인 활성 모임원을 멤버 목록에 반영한다
    setMembers(nextMembers);
    // 현재 초대할 수 있는 맞팔 회원을 초대 목록에 반영한다
    setCandidates(nextCandidates);
    // 활성 회원에게 발송한 만료 전 초대를 보낸 초대 목록에 반영한다
    setSentInvitations(nextSentInvitations);
  }, [clubNumb, navigate]);

  /**
   * 멤버 관리 화면의 최초 조회와 오류 및 로딩 상태를 처리한다
   *
   * @author Hanwon.Jang
   * @return Effect 정리 함수가 없다
   */
  const initializePage = useCallback((): void => {
    // 잘못된 경로의 모임 번호로 관리 API를 호출하지 않는다
    if (!Number.isFinite(clubNumb)) {
      // "요청한 모임을 확인할 수 없어요."
      void sweetError(message("frontend.readingClub.memberManage.invalidClub"));
      // 유효한 독서 모임 목록 화면으로 이동한다
      navigate("/reading-clubs/mine", { replace: true });
      // 잘못된 경로 처리를 마치고 조회를 종료한다
      return;
    }

    // 최초 화면 조회 중에는 목록 대신 고정 크기 스켈레톤을 표시한다
    setIsLoading(true);
    // 모임장 전용 관리 데이터를 조회한다
    void getPageData()
      // 조회 실패는 내부 오류 대신 공통 사용자 메시지로 안내한다
      .catch((error: unknown) => {
        // "멤버 정보를 불러오지 못했어요"
        void sweetError(
          message("frontend.readingClub.memberManage.fetchTitle"),
          getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
        );
      })
      // 성공 여부와 관계없이 로딩 화면을 종료한다
      .finally(() => {
        // 조회가 끝난 실제 목록 또는 빈 상태를 표시한다
        setIsLoading(false);
      });
  }, [clubNumb, getPageData, navigate]);

  // 경로의 모임 번호가 바뀌면 해당 모임의 관리 데이터를 새로 조회한다
  useEffect(initializePage, [initializePage]);

  /**
   * 가입 신청자의 질문과 답변 모달을 연다
   *
   * @author Hanwon.Jang
   * @param application 확인할 가입 신청
   * @return 반환값이 없다
   */
  const handleAnswerOpen = (application: ClubApplication): void => {
    // 선택한 신청의 질문과 답변을 모달에 표시한다
    setSelectedApplication(application);
  };

  /**
   * 가입 신청자의 질문과 답변 모달을 닫는다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleAnswerClose = (): void => {
    // 선택한 신청을 비워 답변 모달을 닫는다
    setSelectedApplication(null);
  };

  /**
   * 가입 신청을 승인하거나 거절한 뒤 최신 목록을 조회한다
   *
   * @author Hanwon.Jang
   * @param joinStat 확정할 가입 신청 상태
   * @return 반환값이 없다
   */
  const handleApplicationDecision = (joinStat: ApplicationDecision): void => {
    // 선택된 신청이 없거나 다른 저장 작업 중이면 중복 처리를 시작하지 않는다
    if (!selectedApplication || isSubmitting) {
      // 처리할 가입 신청이 없으므로 종료한다
      return;
    }

    // 가입 신청 상태 변경이 중복 제출되지 않도록 처리 상태를 시작한다
    setIsSubmitting(true);
    // 현재 모임의 선택한 가입 신청 상태를 서버에서 변경한다
    void decideClubApplicationApi(clubNumb, selectedApplication.applNumb, joinStat)
      // 처리가 끝난 신청을 닫고 최신 멤버 및 신청 목록을 다시 조회한다
      .then(async () => {
        // 처리된 가입 신청의 답변 모달을 닫는다
        setSelectedApplication(null);
        // 승인 결과가 반영된 최신 관리 데이터를 조회한다
        await getPageData();
      })
      // 처리 실패는 서버 원문 대신 공통 사용자 메시지로 안내한다
      .catch((error: unknown) => {
        // "가입 신청을 처리하지 못했어요"
        void sweetError(
          message("frontend.readingClub.memberManage.decisionTitle"),
          getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
        );
      })
      // 성공 여부와 관계없이 다음 관리 작업을 허용한다
      .finally(() => {
        // 가입 신청 처리 중 상태를 종료한다
        setIsSubmitting(false);
      });
  };

  /**
   * 맞팔 회원 초대 선택 모달을 연다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleInviteOpen = (): void => {
    // 최신 후보 목록에서 초대 대상을 선택할 수 있도록 모달을 연다
    setIsInviteOpen(true);
  };

  /**
   * 맞팔 회원 초대 모달을 닫는다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleInviteClose = (): void => {
    // 초대 대상 선택 모달을 닫는다
    setIsInviteOpen(false);
  };

  /**
   * 선택한 맞팔 회원에게 모임 초대를 전송한다
   *
   * @author Hanwon.Jang
   * @param userNumb 초대할 사용자 번호
   * @return 반환값이 없다
   */
  const handleInviteSubmit = (userNumb: number): void => {
    // 다른 저장 작업 중이면 중복 초대 요청을 시작하지 않는다
    if (isSubmitting) {
      // 진행 중인 요청이 끝날 때까지 현재 동작을 종료한다
      return;
    }

    // 초대 요청의 중복 제출을 막기 위해 처리 상태를 시작한다
    setIsSubmitting(true);
    // 선택한 맞팔 회원 한 명에게 현재 모임 초대를 전송한다
    void inviteClubUsersApi(clubNumb, [userNumb])
      // 초대 결과를 모달과 보낸 초대 목록에 즉시 반영한다
      .then(async () => {
        // 초대된 회원이 보낸 초대로 이동한 최신 관리 데이터를 조회한다
        await getPageData();
      })
      // 초대 실패는 정원 안내를 포함한 공통 사용자 메시지로 표시한다
      .catch((error: unknown) => {
        // "회원을 초대하지 못했어요"
        void sweetError(
          message("frontend.readingClub.memberManage.inviteErrorTitle"),
          getApiErrorMessage(error, message("frontend.readingClub.error.inviteCapacity")),
        );
      })
      // 성공 여부와 관계없이 다음 관리 작업을 허용한다
      .finally(() => {
        // 회원 초대 처리 중 상태를 종료한다
        setIsSubmitting(false);
      });
  };

  /**
   * 활성 회원에게 보낸 유효한 모임 초대를 취소한다
   *
   * @author Hanwon.Jang
   * @param userNumb 초대 대상 사용자 번호
   * @return 반환값이 없다
   */
  const handleInviteCancel = (userNumb: number): void => {
    // 다른 저장 작업 중이면 중복 취소 요청을 시작하지 않는다
    if (isSubmitting) {
      // 진행 중인 요청이 끝날 때까지 현재 동작을 종료한다
      return;
    }

    // 초대 취소 요청의 중복 제출을 막는다
    setIsSubmitting(true);
    // 현재 모임에서 선택한 회원에게 보낸 초대를 취소한다
    void cancelSentClubInvitationApi(clubNumb, userNumb)
      // 취소한 회원을 다시 초대 후보에 반영한다
      .then(getPageData)
      // 취소 실패 원인을 공통 사용자 메시지로 안내한다
      .catch((error: unknown) => {
        // "보낸 초대를 취소하지 못했어요"
        void sweetError(
          message("frontend.readingClub.memberManage.cancelInviteErrorTitle"),
          getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
        );
      })
      // 성공 여부와 관계없이 다음 관리 작업을 허용한다
      .finally(() => {
        // 초대 취소 처리 중 상태를 종료한다
        setIsSubmitting(false);
      });
  };

  /**
   * 선택한 활성 일반 멤버의 퇴장 사유 입력 모달을 연다.
   *
   * @author HanWon.Jang
   * @param member 퇴장할 모임원
   * @return 반환값이 없다
   */
  const handleExitOpen = (member: ClubMemberProfile): void => {
    // 모임장이 아닌 일반 멤버만 퇴장 대상으로 선택한다
    if (member.membRole === "OWNER") {
      return;
    }
    // 이전 입력값을 지우고 선택한 멤버를 모달에 표시한다
    setExitReason("");
    setSelectedMember(member);
  };

  /**
   * 모임원 퇴장 모달을 닫고 입력한 사유를 초기화한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleExitClose = (): void => {
    // 제출 중에는 중복 동작으로 모달 상태가 사라지지 않게 한다
    if (isSubmitting) {
      return;
    }
    // 선택 대상과 사유를 함께 초기화한다
    setSelectedMember(null);
    setExitReason("");
  };

  /**
   * 모임원 퇴장 사유 입력값을 최대 허용 길이 안에서 반영한다.
   *
   * @author HanWon.Jang
   * @param value 사용자가 입력한 퇴장 사유
   * @return 반환값이 없다
   */
  const handleExitReasonChange = (value: string): void => {
    // 서버 DTO 제한과 같은 500자까지 입력 상태에 반영한다
    setExitReason(value.slice(0, 500));
  };

  /**
   * 선택한 활성 일반 멤버를 퇴장시키고 최신 관리 목록을 조회한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleMemberExit = (): void => {
    // 선택 대상과 공백을 제외한 필수 사유가 없거나 처리 중이면 요청하지 않는다
    const normalizedReason = exitReason.trim();
    if (!selectedMember || !normalizedReason || isSubmitting) {
      return;
    }

    // 중복 퇴장 요청을 막기 위해 제출 상태를 시작한다
    setIsSubmitting(true);
    // 선택한 모임원과 정규화한 퇴장 사유를 서버에 전달한다
    void exitClubMemberApi(clubNumb, selectedMember.userNumb, normalizedReason)
      .then(async () => {
        // 성공한 대상과 입력값을 지우고 최신 멤버 목록을 반영한다
        setSelectedMember(null);
        setExitReason("");
        await getPageData();
        // 최신 목록 반영 뒤 퇴장 완료를 사용자에게 안내한다
        await sweetSuccess(message("frontend.readingClub.memberManage.exitSuccessTitle"));
      })
      .catch((error: unknown) => {
        // 서버 정책 검증 실패 사유 또는 공통 재시도 안내를 표시한다
        void sweetError(
          message("frontend.readingClub.memberManage.exitErrorTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      })
      .finally(() => {
        // 다음 관리 작업이 가능하도록 제출 상태를 종료한다
        setIsSubmitting(false);
      });
  };

  // 화면 렌더링에 필요한 관리 상태와 이벤트 처리 함수를 반환한다
  return {
    applications,
    candidates,
    club,
    isInviteOpen,
    isLoading,
    isSubmitting,
    members,
    exitReason,
    sentInvitations,
    selectedApplication,
    selectedMember,
    handleAnswerClose,
    handleAnswerOpen,
    handleApplicationDecision,
    handleInviteCancel,
    handleInviteClose,
    handleInviteOpen,
    handleInviteSubmit,
    handleExitClose,
    handleExitOpen,
    handleExitReasonChange,
    handleMemberExit,
  };
};
