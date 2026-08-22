/**
 * 완료된 모임 독서 회차의 DONE 독후감 목록 페이지를 제공한다.
 *
 * @author HanWon.Jang
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import ReportListView from "@/components/ReportList/ReportListView";
import { useClubRoundReportPage } from "@/features/ReadingClub/hooks/useClubRoundReportPage";
import * as styles from "@/components/ReportList/ReportListView.css";

/**
 * 현재 활성 모임원에게 대상 완료 회차의 DONE 독후감 목록을 제공한다.
 *
 * @author HanWon.Jang
 * @return 모임 회차 독후감 목록 페이지
 */
export default function ClubRoundReportPage() {
  // 모임 회차 독후감 전용 서버 상태와 사용자 동작을 조회한다
  const page = useClubRoundReportPage();

  // 모임 또는 회차 번호가 유효하지 않으면 서버 조회 없이 잘못된 접근을 안내한다
  if (!page.isValidRoute) {
    // 잘못된 모임 회차 독후감 경로 안내를 반환한다
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  // 첫 회차 독후감 페이지를 조회하는 동안 공통 로딩 화면을 표시한다
  if (page.isPending) {
    // 모임 회차 독후감 조회 로딩 화면을 반환한다
    return <Loading />;
  }

  // 접근 거절 또는 회차 독후감 조회 실패 시 서버 오류 문구를 표시한다
  if (page.isError) {
    // 모임 회차 독후감 조회 실패 안내 영역을 반환한다
    return (
      <main className={styles.page}>
        <Container className={styles.content}>
          <p className={styles.empty}>
            {getApiErrorMessage(page.error, message("frontend.common.tryAgain"))}
          </p>
        </Container>
      </main>
    );
  }

  // 모임 전용 DONE 조회 결과를 공통 독후감 목록 UI에 전달한다
  return (
    <ReportListView
      book={page.pageState}
      reports={page.visibleReports}
      reportsCount={page.reportsCount}
      sort={page.sort}
      emptyMessage={message("frontend.readingClub.result.noDoneReports")}
      commentReport={page.commentReport}
      isLikePending={page.isLikePending}
      hasNext={page.hasNext}
      isFetchingNext={page.isFetchingNext}
      onSortChange={page.handleSortChange}
      onToggleReport={page.handleToggleReport}
      onProfileClick={page.handleProfileClick}
      onLike={page.handleLike}
      onOpenReply={page.handleOpenReply}
      onCloseReply={page.handleCloseReply}
      onLoadMore={page.handleLoadMore}
    />
  );
}
