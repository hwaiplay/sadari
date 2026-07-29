/**
 * src/main/frontend/src/pages/Book/Detail/DetailPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import { useNavigate, useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { useState } from "react";
import { clsx } from "clsx";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { usePublicReportLikeMutation } from "@/features/Book/Detail/hook/usePublicReports";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import {
  REPORT_STATUS_DONE,
  REPORT_STATUS_READ,
  REPORT_STATUS_STOP,
} from "@/features/Book/constants/reportForm";
import * as styles from "./DetailPage.css";

const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
// 댓글 API 연결 전 기록 헤더의 댓글 수 배치를 확인하기 위한 임시 표시값이다
const TEMPORARY_COMMENT_COUNT = 0;

/**
 * 독서일 문자열을 날짜 차이 계산에 사용할 UTC 기준 일련번호로 변환한다
 *
 * @author HanWon.Jang
 * @param value YYYY-MM-DD 형식의 독서일
 * @return UTC 기준 일련번호 또는 유효하지 않은 날짜의 null
 */
function getDateSerial(value?: string) {

  // 날짜가 없으면 독서일 수를 계산할 수 없으므로 빈 결과를 사용한다
  if (!value) {
    // 유효한 날짜가 없음을 반환한다
    return null;
  }

  // 하이픈 날짜를 연월일 숫자로 분리해 브라우저 시간대 파싱 차이를 제거한다
  const [year, month, day] = value.split("-").map(Number);

  // 연월일 중 하나라도 유효하지 않으면 잘못된 기간을 화면에 계산하지 않는다
  if (!year || !month || !day) {
    // 유효한 날짜가 없음을 반환한다
    return null;
  }

  // 일광 절약 시간의 영향을 받지 않는 UTC 기준 일련번호를 반환한다
  return Date.UTC(year, month - 1, day);
}

/**
 * 오늘 날짜를 독서일 수 계산에 사용할 UTC 기준 일련번호로 변환한다
 *
 * @author HanWon.Jang
 * @return 오늘의 UTC 기준 일련번호
 */
function getTodayDateSerial() {

  // 현재 사용자의 로컬 날짜를 기준으로 읽는 중인 독서일 수를 계산한다
  const today = new Date();

  // 시간 정보 없이 오늘 연월일에 해당하는 UTC 기준 일련번호를 반환한다
  return Date.UTC(today.getFullYear(), today.getMonth(), today.getDate());
}

/**
 * 독서 상태에 따라 완료된 독서일 수 또는 현재 독서일 차수를 표시한다
 *
 * @author HanWon.Jang
 * @param startDate 독서 시작일
 * @param endDate 독서 종료일 또는 목표 종료일
 * @param isReadingStatus 현재 읽는 중인 상태 여부
 * @return N일 또는 N일째 형식의 독서기간 요약
 */
function getReadingDurationLabel(startDate: string, endDate: string, isReadingStatus: boolean) {

  // 독서 시작일을 날짜 차이 계산 기준으로 변환한다
  const startSerial = getDateSerial(startDate);
  // 읽는 중이면 오늘을 사용하고 완료 또는 중단이면 저장된 종료일을 사용한다
  const endSerial = isReadingStatus
    ? getTodayDateSerial()
    : getDateSerial(endDate);

  // 날짜가 비어 있거나 종료일이 시작일보다 빠르면 잘못된 일 수를 표시하지 않는다
  if (startSerial === null || endSerial === null || endSerial < startSerial) {
    // 계산할 수 없는 독서기간 표시값을 반환한다
    return "-";
  }

  // 시작일과 종료일을 모두 독서일에 포함해 최소 1일부터 계산한다
  const durationDays = Math.floor((endSerial - startSerial) / MILLISECONDS_PER_DAY) + 1;

  // 읽는 중인 독후감은 오늘이 몇 번째 독서일인지 표시한다
  if (isReadingStatus) {
    // "{0}일째"
    return message("frontend.report.period.inProgressDays", [durationDays]);
  }

  // "{0}일"
  return message("frontend.report.period.completedDays", [durationDays]);
}

/**
 * 독서 시작일과 종료일을 상세 기간 표시 문자열로 조합한다
 *
 * @author HanWon.Jang
 * @param startDate 독서 시작일
 * @param endDate 독서 종료일 또는 목표 종료일
 * @return 점으로 구분된 실제 독서기간
 */
