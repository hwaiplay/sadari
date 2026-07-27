import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import CustomSelect, {
  type CustomSelectOption,
} from "@/components/Select/CustomSelect";
import {
  usePublicReportLikeMutation,
  usePublicReportsByIsbn,
} from "@/features/Book/Detail/hook/usePublicReports";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { createPortal } from "react-dom";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import * as styles from "./PublicReportPage.css";

const CONTENT_PREVIEW_LENGTH = 180;
const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

type ReportSort = "LATEST" | "RATING";
type ReportStatus = "ALL" | PublicReportType["reptStat"];

const SORT_OPTIONS: readonly CustomSelectOption<ReportSort>[] = [
  { value: "LATEST", label: "최신순" },
  { value: "RATING", label: "별점순" },
];

const STATUS_OPTIONS: readonly CustomSelectOption<ReportStatus>[] = [
  { value: "ALL", label: "전체" },
  { value: "READ", label: "읽고 있어요" },
  { value: "DONE", label: "다 읽었어요" },
  { value: "STOP", label: "중단했어요" },
];

type PublicReportPageState = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

const STATUS_LABELS: Record<PublicReportType["reptStat"], string> = {
  READ: "읽고 있어요",
  DONE: "다 읽었어요",
  STOP: "중단했어요",
};

const getReportStatus = (
  report: PublicReportType,
): PublicReportType["reptStat"] => {
  const reportStatus = String(report.reptStat ?? "")
    .trim()
    .toUpperCase();

  if (
    reportStatus === "READ" ||
    reportStatus === "DONE" ||
    reportStatus === "STOP"
  ) {
    return reportStatus;
  }

  const reportStatusName = report.reptStatName?.replace(/\s/g, "") ?? "";

  if (reportStatusName.includes("중단")) return "STOP";
  if (
    reportStatusName.includes("다읽") ||
    reportStatusName.includes("완료")
  ) {
    return "DONE";
  }

  // 이전 API 응답처럼 상태 코드가 없는 데이터도 '읽고 있어요'로 표시한다.
  return "READ";
};

/**
 * 선택한 책과 같은 책에 작성된 공개 독후감 목록을 표시합니다.
 * 책 정보, 정렬 및 독서 상태 필터, 좋아요와 댓글 바텀시트를 한 화면에서 제공합니다.
 *
 * @author Hanwon.Jang
 * @return 공개 독후감 목록 페이지 컴포넌트
 */
function PublicReportPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>(
    {},
  );
  const [sort, setSort] = useState<ReportSort>("LATEST");
  const [status, setStatus] = useState<ReportStatus>("ALL");
  const [commentReport, setCommentReport] = useState<PublicReportType | null>(
    null,
  );
  const [commentInput, setCommentInput] = useState("");
  const [temporaryComments, setTemporaryComments] = useState<
    Record<number, string[]>
  >({});

  const isbn = searchParams.get("isbn") ?? "";
  const isValidIsbn = isbn.trim().length > 0;
  const publicReportsQuery = usePublicReportsByIsbn(isbn, isValidIsbn);
  const likeMutation = usePublicReportLikeMutation();
  const pageState = (location.state ?? {}) as PublicReportPageState;

  const reports = useMemo(() => {
    return (publicReportsQuery.data?.data ?? []) as PublicReportType[];
  }, [publicReportsQuery.data]);

  const visibleReports = useMemo(() => {
    const filteredReports =
      status === "ALL"
        ? reports
        : reports.filter((report) => getReportStatus(report) === status);

    if (sort === "RATING") {
      return [...filteredReports].sort(
        (left, right) =>
          (Number(right.reptGrde) || 0) - (Number(left.reptGrde) || 0),
      );
    }

    return filteredReports;
  }, [reports, sort, status]);

  useEffect(() => {
    if (!commentReport) {
      return;
    }

    const previousOverflow = document.body.style.overflow;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setCommentReport(null);
        setCommentInput("");
      }
    };

    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [commentReport]);

  const handleToggleReport = (reptNumb: number) => {
    setExpandedReports((prev) => ({
      ...prev,
      [reptNumb]: !prev[reptNumb],
    }));
  };

  const getCountLabel = (countValue?: number) => {
    const count = Number(countValue) || 0;
    return count > 999 ? "999+" : String(count);
  };

  const handleProfileClick = (userNumb: number) => {
    if (userNumb) {
      navigate(`/social/profile/${userNumb}`);
    }
  };

  const handleCloseCommentSheet = () => {
    setCommentReport(null);
    setCommentInput("");
  };

  const handleSubmitComment = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const comment = commentInput.trim();

    if (!commentReport || !comment) {
      return;
    }

    setTemporaryComments((prev) => ({
      ...prev,
      [commentReport.reptNumb]: [
        ...(prev[commentReport.reptNumb] ?? []),
        comment,
      ],
    }));
    setCommentInput("");
  };

  const getStatusClassName = (reportStatus: PublicReportType["reptStat"]) => {
    if (reportStatus === "DONE") {
      return styles.statusDone;
    }

    if (reportStatus === "STOP") {
      return styles.statusStopped;
    }

    return styles.statusReading;
  };

  if (!isValidIsbn) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (publicReportsQuery.isPending) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  if (publicReportsQuery.isError) {
    return (
      <main className={styles.page}>
        <Container className={styles.content}>
          <p className={styles.empty}>
            {getApiErrorMessage(
              publicReportsQuery.error,
              message("frontend.common.tryAgain"),
            )}
          </p>
        </Container>
      </main>
    );
  }

  const commentSheet = commentReport ? (
    <div className={styles.sheetLayer}>
      <button
        className={styles.sheetBackdrop}
        type="button"
        aria-label={message("frontend.common.close")}
        onClick={handleCloseCommentSheet}
      />
      <section
        className={styles.commentSheet}
        role="dialog"
        aria-modal="true"
        aria-label={`${commentReport.userNick}님의 독후감 댓글`}
      >
        <div className={styles.sheetHandle} aria-hidden="true" />
        <div className={styles.commentSheetBody}>
          {(temporaryComments[commentReport.reptNumb] ?? []).length > 0 ? (
            <ul className={styles.temporaryCommentList}>
              {(temporaryComments[commentReport.reptNumb] ?? []).map(
                (comment, index) => (
                  <li
                    className={styles.temporaryComment}
                    key={`${commentReport.reptNumb}-${index}`}
                  >
                    {comment}
                  </li>
                ),
              )}
            </ul>
          ) : (
            <div className={styles.commentEmpty}>
              <img
                className={styles.commentEmptyIcon}
                src="/img/icons/noti-COMMENT.svg"
                alt=""
              />
              <p className={styles.commentEmptyTitle}>아직 댓글이 없어요.</p>
              <p className={styles.commentEmptyText}>
                첫 번째 댓글을 남겨보세요.
              </p>
            </div>
          )}
        </div>
        <form
          className={styles.commentForm}
          onSubmit={handleSubmitComment}
        >
          <input
            className={styles.commentInput}
            type="text"
            value={commentInput}
            maxLength={500}
            placeholder="댓글을 입력해주세요."
            aria-label="댓글 입력"
            onChange={(event) => setCommentInput(event.target.value)}
          />
          <button
            className={styles.commentSubmitButton}
            type="submit"
            disabled={!commentInput.trim()}
          >
            등록
          </button>
        </form>
      </section>
    </div>
  ) : null;

  return (
    <>
      <main className={styles.page}>
        <div className={styles.content}>
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

          <section className={styles.filters} aria-label="독후감 필터">
            <CustomSelect
              value={sort}
              options={SORT_OPTIONS}
              ariaLabel="독후감 정렬"
              onChange={setSort}
            />
            <CustomSelect
              value={status}
              options={STATUS_OPTIONS}
              ariaLabel="독서 상태"
              onChange={setStatus}
            />
          </section>

          {visibleReports.length > 0 ? (
            <section className={styles.list}>
              {visibleReports.map((report) => {
                const rating = Math.max(
                  0,
                  Math.min(5, Number(report.reptGrde) || 0),
                );
                const reportStatus = getReportStatus(report);
                const isExpanded = Boolean(expandedReports[report.reptNumb]);
                const reportContent =
                  report.reptCntn || message("frontend.common.noWrittenReport");
                const isLongContent =
                  reportContent.length > CONTENT_PREVIEW_LENGTH;

                return (
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
                          className={getStatusClassName(reportStatus)}
                        >
                          {report.reptStatName ||
                            STATUS_LABELS[reportStatus]}
                        </span>
                      </div>

                      {/* 별점 */}
                      <span
                        className={styles.reportRating}
                        aria-label={message("frontend.report.gradeValue", [
                          rating,
                        ])}
                      >
                        <img src={"/img/icons/icon-star-rate.svg"} alt={"rate"} />
                        <span>{rating}</span>
                      </span>
                    </div>

                    <div
                      className={
                        isExpanded || !isLongContent
                          ? styles.reportContentWrapOpen
                          : styles.reportContentWrap
                      }
                    >
                      <p className={styles.reportContent}>{reportContent}</p>
                    </div>

                    {isLongContent ? (
                      <button
                        className={styles.expandButton}
                        type="button"
                        aria-label={message(
                          isExpanded
                            ? "frontend.book.publicReports.collapse"
                            : "frontend.book.publicReports.expand",
                        )}
                        onClick={() => handleToggleReport(report.reptNumb)}
                      >
                        <img
                            className={
                              isExpanded
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
                        disabled={likeMutation.isPending}
                        onClick={() =>
                          likeMutation.mutate({
                            tagtType: "REPORT",
                            tagtNumb: report.reptNumb,
                            targetUserNumb: report.userNumb,
                          })
                        }
                      >
                        {
                          report.likeYsno === "Y"
                              ? <img  src={"/img/icons/icon-heart-fill.svg"} alt={"좋아요"}/>
                              : <img  src={"/img/icons/icon-heart.svg"} alt={"좋아요"}/>
                        }

                        <span>{getCountLabel(report.likeCnt)}</span>
                      </button>
                      <button
                        className={styles.commentButton}
                        type="button"
                        aria-label="댓글 보기"
                        onClick={() => setCommentReport(report)}
                      >
                        <img  src={"/img/icons/icon-comment.svg"} alt={"댓글"}/>
                        <span>
                          {getCountLabel(
                            (report.commentCnt ?? 0) +
                              (temporaryComments[report.reptNumb]?.length ?? 0),
                          )}
                        </span>
                      </button>
                    </div>
                  </article>
                );
              })}
            </section>
          ) : (
            <p className={styles.empty}>
              {reports.length > 0
                ? "선택한 조건에 맞는 독후감이 없어요."
                : message("frontend.book.publicReports.empty")}
            </p>
          )}
        </div>
      </main>
      {typeof document !== "undefined" && commentSheet
        ? createPortal(commentSheet, document.body)
        : null}
    </>
  );
}

export default PublicReportPage;
