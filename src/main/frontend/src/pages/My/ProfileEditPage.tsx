import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError, sweetSuccess, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import {
  formatDateValue,
  formatDashedDateToDot,
  getRemainDaysUntil,
  getRemainPeriodRate,
} from "@/app/utils/dateUtil";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import Loading from "@/components/Loading/Loading";
import { uptReptStatusGradeApi } from "@/features/Book/api/bookApi";
import RatingField from "@/features/Book/Set/components/form/ratingField/RatingField";
import {
  REPORT_GRADE_VALUES,
  REPORT_STATUS_DONE,
  REPORT_STATUS_STOP,
} from "@/features/Book/constants/reportForm";
import {
  copyPreviousReadingGoalApi,
  getMyProfileApi,
  getMonthlyReadingSummaryApi,
  updateReadingGoalApi,
  updateMyProfileApi,
  type MonthlyReadingSummary,
  type ReadingSummaryReport,
  type UserProfile,
} from "@/features/User/api/userApi";
import { notifyUserProfileUpdated } from "@/features/User/lib/profileEvents";
import type { FormEvent, MouseEvent } from "react";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import * as styles from "./ProfileEditPage.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";
const USER_NICK_MAX_LENGTH = 10;
const PROFILE_INTRO_MAX_LENGTH = 50;
const USER_NICK_REGEX = /^[A-Za-z0-9\uAC00-\uD7A3]+$/;
const USER_NICK_INPUT_REGEX = /[^A-Za-z0-9\uAC00-\uD7A3\u3131-\u318E\u1100-\u11FF\uA960-\uA97F\uD7B0-\uD7FF]/g;
type ReadingPeriod = "week" | "month" | "year";
type QuickReadingStatus = typeof REPORT_STATUS_DONE | typeof REPORT_STATUS_STOP;
type ProfileModalType = "quick" | "goal" | "goalHelp";

const GOAL_PERIODS: ReadingPeriod[] = ["week", "month", "year"];
const MODAL_CLOSE_DELAY_MS = 180;

const GOAL_COPY_LABELS: Record<ReadingPeriod, { current: string; previous: string; singular: string }> = {
  week: {
    current: "이번 주",
    previous: "지난 주",
    singular: "이번주의",
  },
  month: {
    current: "이번 달",
    previous: "지난 달",
    singular: "이번 달의",
  },
  year: {
    current: "올해",
    previous: "작년",
    singular: "올해의",
  },
};

/**
 * 닉네임 입력값에서 한글/영문/숫자가 아닌 문자를 제거하고 최대 입력 길이를 제한합니다.
 * 사용자가 특수문자를 붙여 넣어도 저장 가능한 닉네임 형식만 상태에 반영합니다.
 *
 * @author Hanwon.Jang
 * @param value 사용자가 입력한 닉네임 원문
 * @return 한글/영문/숫자 10자 이하로 정리한 닉네임
 */
const normalizeUserNick = (value: string) =>
  value.replace(USER_NICK_INPUT_REGEX, "").slice(0, USER_NICK_MAX_LENGTH);

/**
 * 한줄 소개 입력값을 허용 길이 이하로 제한합니다.
 * textarea의 maxLength와 별개로 상태 값도 제한해 브라우저별 입력 차이를 한 번 더 방어합니다.
 *
 * @author Hanwon.Jang
 * @param value 사용자가 입력한 한줄 소개 원문
 * @return 50자 이하로 제한한 한줄 소개
 */
const normalizeProfileIntro = (value: string) =>
  value.slice(0, PROFILE_INTRO_MAX_LENGTH);

const joinKoreanList = (items: string[]) => items.join(", ");

const getCopyablePreviousGoalPeriods = (summary: MonthlyReadingSummary | null) =>
  GOAL_PERIODS.filter((period) => {
    if (!summary) {
      return false;
    }

    if (period === "week") {
      return !summary.weekGoalSet && Boolean(summary.previousWeekGoalCnt);
    }

    if (period === "month") {
      return !summary.monthGoalSet && Boolean(summary.previousMonthGoalCnt);
    }

    return !summary.yearGoalSet && Boolean(summary.previousYearGoalCnt);
  });

const getPreviousGoalCount = (summary: MonthlyReadingSummary, period: ReadingPeriod) => {
  if (period === "week") {
    return summary.previousWeekGoalCnt ?? 0;
  }

  if (period === "month") {
    return summary.previousMonthGoalCnt ?? 0;
  }

  return summary.previousYearGoalCnt ?? 0;
};

const getCopyPreviousGoalConfirmText = (summary: MonthlyReadingSummary, periods: ReadingPeriod[]) => {
  if (periods.length === 1) {
    const period = periods[0];
    const count = getPreviousGoalCount(summary, period);
    const labels = GOAL_COPY_LABELS[period];
    return `${labels.singular} 독서 목표설정이 비어있습니다. ${labels.previous} 목표 권수(${count}권)를 가져오시겠습니까?`;
  }

  const currentLabels = periods.map((period) => GOAL_COPY_LABELS[period].current);
  const previousLabels = periods.map((period) => {
    const labels = GOAL_COPY_LABELS[period];
    return `${labels.previous}(${getPreviousGoalCount(summary, period)}권)`;
  });

  return `${joinKoreanList(currentLabels)} 목표가 비어있습니다. ${joinKoreanList(previousLabels)} 목표를 가져오시겠습니까?`;
};

/**
 * 이전 기간 대비 완료 독서 변화량을 화면 표시용 문자열로 변환합니다.
 * 양수에는 + 기호를 붙이고 0은 증감이 없는 상태로 그대로 표시합니다.
 *
 * @author Hanwon.Jang
 * @param diff 이전 기간 대비 완료 독서 권수 변화량
 * @return 변화량 표시 문자열
 */
const formatReadingDiff = (diff: number) => {
  if (diff > 0) {
    return `+${diff}`;
  }

  return String(diff);
};

/**
 * 독후감 요약 목록에 표시할 독서 기간을 시작일과 종료일 기준으로 조합합니다.
 * 시작일 또는 종료일 중 하나만 내려오는 예외 상황에서도 비어 있는 구분자가 보이지 않도록 처리합니다.
 *
 * @author Hanwon.Jang
 * @param report 독서 기간을 표시할 독후감 요약 정보
 * @return 화면에 표시할 독서 기간 문자열
 */
const getReadingEndDateText = (report: ReadingSummaryReport) => {
  return formatDashedDateToDot(report.reptEndt);
};

/**
 * 현재 읽고 있는 책의 목표 독서기간을 팝업 표시용 문장으로 변환합니다.
 * 시작일과 종료일이 모두 비어 있으면 책 정보 영역에 불필요한 빈 라벨이 나오지 않도록 빈 문자열을 반환합니다.
 *
 * @author Hanwon.Jang
 * @param report 목표 독서기간을 표시할 독후감 요약 정보
 * @return 목표 독서기간 표시 문구
 */
