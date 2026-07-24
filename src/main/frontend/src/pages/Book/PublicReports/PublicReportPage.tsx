import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import {
  usePublicReportLikeMutation,
  usePublicReportsByIsbn,
} from "@/features/Book/Detail/hook/usePublicReports";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import * as styles from "./PublicReportPage.css";

const CONTENT_PREVIEW_LENGTH = 180;
const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

type PublicReportPageState = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

/**
 * 선택한 책과 같은 책에 작성된 공개 독후감 목록을 표시합니다.
 * 작성자 프로필, 별점, 좋아요 상태, 독후감 내용을 한 화면에서 확인할 수 있습니다.
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
  const [isRatingTooltipOpen, setIsRatingTooltipOpen] = useState(false);
  const ratingTooltipRef = useRef<HTMLDivElement | null>(null);

  const isbn = searchParams.get("isbn") ?? "";
  const isValidIsbn = isbn.trim().length > 0;

  const publicReportsQuery = usePublicReportsByIsbn(isbn, isValidIsbn);
  const likeMutation = usePublicReportLikeMutation();
  const pageState = (location.state ?? {}) as PublicReportPageState;
  const ratingAverageValue = pageState.ratingAverage;
  const hasRatingAverage =
    ratingAverageValue !== null &&
    ratingAverageValue !== undefined &&
    ratingAverageValue !== "";

  const reports = useMemo(() => {
    return (publicReportsQuery.data?.data ?? []) as PublicReportType[];
  }, [publicReportsQuery.data]);

  /**
   * 평점 평균 설명 말풍선이 열린 상태에서 다른 영역을 누르면 말풍선을 닫습니다.
   * 평균 평점 버튼과 말풍선 내부 클릭은 같은 영역으로 보아 닫지 않습니다.
   *
   * @author Hanwon.Jang
   * @return
   */
  useEffect(() => {
    const handleDocumentPointerDown = (event: PointerEvent) => {
      if (!isRatingTooltipOpen) {
        return;
      }

      const target = event.target;

      if (
        ratingTooltipRef.current &&
        target instanceof Node &&
        ratingTooltipRef.current.contains(target)
      ) {
        return;
      }

      setIsRatingTooltipOpen(false);
    };

    document.addEventListener("pointerdown", handleDocumentPointerDown);

    return () => {
      document.removeEventListener("pointerdown", handleDocumentPointerDown);
    };
  }, [isRatingTooltipOpen]);

  /**
   * 긴 독후감 내용의 펼침 상태를 독후감 번호 기준으로 전환합니다.
   *
   * @author Hanwon.Jang
   * @param reptNumb 펼치거나 접을 독후감 번호
   */
  const handleToggleReport = (reptNumb: number) => {
    setExpandedReports((prev) => ({
      ...prev,
      [reptNumb]: !prev[reptNumb],
    }));
  };

  /**
   * 좋아요 수가 99개를 넘으면 화면 폭을 보호하기 위해 99+로 축약합니다.
   *
   * @author Hanwon.Jang
   * @param likeCnt 서버에서 조회한 좋아요 수
   * @return 화면에 표시할 좋아요 수 문자열
   */
  const getLikeCountLabel = (likeCnt?: number) => {
    const count = Number(likeCnt) || 0;
    return count > 99 ? "99+" : String(count);
  };

  /**
   * 공개 독후감 작성자 프로필 화면으로 이동합니다.
   * 사용자 번호가 없는 비정상 데이터는 라우팅하지 않아 잘못된 프로필 화면 진입을 방지합니다.
   *
   * @author Hanwon.Jang
   * @param userNumb 이동할 작성자 사용자 번호
   */
  const handleProfileClick = (userNumb: number) => {
    if (!userNumb) {
      return;
    }

    navigate(`/social/profile/${userNumb}`);
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
    <main className={styles.page}>
      <Container className={styles.content}>
        <section className={styles.header}>
          {pageState.cover && (
            <div className={styles.coverFrame}>
              <img
                className={styles.coverImage}
                src={pageState.cover}
                alt={pageState.title ?? message("frontend.common.bookInfo")}
              />
            </div>
          )}
          <div className={styles.headingArea}>
            {pageState.title && (
              <h1 className={styles.bookTitle}>{pageState.title}</h1>
            )}
            {pageState.author && (
              <div className={styles.authorRatingLine}>
                <p className={styles.meta}>{pageState.author}</p>
                {hasRatingAverage && (
                  <span className={styles.metaSeparator}>|</span>
                )}
                {hasRatingAverage && (
                  <div
                    className={styles.ratingTooltipWrap}
                    ref={ratingTooltipRef}
                  >
                    <button
                      className={styles.ratingSummary}
                      type="button"
                      aria-label={message("frontend.report.gradeValue", [
                        ratingAverageValue,
                      ])}
                      aria-expanded={isRatingTooltipOpen}
                      onClick={() =>
                        setIsRatingTooltipOpen((prev) => !prev)
                      }
                    >
                      <span className={styles.ratingStar}>{"\u2605"}</span>
                      <span className={styles.ratingValue}>
                        {ratingAverageValue}
                      </span>
                    </button>
                    {isRatingTooltipOpen && (
                      <div className={styles.ratingTooltip} role="tooltip">
                        {message("frontend.book.ratingAverageHelp")}
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        </section>

        {reports.length > 0 ? (
          <section className={styles.list}>
            {reports.map((report) => {
              const rawRating = Number(report.reptGrde);
              const rating = Number.isFinite(rawRating)
                ? Math.max(0, Math.min(5, rawRating))
                : 0;
              const starCount = Math.floor(rating);
              const isExpanded = Boolean(expandedReports[report.reptNumb]);
              const content =
                report.reptCntn || message("frontend.common.noWrittenReport");
              const isLongContent = content.length > CONTENT_PREVIEW_LENGTH;

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
                      <span className={styles.metaSeparator}>|</span>
                      <span
                        className={styles.stars}
                        aria-label={message("frontend.report.gradeValue", [
                          rating,
                        ])}
                      >
                        {Array.from({ length: 5 }, (_, index) => (
                          <span
                            className={
                              index < starCount ? styles.starFilled : undefined
                            }
                            key={index}
                          >
                            {"\u2605"}
                          </span>
                        ))}
                      </span>
                    </div>
                    <button
                      className={styles.likeButton}
                      type="button"
                      aria-label="좋아요"
                      aria-pressed={report.likeYsno === "Y"}
                      disabled={likeMutation.isPending}
                      onClick={() =>
                        likeMutation.mutate({
                          tagtType: "REPORT",
                          tagtNumb: report.reptNumb,
                        })
                      }
                    >
                      <svg
                        className={styles.likeIcon}
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path
                          d="M12 20.4S4.5 16.1 3.1 10.6C2.2 7 4.3 4.5 7.1 4.5c1.7 0 3.2.9 4.1 2.2.9-1.3 2.4-2.2 4.1-2.2 2.8 0 4.9 2.5 4 6.1C17.9 16.1 12 20.4 12 20.4Z"
                          fill={
                            report.likeYsno === "Y" ? "currentColor" : "none"
                          }
                          stroke="currentColor"
                          strokeWidth="1.8"
                          strokeLinejoin="round"
                        />
                      </svg>
                      <span className={styles.likeCount}>
                        {getLikeCountLabel(report.likeCnt)}
                      </span>
                    </button>
                  </div>

                  <div
                    className={
                      isExpanded || !isLongContent
                        ? styles.reportContentWrapOpen
                        : styles.reportContentWrap
                    }
                  >
                    <p className={styles.reportContent}>{content}</p>
                  </div>

                  {isLongContent && (
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
                      <span
                        className={
                          isExpanded
                            ? styles.expandArrowOpen
                            : styles.expandArrow
                        }
                        aria-hidden="true"
                      >
                        <svg
                          className={styles.expandArrowIcon}
                          viewBox="0 0 24 24"
                          focusable="false"
                        >
                          <path d="M7.4 9.6 12 14.2l4.6-4.6 1.4 1.4-6 6-6-6 1.4-1.4Z" />
                        </svg>
                      </span>
                    </button>
                  )}
                </article>
              );
            })}
          </section>
        ) : (
          <p className={styles.empty}>
            {message("frontend.book.publicReports.empty")}
          </p>
        )}
      </Container>
    </main>
  );
}

export default PublicReportPage;
