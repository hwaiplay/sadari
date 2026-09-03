import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type RefCallback,
} from "react";

type StickySearchState = {
  isSticky: boolean;
  sentinelRef: RefCallback<HTMLSpanElement>;
};

/**
 * 검색 영역 앞의 경계가 화면 상단을 통과했는지 관찰하여 실제 고정 상태를 제공함
 *
 * @author SeungHyeon.Kang
 * @return 검색 영역 고정 여부와 경계 요소 참조 함수
 */
export function useStickySearch(): StickySearchState {

  const observerRef = useRef<IntersectionObserver | null>(null);
  const [isSticky, setIsSticky] = useState(false);

  /**
   * 검색 영역 경계의 화면 위치를 사용하여 실제 고정 여부를 갱신함
   *
   * @author SeungHyeon.Kang
   * @param entries 검색 영역 경계의 교차 상태 목록
   * @return 반환값이 없음
   */
  const handleIntersection = useCallback(
    (entries: IntersectionObserverEntry[]): void => {

      const entry = entries[0];

      // 경계 관찰 결과가 없으면 기존 고정 상태를 유지함
      if (!entry) {
        // 불완전한 관찰 결과의 처리를 종료함
        return;
      }

      const nextIsSticky = !entry.isIntersecting
        && entry.boundingClientRect.top < 0;

      // 실제 경계 위치로 계산한 검색 영역 고정 상태를 반영함
      setIsSticky(nextIsSticky);
    },
    [],
  );

  /**
   * 현재 화면에 표시된 검색 영역 경계를 새 관찰 대상으로 연결함
   *
   * @author SeungHyeon.Kang
   * @param sentinel 검색 영역 바로 앞의 경계 요소
   * @return 반환값이 없음
   */
  const sentinelRef = useCallback(
    (sentinel: HTMLSpanElement | null): void => {

      // 화면 전환 전의 경계 관찰을 먼저 종료함
      observerRef.current?.disconnect();
      // 해제된 관찰자가 다시 사용되지 않도록 참조를 초기화함
      observerRef.current = null;

      // 검색 영역 경계가 아직 표시되지 않았으면 새 관찰을 보류함
      if (!sentinel) {
        // 경계가 없는 화면에서는 검색 영역 그림자를 표시하지 않음
        setIsSticky(false);
        // 새 경계가 연결될 때까지 처리를 종료함
        return;
      }

      // 화면 상단 통과 여부를 감지할 검색 영역 경계 관찰자를 생성함
      const observer = new IntersectionObserver(handleIntersection, {
        threshold: 0,
      });
      // 현재 화면의 검색 영역 경계를 관찰함
      observer.observe(sentinel);
      // 화면 종료 시 같은 관찰자를 정리할 수 있도록 참조를 저장함
      observerRef.current = observer;
    },
    [handleIntersection],
  );

  useEffect(() => {

    /**
     * 검색 화면 종료 시 남아 있는 경계 관찰을 해제함
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없음
     */
    const disconnectObserver = (): void => {

      // 더 이상 표시하지 않는 검색 영역의 관찰을 종료함
      observerRef.current?.disconnect();
    };

    // 컴포넌트가 해제될 때 검색 영역 관찰자를 정리함
    return disconnectObserver;
  }, []);

  // 검색 영역이 공통 고정 스타일을 선택할 수 있도록 관찰 상태를 반환함
  return { isSticky, sentinelRef };
}
