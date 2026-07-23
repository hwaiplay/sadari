import { useEffect } from "react";

let scrollLockCount = 0;
let originalOverflow = "";
let originalPaddingRight = "";
let originalScrollbarCompensation = "";

/**
 * 현재 열린 모달 개수를 기준으로 body 스크롤을 잠급니다.
 * 여러 알럿이나 커스텀 모달이 겹쳐 열려도 마지막 모달이 닫힐 때만 원래 스크롤 상태를 복구합니다.
 *
 * @author Hanwon.Jang
 * @return
 */
export function lockBodyScroll() {
  if (scrollLockCount === 0) {
    const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;

    originalOverflow = document.body.style.overflow;
    originalPaddingRight = document.body.style.paddingRight;
    originalScrollbarCompensation = document.documentElement.style.getPropertyValue(
      "--sadari-scrollbar-compensation",
    );
    document.body.style.overflow = "hidden";

    if (scrollbarWidth > 0) {
      document.body.style.paddingRight = `${scrollbarWidth}px`;
      // fixed 네비게이션은 body padding 영향을 받지 않으므로 별도 CSS 변수로 같은 폭을 보정한다.
      document.documentElement.style.setProperty(
        "--sadari-scrollbar-compensation",
        `${scrollbarWidth}px`,
      );
    }
  }

  scrollLockCount += 1;
}

/**
 * body 스크롤 잠금을 해제합니다.
 * 중첩 모달이 남아 있는 동안에는 배경 스크롤이 다시 열리지 않도록 잠금 개수를 먼저 차감합니다.
 *
 * @author Hanwon.Jang
 * @return
 */
export function unlockBodyScroll() {
  scrollLockCount = Math.max(0, scrollLockCount - 1);

  if (scrollLockCount === 0) {
    document.body.style.overflow = originalOverflow;
    document.body.style.paddingRight = originalPaddingRight;

    if (originalScrollbarCompensation) {
      document.documentElement.style.setProperty(
        "--sadari-scrollbar-compensation",
        originalScrollbarCompensation,
      );
    } else {
      document.documentElement.style.removeProperty("--sadari-scrollbar-compensation");
    }
  }
}

/**
 * React 커스텀 모달이 열린 동안 body 스크롤을 잠그는 공통 hook입니다.
 * 달력, selectBox처럼 자체 위치와 스크롤 동작이 필요한 컴포넌트는 이 hook을 사용하지 않습니다.
 *
 * @author Hanwon.Jang
 * @param isLocked 스크롤 잠금 적용 여부
 * @return
 */
export function useBodyScrollLock(isLocked: boolean) {
  useEffect(() => {
    if (!isLocked) {
      return undefined;
    }

    lockBodyScroll();
    return () => {
      unlockBodyScroll();
    };
  }, [isLocked]);
}
