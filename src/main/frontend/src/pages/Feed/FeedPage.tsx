import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import { setPublicReportLikeApi } from "@/features/Book/api/bookApi";
import { getFeedPageApi, type FeedItem } from "@/features/Feed/api/feedApi";
import ReplySheet from "@/features/reply/ReplySheet";
import ProfileImage from "@/features/User/components/ProfileImage";
import { type SyntheticEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./FeedPage.css";

const MEDIA_BUTTON_CLASSES: Record<FeedItem["tagtType"], string> = {
  REPORT: styles.reportMediaButton,
  PROFILE_IMAGE: styles.profileMediaButton,
  BACKGROUND_IMAGE: styles.backgroundMediaButton,
};

const MEDIA_CLASSES: Record<FeedItem["tagtType"], string> = {
  REPORT: styles.reportMedia,
  PROFILE_IMAGE: styles.profileMedia,
  BACKGROUND_IMAGE: styles.backgroundMedia,
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
    if (item.tagtType === "PROFILE_IMAGE") return message("frontend.feed.profileChanged");
    if (item.tagtType === "BACKGROUND_IMAGE") return message("frontend.feed.backgroundChanged");
    return message("frontend.feed.reportPublished");
  };

  if (isLoading && items.length === 0) return <Loading />;

  return (
    <main className={styles.page}>
      {error && items.length === 0 ? (
        <div className={styles.error}>{error}<br /><button className={styles.retry} onClick={() => void loadPage(1)}>{message("frontend.common.retry")}</button></div>
      ) : null}
      {!error && items.length === 0 ? <p className={styles.empty}>{message("frontend.feed.empty")}</p> : null}
      <div className={styles.list}>
        {items.map((item) => (
          <article className={styles.card} key={`${item.tagtType}-${item.tagtNumb}`}>
            <header className={styles.cardHeader}>
              <button className={styles.authorButton} type="button" onClick={() => navigate(`/social/profile/${item.userNumb}`)}>
                <ProfileImage className={styles.avatar} src={item.porfPath} alt="" />
                <span><p className={styles.authorName}>{item.userNick}</p><p className={styles.activity}>{getActivityText(item)} · {new Date(item.activityDate).toLocaleDateString()}</p></span>
              </button>
            </header>
            <button className={MEDIA_BUTTON_CLASSES[item.tagtType]} type="button" onClick={() => openItem(item)}>
              {item.tagtType === "PROFILE_IMAGE" ? (
                <ProfileImage className={MEDIA_CLASSES[item.tagtType]} src={item.contentImagePath} alt="" />
              ) : (
                <img
                  className={MEDIA_CLASSES[item.tagtType]}
                  src={(item.tagtType === "REPORT" ? item.bookCvim : item.contentImagePath) || "/img/common/no-image.png"}
                  alt=""
                  onError={handleImageError}
                />
              )}
              <span className={styles.mediaInfo}>
                <p className={styles.title}>{item.tagtType === "REPORT" ? item.bookTitl : getActivityText(item)}</p>
                <p className={styles.metadata}>{item.tagtType === "REPORT" ? `${item.bookAthr ?? ""} · ${item.reptStatName ?? ""}` : item.userNick}</p>
                {item.tagtType === "REPORT" && item.reptGrde ? <p className={styles.rating}>★ {item.reptGrde}</p> : null}
              </span>
            </button>
            {item.reptCntn ? <p className={styles.content}>{item.reptCntn}</p> : null}
            <footer className={styles.actions}>
              <button className={styles.actionButton} type="button" aria-label={message("frontend.feed.likeAction")} onClick={() => void handleLike(item)}><img className={styles.icon} src={item.likeYsno === "Y" ? "/img/icons/icon-heart-fill.svg" : "/img/icons/icon-heart.svg"} alt="" />{item.likeCnt}</button>
              <button className={styles.commentButton} type="button" aria-label={message("frontend.book.publicReports.viewComments")} onClick={() => setReplyItem(item)}><img className={styles.icon} src="/img/icons/icon-comment.svg" alt="" />{item.replCnt}</button>
            </footer>
          </article>
        ))}
      </div>
      <InfiniteScrollTrigger hasNext={hasNext} isLoading={isLoading} onLoadMore={() => void loadPage(page + 1)}>{<Loading isFullScreen={false} />}</InfiniteScrollTrigger>
      {replyItem ? <ReplySheet report={{ reptNumb: replyItem.tagtNumb, userNick: replyItem.userNick }} tagtType={replyItem.tagtType} onClose={() => setReplyItem(null)} /> : null}
    </main>
  );
};

export default FeedPage;
