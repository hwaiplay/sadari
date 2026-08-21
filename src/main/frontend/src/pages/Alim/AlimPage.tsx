import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { message } from "@/app/messages/message";
import {
  requestFirebaseToken,
  requestPushPermission,
} from "@/app/pwa/firebaseMessaging";
import { notifyFirebasePushEnabled } from "@/app/pwa/pushEvents";
import Loading from "@/components/Loading/Loading";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import {
  getMyAlimListApi,
  delAllAlimApi,
  uptAlimReadApi,
  type AlimItem,
} from "@/features/Alim/api/alimApi";
import { notifyUnreadAlimChange } from "@/features/Alim/lib/alimEvents";
import {
  delPushSubApi,
  getPushConfigApi,
  setPushSubApi,
} from "@/features/Push/api/pushApi";
import type { ReactNode } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./AlimPage.css";

const PUSH_ENABLED_STORAGE_KEY = "sadari:push-enabled";
const ALIM_DISMISS_ANIMATION_MS = 360;
const ALIM_DISMISS_STAGGER_MS = 30;
const ALIM_DISMISS_MAX_STAGGER_COUNT = 10;

/**
 * 별도 상태 조회 API 없이 버튼 상태를 유지하기 위해 현재 브라우저에 마지막 토글 결과를 저장합니다.
 * 저장값이 아직 없는 기존 사용자는 브라우저 알림 권한이 허용돼 있으면 켜짐 상태로 시작합니다.
 *
 * @author HanWon.Jang
 * @return 현재 브라우저에서 기억한 푸시 알림 활성 여부
 */
function getInitialPushEnabled() {

  if (!("Notification" in window) || Notification.permission !== "granted") {
    return false;
  }

  const storedStatus = window.localStorage.getItem(PUSH_ENABLED_STORAGE_KEY);
  return storedStatus === null ? true : storedStatus === "Y";
}

/**
 * 서버의 USEE_YSNO 변경이 성공한 뒤 버튼 상태를 현재 브라우저에 보관합니다.
 *
 * @author HanWon.Jang
 * @param enabled 푸시 알림 활성 여부
 */
function setStoredPushEnabled(enabled: boolean) {

  window.localStorage.setItem(PUSH_ENABLED_STORAGE_KEY, enabled ? "Y" : "N");
}

/**
 * 푸시 알림의 현재 상태와 변경 진행 여부에 맞는 버튼 스타일을 조회한다
 *
 * @author HanWon.Jang
 * @param isPushChanging 푸시 알림 변경 처리 진행 여부
 * @param isPushEnabled 푸시 알림 활성 여부
 * @return 푸시 알림 버튼 스타일 클래스
 */
function getPushButtonClass(isPushChanging: boolean, isPushEnabled: boolean): string {
  // 토큰 발급 또는 서버 등록 중에는 기존 활성 상태보다 처리 중 상태를 우선 표시한다
  if (isPushChanging) {
    // 노란색 처리 중 버튼 스타일을 반환한다
    return styles.pushButtonChanging;
  }

  // 처리 완료 후에는 현재 푸시 알림 활성 여부에 맞는 상태색을 표시한다
  if (isPushEnabled) {
    // 푸시 알림 켜짐 버튼 스타일을 반환한다
    return styles.pushButton;
  }

  // 푸시 알림 꺼짐 버튼 스타일을 반환한다
  return styles.pushButtonOff;
}

/**
 * 푸시 알림의 현재 상태와 변경 진행 여부를 버튼 내용으로 구성한다
 *
 * @author HanWon.Jang
 * @param isPushChanging 푸시 알림 변경 처리 진행 여부
 * @param isPushEnabled 푸시 알림 활성 여부
 * @return 푸시 알림 상태 문구와 처리 중 표시
 */
