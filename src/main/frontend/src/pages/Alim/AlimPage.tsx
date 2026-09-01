import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import {
  getMyAlimListApi,
  getAlimTargetApi,
  delAllAlimApi,
  uptAlimReadApi,
  type AlimItem,
} from "@/features/Alim/api/alimApi";
import { notifyUnreadAlimChange } from "@/features/Alim/lib/alimEvents";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./AlimPage.css";

const ALIM_DISMISS_ANIMATION_MS = 360;
const ALIM_DISMISS_STAGGER_MS = 30;
const ALIM_DISMISS_MAX_STAGGER_COUNT = 10;

/**
 * 로그인 사용자의 알림 목록을 보여주는 페이지입니다.
 * 삭제되지 않은 알림을 모두 보여주며, 개별 링크 클릭으로 읽음 처리하고 모두 지우기로 목록에서 제거합니다.
 *
 * @author HanWon.Jang
 * @return 알림 목록 화면
 */
function AlimPage() {

  const navigate = useNavigate();
  const [alimList, setAlimList] = useState<AlimItem[]>([]);
  const [nextPage, setNextPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isFetchingMore, setIsFetchingMore] = useState(false);
  const [isDeletingAll, setIsDeletingAll] = useState(false);
  const [isClearingAll, setIsClearingAll] = useState(false);
  const [readingAlimNumb, setReadingAlimNumb] = useState<number | null>(null);
  const dismissTimerRef = useRef<number | null>(null);

  const loadAlimList = useCallback(
    async (page: number) => {

      const isFirstPage = page === 1;

      if (isFirstPage) {
        setIsLoading(true);
      } else {
        setIsFetchingMore(true);
      }

      try {
        const response = await getMyAlimListApi(page);
        const data = response.data;

        // 목록 조회는 상태를 변경하지 않으므로 서버가 반환한 미삭제 알림을 읽음 상태 그대로 페이지 순서대로 병합한다.
        setAlimList((prevList) => (
          isFirstPage ? data.list ?? [] : [...prevList, ...(data.list ?? [])]
        ));
        setHasNext(Boolean(data.hasNext));
        setNextPage(data.nextPage ?? page + 1);
        notifyUnreadAlimChange(data.unreadCnt ?? 0);
      } catch (error) {
        void sweetError(
          message("frontend.alim.list.failedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      } finally {
        if (isFirstPage) {
          setIsLoading(false);
        } else {
          setIsFetchingMore(false);
        }
      }
    },
    [],
  );

  useEffect(() => {

    void loadAlimList(1);
  }, [loadAlimList]);

  useEffect(() => {

    return () => {

      if (dismissTimerRef.current !== null) {
        window.clearTimeout(dismissTimerRef.current);
      }
    };
  }, []);

  /**
   * handle Delete All 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleDeleteAll = async () => {

    if (isDeletingAll) {
      return;
    }

    let dismissAnimationStarted = false;
    setIsDeletingAll(true);

    try {
      const response = await delAllAlimApi();

      // 서버는 아직 불러오지 않은 알림까지 모두 삭제 처리하므로 추가 페이지 요청을 즉시 중단한다.
      setHasNext(false);
      notifyUnreadAlimChange(response.data?.unreadCnt ?? 0);

      // 현재 화면에 카드가 있으면 순차적으로 오른쪽 퇴장시킨 뒤 목록을 비워 빈 상태 문구로 전환한다.
      if (alimList.length > 0) {
        dismissAnimationStarted = true;
        setIsClearingAll(true);
        const maxStaggerCount = Math.min(
          Math.max(alimList.length - 1, 0),
          ALIM_DISMISS_MAX_STAGGER_COUNT,
        );
        const totalAnimationMs =
          ALIM_DISMISS_ANIMATION_MS
          + maxStaggerCount * ALIM_DISMISS_STAGGER_MS;

        dismissTimerRef.current = window.setTimeout(() => {

          setAlimList([]);
          setIsClearingAll(false);
          setIsDeletingAll(false);
          dismissTimerRef.current = null;
        }, totalAnimationMs);
        return;
      }

      setAlimList([]);
    } catch (error) {
      void sweetError(
        message("frontend.alim.readAll.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      // 퇴장 애니메이션이 시작된 경우에는 타이머 완료 시 버튼 잠금을 해제해 중복 요청을 막는다.
      if (!dismissAnimationStarted) {
        setIsDeletingAll(false);
      }
    }
  };

  /**
   * handle Alim Click 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param alim alim 입력값
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleAlimClick = async (alim: AlimItem) => {
    // 링크가 없는 알림은 단순 안내 알림으로 취급해 현재 화면을 유지합니다.
    if (!alim.linkUrlx || readingAlimNumb !== null || isClearingAll) {
      return;
    }

    setReadingAlimNumb(alim.alimNumb);

    try {
      const readResponse = await uptAlimReadApi(alim.alimNumb);
      notifyUnreadAlimChange(readResponse.data?.unreadCnt ?? 0);
      // 읽은 알림도 알림센터에 유지하므로 제거하지 않고 상태만 바꾸어 어두운 스타일을 즉시 적용한다.
      setAlimList((prevList) => (
        prevList.map((item) => (
          item.alimNumb === alim.alimNumb
            ? { ...item, readYsno: "Y" }
            : item
        ))
      ));
      // 클릭 시점의 콘텐츠 공개 여부와 팔로우 관계를 반영한 최종 이동 주소를 조회한다
      const targetResponse = await getAlimTargetApi(alim.alimNumb);
      // 서버가 소유권과 현재 접근 권한을 검증한 내부 경로로 이동한다
      navigate(targetResponse.data.linkUrlx);
    } catch (error) {
      void sweetError(
        message("frontend.alim.readAll.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setReadingAlimNumb(null);
    }
  };

  if (isLoading) {
    return <Loading />;
  }

  return (
    /* 알림 설정과 수신 내역 전체 영역 */
    <main className={styles.page}>
      {/* 알림센터 페이지 설명과 알림 설정 및 전체 삭제 영역 */}
      <section className={styles.header}>
        <p className={styles.description}>
          {/* "내게 온 소식을 확인해보세요" */}
          {message("frontend.alim.subtitle")}
        </p>
        <div className={styles.headerActions}>
          <button
            className={styles.pushButton}
            type="button"
            onClick={() => navigate("/settings/notifications")}
          >
            {/* "알림 설정" */}
            {message("frontend.settings.notifications.title")}
          </button>
          <button
            className={styles.readAllButton}
            type="button"
            disabled={isDeletingAll || isClearingAll || alimList.length === 0}
            onClick={handleDeleteAll}
          >
            {/* "모두 지우기" */}
            {message("frontend.alim.readAll")}
          </button>
        </div>
      </section>

      {alimList.length === 0 ? (
        <div className={styles.empty}>{message("frontend.alim.empty")}</div>
      ) : (
        /* 삭제되지 않은 알림 목록 영역 */
        <section className={styles.list} aria-label={message("frontend.alim.title")}>
          {alimList.map((alim, index) => (
            /* 개별 알림 내용과 이동 영역 */
            <button
              className={[
                styles.itemButton,
                alim.readYsno === "Y" ? styles.itemButtonRead : "",
                isClearingAll ? styles.itemButtonLeaving : "",
              ].filter(Boolean).join(" ")}
              type="button"
              disabled={isClearingAll || readingAlimNumb === alim.alimNumb}
              onClick={() => void handleAlimClick(alim)}
              style={isClearingAll ? {
                animationDelay: `${
                  Math.min(index, ALIM_DISMISS_MAX_STAGGER_COUNT)
                  * ALIM_DISMISS_STAGGER_MS
                }ms`,
              } : undefined}
              key={`${alim.userNumb}-${alim.alimNumb}`}
            >
              <span className={styles.alimIconWrap} aria-hidden="true">
                <img
                  className={styles.alimIconImage}
                  src={alim.alimIconMimeType && alim.alimIconData
                    ? `data:${alim.alimIconMimeType};base64,${alim.alimIconData}`
                    : "/img/icons/noti-DEFAULT.svg"}
                  alt=""
                  onError={(event) => {
                    // 조인된 아이콘 데이터가 손상되었으면 반복 처리를 막고 정적 기본 아이콘으로 대체한다.
                    event.currentTarget.onerror = null;
                    event.currentTarget.src = "/img/icons/noti-DEFAULT.svg";
                  }}
                />
              </span>
              <span className={styles.itemText}>
                <span className={styles.itemContent}>{alim.alimCont}</span>
                <span className={styles.itemDate}>{alim.sendDate}</span>
              </span>
            </button>
          ))}
          <InfiniteScrollTrigger
            hasNext={hasNext}
            isLoading={isLoading || isFetchingMore}
            onLoadMore={() => void loadAlimList(nextPage)}
          >
            {message("frontend.alim.loadingMore")}
          </InfiniteScrollTrigger>
        </section>
      )}
    </main>
  );
}

export default AlimPage;
