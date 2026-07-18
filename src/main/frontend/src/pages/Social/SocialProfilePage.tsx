import { message } from "@/app/messages/message";
import { sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import {
  formatDashedDateToDot,
  getRemainDaysUntil,
  getRemainPeriodRate,
} from "@/app/utils/dateUtil";
import Loading from "@/components/Loading/Loading";
import {
  getSocialProfileApi,
  getSocialReadingSummaryApi,
} from "@/features/Social/api/socialApi";
import type {
  MonthlyReadingSummary,
  ReadingSummaryReport,
  UserProfile,
} from "@/features/User/api/userApi";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as styles from "@/pages/My/ProfileEditPage.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";
type ReadingPeriod = "week" | "month" | "year";

/**
 * 독후감 요약 목록에 표시할 독서 기간을 조합합니다.
 * 시작일과 종료일 중 일부만 존재해도 불필요한 구분자가 표시되지 않도록 빈 값을 제거합니다.
 *
 * @author Hanwon.Jang
 * @param report 독서 기간을 표시할 독후감 요약 정보
 * @return 화면에 표시할 독서 기간 문자열
 */
const getReadingEndDateText = (report: ReadingSummaryReport) => {
  return formatDashedDateToDot(report.reportEndt);
};

/**
 * 숫자 평점을 5개 별 표시 문자열로 변환합니다.
 * 서버 응답이 비어 있거나 숫자로 바꿀 수 없는 경우 0점으로 처리해 화면 표시를 안정적으로 유지합니다.
 *
 * @author Hanwon.Jang
 * @param grade 서버에서 내려온 평점 문자열
 * @return 5개 기준 별점 문자열
 */
const getReadingGradeText = (grade?: string) => {
  const gradeNumber = Math.max(0, Math.min(5, Math.floor(Number(grade) || 0)));
  return `${"\u2605".repeat(gradeNumber)}${"\u2606".repeat(5 - gradeNumber)}`;
};

/**
 * 목표 달성률에 따라 파스텔톤 진행 막대 색상을 반환합니다.
 * 달성률이 높아질수록 차분한 초록 계열로 이동해 목표 달성 상태를 직관적으로 보여줍니다.
 *
 * @author Hanwon.Jang
 * @param rate 목표 달성률
 * @return 진행 막대 색상
 */
const getGoalProgressColor = (rate: number) => {
  if (rate >= 100) {
    return "#95d5b2";
  }

  if (rate >= 70) {
    return "#a8dadc";
  }

  if (rate >= 40) {
    return "#ffd6a5";
  }

  return "#ffb4a2";
};

/**
 * 다른 사용자의 프로필과 독서 활동 현황을 보여주는 읽기 전용 페이지입니다.
 * 공개 독후감 목록에서 작성자 프로필을 눌렀을 때 진입하며, 마이페이지와 같은 활동 요약 데이터를 표시합니다.
 *
 * @author Hanwon.Jang
 * @return 소셜 프로필 페이지 컴포넌트
 */
function SocialProfilePage() {
  const navigate = useNavigate();
  const { userNumb } = useParams();
  const targetUserNumb = Number(userNumb);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [summary, setSummary] = useState<MonthlyReadingSummary | null>(null);
  const [expandedSummary, setExpandedSummary] = useState<Record<ReadingPeriod, boolean>>({
    week: false,
    month: false,
    year: false,
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    // 잘못된 사용자 번호는 API 호출 전에 차단해 불필요한 서버 요청을 만들지 않습니다.
    if (!Number.isFinite(targetUserNumb) || targetUserNumb <= 0) {
      setIsLoading(false);
      return () => {
        ignore = true;
      };
    }

    Promise.all([
      getSocialProfileApi(targetUserNumb),
      getSocialReadingSummaryApi(targetUserNumb),
    ])
      .then(([profileResponse, summaryResponse]) => {
        if (!ignore) {
          setProfile(profileResponse.data as UserProfile);
          setSummary(summaryResponse.data as MonthlyReadingSummary);
        }
      })
      .catch(() => {
        if (!ignore) {
          setProfile(null);
          setSummary(null);
        }
      })
      .finally(() => {
        if (!ignore) {
          setIsLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [targetUserNumb]);

  /**
   * 주간, 월간, 연간 요약 리스트의 펼침 상태를 전환합니다.
   * 읽은 책이 있는 영역에만 호출되어 빈 목록에 대한 불필요한 상태 변경을 막습니다.
   *
   * @author Hanwon.Jang
   * @param period 펼침 상태를 변경할 기간 구분값
   */
  const handleToggleReadingSummary = (period: ReadingPeriod) => {
    setExpandedSummary((prev) => ({
      ...prev,
      [period]: !prev[period],
    }));
  };

  /**
   * 다른 사용자의 요약 독후감 항목을 선택했을 때 공개 여부에 따라 이동 또는 경고를 처리합니다.
   * 공개 독후감은 ISBN 기준 공개 독후감 목록으로 이동하고, 비공개 독후감은 사용자가 내용을 볼 수 없음을 안내합니다.
   *
   * @author Hanwon.Jang
   * @param report 선택한 독후감 요약 정보
   */
  const handleSummaryReportClick = (report: ReadingSummaryReport) => {
    if (report.pubcYsno !== "Y") {
      void sweetWarning(
        message("frontend.social.privateReport.title"),
        message("frontend.social.privateReport.text"),
      );
      return;
    }

    if (!report.bookIsbn) {
      void sweetWarning(
        message("frontend.common.invalidAccess"),
        message("frontend.common.noBookInfo"),
      );
      return;
    }

    navigate(`/book/public-reports/isbn?isbn=${encodeURIComponent(report.bookIsbn)}`, {
      state: {
        title: report.bookTitl,
        author: report.bookAthr,
        cover: report.bookCvim,
      },
    });
  };

  /**
   * 다른 사용자가 현재 읽고 있는 책의 목표 종료일까지 남은 기간 정보를 렌더링합니다.
   * 남은 기간이 적을수록 붉은 계열로 표시해 목표 종료일이 가까움을 보여줍니다.
   *
   * @author Hanwon.Jang
   * @param reports 현재 읽고 있는 독후감 목록
   * @return 현재 읽고 있는 책 섹션 JSX
   */
  const renderCurrentReadingReports = (reports: ReadingSummaryReport[] = []) => {
    if (reports.length === 0) {
      return null;
    }

    return (
      <section
        className={styles.monthlySummary}
        aria-label={message("frontend.profile.currentReading.title")}
      >
        <div className={styles.currentReadingSection}>
          <h2 className={styles.currentReadingTitle}>
            {message("frontend.profile.currentReading.title")}
          </h2>
          <div className={styles.currentReadingList}>
            {reports.map((report) => {
              const remainDays = getRemainDaysUntil(report.reportEndt);
              const remainRate = getRemainPeriodRate(report.reportStdt, report.reportEndt);
              const remainColor = getGoalProgressColor(remainRate);
              const isExpired = remainDays <= 0;

              return (
                <div className={styles.currentReadingCard} key={report.reportNumb}>
                  {report.bookCvim && (
                    <img
                      className={styles.readingSummaryCover}
                      src={report.bookCvim}
                      alt=""
                    />
                  )}
                  <span className={styles.currentReadingText}>
                    <strong className={styles.readingSummaryBookTitle}>
                      {report.bookTitl || message("frontend.common.noBookInfo")}
                    </strong>
                    <span className={styles.currentReadingMeta}>
                      <span className={styles.readingSummaryBookMeta}>
                        {[report.bookAthr, formatDashedDateToDot(report.reportEndt)]
                          .filter(Boolean)
                          .join(" | ")}
                      </span>
                      <span
                        className={styles.currentReadingRemain}
                        style={{ color: remainColor }}
                      >
                        {isExpired
                          ? message("frontend.profile.currentReading.expired")
                          : message("frontend.profile.currentReading.remain", [remainDays])}
                      </span>
                    </span>
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </section>
    );
  };

  /**
   * 기간별 독서 활동 행과 펼침 목록을 공통 구조로 렌더링합니다.
   * 목표 달성률, 목표 권수, 실제 완료 권수, 완료 독후감 목록을 같은 배치로 보여줍니다.
   *
   * @author Hanwon.Jang
   * @param period 주간, 월간, 연간 구분값
   * @param code 달력 아이콘 안에 표시할 코드
   * @param titleKey 제목 메시지 key
   * @param countKey 권수 메시지 key
   * @param count 현재 기간 완료 권수
   * @param reports 펼침 영역에 표시할 완료 독후감 목록
   * @return 독서 활동 요약 JSX
   */
  const renderReadingSummaryRow = (
    period: ReadingPeriod,
    code: string | undefined,
    titleKey: string,
    countKey: string,
    count: number,
    reports: ReadingSummaryReport[] = [],
  ) => {
    const isExpanded = expandedSummary[period];
    const hasReports = reports.length > 0;
    const goalCnt =
      period === "week"
        ? summary?.weekGoalCnt
        : period === "month"
          ? summary?.monthGoalCnt
          : summary?.yearGoalCnt;
    const goalRate =
      period === "week"
        ? summary?.weekGoalRate ?? 0
        : period === "month"
          ? summary?.monthGoalRate ?? 0
          : summary?.yearGoalRate ?? 0;
    const goalSet =
      period === "week"
        ? Boolean(summary?.weekGoalSet)
        : period === "month"
          ? Boolean(summary?.monthGoalSet)
          : Boolean(summary?.yearGoalSet);
    const goalProgressColor = getGoalProgressColor(goalRate);

    return (
      <div>
        <div className={styles.readingSummaryRow}>
          <button
            className={hasReports ? styles.readingSummaryToggle : styles.readingSummaryToggleStatic}
            type="button"
            aria-expanded={hasReports ? isExpanded : undefined}
            disabled={!hasReports}
            onClick={() => {
              if (hasReports) {
                handleToggleReadingSummary(period);
              }
            }}
          >
            <div className={styles.monthlyCalendarIcon} aria-hidden="true">
              <span className={styles.monthlyCalendarRing} />
              <span className={styles.monthlyCalendarMonth}>{code ?? ""}</span>
            </div>
            <div className={styles.monthlySummaryText}>
              <span className={styles.monthlySummaryLabel}>{message(titleKey)}</span>
              <strong className={styles.monthlySummaryCount}>
                {message(countKey, [count])}
              </strong>
            </div>
            {hasReports && (
              <span
                className={
                  isExpanded
                    ? styles.readingSummaryChevronOpen
                    : styles.readingSummaryChevron
                }
                aria-hidden="true"
              >
                <svg
                  className={styles.readingSummaryChevronIcon}
                  viewBox="0 0 24 24"
                  focusable="false"
                >
                  <path d="M7.4 9.6 12 14.2l4.6-4.6 1.4 1.4-6 6-6-6 1.4-1.4Z" />
                </svg>
              </span>
            )}
          </button>
        </div>
        <div className={styles.goalProgressRow}>
          <span className={styles.goalProgressTarget}>
            {goalSet ? message("frontend.profile.goal.target", [goalCnt ?? 0]) : ""}
          </span>
          <div className={styles.goalProgressTrack}>
            <span
              className={styles.goalProgressFill}
              style={{
                width: `${Math.min(100, goalRate)}%`,
                backgroundColor: goalProgressColor,
              }}
            />
          </div>
          <span
            className={styles.goalProgressRate}
            style={goalSet ? { color: goalProgressColor } : undefined}
          >
            {goalSet
              ? message("frontend.profile.goal.rate", [goalRate])
              : message("frontend.profile.goal.unset")}
          </span>
        </div>
        {hasReports && (
          <div
            className={
              isExpanded
                ? styles.readingSummaryPanelOpen
                : styles.readingSummaryPanel
            }
          >
            <div className={styles.readingSummaryPanelInner}>
              {reports.map((report) => (
                <button
                  className={
                    report.pubcYsno === "Y"
                      ? styles.readingSummaryReport
                      : styles.readingSummaryReportPrivate
                  }
                  type="button"
                  key={report.reportNumb}
                  onClick={() => handleSummaryReportClick(report)}
                >
                  {report.bookCvim && (
                    <img
                      className={styles.readingSummaryCover}
                      src={report.bookCvim}
                      alt=""
                    />
                  )}
                  <span className={styles.readingSummaryBookText}>
                    <strong className={styles.readingSummaryBookTitle}>
                      {report.bookTitl || message("frontend.common.noBookInfo")}
                    </strong>
                    <span className={styles.readingSummaryBookMeta}>
                      <span className={styles.readingSummaryMetaLine}>
                        {report.bookAthr && (
                          <span className={styles.readingSummaryMetaText}>
                            {report.bookAthr}
                          </span>
                        )}
                        {report.bookAthr && getReadingEndDateText(report) && (
                          <span>|</span>
                        )}
                        {getReadingEndDateText(report) && (
                          <span className={styles.readingSummaryMetaText}>
                            {getReadingEndDateText(report)}
                          </span>
                        )}
                        {(report.bookAthr || getReadingEndDateText(report)) && (
                          <span>|</span>
                        )}
                        <span className={styles.readingSummaryGrade}>
                          {getReadingGradeText(report.reportGrde)}
                        </span>
                      </span>
                    </span>
                  </span>
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  if (isLoading) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  if (!profile || !summary) {
    return <main className={styles.page}>{message("frontend.common.invalidAccess")}</main>;
  }

  return (
    <main className={styles.page}>
      <section className={styles.profileShell}>
        <div
          className={styles.cover}
          style={
            profile.bgimPath
              ? { backgroundImage: `url("${profile.bgimPath}")` }
              : undefined
          }
        >
          {!profile.bgimPath && (
            <p className={styles.coverEmptyText}>
              {message("frontend.profile.background.empty")}
            </p>
          )}
        </div>

        <section className={styles.profileBody}>
          <div className={styles.profileHeaderRow}>
            <div className={styles.avatarWrap}>
              <img
                className={styles.profileImage}
                src={profile.porfPath || DEFAULT_PROFILE_IMAGE}
                alt={profile.userNick ?? message("frontend.profile.nick")}
              />
            </div>

            <div className={styles.profileText}>
              <h1 className={styles.profileName}>{profile.userNick || "-"}</h1>
              <p className={styles.profileIntro}>
                {profile.intrCntn || message("frontend.profile.intro.empty")}
              </p>
            </div>
          </div>

          {renderCurrentReadingReports(summary.currentReadingReports)}
          <section className={styles.monthlySummary} aria-label={message("frontend.profile.monthlyReading.title")}>
            <div className={styles.goalAchievementSummary}>
              <p className={styles.goalAchievementTitle}>
                {message("frontend.profile.goal.achievementTitle")}
              </p>
              <div className={styles.goalAchievementGrid}>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {message("frontend.profile.goal.weekLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {message("frontend.profile.goal.achievementCount", [summary.weekGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {message("frontend.profile.goal.monthLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {message("frontend.profile.goal.achievementCount", [summary.monthGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {message("frontend.profile.goal.yearLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {message("frontend.profile.goal.achievementCount", [summary.yearGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {message("frontend.profile.goal.totalLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {message("frontend.profile.goal.achievementCount", [summary.totalGoalAchvCnt])}
                  </strong>
                </div>
              </div>
            </div>
            <div className={styles.readingSummaryDivider} />
            {renderReadingSummaryRow(
              "week",
              summary.weekCode,
              "frontend.profile.weeklyReading.title",
              "frontend.profile.weeklyReading.count",
              summary.currentWeekCount,
              summary.currentWeekReports,
            )}
            <div className={styles.readingSummaryDivider} />
            {renderReadingSummaryRow(
              "month",
              summary.monthCode,
              "frontend.profile.monthlyReading.title",
              "frontend.profile.monthlyReading.count",
              summary.currentMonthCount,
              summary.currentMonthReports,
            )}
            <div className={styles.readingSummaryDivider} />
            {renderReadingSummaryRow(
              "year",
              summary.yearCode,
              "frontend.profile.yearlyReading.title",
              "frontend.profile.yearlyReading.count",
              summary.currentYearCount,
              summary.currentYearReports,
            )}
          </section>
        </section>
      </section>
    </main>
  );
}

export default SocialProfilePage;
