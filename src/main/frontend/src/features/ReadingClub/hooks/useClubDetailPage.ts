import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  cancelClubApplicationApi,
  delClubApi,
  decideClubApplicationApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getClubMemberListApi,
  getClubReadingGoalResultApi,
  joinClubApi,
  type ClubApplication,
  type ClubMemberProfile,
  type ClubReadingGoalResult,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

type ApplicationDecision = "APPROVED" | "REJECTED";

/**
 * 모임 상세 화면의 조회, 가입과 가입 승인 상태를 관리한다.
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
  const [applications, setApplications] = useState<ClubApplication[]>([]);
  const [members, setMembers] = useState<ClubMemberProfile[]>([]);
  const [readingGoalResult, setReadingGoalResult] = useState<ClubReadingGoalResult | null>(null);
  const [isCancellingApplication, setIsCancellingApplication] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const rejectedAlertClubRef = useRef<number | null>(null);

  /**
   * 모임 상세와 모임장 전용 관리 데이터를 조회한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const loadPage = useCallback(async (): Promise<void> => {
    const detail = await getClubDtlApi(clubNumb);
    const [nextMembers, nextApplications, nextReadingGoalResult] = await Promise.all([
      detail.membStat === "ACTIVE" ? getClubMemberListApi(clubNumb) : Promise.resolve([]),
      detail.membRole === "OWNER" ? getClubApplicationListApi(clubNumb) : Promise.resolve([]),
      detail.membStat === "ACTIVE" ? getClubReadingGoalResultApi(clubNumb) : Promise.resolve(null),
    ]);

    // 기존 답변을 유지하면서 서버 질문 수에 맞춘다.
    setAnswers((current) => detail.questionList?.map((_, index) => current[index] ?? "") ?? []);
    setClub(detail);
    setMembers(nextMembers);
    setApplications(nextApplications);
    setReadingGoalResult(nextReadingGoalResult);

    // 같은 상세 화면을 다시 조회하더라도 가입 거절 안내는 한 번만 표시한다
    if (detail.joinStat === "REJECTED" && rejectedAlertClubRef.current !== clubNumb) {
      // 현재 모임에서 안내한 상태를 기록해 중복 알림을 막는다
      rejectedAlertClubRef.current = clubNumb;
      // "모임 가입에 거절되었어요. 7일 후 가입 신청을 다시 할 수 있어요."
      void sweetWarning(message("frontend.readingClub.detail.rejectedApplication"));
    }
  }, [clubNumb]);

  /**
   * 상세 조회 오류를 공통 알림으로 표시한다.
   *
   * @author Hanwon.Jang
   * @param error 상세 조회 오류
   * @return 반환값이 없다
   */
  const handleLoadError = useCallback((error: unknown): void => {
    void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
    );
  }, []);

  /**
   * 유효한 모임 번호로 상세 조회를 시작한다.
   *
   * @author Hanwon.Jang
   * @return Effect 정리 함수가 없다
   */
  const initializePage = useCallback((): void => {
    // 잘못된 경로 매개변수로 AㅊPI를 호출하지 않는다.
    if (!Number.isFinite(clubNumb)) {
      return;
    }

    void loadPage().catch(handleLoadError);
  }, [clubNumb, handleLoadError, loadPage]);

  useEffect(initializePage, [initializePage]);

  /**
   * 가입 질문 답변을 변경한다.
   *
   * @author Hanwon.Jang
   * @param index 변경할 질문 순서
   * @param value 변경할 답변
   * @return 반환값이 없다
   */
  const handleAnswerChange = (index: number, value: string): void => {
    setAnswers((current) => current.map((answer, answerIndex) => (
      answerIndex === index ? value : answer
    )));
  };

  /**
   * 모임 가입 또는 가입 신청을 처리한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleJoinClub = (): void => {
    void joinClubApi(clubNumb, answers)
      .then(loadPage)
      .catch((error: unknown) => void sweetError(
        message("frontend.readingClub.error.joinTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      ));
  };

  /**
   * 가입 신청을 승인하거나 거절한다.
   *
   * @author Hanwon.Jang
   * @param applNumb 가입 신청 번호
   * @param joinStat 처리 상태
   * @return 반환값이 없다
   */
  const handleApplicationDecision = (applNumb: number, joinStat: ApplicationDecision): void => {
    void decideClubApplicationApi(clubNumb, applNumb, joinStat)
      .then(loadPage)
      .catch(handleLoadError);
  };

  /**
   * 현재 모임 독서의 독후감 편집 화면으로 이동한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleReportWrite = (): void => {
    // 현재 독서와 연결된 자동 생성 독후감이 없으면 일반 등록 화면을 유지한다
    if (!Number.isFinite(club?.currentReportNumb)) {
      // 선택할 도서가 없는 일반 독후감 등록 화면으로 이동한다
      navigate("/report/set");
      // 현재 독서 독후감 편집 화면 이동을 중단한다
      return;
    }

    // 현재 모임 독서의 책과 목표 기간이 설정된 기존 독후감을 바로 편집한다
    navigate(`/report/detail/${club?.currentReportNumb}`, {
      state: { startEditing: true },
    });
  };

  /**
   * 가입 승인 전 자신의 처리 대기 신청을 확인 후 취소한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleApplicationCancel = async (): Promise<void> => {
    // "가입 신청을 취소할까요?"
    const confirmResult = await sweetConfirm({
      icon: "warning",
      title: message("frontend.readingClub.detail.cancelApplicationConfirmTitle"),
      text: message("frontend.readingClub.detail.cancelApplicationConfirmDescription"),
      confirmButtonText: message("frontend.readingClub.detail.cancelApplicationButton"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 확인하지 않았거나 이미 처리 중이면 현재 신청을 유지한다
    if (!confirmResult.isConfirmed || isCancellingApplication) {
      return;
    }

    setIsCancellingApplication(true);

    try {
      /**
       * 현재 사용자의 처리 대기 가입 신청을 서버에서 삭제한다.
       *
       * @author HanWon.Jang
       * @return 가입 신청 취소 완료 Promise
       * @throws 가입 신청 취소 또는 응답 검증에 실패하면 발생한다
       */
      const cancelApplication = (): ReturnType<typeof cancelClubApplicationApi> => (
        cancelClubApplicationApi(clubNumb)
      );

      // 취소가 끝날 때까지 화면 이동을 막고 완료 안내를 표시한다
      await runBlockingOperation(cancelApplication, {
        title: message("frontend.readingClub.detail.cancellingApplication"),
        success: {
          title: message("frontend.readingClub.detail.cancelApplicationSuccessTitle"),
          text: message("frontend.readingClub.detail.cancelApplicationSuccessDescription"),
        },
      });
      // 최신 상세를 다시 조회해 가입 신청 화면으로 전환한다
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
  * 현재 모임의 가입 이전을 포함한 이전 독서 기록 화면으로 이동한다.
  *
  * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleReadingHistory = (): void => {
    // 현재 활성 모임원 권한으로 전체 종료 회차 목록 경로로 이동한다
    navigate(`/reading-clubs/history/${clubNumb}`);
  };

  /**
   * 현재 모임의 수정 화면으로 이동한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleClubEdit = (): void => {
    // 현재 모임 번호를 유지하여 모임 수정 화면으로 이동한다
    navigate(`/reading-clubs/update/${clubNumb}`);
  };

  /**
   * 모임 삭제 확인 후 복구할 수 없는 삭제 요청을 처리한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleClubDelete = async (): Promise<void> => {
    // "모임을 삭제할까요?"
    const confirmResult = await sweetConfirm({
      icon: "warning",
      title: message("frontend.readingClub.detail.deleteConfirmTitle"),
      text: message("frontend.readingClub.detail.deleteConfirmDescription"),
      confirmButtonText: /* "삭제하기" */ message("frontend.common.delete"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 취소하면 현재 모임과 화면을 유지한다
    if (!confirmResult.isConfirmed) {
      return;
    }

    // 중복 삭제 요청을 막는다
    setIsDeleting(true);

    try {
      /**
       * 현재 모임을 서버에서 삭제한다
       *
       * @author SeungHyeon.Kang
       * @return 모임 삭제 완료 Promise
       * @throws 모임 삭제 또는 응답 검증에 실패하면 발생한다
       */
      const deleteClub = (): ReturnType<typeof delClubApi> => {
        // 사용자에게 삭제 확인을 받은 현재 모임 번호를 서버에 전달한다
        return delClubApi(clubNumb);
      };

      // 삭제가 끝날 때까지 화면 이동을 차단하고 처리 중 알림을 표시한다
      await runBlockingOperation(deleteClub, {
        title: message("frontend.readingClub.detail.deleting"),
        success: {
          // "모임을 삭제했어요"
          title: message("frontend.readingClub.detail.deleteSuccessTitle"),
          // "개인 독후감은 그대로 보관돼요."
          text: message("frontend.readingClub.detail.deleteSuccessDescription"),
        },
      });
      // 삭제된 상세 화면을 남기지 않고 내 모임 목록으로 이동한다
      navigate("/reading-clubs/mine", { replace: true });
    } catch (error) {
      // "모임을 삭제하지 못했어요"
      void sweetError(
        message("frontend.readingClub.detail.deleteErrorTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.detail.deleteErrorDescription")),
      );
    } finally {
      // 삭제 요청이 끝나면 더보기 메뉴 동작을 다시 허용한다
      setIsDeleting(false);
    }
  };

  /**
   * 더보기 메뉴에서 선택한 모임 관리 동작을 실행한다.
   *
   * @author Hanwon.Jang
   * @param action 선택한 관리 동작
   * @return 반환값이 없다
   */
  const handleClubAction = (action: "" | "UPDATE" | "DELETE"): void => {
    // 수정하기를 선택하면 수정 화면으로 이동한다
    if (action === "UPDATE") {
      handleClubEdit();
      return;
    }

    // 삭제하기를 선택하면 확인 절차를 시작한다
    if (action === "DELETE") {
      void handleClubDelete();
    }
  };

  const canJoin = Boolean(
    club
    && !club.membStat
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
    members,
    readingGoalResult,
    handleAnswerChange,
    handleApplicationCancel,
    handleApplicationDecision,
    handleClubAction,
    handleJoinClub,
    handleReadingHistory,
    handleReportWrite,
  };
};
