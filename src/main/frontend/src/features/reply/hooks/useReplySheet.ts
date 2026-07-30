/**
 * fileName       : useReplySheet
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 댓글 바텀시트의 닫기와 드래그 상호작용을 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        HanWon.Jang        최초 생성
 */
import { useEffect, useRef, useState } from "react";
import type {
  CSSProperties,
  KeyboardEvent as ReactKeyboardEvent,
  PointerEvent,
} from "react";
import { useBodyScrollLock } from "@/app/utils/modalUtil";

const CLOSE_DISTANCE = 96;
const CLOSE_VELOCITY = 0.55;
const CLOSE_ANIMATION_MS = 180;
const QUICK_DRAG_DISTANCE = 24;

type DragStart = {
  y: number;
  time: number;
  pointerId: number;
};

type UseReplySheetProps = {
  onClose: () => void;
};

/**
 * 댓글 바텀시트의 닫기와 포인터 드래그 상태를 제공한다
 *
 * @author HanWon.Jang
 * @param props 댓글 바텀시트 닫기 처리 정보
 * @return 댓글 바텀시트가 사용할 참조와 이벤트 처리 함수
 */
export function useReplySheet({ onClose }: UseReplySheetProps) {
  // 댓글 바텀시트가 열린 동안 배경 페이지의 스크롤을 잠근다
  useBodyScrollLock(true);

  const sheetRef = useRef<HTMLElement>(null);
  const dragStartRef = useRef<DragStart>({
    y: 0,
    time: 0,
    pointerId: -1,
  });
  const closeTimerRef = useRef<number | null>(null);
  const [dragOffset, setDragOffset] = useState(0);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    /**
     * Escape 키 입력 시 댓글 바텀시트를 닫는다
     *
     * @author HanWon.Jang
     * @param event 브라우저 키보드 입력 이벤트
     * @return 반환값이 없다
     */
    const handleWindowKeyDown = (event: KeyboardEvent): void => {
      // 사용자가 현재 바텀시트를 빠르게 닫을 수 있도록 Escape 키를 지원한다
      if (event.key === "Escape") {
        // 부모 화면에 댓글 바텀시트 닫기를 요청한다
        onClose();
      }
    };

    // 바텀시트가 열린 동안 전역 Escape 키 입력을 감지한다
    window.addEventListener("keydown", handleWindowKeyDown);

    // 전역 이벤트와 지연 닫기 작업을 해제하는 정리 함수를 반환한다
    return () => {
      // 바텀시트가 닫힌 뒤 전역 키 입력이 중복 처리되지 않도록 해제한다
      window.removeEventListener("keydown", handleWindowKeyDown);

      // 닫기 애니메이션 타이머가 남아 있으면 언마운트 이후 콜백 실행을 차단한다
      if (closeTimerRef.current !== null) {
        // 예약된 바텀시트 닫기 작업을 취소한다
        window.clearTimeout(closeTimerRef.current);
      }
    };
  }, [onClose]);

  /**
   * 댓글 바텀시트를 화면 아래로 이동한 뒤 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const closeWithDragAnimation = (): void => {
    // 닫기 이동에는 전환 애니메이션이 적용되도록 드래그 상태를 종료한다
    setIsDragging(false);
    // 바텀시트 높이만큼 이동하여 화면 아래로 자연스럽게 사라지게 한다
    setDragOffset(sheetRef.current?.offsetHeight ?? window.innerHeight);
    // 닫기 애니메이션이 끝난 뒤 부모 화면에서 바텀시트를 제거한다
    closeTimerRef.current = window.setTimeout(onClose, CLOSE_ANIMATION_MS);
  };

  /**
   * 기본 포인터로 댓글 바텀시트 드래그를 시작한다
   *
   * @author HanWon.Jang
   * @param event 바텀시트 손잡이 포인터 이벤트
   * @return 반환값이 없다
   */
  const handlePointerDown = (event: PointerEvent<HTMLDivElement>): void => {
    // 멀티 터치의 보조 포인터는 바텀시트 이동 기준에서 제외한다
    if (!event.isPrimary) {
      // 보조 포인터 입력을 처리하지 않고 종료한다
      return;
    }

    // 드래그 거리와 속도를 계산할 시작 위치와 시간을 저장한다
    dragStartRef.current = {
      y: event.clientY,
      time: performance.now(),
      pointerId: event.pointerId,
    };
    // 손가락을 따라 즉시 이동하도록 전환 애니메이션을 중지한다
    setIsDragging(true);
    // 손잡이 밖으로 이동한 포인터도 드래그 종료까지 계속 추적한다
    event.currentTarget.setPointerCapture(event.pointerId);
  };

  /**
   * 포인터의 세로 이동 거리를 댓글 바텀시트 위치에 반영한다
   *
   * @author HanWon.Jang
   * @param event 바텀시트 손잡이 포인터 이벤트
   * @return 반환값이 없다
   */
  const handlePointerMove = (event: PointerEvent<HTMLDivElement>): void => {
    // 활성 드래그와 동일한 포인터만 바텀시트 위치를 변경할 수 있다
    if (!isDragging
        || dragStartRef.current.pointerId !== event.pointerId) {
      // 현재 드래그와 무관한 포인터 이동을 처리하지 않고 종료한다
      return;
    }

    // 위쪽 드래그는 무시하고 아래쪽 이동 거리만 바텀시트에 반영한다
    setDragOffset(Math.max(0, event.clientY - dragStartRef.current.y));
  };

  /**
   * 드래그 거리와 속도에 따라 댓글 바텀시트를 닫거나 원위치한다
   *
   * @author HanWon.Jang
   * @param event 바텀시트 손잡이 포인터 이벤트
   * @return 반환값이 없다
   */
  const handlePointerEnd = (event: PointerEvent<HTMLDivElement>): void => {
    // 드래그를 시작한 포인터와 다른 종료 이벤트는 현재 이동에 영향을 주지 않는다
    if (dragStartRef.current.pointerId !== event.pointerId) {
      // 현재 드래그와 무관한 포인터 종료를 처리하지 않고 종료한다
      return;
    }

    const distance = Math.max(0, event.clientY - dragStartRef.current.y);
    const elapsed = Math.max(1, performance.now() - dragStartRef.current.time);
    const velocity = distance / elapsed;
    // 종료된 포인터가 이후 이동 이벤트에 재사용되지 않도록 초기화한다
    dragStartRef.current.pointerId = -1;

    // 충분히 아래로 이동했거나 짧고 빠르게 당긴 경우 사용자의 닫기 의도로 판단한다
    if (distance >= CLOSE_DISTANCE
        || (distance >= QUICK_DRAG_DISTANCE && velocity >= CLOSE_VELOCITY)) {
      // 사용자의 드래그 종료 위치에서 닫기 애니메이션을 이어서 실행한다
      closeWithDragAnimation();
      // 원위치 복원 상태가 덮어쓰지 않도록 종료한다
      return;
    }

    // 닫기 기준에 미달한 드래그는 전환 애니메이션으로 원위치한다
    setIsDragging(false);
    // 댓글 바텀시트의 기본 위치로 복원한다
    setDragOffset(0);
  };

  /**
   * 키보드로 댓글 바텀시트 손잡이를 조작하면 시트를 닫는다
   *
   * @author HanWon.Jang
   * @param event 바텀시트 손잡이 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleHandleKeyDown = (
    event: ReactKeyboardEvent<HTMLDivElement>,
  ): void => {
    // 버튼 역할의 손잡이를 Enter 또는 Space로 조작할 수 있도록 한다
    if (event.key === "Enter" || event.key === " ") {
      // Space 입력으로 페이지가 스크롤되는 기본 동작을 차단한다
      event.preventDefault();
      // 키보드 조작에 따라 부모 화면에 바텀시트 닫기를 요청한다
      onClose();
    }
  };

  const sheetStyle: CSSProperties = {
    transform: dragOffset > 0
      ? `translateY(${dragOffset}px)`
      : undefined,
    transition: isDragging
      ? "none"
      : `transform ${CLOSE_ANIMATION_MS}ms ease-out`,
  };

  // 댓글 바텀시트 UI가 사용할 참조와 상호작용 속성을 반환한다
  return {
    sheetRef,
    sheetStyle,
    handleHandleKeyDown,
    handlePointerDown,
    handlePointerMove,
    handlePointerEnd,
  };
}
