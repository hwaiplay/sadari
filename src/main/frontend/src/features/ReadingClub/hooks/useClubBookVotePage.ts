import {getApiErrorMessage} from "@/app/api/resultData";
import {sweetConfirm, sweetError} from "@/app/lib/sweetAlert/sweetAlert";
import {message} from "@/app/messages/message";
import {runBlockingOperation} from "@/app/navigation/blockingOperation";
import type {BookSearchResultType} from "@/features/Book/types/book.type";
import {
  createClubBookRecommApi,
  deleteClubBookRecommApi,
  getClubBookRecommApi,
  updateClubBookVoteApi,
} from "@/features/ReadingClub/api/readingClubApi";
import {useCallback, useEffect, useMemo, useState} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import {ClubBookRecommendation, ClubBookVotePage} from "@/features/ReadingClub/types/ClubTypes.ts";

type VotePageState = {recommendedBook?: BookSearchResultType};

/**
 * 모임 다음 도서 추천과 투표 화면의 상태와 기능을 관리한다.
 *
 * @author HanWon.Jang
 * @return 다음 도서 추천과 투표 화면 상태 및 이벤트 처리 함수
 */
export const useClubBookVotePage = () => {

  const navigate = useNavigate();
  const location = useLocation();
  const {clubNumb: clubNumbParam} = useParams();
  const clubNumb = Number(clubNumbParam);
  const pageState = (location.state ?? {}) as VotePageState;
  const [candidates, setCandidates] = useState<ClubBookRecommendation[]>([]);
  const [selectedRecommendation, setSelectedRecommendation] = useState<number | null>(null);
  const [votePage, setVotePage] = useState<ClubBookVotePage | null>(null);

  /**
   * 모임의 다음 도서 추천 후보와 현재 투표 상태를 조회한다.
   *
   * @author HanWon.Jang
   * @return 추천 후보 조회가 끝나면 완료되는 Promise
   */
  const loadCandidates = useCallback(async (): Promise<void> => {

    // 유효한 모임 번호로만 추천 목록을 조회한다.
    if (!Number.isSafeInteger(clubNumb) || clubNumb <= 0) {
      // 잘못된 경로의 목록 조회를 종료한다.
      return;
    }

    try {
      // 서버에서 최신 추천 후보와 투표 상태를 조회한다.
      const votePageData = await getClubBookRecommApi(clubNumb);
      const recommendationList = votePageData.candidateList ?? [];
      // 최신 추천 후보 목록을 화면 상태에 반영한다.
      setCandidates(recommendationList);
      // 서버가 반환한 현재 사용자의 투표 후보를 선택 상태에 반영한다.
      setSelectedRecommendation(recommendationList.find((candidate) => candidate.voteYsno === "Y")?.recmNumb ?? null);
      // 추천과 투표 가능 여부를 화면 상태에 반영한다.
      setVotePage(votePageData);
    } catch (error) {
      // "오류"
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  }, [clubNumb]);

  useEffect(() => {
    // 페이지 진입 시 최신 추천 후보와 투표 상태를 조회한다.
    void loadCandidates();
  }, [loadCandidates]);

  useEffect(() => {

    const recommendedBook = pageState.recommendedBook;
    // 검색에서 전달된 도서가 있을 때만 추천 등록 API를 호출한다.
    if (!recommendedBook?.isbn || !Number.isSafeInteger(clubNumb) || clubNumb <= 0) {
      // 등록할 도서가 없는 일반 진입을 종료한다.
      return;
    }

    /**
     * 검색에서 선택한 도서를 추천 후보로 등록하고 목록을 갱신한다.
     *
     * @author HanWon.Jang
     * @return 추천 등록과 후보 조회가 끝나면 완료되는 Promise
     */
    const saveRecommendation = async (): Promise<void> => {
      // 선택한 도서를 현재 모임의 추천 후보로 등록한다.
      await createClubBookRecommApi(clubNumb, recommendedBook);
      // 등록 결과를 반영하도록 추천 후보를 다시 조회한다.
      await loadCandidates();
    };

    /**
     * 도서 추천 등록 중 화면 이탈을 막고 전달된 검색 상태를 정리한다.
     *
     * @author HanWon.Jang
     * @return 추천 등록 처리가 끝나면 완료되는 Promise
     */
    const executeRecommendation = async (): Promise<void> => {
      try {
        // 추천 등록이 끝날 때까지 화면 이동을 차단한다.
        await runBlockingOperation(saveRecommendation);
        // 같은 도서가 다시 등록되지 않도록 라우트 전달 상태를 제거한다.
        navigate(location.pathname, {replace: true, state: {}});
      } catch (error) {
        // "오류"
        await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
      }
    };
    // 검색에서 전달된 도서의 추천 등록을 시작한다.
    void executeRecommendation();
  }, [clubNumb, loadCandidates, location.pathname, navigate, pageState.recommendedBook]);

  const selectedCandidate = useMemo(
    // 현재 선택 번호와 일치하는 추천 후보를 계산한다.
    () => candidates.find((candidate) => candidate.recmNumb === selectedRecommendation),
    [candidates, selectedRecommendation],
  );
  const totalVoteCount = useMemo(
    // 모든 추천 후보의 득표수를 합산한다.
    () => candidates.reduce((total, candidate) => total + candidate.voteCnt, 0),
    [candidates],
  );
  const canShowRecommend = !votePage?.hasRecommended;
  const canShowVote = candidates.length > 0 && !votePage?.hasVoted;

  /**
   * 추천 후보로 등록할 도서를 선택하는 검색 화면으로 이동한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleRecommendation = (): void => {

    // 후보 등록 가능 시점이며 아직 내 후보가 없을 때만 검색 화면으로 이동한다.
    if (!votePage?.canRecommend || votePage.hasRecommended) {
      // 후보 등록이 제한된 상태에서는 화면 이동을 종료한다.
      return;
    }

    // 검색 완료 후 현재 투표 화면으로 돌아오도록 경로를 전달한다.
    navigate(`/reading-clubs/books/search/${clubNumb}`, {
      state: {clubBookVoteReturnPath: `/reading-clubs/vote/book/${clubNumb}`},
    });
  };

  /**
   * 사용자가 선택한 추천 후보 번호를 투표 선택 상태에 반영한다.
   *
   * @author HanWon.Jang
   * @param recmNumb 선택한 추천 후보 번호
   * @return 반환값이 없다
   */
  const handleCandidateSelect = (recmNumb: number): void => {
    // 선택한 추천 후보 번호를 현재 투표 상태에 반영한다.
    setSelectedRecommendation(recmNumb);
  };

  /**
   * 자신의 도서 추천을 확인 후 취소하고 후보 목록을 갱신한다.
   *
   * @author HanWon.Jang
   * @param recmNumb 취소할 추천 후보 번호
   * @return 추천 취소 처리가 끝나면 완료되는 Promise
   */
  const handleDelete = async (recmNumb: number): Promise<void> => {

    // "도서 추천을 취소할까요?"
    const confirmResult = await sweetConfirm({
      title: message("frontend.readingClub.vote.cancelConfirmTitle"),
      // "추천을 취소하면 이 도서에 등록된 투표도 함께 삭제돼요."
      text: message("frontend.readingClub.vote.cancelConfirmDescription"),
      // "추천 취소"
      confirmButtonText: message("frontend.readingClub.vote.cancelRecommendation"),
    });

    // 사용자가 확인하지 않으면 추천과 연결 투표를 그대로 유지한다.
    if (!confirmResult.isConfirmed) {
      // 취소 선택 뒤 삭제 API 호출 없이 처리를 종료한다.
      return;
    }

    try {
      /**
       * 선택한 추천 후보를 삭제하고 최신 후보 목록을 조회한다.
       *
       * @author HanWon.Jang
       * @return 추천 삭제와 후보 조회가 끝나면 완료되는 Promise
       */
      const deleteRecommendation = async (): Promise<void> => {
        // 선택한 추천 후보와 연결된 투표를 삭제한다.
        await deleteClubBookRecommApi(clubNumb, recmNumb);
        // 삭제 결과를 반영하도록 추천 후보를 다시 조회한다.
        await loadCandidates();
      };
      // 추천 취소가 끝날 때까지 화면 이동을 차단한다.
      await runBlockingOperation(deleteRecommendation);
    } catch (error) {
      // "오류"
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  };

  /**
   * 선택한 추천 후보에 투표하고 최신 후보 목록을 갱신한다.
   *
   * @author HanWon.Jang
   * @return 투표 처리가 끝나면 완료되는 Promise
   */
  const handleVote = async (): Promise<void> => {

    // 선택 후보가 있어야 투표 API를 호출한다.
    if (!selectedCandidate) {
      // 선택되지 않은 투표 요청을 종료한다.
      return;
    }

    try {
      /**
       * 선택한 후보에 투표하고 최신 후보 목록을 조회한다.
       *
       * @author HanWon.Jang
       * @return 투표와 후보 조회가 끝나면 완료되는 Promise
       */
      const updateVote = async (): Promise<void> => {
        // 선택한 추천 후보에 현재 사용자의 투표를 등록한다.
        await updateClubBookVoteApi(clubNumb, selectedCandidate.recmNumb);
        // 투표 결과를 반영하도록 추천 후보를 다시 조회한다.
        await loadCandidates();
      };
      // 투표가 끝날 때까지 화면 이동을 차단하고 성공 상태로 전환한다.
      await runBlockingOperation(updateVote, {
        success: {
          // "투표가 완료되었습니다."
          title: message("frontend.readingClub.vote.success"),
        },
      });
    } catch (error) {
      // "오류"
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  };

  // 다음 도서 추천과 투표 화면에서 사용할 상태와 이벤트 처리 함수를 반환한다.
  return {
    candidates,
    selectedCandidate,
    selectedRecommendation,
    totalVoteCount,
    votePage,
    canShowRecommend,
    canShowVote,
    handleCandidateSelect,
    handleDelete,
    handleRecommendation,
    handleVote,
  };
};
