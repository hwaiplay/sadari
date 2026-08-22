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
