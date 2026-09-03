import { message } from "@/app/messages/message";
import {
  getUnreadNoticeListApi,
  type UnreadNotice,
} from "@/features/Notice/api/noticeApi";
import { NoticeCategoryBadge } from "@/features/Notice/components/NoticeCategoryBadge";
import { useQuery } from "@tanstack/react-query";
import {
  type AnimationEvent,
  type CSSProperties,
  useCallback,
  useEffect,
  useState,
} from "react";
import { Link } from "react-router-dom";
import * as styles from "./UnreadNoticeSlider.css";

const MIN_MARQUEE_DURATION_MS = 6000;
const MARQUEE_CHARACTER_DURATION_MS = 220;
const NOTICE_LEFT_EDGE_HOLD_MS = 3000;
const NOTICE_END_HOLD_MS = 3000;
const EMPTY_NOTICE_LIST: readonly UnreadNotice[] = [];

/**
 * 공지 제목 길이에 비례한 가로 슬라이드 시간을 계산함
 *
 * @author SeungHyeon.Kang
 * @param notice 슬라이드로 표시할 미읽음 공지
 * @return 한 번의 가로 슬라이드에 사용할 시간
 */
function getMarqueeDuration(notice: UnreadNotice): number {

  // 고정 카테고리를 제외한 제목 길이를 기준으로 읽기 쉬운 이동 속도를 유지함
  const contentLength = notice.notiTitl.length;

  // 짧은 제목도 너무 빠르게 지나가지 않도록 최소 표시 시간을 보장함
  return Math.max(
    MIN_MARQUEE_DURATION_MS,
    contentLength * MARQUEE_CHARACTER_DURATION_MS,
  );
}

/**
 * 로그인 사용자가 읽지 않은 공지사항 제목을 한 건씩 교체해 보여줌
 *
 * @author SeungHyeon.Kang
 * @return 홈 화면의 미읽음 공지사항 안내
 */
