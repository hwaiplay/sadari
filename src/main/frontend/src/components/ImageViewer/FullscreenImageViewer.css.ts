import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const viewerFadeIn = keyframes({
  from: {
    opacity: 0,
  },
  to: {
    opacity: 1,
  },
});

export const viewer = style({
  position: "fixed",
  inset: 0,
  zIndex: 3000,
  width: "100vw",
  height: "100vh",
  minHeight: "100dvh",
  overflow: "hidden",
  backgroundColor: "rgba(12, 15, 18, 0.68)",
  backdropFilter: "blur(18px)",
  WebkitBackdropFilter: "blur(18px)",
  animation: `${viewerFadeIn} 160ms ease-out`,
});

export const originalImage = style({
  position: "relative",
  zIndex: 1,
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "contain",
  userSelect: "none",
});

// 반응 버튼 높이를 확보한 상태로 원본 사진 묶음을 화면 중앙에 배치함
export const imageViewport = style({
  position: "absolute",
  inset: 0,
  zIndex: 1,
  padding: "max(54px, calc(env(safe-area-inset-top) + 40px)) 0 max(16px, env(safe-area-inset-bottom))",
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  overflow: "hidden",
});

// 원본 사진의 실제 표시 너비에 맞춰 우하단 반응 버튼 정렬 기준을 만듦
export const imageFrame = style({
  width: "fit-content",
  maxWidth: "100%",
  maxHeight: "100%",
  margin: 0,
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-end",
  gap: "8px",
});

// 좋아요와 댓글 영역이 사진을 가리지 않도록 반응 버튼 높이를 제외한 범위에 원본을 맞춤
export const originalImageWithActions = style({
  display: "block",
  width: "auto",
  height: "auto",
  maxWidth: "100%",
  maxHeight: "calc(100dvh - 118px - env(safe-area-inset-top) - env(safe-area-inset-bottom))",
  objectFit: "contain",
  userSelect: "none",
});

// 원본 사진 바깥의 우하단에 사진 반응 기능을 표시함
export const viewerActions = style({
  flexShrink: 0,
  display: "flex",
  justifyContent: "flex-end",
  paddingRight: "14px",
});

export const closeButton = style({
  position: "absolute",
  top: "max(14px, env(safe-area-inset-top))",
  right: "max(14px, env(safe-area-inset-right))",
  zIndex: 3,
  width: "32px",
  height: "32px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f5",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  transition: "background-color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray200,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const closeIcon = style({
  display: "block",
  width: "14px",
  height: "14px",
  opacity: 0.72,
});

export const trigger = style({
  padding: 0,
  border: 0,
  background: "transparent",
  color: "inherit",
  font: "inherit",
  cursor: "zoom-in",
  WebkitTapHighlightColor: "transparent",
  transition: "filter 160ms ease",
  selectors: {
    "&:hover": {
      filter: "brightness(0.96)",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});