function renderPushButtonContent(isPushChanging: boolean, isPushEnabled: boolean): ReactNode {
  // 처리 중에는 스피너와 함께 켜기 또는 끄기 작업에 맞는 진행 문구를 표시한다
  if (isPushChanging) {
    // 켜진 상태를 해제하는 동안에는 사용자가 현재 작업을 구분할 수 있게 해제 문구를 표시한다
    if (isPushEnabled) {
      // 푸시 알림 해제 진행 상태를 반환한다
      return (
        <>
          <span className={styles.pushSpinner} aria-hidden="true" />
          {/* "푸시 알림 해제 중..." */}
          {message("frontend.push.changing.disable")}
        </>
      );
    }

    // 푸시 알림 설정 진행 상태를 반환한다
    return (
      <>
        <span className={styles.pushSpinner} aria-hidden="true" />
        {/* "푸시 알림 설정 중..." */}
        {message("frontend.push.changing.enable")}
      </>
    );
  }

  // 처리 중이 아니면 실제 푸시 알림 활성 여부를 버튼에 표시한다
  if (isPushEnabled) {
    // 푸시 알림 켜짐 상태를 반환한다
    return (
      <>
        {/* "푸시 알림 켜짐" */}
        {message("frontend.push.enable")}
      </>
    );
  }

  // 푸시 알림 꺼짐 상태를 반환한다
  return (
    <>
      {/* "푸시 알림 꺼짐" */}
      {message("frontend.push.disable")}
    </>
  );
}

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
  const [isPushEnabled, setIsPushEnabled] = useState(getInitialPushEnabled);
  const [isPushChanging, setIsPushChanging] = useState(false);
  const pushTokenRef = useRef<string | null>(null);
  const dismissTimerRef = useRef<number | null>(null);

  // 변경 진행 여부를 우선 적용하여 푸시 알림 버튼의 현재 스타일을 조회한다
  const pushButtonClass = getPushButtonClass(isPushChanging, isPushEnabled);

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
   * get Current Push Token 정보를 조회한다
   *
   * @author HanWon.Jang
   * @return 처리 결과
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const getCurrentPushToken = async () => {

    if (pushTokenRef.current) {
      return pushTokenRef.current;
    }

    const configResponse = await getPushConfigApi();
    const token = pushTokenRef.current
      ?? await requestFirebaseToken(configResponse.data);

    pushTokenRef.current = token;
    return token;
  };

  /**
   * handle Push Toggle 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handlePushToggle = async () => {

    if (isPushChanging) {
      return;
    }

    const wasPushEnabled = isPushEnabled;
    setIsPushChanging(true);

    /**
     * 현재 푸시 알림 상태에 맞춰 브라우저 토큰을 서버에 등록하거나 비활성화한다
     *
     * @author SeungHyeon.Kang
     * @return 푸시 알림 설정 변경 완료 Promise
     * @throws 권한 요청과 Firebase Token 또는 구독 API 처리가 실패할 때 발생
     */
    const changePushSetting = async (): Promise<void> => {
      // 켜짐 상태에서 다시 누르면 현재 브라우저 token만 비활성화하고 버튼을 꺼짐 상태로 전환한다.
      if (wasPushEnabled) {
        const token = await getCurrentPushToken();
        await delPushSubApi({ endpUrlx: token });
        setIsPushEnabled(false);
        setStoredPushEnabled(false);
        // 푸시 알림 해제 작업이 끝났으므로 설정 변경 처리를 종료한다
        return;
      }

      // 브라우저 권한 요청은 버튼 클릭 직후 실행해야 팝업이 차단되지 않는다.
      // Firebase 설정 API를 기다린 뒤 요청하면 사용자 액션으로 인정되지 않아 컨펌창이 뜨지 않을 수 있다.
      await requestPushPermission();

      const token = await getCurrentPushToken();

      // TB_PSHSUB.ENDP_URLX는 현재 FCM registration token 저장 위치로 사용한다.
      // 서버는 인증 사용자 번호를 직접 채우므로 프론트에서는 token만 전달한다.
      await setPushSubApi({ endpUrlx: token });
      setIsPushEnabled(true);
      setStoredPushEnabled(true);
      notifyFirebasePushEnabled();
    };

    try {
      // Firebase 권한 요청부터 서버 구독 변경 완료까지 버튼 없는 모달과 화면 이동 차단을 유지한다
      await runBlockingOperation(changePushSetting, {
        // "푸시 알림 해제 중..." 또는 "푸시 알림 설정 중..."
        title: message(
          wasPushEnabled
            ? "frontend.push.changing.disable"
            : "frontend.push.changing.enable",
        ),
        success: {
          // "푸시 알림이 꺼졌습니다." 또는 "푸시 알림이 켜졌습니다."
          title: message(
            wasPushEnabled
              ? "frontend.push.disable.successTitle"
              : "frontend.push.enable.successTitle",
          ),
        },
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "";
      const detailMessage =
        errorMessage === "PUSH_NOT_SUPPORTED"
          ? message("frontend.push.enable.unsupported")
          : errorMessage === "PUSH_INSECURE_CONTEXT"
            ? message("frontend.push.enable.insecureContext")
          : errorMessage === "PUSH_PERMISSION_DENIED"
            ? message("frontend.push.enable.denied")
            : errorMessage === "PUSH_PERMISSION_REQUIRED"
              ? message("frontend.push.enable.denied")
            : errorMessage === "PUSH_SERVICE_WORKER_NOT_READY"
              ? message("frontend.push.enable.serviceWorkerNotReady")
            : getApiErrorMessage(error, message("frontend.common.tryAgain"));

      // "푸시 알림 설정에 실패했습니다."
      // "푸시 알림 해제에 실패했습니다."
      void sweetError(
        message(
          wasPushEnabled
            ? "frontend.push.disable.failedTitle"
            : "frontend.push.enable.failedTitle",
        ),
        detailMessage,
      );
    } finally {
      setIsPushChanging(false);
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
      const response = await uptAlimReadApi(alim.alimNumb);
      notifyUnreadAlimChange(response.data?.unreadCnt ?? 0);
      // 읽은 알림도 알림센터에 유지하므로 제거하지 않고 상태만 바꾸어 어두운 스타일을 즉시 적용한다.
      setAlimList((prevList) => (
        prevList.map((item) => (
          item.alimNumb === alim.alimNumb
            ? { ...item, readYsno: "Y" }
            : item
        ))
      ));
      navigate(alim.linkUrlx);
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
            className={pushButtonClass}
            type="button"
            aria-pressed={isPushEnabled}
            aria-busy={isPushChanging}
            aria-live="polite"
            disabled={isPushChanging}
            onClick={() => void handlePushToggle()}
          >
            {/* 푸시 알림 현재 상태와 처리 진행 표시 영역 */}
            {renderPushButtonContent(isPushChanging, isPushEnabled)}
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
