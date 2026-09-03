import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import type { OwnerElection } from "@/features/ReadingClub/api/readingClubApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useEffect, useState } from "react";
import * as resultStyles from "./ReadingGoalResultOverlay.css";
import * as styles from "./OwnerElectionOverlay.css";
import {guide, guideTitle, guideList} from "@/pages/ReadingClub/ClubBookVotePage.css.ts";

type OwnerElectionOverlayProps = {
  election: OwnerElection;
  submitting: boolean;
  onVote: (userNumb: number) => Promise<boolean>;
};

/**
 * fileName       : OwnerElectionOverlay
 * author         : Hanwon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 후보 선택 모달
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        Hanwon.Jang    최초 생성
 */

export default function OwnerElectionOverlay({
  election,
  submitting,
  onVote,
}: OwnerElectionOverlayProps) {
  const [isOpen, setIsOpen] = useState(!election.voted);
  const [selectedUserNumb, setSelectedUserNumb] = useState<number | null>(
    election.candidateList.find((candidate) => candidate.selected)?.userNumb ?? null,
  );

  useEffect(() => {
    // 서버에서 다시 조회한 기존 선택 후보를 화면 선택값과 동기화함
    setSelectedUserNumb(
      election.candidateList.find((candidate) => candidate.selected)?.userNumb ?? null,
    );
  }, [election]);

  const deadline = election.endxDate.replace("T", " ").slice(0, 16);

  /**
   * 선택한 후보의 최초 투표가 완료되면 모달을 닫음
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleVote = async (): Promise<void> => {
    // 선택 후보가 없으면 투표 요청을 시작하지 않음
    if (selectedUserNumb === null) {
      return;
    }

    // 서버가 최초 투표를 저장한 경우에만 현재 모달을 닫음
    if (await onVote(selectedUserNumb)) {
      setIsOpen(false);
    }
  };

  // 사용자가 닫았거나 이미 투표한 선거이면 모달을 표시하지 않음
  if (!isOpen || election.voted) {
    return null;
  }

  return (
    <>
      <div className={resultStyles.backgroundOverlay} aria-hidden="true" />
      <section
        className={resultStyles.overlay}
        role="dialog"
        aria-modal="true"
        aria-labelledby="owner-election-title"
      >
        <div className={resultStyles.successionSurface}>
          <button
            className={styles.closeButton}
            type="button"
            aria-label={/* "닫기" */ message("frontend.common.close")}
            disabled={submitting}
            onClick={() => setIsOpen(false)}
          >
            <img src={"/img/icons/icon-close.svg"} alt={message("frontend.common.close")} />
          </button>
          <header className={styles.header}>
            <span className={styles.roundBadge}>
              {election.voteRoun === 1
                ? message("frontend.readingClub.ownerElection.firstRound")
                : message("frontend.readingClub.ownerElection.runoff")}
            </span>
            <h2 className={styles.title} id="owner-election-title">
              {message("frontend.readingClub.ownerElection.title")}
            </h2>
            <p className={styles.description}>
              {message("frontend.readingClub.ownerElection.description")}
            </p>
            <time className={styles.deadline} dateTime={election.endxDate}>
              {message("frontend.readingClub.ownerElection.deadline", [deadline])}
            </time>
          </header>

          {election.candidateList.length > 0 ? (
            <div className={styles.candidateList} role="radiogroup"
                 aria-label={message("frontend.readingClub.ownerElection.candidates")}>
              {election.candidateList.map((candidate) => (
                <label className={styles.candidate} key={candidate.userNumb}>
                  <input
                    className={styles.radio}
                    type="radio"
                    name="owner-election-candidate"
                    value={candidate.userNumb}
                    checked={selectedUserNumb === candidate.userNumb}
                    disabled={!election.canVote || submitting}
                    onChange={() => setSelectedUserNumb(candidate.userNumb)}
                  />
                  <ProfileImage
                    className={styles.profile}
                    src={candidate.porfPath}
                    alt=""
                    aria-hidden="true"
                  />
                  <span className={styles.candidateName}>{candidate.userNick}</span>
                </label>
              ))}
            </div>
          ) : (
            <p className={styles.empty}>{message("frontend.readingClub.ownerElection.empty")}</p>
          )}

          {/* 투표 안내 가이드 */}
          <aside className={guide}><p
            className={guideTitle}>{message("frontend.readingClub.vote.guideTitle")}</p>
            <ul className={guideList}>
              <li>{message("frontend.readingClub.vote.guideOnce")}</li>
              <li>{message("frontend.readingClub.ownerElection.guide1")}</li>
              <li>{message("frontend.readingClub.ownerElection.guide2")}</li>
            </ul>
          </aside>

          <ActionButton
            size="lg"
            width="full"
            disabled={!election.canVote || selectedUserNumb === null || submitting}
            onClick={() => void handleVote()}
          >
            {message("frontend.readingClub.ownerElection.vote")}
          </ActionButton>
        </div>
      </section>
    </>
  );
}
