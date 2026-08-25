import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
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
import { getFeedPageApi, type FeedItem } from "@/features/Feed/api/feedApi";
import ReplySheet from "@/features/reply/ReplySheet";
import LikeUserListButton from "@/features/Social/components/LikeUserListButton";
import ProfileImage from "@/features/User/components/ProfileImage";
import { type SyntheticEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
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

/** 팔로잉 사용자의 공개 독후감과 사진 변경 활동을 카드 목록으로 표시한다. */
const FeedPage = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<FeedItem[]>([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [replyItem, setReplyItem] = useState<FeedItem | null>(null);
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>({});

  /** 요청한 피드 페이지를 첫 목록 또는 다음 목록으로 반영한다. */
  const loadPage = useCallback(async (targetPage: number): Promise<void> => {
    setIsLoading(true);
    setError("");
    try {
      const data = await getFeedPageApi(targetPage);
      setItems((current) => targetPage === 1 ? data.list : [...current, ...data.list]);
      setPage(data.page);
      setHasNext(data.hasNext);
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, message("frontend.feed.loadFailed")));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => { void loadPage(1); }, [loadPage]);

  /** 이미지 요청이 실패하면 공통 대체 이미지를 사용한다. */
  const handleImageError = (event: SyntheticEvent<HTMLImageElement>): void => {
    const failedImage = event.currentTarget;
    const fallbackImage = "/img/common/no-image.png";

    // 공통 대체 이미지까지 실패한 경우 같은 경로를 반복 요청하지 않는다
    if (failedImage.getAttribute("src") === fallbackImage) {
      // 현재 대체 이미지 상태를 유지한다
      return;
    }

    // 도서 표지와 배경사진을 공통 대체 이미지로 한 번만 교체한다
    failedImage.src = fallbackImage;
  };

  /** 피드 카드의 좋아요 상태를 서버 결과로 갱신한다. */
  const handleLike = async (item: FeedItem): Promise<void> => {
    try {
      const result = await setPublicReportLikeApi({ tagtType: item.tagtType, tagtNumb: item.tagtNumb });
      const detail = result.data as { likeCnt?: number; likeYsno?: "Y" | "N" } | undefined;
      setItems((current) => current.map((candidate) => candidate.tagtType === item.tagtType && candidate.tagtNumb === item.tagtNumb
        ? { ...candidate, likeCnt: detail?.likeCnt ?? candidate.likeCnt, likeYsno: detail?.likeYsno ?? candidate.likeYsno }
        : candidate));
    } catch (likeError) {
      await sweetError(
        message("frontend.feed.likeFailed"),
        getApiErrorMessage(likeError, message("frontend.common.tryAgain")),
      );
    }
  };

  /** 카드 종류에 맞는 상세 화면으로 이동한다. */
  const openItem = (item: FeedItem): void => {
    navigate(item.tagtType === "REPORT" && item.reptNumb
      ? `/report/detail/${item.reptNumb}`
      : `/social/profile/${item.userNumb}`);
  };

  /** 피드 유형에 맞는 활동 문구를 반환한다. */
  const getActivityText = (item: FeedItem): string => {
    // 프로필 사진 변경 활동에는 전용 문구를 표시한다
    if (item.tagtType === "PROFILE_IMAGE") return message("frontend.feed.profileChanged");
    // 배경사진 변경 활동에는 전용 문구를 표시한다
    if (item.tagtType === "BACKGROUND_IMAGE") return message("frontend.feed.backgroundChanged");
    // 독후감 피드에는 공개 완료 문구를 표시한다
    return message("frontend.feed.reportPublished");
  };

  /** 독후감 본문의 펼침 상태를 반대로 전환한다. */
  const toggleReportContent = (tagtNumb: number): void => {
    setExpandedReports((current) => ({
      ...current,
      [tagtNumb]: !current[tagtNumb],
    }));
  };

  // 첫 피드 데이터를 불러오는 동안 전체 로딩 화면을 반환한다
  if (isLoading && items.length === 0) return <Loading />;

  // 유형별 카드와 공통 교류 동작을 포함한 피드 화면을 반환한다
  return (
    <main className={styles.page}>
      {error && items.length === 0 ? (
        <div className={styles.error}>{error}<br /><button className={styles.retry} onClick={() => void loadPage(1)}>{message("frontend.common.retry")}</button></div>
      ) : null}
      {!error && items.length === 0 ? <p className={styles.empty}>{message("frontend.feed.empty")}</p> : null}
      <div className={styles.list}>
        {items.map((item) => {
          const reportContent = item.reptCntn?.trim() ?? "";
          const isLongContent = reportContent.length > REPORT_CONTENT_PREVIEW_LENGTH;
          const isExpanded = Boolean(expandedReports[item.tagtNumb]);

          // 피드 유형에 맞는 미디어와 교류 기능을 포함한 카드 한 건을 반환한다
          return (
            <article className={styles.card} key={`${item.tagtType}-${item.tagtNumb}`}>
              <header className={styles.cardHeader}>
                <button className={styles.authorButton} type="button" onClick={() => navigate(`/social/profile/${item.userNumb}`)}>
                  <ProfileImage className={styles.avatar} src={item.porfPath} alt="" />
                  <span><span className={styles.authorName}>{item.userNick}</span><span className={styles.activity}>{getActivityText(item)} · {new Date(item.activityDate).toLocaleDateString()}</span></span>
                </button>
              </header>

              {item.tagtType === "REPORT" ? (
                <button className={styles.reportMediaButton} type="button" onClick={() => openItem(item)}>
                  <img
                    className={styles.reportMedia}
                    src={item.bookCvim || "/img/common/no-image.png"}
                    alt=""
                    onError={handleImageError}
                  />
                  <span className={styles.mediaInfo}>
                    <span className={styles.title}>{item.bookTitl}</span>
                    <span className={styles.metadata}>{item.bookAthr ?? ""}</span>
                    <span className={styles.ratingStatusRow}>
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
                      ) : <span />}
                      {item.reptStatName ? (
                        <span className={getStatusClassName(item.reptStat)}>{item.reptStatName}</span>
                      ) : null}
                    </span>
                  </span>
                </button>
              ) : null}

              {item.tagtType === "BACKGROUND_IMAGE" ? (
                <button className={styles.backgroundMediaButton} type="button" onClick={() => openItem(item)}>
                  <img
                    className={styles.backgroundMedia}
                    src={item.contentImagePath || "/img/common/no-image.png"}
                    alt=""
                    onError={handleImageError}
                  />
                </button>
              ) : null}

              {reportContent ? (
                <div className={styles.contentSection}>
                  <AnimatedReportContent
                    content={reportContent}
                    expanded={isExpanded || !isLongContent}
                  />
                  {isLongContent ? (
                    <button
                      className={reportListStyles.expandButton}
                      type="button"
                      aria-label={message(isExpanded
                        ? "frontend.common.collapse"
                        : "frontend.book.publicReports.expand")}
                      onClick={() => toggleReportContent(item.tagtNumb)}
                    >
                      <img
                        className={isExpanded
                          ? reportListStyles.expandArrowOpen
                          : reportListStyles.expandArrow}
                        src="/img/icons/arrow-bottom.svg"
                        alt=""
                        aria-hidden="true"
                      />
                    </button>
                  ) : null}
                </div>
              ) : null}

              <footer className={styles.actions}>
                <div className={styles.likeActionGroup}>
                  <button className={styles.likeIconButton} type="button" aria-label={message("frontend.feed.likeAction")} onClick={() => void handleLike(item)}>
                    <img className={styles.icon} src={item.likeYsno === "Y" ? "/img/icons/icon-heart-fill.svg" : "/img/icons/icon-heart.svg"} alt="" />
                  </button>
                  <LikeUserListButton
                    className={styles.likeCountButton}
                    tagtType={item.tagtType}
                    tagtNumb={item.tagtNumb}
                    countLabel={item.likeCnt}
                  />
                </div>
                <button className={styles.commentButton} type="button" aria-label={message("frontend.book.publicReports.viewComments")} onClick={() => setReplyItem(item)}><img className={styles.icon} src="/img/icons/icon-comment.svg" alt="" />{item.replCnt}</button>
              </footer>
            </article>
          );
        })}
      </div>
      <InfiniteScrollTrigger hasNext={hasNext} isLoading={isLoading} onLoadMore={() => void loadPage(page + 1)}>{<Loading isFullScreen={false} />}</InfiniteScrollTrigger>
      {replyItem ? <ReplySheet report={{ reptNumb: replyItem.tagtNumb, userNick: replyItem.userNick }} tagtType={replyItem.tagtType} onClose={() => setReplyItem(null)} /> : null}
    </main>
  );
};

export default FeedPage;