function getReadingPeriodText(startDate: string, endDate: string) {

  // 독서 시작일을 화면 표시 형식으로 변환한다
  const formattedStartDate = formatDashedDateToDot(startDate);
  // 독서 종료일을 화면 표시 형식으로 변환한다
  const formattedEndDate = formatDashedDateToDot(endDate);
  // 존재하는 날짜만 사용해 불필요한 기간 구분자가 나오지 않게 한다
  const periodText = [formattedStartDate, formattedEndDate].filter(Boolean).join(" ~ ");

  // 실제 날짜가 없으면 대체 문자를 표시한다
  return periodText || "-";
}

/**
 * 독후감 평점을 통계 영역의 간결한 점수 형식으로 변환한다
 *
 * @author HanWon.Jang
 * @param grade 서버에서 조회한 독후감 평점
 * @return 숫자 형식의 평점 또는 유효하지 않은 평점의 대체 문자
 */
function getGradeLabel(grade: string) {

  // 문자열 평점을 화면 계산에 사용할 숫자로 변환한다
  const numericGrade = Number(grade);

  // 숫자가 아닌 평점은 통계 영역에 잘못된 값을 표시하지 않는다
  if (!Number.isFinite(numericGrade)) {
    // 유효하지 않은 평점의 대체 문자를 반환한다
    return "-";
  }

  // 정수 평점은 불필요한 소수점을 제거하고 반점 평점은 한 자리까지 유지한다
  const gradeText = Number.isInteger(numericGrade)
    ? String(numericGrade)
    : numericGrade.toFixed(1);

  // "{0}"
  return message("frontend.report.gradeCompact", [gradeText]);
}

/**
 * 서버의 공개 여부 코드와 코드명을 화면 표시값으로 변환한다
 *
 * @author HanWon.Jang
 * @param publicName 서버에서 조회한 공개 여부 코드명
 * @param publicCode 공개 여부 Y/N 코드
 * @return 공개 또는 비공개 표시값
 */
function getPublicLabel(publicName?: string, publicCode?: "Y" | "N") {

  // 서버가 공통코드명을 제공하면 동일한 표시 정책을 그대로 사용한다
  if (publicName) {
    // 서버에서 조회한 공개 여부 코드명을 반환한다
    return publicName;
  }

  // 공개 코드이면 공개 상태 문구를 사용한다
  if (publicCode === "Y") {
    // "공개"
    return message("frontend.report.public.on");
  }

  // "비공개"
  return message("frontend.report.public.off");
}

/**
 * 독서 상태에 맞는 통계 글자색 클래스를 결정한다
 *
 * @author HanWon.Jang
 * @param reportStatus 독후감 독서 상태 코드
 * @return 읽는 중과 완독 및 중단 상태에 맞는 글자색 클래스
 */
function getReportStatusClassName(reportStatus: string) {

  // 완독 상태는 긍정적인 완료 의미를 전달하는 연녹색을 사용한다
  if (reportStatus === REPORT_STATUS_DONE) {
    // 완독 상태 글자색 클래스를 반환한다
    return styles.reportStatusDone;
  }

  // 중단 상태는 완료와 구분되는 연한 빨간색을 사용한다
  if (reportStatus === REPORT_STATUS_STOP) {
    // 중단 상태 글자색 클래스를 반환한다
    return styles.reportStatusStop;
  }

  // 읽는 중과 알 수 없는 상태는 기본 검정 글자색 클래스를 반환한다
  return styles.reportStatusRead;
}

