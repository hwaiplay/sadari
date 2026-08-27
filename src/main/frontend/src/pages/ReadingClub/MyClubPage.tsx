import type { ClubInvitation, ReadingClub } from "@/features/ReadingClub/api/readingClubApi";
import { message } from "@/app/messages/message";
import Skeleton from "@/components/Skeleton/Skeleton";
import { ActionButton } from "@/components/Button/ActionButton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import { createPortal } from "react-dom";
import { Link } from "react-router-dom";
import {
  getClubCategory,
  getClubMeta,
  getGoalProgress,
  getGoalProgressText,
  type PendingClubApplications,
  useMyClubPage,
} from "@/features/ReadingClub/hooks/useMyClubPage";
import { getReadingDeadline } from "@/features/ReadingClub/utils/readingClubDeadline";
import * as styles from "./MyClubPage.css";

/**
 * fileName       : MyClubPage
 * author         : SeungHyeon.Kang, Hanwon.Jang
 * date           : 2026-08-27
 * description    : 내 모임 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 */

/**
 * 모임별 승인 대기 가입 신청 건수를 전체 건수에 합산
 *
 * @author HanWon.Jang
 * @param total 현재까지 합산한 가입 신청 건수
 * @param club 합산할 모임별 가입 신청 요약
 * @return 누적 승인 대기 가입 신청 건수
 */
const sumApplicationCnt = (
  total: number,
  club: PendingClubApplications,
): number => {
  // 현재 모임의 신청 건수를 더한 누적값을 반환
  return total + club.applicationCnt;
};

