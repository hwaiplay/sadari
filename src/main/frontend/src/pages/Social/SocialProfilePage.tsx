import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetAlert, sweetConfirm, sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import {
  formatDashedDateToDot,
  getRemainDaysUntil,
} from "@/app/utils/dateUtil";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import Loading from "@/components/Loading/Loading";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import { useProgressiveList } from "@/components/InfiniteScroll/useProgressiveList";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import {
  delSocialFollowApi,
  getSocialFollowStatusApi,
  getSocialFollowListApi,
  getSocialProfileApi,
  getSocialReadingApi,
  setSocialFollowApi,
  type FollowListType,
  type FollowUser,
} from "@/features/Social/api/socialApi";
import { isFollowedByMe } from "@/features/Social/utils/followStatus";
import type {
  MonthlyReadingSummary,
  ReadingSummaryReport,
  UserProfile,
} from "@/features/User/api/userApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import ReadingStatisticsSection from "@/pages/My/ReadingStatisticsSection";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate, useParams } from "react-router-dom";
import * as styles from "@/pages/My/ProfileEditPage.css";

type ReadingPeriod = "week" | "month" | "year";

/**
 * 독후감 요약 목록에 표시할 독서 기간을 조합합니다.
 * 시작일과 종료일 중 일부만 존재해도 불필요한 구분자가 표시되지 않도록 빈 값을 제거합니다.
 *
 * @author HanWon.Jang
 * @param report 독서 기간을 표시할 독후감 요약 정보
 * @return 화면에 표시할 독서 기간 문자열
 */
const getReadingEndDateText = (report: ReadingSummaryReport) => {

  return formatDashedDateToDot(report.reptEndt);
};

/**
 * 숫자 평점을 5개 별 표시 문자열로 변환합니다.
 * 서버 응답이 비어 있거나 숫자로 바꿀 수 없는 경우 0점으로 처리해 화면 표시를 안정적으로 유지합니다.
 *
 * @author HanWon.Jang
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
 * @author HanWon.Jang
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
 * get Reading Remain Rate 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param remainDays remain Days 입력값
 * @return 처리 결과
 */
const getReadingRemainRate = (remainDays: number) => {
  // 현재 읽고 있는 책의 남은 기간 색상은 전체 목표기간 비율이 아니라 남은 10일을 기준으로 판단한다.
  // 10일 이상 남으면 가장 여유 있는 색상, 0일에 가까워질수록 기존 색상 단계가 내려간다.
  return Math.max(0, Math.min(100, Math.round((Math.max(remainDays, 0) / 10) * 100)));
};

/**
 * 다른 사용자의 프로필과 독서 활동 현황을 보여주는 읽기 전용 페이지입니다.
 * 공개 독후감 목록에서 작성자 프로필을 눌렀을 때 진입하며, 마이페이지와 같은 활동 요약 데이터를 표시합니다.
 *
 * @author HanWon.Jang
 * @return 소셜 프로필 페이지 컴포넌트
 */
