import { message } from "@/app/messages/message";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { ClubReadingGoalResult } from "@/features/ReadingClub/api/readingClubApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useState, type CSSProperties } from "react";
import * as styles from "./ReadingGoalResultOverlay.css";

const ACHIEVEMENT_PROFILE_VISIBLE_LIMIT = 7;

type ReadingGoalResultOverlayProps = {
  result: ClubReadingGoalResult;
};

/**
 * 종료된 모임 독서의 목표 결과를 전체 화면 레이어로 표시한다.
 *
 * @author HanWon.Jang
 * @param result 종료된 회차의 도서와 목표 달성 결과
 * @return 독서 목표 결과 전체 화면 레이어
 */
export default function ReadingGoalResultOverlay({ result }: ReadingGoalResultOverlayProps) {
  // 사용자가 닫기 버튼을 누른 뒤 상세 화면을 볼 수 있도록 팝업 표시 상태를 관리한다
  const [isOpen, setIsOpen] = useState(true);
  // 참여자가 없는 비정상 집계에서도 진행률 계산이 유효한 숫자를 유지한다
  const achievementRate = result.partCnt > 0
    ? Math.min(100, Math.max(0, (result.goalAchvCnt / result.partCnt) * 100))
    : 0;
  const roundedAchievementRate = Math.round(achievementRate);
  const goalStartDate = formatDashedDateToDot(result.goalStdt);
  const goalEndDate = formatDashedDateToDot(result.goalEndt);
  // 같은 연도의 종료일은 피그마 표기처럼 연도를 생략한다
  const readingPeriod = result.goalStdt.slice(0, 4) === result.goalEndt.slice(0, 4)
    ? `${goalStartDate} ~ ${goalEndDate.slice(5)}`
    : `${goalStartDate} ~ ${goalEndDate}`;
  // 상세 페이지와 같은 배경 및 결과 표지에 사용할 안전한 도서 이미지 경로를 조회한다
  const bookCoverSource = getBookCoverImageSource(result.bookCvim);
  // 도서 표지를 팝업 surface의 불투명 블러 배경 이미지로 전달한다
  const surfaceStyle = {
    "--book-bg-image": `url("${bookCoverSource}")`,
  } as CSSProperties;
  const hasAdditionalAchievementMembers = result.achievementMemberList.length
    > ACHIEVEMENT_PROFILE_VISIBLE_LIMIT;
  const visibleAchievementMembers = result.achievementMemberList.slice(
    0,
    hasAdditionalAchievementMembers
      ? ACHIEVEMENT_PROFILE_VISIBLE_LIMIT - 1
      : ACHIEVEMENT_PROFILE_VISIBLE_LIMIT,
  );
  const additionalAchievementMemberCount = Math.max(
    result.achievementMemberList.length - visibleAchievementMembers.length,
    0,
  );
  // "{0}번째 독서 목표 결과"
  const resultTitle = message("frontend.readingClub.result.roundTitle", [result.readingOrdr]);

  /**
   * 종료 독서 목표 결과 팝업을 닫고 현재 모임 상세 화면을 표시한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function closeReadingGoalResult(): void {
    // 전체 화면 결과 레이어를 제거한다
    setIsOpen(false);
  }

  // 사용자가 팝업을 닫았으면 배경의 모임 상세 화면만 유지한다
  if (!isOpen) {
    return null;
  }

  // 종료된 회차의 도서와 목표 달성 요약 화면을 반환한다
  return (
    <>
      {/* 모임 상세와 공통 헤더 및 내비게이션을 어둡게 표시하는 팝업 배경 영역 */}
      <div className={styles.backgroundOverlay} aria-hidden="true" />

      <section
        className={styles.overlay}
        role="dialog"
        aria-modal="true"
        aria-label={resultTitle}
      >
        {/* 종료 독서 목표 결과 팝업 본문 영역 */}
        <div className={styles.surface} style={surfaceStyle}>
        {/* 종료 독서 회차 제목과 팝업 닫기 영역 */}
        <header className={styles.header}>
          <h2 className={styles.title}>{resultTitle}</h2>
          <button
            className={styles.closeButton}
            type="button"
            aria-label={/* "닫기" */ message("frontend.common.close")}
            title={/* "닫기" */ message("frontend.common.close")}
            onClick={closeReadingGoalResult}
          >
            <img
              className={styles.closeIcon}
              src="/img/icons/icon-close.svg"
              alt="close"
              aria-hidden="true"
            />
          </button>
        </header>

        {/* 종료 회차 도서와 전체 달성률 영역 */}
        <article className={styles.readingCard}>
          <div className={styles.bookSummary}>
            <img
              className={styles.bookCover}
              src={bookCoverSource}
              alt={result.bookTitl}
              onError={handleBookCoverImageError}
            />
            <div className={styles.bookIdentity}>
              <strong className={styles.bookTitle}>{result.bookTitl}</strong>
              {result.bookAthr ? <span className={styles.bookAuthor}>{result.bookAthr}</span> : null}
              <span className={styles.readingPeriod}>{readingPeriod}</span>
            </div>
          </div>

          <div className={styles.progressArea}>
            <div className={styles.progressRow}>
              <div className={styles.progressTrack}>
                <span
                  className={styles.progressFill}
                  style={{ width: `${achievementRate}%` }}
                />
              </div>
              <strong className={styles.progressRate}>{roundedAchievementRate}%</strong>
            </div>
            <span className={styles.progressDescription}>
              {/* "{0}/{1}명 목표 달성" */}
              {message("frontend.readingClub.detail.goalAchievement", [
                result.goalAchvCnt,
                result.partCnt,
              ])}
            </span>
          </div>
        </article>

        {/* 목표 달성자 안내와 프로필 영역 */}
        <article className={styles.achievementCard}>
          <div className={styles.achievementTitleRow}>
            <img
              className={styles.verifiedIcon}
              src="/img/icons/icon-verified.svg"
              alt=""
            />
            <strong className={styles.achievementTitle}>
              {result.myGoalAchieved
                ? /* "목표를 달성했어요" */ message("frontend.readingClub.result.achieved")
                : /* "{0}명이 목표를 달성했어요" */ message(
                  "frontend.readingClub.result.achievedMembers",
                  [result.goalAchvCnt],
                )}
            </strong>
          </div>

          {visibleAchievementMembers.length > 0 ? (
            <div className={styles.achievementProfiles}>
              {visibleAchievementMembers.map((member) => (
                <ProfileImage
                  key={member.userNumb}
                  className={styles.achievementProfile}
                  src={member.porfPath}
                  alt={member.userNick ?? ""}
                  title={member.userNick}
                />
              ))}
              {additionalAchievementMemberCount > 0 ? (
                <span className={styles.additionalAchievementCount}>
                  +{additionalAchievementMemberCount}
                </span>
              ) : null}
            </div>
          ) : (
            <p className={styles.noAchievement}>
              {/* "이번 회차에는 목표 달성자가 없어요" */}
              {message("frontend.readingClub.result.noAchievement")}
            </p>
          )}
        </article>

        {/* 참여와 달성 및 독후감 수를 요약하는 영역 */}
        <article className={styles.summaryCard}>
          <strong className={styles.summaryTitle}>
            {/* "회차 요약" */}
            {message("frontend.readingClub.result.summary")}
          </strong>
          <dl className={styles.summaryList}>
            <div className={styles.summaryItem}>
              <dt>
                {/* "참여" */}
                {message("frontend.readingClub.result.participation")}
              </dt>
              <dd>
                {/* "{0}명" */}
                {message("frontend.readingClub.result.memberUnit", [result.partCnt])}
              </dd>
            </div>
            <div className={styles.summaryItem}>
              <dt>
                {/* "달성" */}
                {message("frontend.readingClub.result.achievement")}
              </dt>
              <dd>
                {/* "{0}명" */}
                {message("frontend.readingClub.result.memberUnit", [result.goalAchvCnt])}
              </dd>
            </div>
            <div className={styles.summaryItem}>
              <dt>
                {/* "독후감" */}
                {message("frontend.readingClub.result.report")}
              </dt>
              <dd>
                {/* "{0}편" */}
                {message("frontend.readingClub.result.reportUnit", [result.reportCnt])}
              </dd>
            </div>
          </dl>
        </article>

        {/* 모임원 독후감과 이전 독서 기록 이동 안내 영역 */}
        <nav className={styles.resultNavigation}>
          <div className={styles.navigationRow}>
            <strong>
              {/* "모임원 독후감 {0}편 보기" */}
              {message("frontend.readingClub.result.viewReports", [result.reportCnt])}
            </strong>
            <img src="/img/icons/icon-chevron-right.svg" alt="arrow"/>
          </div>
          <div className={styles.navigationRowMuted}>
            <strong>
              {/* "이전 독서 기록" */}
              {message("frontend.readingClub.detail.previousReading")}
            </strong>
            <img src="/img/icons/icon-chevron-right-gray.svg" alt="arrow" />
          </div>
        </nav>
        </div>
      </section>
    </>
  );
}