export function UnreadNoticeSlider() {
  // 홈과 공지 화면 전환 사이에서 같은 미읽음 목록 캐시를 재사용함
  const unreadNoticeQuery = useQuery({
    queryKey: ["notice", "unread"],
    queryFn: getUnreadNoticeListApi,
    retry: false,
  });
  // 현재 화면에 표시할 미읽음 공지 순번을 관리함
  const [activeIndex, setActiveIndex] = useState(0);
  // 사용자가 제목을 읽거나 조작하는 동안 자동 교체를 멈춤
  const [isPaused, setIsPaused] = useState(false);
  // 한 건 공지의 이어 붙인 제목이 왼쪽 경계에 도달한 뒤 다음 순환 전까지 대기하는 상태를 관리함
  const [isSingleNoticeHolding, setIsSingleNoticeHolding] = useState(false);
  // 다건 공지의 마지막 글자가 화면에 들어온 뒤 대기하는 상태를 관리함
  const [isMarqueeDone, setIsMarqueeDone] = useState(false);
  const noticeList = unreadNoticeQuery.data ?? EMPTY_NOTICE_LIST;
  const activeNotice = noticeList[activeIndex] ?? noticeList[0];

  /**
   * 현재 순번 다음의 유효한 미읽음 공지 순번을 계산함
   *
   * @author SeungHyeon.Kang
   * @param currentIndex 현재 표시 중인 공지 순번
   * @return 순환 순서의 다음 공지 순번
   */
  const getNextNoticeIndex = useCallback((currentIndex: number): number => {
    // 마지막 공지 다음에는 첫 번째 미읽음 공지로 돌아감
    return (currentIndex + 1) % noticeList.length;
  }, [noticeList.length]);

  /**
   * 현재 미읽음 공지 다음 항목을 순환 순서로 활성화함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleNoticeAdvance = useCallback((): void => {
    // 다음 공지는 처음 위치에서 가로 이동을 시작하도록 완료 상태를 초기화함
    setIsMarqueeDone(false);
    // 최신 목록 길이를 기준으로 다음 공지 순번을 화면 상태에 반영함
    setActiveIndex(getNextNoticeIndex);
  }, [getNextNoticeIndex]);

  /**
   * 제목 끝부분을 확인할 시간을 제공한 뒤 다음 공지 전환 예약을 생성함
   *
   * @author SeungHyeon.Kang
   * @return 예약된 타이머를 해제할 정리 함수 또는 미예약 상태
   */
  const setNoticeHoldTimer = useCallback((): (() => void) | undefined => {
    // 가로 이동 중이거나 사용자가 공지를 조작하면 다음 공지 전환을 예약하지 않음
    if (!isMarqueeDone || isPaused || noticeList.length <= 1) {
      // 전환 타이머를 만들지 않은 Effect 정리 상태를 반환함
      return undefined;
    }

    // 제목 끝부분이 보이는 현재 위치를 3초 유지한 뒤 다음 공지로 전환함
    const timerId = window.setTimeout(handleNoticeAdvance, NOTICE_END_HOLD_MS);

    /**
     * 공지 상태가 바뀌거나 컴포넌트가 해제되면 기존 전환 예약을 취소함
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없음
     */
    function clearNoticeHoldTimer(): void {
      // 이전 공지의 대기 시간이 다음 공지 전환에 영향을 주지 않도록 예약을 제거함
      window.clearTimeout(timerId);
    }

    // 다음 Effect 실행 전에 현재 공지 전환 예약을 정리할 함수를 반환함
    return clearNoticeHoldTimer;
  }, [handleNoticeAdvance, isMarqueeDone, isPaused, noticeList.length]);

  // 제목 끝부분 노출과 사용자 조작 상태에 맞는 다음 공지 전환 예약을 관리함
  useEffect(setNoticeHoldTimer, [setNoticeHoldTimer]);

  /**
   * 한 건 공지의 이어 붙인 제목이 왼쪽 경계에 도달하면 3초 뒤 가로 순환을 재개함
   *
   * @author SeungHyeon.Kang
   * @return 예약된 타이머를 해제할 정리 함수 또는 미예약 상태
   */
  const setSingleNoticeHoldTimer = useCallback((): (() => void) | undefined => {
    // 한 건 공지의 반복 경계에서만 다음 순환 재개를 예약함
    if (!isSingleNoticeHolding || noticeList.length !== 1) {
      // 순환 재개 타이머를 만들지 않은 Effect 정리 상태를 반환함
      return undefined;
    }

    /**
     * 왼쪽 경계에서 제공한 확인 시간이 끝나면 한 건 공지의 가로 순환을 재개함
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없음
     */
    function releaseSingleNoticeHold(): void {
      // 다음 회차가 왼쪽 경계에서 출발할 수 있도록 반복 대기 상태를 해제함
      setIsSingleNoticeHolding(false);
    }

    // 이어 붙인 제목이 왼쪽 경계에 걸린 상태를 3초 유지한 뒤 다음 가로 순환을 재개함
    const timerId = window.setTimeout(releaseSingleNoticeHold, NOTICE_LEFT_EDGE_HOLD_MS);

    /**
     * 공지 상태가 바뀌거나 컴포넌트가 해제되면 기존 순환 재개 예약을 취소함
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없음
     */
    function clearSingleNoticeHoldTimer(): void {
      // 이전 공지의 대기 시간이 새 공지의 가로 순환에 영향을 주지 않도록 예약을 제거함
      window.clearTimeout(timerId);
    }

    // 다음 Effect 실행 전에 한 건 공지의 순환 재개 예약을 정리할 함수를 반환함
    return clearSingleNoticeHoldTimer;
  }, [isSingleNoticeHolding, noticeList.length]);

  // 한 건 공지의 이어 붙인 제목이 왼쪽 경계에 도달한 시점부터 3초 대기 시간을 관리함
  useEffect(setSingleNoticeHoldTimer, [setSingleNoticeHoldTimer]);

  /**
   * 새 미읽음 공지 목록을 받으면 첫 번째 공지부터 다시 표시함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const resetActiveNotice = useCallback((): void => {
    // 새 목록의 첫 공지가 가로 이동을 완료하지 않은 상태로 표시되도록 초기화함
    setIsMarqueeDone(false);
    // 새 목록은 첫 공지의 최초 표시 대기부터 시작하도록 반복 경계 대기 상태를 초기화함
    setIsSingleNoticeHolding(false);
    // 상세 조회로 목록이 줄어든 경우에도 유효한 첫 항목부터 표시함
    setActiveIndex(0);
  }, [noticeList]);

  // 서버의 미읽음 공지 목록이 바뀌면 첫 번째 제목으로 표시 순서를 초기화함
  useEffect(resetActiveNotice, [resetActiveNotice]);

  /**
   * 마우스와 키보드로 현재 공지를 조작하는 동안 자동 교체를 멈춤
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  function handleNoticePause(): void {
    // 사용자가 읽고 선택할 현재 공지 제목을 고정함
    setIsPaused(true);
  }

  /**
   * 현재 공지 조작이 끝나면 다음 제목 자동 교체를 재개함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  function handleNoticeResume(): void {
    // 전체 표시 간격을 다시 제공한 뒤 다음 공지가 나오도록 재개 상태를 설정함
    setIsPaused(false);
  }

  /**
   * 여러 공지 중 현재 항목의 가로 슬라이드가 끝나면 다음 공지를 활성화함
   *
   * @author SeungHyeon.Kang
   * @param event 가로 슬라이드 완료 이벤트
   * @return 반환값이 없음
   */
  function handleMarqueeEnd(event: AnimationEvent<HTMLSpanElement>): void {

    // 하위 요소의 이벤트이거나 공지가 하나뿐이면 현재 공지의 가로 반복을 유지함
    if (event.target !== event.currentTarget || noticeList.length <= 1) {
      // 다음 공지로 전환하지 않고 현재 가로 슬라이드를 계속함
      return;
    }

    // 제목 끝부분이 보이는 현재 위치에서 3초 대기를 시작하도록 완료 상태를 기록함
    setIsMarqueeDone(true);
  }

  /**
   * 한 건 공지의 이어 붙인 제목이 왼쪽 경계에 도달하면 다음 순환을 잠시 멈춤
   *
   * @author SeungHyeon.Kang
   * @param event 한 건 공지의 가로 반복 경계 이벤트
   * @return 반환값이 없음
   */
  function handleSingleMarqueeIteration(event: AnimationEvent<HTMLSpanElement>): void {

    // 하위 요소의 이벤트이거나 공지가 한 건이 아니면 반복 경계 대기를 적용하지 않음
    if (event.target !== event.currentTarget || noticeList.length !== 1) {
      // 현재 애니메이션 상태를 유지하고 반복 경계 처리를 종료함
      return;
    }

    // 이어 붙인 제목이 왼쪽 끝에 걸린 현재 위치에서 3초 대기를 시작함
    setIsSingleNoticeHolding(true);
  }

  // 조회 중이거나 실패했거나 미읽음 공지가 없으면 홈의 기존 영역을 유지함
  if (unreadNoticeQuery.isPending || unreadNoticeQuery.isError || activeNotice === undefined) {
    // 표시할 제목이 없는 미읽음 공지 안내를 렌더링하지 않음
    return null;
  }

  // "공지사항 \"{0}\" 보기"
  const noticeActionLabel = message("frontend.home.notice.action", [activeNotice.notiTitl]);
  // 현재 공지 문구 길이에 맞는 한 번의 가로 이동 시간을 계산함
  const marqueeDuration = getMarqueeDuration(activeNotice);
  const isSingleNotice = noticeList.length === 1;
  // 한 건은 끊김 없이 반복하고 여러 건은 한 번 이동한 뒤 다음 공지로 전환함
  const marqueeTrackClass = isSingleNotice
    ? `${styles.marqueeTrack} ${styles.singleMarqueeTrack}`
    : `${styles.marqueeTrack} ${styles.multipleMarqueeTrack}`;
  // 사용자 상호작용 중이거나 한 건 공지가 반복 경계에 있으면 현재 위치에서 가로 슬라이드를 멈춤
  const marqueePlayState = isPaused || isSingleNoticeHolding ? "paused" : "running";
  const marqueeStyle = {
    animationDelay: `${NOTICE_LEFT_EDGE_HOLD_MS}ms`,
    animationDuration: `${marqueeDuration}ms`,
    animationPlayState: marqueePlayState,
  } satisfies CSSProperties;

  // 현재 미읽음 공지 제목 한 건과 상세 이동을 제공하는 가로 슬라이드를 반환함
  return (
    <section
      className={styles.slider}
      aria-label={
        /* "읽지 않은 공지사항" */ message("frontend.home.notice.unreadLabel")
      }
      onPointerEnter={handleNoticePause}
      onPointerLeave={handleNoticeResume}
      onFocus={handleNoticePause}
      onBlur={handleNoticeResume}
    >
      {/* 카테고리를 고정하고 제목만 가로 순환시키는 미읽음 공지 영역 */}
      <div className={styles.viewport} aria-live="off">
        <Link
          key={`${activeNotice.notiNumb}-${activeIndex}`}
          className={styles.noticeLink}
          to={`/notice/list/${activeNotice.notiNumb}`}
          aria-label={noticeActionLabel}
        >
          {/* 현재 공지에 해당하는 고정 카테고리 영역 */}
          <span className={styles.categoryLayer}>
            <NoticeCategoryBadge categoryName={activeNotice.cateName} />
          </span>

          {/* 현재 공지 제목만 가로로 이동하는 영역 */}
          <span className={styles.marqueeViewport}>
            <span
              className={marqueeTrackClass}
              style={marqueeStyle}
              onAnimationEnd={handleMarqueeEnd}
              onAnimationIteration={handleSingleMarqueeIteration}
            >
              <span className={styles.noticeTitle}>{activeNotice.notiTitl}</span>
              {/* 한 건일 때 끝난 제목 바로 뒤에 같은 제목을 이어 붙이는 영역 */}
              {isSingleNotice && (
                <span className={styles.noticeTitle} aria-hidden="true">
                  {activeNotice.notiTitl}
                </span>
              )}
            </span>
          </span>
        </Link>
      </div>
    </section>
  );
}
