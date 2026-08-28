import { message } from "@/app/messages/message";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import { ActionButton } from "@/components/Button/ActionButton";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import CustomSelect, { type CustomSelectOption } from "@/components/Select/CustomSelect";
import Skeleton from "@/components/Skeleton/Skeleton";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { ClubMemberProfile } from "@/features/ReadingClub/api/readingClubApi";
import ReadingGoalResultOverlay from "@/features/ReadingClub/components/ReadingGoalResultOverlay";
import ProfileImage from "@/features/User/components/ProfileImage";
import { getGoalProgressColor } from "@/features/User/utils/goalProgress";
import { useClubDetailPage } from "@/features/ReadingClub/hooks/useClubDetailPage";
import { getReadingDeadline } from "@/features/ReadingClub/utils/readingClubDeadline";
import clsx from "clsx";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import * as styles from "./ClubDetailPage.css";

// 모임 상세에 한 번에 표시할 프로필 이미지 수를 제한한다
const MEMBER_PROFILE_VISIBLE_LIMIT = 10;
type ClubDetailAction = "" | "UPDATE" | "DELETE";

/**
 * 모임원 한 명의 프로필 이미지 항목을 표시한다
 *
 * @author Hanwon.Jang
 * @param member 표시할 모임원 프로필
 * @return 모임원 프로필 이미지 항목
 */
function renderMemberProfile(member: ClubMemberProfile) {
  // 참여한 모임원의 프로필 이미지 항목을 반환한다
  return (
    <li className={styles.memberProfileItem} key={member.userNumb}>
      {/* 모임원 프로필 이미지 영역 */}
      <ProfileImage
        className={styles.memberProfileImage}
        src={member.porfPath}
        alt={member.userNick ?? ""}
        title={member.userNick}
      />
    </li>
  );
}

/**
 * 모임 소개와 현재 독서, 멤버 및 모임장 관리 영역을 표시한다.
 *
 * @author Hanwon.Jang
 * @return 모임 상세 화면
 */
