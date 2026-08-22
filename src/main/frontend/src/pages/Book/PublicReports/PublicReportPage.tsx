import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import UserActionMenu from "@/components/UserActionMenu/UserActionMenu";
import CustomSelect, {
  type CustomSelectOption,
} from "@/components/Select/CustomSelect";
import {
  usePublicReportLike,
  usePublicReportsByIsbn,
} from "@/features/Book/Detail/hook/usePublicReports";
import { REPORT_STATUS_CODE_GROUP } from "@/features/Book/constants/reportForm";
import type { PublicReportType } from "@/features/Book/types/book.type";
import type { PublicReportSortType } from "@/features/Book/api/bookApi";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import { useCodeList } from "@/features/Common/utils/codeUtil";
import { useReadingRoundReports } from "@/features/ReadingClub/hooks/useReadingRoundReports";
import ReplySheet from "@/features/reply/ReplySheet";
import ProfileImage from "@/features/User/components/ProfileImage";
import { clsx } from "clsx";
import { useMemo, useState } from "react";
import {
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import * as styles from "./PublicReportPage.css";

const CONTENT_PREVIEW_LENGTH = 180;

type ReportStatus = string;

const SORT_OPTIONS: readonly CustomSelectOption<PublicReportSortType>[] = [
  { value: "RELATION_DESC", label: /* "기본순" */ message("frontend.book.publicReports.sort.default") },
  { value: "LATEST_DESC", label: /* "최신순" */ message("frontend.book.publicReports.sort.latest") },
  { value: "GRADE_DESC", label: /* "별점순" */ message("frontend.home.sort.gradeDesc") },
  { value: "LIKE_DESC", label: /* "추천순" */ message("frontend.book.publicReports.sort.recommended") },
];

type PublicReportPageState = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

/**
 * get Report Status 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param report report 입력값
 * @return 처리 결과
 */
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
 * @author HanWon.Jang
 * @return 공개 독후감 목록 페이지 컴포넌트
 */
const PublicReportPage = () => {

  const location = useLocation();
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam, rondNumb: rondNumbParam } = useParams();
  const [searchParams] = useSearchParams();
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>(
    {},
  );
  const clubNumb = Number(clubNumbParam);
  const rondNumb = Number(rondNumbParam);
  const isClubRoundRoute = Number.isFinite(clubNumb) && clubNumb > 0
    && Number.isFinite(rondNumb) && rondNumb > 0;
  const [sort, setSort] = useState<PublicReportSortType>(
    isClubRoundRoute ? "LATEST_DESC" : "RELATION_DESC",
  );
  const [status, setStatus] = useState<ReportStatus>("ALL");
  const [commentReport, setCommentReport] = useState<PublicReportType | null>(
    null,
  );

  const isbn = searchParams.get("isbn") ?? "";
  const isValidIsbn = isbn.trim().length > 0;
  const publicReportsQuery = usePublicReportsByIsbn(
    isbn,
    sort,
    status,
    !isClubRoundRoute && isValidIsbn,
  );
  const clubReportsQuery = useReadingRoundReports(
    clubNumb,
    rondNumb,
    sort,
    isClubRoundRoute,
  );
  const reportStatusCodeQuery = useCodeList(REPORT_STATUS_CODE_GROUP);
  const likeMutation = usePublicReportLike();
  const routePageState = (location.state ?? {}) as PublicReportPageState;
  const clubReportSummary = clubReportsQuery.data?.pages[0]?.data;
  const pageState: PublicReportPageState = isClubRoundRoute
    ? {
      title: clubReportSummary?.bookTitl ?? routePageState.title,
      author: clubReportSummary?.bookAthr ?? routePageState.author,
      cover: clubReportSummary?.bookCvim ?? routePageState.cover,
      ratingAverage: clubReportSummary?.ratingAverage ?? routePageState.ratingAverage,
    }
    : routePageState;
  const reports = useMemo(() => {
    // 현재 경로에 맞는 공개 목록 또는 모임 회차 DONE 독후감 페이지를 화면 순서대로 연결한다
    if (isClubRoundRoute) {
      return clubReportsQuery.data?.pages.flatMap(
        (page) => page.data?.reportPage.list ?? [],
      ) ?? [];
    }

    // 도서별 공개 독후감 서버 페이지를 화면 순서대로 연결해 반환한다
    return publicReportsQuery.data?.pages.flatMap((page) => page.data?.list ?? []) ?? [];
  }, [clubReportsQuery.data, isClubRoundRoute, publicReportsQuery.data]);

  const statusOptions = useMemo<readonly CustomSelectOption<ReportStatus>[]>(() => {
    // 전체 옵션은 화면 전용 값으로 두고 읽는 중 상태를 제외한 READ_STAT 상세코드의 사용 순서를 따릅니다.
    return [
      { value: "ALL", label: /* "전체" */ message("frontend.common.all") },
      ...(reportStatusCodeQuery.data ?? [])
        .filter((code) => code.comdCode.toUpperCase() !== "READ")
        .map((code) => ({
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

  const displayedReports = reports;
  const activeReportsQuery = isClubRoundRoute ? clubReportsQuery : publicReportsQuery;
  const hasNextReport = Boolean(activeReportsQuery.hasNextPage);

  /**
   * handle Toggle Report 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param reptNumb rept Numb 입력값
   * @return 반환값이 없다
   */
  const handleToggleReport = (reptNumb: number) => {

    setExpandedReports((prev) => ({
      ...prev,
      [reptNumb]: !prev[reptNumb],
    }));
  };

  /**
   * get Count Label 정보를 조회한다
   *
   * @author HanWon.Jang
   * @param countValue count Value 입력값
   * @return 처리 결과
   */
  const getCountLabel = (countValue?: number) => {

    const count = Number(countValue) || 0;
    return count > 999 ? "999+" : String(count);
  };

  /**
   * handle Profile Click 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param userNumb user Numb 입력값
   * @return 반환값이 없다
   */
  const handleProfileClick = (userNumb: number) => {

    if (userNumb) {
      navigate(`/social/profile/${userNumb}`);
    }
  };

  /**
   * get Status Class Name 정보를 조회한다
   *
   * @author HanWon.Jang
   * @param reportStatus report Status 입력값
   * @return 처리 결과
   */
  const getStatusClassName = (reportStatus: string) => {

    if (reportStatus === "DONE") {
      return styles.statusDone;
    }

    if (reportStatus === "STOP") {
      return styles.statusStopped;
    }

    return styles.statusReading;
  };

  if (!isClubRoundRoute && !isValidIsbn) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (activeReportsQuery.isPending) {
    return <Loading />;
  }

  if (activeReportsQuery.isError) {
    return (
      /* 공개 독후감 조회 실패 안내 영역 */
      <main className={styles.page}>
        <Container className={styles.content}>
          <p className={styles.empty}>
            {getApiErrorMessage(
              activeReportsQuery.error,
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
              <div className={styles.coverFrame}>
                <img
                  className={styles.coverImage}
                  src={getBookCoverImageSource(pageState.cover)}
                  onError={handleBookCoverImageError}
                  alt={pageState.title ?? message("frontend.common.bookInfo")}
                />
              </div>
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
                        <img src={"/img/icons/icon-star-rate.svg"} alt="" aria-hidden="true" width={"14px"}/>
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
          <section className={styles.filters} aria-label={message("frontend.book.publicReports.filterLabel")}>
            <CustomSelect
              value={sort}
              options={SORT_OPTIONS}
              ariaLabel={message("frontend.home.sort.label")}
              onChange={setSort}
            />
            {!isClubRoundRoute ? (
              <CustomSelect
                value={status}
                options={statusOptions}
                ariaLabel={message("frontend.report.field.status")}
                onChange={setStatus}
              />
            ) : null}
          </section>

          {displayedReports.length > 0 ? (
            /* 공개 독후감 목록 영역 */
            <section className={styles.list}>
              {displayedReports.map((report) => {

                const rating = Math.max(
                  0,
                  Math.min(5, Number(report.reptGrde) || 0),
                );
                const reportStatus = getReportStatus(report);
                const isExpanded = Boolean(expandedReports[report.reptNumb]);
                const reportContent = report.reptCntn?.trim() ?? "";
                const hasReportContent = reportContent.length > 0;
                const isLongContent =
                  hasReportContent && reportContent.length > CONTENT_PREVIEW_LENGTH;

                return (
                  /* 공개 독후감 개별 항목 영역 */
                  <article className={styles.item} key={report.reptNumb}>
                    <div className={styles.itemTop}>
                      <div className={styles.statusWrap}>

                      <button className={styles.profileButton} type="button" onClick={() => handleProfileClick(report.userNumb)}
                      >
                        <ProfileImage className={styles.profileImage} src={report.porfPath} alt=""
                        />
                        <span className={styles.writer}>
                          {report.userNick || "-"}
                        </span>
                      </button>

                      {/* 독서 상태 */}
                      <span
                          className={getStatusClassName(reportStatus)}
                      >
                          {report.reptStatName || statusNameByCode.get(reportStatus) || reportStatus}
                        </span>
                      </div>

                        {/* 신고 및 차단 더보기 메뉴 */}
                        <div className={styles.actionMenuWrap}>
                          <UserActionMenu
                            userNick={report.userNick}
                            triggerIconClassName={styles.actionMenuTriggerIcon}
                            reportTarget={{
                              targetType: "REPORT",
                              targetNumb: report.reptNumb,
                              reportNumb: report.reptNumb,
                              userNumb: report.userNumb,
                              userNick: report.userNick,
                              content: report.reptCntn,
                            }}
                          />
                        </div>

                    </div>

                    <div>

                    {/* 별점 */}
                    <span
                        className={styles.reportRating}
                        aria-label={message("frontend.report.gradeValue", [
                          rating,
                        ])}
                    >
                      {Array.from({ length: 5 }, (_, index) => {
                        const starValue = index + 1;
                        const iconName = rating >= starValue
                          ? "icon-star-rate"
                          : rating >= starValue - 0.5
                            ? "icon-star-rate-half"
                            : "icon-star-rate-empty";

                        return (
                          <img
                            className={styles.reportRatingIcon}
                            key={starValue}
                            src={`/img/icons/${iconName}.svg`}
                            alt=""
                            aria-hidden="true"
                          />
                        );
                      })}
                    </span>

                    </div>

                    {hasReportContent ? (
                      /* 공개 독후감 본문과 긴 내용 펼치기 영역 */
                      <>
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
                                ? "frontend.common.collapse"
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
                              alt=""
                              aria-hidden="true"
                            />
                          </button>
                        ) : null}
                      </>
                    ) : null}

                    <div className={styles.itemMetrics}>
                      <button
                        className={styles.metricButton}
                        type="button"
                        aria-label={message("frontend.common.like")}
                        aria-pressed={report.likeYsno === "Y"}
                        disabled={likeMutation.isPending}
                        onClick={() =>
                          likeMutation.mutate({
                            tagtType: "REPORT",
                            tagtNumb: report.reptNumb,
                          })
                        }
                      >
                        {
                          report.likeYsno === "Y"
                              ? <img src={"/img/icons/icon-heart-fill.svg"} alt="" aria-hidden="true"/>
                              : <img src={"/img/icons/icon-heart.svg"} alt="" aria-hidden="true"/>
                        }

                        <span>{getCountLabel(report.likeCnt)}</span>
                      </button>
                      <button
                        className={styles.commentButton}
                        type="button"
                        aria-label={message("frontend.book.publicReports.viewComments")}
                        onClick={() => setCommentReport(report)}
                      >
                        <img src={"/img/icons/icon-comment.svg"} alt="" aria-hidden="true"/>
                        <span>
                          {getCountLabel(report.replCnt)}
                        </span>
                      </button>
                    </div>
                  </article>
                );
              })}
              <InfiniteScrollTrigger
                hasNext={hasNextReport}
                isLoading={activeReportsQuery.isFetchingNextPage}
                onLoadMore={() => {
                  // 목록 하단에 도달하면 현재 경로의 다음 독후감 서버 페이지를 조회한다
                  void activeReportsQuery.fetchNextPage();
                }}
              />
            </section>
          ) : (
            <p className={styles.empty}>
              {reports.length > 0
                ? /* "선택한 조건에 맞는 독후감이 없어요." */ message("frontend.book.publicReports.filteredEmpty")
                : message(
                  isClubRoundRoute
                    ? "frontend.readingClub.result.noDoneReports"
                    : "frontend.book.publicReports.empty",
                )}
            </p>
          )}
        </div>
      </main>
      {commentReport ? (
        <ReplySheet
          report={commentReport}
          onClose={() => setCommentReport(null)}
        />
      ) : null}
    </>
  );
};

export default PublicReportPage;
