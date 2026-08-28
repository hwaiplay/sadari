import {message} from "@/app/messages/message";
import {ActionButton} from "@/components/Button/ActionButton";
import {getBookCoverImageSource, handleBookCoverImageError} from "@/features/Book/utils/bookCoverImage";
import {useClubBookVotePage} from "@/features/ReadingClub/hooks/useClubBookVotePage";
import {getGoalProgressColor} from "@/features/User/utils/goalProgress";
import * as styles from "./ClubBookVotePage.css";
import {buttonDanger} from "./SetClubPage.css.ts"
import {clsx} from "clsx";

/** 모임 다음 도서 추천과 투표 API 상태를 표시한다. @author HanWon.Jang @return 다음 도서 투표 화면 */
const ClubBookVotePage = () => {

  const {
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
  } = useClubBookVotePage();

  // 서버 추천 목록과 추천·삭제·투표 명령을 반환한다.
  return (
    <main className={styles.page}>
      <section className={styles.voteSummary}>
        <div>
          <h1 className={styles.summaryTitle}>{message("frontend.readingClub.vote.title")}</h1>
          <p className={styles.deadline}>
            {votePage?.voteDeadline
              ? message("frontend.readingClub.vote.deadline", [votePage.voteDeadline.replaceAll("-", ".")])
              : message("frontend.readingClub.vote.alwaysOpen")
            }
          </p>
        </div>
        {votePage?.voteDeadline ? (
          <>
            <span className={styles.dDay}>
              {message("frontend.readingClub.vote.dDay", [votePage.dDay ?? 0])}
            </span>
          </>
        ) : null
        }
      </section>
      <section className={styles.candidateSection}>
        <h2
          className={styles.sectionTitle}>{message("frontend.readingClub.vote.candidateCount", [candidates.length])}</h2>
        {candidates.length === 0 ? <div className={styles.emptyState}><strong
          className={styles.emptyTitle}>{message("frontend.readingClub.vote.emptyTitle")}</strong><p
          className={styles.emptyDescription}>{message("frontend.readingClub.vote.emptyDescription")}</p></div> : (
          <div className={styles.candidateList} role={votePage?.hasVoted ? undefined : "radiogroup"}
               aria-label={message("frontend.readingClub.vote.candidates")}>
            {candidates.map((candidate) => {
              const selected = candidate.recmNumb === selectedRecommendation;
              const voteRate = totalVoteCount > 0 ? Math.round((candidate.voteCnt / totalVoteCount) * 100) : 0;
              // 투표 완료 여부에 따라 후보 선택 또는 득표율 카드를 반환한다.
              return <article className={styles.candidateCard} data-selected={selected} key={candidate.recmNumb}>
                {votePage?.hasVoted ? (
                  <div className={styles.candidateResult}>
                    <img className={styles.cover} src={getBookCoverImageSource(candidate.bookCvim)} alt=""
                         onError={handleBookCoverImageError}/>
                    <span className={styles.bookInformation}>
                      <small
                        className={styles.recommender}>
                        {candidate.mineYsno === "Y" ? message("frontend.readingClub.vote.myRecommendation") : candidate.userNick}</small><strong
                      className={styles.bookTitle}>
                      {candidate.bookTitl}
                    </strong>
                      <span className={styles.author}>{candidate.bookAthr}</span>
                      {/* 후보별 득표율 영역 */}
                      <span className={styles.voteRateRow}>
                        <span className={styles.voteRateTrack} role="progressbar"
                              aria-label={message("frontend.readingClub.vote.rateLabel")}
                              aria-valuemin={0} aria-valuemax={100} aria-valuenow={voteRate}>
                          <span className={styles.voteRateFill} style={{
                            width: `${voteRate}%`,
                            backgroundColor: getGoalProgressColor(voteRate),
                          }}/>
                        </span>
                        {/* "{0}%" */}
                        <strong
                          className={styles.voteRate}>{message("frontend.readingClub.vote.rate", [voteRate])}</strong>
                      </span>
                    </span>
                    {candidate.voteYsno === "Y" ?
                      <span className={styles.myVoteBadge}>
                        {/* "내 투표" */}
                        <svg width="9" height="6" viewBox="0 0 9 6" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M1 2.97491L2.99511 4.97L7.28761 1" stroke="#2F8F64" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        {message("frontend.readingClub.vote.myVote")}
                      </span>
                      : null}
                  </div>
                ) : (
                  <button className={styles.candidateSelect} role="radio" aria-checked={selected} type="button"
                          onClick={() => handleCandidateSelect(candidate.recmNumb)}>
                    <img className={styles.cover} src={getBookCoverImageSource(candidate.bookCvim)} alt=""
                         onError={handleBookCoverImageError}/>
                    <span className={styles.bookInformation}><small
                      className={styles.recommender}>{candidate.mineYsno === "Y" ? message("frontend.readingClub.vote.myRecommendation") : candidate.userNick}</small><strong
                      className={styles.bookTitle}>{candidate.bookTitl}</strong><span
                      className={styles.author}>{candidate.bookAthr}</span></span><span
                    className={styles.radioIndicator}
                    aria-hidden="true"/>
                  </button>
                )}
                {/* 추천 취소 버튼 */}
                {candidate.mineYsno === "Y" && votePage?.canRecommend && !votePage.hasVoted ? (
                  <button className={clsx(buttonDanger, styles.cancelRecommendationButton)}
                          onClick={() => void handleDelete(candidate.recmNumb)}>
                    <svg width="8" height="2" viewBox="0 0 8 2" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M0.75 0.75H6.75" stroke="#FF3747" strokeWidth="1.5" strokeLinecap="round"
                            strokeLinejoin="round"/>
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
          <li>{message("frontend.readingClub.vote.guideCandidate")}</li>
          <li>{message("frontend.readingClub.vote.guideTie")}</li>
        </ul>
      </aside>
      {canShowRecommend || canShowVote ? (
        <div className={styles.actions}
             data-button-count={canShowRecommend && canShowVote ? "two" : "one"}>
          {/* 추천하지 않은 사용자에게 제공하는 도서 추천 명령 영역 */}
          {canShowRecommend ? (
            <ActionButton variant={canShowVote ? "secondary" : "primary"} size="lg" width="full"
                          disabled={!votePage?.canRecommend} onClick={handleRecommendation}>
              {/* "도서 추천하기" */}
              {message("frontend.readingClub.vote.recommend")}
            </ActionButton>
          ) : null}
          {/* 투표하기 */}
          {canShowVote ? <ActionButton size="lg" width="full"
                                       disabled={!selectedCandidate}
                                       onClick={() => void handleVote()}>
            {/* "투표하기" */}
            {message("frontend.readingClub.vote.submit")}
          </ActionButton> : null}
        </div>
      ) : null}
    </main>
  );
};

export default ClubBookVotePage;
