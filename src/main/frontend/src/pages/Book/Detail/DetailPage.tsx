/**
 * src/main/frontend/src/pages/Book/Detail/DetailPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { useNavigate, useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { useState } from "react";
import { clsx } from "clsx";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { usePublicReportLikeMutation } from "@/features/Book/Detail/hook/usePublicReports";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { REPORT_STATUS_READ } from "@/features/Book/constants/reportForm";
import * as styles from "./DetailPage.css";
import * as infoStyles from "@/pages/Book/Info/BookInfoPage.css";

function RatingStars({ grade }: { grade: string }) {
  const rawRating = Number(grade);
  const rating = Number.isFinite(rawRating)
    ? Math.max(0, Math.min(5, rawRating))
    : 0;
  const starCount = Math.floor(rating);

  return (
    <div
      className={styles.stars}
      aria-label={message("frontend.report.gradeValue", [rating])}
    >
      {[1, 2, 3, 4, 5].map((value) => (
        <span
          key={value}
          className={value <= starCount ? styles.starFilled : undefined}
        >
          {"\u2605"}
        </span>
      ))}
    </div>
  );
}

function DetailPage() {
  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();
  const { data, isPending } = useBookDetail(idNum);
  const likeMutation = usePublicReportLikeMutation();
  const [showBookInfo, setShowBookInfo] = useState(false);

  const goUpdatePage = (reportNumb: number) => {
    // 상세에서 수정으로 진입한 뒤 저장하면 다시 상세로 이동한다.
    // 이때 기존 상세 히스토리를 남기면 뒤로가기 시 같은 상세 화면으로 돌아오므로 수정 진입 시 현재 상세 엔트리를 교체한다.
    navigate(`/book/upt/${reportNumb}`, { replace: true });
  };

  const showBookInfoView = () => setShowBookInfo(true);

  const getLikeCountLabel = (likeCnt?: number) => {
    const count = Number(likeCnt) || 0;
    return count > 99 ? "99+" : String(count);
  };

  if (data?.code === 2004) {
    return <div>{data.message}</div>;
  }

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
  }

  const bookData = data?.data;

  if (data?.code !== 200 || !bookData) {
    return <h3>{data?.message}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${bookData.bookCvim}")`,
  } as CSSProperties;
  const isReadingStatus = bookData.reportStat === REPORT_STATUS_READ;
  const startDateLabel = isReadingStatus
    ? message("frontend.report.field.targetStartDate")
    : message("frontend.report.field.startDate");
  const endDateLabel = isReadingStatus
    ? message("frontend.report.field.targetEndDate")
    : message("frontend.report.field.endDate");

  // 같은 상세 API에서 받은 책 정보를 사용하므로 URL 이동 없이 화면 표시 모드만 변경합니다.
  if (showBookInfo) {
    return (
      <main className={infoStyles.page} style={pageStyle}>
        <Container className={clsx(infoStyles.content, styles.viewFade)}>
          <section className={infoStyles.header}>
            <div className={infoStyles.coverFrame}>
              <img
                className={infoStyles.coverImage}
                src={bookData.bookCvim}
                alt={bookData.bookTitl}
              />
            </div>
            <h1 className={infoStyles.title}>{bookData.bookTitl}</h1>
            <div className={infoStyles.authorRatingLine}>
              <p className={infoStyles.meta}>{bookData.bookAthr}</p>
              {bookData.bookAvgGrde && (
                <span className={infoStyles.metaSeparator}>|</span>
              )}
              {bookData.bookAvgGrde && (
                <span
                  className={infoStyles.ratingSummary}
                  aria-label={message("frontend.report.gradeValue", [
                    bookData.bookAvgGrde,
                  ])}
                >
                  <span className={infoStyles.ratingStar}>{"\u2605"}</span>
                  <span className={infoStyles.ratingValue}>
                    {bookData.bookAvgGrde}
                  </span>
                </span>
              )}
            </div>
            <div className={infoStyles.bookInfoActionRow}>
              <button
                className={infoStyles.reportBackButton}
                type="button"
                onClick={() => setShowBookInfo(false)}
              >
                <svg
                  className={infoStyles.reportBackIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M15 6 9 12l6 6"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                {message("frontend.report.backToReport")}
              </button>
              <button
                className={infoStyles.bookInfoButton}
                type="button"
                onClick={() =>
                  navigate(
                    `/book/public-reports/isbn?isbn=${encodeURIComponent(
                      bookData.bookIsbn,
                    )}`,
                    {
                      state: {
                        title: bookData.bookTitl,
                        author: bookData.bookAthr,
                        cover: bookData.bookCvim,
                        ratingAverage: bookData.bookAvgGrde,
                      },
                    },
                  )
                }
              >
                {message("frontend.book.publicReports.button")}
              </button>
            </div>
          </section>

          <div
            className={clsx(infoStyles.contentPanel, styles.contentSwitchFade)}
          >
            <section className={infoStyles.section}>
              <h2 className={infoStyles.sectionTitle}>
                {message("frontend.common.bookInfo")}
              </h2>
              <div className={infoStyles.infoGrid}>
                <span className={infoStyles.infoLabel}>
                  {message("frontend.common.author")}
                </span>
                <p className={infoStyles.infoValue}>
                  {bookData.bookAthr || "-"}
                </p>
                <span className={infoStyles.infoLabel}>
                  {message("frontend.common.publisher")}
                </span>
                <p className={infoStyles.infoValue}>
                  {bookData.bookPubl || "-"}
                </p>
                <span className={infoStyles.infoLabel}>
                  {message("frontend.common.publDate")}
                </span>
                <p className={infoStyles.infoValue}>
                  {bookData.publDate || "-"}
                </p>
              </div>
            </section>

            <section className={infoStyles.section}>
              <h2 className={infoStyles.sectionTitle}>
                {message("frontend.common.bookDescription")}
              </h2>
              <p className={infoStyles.description}>
                {bookData.bookDesc ||
                  message("frontend.common.noBookDescription")}
              </p>
            </section>
          </div>
        </Container>
      </main>
    );
  }

  return (
    <main className={styles.page} style={pageStyle}>
      <Container className={clsx(styles.detail, styles.viewFade)}>
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookData.bookCvim}
              alt={bookData.bookTitl}
            />
          </div>
          <h1 className={styles.title}>{bookData.bookTitl}</h1>
          <p className={styles.meta}>{bookData.bookAthr}</p>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={showBookInfoView}
          >
            {message("frontend.report.bookInfoMore")}
          </button>
        </section>

        <div className={clsx(styles.contentPanel, styles.contentSwitchFade)}>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.status")}
            </h2>
            <div className={styles.statusPill}>
              {bookData.reportStatName || bookData.reportStat}
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.public")}
            </h2>
            <div className={styles.statusPill}>
              {bookData.pubcYsnoName ||
                (bookData.pubcYsno === "Y"
                  ? message("frontend.report.public.on")
                  : message("frontend.report.public.off"))}
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.period")}
            </h2>
            <div className={styles.dateStack}>
              <div className={styles.dateRow}>
                <span className={styles.dateLabel}>
                  {startDateLabel}
                </span>
                <span className={styles.dateValue}>
                  {bookData.reportStdt || "-"}
                </span>
              </div>
              <div className={styles.dateRow}>
                <span className={styles.dateLabel}>
                  {endDateLabel}
                </span>
                <span className={styles.dateValue}>
                  {bookData.reportEndt || "-"}
                </span>
              </div>
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {message("frontend.report.field.grade")}
            </h2>
            <RatingStars grade={bookData.reportGrde} />
          </section>

          <section className={styles.section}>
            <div className={styles.sectionTitleRow}>
              <h2 className={styles.sectionTitle}>
                {message("frontend.report.field.content")}
              </h2>
              <button
                className={styles.likeButton}
                type="button"
                aria-label="좋아요"
                aria-pressed={bookData.likeYsno === "Y"}
                disabled={likeMutation.isPending}
                onClick={() => likeMutation.mutate(idNum)}
              >
                <svg
                  className={styles.likeIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M12 20.4S4.5 16.1 3.1 10.6C2.2 7 4.3 4.5 7.1 4.5c1.7 0 3.2.9 4.1 2.2.9-1.3 2.4-2.2 4.1-2.2 2.8 0 4.9 2.5 4 6.1C17.9 16.1 12 20.4 12 20.4Z"
                    fill={bookData.likeYsno === "Y" ? "currentColor" : "none"}
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinejoin="round"
                  />
                </svg>
                <span className={styles.likeCount}>
                  {getLikeCountLabel(bookData.likeCnt)}
                </span>
              </button>
            </div>
            <p className={styles.contentBox}>
              {bookData.reportCntn || message("frontend.common.noWrittenReport")}
            </p>
          </section>

          <div className={styles.actions}>
            <button
              className={styles.actionButton}
              type="button"
              onClick={() => goUpdatePage(idNum)}
            >
              <svg
                className={styles.buttonIcon}
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  d="M5 19h3.2L18.7 8.5a1.7 1.7 0 0 0 0-2.4l-.8-.8a1.7 1.7 0 0 0-2.4 0L5 15.8V19Z"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinejoin="round"
                />
                <path
                  d="M14.3 6.5l3.2 3.2M4 21h16"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                />
              </svg>
              {message("frontend.report.update")}
            </button>
          </div>
        </div>
      </Container>
    </main>
  );
}

export default DetailPage;
