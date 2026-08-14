import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  decideClubApplicationApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getClubMemberListApi,
  getInviteCandidateListApi,
  inviteClubUsersApi,
  joinClubApi,
  type ClubApplication,
  type ClubMemberProfile,
  type InviteCandidate,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

type ApplicationDecision = "APPROVED" | "REJECTED";

/**
 * 모임 상세 화면의 조회, 가입, 초대와 가입 승인 상태를 관리한다.
 *
 * @author SeungHyeon.Kang
 * @return 모임 상세 화면 상태와 이벤트 처리 함수
 */
export const useClubDetailPage = () => {
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const [club, setClub] = useState<ReadingClub | null>(null);
  const [answers, setAnswers] = useState<string[]>([]);
  const [candidates, setCandidates] = useState<InviteCandidate[]>([]);
  const [selectedCandidates, setSelectedCandidates] = useState<Set<number>>(new Set());
  const [applications, setApplications] = useState<ClubApplication[]>([]);
  const [members, setMembers] = useState<ClubMemberProfile[]>([]);

  /**
   * 모임 상세와 모임장 전용 관리 데이터를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const loadPage = useCallback(async (): Promise<void> => {
    const detail = await getClubDtlApi(clubNumb);
    const [nextMembers, nextCandidates, nextApplications] = await Promise.all([
      detail.membStat === "ACTIVE" ? getClubMemberListApi(clubNumb) : Promise.resolve([]),
      detail.membRole === "OWNER" ? getInviteCandidateListApi(clubNumb) : Promise.resolve([]),
      detail.membRole === "OWNER" ? getClubApplicationListApi(clubNumb) : Promise.resolve([]),
    ]);

    // 기존 답변을 유지하면서 서버 질문 수에 맞춘다.
    setAnswers((current) => detail.questionList?.map((_, index) => current[index] ?? "") ?? []);
    setClub(detail);
    setMembers(nextMembers);
    setCandidates(nextCandidates);
    setApplications(nextApplications);
  }, [clubNumb]);

  /**
   * 상세 조회 오류를 공통 알림으로 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param error 상세 조회 오류
   * @return 반환값이 없다
   */
  const handleLoadError = useCallback((error: unknown): void => {
    void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
    );
  }, []);

  /**
   * 유효한 모임 번호로 상세 조회를 시작한다.
   *
   * @author SeungHyeon.Kang
   * @return Effect 정리 함수가 없다
   */
  const initializePage = useCallback((): void => {
    // 잘못된 경로 매개변수로 API를 호출하지 않는다.
    if (!Number.isFinite(clubNumb)) {
      return;
    }

    void loadPage().catch(handleLoadError);
  }, [clubNumb, handleLoadError, loadPage]);

  useEffect(initializePage, [initializePage]);

  /**
   * 가입 질문 답변을 변경한다.
   *
   * @author SeungHyeon.Kang
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
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleJoinClub = (): void => {
    void joinClubApi(clubNumb, answers)
      .then(loadPage)
      .catch((error: unknown) => void sweetError(
        message("frontend.readingClub.error.joinTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
      ));
  };

  /**
   * 초대 후보의 선택 여부를 전환한다.
   *
   * @author SeungHyeon.Kang
   * @param userNumb 사용자 번호
   * @return 반환값이 없다
   */
  const handleCandidateToggle = (userNumb: number): void => {
    setSelectedCandidates((current) => {
      const next = new Set(current);

      // 이미 선택된 사용자는 해제하고 그렇지 않으면 추가한다.
      if (next.has(userNumb)) {
        next.delete(userNumb);
      } else {
        next.add(userNumb);
      }

      return next;
    });
  };

  /**
   * 선택한 사용자에게 모임 초대를 보낸다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleInviteCandidates = (): void => {
    void inviteClubUsersApi(clubNumb, Array.from(selectedCandidates))
      .then(() => {
        setSelectedCandidates(new Set());
        return loadPage();
      })
      .catch((error: unknown) => void sweetError(
        message("frontend.readingClub.error.inviteTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.error.inviteCapacity")),
      ));
  };

  /**
   * 가입 신청을 승인하거나 거절한다.
   *
   * @author SeungHyeon.Kang
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
   * 독후감 작성 화면으로 이동한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReportWrite = (): void => {
    navigate("/report/set");
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
    candidates,
    canJoin,
    club,
    members,
    selectedCandidates,
    handleAnswerChange,
    handleApplicationDecision,
    handleCandidateToggle,
    handleInviteCandidates,
    handleJoinClub,
    handleReportWrite,
  };
};
