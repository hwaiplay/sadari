import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import type { PopularSearchKeywordType } from "@/features/Book/types/book.type";
import { useCallback, useEffect, useState } from "react";
import * as styles from "./PopularKeywordSlider.css";

const KEYWORD_SLIDE_INTERVAL_MS = 3200;

type PopularKeywordSliderProps = {
  keywordList: PopularSearchKeywordType[];
  isDisabled: boolean;
  onSelect: (keyword: string) => Promise<void>;
};

/**
 * 인기 검색어 한 건씩 세로로 교체하고 선택한 검색어를 즉시 조회함
 *
 * @author SeungHyeon.Kang
 * @param props 인기 검색어 목록과 검색 실행 상태 및 선택 처리 함수
 * @return 한 줄 세로 슬라이드 인기 검색어 영역
 */
export function PopularKeywordSlider({
  keywordList,
  isDisabled,
  onSelect,
}: PopularKeywordSliderProps) {

  const [activeIndex, setActiveIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const activeKeyword = keywordList[activeIndex] ?? keywordList[0];

  /**
   * 현재 인기 검색어 다음 항목을 순환 순서로 활성화함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleKeywordAdvance = useCallback((): void => {
    // 목록 끝 다음에는 첫 번째 검색어를 다시 표시함
    setActiveIndex((activeIndex + 1) % keywordList.length);
  }, [activeIndex, keywordList.length]);

  /**
   * 사용자 상호작용이 없을 때 다음 인기 검색어 교체 예약을 생성함
   *
   * @author SeungHyeon.Kang
   * @return 예약된 타이머를 해제할 정리 함수 또는 미예약 상태
   */
  const setKeywordSlideTimer = useCallback((): (() => void) | undefined => {
    // 검색어가 하나뿐이거나 사용자가 항목을 조작 중이면 자동 교체하지 않음
    if (keywordList.length <= 1 || isPaused) {
      // 타이머를 만들지 않은 Effect 정리 상태를 반환함
      return undefined;
    }

    // 현재 검색어를 읽을 시간을 제공한 뒤 다음 검색어로 교체함
    const timerId = window.setTimeout(
      handleKeywordAdvance,
      KEYWORD_SLIDE_INTERVAL_MS,
    );

    /**
     * 인기 검색어가 바뀌거나 컴포넌트가 해제될 때 기존 교체 예약을 취소함
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없음
     */
    function clearKeywordSlideTimer(): void {
      // 해제된 화면에서 검색어 상태를 변경하지 않도록 예약을 제거함
      window.clearTimeout(timerId);
    }

    // 다음 Effect 실행 전에 현재 검색어 교체 예약을 정리할 함수를 반환함
    return clearKeywordSlideTimer;
  }, [handleKeywordAdvance, isPaused, keywordList.length]);

  // 활성 검색어와 사용자 조작 상태에 맞는 다음 교체 예약을 관리함
  useEffect(setKeywordSlideTimer, [setKeywordSlideTimer]);

  /**
   * 새 인기 검색어 목록을 받으면 유효한 첫 번째 순위부터 다시 표시함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const resetActiveKeyword = useCallback((): void => {
    // 이전 목록 순번이 새 목록 범위를 벗어나지 않도록 첫 항목으로 초기화함
    setActiveIndex(0);
  }, [keywordList]);

  // 서버에서 인기 검색어 목록이 바뀌면 첫 번째 검색어로 표시 순서를 초기화함
  useEffect(resetActiveKeyword, [resetActiveKeyword]);

  /**
   * 마우스와 키보드로 현재 검색어를 조작하는 동안 자동 교체를 멈춤
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  function handleKeywordPause(): void {
    // 사용자가 읽고 선택할 현재 검색어를 고정함
    setIsPaused(true);
  }

  /**
   * 현재 검색어 조작이 끝나면 다음 항목 자동 교체를 재개함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  function handleKeywordResume(): void {
    // 전체 표시 간격을 다시 제공한 뒤 다음 검색어가 나오도록 재개 상태를 설정함
    setIsPaused(false);
  }

  /**
   * 현재 표시 중인 인기 검색어를 검색 입력과 첫 페이지 조회에 전달함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  function handleKeywordSelect(): void {
    // 검색 중이거나 표시할 검색어가 없으면 중복 검색을 실행하지 않음
    if (isDisabled || activeKeyword === undefined) {
      // 현재 검색 처리 또는 빈 인기 검색어 상태를 유지함
      return;
    }

    // 클릭하거나 터치한 인기 검색어로 즉시 첫 페이지 검색을 실행함
    void onSelect(activeKeyword.keyword);
  }

  // 인기 검색어가 없으면 입력창과 인기 도서 선택 사이의 기존 여백만 유지함
  if (activeKeyword === undefined) {
    // 빈 인기 검색어 영역을 렌더링하지 않음
    return null;
  }

  // "{0}(으)로 검색"
  const keywordActionLabel = message(
    "frontend.book.search.popularKeywordAction",
    [activeKeyword.keyword],
  );

  // 현재 순위의 인기 검색어 한 건과 즉시 검색 동작을 제공하는 세로 슬라이더를 반환함
  return (
    <section
      className={styles.slider}
      aria-label={
        /* "인기 검색어" */ message(
          "frontend.book.search.popularKeywordTitle",
        )
      }
      onPointerEnter={handleKeywordPause}
      onPointerLeave={handleKeywordResume}
      onFocus={handleKeywordPause}
      onBlur={handleKeywordResume}
    >
      {/* 현재 순위의 검색어 한 건이 아래에서 위로 교체되는 영역 */}
      <div className={styles.viewport} aria-live="off">
        <ActionButton
          key={`${activeKeyword.rank}-${activeKeyword.keyword}-${activeIndex}`}
          variant="secondary"
          size="sm"
          width="full"
          className={styles.keywordButton}
          type="button"
          aria-label={keywordActionLabel}
          disabled={isDisabled}
          onClick={handleKeywordSelect}
        >
          {activeKeyword.rank}. {activeKeyword.keyword}
        </ActionButton>
      </div>
    </section>
  );
}
