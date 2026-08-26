import {getApiErrorMessage} from "@/app/api/resultData";
import {sweetConfirm, sweetError} from "@/app/lib/sweetAlert/sweetAlert";
import {message} from "@/app/messages/message";
import {runBlockingOperation} from "@/app/navigation/blockingOperation";
import {ActionButton} from "@/components/Button/ActionButton";
import type {BookSearchResultType} from "@/features/Book/types/book.type";
import {getBookCoverImageSource, handleBookCoverImageError} from "@/features/Book/utils/bookCoverImage";
import {
  createClubBookRecommApi,
  deleteClubBookRecommApi,
  getClubBookRecommApi,
  updateClubBookVoteApi,
  type ClubBookRecommendation,
} from "@/features/ReadingClub/api/readingClubApi";
import {useCallback, useEffect, useMemo, useState} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import * as styles from "./ClubBookVotePage.css";
import {buttonDanger} from "./SetClubPage.css.ts"
import {clsx} from "clsx";

type VotePageState = { recommendedBook?: BookSearchResultType };

/** 모임 다음 도서 추천과 투표 API 상태를 표시한다. @author HanWon.Jang @return 다음 도서 투표 화면 */
const ClubBookVotePage = () => {

  const navigate = useNavigate();
  const location = useLocation();
  const {clubNumb: clubNumbParam} = useParams();
  const clubNumb = Number(clubNumbParam);
  const pageState = (location.state ?? {}) as VotePageState;
  const [candidates, setCandidates] = useState<ClubBookRecommendation[]>([]);
  const [selectedRecommendation, setSelectedRecommendation] = useState<number | null>(null);

  const loadCandidates = useCallback(async () => {

    // 유효한 모임 번호로만 추천 목록을 조회한다.
    if (!Number.isSafeInteger(clubNumb) || clubNumb <= 0) {
      // 잘못된 경로의 목록 조회를 종료한다.
      return;
    }

    try {
      const recommendationList = await getClubBookRecommApi(clubNumb);
      setCandidates(recommendationList);
      setSelectedRecommendation(recommendationList.find((candidate) => candidate.voteYsno === "Y")?.recmNumb ?? null);
    } catch (error) {
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  }, [clubNumb]);

  useEffect(() => {
    void loadCandidates();
  }, [loadCandidates]);


  useEffect(() => {

    const recommendedBook = pageState.recommendedBook;
    // 검색에서 전달된 도서가 있을 때만 추천 등록 API를 호출한다.
    if (!recommendedBook?.isbn || !Number.isSafeInteger(clubNumb) || clubNumb <= 0) {
      // 등록할 도서가 없는 일반 진입을 종료한다.
      return;
    }

    const saveRecommendation = async () => {
      await createClubBookRecommApi(clubNumb, recommendedBook);
      await loadCandidates();
    };

    const executeRecommendation = async () => {
      try {
        await runBlockingOperation(saveRecommendation);
        navigate(location.pathname, {replace: true, state: {}});
      } catch (error) {
        await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
      }
    };
    void executeRecommendation();
  }, [clubNumb, loadCandidates, location.pathname, navigate, pageState.recommendedBook]);

  const selectedCandidate = useMemo(
    () => candidates.find((candidate) => candidate.recmNumb === selectedRecommendation),
    [candidates, selectedRecommendation],
  );

  const handleRecommendation = () => {
    navigate(`/reading-clubs/${clubNumb}/books/search`, {
      state: {clubBookVoteReturnPath: `/reading-clubs/${clubNumb}/book-vote`},
    });
  };

  const handleDelete = async (recmNumb: number) => {
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
      await runBlockingOperation(async () => {
        await deleteClubBookRecommApi(clubNumb, recmNumb);
        await loadCandidates();
      });
    } catch (error) {
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  };

  const handleVote = async () => {
    // 선택 후보가 있어야 투표 API를 호출한다.
    if (!selectedCandidate) {
      // 선택되지 않은 투표 요청을 종료한다.
      return;
    }

    try {
      await runBlockingOperation(async () => {
        await updateClubBookVoteApi(clubNumb, selectedCandidate.recmNumb);
        await loadCandidates();
      }, {
        success: {
          // "투표가 완료되었습니다."
          title: message("frontend.readingClub.vote.success"),
        },
      });
    } catch (error) {
      await sweetError(message("frontend.alert.errorTitle"), getApiErrorMessage(error, message("frontend.common.error")));
    }
  };

  // 서버 추천 목록과 추천·삭제·투표 명령을 반환한다.
  return (
    <main className={styles.page}>
      <section className={styles.voteSummary}>
        <div><h1 className={styles.summaryTitle}>{message("frontend.readingClub.vote.title")}</h1><p
          className={styles.deadline}>{message("frontend.readingClub.vote.deadline")}</p></div>
        <span className={styles.dDay}>D-2</span>
      </section>
      <section className={styles.candidateSection}>
        <h2
          className={styles.sectionTitle}>{message("frontend.readingClub.vote.candidateCount", [candidates.length])}</h2>
        {candidates.length === 0 ? <div className={styles.emptyState}><strong
          className={styles.emptyTitle}>{message("frontend.readingClub.vote.emptyTitle")}</strong><p
          className={styles.emptyDescription}>{message("frontend.readingClub.vote.emptyDescription")}</p></div> : (
          <div className={styles.candidateList} role="radiogroup"
               aria-label={message("frontend.readingClub.vote.candidates")}>
            {candidates.map((candidate) => {
              const selected = candidate.recmNumb === selectedRecommendation;
              // 추천 도서 선택 카드와 본인 추천 삭제 명령을 반환한다.
              return <article className={styles.candidateCard} data-selected={selected} key={candidate.recmNumb}>
                <button className={styles.candidateSelect} role="radio" aria-checked={selected} type="button"
                        onClick={() => setSelectedRecommendation(candidate.recmNumb)}>
                  <img className={styles.cover} src={getBookCoverImageSource(candidate.bookCvim)} alt=""
                       onError={handleBookCoverImageError}/>
                  <span className={styles.bookInformation}><small
                    className={styles.recommender}>{candidate.mineYsno === "Y" ? message("frontend.readingClub.vote.myRecommendation") : candidate.userNick}</small><strong
                    className={styles.bookTitle}>{candidate.bookTitl}</strong><span
                    className={styles.author}>{candidate.bookAthr}</span></span><span className={styles.radioIndicator}
                                                                                      aria-hidden="true"/>
                </button>
                {/* 추천 취소 버튼 */}
                {candidate.mineYsno === "Y" ? (
                  <button className={clsx(buttonDanger, styles.cancelRecommendationButton)}
                          onClick={() => void handleDelete(candidate.recmNumb)}>
                    <svg width="8" height="2" viewBox="0 0 8 2" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M0.75 0.75H6.75" stroke="#FF3747" stroke-width="1.5" stroke-linecap="round"
                            stroke-linejoin="round"/>
                    </svg>
                    {message("frontend.readingClub.vote.cancelRecommendation")}
                  </button>
                ) : null}
              </article>;
            })}
          </div>
        )}
      </section>
      <aside className={styles.guide}><p
        className={styles.guideTitle}>{message("frontend.readingClub.vote.guideTitle")}</p>
        <ul className={styles.guideList}>
          <li>{message("frontend.readingClub.vote.guideOnce")}</li>
          <li>{message("frontend.readingClub.vote.guideTie")}</li>
        </ul>
      </aside>
      <div className={styles.actions} data-button-count={candidates.length === 0 ? "one" : "two"}>
        {/* 도서 추천하기 */}
        <ActionButton variant={candidates.length === 0 ? "primary" : "secondary"} size="lg" width="full"
                      onClick={handleRecommendation}>{message("frontend.readingClub.vote.recommend")}</ActionButton>
        {/* 투표하기 */}
        {candidates.length > 0 ? <ActionButton size="lg" width="full" disabled={!selectedCandidate}
                                               onClick={() => void handleVote()}>{message("frontend.readingClub.vote.submit")}</ActionButton> : null}
      </div>
    </main>
  );
};

export default ClubBookVotePage;
