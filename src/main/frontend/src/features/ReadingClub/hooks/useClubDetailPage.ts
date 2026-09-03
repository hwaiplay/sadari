import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  cancelClubApplicationApi,
  completeClubReadingApi,
  delClubApi,
  decideClubApplicationApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getClubMemberListApi,
  getReadingGoalResultApi,
  getOwnerElectionApi,
  joinClubApi,
  uptReadingResultApi,
  updateOwnerVoteApi, delMembershipApi,
} from "@/features/ReadingClub/api/readingClubApi";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ClubApplication,
  ClubMemberProfile,
  ClubReadingGoalResult, OwnerElection,
  ReadingClub
} from "@/features/ReadingClub/types/club.type.ts";

type ApplicationDecision = "APPROVED" | "REJECTED";

/**
 * 가입 답변의 앞뒤 공백을 제거함
 *
 * @author HanWon.Jang
 * @param answer 정리할 가입 답변
 * @return 앞뒤 공백을 제거한 답변
 */
const trimAnswer = (answer: string): string => {
  // 서버에 저장할 답변 원문에서 의미 없는 앞뒤 공백만 제거함
  return answer.trim();
};

/**
 * 가입 답변이 비어 있는지 확인함
 *
 * @author HanWon.Jang
 * @param answer 확인할 가입 답변
 * @return 답변이 비어 있으면 true
 */
const isEmptyAnswer = (answer: string): boolean => {
  // 필수 답변 검증 결과를 반환함
  return answer.length === 0;
};

/**
 * 모임 상세 화면의 조회, 가입과 가입 승인 상태를 관리함
 *
 * @author Hanwon.Jang
 * @return 모임 상세 화면 상태와 이벤트 처리 함수
 */
