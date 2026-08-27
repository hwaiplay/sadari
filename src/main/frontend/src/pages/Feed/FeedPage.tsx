import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import BackgroundImage from "@/components/BackgroundImage/BackgroundImage";
import { FullscreenImageButton } from "@/components/ImageViewer/FullscreenImageViewer";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import AnimatedReportContent from "@/components/ReportList/AnimatedReportContent";
import * as reportListStyles from "@/components/ReportList/ReportListView.css";
import { setPublicReportLikeApi } from "@/features/Book/api/bookApi";
import {
  REPORT_STATUS_DONE,
  REPORT_STATUS_STOP,
} from "@/features/Book/constants/reportForm";
import { REPORT_CONTENT_PREVIEW_LENGTH } from "@/features/Book/utils/reportListView";
import { getFeedPageApi, getFeedTargetApi, type FeedItem } from "@/features/Feed/api/feedApi";
import type { ReplyTargetType } from "@/features/reply/types/reply.types";
import ReplySheet from "@/features/reply/ReplySheet";
import LikeUserListButton from "@/features/Social/components/LikeUserListButton";
import ProfileImage, { DEFAULT_PROFILE_IMAGE } from "@/features/User/components/ProfileImage";
import { type ReactNode, type SyntheticEvent, useCallback, useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import * as styles from "./FeedPage.css";

/**
 * 독서 상태 코드에 대응하는 공통 독후감 카드의 배지 스타일을 반환한다.
 *
 * @author HanWon.Jang
 * @param reptStat 피드 독후감의 독서 상태 코드
 * @return 독서 상태별 배지 클래스명
 */
const getStatusClassName = (reptStat?: FeedItem["reptStat"]): string => {
  // 완독한 독후감은 브랜드 색상의 완료 배지를 사용한다
  if (reptStat === REPORT_STATUS_DONE) {
    // 다른 사람 독후감 카드와 같은 완독 배지 클래스를 반환한다
    return reportListStyles.statusDone;
  }

  // 독서를 중단한 독후감은 회색의 중단 배지를 사용한다
  if (reptStat === REPORT_STATUS_STOP) {
    // 다른 사람 독후감 카드와 같은 중단 배지 클래스를 반환한다
    return reportListStyles.statusStopped;
  }

  // 나머지 상태에는 다른 사람 독후감 카드와 같은 독서 중 배지를 반환한다
  return reportListStyles.statusReading;
};

type FeedLikeDetail = Pick<FeedItem, "likeCnt" | "likeYsno">;

// 알림 링크로 직접 조회할 수 있는 피드 대상 유형
const FEED_TARGET_TYPES: ReplyTargetType[] = ["REPORT", "PROFILE_IMAGE", "BACKGROUND_IMAGE"];

/**
 * 피드 대상 식별값이 현재 갱신 대상과 일치하는지 판정한다
 *
 * @author HanWon.Jang
 * @param candidate 비교할 피드 항목
 * @param target 갱신 대상 피드 항목
 * @return 피드 유형과 대상 번호가 모두 같으면 true
 */
const isSameFeedTarget = (candidate: FeedItem, target: FeedItem): boolean => {
  // 유형과 번호가 모두 일치해야 같은 좋아요 대상으로 판정한다
  return candidate.tagtType === target.tagtType && candidate.tagtNumb === target.tagtNumb;
};

/**
 * 지정한 피드 대상의 좋아요 수와 로그인 사용자 좋아요 여부를 불변 배열로 갱신한다
 *
 * @author HanWon.Jang
 * @param current 현재 화면에 누적된 피드 목록
 * @param target 좋아요 상태를 갱신할 피드 항목
 * @param detail 화면에 반영할 좋아요 수와 여부
 * @return 지정한 대상만 좋아요 상태가 변경된 새 피드 목록
 */
const getUpdatedLikeItems = (
  current: FeedItem[],
  target: FeedItem,
  detail: FeedLikeDetail,
): FeedItem[] => {
  /**
   * 현재 순회 항목이 좋아요 대상이면 전달받은 서버 또는 낙관적 상태를 병합한다
   *
   * @author HanWon.Jang
   * @param candidate 현재 순회 중인 피드 항목
   * @return 대상 항목이면 좋아요 상태가 갱신된 항목이며 아니면 기존 항목
   */
  const updateCandidate = (candidate: FeedItem): FeedItem => {
    // 다른 피드 대상은 기존 상태를 그대로 유지한다
    if (!isSameFeedTarget(candidate, target)) {
      // 현재 대상과 일치하지 않는 피드 항목을 반환한다
      return candidate;
    }

    // 현재 대상에는 검증된 좋아요 상태를 병합해 새 객체로 반환한다
    return { ...candidate, ...detail };
  };

  // React 상태 불변성을 유지하도록 대상 항목만 새 객체로 치환한다
  return current.map(updateCandidate);
};

/**
 * 최초 피드 페이지는 목록을 교체하고 추가 페이지는 기존 목록 뒤에 연결한다
 *
 * @author HanWon.Jang
 * @param current 현재 화면에 누적된 피드 목록
 * @param incoming 서버가 반환한 피드 페이지 목록
 * @param targetPage 조회한 페이지 번호
 * @return 페이지 위치에 맞게 구성한 새 피드 목록
 */
const getMergedFeedItems = (
  current: FeedItem[],
  incoming: FeedItem[],
  targetPage: number,
): FeedItem[] => {
  // 최초 페이지는 이전 조회 결과를 남기지 않고 서버 목록으로 교체한다
  if (targetPage === 1) {
    // 첫 페이지에서 받은 피드 목록을 반환한다
    return incoming;
  }

  // 추가 페이지는 기존 목록과 서버 목록을 새 배열로 연결해 반환한다
  return [...current, ...incoming];
};

/**
 * 지정한 독후감 피드의 본문 펼침 상태를 불변 객체로 반전한다
 *
 * @author HanWon.Jang
 * @param current 피드 대상별 현재 펼침 상태
 * @param tagtNumb 펼침 상태를 변경할 피드 대상 번호
 * @return 지정한 피드의 펼침 상태만 반전된 새 객체
 */
const getToggledReports = (
  current: Record<number, boolean>,
  tagtNumb: number,
): Record<number, boolean> => {
  // 다른 피드의 펼침 상태는 유지하고 선택한 대상만 반전해 반환한다
  return {
    ...current,
    [tagtNumb]: !current[tagtNumb],
  };
};

/**
 * 독후감 본문 펼침 상태에 맞는 접근성 동작 문구를 반환한다
 *
 * @author HanWon.Jang
 * @param isExpanded 독후감 본문 펼침 여부
 * @return 현재 상태에서 실행할 접기 또는 펼치기 문구
 */
const getExpandActionLabel = (isExpanded: boolean): string => {
  // 펼쳐진 본문에는 내용을 접는 동작 문구를 제공한다
  if (isExpanded) {
    // "접기"
    return message("frontend.common.collapse");
  }

  // "펼치기"
  return message("frontend.book.publicReports.expand");
};

/**
 * 로그인 사용자 본인과 팔로잉 사용자의 공개 독후감 및 사진 변경 활동을 카드 목록으로 표시한다
 *
 * @author HanWon.Jang
 * @return 본인과 팔로잉 사용자의 활동 피드 화면
 */
const FeedPage = () => {
  // 피드 카드의 프로필 및 도서 화면 이동에 공통 라우터 함수를 사용한다
  const navigate = useNavigate();
  // 알림 링크에 포함된 원본 콘텐츠 유형과 번호를 조회한다
  const [searchParams] = useSearchParams();
  // 서버에서 페이지 단위로 받은 피드 항목을 화면 목록 상태로 관리한다
  const [items, setItems] = useState<FeedItem[]>([]);
  // 알림 링크로 직접 연 피드 항목을 일반 페이지 목록과 독립적으로 관리한다
  const [focusedItem, setFocusedItem] = useState<FeedItem | null>(null);
  // 마지막으로 조회에 성공한 피드 페이지 번호를 관리한다
  const [page, setPage] = useState(1);
  // 목록 하단에서 다음 피드 페이지를 조회할 수 있는지 관리한다
  const [hasNext, setHasNext] = useState(false);
  // 최초 조회와 추가 조회에서 중복 요청을 차단할 로딩 상태를 관리한다
  const [isLoading, setIsLoading] = useState(true);
  // 최초 피드 조회 실패 시 화면에 표시할 안전한 오류 문구를 관리한다
  const [error, setError] = useState("");
  // 댓글 목록을 열어 확인할 현재 피드 항목을 관리한다
  const [replyItem, setReplyItem] = useState<FeedItem | null>(null);
  // 독후감 피드별 본문 펼침 여부를 대상 번호 기준으로 관리한다
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>({});
  // 같은 피드 대상의 좋아요 요청이 동시에 실행되지 않도록 진행 키를 보관한다
  const pendingLikeKeysRef = useRef(new Set<string>());
  // 알림 링크가 전달한 피드 대상 유형 문자열을 조회한다
  const targetTypeParam = searchParams.get("tagtType");
  // 알림 링크가 전달한 피드 대상 번호를 숫자로 변환한다
  const targetNumbParam = Number(searchParams.get("tagtNumb"));
  // 알림이 지정한 댓글 번호를 안전한 양수 정수로 변환한다
  const requestedReplyNumb = Number(searchParams.get("replNumb"));
  const focusReplNumb = Number.isSafeInteger(requestedReplyNumb) && requestedReplyNumb > 0
    ? requestedReplyNumb
    : undefined;
  // 허용된 유형과 양의 번호가 모두 있으면 단건 피드 조회 대상으로 판정한다
  const hasFeedTarget = FEED_TARGET_TYPES.includes(targetTypeParam as ReplyTargetType)
    && Number.isSafeInteger(targetNumbParam)
    && targetNumbParam > 0;

  /**
   * 요청한 피드 페이지를 조회해 최초 목록 또는 추가 목록으로 화면에 반영한다
   *
   * @author HanWon.Jang
   * @param targetPage 조회할 피드 페이지 번호
   * @return 피드 페이지 반영 완료 Promise
   */
  const loadPage = useCallback(async (targetPage: number): Promise<void> => {
    // 피드 조회 중 중복 추가 요청을 차단하고 공통 로딩 상태를 표시한다
    setIsLoading(true);
    // 새 조회가 시작되면 이전 오류 문구를 제거한다
    setError("");

    // 피드 조회 성공과 실패를 화면 상태별로 분리해 처리한다
    try {
      // 인증 사용자 기준으로 공개 범위가 적용된 피드 페이지를 조회한다
      const data = await getFeedPageApi(targetPage);

      /**
       * 서버 페이지를 최초 또는 추가 조회 위치에 맞춰 현재 피드 목록과 병합한다
       *
       * @author HanWon.Jang
       * @param current 현재 화면에 누적된 피드 목록
       * @return 조회한 페이지가 반영된 새 피드 목록
       */
      const mergeCurrentItems = (current: FeedItem[]): FeedItem[] => {
        // 페이지 위치에 맞게 기존 목록과 서버 목록을 결합해 반환한다
        return getMergedFeedItems(current, data.list, targetPage);
      };

      // 첫 페이지는 교체하고 추가 페이지는 기존 목록 뒤에 연결한다
      setItems(mergeCurrentItems);
      // 마지막으로 조회에 성공한 서버 페이지 번호를 저장한다
      setPage(data.page);
      // 서버가 판정한 다음 페이지 존재 여부를 저장한다
      setHasNext(data.hasNext);
    }

    // 피드 조회 실패 시 기존 목록을 유지하고 안전한 화면 문구를 설정한다
    catch (loadError) {
      // "피드를 불러오지 못했어요."
      const fallbackMessage = message("frontend.feed.loadFailed");
      // 원시 오류 대신 서버 또는 공통 피드 조회 실패 문구를 선택한다
      const errorMessage = getApiErrorMessage(loadError, fallbackMessage);
      // 최초 조회 실패 화면에 검증된 오류 문구를 표시한다
      setError(errorMessage);
    }

    // 성공과 실패 모두 현재 피드 조회의 로딩 상태를 종료한다
    finally {
      // 다음 피드 페이지를 조회할 수 있도록 로딩 상태를 해제한다
      setIsLoading(false);
    }
  }, []);

  /**
   * 피드 화면 진입 시 중복 초기화 없이 첫 페이지 조회를 시작한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const loadInitialPage = useCallback((): void => {
    // 화면 최초 렌더링에서 첫 피드 페이지를 비동기로 조회한다
    void loadPage(1);
  }, [loadPage]);

  // 화면 진입과 피드 조회 함수 변경 시 첫 페이지를 다시 조회한다
  useEffect(loadInitialPage, [loadInitialPage]);

  /**
   * 알림 링크가 지정한 피드 한 건을 조회하고 해당 댓글 목록을 자동으로 연다
   *
   * @author SeungHyeon.Kang
   * @return 알림 대상 피드 조회 완료 Promise
   */
  const loadTargetFeed = useCallback(async (): Promise<void> => {
    // 유효한 대상 식별값이 없는 일반 피드 진입은 단건 조회를 실행하지 않는다
    if (!hasFeedTarget) {
      // 이전 알림 대상 강조 상태를 제거하고 일반 피드 목록만 유지한다
      setFocusedItem(null);
      // 알림 대상 쿼리가 제거되면 자동으로 열었던 댓글 목록도 닫는다
      setReplyItem(null);
      return;
    }

    // 허용 유형 검사로 검증된 문자열을 댓글 대상 유형으로 사용한다
    const targetType = targetTypeParam as ReplyTargetType;

    // 대상 조회 성공과 만료 또는 접근 제한 실패를 분리해 처리한다
    try {
      // 팔로우 여부와 무관하게 현재 공개 상태를 검증한 알림 이동 대상 피드 한 건을 조회한다
      const targetItem = await getFeedTargetApi(targetType, targetNumbParam);
      // 페이지 위치와 무관하게 알림 대상 카드를 목록 맨 앞에서 확인할 수 있도록 저장한다
      setFocusedItem(targetItem);
      // 알림을 누른 사용자가 즉시 댓글을 확인할 수 있도록 대상 댓글 목록을 연다
      setReplyItem(targetItem);
    }

    // 교체되거나 공개 범위에서 제외된 대상이면 일반 피드는 유지하고 안전한 안내만 표시한다
    catch (targetError) {
      // 만료된 대상 카드가 화면에 남지 않도록 직접 조회 상태를 초기화한다
      setFocusedItem(null);
      // "이 알림의 피드를 열 수 없어요."
      const targetUnavailableTitle = message("frontend.feed.targetUnavailable");
      // "삭제되었거나 더 이상 볼 수 없는 소식이에요."
      const targetUnavailableMessage = message("frontend.feed.targetUnavailableDetail");
      // 원시 오류 대신 서버 또는 대상 만료 안내 문구를 선택한다
      const targetErrorMessage = getApiErrorMessage(targetError, targetUnavailableMessage);
      // 사용자가 일반 피드 화면에 머문 상태에서 대상 조회 실패 원인을 안내한다
      await sweetError(targetUnavailableTitle, targetErrorMessage);
    }
  }, [hasFeedTarget, targetNumbParam, targetTypeParam]);

  /**
   * 알림 대상 식별값이 변경될 때 단건 피드 조회를 시작한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const startTargetFeedLoad = useCallback((): void => {
    // 유효한 알림 대상이면 비동기 단건 조회를 시작한다
    void loadTargetFeed();
  }, [loadTargetFeed]);

  // 일반 진입 또는 알림 대상 변경에 맞춰 직접 조회 상태와 댓글 목록을 갱신한다
  useEffect(startTargetFeedLoad, [startTargetFeedLoad]);

  /**
   * 도서 표지 이미지 요청이 실패하면 공통 대체 이미지를 한 번만 적용한다
   *
   * @author HanWon.Jang
   * @param event 로드에 실패한 이미지 이벤트
   * @return 반환값이 없다
   */
  const handleImageError = (event: SyntheticEvent<HTMLImageElement>): void => {
    // 오류가 발생한 실제 이미지 요소를 대체 경로 적용 대상으로 사용한다
    const failedImage = event.currentTarget;
    // 도서 표지 오류 시 프로젝트 공통 대체 이미지를 사용한다
    const fallbackImage = "/img/common/no-image.png";

    // 공통 대체 이미지까지 실패한 경우 같은 경로를 반복 요청하지 않는다
    if (failedImage.getAttribute("src") === fallbackImage) {
      // 현재 대체 이미지 상태를 유지한다
      return;
    }

    // 도서 표지와 배경사진을 공통 대체 이미지로 한 번만 교체한다
    failedImage.src = fallbackImage;
  };

  /**
   * 피드 카드의 좋아요 상태를 즉시 반영하고 서버가 반환한 최종 값으로 확정한다
   *
   * @author HanWon.Jang
   * @param item 좋아요 상태를 변경할 피드 항목
   * @return 좋아요 상태 확정 완료 Promise
   */
  const handleLike = async (item: FeedItem): Promise<void> => {
    // 피드 유형과 대상 번호를 결합해 대상별 중복 요청 차단 키를 생성한다
    const pendingKey = `${item.tagtType}:${item.tagtNumb}`;

    // 같은 피드 대상의 좋아요 요청이 진행 중이면 중복 토글을 차단한다
    if (pendingLikeKeysRef.current.has(pendingKey)) {
      // 진행 중인 좋아요 요청을 유지하고 추가 입력을 무시한다
      return;
    }

    // 서버 응답 전에 반전된 좋아요 상태를 화면에 즉시 표시한다
    const optimisticDetail = {
      likeCnt: Math.max(0, item.likeCnt + (item.likeYsno === "Y" ? -1 : 1)),
      likeYsno: item.likeYsno === "Y" ? "N" as const : "Y" as const,
    };
    // 같은 대상의 추가 좋아요 입력을 차단하도록 진행 키를 등록한다
    pendingLikeKeysRef.current.add(pendingKey);

    /**
     * 서버 응답 전에 현재 피드 목록에 낙관적 좋아요 상태를 반영한다
     *
     * @author HanWon.Jang
     * @param current 현재 화면에 누적된 피드 목록
     * @return 선택한 대상의 좋아요 상태가 반전된 새 피드 목록
     */
    const applyOptimisticLike = (current: FeedItem[]): FeedItem[] => {
      // 선택한 대상에 계산된 낙관적 좋아요 상태를 반영해 반환한다
      return getUpdatedLikeItems(current, item, optimisticDetail);
    };

    // 서버 응답을 기다리는 동안 사용자가 누른 좋아요 상태를 즉시 표시한다
    setItems(applyOptimisticLike);
    // 알림으로 직접 연 카드가 같은 대상이면 낙관적 좋아요 상태를 함께 반영한다
    setFocusedItem((current) => current && isSameFeedTarget(current, item)
      ? { ...current, ...optimisticDetail }
      : current);

    // 좋아요 저장 성공과 실패를 낙관적 화면 상태에 맞춰 분리해 처리한다
    try {
      // 피드 대상 유형과 번호를 서버에 전달해 좋아요 최종 상태를 확정한다
      const result = await setPublicReportLikeApi({ tagtType: item.tagtType, tagtNumb: item.tagtNumb });
      // 서버가 반환한 좋아요 수와 로그인 사용자 좋아요 여부를 사용한다
      const detail = result.data;

      /**
       * 서버가 반환한 값이 있는 항목만 현재 낙관적 좋아요 상태에 병합한다
       *
       * @author HanWon.Jang
       * @param current 현재 화면에 누적된 피드 목록
       * @return 서버가 확정한 좋아요 상태가 반영된 새 피드 목록
       */
      const applyServerLike = (current: FeedItem[]): FeedItem[] => {
        // 응답 필드가 없으면 현재 낙관적 값을 유지해 불완전한 응답의 역변경을 막는다
        const serverDetail = {
          likeCnt: detail?.likeCnt ?? optimisticDetail.likeCnt,
          likeYsno: detail?.likeYsno ?? optimisticDetail.likeYsno,
        };
        // 서버가 확정한 좋아요 상태를 선택한 피드 대상에 반영해 반환한다
        return getUpdatedLikeItems(current, item, serverDetail);
      };

      // 서버가 확정한 값으로 화면의 낙관적 상태를 보정한다
      setItems(applyServerLike);
      // 알림으로 직접 연 카드가 같은 대상이면 서버가 확정한 좋아요 상태를 함께 반영한다
      setFocusedItem((current) => current && isSameFeedTarget(current, item)
        ? { ...current, likeCnt: detail?.likeCnt ?? optimisticDetail.likeCnt,
            likeYsno: detail?.likeYsno ?? optimisticDetail.likeYsno }
        : current);
    }

    // 핵심 좋아요 요청 실패 시 클릭 전 상태를 복원하고 안전한 오류를 안내한다
    catch (likeError) {
      /**
       * 좋아요 저장 실패 시 선택한 피드 대상을 클릭 전 상태로 복원한다
       *
       * @author HanWon.Jang
       * @param current 현재 화면에 누적된 피드 목록
       * @return 선택한 대상의 좋아요 상태가 복원된 새 피드 목록
       */
      const restorePreviousLike = (current: FeedItem[]): FeedItem[] => {
        // 클릭 전 좋아요 수와 여부를 선택한 피드 대상에 다시 반영해 반환한다
        return getUpdatedLikeItems(current, item, item);
      };

      // 핵심 좋아요 요청이 실패한 경우에만 클릭 전 상태로 되돌린다
      setItems(restorePreviousLike);
      // 알림으로 직접 연 카드가 같은 대상이면 클릭 전 좋아요 상태로 복원한다
      setFocusedItem((current) => current && isSameFeedTarget(current, item)
        ? { ...current, likeCnt: item.likeCnt, likeYsno: item.likeYsno }
        : current);
      // "좋아요 처리에 실패했어요."
      const likeFailedTitle = message("frontend.feed.likeFailed");
      // "다시 시도해주세요."
      const retryMessage = message("frontend.common.tryAgain");
      // 원시 오류 대신 서버 또는 공통 재시도 문구를 선택한다
      const likeErrorMessage = getApiErrorMessage(likeError, retryMessage);
      // 좋아요 상태를 복원한 뒤 사용자에게 안전한 실패 문구를 표시한다
      await sweetError(likeFailedTitle, likeErrorMessage);
    }

    // 성공과 실패 모두 같은 대상의 다음 좋아요 입력을 허용한다
    finally {
      // 성공과 실패 모두에서 같은 대상의 다음 좋아요 입력을 허용한다
      pendingLikeKeysRef.current.delete(pendingKey);
    }
  };

  /**
   * 사진 변경 피드 유형에 맞는 활동 문구를 반환한다.
   *
   * @author HanWon.Jang
   * @param item 활동 유형을 포함한 피드 항목
   * @return 프로필 또는 배경사진 변경 설명이며 사진 변경 피드가 아니면 빈 문자열
   */
  const getImageActivityText = (item: FeedItem): string => {
    // 프로필 사진 변경 활동에는 전용 문구를 표시한다
    if (item.tagtType === "PROFILE_IMAGE") {
      // "프로필 사진을 변경했어요"
      return message("frontend.feed.profileChanged");
    }

    // 배경사진 변경 활동에는 전용 문구를 표시한다
    if (item.tagtType === "BACKGROUND_IMAGE") {
      // "배경사진을 변경했어요"
      return message("frontend.feed.backgroundChanged");
    }

    // 사진 변경 이외의 피드에는 활동 문구를 제공하지 않는다
    return "";
  };

  /**
   * 지정한 독후감 피드 본문의 펼침 상태를 반대로 전환한다
   *
   * @author HanWon.Jang
   * @param tagtNumb 펼침 상태를 변경할 피드 대상 번호
   * @return 반환값이 없다
   */
  const toggleReportContent = (tagtNumb: number): void => {
    /**
     * 현재 피드별 펼침 상태에서 지정한 독후감 대상만 반전한다
     *
     * @author HanWon.Jang
     * @param current 피드 대상별 현재 펼침 상태
     * @return 지정한 피드의 펼침 상태가 반전된 새 객체
     */
    const toggleCurrentReport = (current: Record<number, boolean>): Record<number, boolean> => {
      // 다른 피드 상태를 유지하며 선택한 대상만 반전해 반환한다
      return getToggledReports(current, tagtNumb);
    };

    // 선택한 독후감 피드 본문의 펼침 상태를 갱신한다
    setExpandedReports(toggleCurrentReport);
  };

  /**
   * 피드 최초 조회 실패 상태에서 첫 페이지를 다시 요청한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const retryFeed = (): void => {
    // 기존 오류 문구를 초기화하는 첫 페이지 조회를 다시 시작한다
    void loadPage(1);
  };

  /**
   * 현재 마지막 성공 페이지 다음의 피드 목록을 추가로 요청한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const loadMoreFeed = (): void => {
    // 무한 스크롤 도달 시 마지막 성공 페이지의 다음 번호를 조회한다
    void loadPage(page + 1);
  };

  /**
   * 현재 열려 있는 피드 댓글 목록 대상을 해제한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const closeReplySheet = (): void => {
    // 댓글 목록을 닫고 선택된 피드 대상 상태를 초기화한다
    setReplyItem(null);
  };

  /**
   * 피드 유형별 미디어와 독후감 정보 및 교류 기능을 포함한 카드 한 건을 렌더링한다
   *
   * @author HanWon.Jang
   * @param item 렌더링할 피드 항목
   * @return 피드 유형에 맞게 구성된 카드 요소
   */
  const renderFeedItem = (item: FeedItem): ReactNode => {
    // 독후감 본문 앞뒤 공백을 제거해 빈 내용과 펼침 기준을 정확히 판정한다
    const reportContent = item.reptCntn?.trim() ?? "";
    // 공통 미리보기 길이를 초과한 독후감에만 펼침 기능을 제공한다
    const isLongContent = reportContent.length > REPORT_CONTENT_PREVIEW_LENGTH;
    // 저장된 대상별 펼침 상태를 boolean 값으로 보정한다
    const isExpanded = Boolean(expandedReports[item.tagtNumb]);
    // 피드 대상 유형별 카드 영역을 선택할 판정값을 계산한다
    const isReportFeed = item.tagtType === "REPORT";
    const isProfileImageFeed = item.tagtType === "PROFILE_IMAGE";
    const isBackgroundImageFeed = item.tagtType === "BACKGROUND_IMAGE";
    const isImageFeed = isProfileImageFeed || isBackgroundImageFeed;
    // 피드 작성자와 사진 활동에 동일하게 표시할 발생 날짜를 계산한다
    const activityDateLabel = new Date(item.activityDate).toLocaleDateString();
    // 독후감 번호가 있는 정상 피드는 도서 정보 상세로 이동하고 누락된 예외 데이터는 도서 검색으로 이동한다
    const bookInfoPath = item.reptNumb
      ? `/book/info/${item.reptNumb}`
      : "/book/search";
    // 책 표지와 제목에서 도서검색을 즉시 실행할 제목 검색어를 정규화한다
    const bookTitleKeyword = item.bookTitl?.trim() ?? "";
    // 저자 검색 링크는 공백을 제거한 실제 저자명이 있을 때만 표시한다
    const bookAuthorKeyword = item.bookAthr?.trim() ?? "";
    // 펼침 버튼의 현재 동작을 보조기기에 전달할 문구를 조회한다
    const expandActionLabel = getExpandActionLabel(isExpanded);
    // 펼침 상태에 맞는 공통 독후감 화살표 스타일을 선택한다
    const expandArrowClass = isExpanded
      ? reportListStyles.expandArrowOpen
      : reportListStyles.expandArrow;
    // "좋아요"
    const likeActionLabel = message("frontend.feed.likeAction");
    // "댓글 보기"
    const viewCommentsLabel = message("frontend.book.publicReports.viewComments");
    // 프로필 사진 피드는 사용자 기본 이미지를 사용하고 배경사진 피드는 도서 공통 대체 이미지를 사용한다
    const imageFallback = isProfileImageFeed
      ? DEFAULT_PROFILE_IMAGE
      : "/img/common/no-image.png";
    // 원본 경로를 우선 사용하고 없으면 화면용 경로와 유형별 대체 이미지 순서로 보정한다
    const imageSource = item.contentImagePath || item.contentImageDisplayPath || imageFallback;

    /**
     * 현재 피드 작성자의 공개 프로필 화면으로 이동한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const moveAuthorProfile = (): void => {
      // 피드 작성자 번호를 공개 프로필 경로에 포함해 이동한다
      navigate(`/social/profile/${item.userNumb}`);
    };

    /**
     * 현재 독후감 피드 본문의 펼침 상태를 반대로 전환한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const toggleCurrentContent = (): void => {
      // 현재 카드의 안정적인 피드 대상 번호로 펼침 상태를 변경한다
      toggleReportContent(item.tagtNumb);
    };

    /**
     * 현재 피드 대상의 좋아요 상태 변경을 비동기로 시작한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const toggleCurrentLike = (): void => {
      // 현재 카드의 좋아요 상태를 낙관적으로 반영하고 서버 결과로 확정한다
      void handleLike(item);
    };

    /**
     * 현재 피드 대상의 댓글 목록을 확인할 수 있도록 선택 상태를 설정한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    const openCurrentReplies = (): void => {
      // 댓글 목록에 현재 피드 유형과 대상 번호를 전달하도록 선택 항목을 저장한다
      setReplyItem(item);
    };

    // 피드 유형에 맞는 미디어와 교류 기능을 포함한 카드 한 건을 반환한다
    return (
      /* 피드 개별 활동 카드 영역 */
      <article className={styles.card} key={`${item.tagtType}-${item.tagtNumb}`}>
        {/* 피드 작성자 프로필 이동 영역 */}
        <header className={styles.cardHeader}>
          <button className={styles.authorButton} type="button" onClick={moveAuthorProfile}>
            {/* 활동 작성자의 프로필 사진과 닉네임 영역 */}
            <span className={styles.authorIdentity}>
              <ProfileImage className={styles.avatar} src={item.porfPath} alt="" />
              <span className={styles.authorName}>{item.userNick}</span>
            </span>
          </button>
        </header>

        {/* 독후감 도서 표지와 제목 및 저자 정보 영역 */}
        {isReportFeed ? (
          <div className={styles.reportMediaRow}>
            {/* 도서 제목 검색으로 이동하는 표지 영역 */}
            <Link
              className={styles.reportCoverLink}
              to="/book/search"
              state={{ initialSearchKeyword: bookTitleKeyword }}
            >
              <img
                className={styles.reportMedia}
                src={item.bookCvim || "/img/common/no-image.png"}
                alt={item.bookTitl ?? ""}
                onError={handleImageError}
              />
            </Link>
            {/* 도서 제목과 저자 및 독후감 상태 영역 */}
            <div className={styles.mediaInfo}>
              <Link
                className={styles.bookInfoLink}
                to="/book/search"
                state={{ initialSearchKeyword: bookTitleKeyword }}
              >
                <span className={styles.title}>{item.bookTitl}</span>
              </Link>
              {/* 도서 저자와 독후감 공개 날짜 영역 */}
              <div className={styles.bookAuthorRow}>
                {/* 저자명이 있으면 해당 이름으로 도서를 검색하는 링크를 표시한다 */}
                {bookAuthorKeyword ? (
                  <Link
                    className={styles.authorSearchLink}
                    to="/book/search"
                    state={{ initialSearchKeyword: bookAuthorKeyword }}
                  >
                    {item.bookAthr}
                  </Link>
                ) : null}
                {/* 독후감 공개 날짜를 도서 저자 바로 옆에 표시한다 */}
                <span className={styles.activityDate}>{activityDateLabel}</span>
              </div>
              {/* 평점 또는 독서 상태가 있는 독후감의 도서 정보 이동 영역 */}
              {item.reptGrde || item.reptStatName ? (
                <Link className={styles.bookInfoLink} to={bookInfoPath}>
                  <span className={styles.ratingStatusRow}>
                    {/* 독후감 평점 표시 영역 */}
                    {item.reptGrde ? (
                      <span className={styles.rating}>
                        <svg className={styles.ratingIcon} viewBox="0 0 24 24" aria-hidden="true">
                          <path
                            d="m12 3.5 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.5Z"
                            fill="currentColor"
                            stroke="currentColor"
                            strokeWidth="1.4"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                        </svg>
                        {item.reptGrde}
                      </span>
                    ) : null}
                    {/* 독후감 독서 상태 표시 영역 */}
                    {item.reptStatName ? (
                      <span className={getStatusClassName(item.reptStat)}>{item.reptStatName}</span>
                    ) : null}
                  </span>
                </Link>
              ) : null}
            </div>
          </div>
        ) : null}

        {/* 프로필 또는 배경사진과 사진 변경 설명 영역 */}
        {isImageFeed ? (
          <FullscreenImageButton
            className={styles.backgroundMediaButton}
            source={imageSource}
            fallbackSource={imageFallback}
            alt={isProfileImageFeed
              ? /* "프로필 사진" */ message("frontend.imageViewer.profileAlt")
              : /* "배경사진" */ message("frontend.imageViewer.backgroundAlt")}
          >
            {/* 프로필 또는 배경사진 영역 */}
            <span className={styles.backgroundMediaWrap}>
              {/* 프로필 사진 변경 피드는 배경사진과 같은 크기의 사진으로 표시한다 */}
              {isProfileImageFeed ? (
                <ProfileImage
                  className={styles.backgroundMedia}
                  src={item.contentImageDisplayPath || item.contentImagePath}
                  alt={/* "프로필 사진" */ message("frontend.imageViewer.profileAlt")}
                />
              ) : (
                <BackgroundImage
                  source={item.contentImageDisplayPath || item.contentImagePath || "/img/common/no-image.png"}
                  imageClassName={styles.backgroundMedia}
                  alt={/* "배경사진" */ message("frontend.imageViewer.backgroundAlt")}
                  fallbackSource="/img/common/no-image.png"
                />
              )}
            </span>
            {/* 사진 변경 유형과 발생 날짜를 사진 아래 빈 공간의 오른쪽에 표시한다 */}
            <span className={styles.imageActivity}>
              {getImageActivityText(item)} · {activityDateLabel}
            </span>
          </FullscreenImageButton>
        ) : null}

        {/* 독후감 본문과 펼침 제어 영역 */}
        {reportContent ? (
          <div className={styles.contentSection}>
            {/* 독후감 본문은 도서 정보 상세로 이동하고 다른 활동 본문은 정적으로 표시한다 */}
            {item.tagtType === "REPORT" ? (
              <Link className={styles.reportContentLink} to={bookInfoPath}>
                <AnimatedReportContent
                  content={reportContent}
                  expanded={isExpanded || !isLongContent}
                />
              </Link>
            ) : (
              <AnimatedReportContent
                content={reportContent}
                expanded={isExpanded || !isLongContent}
              />
            )}
            {/* 긴 독후감 본문의 펼침 또는 접기 버튼 영역 */}
            {isLongContent ? (
              <button
                className={reportListStyles.expandButton}
                type="button"
                aria-label={expandActionLabel}
                onClick={toggleCurrentContent}
              >
                <img
                  className={expandArrowClass}
                  src="/img/icons/arrow-bottom.svg"
                  alt=""
                  aria-hidden="true"
                />
              </button>
            ) : null}
          </div>
        ) : null}

        {/* 피드 좋아요와 댓글 교류 영역 */}
        <footer className={styles.actions}>
          {/* 좋아요 변경과 좋아요 사용자 목록 영역 */}
          <div className={styles.likeActionGroup}>
            <button
              className={styles.likeIconButton}
              type="button"
              aria-label={likeActionLabel}
              onClick={toggleCurrentLike}
            >
              <img
                className={styles.icon}
                src={item.likeYsno === "Y" ? "/img/icons/icon-heart-fill.svg" : "/img/icons/icon-heart.svg"}
                alt=""
              />
            </button>
            <LikeUserListButton
              className={styles.likeCountButton}
              tagtType={item.tagtType}
              tagtNumb={item.tagtNumb}
              countLabel={item.likeCnt}
            />
          </div>
          {/* 댓글 목록 열기 영역 */}
          <button
            className={styles.commentButton}
            type="button"
            aria-label={viewCommentsLabel}
            onClick={openCurrentReplies}
          >
            <img className={styles.icon} src="/img/icons/icon-comment.svg" alt="" />
            {item.replCnt}
          </button>
        </footer>
      </article>
    );
  };

  // 알림 대상과 페이지 목록에서 같은 카드는 한 번만 표시하도록 화면 목록을 구성한다
  const visibleItems = focusedItem
    ? [focusedItem, ...items.filter((item) => !isSameFeedTarget(item, focusedItem))]
    : items;

  // 첫 피드 데이터를 불러오는 동안 전체 로딩 화면을 반환한다
  if (isLoading && visibleItems.length === 0) return <Loading />;

  // 유형별 카드와 공통 교류 동작을 포함한 피드 화면을 반환한다
  return (
    <main className={styles.page}>
      {/* 본인과 팔로잉 사용자의 공개 활동 피드 전체 영역 */}
      {/* 피드 최초 조회 실패와 재시도 영역 */}
      {error && visibleItems.length === 0 ? (
        <div className={styles.error}>
          {error}
          <br />
          <button className={styles.retry} type="button" onClick={retryFeed}>
            {/* "다시 시도" */}
            {message("frontend.common.retry")}
          </button>
        </div>
      ) : null}

      {/* 본인과 팔로잉 공개 활동이 없는 피드 빈 상태 영역 */}
      {!error && visibleItems.length === 0 ? (
        <p className={styles.empty}>
          {/* "아직 표시할 공개 소식이 없어요.\n내 독후감과 사진 변경, 팔로잉 소식이 여기에 표시됩니다." */}
          {message("frontend.feed.empty")}
        </p>
      ) : null}

      {/* 페이지 단위로 누적되는 피드 카드 목록 영역 */}
      <div className={styles.list}>
        {visibleItems.map(renderFeedItem)}
      </div>

      {/* 다음 피드 페이지 자동 조회와 추가 로딩 영역 */}
      <InfiniteScrollTrigger hasNext={hasNext} isLoading={isLoading} onLoadMore={loadMoreFeed}>
        <Loading isFullScreen={false} />
      </InfiniteScrollTrigger>

      {/* 선택한 피드 대상의 댓글 목록 영역 */}
      {replyItem ? (
        <ReplySheet
          report={{ reptNumb: replyItem.tagtNumb, userNick: replyItem.userNick }}
          tagtType={replyItem.tagtType}
          focusReplNumb={focusReplNumb}
          onClose={closeReplySheet}
        />
      ) : null}
    </main>
  );
};

export default FeedPage;
