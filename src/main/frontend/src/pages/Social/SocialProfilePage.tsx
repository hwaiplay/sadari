import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetAlert, sweetConfirm, sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import BackgroundImage from "@/components/BackgroundImage/BackgroundImage";
import {
  formatDashedDateToDot,
  getRemainDaysUntil,
} from "@/app/utils/dateUtil";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import Loading from "@/components/Loading/Loading";
import { FullscreenImageButton } from "@/components/ImageViewer/FullscreenImageViewer";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import UserActionMenu from "@/components/UserActionMenu/UserActionMenu";
import type { SafetyReportOption } from "@/components/UserActionMenu/userActionMenu.types";
import * as modalControlStyles from "@/components/Modal/ModalControls.css";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import { setPublicReportLikeApi } from "@/features/Book/api/bookApi";
import ReplySheet from "@/features/reply/ReplySheet";
import {
  delSocialFollowApi,
  getSocialFollowStatusApi,
  getSocialProfileApi,
  getSocialReadingApi,
  setSocialFollowApi,
  type FollowListType,
  type FollowUser,
} from "@/features/Social/api/socialApi";
import LikeUserListButton from "@/features/Social/components/LikeUserListButton";
import { useFollowListModal } from "@/features/Social/hooks/useFollowListModal";
import { isFollowedByMe } from "@/features/Social/utils/followStatus";
import type {
  MonthlyReadingSummary,
  ReadingSummaryReport,
  UserProfile,
  ImageReaction,
} from "@/features/User/api/userApi";
import ProfileImage, {
  DEFAULT_PROFILE_IMAGE,
  normalizeProfileImageSource,
} from "@/features/User/components/ProfileImage";
import {
  getReadingEndDateText,
  getReadingGradeText,
} from "@/features/User/utils/profileReadingFormat";
import { getGoalProgressColor } from "@/features/User/utils/goalProgress";
import ReadingStatisticsSection from "@/pages/My/ReadingStatisticsSection";
import * as userListStyles from "@/features/Social/components/LikeUserListButton.css";
import { useEffect, useRef, useState, type ReactNode, type SyntheticEvent } from "react";
import { createPortal } from "react-dom";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import * as styles from "@/pages/My/ProfileEditPage.css";

type ReadingPeriod = "week" | "month" | "year";

type LoadedProfileImage = {
  userNumb: number;
  source: string;
};

type ImageReactionDetail = Pick<ImageReaction, "likeCnt" | "likeYsno">;

/**
 * 다른 사용자 프로필의 현재 사진 반응에 서버 또는 낙관적 좋아요 상태를 병합함
 *
 * @author SeungHyeon.Kang
 * @param current 현재 소셜 프로필 정보
 * @param reaction 좋아요를 변경한 사진 반응 정보
 * @param detail 적용할 좋아요 수와 여부
 * @return 지정한 현재 사진 반응만 변경된 소셜 프로필 정보
 */
const mergeImageReaction = (
  current: UserProfile | null,
  reaction: ImageReaction,
  detail: ImageReactionDetail,
): UserProfile | null => {
  const reactionKey = reaction.tagtType === "PROFILE_IMAGE"
    ? "profileImageReaction"
    : "backgroundImageReaction";
  const currentReaction = current?.[reactionKey];

  // 사진이 교체되었거나 프로필 조회 전이면 이전 사진 반응을 현재 화면에 반영하지 않음
  if (!current || !currentReaction || currentReaction.tagtNumb !== reaction.tagtNumb) {
    // 기존 소셜 프로필 상태를 그대로 반환함
    return current;
  }

  // 지정한 현재 사진의 좋아요 수와 로그인 사용자 상태만 병합해 반환함
  return {
    ...current,
    [reactionKey]: {
      ...currentReaction,
      ...detail,
    },
  };
};

/**
 * get Reading Remain Rate 정보를 조회함
 *
 * @author HanWon.Jang
 * @param remainDays remain Days 입력값
 * @return 처리 결과
 */