function SocialProfilePage() {

  const navigate = useNavigate();
  const { userNumb } = useParams();
  const targetUserNumb = Number(userNumb);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [summary, setSummary] = useState<MonthlyReadingSummary | null>(null);
  const [followStatName, setFollowStatName] = useState("");
  const [isFollowUpdating, setIsFollowUpdating] = useState(false);
  const [followListType, setFollowListType] = useState<FollowListType | null>(null);
  const [followUsers, setFollowUsers] = useState<FollowUser[]>([]);
  const [isFollowListLoading, setIsFollowListLoading] = useState(false);
  const [isFollowListScrolling, setIsFollowListScrolling] = useState(false);
  const [followUpdatingUserNumb, setFollowUpdatingUserNumb] = useState<number | null>(null);
  const {
    visibleItems: visibleFollowUsers,
    hasNext: hasNextFollowUser,
    loadMore: loadMoreFollowUser,
  } = useProgressiveList(
    followUsers,
    `${targetUserNumb}:${followListType ?? "closed"}`,
  );
  const [expandedSummary, setExpandedSummary] = useState<Record<ReadingPeriod, boolean>>({
    week: false,
    month: false,
    year: false,
  });
  const [isLoading, setIsLoading] = useState(true);
  const followListScrollTimeoutRef = useRef<number | null>(null);
  useBodyScrollLock(Boolean(followListType));

  useEffect(() => {

    let ignore = false;

    // 잘못된 사용자 번호는 API 호출 전에 차단해 불필요한 서버 요청을 만들지 않습니다.
    if (!Number.isFinite(targetUserNumb) || targetUserNumb <= 0) {
      setIsLoading(false);
      return () => {

        ignore = true;
      };
    }

    // 후속 API 호출 전에 대상 회원의 최신 상태를 먼저 조회한다.
    getSocialProfileApi(targetUserNumb)
      .then(async (profileResponse) => {

        const nextProfile = profileResponse.data as UserProfile;

        // 정상 이용 상태가 아니면 상태를 안내하고 프로필 세부 조회를 중단한다.
        if (nextProfile.userStat && nextProfile.userStat !== "ACTIVE") {
          const userStatus = `${nextProfile.userStat}(${nextProfile.userStatName ?? "-"})`;
          // 접근할 수 없는 회원 상태를 안내한다.
          const alertResult = await sweetAlert({
            // "현재 접근할 수 없는 회원이에요."
            title: message("frontend.social.restrictedProfile.title"),
            // "회원 상태는 {0}입니다. 확인하면 이전 화면으로 이동해요."
            text: message("frontend.social.restrictedProfile.text", [userStatus]),
            icon: "warning",
            allowOutsideClick: false,
          });

          // 상태 안내를 확인하면 진입 전 화면으로 돌아간다.
          if (!ignore && alertResult.isConfirmed) {
            navigate(-1);
          }
          return;
        }

        // 정상 회원에게만 독서 활동과 팔로우 관계를 병렬로 조회한다.
        const [summaryResponse, followStatusResponse] = await Promise.all([
          getSocialReadingApi(targetUserNumb),
          getSocialFollowStatusApi(targetUserNumb),
        ]);

        if (!ignore) {
          setProfile(nextProfile);
          setSummary(summaryResponse.data as MonthlyReadingSummary);
          setFollowStatName(followStatusResponse.data?.followStatName ?? "");
        }
      })
      .catch(() => {

        if (!ignore) {
          setProfile(null);
          setSummary(null);
          setFollowStatName("");
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
  }, [navigate, targetUserNumb]);

  useEffect(() => {

    return () => {

      if (followListScrollTimeoutRef.current) {
        window.clearTimeout(followListScrollTimeoutRef.current);
      }
    };
  }, []);

  /**
   * 프로필 팔로우 버튼의 현재 관계에 맞춰 팔로우 또는 언팔로우 API를 호출한다
   * 팔로잉과 친구는 내가 상대를 팔로우 중인 상태이므로 삭제하고, 그 외에는 팔로우 관계를 저장한다
   *
   * @author HanWon.Jang
   * @return 팔로우 관계와 프로필 통계 갱신이 끝난 Promise
   * @throws 팔로우 또는 프로필 통계 요청 실패 시 발생
   */
  const handleFollowButtonClick = async () => {

    // 중복 클릭 중에는 현재 팔로우 요청이 끝날 때까지 추가 조작을 차단한다
    if (isFollowUpdating) {
      // 진행 중인 팔로우 요청을 유지하고 추가 요청 없이 종료한다
      return;
    }

    // 현재 버튼명으로 로그인 사용자가 만든 팔로우 관계의 존재 여부를 판정한다
    const isFollowing = isFollowedByMe(followStatName);

    // 팔로잉 또는 친구 상태를 해제하기 전에 사용자 확인을 받는다
    if (isFollowing) {
      const result = await sweetConfirm({
        // "언팔로우하시겠어요?"
        title: message("frontend.social.unfollow.title"),
        // "팔로잉 목록에서 삭제돼요."
        text: message("frontend.social.unfollow.text"),
        // "언팔로우"
        confirmButtonText: message("frontend.social.unfollow.confirm"),
        // "취소"
        cancelButtonText: message("frontend.common.cancel"),
      });

      // 사용자가 취소하면 기존 관계를 유지한다
      if (!result.isConfirmed) {
        // 팔로우 관계 변경 없이 종료한다
        return;
      }
    }

    // 팔로우 관계 변경이 끝날 때까지 버튼을 비활성화한다
    setIsFollowUpdating(true);

    // 현재 관계에 맞는 팔로우 등록 또는 삭제 요청을 실행한다
    try {
      const response =
        isFollowing
          ? await delSocialFollowApi(targetUserNumb)
          : await setSocialFollowApi(targetUserNumb);

      setFollowStatName(response.data?.followStatName ?? "");
      const summaryResponse = await getSocialReadingApi(targetUserNumb);
      setSummary(summaryResponse.data as MonthlyReadingSummary);
    } catch {
      void sweetWarning(
        message("frontend.common.invalidAccess"),
        message("frontend.common.tryAgain"),
      );
    } finally {
      setIsFollowUpdating(false);
    }
  };

  /**
   * handle Follow List Open 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param type type 입력값
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleFollowListOpen = async (type: FollowListType) => {

    setFollowListType(type);
    setFollowUsers([]);
    setIsFollowListScrolling(false);
    setIsFollowListLoading(true);

    try {
      const response = await getSocialFollowListApi(targetUserNumb, type);
      setFollowUsers((response.data ?? []) as FollowUser[]);
    } catch (error) {
      void sweetError(
        message("frontend.common.invalidAccess"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
      setFollowListType(null);
    } finally {
      setIsFollowListLoading(false);
    }
  };

  /**
   * handle Follow List Close 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleFollowListClose = () => {

    setFollowListType(null);
    setFollowUsers([]);
    setIsFollowListScrolling(false);
  };

  /**
   * handle Follow List Scroll 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleFollowListScroll = () => {

    setIsFollowListScrolling(true);

    if (followListScrollTimeoutRef.current) {
      window.clearTimeout(followListScrollTimeoutRef.current);
    }

    followListScrollTimeoutRef.current = window.setTimeout(() => {

      setIsFollowListScrolling(false);
      followListScrollTimeoutRef.current = null;
    }, 650);
  };

  /**
   * handle Follow List User Click 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param nextUserNumb next User Numb 입력값
   * @return 반환값이 없다
   */
  const handleFollowListUserClick = (nextUserNumb: number) => {

    handleFollowListClose();
    navigate(`/social/profile/${nextUserNumb}`);
  };

  /**
   * 팔로우 목록 사용자의 현재 관계에 맞춰 팔로우 또는 언팔로우 API를 호출한다
   *
   * @author HanWon.Jang
   * @param user 관계를 변경할 팔로우 목록 사용자
   * @return 팔로우 관계와 프로필 통계 갱신이 끝난 Promise
   * @throws 팔로우 또는 프로필 통계 요청 실패 시 발생
   */
  const handleFollowStatusClick = async (user: FollowUser) => {

    // 다른 관계 변경이 진행 중이거나 내 계정 행이면 추가 조작을 허용하지 않는다
    if (followUpdatingUserNumb || user.meYsno === "Y") {
      // 현재 목록 상태를 유지하고 종료한다
      return;
    }

    // 목록 사용자를 내가 팔로우 중인지 버튼명으로 판정한다
    const isFollowing = isFollowedByMe(user.followStatName);

    // 팔로잉 또는 친구 상태를 해제하기 전에 사용자 확인을 받는다
    if (isFollowing) {
      const result = await sweetConfirm({
        // "언팔로우하시겠어요?"
        title: message("frontend.social.unfollow.title"),
        // "팔로잉 목록에서 삭제돼요."
        text: message("frontend.social.unfollow.text"),
        // "언팔로우"
        confirmButtonText: message("frontend.social.unfollow.confirm"),
        // "취소"
        cancelButtonText: message("frontend.common.cancel"),
      });

      // 사용자가 취소하면 기존 관계를 유지한다
      if (!result.isConfirmed) {
        // 팔로우 관계 변경 없이 종료한다
        return;
      }
    }

    setFollowUpdatingUserNumb(user.userNumb);

    try {
      const response =
        isFollowing
          ? await delSocialFollowApi(user.userNumb)
          : await setSocialFollowApi(user.userNumb);

      setFollowUsers((prev) =>
        prev.map((item) =>
          item.userNumb === user.userNumb
            ? { ...item, followStatName: response.data?.followStatName ?? item.followStatName }
            : item,
        ),
      );

      const summaryResponse = await getSocialReadingApi(targetUserNumb);
      setSummary(summaryResponse.data as MonthlyReadingSummary);
    } catch (error) {
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setFollowUpdatingUserNumb(null);
    }
  };

  /**
   * 주간, 월간, 연간 요약 리스트의 펼침 상태를 전환합니다.
   * 읽은 책이 있는 영역에만 호출되어 빈 목록에 대한 불필요한 상태 변경을 막습니다.
   *
   * @author HanWon.Jang
   * @param period 펼침 상태를 변경할 기간 구분값
   */
  const handleReadingSummary = (period: ReadingPeriod) => {

    setExpandedSummary((prev) => ({
      ...prev,
      [period]: !prev[period],
    }));
  };

  /**
   * 다른 사용자의 요약 독후감 항목을 선택했을 때 공개 여부에 따라 이동 또는 경고를 처리합니다.
   * 공개 독후감은 ISBN 기준 공개 독후감 목록으로 이동하고, 비공개 독후감은 사용자가 내용을 볼 수 없음을 안내합니다.
   *
   * @author HanWon.Jang
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

    navigate(`/report/public-reports/isbn?isbn=${encodeURIComponent(report.bookIsbn)}`, {
      state: {
        title: report.bookTitl,
        author: report.bookAthr,
        cover: report.bookCvim,
      },
    });
  };

  /**
   * render Profile Stats 화면 요소를 구성한다
   *
   * @author HanWon.Jang
   * @param summaryData summary Data 입력값
   * @return 구성된 화면 요소
   */
  const renderProfileStats = (summaryData: MonthlyReadingSummary) => {

    const stats = [
      {
        label: message("frontend.profile.stats.totalReadBook"),
        value: /* "{0}권" */ message("frontend.common.bookCount", [summaryData.totalReadBookCnt ?? 0]),
        listType: null,
      },
      {
        label: /* "팔로우" */ message("frontend.common.following"),
        value: message("frontend.profile.stats.userCount", [summaryData.followingCnt ?? 0]),
        listType: "following" as FollowListType,
      },
      {
        label: /* "팔로워" */ message("frontend.common.followers"),
        value: message("frontend.profile.stats.userCount", [summaryData.followerCnt ?? 0]),
        listType: "followers" as FollowListType,
      },
      {
        label: message("frontend.profile.stats.receivedLike"),
        value: message("frontend.profile.stats.likeCount", [summaryData.receivedLikeCnt ?? 0]),
        listType: null,
      },
    ];

    return (
      /* 상대 사용자의 독서와 소셜 활동 통계 영역 */
      <section className={styles.monthlySummary} aria-label={message("frontend.profile.stats.title")}>
        <div className={styles.profileStatsSummary}>
          <div className={styles.goalAchievementGrid}>
            {stats.map((stat) => (
              <div className={styles.goalAchievementItem} key={stat.label}>
                {stat.listType ? (
                  <button
                    className={styles.profileStatsButton}
                    type="button"
                    onClick={() => void handleFollowListOpen(stat.listType)}
                  >
                    <span className={styles.goalAchievementLabel}>{stat.label}</span>
                    <strong className={styles.goalAchievementCount}>{stat.value}</strong>
                  </button>
                ) : (
                  <>
                    <span className={styles.goalAchievementLabel}>{stat.label}</span>
                    <strong className={styles.goalAchievementCount}>{stat.value}</strong>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>
    );
  };

  /**
   * 다른 사용자가 현재 읽고 있는 책의 목표 종료일까지 남은 기간 정보를 렌더링합니다.
   * 남은 기간이 적을수록 붉은 계열로 표시해 목표 종료일이 가까움을 보여줍니다.
   *
   * @author HanWon.Jang
   * @param reports 현재 읽고 있는 독후감 목록
   * @return 현재 읽고 있는 책 섹션 JSX
   */
  const renderCurrentReports = (reports: ReadingSummaryReport[] = []) => {

    if (reports.length === 0) {
      return null;
    }

    return (
      /* 상대 사용자가 현재 읽고 있는 책 목록 영역 */
      <section
        className={styles.monthlySummary}
        aria-label={message("frontend.profile.currentReading.title")}
      >
        <div className={styles.currentReadingSection}>
          <h2 className={`${styles.currentReadingTitle} ${styles.socialSectionTitle}`}>
            {/* "현재 읽고 있는 책" */}
            {message("frontend.profile.currentReading.title")}
          </h2>
          <div className={styles.currentReadingList}>
            {reports.map((report) => {

              const remainDays = getRemainDaysUntil(report.reptEndt);
              const remainRate = getReadingRemainRate(remainDays);
              const remainColor = getGoalProgressColor(remainRate);
              const isExpired = remainDays <= 0;

              return (
                <div className={styles.currentReadingCard} key={report.reptNumb}>
                  <img
                    className={styles.readingSummaryCover}
                    src={getBookCoverImageSource(report.bookCvim)}
                    onError={handleBookCoverImageError}
                    alt=""
                  />
                  <span className={styles.currentReadingText}>
                    <button
                      className={styles.readingSummaryBookTitleButton}
                      type="button"
                      onClick={() => {

                        if (!report.bookIsbn) {
                          void sweetWarning(
                            message("frontend.common.invalidAccess"),
                            message("frontend.common.noBookInfo"),
                          );
                          return;
                        }

                        // 다른 사용자 도서는 특정 독후감의 공개 여부와 무관하게 ISBN 기반 도서 정보로 이동합니다.
                        navigate(
                          `/report/public-reports/isbn?isbn=${encodeURIComponent(report.bookIsbn)}`,
                          {
                            state: {
                              title: report.bookTitl,
                              author: report.bookAthr,
                              cover: report.bookCvim,
                            },
                          },
                        );
                      }}
                    >
                      {report.bookTitl || message("frontend.common.noBookInfo")}
                    </button>
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
   * @author HanWon.Jang
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
                handleReadingSummary(period);
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
                  key={report.reptNumb}
                  onClick={() => handleSummaryReportClick(report)}
                >
                  <img
                    className={styles.readingSummaryCover}
                    src={getBookCoverImageSource(report.bookCvim)}
                    onError={handleBookCoverImageError}
                    alt=""
                  />
                  <span className={styles.readingSummaryBookText}>
                    <span
                      className={styles.readingSummaryBookTitleButton}
                      role="link"
                      tabIndex={0}
                      onClick={(event) => {

                        event.stopPropagation();

                        if (!report.bookIsbn) {
                          void sweetWarning(
                            message("frontend.common.invalidAccess"),
                            message("frontend.common.noBookInfo"),
                          );
                          return;
                        }

                        // 공개 여부와 관계없이 제목에서는 ISBN 기준 도서 정보 화면으로 이동합니다.
                        navigate(
                          `/report/public-reports/isbn?isbn=${encodeURIComponent(report.bookIsbn)}`,
                          {
                            state: {
                              title: report.bookTitl,
                              author: report.bookAthr,
                              cover: report.bookCvim,
                            },
                          },
                        );
                      }}
                      onKeyDown={(event) => {

                        if (event.key !== "Enter" && event.key !== " ") {
                          return;
                        }

                        event.preventDefault();
                        event.stopPropagation();

                        if (!report.bookIsbn) {
                          void sweetWarning(
                            message("frontend.common.invalidAccess"),
                            message("frontend.common.noBookInfo"),
                          );
                          return;
                        }

                        navigate(
                          `/report/public-reports/isbn?isbn=${encodeURIComponent(report.bookIsbn)}`,
                          {
                            state: {
                              title: report.bookTitl,
                              author: report.bookAthr,
                              cover: report.bookCvim,
                            },
                          },
                        );
                      }}
                    >
                      {report.bookTitl || message("frontend.common.noBookInfo")}
                    </span>
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

  if (isLoading) {
    return <Loading />;
  }

  if (!profile || !summary) {
    return <main className={styles.page}>{message("frontend.common.invalidAccess")}</main>;
  }

  // 탈퇴 회원은 기존 관계를 유지하되 프로필과 활동 정보를 공개하지 않습니다
  if (profile.userStat && profile.userStat !== "ACTIVE") {
    // 탈퇴 상태만 표시하는 제한된 공개 프로필 화면을 반환합니다
    return (
      <main className={styles.page}>
        {/* 탈퇴 회원 공개 프로필 제한 안내 영역 */}
        <section className={styles.profileShell}>
          <section className={styles.socialProfileBody}>
            <div className={styles.profileText}>
              <h1 className={styles.profileName}>탈퇴한 사용자</h1>
              <p className={styles.profileIntro}>탈퇴한 사용자의 정보는 표시되지 않아요.</p>
            </div>
          </section>
        </section>
      </main>
    );
  }

  return (
    /* 상대 사용자의 프로필과 독서 활동 전체 영역 */
    <main className={styles.page}>
      {/* 상대 사용자의 프로필 배경과 기본 정보 영역 */}
      <section className={styles.profileShell}>
        <div
          className={styles.cover}
          style={
            profile.bgimPath
              ? { backgroundImage: `url("${profile.bgimPath}")` }
              : undefined
          }
        >
        </div>

        {/* 상대 사용자 정보와 팔로우 상태 영역 */}
        <section className={styles.socialProfileBody}>
          <div className={styles.socialProfileHeaderRow}>
            <div className={styles.avatarWrap}>
              <ProfileImage
                className={styles.profileImage}
                src={profile.porfPath}
                alt={profile.userNick ?? message("frontend.profile.nick")}
              />
              {followStatName && (
                <button
                  className={styles.socialFollowButton}
                  data-follow-status={followStatName}
                  type="button"
                  disabled={isFollowUpdating}
                  onClick={handleFollowButtonClick}
                >
                  {followStatName}
                </button>
              )}
            </div>

            <div className={styles.profileText}>
              <h1 className={styles.profileName}>{profile.userNick || "-"}</h1>
              <p className={styles.profileIntro}>
                {profile.intrCntn || message("frontend.profile.intro.empty")}
              </p>
            </div>
          </div>

          {renderProfileStats(summary)}
          {renderCurrentReports(summary.currentReadingReports)}
        {/* 상대 사용자의 월간 독서 요약 영역 */}
          <section className={styles.monthlySummary} aria-label={message("frontend.profile.monthlyReading.title")}>
            <div className={styles.goalAchievementSummary}>
              <p className={`${styles.goalAchievementTitle} ${styles.socialSectionTitle}`}>
                {/* "목표 달성 횟수" */}
                {message("frontend.profile.goal.achievementTitle")}
              </p>
              <div className={styles.goalAchievementGrid}>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {/* "주간" */}
                    {message("frontend.profile.goal.weekLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {/* "{0}회" */}
                    {message("frontend.profile.goal.achievementCount", [summary.weekGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {/* "월간" */}
                    {message("frontend.profile.goal.monthLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {/* "{0}회" */}
                    {message("frontend.profile.goal.achievementCount", [summary.monthGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {/* "연간" */}
                    {message("frontend.profile.goal.yearLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {/* "{0}회" */}
                    {message("frontend.profile.goal.achievementCount", [summary.yearGoalAchvCnt])}
                  </strong>
                </div>
                <div className={styles.goalAchievementItem}>
                  <span className={styles.goalAchievementLabel}>
                    {/* "총" */}
                    {message("frontend.profile.goal.totalLabel")}
                  </span>
                  <strong className={styles.goalAchievementCount}>
                    {/* "{0}회" */}
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
              "frontend.common.bookCount",
              summary.currentWeekCount,
              summary.currentWeekReports,
            )}
            <div className={styles.readingSummaryDivider} />
            {renderReadingSummaryRow(
              "month",
              summary.monthCode,
              "frontend.profile.monthlyReading.title",
              "frontend.common.bookCount",
              summary.currentMonthCount,
              summary.currentMonthReports,
            )}
            <div className={styles.readingSummaryDivider} />
            {renderReadingSummaryRow(
              "year",
              summary.yearCode,
              "frontend.profile.yearlyReading.title",
              "frontend.common.bookCount",
              summary.currentYearCount,
              summary.currentYearReports,
            )}
          </section>
          {/* 스크롤 진입 시 공개 여부를 확인하는 상대 사용자의 독서 통계 영역 */}
          <ReadingStatisticsSection key={targetUserNumb} targetUserNumb={targetUserNumb} />
        </section>
      </section>

      {followListType && createPortal((
        <div
          className={styles.goalModalOverlay}
          role="presentation"
          onMouseDown={(event) => {

            if (event.currentTarget === event.target) {
              handleFollowListClose();
            }
          }}
        >
        {/* 상대 사용자의 목표 달성 기록 영역 */}
        <section
            className={styles.followModal}
            role="dialog"
            aria-modal="true"
            aria-labelledby="follow-list-title"
          >
            <div className={styles.goalModalHeader}>
              <h2 className={styles.goalModalTitle} id="follow-list-title">
                {message(
                  followListType === "following"
                    ? "frontend.common.following"
                    : "frontend.common.followers",
                )}
              </h2>
              <button
                className={styles.goalModalClose}
                type="button"
                aria-label={message("frontend.common.close")}
                onClick={handleFollowListClose}
              >
                ×
              </button>
            </div>

            <div
              className={isFollowListScrolling ? styles.followModalListScrolling : styles.followModalList}
              onScroll={handleFollowListScroll}
            >
              {isFollowListLoading && (
                <p className={styles.followModalEmpty}>
                  {/* "목록 조회 중" */}
                  {message("frontend.common.loadingList")}
                </p>
              )}
              {!isFollowListLoading && followUsers.length === 0 && (
                <p className={styles.followModalEmpty}>
                  {message(
                    followListType === "following"
                      ? "frontend.profile.followingList.empty"
                      : "frontend.profile.followerList.empty",
                  )}
                </p>
              )}
              {!isFollowListLoading && visibleFollowUsers.map((user) => (
                <div className={styles.followModalItem} key={user.userNumb}>
                  <button
                    className={styles.followModalProfileButton}
                    type="button"
                    onClick={() => handleFollowListUserClick(user.userNumb)}
                  >
                    <ProfileImage
                      className={styles.followModalAvatar}
                      src={user.porfPath}
                      alt={user.userNick ?? message("frontend.profile.nick")}
                    />
                    <span className={styles.followModalText}>
                      <strong className={styles.followModalName}>
                        {user.userNick || "-"}
                      </strong>
                      <span className={styles.followModalIntro}>
                        {user.intrCntn || message("frontend.profile.intro.empty")}
                      </span>
                    </span>
                  </button>
                  {user.meYsno !== "Y" && (
                    <button
                      className={styles.followModalStatusButton}
                      data-follow-status={user.followStatName}
                      type="button"
                      disabled={followUpdatingUserNumb === user.userNumb}
                      onClick={() => void handleFollowStatusClick(user)}
                    >
                      {user.followStatName}
                    </button>
                  )}
                </div>
              ))}
              <InfiniteScrollTrigger
                hasNext={!isFollowListLoading && hasNextFollowUser}
                onLoadMore={loadMoreFollowUser}
              />
            </div>
          </section>
        </div>
      ), document.body)}
    </main>
  );
}

export default SocialProfilePage;