const getTargetReadingPeriodText = (report: ReadingSummaryReport) => {
  const periodText = [
    formatDashedDateToDot(report.reptStdt),
    formatDashedDateToDot(report.reptEndt),
  ]
    .filter(Boolean)
    .join(" ~ ");

  return periodText
    ? message("frontend.profile.currentReading.targetPeriod", [periodText])
    : "";
};

/**
 * 독후감 평점을 5개의 별 문자열로 변환합니다.
 * 완료 독후감 목록의 평점은 숫자 형태로 내려오므로 0점부터 5점 사이로 보정해 화면 표시가 깨지지 않게 합니다.
 *
 * @author Hanwon.Jang
 * @param grade 서버에서 내려온 평점 문자열
 * @return 5개 기준의 별점 표시 문자열
 */
const getReadingGradeText = (grade?: string) => {
  const gradeNumber = Math.max(0, Math.min(5, Math.floor(Number(grade) || 0)));
  return `${"\u2605".repeat(gradeNumber)}${"\u2606".repeat(5 - gradeNumber)}`;
};

/**
 * 로그인 사용자의 프로필 사진, 배경 사진, 닉네임, 한줄 소개를 조회하고 수정합니다.
 * 수정 모드에서는 화면을 전환하지 않고 기존 요소 위치에서 텍스트와 이미지만 편집할 수 있게 제공합니다.
 *
 * @author Hanwon.Jang
 * @return 프로필 상세 및 수정 페이지 컴포넌트
 */
