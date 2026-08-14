import type { ReactNode } from "react";
import { useEffect, useRef } from "react";
import * as styles from "./InfiniteScrollTrigger.css";

type InfiniteScrollTriggerProps = {
  hasNext: boolean;
  isLoading?: boolean;
  onLoadMore: () => void;
  children?: ReactNode;
};

/**
 * 목록 하단 감지 영역이 보이면 다음 10개 항목 조회 또는 노출을 요청한다.
 *
 * @author SeungHyeon.Kang
 * @param props 다음 항목 존재 여부와 로딩 상태 및 추가 노출 처리 함수
 * @return 자동 더보기 감지 영역
 */
function InfiniteScrollTrigger({
  hasNext,
  isLoading = false,
  onLoadMore,
  children,
}: InfiniteScrollTriggerProps) {

  const targetRef = useRef<HTMLDivElement | null>(null);
  const onLoadMoreRef = useRef(onLoadMore);

  useEffect(() => {

    // 감지 콜백이 최신 목록 상태를 사용하도록 현재 처리 함수를 보관한다.
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  useEffect(() => {

    const target = targetRef.current;

    // 다음 항목이 없거나 조회 중이면 중복 감지를 시작하지 않는다.
    if (!target || !hasNext || isLoading) {
      return;
    }

    // 목록의 실제 스크롤 컨테이너에 의해 가려지는 영역까지 반영하는 하단 감지기를 생성한다.
    const observer = new IntersectionObserver((entries) => {

      const [entry] = entries;

      // 사용자가 목록 하단까지 이동했을 때만 다음 항목을 요청한다.
      if (entry?.isIntersecting) {
        onLoadMoreRef.current();
      }
    });

    observer.observe(target);

    return () => {

      // 화면 전환이나 목록 상태 변경 시 기존 감지기를 해제한다.
      observer.disconnect();
    };
  }, [hasNext, isLoading]);

  // 다음 항목이 있을 때만 감지 영역과 선택적 로딩 문구를 반환한다.
  return hasNext ? (
    <div className={styles.trigger} ref={targetRef} aria-live="polite">
      {isLoading ? children : null}
    </div>
  ) : null;
}

export default InfiniteScrollTrigger;
