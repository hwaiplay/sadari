import { style } from "@vanilla-extract/css";

// 배경사진이 브라우저에서 완전히 로드되기 전에는 빈 이미지 요소를 숨긴다
export const imageLoading = style({
  opacity: 0,
});

// 배경사진 로드가 끝나면 원래 화면 영역에 이미지를 표시한다
export const imageLoaded = style({
  opacity: 1,
});

// 기존 배경사진 영역의 중앙에 공통 소형 회전 링을 겹쳐 표시한다
export const loadingOverlay = style({
  position: "absolute",
  inset: 0,
  zIndex: 1,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  pointerEvents: "none",
});
