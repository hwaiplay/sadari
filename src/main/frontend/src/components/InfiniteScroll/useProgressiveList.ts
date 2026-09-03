import { useCallback, useEffect, useMemo, useState } from "react";

export const INFINITE_LIST_PAGE_SIZE = 10;

type ProgressiveListResult<T> = {
  visibleItems: T[];
  hasNext: boolean;
  loadMore: () => void;
};

/**
 * 전체 조회 목록을 최초 10개부터 10개 단위로 점진적으로 노출함
 *
 * @author SeungHyeon.Kang
 * @param items 화면에 점진적으로 노출할 전체 목록
 * @param resetKey 검색 또는 필터 변경 시 최초 10개로 되돌릴 기준값
 * @return 현재 노출 목록과 다음 항목 존재 여부 및 추가 노출 함수
 */
export function useProgressiveList<T>(
  items: readonly T[],
  resetKey: string,
): ProgressiveListResult<T> {

  const [visibleCount, setVisibleCount] = useState(INFINITE_LIST_PAGE_SIZE);

  useEffect(() => {

    // 조회 조건이나 대상 목록이 바뀌면 새 결과의 최초 10개부터 표시함
    setVisibleCount(INFINITE_LIST_PAGE_SIZE);
  }, [resetKey]);

  const visibleItems = useMemo(
    () => items.slice(0, visibleCount),
    [items, visibleCount],
  );
  const hasNext = visibleCount < items.length;

  /**
   * 현재 노출 범위 다음의 최대 10개 항목을 추가로 표시함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const loadMore = useCallback((): void => {

    // 전체 목록 길이를 넘지 않는 범위에서 다음 10개까지 노출 범위를 확장함
    setVisibleCount((current) => (
      Math.min(current + INFINITE_LIST_PAGE_SIZE, items.length)
    ));
  }, [items.length]);

  // 화면이 사용할 현재 목록과 자동 더보기 상태를 반환함
  return { visibleItems, hasNext, loadMore };
}