function ProfileEditPage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isEditMode, setIsEditMode] = useState(false);
  const [userNick, setUserNick] = useState("");
  const [intrCntn, setIntrCntn] = useState("");
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [backgroundImage, setBackgroundImage] = useState<File | null>(null);
  const [previewImage, setPreviewImage] = useState(DEFAULT_PROFILE_IMAGE);
  const [previewBackground, setPreviewBackground] = useState("");
  const [monthlySummary, setMonthlySummary] = useState<MonthlyReadingSummary | null>(null);
  const [quickReport, setQuickReport] = useState<ReadingSummaryReport | null>(null);
  const [quickStatus, setQuickStatus] = useState<QuickReadingStatus>(REPORT_STATUS_DONE);
  const [quickGrade, setQuickGrade] = useState(5);
  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);
  const [isGoalHelpModalOpen, setIsGoalHelpModalOpen] = useState(false);
  const [closingModal, setClosingModal] = useState<ProfileModalType | null>(null);
  const [weekGoalCnt, setWeekGoalCnt] = useState("");
  const [monthGoalCnt, setMonthGoalCnt] = useState("");
  const [yearGoalCnt, setYearGoalCnt] = useState("");
  const [expandedSummary, setExpandedSummary] = useState<Record<ReadingPeriod, boolean>>({
    week: false,
    month: false,
    year: false,
  });
  const [activeDiffTooltip, setActiveDiffTooltip] = useState<ReadingPeriod | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isGoalSaving, setIsGoalSaving] = useState(false);
  const [isQuickSaving, setIsQuickSaving] = useState(false);
  const diffTooltipRefs = useRef<Record<ReadingPeriod, HTMLDivElement | null>>({
    week: null,
    month: null,
    year: null,
  });
  useBodyScrollLock(Boolean(quickReport) || isGoalModalOpen || isGoalHelpModalOpen);

  /**
   * 서버에서 받은 프로필 값을 화면 상태와 이미지 미리보기 상태에 함께 반영합니다.
   * 저장 완료 후 파일 선택 상태를 비워 같은 파일을 다시 선택하더라도 정상적으로 반응하게 만듭니다.
   *
   * @author Hanwon.Jang
   * @param nextProfile 서버에서 조회하거나 저장 후 반환한 사용자 프로필 정보
   */
  const syncProfileState = (nextProfile: UserProfile) => {
    setProfile(nextProfile);
    setUserNick(nextProfile?.userNick ?? "");
    setIntrCntn(nextProfile?.intrCntn ?? "");
    setPreviewImage(nextProfile?.porfPath || DEFAULT_PROFILE_IMAGE);
    setPreviewBackground(nextProfile?.bgimPath || "");
    setProfileImage(null);
    setBackgroundImage(null);
  };

  useEffect(() => {
    let ignore = false;

    getMyProfileApi()
      .then((response) => {
        if (!ignore) {
          syncProfileState(response.data as UserProfile);
        }
      })
      .finally(() => {
        if (!ignore) {
          setIsLoading(false);
        }
      });

    getMonthlyReadingSummaryApi().then((response) => {
      if (!ignore) {
        setMonthlySummary(response.data as MonthlyReadingSummary);
      }
    });

    return () => {
      ignore = true;
    };
  }, []);

  /**
   * 이전 기간 대비 완료 독서량 말풍선이 열린 상태에서 다른 영역을 누르면 말풍선을 닫습니다.
   * 비교 숫자와 말풍선 자체를 누르는 경우에는 같은 요소 안에서 발생한 클릭으로 판단해 닫지 않습니다.
   *
   * @author Hanwon.Jang
   * @return
   */
  useEffect(() => {
    const handleDocumentPointerDown = (event: PointerEvent) => {
      if (!activeDiffTooltip) {
        return;
      }

      const tooltipArea = diffTooltipRefs.current[activeDiffTooltip];
      const target = event.target;

      if (tooltipArea && target instanceof Node && tooltipArea.contains(target)) {
        return;
      }

      setActiveDiffTooltip(null);
    };

    document.addEventListener("pointerdown", handleDocumentPointerDown);

    return () => {
      document.removeEventListener("pointerdown", handleDocumentPointerDown);
    };
  }, [activeDiffTooltip]);

  /**
   * 이전 기간 대비 완료 독서 변화량 상세 문구를 info 알림으로 보여줍니다.
   * 월간과 연간 비교 모두 같은 UI 패턴을 사용하므로 비교 단위별 메시지 key만 분기합니다.
   *
   * @author Hanwon.Jang
   * @param diff 이전 기간 대비 완료 독서 권수 변화량
   * @param period 비교 단위
   */
  const getReadingDiffMessage = (diff: number, period: ReadingPeriod) => {
    const diffCount = Math.abs(diff);
    const periodMessagePrefix =
      period === "week" ? "weeklyReading" : period === "month" ? "monthlyReading" : "yearlyReading";
    const messageKey =
      diff === 0
        ? `frontend.profile.${periodMessagePrefix}.diffSame`
        : `frontend.profile.${periodMessagePrefix}.${diff > 0 ? "diffMore" : "diffLess"}`;

    return message(messageKey, [diffCount]);
  };

  const handleReadingDiffClick = (diff: number, period: ReadingPeriod) => {
    setActiveDiffTooltip((prev) => (prev === period ? null : period));
  };

  /**
   * 사용자가 선택한 이미지 파일을 프로필 또는 배경 대상에 맞춰 미리보기로 반영합니다.
   * 이미지가 아닌 파일은 서버 저장 대상에서 제외하고 경고 알림만 표시합니다.
   *
   * @author Hanwon.Jang
   * @param file 사용자가 선택한 이미지 파일
   * @param target 이미지가 적용될 영역 구분값
   */
  const applyImagePreview = (file: File | undefined, target: "profile" | "background") => {
    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.imageOnly"),
      );
      return;
    }

    const previewUrl = URL.createObjectURL(file);

    if (target === "profile") {
      setProfileImage(file);
      setPreviewImage(previewUrl);
      return;
    }

    setBackgroundImage(file);
    setPreviewBackground(previewUrl);
  };

  /**
   * 독서 요약 행의 펼침 상태를 월간/연간 단위로 전환합니다.
   * 같은 섹션 안에서 두 목록을 독립적으로 열 수 있어 사용자가 비교 중인 목록을 잃지 않게 합니다.
   *
   * @author Hanwon.Jang
   * @param period 열거나 닫을 독서 요약 기간 구분값
   */
  const handleToggleReadingSummary = (period: ReadingPeriod) => {
    setExpandedSummary((prev) => ({
      ...prev,
      [period]: !prev[period],
    }));
  };

  /**
   * 요약 목록에서 선택한 책의 독후감 상세 화면으로 이동합니다.
   * 백엔드가 내려준 reptNumb를 그대로 사용해 책 정보가 아닌 사용자의 독후감 상세로 연결합니다.
   *
   * @author Hanwon.Jang
   * @param reptNumb 이동할 독후감 번호
   */
  const handleSummaryReportClick = (reptNumb: number) => {
    navigate(`/book/detail/${reptNumb}`);
  };

  /**
   * 커스텀 모달을 닫을 때 fade-out 애니메이션이 끝난 뒤 실제 상태를 제거합니다.
   * sweetAlert, 달력, selectBox 성격의 모달은 이 흐름을 사용하지 않고 각 컴포넌트의 기본 동작을 유지합니다.
   *
   * @author Hanwon.Jang
   * @param modal 닫을 마이페이지 커스텀 모달 구분값
   * @return fade-out 완료 Promise
   */
  const closeProfileModal = (modal: ProfileModalType) => {
    setClosingModal(modal);

    return new Promise<void>((resolve) => {
      window.setTimeout(() => {
        if (modal === "quick") {
          setQuickReport(null);
        }

        if (modal === "goal") {
          setIsGoalModalOpen(false);
          setIsGoalHelpModalOpen(false);
        }

        if (modal === "goalHelp") {
          setIsGoalHelpModalOpen(false);
        }

        setClosingModal((current) => (current === modal ? null : current));
        resolve();
      }, MODAL_CLOSE_DELAY_MS);
    });
  };

  /**
   * 현재 읽고 있는 책을 눌렀을 때 빠른 상태/별점 수정 모달을 엽니다.
   * 아직 별점이 없는 독후감은 사용자가 바로 완료 처리할 수 있도록 기본값을 5점으로 보여줍니다.
   *
   * @author Hanwon.Jang
   * @param report 선택한 현재 읽고 있는 책 정보
   */
  const handleCurrentReadingClick = (report: ReadingSummaryReport) => {
    const reportGrade = Number(report.reptGrde);

    setClosingModal(null);
    setQuickReport(report);
    setQuickStatus(REPORT_STATUS_DONE);
    setQuickGrade(Number.isFinite(reportGrade) ? reportGrade : 5);
  };

  const handleQuickEditClick = () => {
    if (!quickReport) {
      return;
    }

    navigate(`/book/upt/${quickReport.reptNumb}`);
  };

  const handleQuickSaveClick = async () => {
    if (!quickReport) {
      return;
    }

    if (
      quickStatus === REPORT_STATUS_DONE
      && !(REPORT_GRADE_VALUES as readonly number[]).includes(quickGrade)
    ) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.report.field.grade"),
      );
      return;
    }

    try {
      setIsQuickSaving(true);
      await uptReptStatusGradeApi({
        reptNumb: quickReport.reptNumb,
        data: {
          reptStat: quickStatus,
          reptGrde: quickStatus === REPORT_STATUS_DONE ? String(quickGrade) : "0",
          reptEndt: quickStatus === REPORT_STATUS_DONE || quickStatus === REPORT_STATUS_STOP
            ? formatDateValue(new Date())
            : quickReport.reptEndt,
        },
      });

      const response = await getMonthlyReadingSummaryApi();
      setMonthlySummary(response.data as MonthlyReadingSummary);
      await closeProfileModal("quick");
      await sweetSuccess(
        message("frontend.alert.saveSuccessTitle"),
        message("frontend.report.saved"),
      );
    } catch (error) {
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsQuickSaving(false);
    }
  };

  /**
   * 목표 설정 모달을 열 때 현재 저장된 목표값을 입력값에 반영합니다.
   * 아직 목표가 없으면 빈 값으로 시작해 사용자가 직접 입력하도록 유도합니다.
   *
   * @author Hanwon.Jang
   * @return
   */
  const handleGoalModalOpen = async () => {
    let nextSummary = monthlySummary;
    const copyablePreviousGoalPeriods = getCopyablePreviousGoalPeriods(nextSummary);

    if (nextSummary && copyablePreviousGoalPeriods.length > 0) {
      const confirmResult = await sweetConfirm({
        title: message("frontend.profile.goal.copyPreviousTitle"),
        text: getCopyPreviousGoalConfirmText(nextSummary, copyablePreviousGoalPeriods),
        confirmButtonText: message("frontend.profile.goal.copyPreviousConfirm"),
        cancelButtonText: message("frontend.profile.goal.copyPreviousCancel"),
      });

      if (confirmResult.isConfirmed) {
        try {
          setIsGoalSaving(true);
          const response = await copyPreviousReadingGoalApi();
          nextSummary = response.data as MonthlyReadingSummary;
          setMonthlySummary(nextSummary);
          await sweetSuccess(
            message("frontend.profile.goal.savedTitle"),
            message("frontend.profile.goal.saved"),
          );
          return;
        } catch (error) {
          void sweetError(
            message("frontend.alert.updateFailedTitle"),
            getApiErrorMessage(error, message("frontend.common.tryAgain")),
          );
          return;
        } finally {
          setIsGoalSaving(false);
        }
      }
    }

    setWeekGoalCnt(nextSummary?.weekGoalCnt ? String(nextSummary.weekGoalCnt) : "");
    setMonthGoalCnt(nextSummary?.monthGoalCnt ? String(nextSummary.monthGoalCnt) : "");
    setYearGoalCnt(nextSummary?.yearGoalCnt ? String(nextSummary.yearGoalCnt) : "");
    setClosingModal(null);
    setIsGoalModalOpen(true);
  };

  /**
   * 목표 입력값을 1 이상 숫자만 남긴 문자열로 정리합니다.
   *
   * @author Hanwon.Jang
   * @param value 사용자가 입력한 목표 권수
   * @return 숫자로만 구성된 목표 권수 문자열
   */
  const normalizeGoalCount = (value: string) =>
    value.replace(/[^0-9]/g, "").replace(/^0+/, "");

  /**
   * 목표 달성률에 따라 진행 막대와 달성률 텍스트에 사용할 파스텔 색상을 반환합니다.
   * 낮은 달성률은 부드러운 붉은색으로 경고성을 주고, 목표에 가까워질수록 노랑/파랑/초록 계열로
   * 변하게 하여 사용자가 현재 진행 상태를 숫자를 읽기 전에도 빠르게 구분할 수 있게 합니다.
   *
   * @author Hanwon.Jang
   * @param rate 현재 목표 달성률
   * @return 달성률 구간에 대응하는 파스텔 색상 코드
   */
  const getGoalProgressColor = (rate: number) => {
    if (rate >= 100) {
      return "#9edfc2";
    }

    if (rate >= 70) {
      return "#9ed8f2";
    }

    if (rate >= 40) {
      return "#f7d98b";
    }

    return "#f4a7ad";
  };

  /**
   * 현재 읽고 있는 책의 목표 종료일까지 남은 기간 정보를 렌더링합니다.
   * 전체 목표기간 대비 남은 비율을 색상 기준으로 사용해 기간이 가까워질수록 붉은 계열로 표시합니다.
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
              const remainDays = getRemainDaysUntil(report.reptEndt);
              const remainRate = getRemainPeriodRate(report.reptStdt, report.reptEndt);
              const remainColor = getGoalProgressColor(remainRate);
              const isExpired = remainDays <= 0;
              const content = (
                <>
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
                        {[report.bookAthr, formatDashedDateToDot(report.reptEndt)]
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
                </>
              );

              return (
                <button
                  className={styles.currentReadingButton}
                  key={report.reptNumb}
                  type="button"
                  onClick={() => handleCurrentReadingClick(report)}
                >
                  {content}
                </button>
              );
            })}
          </div>
        </div>
      </section>
    );
  };

  /**
   * 목표 입력 모달에서 버튼 클릭으로 월별/연도별 목표 권수를 1권 단위로 증감합니다.
   * 목표 권수는 저장 가능한 최소 단위가 1권이므로 감소 버튼을 반복해서 눌러도 1 미만으로 내려가지 않게 제한합니다.
   *
   * @author Hanwon.Jang
   * @param period 조정할 목표 기간
   * @param amount 증감할 권수
   * @return
   */
  const handleGoalCountStep = (period: ReadingPeriod, amount: number) => {
    const setGoalCnt =
      period === "week"
        ? setWeekGoalCnt
        : period === "month"
          ? setMonthGoalCnt
          : setYearGoalCnt;

    setGoalCnt((prev) => {
      const currentCount = Number(prev) || 1;
      return String(Math.max(1, currentCount + amount));
    });
  };

  /**
   * 월간/연간 목표 권수를 저장하고 저장 후 갱신된 요약 정보를 화면에 반영합니다.
   *
   * @author Hanwon.Jang
   * @return
   */
  /**
   * 목표 기간에 맞는 화면 라벨 메시지 key를 반환합니다.
   * 같은 기간 분기값을 입력 카드, 제한 안내, 저장 전 검증에서 함께 사용해 화면 안내와 검증 기준이 어긋나지 않게 합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 기간 라벨 메시지 key
   */
  const getGoalPeriodLabelKey = (period: ReadingPeriod) => {
    if (period === "week") {
      return "frontend.profile.goal.weekLabel";
    }

    if (period === "month") {
      return "frontend.profile.goal.monthLabel";
    }

    return "frontend.profile.goal.yearLabel";
  };

  /**
   * 현재 모달 입력값 중 기간에 맞는 목표 권수를 숫자로 반환합니다.
   * 빈 문자열은 Number 변환 시 0이 되므로 필수 입력 검증과 같은 기준으로 처리됩니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 사용자가 입력한 목표 권수
   */
  const getGoalInputCount = (period: ReadingPeriod) => {
    if (period === "week") {
      return Number(weekGoalCnt);
    }

    if (period === "month") {
      return Number(monthGoalCnt);
    }

    return Number(yearGoalCnt);
  };

  /**
   * 서버에 저장되어 있던 기간별 목표 권수를 반환합니다.
   * 저장된 값과 입력값이 다른 기간만 수정 제한 검증을 적용해야 같은 값을 다시 저장할 때 수정 횟수를 소모하지 않습니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 서버에 저장된 목표 권수
   */
  const getSavedGoalCount = (period: ReadingPeriod) => {
    if (period === "week") {
      return monthlySummary?.weekGoalCnt ?? null;
    }

    if (period === "month") {
      return monthlySummary?.monthGoalCnt ?? null;
    }

    return monthlySummary?.yearGoalCnt ?? null;
  };

  /**
   * 기간별 목표가 이미 설정되어 있는지 확인합니다.
   * 최초 설정은 수정 제한 대상이 아니므로 기존 목표가 있는 기간만 수정 제한 안내와 저장 전 차단에 사용합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 기존 목표가 있으면 true
   */
  const isGoalAlreadySet = (period: ReadingPeriod) => {
    if (period === "week") {
      return Boolean(monthlySummary?.weekGoalSet);
    }

    if (period === "month") {
      return Boolean(monthlySummary?.monthGoalSet);
    }

    return Boolean(monthlySummary?.yearGoalSet);
  };

  /**
   * 기간별로 앞으로 남은 목표 수정 횟수를 반환합니다.
   * 값은 백엔드 제한 로직과 같은 기준으로 내려온 응답값을 사용해 화면 선검증과 서버 검증의 기준을 맞춥니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 남은 수정 가능 횟수
   */
  const getGoalRemainUpdateCount = (period: ReadingPeriod) => {
    if (period === "week") {
      return monthlySummary?.weekGoalRemainUpdateCnt ?? 0;
    }

    if (period === "month") {
      return monthlySummary?.monthGoalRemainUpdateCnt ?? 0;
    }

    return monthlySummary?.yearGoalRemainUpdateCnt ?? 0;
  };

  /**
   * 목표 수정 제한 기간이 시작되기 전까지 남은 일수를 반환합니다.
   * 0이면 이미 수정 가능 기간이 끝난 상태로 보고 저장 전에 사용자에게 안내합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 수정 가능 기간 남은 일수
   */
  const getGoalEditableRemainDays = (period: ReadingPeriod) => {
    if (period === "week") {
      return monthlySummary?.weekGoalEditableRemainDays ?? 0;
    }

    if (period === "month") {
      return monthlySummary?.monthGoalEditableRemainDays ?? 0;
    }

    return monthlySummary?.yearGoalEditableRemainDays ?? 0;
  };

  /**
   * 목표 기간이 마감 규칙 때문에 잠겨 있는지 반환합니다.
   * 수정 횟수가 남아 있어도 기간이 잠긴 경우에는 프론트에서 먼저 저장을 차단합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 기간 제한으로 수정할 수 없으면 true
   */
  const isGoalUpdateLocked = (period: ReadingPeriod) => {
    if (period === "week") {
      return Boolean(monthlySummary?.weekGoalUpdateLocked);
    }

    if (period === "month") {
      return Boolean(monthlySummary?.monthGoalUpdateLocked);
    }

    return Boolean(monthlySummary?.yearGoalUpdateLocked);
  };

  /**
   * 모달 입력값이 기존 목표보다 낮아졌는지 확인합니다.
   * 목표를 올리는 것은 언제든 허용되어야 하므로 낮아진 기간만 목표 내리기 제한 검증과 확인 alert 대상이 됩니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 기존 목표보다 입력 목표가 낮으면 true
   */
  const isGoalDecreased = (period: ReadingPeriod) => {
    const savedGoalCount = getSavedGoalCount(period);
    return (
      isGoalAlreadySet(period) &&
      savedGoalCount !== null &&
      getGoalInputCount(period) < savedGoalCount
    );
  };

  /**
   * 저장 전 목표 내리기 제한에 걸리는 기간의 안내 문구를 반환합니다.
   * 제한이 없는 기간은 빈 문자열을 반환해 저장 검증 루프에서 다음 기간을 계속 확인할 수 있게 합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 제한 안내 메시지
   */
  const getGoalEditBlockMessage = (period: ReadingPeriod) => {
    if (!isGoalDecreased(period)) {
      return "";
    }

    const label = message(getGoalPeriodLabelKey(period));

    if (getGoalRemainUpdateCount(period) <= 0) {
      return message("frontend.profile.goal.downCountBlocked", [label]);
    }

    if (isGoalUpdateLocked(period)) {
      return message("frontend.profile.goal.downPeriodBlocked", [label]);
    }

    return "";
  };

  /**
   * 목표 입력 카드 하단에 목표 내리기 가능 횟수와 가능 기간 안내를 표시합니다.
   * 목표 올리기는 항상 가능하므로 내리기 제한 정보를 짧은 보조 정보로 분리해 표시합니다.
   *
   * @author Hanwon.Jang
   * @param period 목표 기간 구분값
   * @return 목표 수정 제한 안내 JSX
   */
  const renderGoalLimitInfo = (period: ReadingPeriod) => {
    const remainUpdateCount = getGoalRemainUpdateCount(period);
    const remainDays = getGoalEditableRemainDays(period);
    const isLocked = isGoalUpdateLocked(period);
    const isUnset = !isGoalAlreadySet(period);
    const isDownClosed = !isUnset && (remainUpdateCount <= 0 || isLocked);

    return (
      <div className={styles.goalLimitInfo}>
        {isDownClosed ? (
          <span className={styles.goalLimitDanger}>
            {message("frontend.profile.goal.downLocked")}
          </span>
        ) : (
          <>
            <span className={styles.goalLimitPill}>
              {isUnset
                ? message("frontend.profile.goal.firstSet")
                : message("frontend.profile.goal.remainDown", [remainUpdateCount])}
            </span>
            <span className={styles.goalLimitMuted}>
              {message("frontend.profile.goal.downRemainDays", [remainDays])}
            </span>
          </>
        )}
      </div>
    );
  };

  const handleGoalSubmit = async () => {
    const nextWeekGoalCnt = Number(weekGoalCnt);
    const nextMonthGoalCnt = Number(monthGoalCnt);
    const nextYearGoalCnt = Number(yearGoalCnt);

    if (nextWeekGoalCnt <= 0 || nextMonthGoalCnt <= 0 || nextYearGoalCnt <= 0) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.goal.required"),
      );
      return;
    }

    const blockMessage = GOAL_PERIODS.map(getGoalEditBlockMessage).find(Boolean);

    if (blockMessage) {
      void sweetWarning(message("frontend.profile.goal.downBlockedTitle"), blockMessage);
      return;
    }

    const downGoalLabels = GOAL_PERIODS.filter(isGoalDecreased).map((period) =>
      message(getGoalPeriodLabelKey(period)),
    );

    if (downGoalLabels.length > 0) {
      const confirmResult = await sweetConfirm({
        title: message("frontend.profile.goal.downConfirmTitle"),
        text: message("frontend.profile.goal.downConfirmText", [downGoalLabels.join(", ")]),
        confirmButtonText: message("frontend.common.confirm"),
        cancelButtonText: message("frontend.common.cancel"),
      });

      if (!confirmResult.isConfirmed) {
        return;
      }
    }

    try {
      setIsGoalSaving(true);
      const response = await updateReadingGoalApi({
        weekGoalCnt: nextWeekGoalCnt,
        monthGoalCnt: nextMonthGoalCnt,
        yearGoalCnt: nextYearGoalCnt,
      });
      setMonthlySummary(response.data as MonthlyReadingSummary);
      await closeProfileModal("goal");
      await sweetSuccess(
        message("frontend.profile.goal.savedTitle"),
        message("frontend.profile.goal.saved"),
      );
    } catch (error) {
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsGoalSaving(false);
    }
  };

  /**
   * 월간/연간 독서 요약 행과 펼침 목록을 공통 구조로 렌더링합니다.
   * 권수 비교 버튼은 별도 버튼으로 유지하고, 제목/권수 영역을 누르면 목록만 부드럽게 열리도록 분리합니다.
   *
   * @author Hanwon.Jang
   * @param period 월간 또는 연간 구분값
   * @param code 달력 아이콘 안에 표시할 월 영문 또는 연도
   * @param titleKey 제목 메시지 key
   * @param countKey 권수 메시지 key
   * @param count 현재 기간 완료 권수
   * @param diff 이전 기간 대비 증감 권수
   * @param diffAriaKey 증감 버튼 접근성 메시지 key
   * @param reports 펼침 영역에 표시할 완료 독후감 목록
   * @return 독서 요약 행 JSX
   */
  const renderReadingSummaryRow = (
    period: ReadingPeriod,
    code: string | undefined,
    titleKey: string,
    countKey: string,
    count: number,
    diff: number,
    diffAriaKey: string,
    reports: ReadingSummaryReport[] = [],
  ) => {
    const isExpanded = expandedSummary[period];
    const hasReports = reports.length > 0;
    const goalCnt =
      period === "week"
        ? monthlySummary?.weekGoalCnt
        : period === "month"
          ? monthlySummary?.monthGoalCnt
          : monthlySummary?.yearGoalCnt;
    const goalRate =
      period === "week"
        ? monthlySummary?.weekGoalRate ?? 0
        : period === "month"
          ? monthlySummary?.monthGoalRate ?? 0
          : monthlySummary?.yearGoalRate ?? 0;
    const goalSet =
      period === "week"
        ? Boolean(monthlySummary?.weekGoalSet)
        : period === "month"
          ? Boolean(monthlySummary?.monthGoalSet)
          : Boolean(monthlySummary?.yearGoalSet);
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
          <div
            className={styles.monthlyDiffTooltipWrap}
            ref={(element) => {
              diffTooltipRefs.current[period] = element;
            }}
          >
            <button
              className={
                diff === 0
                  ? styles.monthlyDiffNeutral
                  : diff > 0
                    ? styles.monthlyDiffUp
                    : styles.monthlyDiffDown
              }
              type="button"
              aria-label={message(diffAriaKey)}
              aria-expanded={activeDiffTooltip === period}
              onClick={() => handleReadingDiffClick(diff, period)}
            >
              {formatReadingDiff(diff)}
            </button>
            {activeDiffTooltip === period && (
              <div className={styles.monthlyDiffTooltip} role="tooltip">
                {getReadingDiffMessage(diff, period)}
              </div>
            )}
          </div>
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
                  className={styles.readingSummaryReport}
                  type="button"
                  key={report.reptNumb}
                  onClick={() => handleSummaryReportClick(report.reptNumb)}
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
                          {getReadingGradeText(report.reptGrde)}
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

  /**
   * 프로필 수정 버튼 클릭 시 기본 동작과 상위 영역 이벤트 전파를 막고 수정 모드로 전환합니다.
   * 배경 영역 안의 버튼이 다른 요소로 포커스되거나 클릭 이벤트가 겹치지 않도록 클릭 흐름을 고정합니다.
   *
   * @author Hanwon.Jang
   * @param event 프로필 수정 버튼 클릭 이벤트
   */
  const handleEditModeClick = (event: MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
    event.stopPropagation();
    // 편집 진입 시점의 최신 프로필 값을 input 상태에 다시 주입해 빈 값으로 열리는 경우를 막는다.
    setUserNick(profile?.userNick ?? "");
    setIntrCntn(profile?.intrCntn ?? "");
    setIsEditMode(true);
  };

  /**
   * 닉네임 필수값을 확인한 뒤 프로필 수정 API를 호출해 텍스트와 이미지 파일을 함께 저장합니다.
   * 저장에 성공하면 서버가 반환한 최신 프로필 정보로 화면을 갱신하고 조회 모드로 되돌립니다.
   *
   * @author Hanwon.Jang
   * @param event 프로필 수정 폼 제출 이벤트
   */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!userNick.trim()) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.nickRequired"),
      );
      return;
    }

    if (
      userNick.trim().length > USER_NICK_MAX_LENGTH ||
      !USER_NICK_REGEX.test(userNick.trim())
    ) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.nickKoreanOnly"),
      );
      return;
    }

    try {
      setIsSaving(true);
      const response = await updateMyProfileApi({
        userNick: userNick.trim(),
        intrCntn: intrCntn.trim(),
        profileImage,
        backgroundImage,
      });
      const nextProfile = response.data as UserProfile;
      syncProfileState(nextProfile);
      notifyUserProfileUpdated(nextProfile);
      setIsEditMode(false);
      await sweetSuccess(
        message("frontend.profile.savedTitle"),
        message("frontend.profile.saved"),
      );
    } catch (error) {
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  return (
    <main className={styles.page}>
      <form className={styles.profileShell} onSubmit={handleSubmit}>
        <section
          className={styles.cover}
          style={
            previewBackground
              ? { backgroundImage: `url("${previewBackground}")` }
              : undefined
          }
        >
          {!previewBackground && (
            <p className={styles.coverEmptyText}>
              {message("frontend.profile.background.empty")}
            </p>
          )}

          <div className={styles.coverActionGroup}>
            {isEditMode && (
              <label className={styles.coverImageButton}>
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M9 4 7.2 6H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-3.2L15 4H9Zm3 14a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                </svg>
                {message("frontend.profile.backgroundChange")}
                <input
                  className={styles.hiddenInput}
                  type="file"
                  accept="image/*"
                  onChange={(event) =>
                    applyImagePreview(event.currentTarget.files?.[0], "background")
                  }
                />
              </label>
            )}

            {isEditMode ? (
              <button className={styles.coverSaveButton} type="submit" disabled={isSaving}>
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M5 3h12.6L21 6.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Zm2 2v5h9V5H7Zm0 14h10v-6H7v6Z" />
                </svg>
                {message("frontend.report.save")}
              </button>
            ) : (
              <button
                className={styles.coverProfileEditButton}
                type="button"
                onClick={handleEditModeClick}
              >
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M4 20h4.7L19.4 9.3a2.1 2.1 0 0 0 0-3L17.7 4.6a2.1 2.1 0 0 0-3 0L4 15.3V20Zm2-2v-1.9L16.1 6l1.9 1.9L7.9 18H6Z" />
                </svg>
                {message("frontend.profile.edit")}
              </button>
            )}
          </div>
        </section>

        <section className={styles.profileBody}>
          <div className={styles.profileHeaderRow}>
            <div className={styles.avatarWrap}>
              <img
                className={styles.profileImage}
                src={previewImage}
                alt={profile?.userNick ?? message("frontend.profile.edit")}
              />
              {isEditMode && (
                <label className={styles.avatarCameraButton}>
                  <svg
                    className={styles.cameraIcon}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                    focusable="false"
                  >
                    <path d="M9 4 7.2 6H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-3.2L15 4H9Zm3 14a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                  </svg>
                  <input
                    className={styles.hiddenInput}
                    type="file"
                    accept="image/*"
                    onChange={(event) =>
                      applyImagePreview(event.currentTarget.files?.[0], "profile")
                    }
                  />
                </label>
              )}
            </div>

            <div className={styles.profileText}>
              {isEditMode ? (
                <input
                  className={styles.profileNameInput}
                  value={userNick}
                  maxLength={USER_NICK_MAX_LENGTH}
                  aria-label={message("frontend.profile.nick")}
                  onChange={(event) =>
                    setUserNick(normalizeUserNick(event.currentTarget.value))
                  }
                />
              ) : (
                <h1 className={styles.profileName}>{profile?.userNick || "-"}</h1>
              )}

              {isEditMode ? (
                <textarea
                  className={styles.profileIntroInput}
                  value={intrCntn}
                  maxLength={PROFILE_INTRO_MAX_LENGTH}
                  aria-label={message("frontend.profile.intro")}
                  onChange={(event) =>
                    setIntrCntn(normalizeProfileIntro(event.currentTarget.value))
                  }
                />
              ) : (
                <p className={styles.profileIntro}>
                  {profile?.intrCntn || message("frontend.profile.intro.empty")}
                </p>
              )}
            </div>
          </div>
        </section>

          {monthlySummary && (
            <>
              {renderCurrentReadingReports(monthlySummary.currentReadingReports)}
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
                        {message("frontend.profile.goal.achievementCount", [monthlySummary.weekGoalAchvCnt])}
                      </strong>
                    </div>
                    <div className={styles.goalAchievementItem}>
                      <span className={styles.goalAchievementLabel}>
                        {message("frontend.profile.goal.monthLabel")}
                      </span>
                      <strong className={styles.goalAchievementCount}>
                        {message("frontend.profile.goal.achievementCount", [monthlySummary.monthGoalAchvCnt])}
                      </strong>
                    </div>
                    <div className={styles.goalAchievementItem}>
                      <span className={styles.goalAchievementLabel}>
                        {message("frontend.profile.goal.yearLabel")}
                      </span>
                      <strong className={styles.goalAchievementCount}>
                        {message("frontend.profile.goal.achievementCount", [monthlySummary.yearGoalAchvCnt])}
                      </strong>
                    </div>
                    <div className={styles.goalAchievementItem}>
                      <span className={styles.goalAchievementLabel}>
                        {message("frontend.profile.goal.totalLabel")}
                      </span>
                      <strong className={styles.goalAchievementCount}>
                        {message("frontend.profile.goal.achievementCount", [monthlySummary.totalGoalAchvCnt])}
                      </strong>
                    </div>
                  </div>
                </div>
                <div className={styles.readingSummaryDivider} />
                {renderReadingSummaryRow(
                  "week",
                  monthlySummary.weekCode,
                  "frontend.profile.weeklyReading.title",
                  "frontend.profile.weeklyReading.count",
                  monthlySummary.currentWeekCount,
                  monthlySummary.weekCountDiff,
                  "frontend.profile.weeklyReading.diffAria",
                  monthlySummary.currentWeekReports,
                )}
                <div className={styles.readingSummaryDivider} />
                {renderReadingSummaryRow(
                  "month",
                  monthlySummary.monthCode,
                  "frontend.profile.monthlyReading.title",
                  "frontend.profile.monthlyReading.count",
                  monthlySummary.currentMonthCount,
                  monthlySummary.countDiff,
                  "frontend.profile.monthlyReading.diffAria",
                  monthlySummary.currentMonthReports,
                )}
                <div className={styles.readingSummaryDivider} />
                {renderReadingSummaryRow(
                  "year",
                  monthlySummary.yearCode,
                  "frontend.profile.yearlyReading.title",
                  "frontend.profile.yearlyReading.count",
                  monthlySummary.currentYearCount,
                  monthlySummary.yearCountDiff,
                  "frontend.profile.yearlyReading.diffAria",
                  monthlySummary.currentYearReports,
                )}
              </section>
              <button
                className={styles.goalSettingButton}
                type="button"
                onClick={handleGoalModalOpen}
              >
                {message(
                  monthlySummary.weekGoalSet && monthlySummary.monthGoalSet && monthlySummary.yearGoalSet
                    ? "frontend.profile.goal.edit"
                    : "frontend.profile.goal.set",
                )}
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M5.19751 11.62L9.00083 7.81668C9.44999 7.36752 9.44999 6.63252 9.00083 6.18335L5.19751 2.38" stroke="#8a8a8a" strokeWidth="1.5" strokeMiterlimit="10" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
              </button>
            </>
          )}
      </form>

      {/* Render dialogs under body so page scroll/transition transforms cannot move fixed overlays. */}
      {quickReport && createPortal((
        <div
          className={`${styles.goalModalOverlay} ${
            closingModal === "quick" ? styles.goalModalOverlayClosing : ""
          }`}
          role="presentation"
          onMouseDown={(event) => {
            if (event.currentTarget === event.target) {
              void closeProfileModal("quick");
            }
          }}
        >
          <section
            className={`${styles.goalModal} ${
              closingModal === "quick" ? styles.goalModalClosing : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="quick-reading-title"
          >
            <div className={styles.goalModalHeader}>
              <div>
                <h2 className={styles.goalModalTitle} id="quick-reading-title">
                  {message("frontend.profile.currentReading.quickTitle")}
                </h2>
                <p className={styles.quickReadingHelp}>
                  {message("frontend.profile.currentReading.quickHelp")}
                </p>
              </div>
              <button
                className={styles.goalModalClose}
                type="button"
                aria-label={message("frontend.common.close")}
                onClick={() => void closeProfileModal("quick")}
              >
                ×
              </button>
            </div>

            <div className={styles.quickReadingBody}>
              <div className={styles.quickReadingBookInfo}>
                {quickReport.bookCvim && (
                  <img
                    className={styles.quickReadingCover}
                    src={quickReport.bookCvim}
                    alt=""
                  />
                )}
                {!quickReport.bookCvim && (
                  <span className={styles.quickReadingCoverPlaceholder} aria-hidden="true" />
                )}
                <div className={styles.quickReadingBookText}>
                  <p className={styles.quickReadingBookTitle}>
                    {quickReport.bookTitl || message("frontend.common.noBookInfo")}
                  </p>
                  {getTargetReadingPeriodText(quickReport) && (
                    <p className={styles.quickReadingBookMeta}>
                      {getTargetReadingPeriodText(quickReport)}
                    </p>
                  )}
                </div>
                <button
                  className={styles.quickReadingEditButton}
                  type="button"
                  aria-label={message("frontend.profile.currentReading.editFull")}
                  onClick={handleQuickEditClick}
                >
                  <span>{message("frontend.profile.currentReading.editFull")}</span>
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <path d="M5.25 2.92L9.33 7L5.25 11.08" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </button>
              </div>
              <div className={styles.quickStatusGroup}>
                <button
                  className={
                    quickStatus === REPORT_STATUS_DONE
                      ? styles.quickStatusOptionActive
                      : styles.quickStatusOption
                  }
                  type="button"
                  onClick={() => setQuickStatus(REPORT_STATUS_DONE)}
                >
                  {message("frontend.report.status.done")}
                </button>
                <button
                  className={
                    quickStatus === REPORT_STATUS_STOP
                      ? styles.quickStatusOptionActive
                      : styles.quickStatusOption
                  }
                  type="button"
                  onClick={() => setQuickStatus(REPORT_STATUS_STOP)}
                >
                  {message("frontend.report.status.stopped")}
                </button>
              </div>
              <div className={styles.quickStarGroup}>
                <RatingField
                  value={quickGrade}
                  onChange={setQuickGrade}
                  disabled={quickStatus !== REPORT_STATUS_DONE}
                />
              </div>
            </div>

            <div className={styles.goalModalActions}>
              <button
                className={styles.goalModalCancel}
                type="button"
                onClick={() => void closeProfileModal("quick")}
              >
                {message("frontend.profile.currentReading.close")}
              </button>
              <button
                className={styles.goalModalSave}
                type="button"
                disabled={isQuickSaving}
                onClick={handleQuickSaveClick}
              >
                {message("frontend.profile.currentReading.save")}
              </button>
            </div>
          </section>
        </div>
      ), document.body)}

      {isGoalModalOpen && createPortal((
        <div
          className={`${styles.goalModalOverlay} ${
            closingModal === "goal" ? styles.goalModalOverlayClosing : ""
          }`}
          role="presentation"
          onMouseDown={(event) => {
            if (event.currentTarget === event.target) {
              void closeProfileModal("goal");
            }
          }}
        >
          <section
            className={`${styles.goalModal} ${
              closingModal === "goal" ? styles.goalModalClosing : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="reading-goal-title"
          >
            <div className={styles.goalModalHeader}>
              <h2 className={styles.goalModalTitle} id="reading-goal-title">
                {message("frontend.profile.goal.modalTitle")}
              </h2>
              <div className={styles.goalModalHeaderActions}>
                <button
                  className={styles.goalHelpButton}
                  type="button"
                  onClick={() => {
                    setClosingModal(null);
                    setIsGoalHelpModalOpen(true);
                  }}
                >
                  {message("frontend.profile.goal.helpButton")}
                </button>
                <button
                  className={styles.goalModalClose}
                  type="button"
                  aria-label={message("frontend.common.close")}
                  onClick={() => void closeProfileModal("goal")}
                >
                  ×
                </button>
              </div>
            </div>
            <div className={styles.goalModalBody}>
              <label className={styles.goalInputLabel}>
                <span>{message("frontend.profile.goal.weekLabel")}</span>
                <div className={styles.goalStepper}>
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.weekLabel")} 감소`}
                    onClick={() => handleGoalCountStep("week", -1)}
                  >
                    -
                  </button>
                  <input
                    className={styles.goalInput}
                    inputMode="numeric"
                    value={weekGoalCnt}
                    placeholder={message("frontend.profile.goal.placeholder")}
                    onChange={(event) =>
                      setWeekGoalCnt(normalizeGoalCount(event.currentTarget.value))
                    }
                  />
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.weekLabel")} 증가`}
                    onClick={() => handleGoalCountStep("week", 1)}
                  >
                    +
                  </button>
                </div>
                {renderGoalLimitInfo("week")}
              </label>
              <label className={styles.goalInputLabel}>
                <span>{message("frontend.profile.goal.monthLabel")}</span>
                <div className={styles.goalStepper}>
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.monthLabel")} 감소`}
                    onClick={() => handleGoalCountStep("month", -1)}
                  >
                    -
                  </button>
                  <input
                    className={styles.goalInput}
                    inputMode="numeric"
                    value={monthGoalCnt}
                    placeholder={message("frontend.profile.goal.placeholder")}
                    onChange={(event) =>
                      setMonthGoalCnt(normalizeGoalCount(event.currentTarget.value))
                    }
                  />
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.monthLabel")} 증가`}
                    onClick={() => handleGoalCountStep("month", 1)}
                  >
                    +
                  </button>
                </div>
                {renderGoalLimitInfo("month")}
              </label>
              <label className={styles.goalInputLabel}>
                <span>{message("frontend.profile.goal.yearLabel")}</span>
                <div className={styles.goalStepper}>
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.yearLabel")} 감소`}
                    onClick={() => handleGoalCountStep("year", -1)}
                  >
                    -
                  </button>
                  <input
                    className={styles.goalInput}
                    inputMode="numeric"
                    value={yearGoalCnt}
                    placeholder={message("frontend.profile.goal.placeholder")}
                    onChange={(event) =>
                      setYearGoalCnt(normalizeGoalCount(event.currentTarget.value))
                    }
                  />
                  <button
                    className={styles.goalStepperButton}
                    type="button"
                    aria-label={`${message("frontend.profile.goal.yearLabel")} 증가`}
                    onClick={() => handleGoalCountStep("year", 1)}
                  >
                    +
                  </button>
                </div>
                {renderGoalLimitInfo("year")}
              </label>
            </div>
            <div className={styles.goalModalActions}>
              <button
                className={styles.goalModalCancel}
                type="button"
                onClick={() => void closeProfileModal("goal")}
              >
                {message("frontend.common.cancel")}
              </button>
              <button
                className={styles.goalModalSave}
                type="button"
                disabled={isGoalSaving}
                onClick={handleGoalSubmit}
              >
                {message("frontend.report.save")}
              </button>
            </div>
          </section>
        </div>
      ), document.body)}
      {isGoalHelpModalOpen && createPortal((
        <div
          className={`${styles.goalModalOverlay} ${
            closingModal === "goalHelp" ? styles.goalModalOverlayClosing : ""
          }`}
          role="presentation"
          onMouseDown={(event) => {
            if (event.currentTarget === event.target) {
              void closeProfileModal("goalHelp");
            }
          }}
        >
          <section
            className={`${styles.goalHelpModal} ${
              closingModal === "goalHelp" ? styles.goalModalClosing : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="reading-goal-help-title"
          >
            <div className={styles.goalModalHeader}>
              <h2 className={styles.goalModalTitle} id="reading-goal-help-title">
                {message("frontend.profile.goal.helpTitle")}
              </h2>
              <button
                className={styles.goalModalClose}
                type="button"
                aria-label={message("frontend.common.close")}
                onClick={() => void closeProfileModal("goalHelp")}
              >
                x
              </button>
            </div>
            <div className={styles.goalHelpBody}>
              <p className={styles.goalHelpLead}>
                {message("frontend.profile.goal.helpLead")}
              </p>
              <ul className={styles.goalHelpList}>
                <li>{message("frontend.profile.goal.helpWeek")}</li>
                <li>{message("frontend.profile.goal.helpMonth")}</li>
                <li>{message("frontend.profile.goal.helpYear")}</li>
                <li>{message("frontend.profile.goal.helpSameValue")}</li>
              </ul>
            </div>
          </section>
        </div>
      ), document.body)}
    </main>
  );
}

export default ProfileEditPage;
