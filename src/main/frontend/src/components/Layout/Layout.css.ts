import { keyframes, style } from "@vanilla-extract/css";

const enterFromRight = keyframes({
  "0%": {
    opacity: 0.92,
    transform: "translateX(32px)",
  },
  "100%": {
    opacity: 1,
    transform: "translateX(0)",
  },
});

const enterFromLeft = keyframes({
  "0%": {
    opacity: 0.92,
    transform: "translateX(-32px)",
  },
  "100%": {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const pageTransitionViewport = style({
  // hidden은 세로축까지 스크롤 컨테이너로 만들어 하위 sticky 요소를 방해함
  overflowX: "clip",
});

export const pageTransitionBase = style({
  width: "100%",
  willChange: "transform, opacity",
});

export const pageTransitionForward = style({
  // ease-out 대신 고급스러운 베지에 곡선 적용
  animation: `${enterFromRight} 400ms cubic-bezier(0.25, 1, 0.5, 1) both`,
});

export const pageTransitionBack = style({
  animation: `${enterFromLeft} 400ms cubic-bezier(0.25, 1, 0.5, 1) both`,
});
