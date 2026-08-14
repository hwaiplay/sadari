/**
 * 독후감 등록 화면의 도서 요약과 직접 입력 및 도서 정보 전환을 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as styles from "./SetReportPage.css";
import * as detailStyles from "@/pages/Book/Detail/DetailPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { clsx } from "clsx";
import Loading from "@/components/Loading/Loading";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ReportStatsEditor from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor";
import { useSetReportPage } from "@/features/Book/Set/hooks/useSetReportPage";

/**
 * 선택한 도서를 기준으로 독후감 등록 입력과 도서 정보 전환 화면을 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function SetReportPage() {

  const {
    selectedBook,
    statusCodes,
    status,
    grade,
    reptColr,
    pubcYsno,
    startDate,
    endDate,
    contentByteLength,
    maxContentBytes,
    showBookInfo,
    isContentFadingOut,
    isPending,
    pageStyle,
    periodTitle,
    selectedBookAuthor,
    selectedBookPublisher,
    selectedBookPublishDate,
    selectedBookDescription,
    contentPlaceholder,
    handleFormSubmit,
    handleRangeChange,
    handleStatusChange,
    handleGradeChange,
    handlePublicChange,
    handleCancel,
    handleBookInfoToggle,
    handleBookChange,
    handleContentChange,
  } = useSetReportPage();

  return isPending ? (
    <Loading title={message("frontend.report.loading.create")} />
  ) : (
    /* 독후감 등록 입력 전체 영역 */
    <main className={styles.page} style={pageStyle}>
      {/* 도서와 독서 정보 입력 영역 */}
      <form className={styles.form} onSubmit={handleFormSubmit}>
        {/* 표지 대표색을 기반으로 자동 선택된 책장 색상 값 */}
        <input type="hidden" name="reptColr" value={reptColr} />
        {selectedBook ? (
          <BookSummary
            image={selectedBook.image}
            title={selectedBook.title}
            author={selectedBook.author}
            publisher={selectedBook.publisher}
            onShowBookInfo={handleBookInfoToggle}
            showingBookInfo={showBookInfo}
            onChangeBook={handleBookChange}
          />
        ) : (
          <div className={styles.searchBookArea}>
            <SearchBookButton />
          </div>
        )}

        {showBookInfo && selectedBook ? (
          <div
            key="set-book-info"
            className={clsx(
              detailStyles.contentPanel,
              isContentFadingOut
                ? detailStyles.contentSwitchFadeOut
                : detailStyles.contentSwitchFade,
            )}
          >
            {/* 등록할 도서의 저자와 출판사 및 출간일 요약 영역 */}
            <section
              className={detailStyles.reportStatsSection}
              aria-label={/* "도서 정보" */ message("frontend.common.bookInfo")}
            >
              <div className={detailStyles.bookInfoRows}>
                {/* 등록할 도서 저자 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "저자" */}
                    {message("frontend.common.author")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookAuthor || "-"}
                  </strong>
                </div>

                {/* 등록할 도서 출판사 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "출판사" */}
                    {message("frontend.common.publisher")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookPublisher || "-"}
                  </strong>
                </div>

                {/* 등록할 도서 출간일 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "출간일" */}
                    {message("frontend.common.publDate")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookPublishDate || "-"}
                  </strong>
                </div>
              </div>
            </section>

            {/* 독후감 상세의 기록 카드 위치와 같은 등록 도서 소개 영역 */}
            <div className={detailStyles.recordArea}>
              <section className={detailStyles.recordSection}>
                <div className={detailStyles.recordTitleRow}>
                  <h2 className={detailStyles.sectionTitle}>
                    {/* "책 소개" */}
                    {message("frontend.common.bookDescription")}
                  </h2>
                </div>
                <p className={detailStyles.contentBox}>
                  {selectedBookDescription}
                </p>
              </section>
            </div>
          </div>
        ) : (
          <div
            key="set-report-input"
            className={clsx(
              styles.contentPanel,
              isContentFadingOut
                ? detailStyles.contentSwitchFadeOut
                : detailStyles.contentSwitchFade,
            )}
          >
            {/* 독서 상태에 따라 허용된 항목을 세로 행으로 표시하는 독서 정보 요약 영역 */}
            <ReportStatsEditor
              statusCodes={statusCodes}
              status={status}
              grade={grade}
              pubcYsno={pubcYsno}
              startDate={startDate}
              endDate={endDate}
              periodTitle={periodTitle}
              onStatusChange={handleStatusChange}
              onGradeChange={handleGradeChange}
              onPublicChange={handlePublicChange}
              onRangeChange={handleRangeChange}
            />

            {/* 독후감 기록 입력 영역 */}
            <section className={styles.recordSection}>
              <FormField title={message("frontend.report.field.content")}>
                <div className={styles.textAreaWrap}>
                  <span className={styles.counter}>
                    ({contentByteLength}/{maxContentBytes} byte)
                  </span>
                  <textarea
                    className={styles.textArea}
                    name="content"
                    id="content"
                    placeholder={contentPlaceholder}
                    onChange={handleContentChange}
                  />
                </div>
              </FormField>
            </section>

            {/* 독후감 등록 취소와 저장 명령 영역 */}
            <div className={styles.formActions}>
              <ActionButton
                variant="secondary"
                onClick={handleCancel}
              >
                {/* "취소" */}
                {message("frontend.common.cancel")}
              </ActionButton>
              <ActionButton
                type="submit"
              >
                {/* "저장하기" */}
                {message("frontend.common.save")}
              </ActionButton>
            </div>
          </div>
        )}
      </form>
    </main>
  );
}

export default SetReportPage;
