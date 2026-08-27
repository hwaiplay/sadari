/**
 * 공개된 도서별 독후감 목록 페이지를 제공한다.
 *
 * @author HanWon.Jang
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import ReportListView from "@/components/ReportList/ReportListView";
import { usePublicReportPage } from "./hooks/usePublicReportPage";
import * as styles from "@/components/ReportList/ReportListView.css";

/**
 * 선택한 도서의 공개 독후감 조회와 상태 필터를 전용 페이지로 제공한다.
 *
 * @author HanWon.Jang
 * @return 공개 독후감 목록 페이지
 */
export default function PublicReportPage() {
  // 공개 독후감 페이지의 서버 상태와 사용자 동작을 조회한다
  const page = usePublicReportPage();

  // ISBN이 없으면 공개 독후감 API를 호출하지 않고 잘못된 접근을 안내한다
  if (!page.isValidIsbn) {
    // 잘못된 공개 독후감 경로 안내를 반환한다
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  // 첫 공개 독후감 페이지를 조회하는 동안 공통 로딩 화면을 표시한다
  if (page.isPending) {
    // 공개 독후감 조회 로딩 화면을 반환한다
    return <Loading />;
  }

  // 공개 독후감 조회 실패 시 공통 API 오류 문구를 표시한다
  if (page.isError) {
    // 공개 독후감 조회 실패 안내 영역을 반환한다
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

  // 공개 조회 정책과 필터 상태를 공통 독후감 목록 UI에 전달한다
  return (
    <ReportListView
      book={page.pageState}
      reports={page.visibleReports}
      reportsCount={page.reportsCount}
      sort={page.sort}
      status={page.status}
      statusOptions={page.statusOptions}
      emptyMessage={message("frontend.book.publicReports.empty")}
      commentReport={page.commentReport}
      focusReplNumb={page.focusReplNumb}
      isLikePending={page.isLikePending}
      hasNext={page.hasNext}
      isFetchingNext={page.isFetchingNext}
      onSortChange={page.handleSortChange}
      onStatusChange={page.handleStatusChange}
      onToggleReport={page.handleToggleReport}
      onProfileClick={page.handleProfileClick}
      onLike={page.handleLike}
      onOpenReply={page.handleOpenReplySheet}
      onCloseReply={page.handleCloseReplySheet}
      onLoadMore={page.handleLoadMore}
    />
  );
}
