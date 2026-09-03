/**
 * fileName       : SetClubReadingPage
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임 독서를 등록하는 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 * 2026-08-20        Hanwon.Jang        현재 독서 수정 화면 지원
 */

import { ActionButton } from "@/components/Button/ActionButton";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ReportStatsEditor from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor";
import { useSetClubReadingPage } from "@/features/ReadingClub/hooks/useSetClubReadingPage";
import * as reportStyles from "@/pages/Book/Set/SetReportPage.css";
import { clsx } from "clsx";
import * as styles from "./SetClubReadingPage.css";

function SetClubReadingPage() {

  const {
    selectedBook,
    startDate,
    endDate,
    isEditMode,
    isLoading,
    isPending,
    bookChangeAllowed,
    pageStyle,
    handleRangeChange,
    handleBookChange,
    handleCancel,
    handleFormSubmit,
  } = useSetClubReadingPage();

  if (isLoading || isPending) {
    // 최신 수정값 조회 또는 멤버별 독후감 동기화가 끝날 때까지 화면 이동을 차단함
    const loadingMessageKey = isLoading
      ? "frontend.readingClub.reading.loading"
      : isEditMode
        ? "frontend.readingClub.reading.updating"
        : "frontend.readingClub.reading.saving";
    return <Loading title={message(loadingMessageKey)} />;
  }

  return (
    <main
      className={reportStyles.page}
      style={pageStyle}
    >
      <form
        className={clsx(reportStyles.form, styles.formTop)}
        onSubmit={handleFormSubmit}
      >
        {selectedBook ? (
          <BookSummary
            image={selectedBook.image}
            title={selectedBook.title}
            author={selectedBook.author}
            publisher={selectedBook.publisher}
            onChangeBook={bookChangeAllowed ? handleBookChange : undefined}
          />
        ) : (
          <section className={styles.emptyState}>
            <p className={styles.emptyText}>
              {/* "모임에서 읽을 책을 다시 선택해주세요." */}
              {message("frontend.readingClub.reading.bookMissingDescription")}
            </p>
            <ActionButton onClick={handleBookChange}>
              {/* "책 검색하기" */}
              {message("frontend.book.search.open")}
            </ActionButton>
          </section>
        )}

        {selectedBook ? (
          <div className={reportStyles.contentPanel}>
            {isEditMode && !bookChangeAllowed ? (
              <p className={styles.bookChangeNotice}>
                {/* "작성된 독후감이 있어 도서는 변경할 수 없어요. 독서 기간은 변경할 수 있어요." */}
                {message("frontend.readingClub.reading.bookChangeLocked")}
              </p>
            ) : null}
            <ReportStatsEditor
              periodOnly
              statusCodes={[]}
              status="READ"
              grade={0}
              pubcYsno="N"
              startDate={startDate}
              endDate={endDate}
              periodTitle={/* "목표 독서 기간" */ message("frontend.readingClub.reading.periodTitle")}
              onStatusChange={() => undefined}
              onGradeChange={() => undefined}
              onPublicChange={() => undefined}
              onRangeChange={handleRangeChange}
            />

            <div className={reportStyles.formActions}>
              <ActionButton variant="secondary" onClick={handleCancel}>
                {/* "취소" */}
                {message("frontend.common.cancel")}
              </ActionButton>
              <ActionButton type="submit">
                {/* "저장하기" */}
                {message("frontend.common.save")}
              </ActionButton>
            </div>
          </div>
        ) : null}
      </form>
    </main>
  );
}

export default SetClubReadingPage;