export default function MyClubPage() {
  const {
    clubs,
    invitations,
    pendingApplications,
    isNoticeOpen,
    isLoading,
    handleNoticeToggle,
    handleClubKeyDown,
    handleClubClick,
    handleAcceptInvitation,
    handleDeclineInvitation,
  } = useMyClubPage();

  // 모임장인 모든 모임의 승인 대기 신청 건수를 합산
  const pendingApplicationCnt = pendingApplications.reduce(sumApplicationCnt, 0);
  const hasClubNotice = invitations.length > 0 || pendingApplicationCnt > 0;

  /**
   * 받은 초대 한 건의 수락과 거절 제어 영역을 구성
   *
   * @author Hanwon.Jang
   * @param invitation 표시할 받은 초대
   * @return 받은 초대 카드
   */
  const renderInvitation = (invitation: ClubInvitation) => (
    /* 받은 초대 개별 항목 영역 */
    <article className={styles.invitationItem} key={invitation.clubNumb}>
      {/* 초대 모임명과 발신자 안내 영역 */}
      <div className={styles.invitationCopy}>
        <h3 className={styles.invitationName}>{invitation.clubName}</h3>
        <p className={styles.invitationSender}>
          {/* "모임장님이 모임에 초대했어요." */}
          {message("frontend.readingClub.my.invitationSender", [
            invitation.senderNick ?? message("frontend.readingClub.my.defaultSender"),
          ])}
        </p>
      </div>
      {/* 초대 수락과 거절 버튼 영역 */}
      <div className={styles.invitationActions}>
        <button
          className={styles.invitationAccept}
          type="button"
          data-club-numb={invitation.clubNumb}
          onClick={handleAcceptInvitation}
        >
          {/* "수락" */}
          {message("frontend.readingClub.my.accept")}
        </button>
        <button
          className={styles.invitationDecline}
          type="button"
          data-club-numb={invitation.clubNumb}
          onClick={handleDeclineInvitation}
        >
          {/* "거절" */}
          {message("frontend.readingClub.my.decline")}
        </button>
      </div>
    </article>
  );

  /**
   * 승인 대기 가입 신청이 있는 모임의 관리 화면 이동 항목을 구성한다
   *
   * @author HanWon.Jang
   * @param club 표시할 모임별 가입 신청 요약
   * @return 가입 신청 관리 화면 이동 항목
   */
  const renderApplicationNotice = (club: PendingClubApplications) => (
    /* 승인 대기 신청이 있는 개별 모임 영역 */
    <Link
      className={styles.applicationItem}
      key={club.clubNumb}
      to={`/reading-clubs/manage/members/${club.clubNumb}`}
    >
      <span className={styles.applicationClubName}>{club.clubName}</span>
      <span className={styles.applicationCount}>
        {/* "가입 신청 N건" */}
        {message("frontend.readingClub.my.applicationCount", [club.applicationCnt])}
      </span>
    </Link>
  );

  /**
   * 진행 중인 모임 한 건을 카드 형태로 구성한다
   *
   * @author Hanwon.Jang
   * @param club 표시할 모임
   * @return 진행 중인 모임 카드
  */
  const renderClub = (club: ReadingClub) => {
    // 두 모임 화면이 같은 날짜 경계와 문구를 사용하도록 공통 표시값을 조회한다
    const readingDeadline = getReadingDeadline(club.currentGoalEndt);

    // 현재 독서 종료일까지 남은 기간을 포함한 모임 카드를 반환한다
    return (
      /* 진행 중인 모임 개별 항목 영역 */
      <article
        className={styles.clubCard}
        key={club.clubNumb}
        role="button"
        tabIndex={0}
        data-club-numb={club.clubNumb}
        onClick={handleClubClick}
        onKeyDown={handleClubKeyDown}
      >
        {/* 모임 대표 이미지 영역 */}
        <img
          className={styles.clubCover}
          src={getBookCoverImageSource(club.currentBookCvim)}
          onError={handleBookCoverImageError}
          alt={club.currentBookTitl ?? ""}
          loading="lazy"
        />
        {/* 모임 기본 정보와 참여 현황 영역 */}
        <div className={styles.clubInfo}>
          {/* 모임 분류와 현재 독서 종료일 영역 */}
          <div className={styles.clubTop}>
            <span className={styles.clubCategory}>{getClubCategory(club)}</span>
            {readingDeadline ? (
              <span
                className={styles.clubStatus}
                data-ended={readingDeadline.state === "ENDED"}
              >
                {readingDeadline.label}
              </span>
            ) : null}
          </div>
          <h3 className={styles.clubName}>{club.clubName}</h3>
          <p className={styles.clubMeta}>{getClubMeta(club)}</p>
          {/* 모임 목표 달성 현황 영역 */}
          <div className={styles.progressTrack} aria-hidden="true">
            <span className={styles.progressValue} style={{ width: `${getGoalProgress(club)}%` }} />
          </div>
          <p className={styles.progressText}>
            {getGoalProgressText(club)}
          </p>
        </div>
      </article>
    );
  };

  // 조회 상태와 관계없이 검색과 모임 찾기를 사용할 수 있는 전체 화면을 반환한다
  return (
    <div className={styles.page}>
      {/* 모임 검색 진입 영역 */}
      <Link className={styles.searchTrigger} to="/reading-clubs/find">
        <span>
          {/* "모임 이름을 검색해보세요" */}
          {message("frontend.readingClub.my.searchPlaceholder")}
        </span>
        <img className={styles.searchIcon} src="/img/icons/icon-search.svg" alt="" />
      </Link>

      {/* 받은 초대와 모임장 가입 신청 요약 영역 */}
      {hasClubNotice && (
        <section className={styles.invitationSummary}>
          {/* 확인할 모임 알림 안내 영역 */}
          <div className={styles.invitationSummaryTop}>
            <img className={styles.invitationIcon} src="/img/icons/icon-notification.svg" alt="" />
            <div className={styles.invitationSummaryCopy}>
              <h2 className={styles.invitationSummaryTitle}>
                {/* "확인할 초대와 신청이 있어요" */}
                {message("frontend.readingClub.my.clubNotice")}
              </h2>
              <p className={styles.invitationSummaryText}>
                {/* "받은 초대 N · 승인 대기 N" */}
                {message("frontend.readingClub.my.clubNoticeSummary", [
                  invitations.length,
                  pendingApplicationCnt,
                ])}
              </p>
            </div>
          </div>
          {/* 모임 알림 상세 확인 버튼 영역 */}
          <div className={styles.invitationSummaryAction}>
            <ActionButton
              className={styles.quickButton}
              variant="secondary"
              size="sm"
              aria-expanded={isNoticeOpen}
              aria-controls="club-notice-details"
              onClick={handleNoticeToggle}
            >
              <>
                {/* "바로 확인" 또는 "접기" */}
                {isNoticeOpen
                  ?
                  /* "접기" */
                  message("frontend.common.collapse")
                  :
                  message("frontend.readingClub.my.checkNow")
                }

                {/* 화살표 */}
                <svg
                  className={isNoticeOpen ? styles.noticeArrowOpen : styles.noticeArrow}
                  width="12"
                  height="12"
                  viewBox="0 0 12 12"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M2.04 4.455 5.3 7.715a.99.99 0 0 0 1.4 0l3.26-3.26"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeMiterlimit="10"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </>
            </ActionButton>
          </div>

          {/* 접힌 상태에서도 상세 DOM을 유지하여 높이 전환이 자연스럽게 이어지게 한다 */}
          <div
            id="club-notice-details"
            className={isNoticeOpen ? styles.noticeDetailsOpen : styles.noticeDetails}
            aria-hidden={!isNoticeOpen}
            inert={!isNoticeOpen}
          >
            <div className={styles.noticeDetailsInner}>
              {/* 받은 초대 상세 목록 영역 */}
              {invitations.length > 0 && (
                <div className={styles.invitationDetail}>
                  <h2 className={styles.sectionTitle}>
                    {/* "받은 초대" */}
                    {message("frontend.readingClub.my.receivedInvitation")}
                  </h2>
                  <div className={styles.invitationList}>{invitations.map(renderInvitation)}</div>
                </div>
              )}

              {/* 모임장 승인 대기 가입 신청 상세 목록 영역 */}
              {pendingApplications.length > 0 && (
                <div className={styles.invitationDetail}>
                  <h2 className={styles.sectionTitle}>
                    {/* "승인 대기" */}
                    {message("frontend.readingClub.my.pendingApplications")}
                  </h2>
                  {/* 승인 대기 신청이 있는 모임 목록 영역 */}
                  <div className={styles.applicationList}>
                    {pendingApplications.map(renderApplicationNotice)}
                  </div>
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {/* 진행 중인 모임 목록 영역 */}
      <section className={styles.clubSection}>
        <h2 className={styles.sectionTitle}>
          {/* "진행 중인 모임 N" */}
          {message("frontend.readingClub.my.activeClubCount", [clubs.length])}
        </h2>
        {/* 참여 중인 모임 카드 목록 영역 */}
        {isLoading ? (
          <div className={styles.clubList}>
            {/* 진행 중인 모임 카드 로딩 영역 */}
            {/* "모임을 불러오고 있어요." */}
            <Skeleton
              width="100%"
              height={156}
              borderRadius={22}
              ariaLabel={message("frontend.readingClub.common.loading")}
            />
          </div>
        ) : clubs.length > 0 ? (
          <div className={styles.clubList}>{clubs.map(renderClub)}</div>
        ) : (
          <p className={styles.empty}>
            {/* "아직 참여 중인 모임이 없어요." */}
            {message("frontend.readingClub.my.empty")}
          </p>
        )}
      </section>

      {/* 모임 찾기 진입 영역 */}
      <Link className={styles.findClub} to="/reading-clubs/find">
        <span className={styles.findClubCopy}>
          <strong className={styles.findClubTitle}>
            {/* "모임 찾기" */}
            {message("frontend.readingClub.my.findTitle")}
          </strong>
          <span className={styles.findClubDescription}>
            {/* "나에게 맞는 모임을 찾아보세요" */}
            {message("frontend.readingClub.my.findDescription")}
          </span>
        </span>
        <span className={styles.findClubArrow} aria-hidden="true">›</span>
      </Link>

      {/* 페이지 전환 transform의 영향을 받지 않는 새 모임 만들기 플로팅 버튼 영역 */}
      {createPortal(
        <Link className={styles.createButton} to="/reading-clubs/set" aria-label={message("frontend.readingClub.my.create")}>
          {/* "새 모임 만들기" */}
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
            <path d="M36 26H26v10a2 2 0 0 1-4 0V26H12a2 2 0 0 1 0-4h10V12a2 2 0 0 1 4 0v10h10a2 2 0 0 1 0 4Z" fill="currentColor" />
          </svg>
        </Link>,
        document.body,
      )}
    </div>
  );
}
