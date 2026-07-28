import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import CustomSelect, {
  type CustomSelectOption,
} from "@/components/Select/CustomSelect";
import ReplySheet from "@/features/reply/ReplySheet";
import {
  type ReportSort,
  type ReportStatusTone,
  usePublicReportPage,
} from "./hooks/usePublicReportPage";
import * as styles from "./PublicReportPage.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

const SORT_OPTIONS: readonly CustomSelectOption<ReportSort>[] = [
  { value: "LATEST", label: "최신순" },
  { value: "RATING", label: "별점순" },
];

const STATUS_CLASS_NAME: Record<ReportStatusTone, string> = {
  done: styles.statusDone,
  reading: styles.statusReading,
  stopped: styles.statusStopped,
};

/**
 * 선택한 책과 같은 책에 작성된 공개 독후감 목록을 표시한다
 * 책 정보, 정렬 및 독서 상태 필터, 좋아요와 댓글 바텀시트를 한 화면에서 제공한다
 *
 * @author HanWon.Jang
 * @return 공개 독후감 목록 페이지 컴포넌트
 */
function PublicReportPage() {
  // 공개 독후감 페이지의 조회 결과와 필터 및 사용자 동작을 조회한다
  const {
    pageState,
    isValidIsbn,
    isPending,
    isError,
    error,
    reportsCount,
    visibleReports,
    sort,
    status,
    statusOptions,
    commentReport,
    isLikePending,
    handleSortChange,
    handleStatusChange,
    handleToggleReport,
    handleProfileClick,
    handleLike,
    handleOpenReplySheet,
    handleCloseReplySheet,
  } = usePublicReportPage();

  // 유효한 ISBN이 없으면 잘못된 공개 독후감 접근 안내만 표시한다
  if (!isValidIsbn) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  // 공개 독후감 목록 조회 중에는 공통 로딩 화면을 표시한다
  if (isPending) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  // 공개 독후감 조회 실패 시 서버 공통 메시지를 현재 페이지에 표시한다
  if (isError) {
    return (
      /* 공개 독후감 조회 실패 안내 영역 */
      <main className={styles.page}>
        <Container className={styles.content}>
          <p className={styles.empty}>
            {getApiErrorMessage(
              error,
              message("frontend.common.tryAgain"),
            )}
          </p>
        </Container>
      </main>
    );
  }

  return (
    <>
      {/* 도서별 공개 독후감 전체 영역 */}
      <main className={styles.page}>
        <div className={styles.content}>
          {/* 도서 표지와 공개 독후감 요약 영역 */}
          <section className={styles.header}>
            <div className={styles.headerWrap}>
              {pageState.cover ? (
                  <div className={styles.coverFrame}>
                    <img
                        className={styles.coverImage}
                        src={pageState.cover}
                        alt={pageState.title ?? message("frontend.common.bookInfo")}
                    />
                  </div>
              ) : null}
              <div className={styles.headingArea}>
                <h1 className={styles.bookTitle}>{pageState.title ?? "-"}</h1>
                <div className={styles.authorRatingLine}>
                  <p className={styles.meta}>{pageState.author ?? "-"}</p>
                  {pageState.ratingAverage !== null &&
                  pageState.ratingAverage !== undefined &&
                  pageState.ratingAverage !== "" ? (
                      <>
                        <span className={styles.metaSeparator}>|</span>
                        <span className={styles.ratingSummary}>
                      <span className={styles.ratingStar}>
                        <img src={"/img/icons/icon-star-rate.svg"} alt={"rate"} />
                      </span>
                      <span>{pageState.ratingAverage}</span>
                    </span>
                      </>
                  ) : null}
                </div>
              </div>
            </div>
          </section>

          {/* 공개 독후감 정렬과 독서 상태 필터 영역 */}
          <section className={styles.filters} aria-label="독후감 필터">
            <CustomSelect
              value={sort}
              options={SORT_OPTIONS}
              ariaLabel="독후감 정렬"
              onChange={handleSortChange}
            />
            <CustomSelect
              value={status}
              options={statusOptions}
              ariaLabel="독서 상태"
              onChange={handleStatusChange}
            />
          </section>

          {visibleReports.length > 0 ? (
            /* 공개 독후감 목록 영역 */
            <section className={styles.list}>
              {visibleReports.map((report) => {
                // 공개 독후감 화면 모델을 카드 UI로 렌더링한다
                return (
                  /* 공개 독후감 개별 항목 영역 */
                  <article className={styles.item} key={report.reptNumb}>
                    <div className={styles.itemTop}>
                      <div className={styles.itemHeader}>
                        <button
                          className={styles.profileButton}
                          type="button"
                          onClick={() => handleProfileClick(report.userNumb)}
                        >
                          <img
                            className={styles.profileImage}
                            src={report.porfPath || DEFAULT_PROFILE_IMAGE}
                            alt=""
                          />
                          <span className={styles.writer}>
                            {report.userNick || "-"}
                          </span>
                        </button>
                        <span
                          className={STATUS_CLASS_NAME[report.statusTone]}
                        >
                          {report.reportStatusName}
                        </span>
                      </div>

                      {/* 별점 */}
                      <span
                        className={styles.reportRating}
                        aria-label={message("frontend.report.gradeValue", [
                          report.rating,
                        ])}
                      >
                        <img src={"/img/icons/icon-star-rate.svg"} alt={"rate"} />
                        <span>{report.rating}</span>
                      </span>
                    </div>

                    <div
                      className={
                        report.isExpanded || !report.isLongContent
                          ? styles.reportContentWrapOpen
                          : styles.reportContentWrap
                      }
                    >
                      <p className={styles.reportContent}>
                        {report.reportContent
                          || message("frontend.common.noWrittenReport")}
                      </p>
                    </div>

                    {report.isLongContent ? (
                      <button
                        className={styles.expandButton}
                        type="button"
                        aria-label={message(
                          report.isExpanded
                            ? "frontend.book.publicReports.collapse"
                            : "frontend.book.publicReports.expand",
                        )}
                        onClick={() => handleToggleReport(report.reptNumb)}
                      >
                        <img
                            className={
                              report.isExpanded
                                  ? styles.expandArrowOpen
                                  : styles.expandArrow
                            }
                            src={"/img/icons/arrow-bottom.svg"}
                            alt={"arrow"} />
                      </button>
                    ) : null}

                    <div className={styles.itemMetrics}>
                      <button
                        className={styles.metricButton}
                        type="button"
                        aria-label="좋아요"
                        aria-pressed={report.likeYsno === "Y"}
                        disabled={isLikePending}
                        onClick={() => handleLike(report)}
                      >
                        {
                          report.likeYsno === "Y"
                              ? <img  src={"/img/icons/icon-heart-fill.svg"} alt={"좋아요"}/>
                              : <img  src={"/img/icons/icon-heart.svg"} alt={"좋아요"}/>
                        }

                        <span>{report.likeCountLabel}</span>
                      </button>
                      <button
                        className={styles.commentButton}
                        type="button"
                        aria-label="댓글 보기"
                        onClick={() => handleOpenReplySheet(report)}
                      >
                        <img  src={"/img/icons/icon-comment.svg"} alt={"댓글"}/>
                        <span>{report.commentCountLabel}</span>
                      </button>
                    </div>
                  </article>
                );
              })}
            </section>
          ) : (
            <p className={styles.empty}>
              {reportsCount > 0
                ? "선택한 조건에 맞는 독후감이 없어요."
                : message("frontend.book.publicReports.empty")}
            </p>
          )}
        </div>
      </main>
      {commentReport ? (
        <ReplySheet
          report={commentReport}
          onClose={handleCloseReplySheet}
        />
      ) : null}
    </>
  );
}

export default PublicReportPage;