/**
 * Detail Page 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function DetailPage() {

  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();
  const { data, error, isError, isPending } = useBookDetail(idNum);
  const likeMutation = usePublicReportLikeMutation();
  const [showBookInfo, setShowBookInfo] = useState(false);
  const [isPeriodDetailOpen, setIsPeriodDetailOpen] = useState(false);

  /**
   * go Update Page 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param reptNumb rept Numb 입력값
   * @return 반환값이 없다
   */
  const goUpdatePage = (reptNumb: number) => {
    // 상세에서 수정으로 진입한 뒤 저장하면 다시 상세로 이동한다.
    // 이때 기존 상세 히스토리를 남기면 뒤로가기 시 같은 상세 화면으로 돌아오므로 수정 진입 시 현재 상세 엔트리를 교체한다.
    navigate(`/book/upt/${reptNumb}`, { replace: true });
  };

  /**
   * 독후감 상세에서 도서 정보 화면으로 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const showBookInfoView = () => {

    // 같은 상세 페이지 안에서 도서 정보가 보이도록 화면 상태를 변경한다
    setShowBookInfo(true);
  };

  /**
   * 모바일과 클릭 환경에서 실제 독서기간 툴팁을 열거나 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handlePeriodDetailToggle = () => {

    // 이전 열림 상태를 반전해 같은 버튼으로 기간 확인과 닫기를 모두 제공한다
    setIsPeriodDetailOpen(!isPeriodDetailOpen);
  };

  /**
   * 독서기간 통계에서 포커스가 벗어나면 클릭으로 연 툴팁을 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handlePeriodDetailClose = () => {

    // 다른 화면 요소로 이동한 뒤 기간 툴팁이 남지 않게 닫는다
    setIsPeriodDetailOpen(false);
  };

  /**
   * get Like Count Label 정보를 조회한다
   *
   * @author HanWon.Jang
   * @param likeCnt like Cnt 입력값
   * @return 처리 결과
   */
  const getLikeCountLabel = (likeCnt?: number) => {

    const count = Number(likeCnt) || 0;
    return count > 99 ? "99+" : String(count);
  };

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
  }

  if (isError) {
    return <h3>{getApiErrorMessage(error, message("frontend.common.tryAgain"))}</h3>;
  }

  const bookData = data?.data;

  if (!bookData) {
    return <h3>{data?.message}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${bookData.bookCvim}")`,
  } as CSSProperties;
  const isReadingStatus = bookData.reptStat === REPORT_STATUS_READ;
  // 읽는 중인 독후감은 저장된 종료일이 목표일이므로 목표 독서기간으로 구분한다
  const periodTitle = isReadingStatus
    ? /* "목표 독서기간" */ message("frontend.report.field.targetPeriod")
    : /* "독서 기간" */ message("frontend.report.field.period");
  // 독서 상태에 맞는 N일 또는 N일째 형식의 요약값을 계산한다
  const periodSummary = getReadingDurationLabel(
    bookData.reptStdt,
    bookData.reptEndt,
    isReadingStatus,
  );
  // 툴팁에 표시할 시작일과 종료일의 실제 범위를 조합한다
  const periodText = getReadingPeriodText(bookData.reptStdt, bookData.reptEndt);
  // 통계 영역에 표시할 공개 여부 코드명을 결정한다
  const publicLabel = getPublicLabel(bookData.pubcYsnoName, bookData.pubcYsno);
  // 통계 영역에 표시할 평점 문자열을 결정한다
  const gradeLabel = getGradeLabel(bookData.reptGrde);
  // 독서 상태 코드에 맞는 통계 글자색을 결정한다
  const reportStatusClassName = getReportStatusClassName(bookData.reptStat);
  const rawBookAverageGrade = Number(bookData.bookAvgGrde);
  const hasBookAverageGrade =
    Number.isFinite(rawBookAverageGrade) && rawBookAverageGrade > 0;
  // "등록된 책 소개가 없습니다."
  const bookDescription =
    bookData.bookDesc || message("frontend.common.noBookDescription");

  /**
   * 도서 정보 화면에서 독후감 상세 화면으로 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const showReportDetailView = () => {

    // 같은 상세 페이지 안에서 독후감 정보가 다시 보이도록 화면 상태를 변경한다
    setShowBookInfo(false);
  };

  /**
   * 현재 도서와 같은 ISBN으로 작성된 공개 독후감 목록으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const goPublicReportsPage = () => {

    // 현재 도서의 ISBN을 URL 쿼리에서 사용할 수 있는 문자열로 변환한다
    const encodedBookIsbn = encodeURIComponent(bookData.bookIsbn);

    // 현재 조회된 도서 정보를 전달해 공개 독후감 목록의 헤더를 즉시 구성한다
    navigate(
      `/book/public-reports/isbn?isbn=${encodedBookIsbn}`,
      {
        state: {
          title: bookData.bookTitl,
          author: bookData.bookAthr,
          cover: bookData.bookCvim,
          ratingAverage: bookData.bookAvgGrde,
        },
      },
    );
  };

  /**
   * 현재 독후감의 좋아요 상태를 변경한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLikeToggle = () => {

    // 상세 화면에 이미 조회된 대상 정보를 사용해 별도 독후감 조회 없이 좋아요를 변경한다
    likeMutation.mutate({
      tagtType: "REPORT",
      tagtNumb: idNum,
      targetUserNumb: bookData.userNumb,
    });
  };

  // 같은 상세 API에서 받은 도서 정보를 사용해 추가 조회 없이 도서 정보 화면을 구성한다
  if (showBookInfo) {
    return (
      /* 독후감에 연결된 도서 정보 전체 영역 */
      <main className={styles.page} style={pageStyle}>
        <Container className={styles.detail}>
          {/* 도서 표지와 도서 정보 전환 영역 */}
          <section className={styles.header}>
            <div className={styles.coverFrame}>
              <img
                className={styles.coverImage}
                src={bookData.bookCvim}
                alt={bookData.bookTitl}
              />
            </div>
            <h1 className={styles.title}>{bookData.bookTitl}</h1>

            {/* 독후감 상세의 저자 표시 줄과 높이를 맞춘 도서 평균 평점 영역 */}
            <div className={styles.bookAverageSummary}>
              {hasBookAverageGrade ? (
                <>
                  {/* 평균 평점이 있으면 평균 문구와 별 아이콘 및 점수를 표시한다 */}
                  <span className={styles.bookAverageLabel}>
                    {/* "평균" */}
                    {message("frontend.book.ratingAverageShort")}
                  </span>
                  <svg
                    className={styles.bookAverageStar}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      d="m12 3.5 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.5Z"
                      fill="currentColor"
                      stroke="currentColor"
                      strokeWidth="1.4"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                  <strong className={styles.bookAverageScore}>
                    {bookData.bookAvgGrde}
                  </strong>
                </>
              ) : (
                <span className={styles.bookAverageEmpty}>
                  {/* "아직 별점이 없습니다." */}
                  {message("frontend.book.ratingAverageEmpty")}
                </span>
              )}
            </div>

            {/* 독후감 상세 복귀와 같은 도서의 공개 독후감 이동 영역 */}
            <div className={styles.bookInfoActionRow}>
              <button
                className={styles.bookInfoButton}
                type="button"
                onClick={showReportDetailView}
              >
                <svg
                  className={styles.bookInfoButtonIcon}
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
                {/* "돌아가기" */}
                {message("frontend.report.backToReport")}
              </button>
              <button
                className={styles.bookInfoButton}
                type="button"
                onClick={goPublicReportsPage}
              >
                {/* "다른 사람이 쓴 독후감 보기" */}
                {message("frontend.book.publicReports.button")}
              </button>
            </div>
          </section>

          <div
            key="book-info"
            className={clsx(styles.contentPanel, styles.contentSwitchFade)}
          >
            {/* 저자와 출판사 및 출간일의 세로 요약 영역 */}
            <section
              className={styles.reportStatsSection}
              aria-label={/* "도서 정보" */ message("frontend.common.bookInfo")}
            >
              <div className={styles.bookInfoRows}>
                {/* 도서 저자 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "저자" */}
                    {message("frontend.common.author")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.bookAthr || "-"}
                  </strong>
                </div>

                {/* 도서 출판사 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "출판사" */}
                    {message("frontend.common.publisher")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.bookPubl || "-"}
                  </strong>
                </div>

                {/* 도서 출간일 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "출간일" */}
                    {message("frontend.common.publDate")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.publDate || "-"}
                  </strong>
                </div>
              </div>
            </section>

            {/* 책 소개 제목부터 하단 배경을 흰색으로 유지하는 도서 소개 영역 */}
            <div className={styles.recordArea}>
              {/* 독후감 기록 카드와 같은 위치의 책 소개 영역 */}
              <section className={styles.recordSection}>
                <div className={styles.recordTitleRow}>
                  <h2 className={styles.sectionTitle}>
                    {/* "책 소개" */}
                    {message("frontend.common.bookDescription")}
                  </h2>
                </div>
                <p className={styles.contentBox}>{bookDescription}</p>
              </section>
            </div>
          </div>
        </Container>
      </main>
    );
  }

  return (
    /* 독후감 상세 정보 전체 영역 */
    <main className={styles.page} style={pageStyle}>
      <Container className={styles.detail}>
        {/* 도서 표지와 독후감 전환 영역 */}
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
            {/* "도서 정보 자세히보기" */}
            {message("frontend.report.bookInfoMore")}
          </button>
        </section>

        <div
          key="report-detail"
          className={clsx(styles.contentPanel, styles.contentSwitchFade)}
        >
          {/* 독서 상태와 공개 여부 및 평점과 독서기간 요약 영역 */}
          <section
            className={styles.reportStatsSection}
            aria-label={/* "독후감 요약" */ message("frontend.report.summary.aria")}
          >
            <div className={styles.reportStatsGrid}>
              {/* 독서 상태 통계 영역 */}
              <div className={styles.reportStatsItem}>
                <span className={styles.reportStatsLabel}>
                  {/* "독서 상태" */}
                  {message("frontend.report.field.status")}
                </span>
                <strong
                  className={clsx(
                    styles.reportStatsValue,
                    reportStatusClassName,
                  )}
                >
                  {bookData.reptStatName || bookData.reptStat}
                </strong>
              </div>

              {/* 공개 여부 통계 영역 */}
              <div className={styles.reportStatsItem}>
                <span className={styles.reportStatsLabel}>
                  {/* "공개 여부" */}
                  {message("frontend.report.field.public")}
                </span>
                <strong className={styles.reportStatsValue}>
                  {publicLabel}
                </strong>
              </div>

              {/* 평점 통계 영역 */}
              <div className={styles.reportStatsItem}>
                <span className={styles.reportStatsLabel}>
                  {/* "평점" */}
                  {message("frontend.report.field.grade")}
                </span>
                <strong className={styles.reportGradeValue}>
                  <svg
                    className={styles.reportGradeStar}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      d="m12 3.5 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.5Z"
                      fill="currentColor"
                      stroke="currentColor"
                      strokeWidth="1.4"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                  {gradeLabel}
                </strong>
              </div>

              {/* 독서기간 요약과 실제 날짜 확인 영역 */}
              <div className={styles.reportStatsItem}>
                <span className={styles.reportStatsLabel}>{periodTitle}</span>
                <button
                  className={styles.periodStatButton}
                  type="button"
                  aria-expanded={isPeriodDetailOpen}
                  aria-label={
                    /* "{0}: {1}" */
                    message("frontend.report.period.detailAria", [
                      periodTitle,
                      periodText,
                    ])
                  }
                  onClick={handlePeriodDetailToggle}
                  onBlur={handlePeriodDetailClose}
                >
                  <strong className={styles.reportStatsValue}>
                    {periodSummary}
                  </strong>
                  <span
                    className={clsx(
                      styles.periodTooltip,
                      isPeriodDetailOpen && styles.periodTooltipOpen,
                    )}
                    role="tooltip"
                  >
                    {periodText}
                  </span>
                </button>
              </div>
            </div>
          </section>

          {/* 기록 제목부터 하단 배경을 흰색으로 유지하는 독후감 기록 영역 */}
          <div className={styles.recordArea}>
            {/* 독후감 기록과 좋아요 및 댓글 지표 영역 */}
            <section className={styles.recordSection}>
              <div className={styles.recordTitleRow}>
                <h2 className={styles.sectionTitle}>
                  {/* "기록" */}
                  {message("frontend.report.field.content")}
                </h2>
              </div>
              {/* 기록 반응 지표 영역 */}
              <div className={styles.recordMetrics}>
                <button
                  className={styles.likeButton}
                  type="button"
                  aria-label={/* "좋아요" */ message("frontend.report.like.aria")}
                  aria-pressed={bookData.likeYsno === "Y"}
                  disabled={likeMutation.isPending}
                  onClick={handleLikeToggle}
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
                <span
                  className={styles.commentIndicator}
                  role="img"
                  aria-label={/* "댓글" */ message("frontend.report.comment.aria")}
                >
                  <img
                    className={styles.commentIcon}
                    src="/img/icons/icon-comment.svg"
                    alt=""
                  />
                  <span className={styles.commentCount}>
                    {TEMPORARY_COMMENT_COUNT}
                  </span>
                </span>
              </div>
              <p className={styles.contentBox}>
                {bookData.reptCntn || message("frontend.common.noWrittenReport")}
              </p>
            </section>

            {/* 독후감 수정 이동 영역 */}
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
                {/* "수정" */}
                {message("frontend.report.update")}
              </button>
            </div>
          </div>
        </div>
      </Container>
    </main>
  );
}

export default DetailPage;
