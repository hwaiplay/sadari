/**
 * fileName       : SetClubReadingPage
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 선택한 도서와 목표 기간으로 모임 독서를 등록하는 화면을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */

import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as statsStyles from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor.css";
import { useSetClubReadingPage } from "@/features/ReadingClub/hooks/useSetClubReadingPage";
import * as reportStyles from "@/pages/Book/Set/SetReportPage.css";
import { clsx } from "clsx";
import * as styles from "./SetClubReadingPage.css";

/**
 * 선택 도서 요약과 목표 독서 기간만 포함한 모임 독서 등록 화면을 구성한다.
 *
 * @author Hanwon.Jang
 * @return 모임 독서 등록 화면 요소
 */
function SetClubReadingPage() {

  const {
    selectedBook,
    startDate,
    endDate,
    isPending,
    pageStyle,
    handleRangeChange,
    handleBookChange,
    handleCancel,
    handleFormSubmit,
  } = useSetClubReadingPage();

  if (isPending) {
    // 멤버별 독후감까지 저장하는 동안 화면 이동을 차단한다
    return <Loading title="모임 독서를 등록하고 있어요" />;
  }

  return (
    <main
      className={clsx(reportStyles.page, styles.pageTop)}
      style={pageStyle}
    >
      <form className={reportStyles.form} onSubmit={handleFormSubmit}>
        {selectedBook ? (
          <BookSummary
            image={selectedBook.image}
            title={selectedBook.title}
            author={selectedBook.author}
            publisher={selectedBook.publisher}
            onChangeBook={handleBookChange}
          />
        ) : (
          <section className={styles.emptyState}>
            <p className={styles.emptyText}>모임에서 읽을 책을 다시 선택해주세요.</p>
            <ActionButton onClick={handleBookChange}>책 검색하기</ActionButton>
          </section>
        )}

        {selectedBook ? (
          <div className={reportStyles.contentPanel}>
            <section className={clsx(statsStyles.statsSection, styles.periodSection)}>
              <FormField title="목표 독서 기간">
                <CalendarDatePicker
                  name="goalStdt"
                  value={startDate}
                  endName="goalEndt"
                  endValue={endDate}
                  allowFuture
                  onRangeChange={handleRangeChange}
                />
              </FormField>
            </section>

            <div className={reportStyles.formActions}>
              <ActionButton variant="secondary" onClick={handleCancel}>
                취소
              </ActionButton>
              <ActionButton type="submit">
                저장하기
              </ActionButton>
            </div>
          </div>
        ) : null}
      </form>
    </main>
  );
}

export default SetClubReadingPage;
