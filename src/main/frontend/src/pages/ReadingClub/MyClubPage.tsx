import type { ClubInvitation, ReadingClub } from "@/features/ReadingClub/api/readingClubApi";
import { message } from "@/app/messages/message";
import Skeleton from "@/components/Skeleton/Skeleton";
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
  useMyClubPage,
} from "@/features/ReadingClub/hooks/useMyClubPage";
import * as styles from "./MyClubPage.css";

/**
 * 참여 중인 모임의 카테고리와 현재 독서 현황을 목록으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @return 내 모임 목록 화면
 */
export default function MyClubPage() {
  // 화면 로직 훅에서 조회 상태와 사용자 이벤트 처리 함수를 가져온다
  const {
    clubs,
    invitations,
    isInvitationOpen,
    isLoading,
    handleInvitationToggle,
    handleClubKeyDown,
    handleClubClick,
    handleAcceptInvitation,
    handleDeclineInvitation,
  } = useMyClubPage();

  /**
   * 받은 초대 한 건의 수락과 거절 제어 영역을 구성한다
   *
   * @author SeungHyeon.Kang
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
   * 진행 중인 모임 한 건을 피그마 비율의 카드로 구성한다
   *
   * @author SeungHyeon.Kang
   * @param club 표시할 모임
   * @return 진행 중인 모임 카드
   */
  const renderClub = (club: ReadingClub) => (
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
        {/* 모임 분류와 운영 상태 영역 */}
        <div className={styles.clubTop}>
          <span className={styles.clubCategory}>{getClubCategory(club)}</span>
          <span className={styles.clubStatus}>
            {/* "운영 중" */}
            {message("frontend.readingClub.my.operating")}
          </span>
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

      {/* 받은 초대 요약 영역 */}
      {invitations.length > 0 && (
        <section className={styles.invitationSummary}>
          {/* 받은 초대 안내 영역 */}
          <div className={styles.invitationSummaryTop}>
            <img className={styles.invitationIcon} src="/img/icons/icon-notification.svg" alt="" />
            <div className={styles.invitationSummaryCopy}>
              <h2 className={styles.invitationSummaryTitle}>
                {/* "확인할 초대가 있어요" */}
                {message("frontend.readingClub.my.invitationNotice")}
              </h2>
              <p className={styles.invitationSummaryText}>
                {/* "받은 초대 N · 진행 중 N" */}
                {message("frontend.readingClub.my.invitationSummary", [invitations.length, clubs.length])}
              </p>
            </div>
          </div>
          {/* 받은 초대 상세 확인 버튼 영역 */}
          <div className={styles.invitationSummaryAction}>
            <button className={styles.quickButton} type="button" onClick={handleInvitationToggle}>
              {/* "바로 확인" 또는 "접기" */}
              {isInvitationOpen
                ? /* "접기" */ message("frontend.common.collapse")
                : message("frontend.readingClub.my.checkNow")}
            </button>
          </div>
        </section>
      )}

      {/* 받은 초대 상세 목록 영역 */}
      {isInvitationOpen && invitations.length > 0 && (
        <section className={styles.invitationDetail}>
          <h2 className={styles.sectionTitle}>
            {/* "받은 초대" */}
            {message("frontend.readingClub.my.receivedInvitation")}
          </h2>
          <div className={styles.invitationList}>{invitations.map(renderInvitation)}</div>
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
