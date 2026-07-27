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
import { REPORT_STATUS_CODE_GROUP } from "@/features/Book/constants/reportForm";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { useCodeList } from "@/features/Common/utils/codeUtil";
import { useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import CommentSheet from "./components/CommentSheet";
import * as styles from "./PublicReportPage.css";

const CONTENT_PREVIEW_LENGTH = 180;
const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

type ReportSort = "LATEST" | "RATING";
type ReportStatus = string;

const SORT_OPTIONS: readonly CustomSelectOption<ReportSort>[] = [
  { value: "LATEST", label: "최신순" },
  { value: "RATING", label: "별점순" },
];

type PublicReportPageState = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

const getReportStatus = (
  report: PublicReportType,
): string => {
  return String(report.reptStat ?? "")
    .trim()
    .toUpperCase();
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
  const [temporaryComments, setTemporaryComments] = useState<
    Record<number, string[]>
  >({});

  const isbn = searchParams.get("isbn") ?? "";
  const isValidIsbn = isbn.trim().length > 0;
  const publicReportsQuery = usePublicReportsByIsbn(isbn, isValidIsbn);
  const reportStatusCodeQuery = useCodeList(REPORT_STATUS_CODE_GROUP);
  const likeMutation = usePublicReportLikeMutation();
  const pageState = (location.state ?? {}) as PublicReportPageState;

  const reports = useMemo(() => {
    return (publicReportsQuery.data?.data ?? []) as PublicReportType[];
  }, [publicReportsQuery.data]);

  const statusOptions = useMemo<readonly CustomSelectOption<ReportStatus>[]>(() => {
    // 전체 옵션만 화면 전용 값으로 두고 실제 독서 상태는 READ_STAT 상세코드의 사용 순서를 그대로 따릅니다.
    return [
      { value: "ALL", label: "전체" },
      ...(reportStatusCodeQuery.data ?? []).map((code) => ({
        value: code.comdCode,
        label: code.comdName,
      })),
    ];
  }, [reportStatusCodeQuery.data]);

  const statusNameByCode = useMemo(() => {
    return new Map(
      (reportStatusCodeQuery.data ?? []).map((code) => [
        code.comdCode.toUpperCase(),
        code.comdName,
      ]),
    );
  }, [reportStatusCodeQuery.data]);

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

  const handleSubmitComment = (reptNumb: number, comment: string) => {
    setTemporaryComments((prev) => ({
      ...prev,
      [reptNumb]: [
        ...(prev[reptNumb] ?? []),
        comment,
      ],
    }));
  };

  const getStatusClassName = (reportStatus: string) => {
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
              options={statusOptions}
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
                            statusNameByCode.get(reportStatus) ||
                            reportStatus}
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
      {commentReport ? (
        <CommentSheet
          report={commentReport}
          comments={temporaryComments[commentReport.reptNumb] ?? []}
          onClose={() => setCommentReport(null)}
          onSubmitComment={(comment) =>
            handleSubmitComment(commentReport.reptNumb, comment)
          }
        />
      ) : null}
    </>
  );
}

export default PublicReportPage;
