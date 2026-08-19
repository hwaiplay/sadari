import { message } from "@/app/messages/message";
import {
  getUnreadNoticeListApi,
  type UnreadNotice,
} from "@/features/Notice/api/noticeApi";
import { useQuery } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as styles from "./UnreadNoticeSlider.css";

const NOTICE_SLIDE_INTERVAL_MS = 3200;
const EMPTY_NOTICE_LIST: readonly UnreadNotice[] = [];

/**
 * 로그인 사용자가 읽지 않은 공지사항 제목을 한 건씩 가로로 교체한다
 *
 * @author SeungHyeon.Kang
 * @return 홈 화면의 미읽음 공지사항 제목 슬라이드
 */
export function UnreadNoticeSlider() {
  // 홈과 공지 화면 전환 사이에서 같은 미읽음 목록 캐시를 재사용한다
  const unreadNoticeQuery = useQuery({
    queryKey: ["notice", "unread"],
    queryFn: getUnreadNoticeListApi,
    retry: false,
  });
  // 현재 화면에 표시할 미읽음 공지 순번을 관리한다
  const [activeIndex, setActiveIndex] = useState(0);
  // 사용자가 제목을 읽거나 조작하는 동안 자동 교체를 멈춘다
  const [isPaused, setIsPaused] = useState(false);
  const noticeList = unreadNoticeQuery.data ?? EMPTY_NOTICE_LIST;
  const activeNotice = noticeList[activeIndex] ?? noticeList[0];

  /**
   * 현재 순번 다음의 유효한 미읽음 공지 순번을 계산한다
   *
   * @author SeungHyeon.Kang
   * @param currentIndex 현재 표시 중인 공지 순번
   * @return 순환 순서의 다음 공지 순번
   */
  const getNextNoticeIndex = useCallback((currentIndex: number): number => {
    // 마지막 공지 다음에는 첫 번째 미읽음 공지로 돌아간다
    return (currentIndex + 1) % noticeList.length;
  }, [noticeList.length]);

  /**
   * 현재 미읽음 공지 다음 항목을 순환 순서로 활성화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleNoticeAdvance = useCallback((): void => {
    // 최신 목록 길이를 기준으로 다음 공지 순번을 화면 상태에 반영한다
    setActiveIndex(getNextNoticeIndex);
  }, [getNextNoticeIndex]);

  /**
   * 사용자 상호작용이 없을 때 다음 미읽음 공지 교체 예약을 생성한다
   *
   * @author SeungHyeon.Kang
   * @return 예약된 타이머를 해제할 정리 함수 또는 미예약 상태
   */
  const setNoticeSlideTimer = useCallback((): (() => void) | undefined => {
    // 미읽음 공지가 하나뿐이거나 사용자가 조작 중이면 자동 교체하지 않는다
    if (noticeList.length <= 1 || isPaused) {
      // 타이머를 만들지 않은 Effect 정리 상태를 반환한다
      return undefined;
    }

    // 현재 제목을 읽을 시간을 제공한 뒤 다음 미읽음 공지로 교체한다
    const timerId = window.setTimeout(
      handleNoticeAdvance,
      NOTICE_SLIDE_INTERVAL_MS,
    );

    /**
     * 미읽음 공지가 바뀌거나 컴포넌트가 해제될 때 기존 교체 예약을 취소한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function clearNoticeSlideTimer(): void {
      // 해제된 홈 화면에서 공지 순번을 변경하지 않도록 예약을 제거한다
      window.clearTimeout(timerId);
    }

    // 다음 Effect 실행 전에 현재 공지 교체 예약을 정리할 함수를 반환한다
    return clearNoticeSlideTimer;
  }, [handleNoticeAdvance, isPaused, noticeList.length]);

  // 활성 공지와 사용자 조작 상태에 맞는 다음 교체 예약을 관리한다
  useEffect(setNoticeSlideTimer, [setNoticeSlideTimer]);

  /**
   * 새 미읽음 공지 목록을 받으면 첫 번째 공지부터 다시 표시한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const resetActiveNotice = useCallback((): void => {
    // 상세 조회로 목록이 줄어든 경우에도 유효한 첫 항목부터 표시한다
    setActiveIndex(0);
  }, [noticeList]);

  // 서버의 미읽음 공지 목록이 바뀌면 첫 번째 제목으로 표시 순서를 초기화한다
  useEffect(resetActiveNotice, [resetActiveNotice]);

  /**
   * 마우스와 키보드로 현재 공지를 조작하는 동안 자동 교체를 멈춘다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function handleNoticePause(): void {
    // 사용자가 읽고 선택할 현재 공지 제목을 고정한다
    setIsPaused(true);
  }

  /**
   * 현재 공지 조작이 끝나면 다음 제목 자동 교체를 재개한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function handleNoticeResume(): void {
    // 전체 표시 간격을 다시 제공한 뒤 다음 공지가 나오도록 재개 상태를 설정한다
    setIsPaused(false);
  }

  // 조회 중이거나 실패했거나 미읽음 공지가 없으면 홈의 기존 영역을 유지한다
  if (unreadNoticeQuery.isPending || unreadNoticeQuery.isError || activeNotice === undefined) {
    // 표시할 제목이 없는 미읽음 공지 슬라이드를 렌더링하지 않는다
    return null;
  }

  // "공지사항 \"{0}\" 보기"
  const noticeActionLabel = message("frontend.home.notice.action", [activeNotice.notiTitl]);

  // 현재 미읽음 공지 제목 한 건과 상세 이동을 제공하는 가로 슬라이드를 반환한다
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
      {/* 현재 미읽음 공지 제목이 오른쪽에서 왼쪽으로 교체되는 영역 */}
      <div className={styles.viewport} aria-live="off">
        <Link
          key={`${activeNotice.notiNumb}-${activeIndex}`}
          className={styles.noticeLink}
          to={`/notice/list/${activeNotice.notiNumb}`}
          aria-label={noticeActionLabel}
        >
          {activeNotice.notiTitl}
        </Link>
      </div>
    </section>
  );
}