export default function ClubDetailPage() {
  const navigate = useNavigate();
  const {
    answers,
    canJoin,
    club,
    isCancellingApplication,
    isDeleting,
    members,
    readingGoalResult,
    handleAnswerChange,
    handleApplicationCancel,
    handleClubAction,
    handleJoinClub,
    handleReadingHistory,
    handleReportWrite,
  } = useClubDetailPage();

  // 상세 데이터가 준비되기 전에는 로딩 안내를 표시한다.
  if (!club) {
    return (
      <main
        className={styles.page}
        aria-busy="true"
        aria-label={message("frontend.readingClub.common.loading")}
      >
        <Skeleton width="100%" height={156} borderRadius={20} />
        <Skeleton width="100%" height={289} borderRadius={22} />
        <Skeleton width="100%" height={80} borderRadius={18} />
        <Skeleton width="100%" height={108} borderRadius={12} />
      </main>
    );
  }

  const visibility = club.clubVisb === "PUBLIC"
    ? /* "공개" */ message("frontend.common.public")
    : /* "비공개" */ message("frontend.common.private");
  const isActiveMember = club.membStat === "ACTIVE";
  // 프로필 이미지는 최대 10명까지만 표시
  const memberProfiles = members.slice(0, MEMBER_PROFILE_VISIBLE_LIMIT);
  // 10명을 초과한 모임원 수만 추가 인원 문구로 표시
  const additionalMemberCount = Math.max(members.length - MEMBER_PROFILE_VISIBLE_LIMIT, 0);
  // 추가 인원이 있는 경우 표시 프로필을 겹쳐 배치
  const hasAdditionalMembers = additionalMemberCount > 0;
  // 예정 또는 진행 중인 회차 번호가 있으면 현재 독서 정보를 표시
  const hasCurrentReading = Number.isFinite(club.currentRondNumb);
  // 첫 회차가 아직 없으면 다음 독서 순번을 1로 표시한다
  const readingOrder = club.readingOrdr ?? 1;
  // API 일시값에서 화면과 날짜 계산에 사용할 로컬 날짜 부분만 분리한다
  const goalStartDate = club.currentGoalStdt?.slice(0, 10);
  const goalEndDate = club.currentGoalEndt?.slice(0, 10);
  const formattedGoalStartDate = formatDashedDateToDot(goalStartDate);
  const formattedGoalEndDate = formatDashedDateToDot(goalEndDate);
  // 같은 연도의 종료일은 피그마 표기처럼 연도를 생략한다
  const readingPeriod = goalStartDate?.slice(0, 4) === goalEndDate?.slice(0, 4)
    ? `${formattedGoalStartDate} ~ ${formattedGoalEndDate.slice(5)}`
    : `${formattedGoalStartDate} ~ ${formattedGoalEndDate}`;
  // 목록과 상세 화면이 같은 날짜 경계와 문구를 사용하도록 공통 표시값을 조회한다
  const readingDeadline = getReadingDeadline(club.currentGoalEndt);
  // 현재 회차에는 모임장이 반드시 참여하므로 빈 집계도 한 명으로 표시한다
  const goalMemberCount = Math.max(1, club.currentGoalMembCnt ?? 0);
  const goalAchievementCount = Math.min(
    goalMemberCount,
    Math.max(0, club.currentGoalAchvCnt ?? 0),
  );
  const goalAchievementRate = goalMemberCount > 0
    ? (goalAchievementCount / goalMemberCount) * 100
    : 0;
  const goalProgressColor = getGoalProgressColor(goalAchievementRate);
  const currentReportStatusLabel = club.currentReportStat === "DONE"
    ? message("frontend.report.status.done")
    : club.currentReportStat === "STOP"
      ? message("frontend.report.status.stopped")
      : club.currentReportStat === "READ"
        ? message("frontend.report.status.reading")
        : message("frontend.readingClub.detail.readingParticipationUnavailable");
  const clubActionOptions: readonly CustomSelectOption<ClubDetailAction>[] = [
    {
      value: "UPDATE",
      label: /* "수정하기" */ message("frontend.common.update"),
      disabled: isDeleting,
    },
    {
      value: "DELETE",
      label: /* "삭제하기" */ message("frontend.common.delete"),
      className: styles.dangerOption,
      disabled: isDeleting,
    },
  ];

  const handleNextBookVote = () => {

    navigate(`/reading-clubs/vote/book/${club.clubNumb}`);
  };

  return (
    <>
      <main className={styles.page}>
        <header className={styles.clubSummary}>
          {club.membRole === "OWNER" ? (
            <CustomSelect<ClubDetailAction>
              className={styles.moreSelect}
              triggerClassName={styles.moreButton}
              optionListClassName={styles.moreOptionList}
              optionClassName={styles.moreOption}
              value=""
              options={clubActionOptions}
              ariaLabel={message("frontend.readingClub.detail.more")}
              triggerContent={<img className={styles.moreIcon} src="/img/icons/icon-more.svg" alt="" />}
              showArrow={false}
              onChange={handleClubAction}
            />
          ) : null}
          <div className={styles.chips}>
            {club.categoryList?.map((category) => (
              <span className={styles.chip} key={category.intrCode}>{category.intrName}</span>
            ))}
          </div>
          <div className={styles.summaryText}>
            <h1 className={styles.detailTitle}>{club.clubName}</h1>
            <p className={styles.meta}>
              {message("frontend.readingClub.detail.summaryMeta", [
                visibility,
                club.memberCnt,
                club.ownrNick ?? "-",
              ])}
            </p>
          </div>
          <p className={styles.description}>{club.clubCntn || "-"}</p>
        </header>

        {isActiveMember ? (
          <>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>{message("frontend.readingClub.detail.currentReading")}</h2>

              <div>
                <div className={styles.currentReadingCard}>
                <div className={styles.readingCardHeader}>
                  <strong className={styles.readingOrder}>
                    {/* "{0}번째 독서" */}
                    {message("frontend.readingClub.detail.readingOrder", [readingOrder])}
                  </strong>
                  {hasCurrentReading && readingDeadline ? (
                    <span
                      className={styles.dDay}
                      data-ended={readingDeadline.state === "ENDED"}
                    >
                      {readingDeadline.label}
                    </span>
                  ) : null}
                </div>
                {hasCurrentReading ? (
                  <div className={styles.currentReadingContent}>
                    <div className={styles.readingBook}>
                      <img
                        className={styles.currentBookImage}
                        src={getBookCoverImageSource(club.currentBookCvim)}
                        onError={handleBookCoverImageError}
                        alt={club.currentBookTitl ?? ""}
                      />
                      <div className={styles.currentBookInformation}>
                        <div className={styles.currentBookSummary}>
                          <div className={styles.currentBookIdentity}>
                            <strong className={styles.currentBookTitle}>{club.currentBookTitl}</strong>
                            {club.currentBookAthr ? (
                              <span className={styles.currentBookAuthor}>{club.currentBookAthr}</span>
                            ) : null}
                          </div>
                          {goalStartDate && goalEndDate ? (
                            <span className={styles.currentReadingPeriod}>{readingPeriod}</span>
                          ) : null}
                        </div>
                        <div className={styles.myReadingStatus}>
                          <span className={styles.myReadingStatusLabel}>
                            {message("frontend.readingClub.detail.myReadingStatus")}
                          </span>
                          <span
                            className={clsx(
                              styles.myReadingStatusValue,
                              !club.currentReportStat && styles.readingStatusUnavailable,
                            )}
                          >
                            <span className={styles.readingStatusDot} aria-hidden="true" />
                            {currentReportStatusLabel}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div className={styles.goalStatus}>
                      <div className={styles.goalProgressTrack}>
                        <span
                          className={styles.goalProgressFill}
                          style={{
                            width: `${goalAchievementRate}%`,
                            backgroundColor: goalProgressColor,
                          }}
                        />
                      </div>
                      <span className={styles.goalAchievementText}>
                        {/* "{0}/{1}명 목표 달성" */}
                        {message("frontend.readingClub.detail.goalAchievement", [
                          goalAchievementCount,
                          goalMemberCount,
                        ])}
                      </span>
                    </div>
                  </div>
                ) : (
                  <div className={styles.readingEmpty}>
                    {/* 현재 독서 등록을 시작하는 책 검색은 모임장에게만 제공한다 */}
                    {club.membRole === "OWNER" ? (
                      <SearchBookButton to={`/reading-clubs/books/search/${club.clubNumb}`} />
                    ) : null}
                    <p>{message("frontend.readingClub.detail.currentReadingEmpty")}</p>
                  </div>
                )}
                </div>

                {/* 현재 독서 관리는 활성 모임장에게만 제공한다 */}
                {club.membRole === "OWNER" && hasCurrentReading ? (
                  <LinkButton
                    link={`/reading-clubs/${club.clubNumb}/${club.currentRondNumb}/edit`}
                    className={styles.managementReadingBtn}
                  >
                    {message("frontend.readingClub.management.reading")}
                    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M6.68262 14.9401L11.5726 10.0501C12.1501 9.47257 12.1501 8.52757 11.5726 7.95007L6.68262 3.06006" stroke="#878787" strokeWidth="1.5" strokeMiterlimit="10" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </LinkButton>
                ) : null}

              </div>
            </section>

            <section className={styles.section}>
              <div>
              {/* 함께 읽는 멤버 */}
              <div className={styles.memberHeader}>
                <h2 className={styles.sectionTitle}>
                  {message("frontend.readingClub.detail.members", [club.memberCnt])}
                </h2>

                {/* 모임 채팅 버튼 */}
                <button className={styles.chatButton} type="button">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10.3933 8.26667V10.98C10.3933 11.22 10.3666 11.4467 10.3066 11.6533C10.0599 12.6333 9.24659 13.2467 8.12659 13.2467H6.31325L4.29992 14.5867C3.99992 14.7933 3.59992 14.5733 3.59992 14.2133V13.2467C2.91992 13.2467 2.35325 13.02 1.95992 12.6267C1.55992 12.2267 1.33325 11.66 1.33325 10.98V8.26667C1.33325 7 2.11992 6.12667 3.33325 6.01333C3.41992 6.00667 3.50659 6 3.59992 6H8.12659C9.48659 6 10.3933 6.90667 10.3933 8.26667Z" fill="#293038"/>
                    <path d="M11.8333 10.4002C12.6799 10.4002 13.3933 10.1202 13.8866 9.62016C14.3866 9.12683 14.6666 8.4135 14.6666 7.56683V4.16683C14.6666 2.60016 13.3999 1.3335 11.8333 1.3335H6.16659C4.59992 1.3335 3.33325 2.60016 3.33325 4.16683V4.66683C3.33325 4.8535 3.47992 5.00016 3.66659 5.00016H8.12659C9.93325 5.00016 11.3933 6.46016 11.3933 8.26683V10.0668C11.3933 10.2535 11.5399 10.4002 11.7266 10.4002H11.8333Z" fill="#293038"/>
                  </svg>
                  {message("frontend.readingClub.detail.clubChat")}
                </button>
              </div>
              <div className={styles.memberSummary} aria-label={message("frontend.readingClub.detail.members", [club.memberCnt])}>
                {/* 참여한 모임원 프로필 이미지 목록 영역 */}
                <ul
                  className={clsx(
                    styles.memberProfiles,
                    hasAdditionalMembers && styles.memberProfilesOverlapped,
                  )}
                >
                  {memberProfiles.map(renderMemberProfile)}
                </ul>
                {hasAdditionalMembers ? (
                  <span className={styles.memberCountText}>
                    +{message("frontend.readingClub.common.memberCount", [additionalMemberCount])}
                  </span>
                ) : null}
              </div>

              {/* 멤버 관리는 활성 모임장에게만 제공한다 */}
              {club.membRole === "OWNER" ? (
                <LinkButton
                  link={`/reading-clubs/manage/members/${club.clubNumb}`}
                  className={styles.managementMembersBtn}
                >
                  {message("frontend.readingClub.management.members")}
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.68262 14.9401L11.5726 10.0501C12.1501 9.47257 12.1501 8.52757 11.5726 7.95007L6.68262 3.06006" stroke="#878787" strokeWidth="1.5" strokeMiterlimit="10" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </LinkButton>
              ) : null}
              </div>
            </section>

            <nav className={styles.clubNavigation} aria-label={message("frontend.readingClub.detail.clubMenu")}>
              <button
                className={styles.navigationRow}
                type="button"
                onClick={handleNextBookVote}
              >
                {/* 다음 도서 투표 */}
                <span>
                  <strong>{message("frontend.readingClub.detail.nextVote")}</strong>
                  <small className={styles.navigationDescription}>
                    {message("frontend.readingClub.detail.nextVoteDescription")}
                  </small>
                </span>
                <img
                  src="/img/icons/icon-chevron-right.svg"
                  alt=""
                  aria-hidden="true"
                />
              </button>

              {/* 이전 독서 기록 */}
              <button
                className={styles.navigationRow}
                type="button"
                onClick={handleReadingHistory}
              >
                <strong>{message("frontend.readingClub.detail.previousReading")}</strong>
                <img
                  src="/img/icons/icon-chevron-right.svg"
                  alt=""
                  aria-hidden="true"
                />
              </button>
            </nav>
          </>
        ) : null}

        {club.joinStat === "PENDING" ? (
          <section className={styles.panel}>
            <h2 className={styles.sectionTitle}>{message("frontend.readingClub.detail.pendingTitle")}</h2>
            <p className={styles.panelDescription}>{message("frontend.readingClub.detail.pendingDescription")}</p>
            <ActionButton
              type="button"
              variant="secondary"
              width="full"
              disabled={isCancellingApplication}
              onClick={() => void handleApplicationCancel()}
            >
              {message("frontend.readingClub.detail.cancelApplicationButton")}
            </ActionButton>
          </section>
        ) : null}

        {canJoin ? (
          <section className={styles.panel}>
            <h2 className={styles.sectionTitle}>
              {club.joinType === "OPEN"
                ? message("frontend.readingClub.detail.joinNow")
                : message("frontend.readingClub.detail.apply")}
            </h2>
            {club.questionList?.map((question, index) => (
              <label className={styles.field} key={question}>
                <span className={styles.label}>{question}</span>
                <textarea
                  className={styles.textarea}
                  maxLength={2000}
                  value={answers[index] ?? ""}
                  onChange={(event) => handleAnswerChange(index, event.target.value)}
                />
              </label>
            ))}
            <ActionButton
              width="full"
              disabled={club.joinType === "APPROVAL" && answers.some((answer) => !answer.trim())}
              onClick={handleJoinClub}
            >
              {club.joinType === "OPEN"
                ? message("frontend.readingClub.common.join.open")
                : message("frontend.readingClub.detail.applyButton")}
            </ActionButton>
          </section>
        ) : null}
      </main>

      {/* 목표 결과 오버레이 팝업 */}
      {isActiveMember && readingGoalResult && !hasCurrentReading ? createPortal(
        <ReadingGoalResultOverlay key={readingGoalResult.rondNumb} result={readingGoalResult} />,
        document.body,
      ) : null}

      {isActiveMember && hasCurrentReading ? createPortal(
        <div className={styles.fixedActionArea}>
          {/* "내 독후감 쓰기" */}
          <ActionButton size="lg" width="full" onClick={handleReportWrite}>
            {club.currentReportStat === 'DONE'
              ? message("frontend.readingClub.detail.viewReport")

              : message("frontend.readingClub.detail.writeReport")
            }
          </ActionButton>
        </div>,
        document.body,
      ) : null}
    </>
  );
}