const getReadingRemainRate = (remainDays: number) => {
  // 현재 읽고 있는 책의 남은 기간 색상은 전체 목표기간 비율이 아니라 남은 10일을 기준으로 판단함
  // 10일 이상 남으면 가장 여유 있는 색상, 0일에 가까워질수록 기존 색상 단계가 내려감
  return Math.max(0, Math.min(100, Math.round((Math.max(remainDays, 0) / 10) * 100)));
};

/**
 * 다른 사용자의 프로필과 독서 활동 현황을 보여주는 읽기 전용 페이지임
 * 공개 독후감 목록에서 작성자 프로필을 눌렀을 때 진입하며, 마이페이지와 같은 활동 요약 데이터를 표시함
 *
 * @author SeungHyeon.Kang
 * @return 소셜 프로필 페이지 컴포넌트
 */
const SocialProfilePage = () => {

  const navigate = useNavigate();
  const { userNumb } = useParams();
  // 알림이 지정한 현재 사진과 댓글 위치를 프로필 화면에서 해석함
  const [searchParams] = useSearchParams();
  const targetUserNumb = Number(userNumb);
  const requestedTagtType = searchParams.get("tagtType");
  const requestedTagtNumb = Number(searchParams.get("tagtNumb"));
  const requestedReplNumb = Number(searchParams.get("replNumb"));
  const focusReplNumb = Number.isSafeInteger(requestedReplNumb) && requestedReplNumb > 0
    ? requestedReplNumb
    : undefined;
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [summary, setSummary] = useState<MonthlyReadingSummary | null>(null);
  const [followStatName, setFollowStatName] = useState("");
  const [isFollowUpdating, setIsFollowUpdating] = useState(false);
  const [isFollowListScrolling, setIsFollowListScrolling] = useState(false);
  const [followUpdatingUserNumb, setFollowUpdatingUserNumb] = useState<number | null>(null);
  const {
    followListType,
    followUsers,
    isFollowListLoading,
    isNextFollowLoading,
    hasNextFollowUser,
    openFollowList,
    loadMoreFollow,
    closeFollowList,
    setFollowUsers,
  } = useFollowListModal({ targetUserNumb, isMyProfile: false });
  const [expandedSummary, setExpandedSummary] = useState<Record<ReadingPeriod, boolean>>({
    week: false,
    month: false,
    year: false,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [loadedProfileImage, setLoadedProfileImage] = useState<LoadedProfileImage | null>(null);
  const [imageLikeUpdatingType, setImageLikeUpdatingType] = useState<ImageReaction["tagtType"] | null>(null);
  const [replyTarget, setReplyTarget] = useState<ImageReaction | null>(null);
  const followListScrollTimeoutRef = useRef<number | null>(null);
  const processedReplyRouteRef = useRef("");
  useBodyScrollLock(Boolean(followListType) || Boolean(replyTarget));

  useEffect(() => {

    let ignore = false;

    // 잘못된 사용자 번호는 API 호출 전에 차단해 불필요한 서버 요청을 만들지 않음
    if (!Number.isFinite(targetUserNumb) || targetUserNumb <= 0) {
      setIsLoading(false);
      return () => {

        ignore = true;
      };
    }

    // 후속 API 호출 전에 대상 회원의 최신 상태를 먼저 조회함
    getSocialProfileApi(targetUserNumb)
      .then(async (profileResponse) => {

        const nextProfile = profileResponse.data as UserProfile;

        // 정상 이용 상태가 아니면 상태를 안내하고 프로필 세부 조회를 중단함
        if (nextProfile.userStat && nextProfile.userStat !== "ACTIVE") {
          const userStatus = `${nextProfile.userStat}(${nextProfile.userStatName ?? "-"})`;
          // 접근할 수 없는 회원 상태를 안내함
          const alertResult = await sweetAlert({
            // "현재 접근할 수 없는 회원이에요."
            title: message("frontend.social.restrictedProfile.title"),
            // "회원 상태는 {0}입니다. 확인하면 이전 화면으로 이동해요."
            text: message("frontend.social.restrictedProfile.text", [userStatus]),
            icon: "warning",
            allowOutsideClick: false,
          });

          // 상태 안내를 확인하면 진입 전 화면으로 돌아감
          if (!ignore && alertResult.isConfirmed) {
            navigate(-1);
          }
          return;
        }

        // 정상 회원에게만 독서 활동과 팔로우 관계를 병렬로 조회함
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

  // 알림이 요청한 사진 유형에 대응하는 현재 소셜 프로필 반응을 선택함
  const requestedReaction = requestedTagtType === "PROFILE_IMAGE"
    ? profile?.profileImageReaction
    : requestedTagtType === "BACKGROUND_IMAGE"
      ? profile?.backgroundImageReaction
      : null;
  // 알림의 파일 번호가 현재 사진과 일치할 때만 자동 열기 대상으로 인정함
  const hasRequestedImage = Boolean(
    requestedReaction
      && Number.isSafeInteger(requestedTagtNumb)
      && requestedTagtNumb > 0
      && requestedReaction.tagtNumb === requestedTagtNumb,
  );
  const requestedRouteKey = `${requestedTagtType ?? ""}:${requestedTagtNumb}:${focusReplNumb ?? 0}`;

  // 사진 댓글 알림이면 현재 사진 검증이 끝난 뒤 댓글 바텀시트와 강조 위치를 한 번만 엶
  useEffect(() => {
    // 일반 프로필 진입 또는 사진 좋아요 알림은 전체 화면 사진만 열고 댓글 시트는 열지 않음
    if (!hasRequestedImage || !requestedReaction || !focusReplNumb) {
      // 댓글 자동 열기 상태를 변경하지 않고 종료함
      return;
    }

    // 같은 알림 경로를 이미 처리했으면 댓글 집계 갱신으로 시트를 다시 열지 않음
    if (processedReplyRouteRef.current === requestedRouteKey) {
      // 사용자가 닫은 댓글 시트 상태를 유지함
      return;
    }

    // 현재 알림 경로의 댓글 자동 열기를 처리했음을 기록함
    processedReplyRouteRef.current = requestedRouteKey;
    // 현재 사진 댓글 시트를 열고 알림이 지정한 댓글을 강조하도록 대상을 설정함
    setReplyTarget(requestedReaction);
  }, [focusReplNumb, hasRequestedImage, requestedReaction, requestedRouteKey]);

  /**
   * 화면에 표시할 원본 프로필 사진이 실제로 로드됐는지 기록함
   *
   * @author HanWon.Jang
   * @param event 프로필 이미지 로드 완료 이벤트
   * @return 반환값이 없음
   */
  const handleProfileImageLoad = (event: SyntheticEvent<HTMLImageElement>) => {

    // 현재 프로필 응답에서 기대한 이미지 경로를 브라우저 절대 경로로 변환함
    const expectedSource = normalizeProfileImageSource(profile?.porfPath);
    const expectedUrl = new URL(expectedSource, window.location.origin).href;
    // 기본 이미지이거나 로드 실패 뒤 대체된 이미지이면 신고 가능한 원본으로 기록하지 않음
    if (expectedSource === DEFAULT_PROFILE_IMAGE || event.currentTarget.currentSrc !== expectedUrl) {
      setLoadedProfileImage(null);
      return;
    }

    // 다른 사용자 또는 이전 경로의 로드 결과와 섞이지 않도록 대상과 원본 경로를 함께 기록함
    setLoadedProfileImage({
      userNumb: targetUserNumb,
      source: expectedSource,
    });
  };

  /**
   * 다른 사용자의 현재 프로필 또는 배경사진 좋아요를 낙관적으로 변경함
   *
   * @author SeungHyeon.Kang
   * @param reaction 좋아요를 변경할 현재 사진 반응 정보
   * @return 좋아요 변경 완료 Promise
   * @throws 사진 좋아요 API 요청 또는 업무 검증 실패 시 발생
   */
  const handleImageLike = async (reaction: ImageReaction): Promise<void> => {
    // 같은 사진의 좋아요 요청이 진행 중이면 첫 요청이 끝날 때까지 중복 토글을 막음
    if (imageLikeUpdatingType === reaction.tagtType) {
      // 진행 중인 첫 좋아요 요청 결과를 유지함
      return;
    }

    const optimisticDetail: ImageReactionDetail = {
      likeCnt: Math.max(0, reaction.likeCnt + (reaction.likeYsno === "Y" ? -1 : 1)),
      likeYsno: reaction.likeYsno === "Y" ? "N" : "Y",
    };

    // 현재 사진의 중복 요청을 막고 서버 응답 전에 변경 상태를 표시함
    setImageLikeUpdatingType(reaction.tagtType);
    // 현재 사진의 좋아요 수와 로그인 사용자 상태를 낙관적으로 갱신함
    setProfile((current) => mergeImageReaction(current, reaction, optimisticDetail));

    // 서버가 현재 사진과 활성 사용자를 다시 검증한 좋아요 결과를 처리함
    try {
      // 범용 좋아요 API에 현재 사진 유형과 파일 번호를 전달함
      const result = await setPublicReportLikeApi({
        tagtType: reaction.tagtType,
        tagtNumb: reaction.tagtNumb,
      });
      const detail = result.data;

      // 서버가 최종 좋아요 상태를 반환하면 낙관적 상태를 확정값으로 보정함
      if (detail) {
        // 현재 사진의 좋아요 수와 로그인 사용자 상태를 서버 결과로 갱신함
        setProfile((current) => mergeImageReaction(current, reaction, detail));
      }
    }

    // 좋아요 저장 실패 시 클릭 전 상태를 복원하고 공통 오류를 표시함
    catch (error) {
      const originalDetail: ImageReactionDetail = {
        likeCnt: reaction.likeCnt,
        likeYsno: reaction.likeYsno,
      };
      // 핵심 저장 실패 시 현재 사진 반응을 클릭 전 상태로 복원함
      setProfile((current) => mergeImageReaction(current, reaction, originalDetail));
      // "좋아요 처리에 실패했어요."
      await sweetError(
        message("frontend.feed.likeFailed"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 성공과 실패 모두에서 현재 사진 좋아요 버튼을 다시 활성화함
      setImageLikeUpdatingType(null);
    }
  };

  /**
   * 다른 사용자 사진 댓글 바텀시트를 닫고 최신 사진 반응 집계를 다시 조회함
   *
   * @author SeungHyeon.Kang
   * @return 댓글 집계 갱신 완료 Promise
   * @throws 공개 프로필 재조회 실패 시 발생
   */
  const handleImageReplyClose = async (): Promise<void> => {
    // 댓글 바텀시트를 먼저 닫아 소셜 프로필 조작을 복구함
    setReplyTarget(null);

    // 댓글 등록과 삭제 결과가 반영된 현재 사진 집계를 다시 조회함
    try {
      // 다른 사용자의 최신 공개 프로필과 사진 반응을 조회함
      const nextProfile = (await getSocialProfileApi(targetUserNumb)).data as UserProfile;
      // 소셜 프로필의 사진 반응과 기본 정보를 최신 서버 응답으로 교체함
      setProfile(nextProfile);
    }

    // 댓글 창은 닫은 상태로 유지하고 프로필 재조회 실패만 안내함
    catch (error) {
      // "조회에 실패했습니다."
      await sweetError(
        message("frontend.alert.loadFailedTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    }
  };

  /**
   * 소셜 프로필의 전체 화면 사진에 표시할 좋아요와 댓글 버튼을 구성함
   *
   * @author SeungHyeon.Kang
   * @param reaction 현재 사진의 좋아요와 댓글 집계
   * @param className 사진 위치에 맞는 반응 버튼 묶음 스타일
   * @return 좋아요와 댓글 버튼 묶음
   */
  const renderImageReactions = (reaction: ImageReaction, className: string): ReactNode => (
    <div className={className}>
      {/* 현재 사진 좋아요 변경과 좋아요 사용자 목록 영역 */}
      <div className={styles.likeMetricGroup}>
        <button
          className={styles.likeIconButton}
          type="button"
          aria-label={/* "좋아요" */ message("frontend.feed.likeAction")}
          aria-pressed={reaction.likeYsno === "Y"}
          disabled={imageLikeUpdatingType === reaction.tagtType}
          onClick={() => void handleImageLike(reaction)}
        >
          <img
            className={styles.metricIcon}
            src={reaction.likeYsno === "Y"
              ? "/img/icons/icon-heart-fill.svg"
              : "/img/icons/icon-heart.svg"}
            alt=""
          />
        </button>
        <LikeUserListButton
          className={styles.likeCountButton}
          tagtType={reaction.tagtType}
          tagtNumb={reaction.tagtNumb}
          countLabel={reaction.likeCnt}
        />
      </div>
      <button
        className={styles.commentButton}
        type="button"
        aria-label={/* "댓글 보기" */ message("frontend.book.publicReports.viewComments")}
        onClick={() => setReplyTarget(reaction)}
      >
        <img
          className={styles.metricIcon}
          src="/img/icons/icon-comment.svg"
          alt=""
        />
        {reaction.replCnt}
      </button>
    </div>
  );

  /**
   * 프로필 팔로우 버튼의 현재 관계에 맞춰 팔로우 또는 언팔로우 API를 호출함
   * 팔로잉과 친구는 내가 상대를 팔로우 중인 상태이므로 삭제하고, 그 외에는 팔로우 관계를 저장함
   *
   * @author HanWon.Jang
   * @return 팔로우 관계와 프로필 통계 갱신이 끝난 Promise
   * @throws 팔로우 또는 프로필 통계 요청 실패 시 발생
   */
  const handleFollowButtonClick = async () => {

    // 중복 클릭 중에는 현재 팔로우 요청이 끝날 때까지 추가 조작을 차단함
    if (isFollowUpdating) {
      // 진행 중인 팔로우 요청을 유지하고 추가 요청 없이 종료함
      return;
    }

    // 현재 버튼명으로 로그인 사용자가 만든 팔로우 관계의 존재 여부를 판정함
    const isFollowing = isFollowedByMe(followStatName);

    // 팔로잉 또는 친구 상태를 해제하기 전에 사용자 확인을 받음
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

      // 사용자가 취소하면 기존 관계를 유지함
      if (!result.isConfirmed) {
        // 팔로우 관계 변경 없이 종료함
        return;
      }
    }

    // 팔로우 관계 변경이 끝날 때까지 버튼을 비활성화함
    setIsFollowUpdating(true);

    // 현재 관계에 맞는 팔로우 등록 또는 삭제 요청을 실행함
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
   * handle Follow List Open 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @param type type 입력값
   * @return 반환값이 없음
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleFollowListOpen = async (type: FollowListType) => {
    // 새 모달을 열 때 스크롤 상태를 초기화함
    setIsFollowListScrolling(false);
    // 다른 사용자의 팔로우 목록 모달을 열고 첫 서버 페이지를 조회함
    await openFollowList(type);
  };

  /**
   * handle Follow List Close 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleFollowListClose = () => {
    // 공통 팔로우 목록과 페이지 상태를 초기화함
    closeFollowList();
    setIsFollowListScrolling(false);
  };

  /**
   * handle Follow List Scroll 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
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
   * handle Follow List User Click 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @param nextUserNumb next User Numb 입력값
   * @return 반환값이 없음
   */
  const handleFollowListUserClick = (nextUserNumb: number) => {

    handleFollowListClose();
    navigate(`/social/profile/${nextUserNumb}`);
  };

  /**
   * 팔로우 목록 사용자의 현재 관계에 맞춰 팔로우 또는 언팔로우 API를 호출함
   *
   * @author HanWon.Jang
   * @param user 관계를 변경할 팔로우 목록 사용자
   * @return 팔로우 관계와 프로필 통계 갱신이 끝난 Promise
   * @throws 팔로우 또는 프로필 통계 요청 실패 시 발생
   */
  const handleFollowStatusClick = async (user: FollowUser) => {

    // 다른 관계 변경이 진행 중이거나 내 계정 행이면 추가 조작을 허용하지 않음
    if (followUpdatingUserNumb || user.meYsno === "Y") {
      // 현재 목록 상태를 유지하고 종료함
      return;
    }

    // 목록 사용자를 내가 팔로우 중인지 버튼명으로 판정함
    const isFollowing = isFollowedByMe(user.followStatName);

    // 팔로잉 또는 친구 상태를 해제하기 전에 사용자 확인을 받음
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

      // 사용자가 취소하면 기존 관계를 유지함
      if (!result.isConfirmed) {
        // 팔로우 관계 변경 없이 종료함
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
   * 주간, 월간, 연간 요약 리스트의 펼침 상태를 전환함
   * 읽은 책이 있는 영역에만 호출되어 빈 목록에 대한 불필요한 상태 변경을 막음
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
   * 다른 사용자의 요약 독후감 항목을 선택했을 때 공개 여부에 따라 이동 또는 경고를 처리함
   * 공개 독후감은 ISBN 기준 공개 독후감 목록으로 이동하고, 비공개 독후감은 사용자가 내용을 볼 수 없음을 안내함
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
   * render Profile Stats 화면 요소를 구성함
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
        <div className={styles.myProfileStatsSummary}>
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
   * 다른 사용자가 현재 읽고 있는 책의 목표 종료일까지 남은 기간 정보를 렌더링함
   * 남은 기간이 적을수록 붉은 계열로 표시해 목표 종료일이 가까움을 보여줌
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

                        // 다른 사용자 도서는 특정 독후감의 공개 여부와 무관하게 ISBN 기반 도서 정보로 이동함
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
   * 기간별 독서 활동 행과 펼침 목록을 공통 구조로 렌더링함
   * 목표 달성률, 목표 권수, 실제 완료 권수, 완료 독후감 목록을 같은 배치로 보여줌
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
        {summary?.goalPublicYsno !== "N" && <div className={styles.goalProgressRow}>
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
        </div>}
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

                        // 공개 여부와 관계없이 제목에서는 ISBN 기준 도서 정보 화면으로 이동함
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

  // 탈퇴 회원은 기존 관계를 유지하되 프로필과 활동 정보를 공개하지 않음
  if (profile.userStat && profile.userStat !== "ACTIVE") {
    // 탈퇴 상태만 표시하는 제한된 공개 프로필 화면을 반환함
    return (
      <main className={styles.page}>
        {/* 탈퇴 회원 공개 프로필 제한 안내 영역 */}
        <section className={styles.profileShell}>
          <section className={styles.socialProfileBody}>
            <div className={styles.profileText}>
              <h1 className={styles.profileName}>
                {/* "탈퇴한 사용자" */}
                {message("frontend.social.withdrawnUser.title")}
              </h1>
              <p className={styles.profileIntro}>
                {/* "탈퇴한 사용자의 정보는 표시되지 않아요." */}
                {message("frontend.social.withdrawnUser.description")}
              </p>
            </div>
          </section>
        </section>
      </main>
    );
  }

  // "-"
  const emptyValue = message("frontend.common.emptyValue");
  const targetUserNick = profile.userNick || emptyValue;
  // 한줄소개 앞뒤 공백을 제거하여 실제 신고 선택지 표시 여부를 판단함
  const profileIntroduction = profile.intrCntn?.trim();
  // 사용자 계정 신고는 세부 신고 대상과 겹치지 않도록 현재 닉네임만 표시함
  const userProfileContent = targetUserNick;
  // 공통 기본 이미지와 실제 원본 프로필 사진의 경로를 같은 기준으로 비교함
  const profileImageSource = normalizeProfileImageSource(profile.porfPath);
  // 현재 대상의 비기본 원본 이미지가 정상 로드된 경우에만 프로필 사진 신고를 허용함
  const hasReportableProfileImage = profileImageSource !== DEFAULT_PROFILE_IMAGE
    && loadedProfileImage?.userNumb === targetUserNumb
    && loadedProfileImage.source === profileImageSource;
  // 다른 활성 사용자의 프로필에서 신고할 수 있는 현재 사용자 계정 대상을 구성함
  const userProfileTarget = {
    targetType: "USER" as const,
    targetNumb: targetUserNumb,
    userNumb: targetUserNumb,
    userNick: targetUserNick,
    content: userProfileContent,
  };
  // 현재 화면에 실제로 표시된 이미지와 한줄소개만 세부 신고 선택지에 포함함
  const profileReportOptions: SafetyReportOption[] = [
    {
      // "사용자 계정 신고"
      label: message("frontend.social.report.user"),
      target: userProfileTarget,
    },
  ];

  // 실제 프로필 사진이 있는 사용자만 접수 시점 이미지 증거를 신고할 수 있게 함
  if (hasReportableProfileImage) {
    // "프로필 사진 신고"
    const profileImageReportLabel = message("frontend.social.report.profileImage");
    // "프로필 사진"
    const profileImageTargetContent = message("frontend.userReport.target.profileImage");
    // 프로필 이미지 신고 대상을 선택 메뉴에 추가함
    profileReportOptions.push({
      label: profileImageReportLabel,
      target: {
        targetType: "PROFILE" as const,
        targetNumb: targetUserNumb,
        userNumb: targetUserNumb,
        userNick: targetUserNick,
        content: profileImageTargetContent,
      },
    });
  }

  // 실제 배경사진이 있는 사용자만 접수 시점 이미지 증거를 신고할 수 있게 함
  if (profile.bgimPath) {
    // "배경사진 신고"
    const backgroundImageReportLabel = message("frontend.social.report.backgroundImage");
    // "배경사진"
    const backgroundImageTargetContent = message("frontend.userReport.target.backgroundImage");
    // 배경 이미지 신고 대상을 선택 메뉴에 추가함
    profileReportOptions.push({
      label: backgroundImageReportLabel,
      target: {
        targetType: "BACKGROUND" as const,
        targetNumb: targetUserNumb,
        userNumb: targetUserNumb,
        userNick: targetUserNick,
        content: backgroundImageTargetContent,
      },
    });
  }

  // 공백이 아닌 한줄소개가 있는 사용자만 한줄소개를 별도 신고할 수 있게 함
  if (profileIntroduction) {
    // "한줄소개 신고"
    const introductionReportLabel = message("frontend.social.report.introduction");
    // 한줄소개 신고 대상을 선택 메뉴에 추가함
    profileReportOptions.push({
      label: introductionReportLabel,
      target: {
        targetType: "INTRO" as const,
        targetNumb: targetUserNumb,
        userNumb: targetUserNumb,
        userNick: targetUserNick,
        content: profileIntroduction,
      },
    });
  }

  return (
    /* 상대 사용자의 프로필과 독서 활동 전체 영역 */
    <main className={styles.page}>
      {/* 상대 사용자의 프로필 배경과 기본 정보 영역 */}
      <section className={styles.profileShell}>
        <div className={styles.cover}>
          {profile.bgimPath && (
            <BackgroundImage
              source={profile.bgimDisplayPath || profile.bgimPath}
              imageClassName={styles.coverImage}
              alt=""
            />
          )}
          {profile.bgimPath && (
            <FullscreenImageButton
              key={hasRequestedImage && requestedTagtType === "BACKGROUND_IMAGE"
                ? `notification-${requestedRouteKey}`
                : "social-background-image"}
              className={styles.coverImageViewerButton}
              source={normalizeProfileImageSource(profile.bgimPath)}
              alt={/* "배경사진" */ message("frontend.imageViewer.backgroundAlt")}
              initiallyOpen={hasRequestedImage && requestedTagtType === "BACKGROUND_IMAGE"}
              actions={profile.backgroundImageReaction
                ? renderImageReactions(
                  profile.backgroundImageReaction,
                  styles.viewerImageReactionBar,
                )
                : undefined}
            >
              <span aria-hidden="true" />
            </FullscreenImageButton>
          )}
          {/* 마이페이지 프로필 수정 버튼과 같은 우하단 위치의 상대 사용자 신고·차단 메뉴 */}
          <div className={styles.coverActionGroup}>
            <UserActionMenu
              userNick={targetUserNick}
              reportTarget={userProfileTarget}
              reportOptions={profileReportOptions}
              triggerClassName={styles.socialProfileMoreButton}
              triggerIconClassName={styles.socialProfileMoreIcon}
              menuClassName={styles.socialProfileMoreMenu}
            />
          </div>
        </div>

        {/* 상대 사용자 정보와 팔로우 상태 영역 */}
        <section className={styles.socialProfileBody}>
          <div className={styles.socialProfileHeaderRow}>
            <div className={styles.avatarWrap}>
              <FullscreenImageButton
                key={hasRequestedImage && requestedTagtType === "PROFILE_IMAGE"
                  ? `notification-${requestedRouteKey}`
                  : "social-profile-image"}
                className={styles.profileImageViewerButton}
                source={normalizeProfileImageSource(profile.porfPath)}
                fallbackSource={DEFAULT_PROFILE_IMAGE}
                alt={/* "프로필 사진" */ message("frontend.imageViewer.profileAlt")}
                initiallyOpen={hasRequestedImage && requestedTagtType === "PROFILE_IMAGE"}
                actions={profile.profileImageReaction
                  ? renderImageReactions(
                    profile.profileImageReaction,
                    styles.viewerImageReactionBar,
                  )
                  : undefined}
              >
                <ProfileImage
                  className={styles.profileImage}
                  src={profile.porfPath}
                  alt={profile.userNick ?? message("frontend.profile.nick")}
                  onLoad={handleProfileImageLoad}
                />
              </FullscreenImageButton>
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
            {summary.goalPublicYsno !== "N" && <div className={styles.goalAchievementSummary}>
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
            </div>}
            {summary.goalPublicYsno !== "N" && <div className={styles.readingSummaryDivider} />}
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

      {/* 현재 소셜 프로필 사진 또는 배경사진의 범용 댓글 바텀시트 영역 */}
      {replyTarget ? (
        <ReplySheet
          report={{ reptNumb: replyTarget.tagtNumb, userNick: profile.userNick }}
          tagtType={replyTarget.tagtType}
          focusReplNumb={focusReplNumb}
          onClose={() => void handleImageReplyClose()}
        />
      ) : null}

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
                className={modalControlStyles.roundClose}
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
                <Loading isFullScreen={false} />
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
              {!isFollowListLoading && followUsers.map((user) => (
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
                      className={userListStyles.statusButton}
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
                isLoading={isNextFollowLoading}
                onLoadMore={() => {
                  // 목록 하단에 도달하면 다음 팔로우 사용자 서버 페이지를 조회함
                  void loadMoreFollow();
                }}
              />
            </div>
          </section>
        </div>
      ), document.body)}
    </main>
  );
};

export default SocialProfilePage;
