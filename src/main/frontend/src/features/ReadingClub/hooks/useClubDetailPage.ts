import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError, sweetSuccess } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  delClubApi,
  decideClubApplicationApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getClubMemberListApi,
  joinClubApi,
  type ClubApplication,
  type ClubMemberProfile,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useCallback, useEffect, useState } from "react";
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
  const [isDeleting, setIsDeleting] = useState(false);

  /**
   * 모임 상세와 모임장 전용 관리 데이터를 조회한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const loadPage = useCallback(async (): Promise<void> => {
    const detail = await getClubDtlApi(clubNumb);
    const [nextMembers, nextApplications] = await Promise.all([
      detail.membStat === "ACTIVE" ? getClubMemberListApi(clubNumb) : Promise.resolve([]),
      detail.membRole === "OWNER" ? getClubApplicationListApi(clubNumb) : Promise.resolve([]),
    ]);

    // 기존 답변을 유지하면서 서버 질문 수에 맞춘다.
    setAnswers((current) => detail.questionList?.map((_, index) => current[index] ?? "") ?? []);
    setClub(detail);
    setMembers(nextMembers);
    setApplications(nextApplications);
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
   * 현재 모임의 수정 화면으로 이동한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleClubEdit = (): void => {
    // 현재 모임 번호를 유지하여 모임 수정 화면으로 이동한다
    navigate(`/reading-clubs/${clubNumb}/edit`);
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
      // 삭제가 끝날 때까지 화면 이동을 차단하고 처리 중 알림을 표시한다
      await runBlockingOperation(() => delClubApi(clubNumb), {
        title: message("frontend.readingClub.detail.deleting"),
      });
      // "모임을 삭제했어요"
      await sweetSuccess(
        message("frontend.readingClub.detail.deleteSuccessTitle"),
        message("frontend.readingClub.detail.deleteSuccessDescription"),
      );
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
  const handleClubAction = (action: "" | "EDIT" | "DELETE"): void => {
    // 수정하기를 선택하면 수정 화면으로 이동한다
    if (action === "EDIT") {
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
    isDeleting,
    members,
    handleAnswerChange,
    handleApplicationDecision,
    handleClubAction,
    handleJoinClub,
    handleReportWrite,
  };
};
