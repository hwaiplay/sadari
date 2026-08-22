import { useEffect, useRef, type RefObject } from "react";

type HeaderOffsetChangeHandler = (headerOffset: number) => void;

type ScrollHeaderState = {
  headerRef: RefObject<HTMLElement | null>;
};

/**
 * 스크롤 이동량만큼 헤더를 연속적으로 이동할 수 있는 위치 상태를 제공한다.
 *
 * @author SeungHyeon.Kang
 * @param onOffsetChange 헤더 이동 거리 변경 전달 함수
 * @param resetKey 헤더 위치를 초기화할 화면 식별값
 * @return 헤더 요소 참조
 */
export function useScrollHeader(
  onOffsetChange?: HeaderOffsetChangeHandler,
  resetKey?: string,
): ScrollHeaderState {

  const headerRef = useRef<HTMLElement | null>(null);
  const lastScrollYRef = useRef(0);
  const headerOffsetRef = useRef(0);

  useEffect(() => {

    // 화면이 바뀌면 새 화면의 헤더를 완전히 표시한 위치에서 시작한다
    headerOffsetRef.current = 0;
    // 헤더 요소의 이동 스타일을 초기화한다
    headerRef.current?.style.setProperty(
      "transform",
      "translate3d(0, 0, 0)",
    );
    // 헤더 아래 고정 영역도 초기 위치로 되돌린다
    onOffsetChange?.(0);
    // 현재 스크롤 위치부터 이후 이동량을 계산한다
    lastScrollYRef.current = Math.max(0, window.scrollY);
    // 스크롤 중 레이아웃을 반복 측정하지 않도록 고정 헤더 높이를 저장한다
    const headerHeight = headerRef.current?.offsetHeight ?? 0;

    /**
     * 실제 스크롤 이동량을 헤더 높이 범위 안에서 누적한다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const handleScroll = (): void => {

      const currentScrollY = Math.max(0, window.scrollY);
      const scrollDiff = currentScrollY - lastScrollYRef.current;
      const nextHeaderOffset = currentScrollY === 0
        ? 0
        : Math.min(
          headerHeight,
          Math.max(0, headerOffsetRef.current + scrollDiff),
        );

      // 다음 스크롤 이벤트가 현재 위치를 기준으로 이동량을 계산하게 저장한다
      lastScrollYRef.current = currentScrollY;

      // 헤더 위치가 그대로면 불필요한 화면 상태 갱신을 생략한다
      if (nextHeaderOffset === headerOffsetRef.current) {
        // 현재 헤더 위치를 유지하고 처리를 종료한다
        return;
      }

      // 다음 스크롤 이동량 계산에 사용할 헤더 위치를 저장한다
      headerOffsetRef.current = nextHeaderOffset;
      // 본문을 다시 렌더링하지 않고 헤더를 스크롤과 같은 거리만큼 이동한다
      headerRef.current?.style.setProperty(
        "transform",
        `translate3d(0, -${nextHeaderOffset}px, 0)`,
      );
      // 헤더 아래 고정 영역이 같은 속도로 이동하도록 현재 거리를 전달한다
      onOffsetChange?.(nextHeaderOffset);
    };

    // 스크롤 흐름을 막지 않는 수신기로 헤더 위치를 동기화한다
    window.addEventListener("scroll", handleScroll, { passive: true });

    return () => {

      // 화면 종료 시 등록한 스크롤 수신기를 해제한다
      window.removeEventListener("scroll", handleScroll);
    };
  }, [onOffsetChange, resetKey]);

  // 헤더 컴포넌트가 이동에 필요한 요소 참조를 사용하도록 반환한다
  return { headerRef };
}