export const useClubDetailPage = () => {
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const [club, setClub] = useState<ReadingClub | null>(null);
  const [answers, setAnswers] = useState<string[]>([]);
  const [isJoinModalOpen, setIsJoinModalOpen] = useState(false);
  const [applications, setApplications] = useState<ClubApplication[]>([]);
  const [members, setMembers] = useState<ClubMemberProfile[]>([]);
  const [readingGoalResult, setReadingGoalResult] = useState<ClubReadingGoalResult | null>(null);
  const [ownerElection, setOwnerElection] = useState<OwnerElection | null>(null);
  const [isVotingOwner, setIsVotingOwner] = useState(false);
  const [isCancellingApplication, setIsCancellingApplication] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);
  const [isCompletingReading, setIsCompletingReading] = useState(false);
  const [isClosingResult, setIsClosingResult] = useState(false);
  const rejectedAlertClubRef = useRef<number | null>(null);

  /**
   * 모임 상세 정보와 모임장 전용 관리 데이터를 조회
   *
   * @author Hanwon.Jang
   * @return
   */
  const loadPage = useCallback(async (): Promise<void> => {
    // 모임 상세보기 조회
    const detail = await getClubDtlApi(clubNumb);
    // 활성 모임원과 공개 중인 활성 모임 조회자에게 요약 정보를 제공함
    const canViewOverview = detail.membStat === "ACTIVE"
      || (detail.clubVisb === "PUBLIC" && detail.clubStat === "ACTIVE");

    const [nextMembers, nextApplications, nextReadingGoalResult, nextOwnerElection] = await Promise.all([
      canViewOverview ? getClubMemberListApi(clubNumb) : Promise.resolve([]),
      detail.membRole === "OWNER" ? getClubApplicationListApi(clubNumb) : Promise.resolve([]),
      detail.membStat === "ACTIVE" ? getReadingGoalResultApi(clubNumb) : Promise.resolve(null),
      detail.membStat === "ACTIVE" && detail.clubStat === "OWNER_ELECTION"
        ? getOwnerElectionApi(clubNumb)
        : Promise.resolve(null),
    ]);

    // 기존 답변을 유지하면서 서버 질문 수에 맞춤
    setAnswers((current) => detail.questionList?.map((_, index) => current[index] ?? "") ?? []);
    setClub(detail);
    setMembers(nextMembers);
    setApplications(nextApplications);
    setReadingGoalResult(nextReadingGoalResult);
    setOwnerElection(nextOwnerElection);

    // 같은 상세 화면을 다시 조회하더라도 가입 거절 안내는 한 번만 표시
    if (detail.joinStat === "REJECTED" && rejectedAlertClubRef.current !== clubNumb) {
      // 현재 모임에서 안내한 상태를 기록해 중복 알림을 막음
      rejectedAlertClubRef.current = clubNumb;
      // "모임 가입에 거절되었어요. 7일 후 가입 신청을 다시 할 수 있어요."
      void sweetWarning(message("frontend.readingClub.detail.rejectedApplication"));
    }
  }, [clubNumb]);

  /**
   * 상세 조회 오류를 공통 알림으로 표시
   *
   * @author Hanwon.Jang
   * @param error 상세 조회 오류
   * @return
   */
  const handleLoadError = useCallback((error: unknown): void => {
    void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
    );
  }, []);

  /**
   * 유효한 모임 번호로 상세 조회
   *
   * @author Hanwon.Jang
   * @return
   */
  const initializePage = useCallback((): void => {
    // 잘못된 경로 매개변수로 API를 호출하지 않게 함
    if (!Number.isFinite(clubNumb)) {
      return;
    }

    void loadPage().catch(handleLoadError);
  }, [clubNumb, handleLoadError, loadPage]);

  useEffect(initializePage, [initializePage]);

  /**
   * 가입 질문 답변을 변경
   *
   * @author Hanwon.Jang
   * @param index 변경할 질문 순서
   * @param value 변경할 답변
   */
  const handleAnswerChange = (index: number, value: string): void => {
    setAnswers((current) => current.map((answer, answerIndex) => (
      answerIndex === index ? value : answer
    )));
  };

  /**
   * 모임 가입 방식에 맞는 가입 동작을 시작함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleJoinAction = (): void => {
    // 승인제 모임은 질문 답변을 먼저 작성하도록 가입 신청 모달을 엶
    if (club?.joinType === "APPROVAL") {
      // 현재 모임의 가입 질문 모달을 표시함
      setIsJoinModalOpen(true);
      return;
    }

    // 즉시 가입 모임은 기존 가입 요청을 바로 실행함
    void handleJoinClub();
  };

  /**
   * 승인제 모임 가입 신청 모달을 닫음
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleJoinModalClose = (): void => {
    // 작성 중인 답변은 유지하고 가입 신청 모달만 닫음
    setIsJoinModalOpen(false);
  };

  /**
   * 모임 가입 또는 가입 신청을 처리
   *
   * @author Hanwon.Jang
   */
  const handleJoinClub = async (): Promise<void> => {
    // 즉시 가입형만 가입 완료 성공 안내를 표시
    const isOpenJoin = club?.joinType === "OPEN";
    // 승인제 가입 요청에는 공백을 제거한 답변을 사용함
    const normalizedAnswers = answers.map(trimAnswer);
    const questionCount = club?.questionList?.length ?? 0;

    // 승인 질문이 없거나 모든 질문에 답하지 않으면 신청을 보내지 않음
    if (!isOpenJoin && (questionCount === 0 || normalizedAnswers.length !== questionCount
        || normalizedAnswers.some(isEmptyAnswer))) {
      // "모든 질문에 답변해 주세요."
      void sweetWarning(message("frontend.readingClub.detail.answerRequired"));
      return;
    }

    try {
      // 가입 처리가 성공하면 가입 방식에 맞는 완료 안내로 같은 모달을 전환함
      await runBlockingOperation(() => joinClubApi(clubNumb, normalizedAnswers), {
        title: message(
          isOpenJoin
            ? "frontend.readingClub.detail.joining"
            : "frontend.readingClub.detail.applying",
        ),
        success: {
          title: isOpenJoin
            ? /* "모임에 가입했어요" */ message("frontend.readingClub.detail.joinSuccessTitle")
            : /* "가입을 신청했어요" */ message("frontend.readingClub.detail.applicationSuccessTitle"),
          text: isOpenJoin
            ? /* "이제 모임원들과 함께 책을 읽어보세요." */
              message("frontend.readingClub.detail.joinSuccessDescription")
            : /* "모임장이 답변을 확인하면 승인 결과를 알려드릴게요." */
              message("frontend.readingClub.detail.applicationSuccessDescription"),
        },
      });

      // 사용자가 성공 안내를 확인한 뒤 활성 모임원 상태로 상세 화면을 갱신
      await loadPage();
      // 승인 신청이 완료되면 답변 모달을 닫고 대기 상태를 표시함
      setIsJoinModalOpen(false);

    } catch (error) {
      // 가입 또는 가입 신청 실패 원인 공통 alert 표시
      void sweetError(
        message("frontend.readingClub.error.joinTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    }
  };

  /**
   * 선택한 후보에게 투표한 뒤 최신 선거 정보를 다시 조회
   *
   * @author HanWon.Jang
   * @param userNumb 선택한 후보 사용자 번호
   * @return 투표 성공 여부 Promise
   */
  const handleOwnerVote = async (userNumb: number): Promise<boolean> => {
    // 중복 클릭으로 같은 투표 요청이 동시에 실행되지 않게 함
    if (isVotingOwner) {
      return false;
    }

    setIsVotingOwner(true);

    try {
      // 서버에서 유권자와 후보 자격을 재검증한 투표를 등록
      await updateOwnerVoteApi(clubNumb, userNumb);
      // 변경된 내 선택 상태를 포함한 선거 정보를 다시 조회
      setOwnerElection(await getOwnerElectionApi(clubNumb));
      // 모달을 닫을 수 있도록 투표 성공 여부를 반환
      return true;

    } catch (error) {
      void sweetError(
        message("frontend.readingClub.ownerElection.errorTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );

      // 오류 안내 뒤 현재 모달을 유지하도록 실패 여부를 반환
      return false;

    } finally {
      setIsVotingOwner(false);
    }
  };

  /**
   * 가입 신청 승인/거절 처리
   *
   * @author Hanwon.Jang
   * @param applNumb 가입 신청 번호
   * @param joinStat 처리 상태
   */
  const handleApplicationDecision = (applNumb: number, joinStat: ApplicationDecision): void => {
    void decideClubApplicationApi(clubNumb, applNumb, joinStat)
      .then(loadPage)
      .catch(handleLoadError);
  };

  /**
   * 현재 모임 독서의 독후감 편집 화면으로 이동
   *
   * @author Hanwon.Jang
   */
  const handleReportWrite = (): void => {
    // 현재 독서와 연결된 자동 생성 독후감이 없으면 일반 등록 화면을 유지
    if (!Number.isFinite(club?.currentReportNumb)) {
      // 선택할 도서가 없는 일반 독후감 등록 화면으로 이동
      navigate("/report/set");
      return;
    }

    // 현재 모임 독서의 책과 목표 기간이 설정된 기존 독후감을 바로 편집
    navigate(`/report/detail/${club?.currentReportNumb}`, {
      state: { startEditing: true },
    });
  };

  /**
   * 활성 모임장의 현재 회차 조기 마감을 확인하고 완료 결과를 표시함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleReadingComplete = async (): Promise<void> => {
    const rondNumb = club?.currentRondNumb;
    // 화면 노출과 별개로 모임장과 회차 및 중복 요청 조건을 다시 확인함
    if (isCompletingReading || club?.membRole !== "OWNER"
        || typeof rondNumb !== "number" || !Number.isFinite(rondNumb)) {
      return;
    }

    const confirmResult = await sweetConfirm({
      icon: "warning",
      // "독서를 미리 마감할까요?"
      title: message("frontend.readingClub.detail.earlyCloseConfirmTitle"),
      // "모든 모임원이 독서를 완료했어요. 마감하면 이번 회차의 목표 결과가 확정돼요."
      text: message("frontend.readingClub.detail.earlyCloseConfirmDescription"),
      // "독서 마감하기"
      confirmButtonText: message("frontend.readingClub.detail.earlyCloseConfirmButton"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 취소하면 현재 진행 회차를 그대로 유지함
    if (!confirmResult.isConfirmed) {
      return;
    }

    setIsCompletingReading(true);

    try {
      // 서버에서 조기 마감 조건을 다시 검증하고 모든 모임원의 미확인 결과를 생성함
      await completeClubReadingApi(clubNumb, rondNumb);
      // 현재 회차와 모든 모임원에게 생성된 미확인 결과를 최신 상세에 반영함
      await loadPage();
    } catch (error) {
      void sweetError(
        /* "독서를 마감하지 못했어요" */ message("frontend.readingClub.detail.earlyCloseErrorTitle"),
        getApiErrorMessage(
          error,
          /* "모임원들의 완료 상태를 확인한 뒤 다시 시도해 주세요." */
          message("frontend.readingClub.detail.earlyCloseErrorDescription"),
        ),
      );
    } finally {
      // 요청이 끝나면 조기 마감 버튼을 다시 사용할 수 있게 함
      setIsCompletingReading(false);
    }
  };

  /**
   * 사용자가 직접 닫은 독서 목표 결과를 서버에 확인 처리함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleResultClose = async (): Promise<void> => {
    // 미확인 결과가 없거나 확인 저장 중이면 중복 요청을 만들지 않음
    if (isClosingResult || !readingGoalResult) {
      return;
    }

    // 확인 저장이 끝날 때까지 팝업의 두 닫기 명령을 비활성화함
    setIsClosingResult(true);

    try {
      // 현재 표시 중인 회차만 확인 처리하여 이후 회차의 미확인 결과를 보존함
      await uptReadingResultApi(clubNumb, readingGoalResult.rondNumb);
      // 서버 확인이 성공한 뒤에만 현재 결과 팝업을 닫음
      setReadingGoalResult(null);
    }

    catch (error) {
      // "처리하지 못했어요"
      const errorTitle = message("frontend.alert.errorTitle");
      // "다시 시도해주세요."
      const fallbackMessage = message("frontend.common.tryAgain");
      // 확인 저장에 실패하면 팝업을 유지하고 안전한 오류 문구를 표시함
      await sweetError(errorTitle, getApiErrorMessage(error, fallbackMessage));
    }

    finally {
      // 성공 또는 실패가 확정되면 닫기 명령을 다시 사용할 수 있게 함
      setIsClosingResult(false);
    }
  };

  /**
   * 가입 승인 전 자신의 처리 대기 신청을 확인 후 취소
   * @author HanWon.Jang
   */
  const handleApplicationCancel = async (): Promise<void> => {

    /**
     * 가입 신청 취소 확인 alert
     * "가입 신청을 취소할까요?"
     */
    const confirmResult = await sweetConfirm({
      icon: "warning",
      title: message("frontend.readingClub.detail.cancelApplicationConfirmTitle"),
      text: message("frontend.readingClub.detail.cancelApplicationConfirmDescription"),
      confirmButtonText: message("frontend.readingClub.detail.cancelApplicationButton"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 확인하지 않았거나 이미 처리 중이면 현재 신청을 유지함
    if (!confirmResult.isConfirmed || isCancellingApplication) {
      return;
    }

    setIsCancellingApplication(true);

    try {

      // 현재 사용자의 처리 대기 가입 신청을 서버에서 삭제
      const cancelApplication = (): ReturnType<typeof cancelClubApplicationApi> => (
        cancelClubApplicationApi(clubNumb)
      );

      // 취소가 끝날 때까지 화면 이동을 막고 완료 안내를 표시
      await runBlockingOperation(cancelApplication, {
        title: message("frontend.readingClub.detail.cancellingApplication"),
        success: {
          title: message("frontend.readingClub.detail.cancelApplicationSuccessTitle"),
          text: message("frontend.readingClub.detail.cancelApplicationSuccessDescription"),
        },
      });

      // 최신 상세를 다시 조회해 가입 신청 화면으로 전환
      await loadPage();

    } catch (error) {
      void sweetError(
        message("frontend.readingClub.detail.cancelApplicationErrorTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.detail.cancelApplicationErrorDescription")),
      );
    } finally {
      setIsCancellingApplication(false);
    }
  };

  /**
   * 이전 독서 회차 목록 페이지로 이동
   */
  const handleReadingHistory = (): void => {
    navigate(`/reading-clubs/history/${clubNumb}`);
  };

  /**
   * 활성 일반 모임원의 자진 탈퇴
   *
   * @author HanWon.Jang
   * @return
   */
  const handleClubLeave = async (): Promise<void> => {
    // 활성 일반 모임원만 탈퇴 확인 절차를 시작하고 중복 요청을 막음
    if (isLeaving || club?.membStat !== "ACTIVE" || club.membRole !== "MEMBER") {
      return;
    }

    // "모임을 탈퇴할까요?"
    const confirmResult = await sweetConfirm({
      icon: "warning",
      title: message("frontend.readingClub.detail.leaveConfirmTitle"),
      // "탈퇴하면 도서 투표와 모임원 독후감 등 모든 모임 활동 기록이 삭제돼요. 개인 독후감은 내 독후감 목록에 그대로 남아요."
      text: message("frontend.readingClub.detail.leaveConfirmDescription"),
      // "탈퇴하기"
      confirmButtonText: message("frontend.readingClub.detail.leaveButton"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자 취소
    if (!confirmResult.isConfirmed) {
      return;
    }

    // 탈퇴 처리 중 같은 요청을 다시 시작하지 못하게 함
    setIsLeaving(true);

    try {
      const leaveClub = (): ReturnType<typeof delMembershipApi> => {
        // 사용자에게 확인받은 현재 모임 번호를 서버에 전달
        return delMembershipApi(clubNumb);
      };

      // 탈퇴 처리가 끝날 때까지 화면 이동을 차단하고 같은 모달에서 완료를 안내
      await runBlockingOperation(leaveClub, {
        // "모임 탈퇴 중"
        title: message("frontend.readingClub.detail.leaving"),
        success: {
          // "모임을 탈퇴했어요"
          title: message("frontend.readingClub.detail.leaveSuccessTitle"),
          // "개인 독후감은 내 독후감 목록에 그대로 남아 있어요."
          text: message("frontend.readingClub.detail.leaveSuccessDescription"),
        },
      });

      // 성공 안내를 확인한 뒤 탈퇴한 모임을 제외한 내 모임 목록으로 이동
      navigate("/reading-clubs/mine", { replace: true });

    } catch (error) {
      // "모임을 탈퇴하지 못했어요"
      void sweetError(
        message("frontend.readingClub.detail.leaveErrorTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.detail.leaveErrorDescription")),
      );

    } finally {
      // 성공 또는 실패가 확정되면 탈퇴 버튼의 처리 중 상태를 해제
      setIsLeaving(false);
    }
  };

  /**
   * 모임 수정 페이지로 이동
   */
  const handleClubEdit = (): void => {
    navigate(`/reading-clubs/update/${clubNumb}`);
  };

  /**
   * 모임 삭제
   */
  const handleClubDelete = async (): Promise<void> => {

    // 모임 삭제 확인 alert
    const confirmResult = await sweetConfirm({
      icon: "warning",
      title: message("frontend.readingClub.detail.deleteConfirmTitle"),
      text: message("frontend.readingClub.detail.deleteConfirmDescription"),
      confirmButtonText: /* "삭제하기" */ message("frontend.common.delete"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    if (!confirmResult.isConfirmed) {
      return;
    }

    // 중복 삭제 요청을 막음
    setIsDeleting(true);

    try {

      // 현재 모임을 서버에서 삭제함
      const deleteClub = (): ReturnType<typeof delClubApi> => {
        // 사용자에게 삭제 확인을 받은 현재 모임 번호를 서버에 전달
        return delClubApi(clubNumb);
      };

      // 삭제가 끝날 때까지 화면 이동을 차단하고 처리 중 알림을 표시
      await runBlockingOperation(deleteClub, {
        title: message("frontend.readingClub.detail.deleting"),
        success: {
          // "모임을 삭제했어요"
          title: message("frontend.readingClub.detail.deleteSuccessTitle"),
          // "개인 독후감은 그대로 보관돼요."
          text: message("frontend.readingClub.detail.deleteSuccessDescription"),
        },
      });

      // 삭제된 상세 화면을 남기지 않고 내 모임 목록으로 이동
      navigate("/reading-clubs/mine", { replace: true });

    } catch (error) {

      // "모임을 삭제하지 못했어요"
      void sweetError(
        message("frontend.readingClub.detail.deleteErrorTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.detail.deleteErrorDescription")),
      );

    } finally {
      // 삭제 요청이 끝나면 더보기 메뉴 동작을 다시 허용
      setIsDeleting(false);
    }
  };

  /**
   * 더보기 메뉴에서 선택한 모임 관리 동작을 실행
   *
   * @author Hanwon.Jang
   * @param action 선택한 관리 동작
   */
  const handleClubAction = (action: "" | "UPDATE" | "DELETE"): void => {
    // 수정하기를 선택하면 수정 화면으로 이동
    if (action === "UPDATE") {
      handleClubEdit();
      return;
    }

    // 삭제하기를 선택하면 확인 절차를 시작
    if (action === "DELETE") {
      void handleClubDelete();
    }
  };

  // 가입 이력이 없거나 자진 탈퇴한 공개 모임은 가입 절차를 다시 제공함
  const canJoin = Boolean(
    club
    && (!club.membStat || club.membStat === "EXITED")
    && !club.joinStat
    && club.clubVisb === "PUBLIC"
    && club.joinType !== "INVITE",
  );

  return {
    answers,
    applications,
    canJoin,
    club,
    isCancellingApplication,
    isDeleting,
    isJoinModalOpen,
    isLeaving,
    isCompletingReading,
    isClosingResult,
    isVotingOwner,
    members,
    ownerElection,
    readingGoalResult,
    handleAnswerChange,
    handleApplicationCancel,
    handleApplicationDecision,
    handleClubAction,
    handleClubLeave,
    handleJoinClub,
    handleJoinAction,
    handleJoinModalClose,
    handleOwnerVote,
    handleReadingHistory,
    handleReadingComplete,
    handleResultClose,
    handleReportWrite,
  };
};
