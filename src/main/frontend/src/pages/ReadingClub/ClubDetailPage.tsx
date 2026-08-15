import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import CustomSelect, { type CustomSelectOption } from "@/components/Select/CustomSelect";
import Skeleton from "@/components/Skeleton/Skeleton";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { ClubMemberProfile } from "@/features/ReadingClub/api/readingClubApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useClubDetailPage } from "@/features/ReadingClub/hooks/useClubDetailPage";
import clsx from "clsx";
import { createPortal } from "react-dom";
import * as styles from "./ClubDetailPage.css";

// 모임 상세에 한 번에 표시할 프로필 이미지 수를 제한한다
const MEMBER_PROFILE_VISIBLE_LIMIT = 10;
type ClubDetailAction = "" | "EDIT" | "DELETE";

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
  const {
    answers,
    applications,
    canJoin,
    club,
    isDeleting,
    members,
    handleAnswerChange,
    handleApplicationDecision,
    handleClubManagement,
    handleClubAction,
    handleJoinClub,
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
  // 프로필 이미지는 최대 10명까지만 표시한다
  const memberProfiles = members.slice(0, MEMBER_PROFILE_VISIBLE_LIMIT);
  // 10명을 초과한 모임원 수만 추가 인원 문구로 표시한다
  const additionalMemberCount = Math.max(members.length - MEMBER_PROFILE_VISIBLE_LIMIT, 0);
  // 추가 인원이 있는 경우 표시 프로필을 겹쳐 배치한다
  const hasAdditionalMembers = additionalMemberCount > 0;
  // 예정 또는 진행 중인 회차 번호가 있으면 현재 독서 정보를 표시한다
  const hasCurrentReading = Number.isFinite(club.currentRondNumb);
  // 첫 회차가 아직 없으면 다음 독서 순번을 1로 표시한다
  const readingOrder = club.readingOrdr ?? 1;
  const clubActionOptions: readonly CustomSelectOption<ClubDetailAction>[] = [
    {
      value: "EDIT",
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
          <p className={styles.description}>{club.clubCntn}</p>
        </header>

        {isActiveMember ? (
          <>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>{message("frontend.readingClub.detail.currentReading")}</h2>
              <div className={styles.currentReadingCard}>
                <div className={styles.readingCardHeader}>
                  <strong>
                    {/* "{0}번째 독서" */}
                    {message("frontend.readingClub.detail.readingOrder", [readingOrder])}
                  </strong>
                </div>
                {hasCurrentReading ? (
                  <div className={styles.readingBook}>
                    <img
                      className={styles.currentBookImage}
                      src={getBookCoverImageSource(club.currentBookCvim)}
                      onError={handleBookCoverImageError}
                      alt={club.currentBookTitl ?? ""}
                    />
                    <strong className={styles.currentBookTitle}>{club.currentBookTitl}</strong>
                    {club.currentBookAthr ? (
                      <span className={styles.currentBookAuthor}>{club.currentBookAthr}</span>
                    ) : null}
                  </div>
                ) : (
                  <div className={styles.readingEmpty}>
                    {/* 현재 독서가 없으면 공통 책 검색 버튼으로 검색 화면에 이동한다 */}
                    <SearchBookButton to={`/reading-clubs/${club.clubNumb}/books/search`} />
                    <p>{message("frontend.readingClub.detail.currentReadingEmpty")}</p>
                  </div>
                )}
              </div>
            </section>

            <section className={styles.section}>
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
            </section>

            <nav className={styles.clubNavigation} aria-label={message("frontend.readingClub.detail.clubMenu")}>
              <button className={styles.navigationRow} type="button">
                <span>
                  <strong>{message("frontend.readingClub.detail.nextVote")}</strong>
                  <small className={styles.navigationDescription}>
                    {message("frontend.readingClub.detail.nextVoteDescription")}
                  </small>
                </span>
                <img
                  src="/img/icons/icon-chevron-right.svg"
                  alt="arrow"
                />
              </button>
              <button className={styles.navigationRow} type="button">
                <strong>{message("frontend.readingClub.detail.previousReading")}</strong>
                <img
                  src="/img/icons/icon-chevron-right.svg"
                  alt="arrow"
                />
              </button>
            </nav>
          </>
        ) : null}

        {club.joinStat === "PENDING" ? (
          <section className={styles.panel}>
            <h2 className={styles.sectionTitle}>{message("frontend.readingClub.detail.pendingTitle")}</h2>
            <p className={styles.panelDescription}>{message("frontend.readingClub.detail.pendingDescription")}</p>
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

        {club.membRole === "OWNER" && club.joinType === "APPROVAL" ? (
          <section className={styles.management}>
            <section className={styles.panel}>
              <h2 className={styles.sectionTitle}>
                {message("frontend.readingClub.detail.pendingApplications", [applications.length])}
              </h2>
              {applications.length ? applications.map((application) => (
                <article className={styles.application} key={application.applNumb}>
                  <div className={styles.profileRow}>
                    <ProfileImage className={styles.avatar} src={application.porfPath} alt="" />
                    <strong className={styles.profileName}>{application.userNick}</strong>
                    <span />
                  </div>
                  {application.questionList.map((question, index) => (
                    <div className={styles.qa} key={question}>
                      <strong>{message("frontend.readingClub.detail.question", [question])}</strong>
                      <span>{message("frontend.readingClub.detail.answer", [application.answerList[index]])}</span>
                    </div>
                  ))}
                  <div className={styles.actions}>
                    <ActionButton onClick={() => handleApplicationDecision(application.applNumb, "APPROVED")}>
                      {message("frontend.readingClub.detail.approve")}
                    </ActionButton>
                    <ActionButton variant="danger" onClick={() => handleApplicationDecision(application.applNumb, "REJECTED")}>
                      {message("frontend.readingClub.detail.reject")}
                    </ActionButton>
                  </div>
                </article>
              )) : <p className={styles.empty}>{message("frontend.readingClub.detail.noApplications")}</p>}
            </section>
          </section>
        ) : null}
      </main>

      {isActiveMember ? createPortal(
        <div className={styles.fixedActionArea}>
          {/* 모임장이라면 모임 관리 버튼 노출 */}
          {club.membRole === 'OWNER' ? (
            <ActionButton variant="secondary" size="lg" width="full" onClick={handleClubManagement}>
              {/* "모임 관리하기" */}
              {message("frontend.readingClub.detail.managementClub")}
            </ActionButton>
          ):null}
          <ActionButton size="lg" width="full" onClick={handleReportWrite}>
            {message("frontend.readingClub.detail.writeReport")}
          </ActionButton>
        </div>,
        document.body,
      ) : null}
    </>
  );
}
