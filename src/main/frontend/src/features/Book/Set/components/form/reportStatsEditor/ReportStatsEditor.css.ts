import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const modalFadeIn = keyframes({
  from: {
    opacity: 0,
  },
  to: {
    opacity: 1,
  },
});

const modalContentFadeIn = keyframes({
  from: {
    opacity: 0,
    transform: "translateY(10px) scale(0.98)",
  },
  to: {
    opacity: 1,
    transform: "translateY(0) scale(1)",
  },
});

const stepSlideForwardKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(18px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

const stepSlideBackwardKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(-18px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const statsSection = style({
  position: "relative",
  width: "100%",
  padding: "16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
  boxSizing: "border-box",
});

export const statsRows = style({
  display: "flex",
  flexDirection: "column",
});

export const statsItem = style({
  position: "relative",
  width: "100%",
  minWidth: 0,
  minHeight: "50px",
  padding: "6px",
  border: 0,
  borderBottom: `1px solid ${vars.color.gray200}`,
  borderRadius: 0,
  backgroundColor: "transparent",
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  gap: "12px",
  color: "inherit",
  textAlign: "left",
  boxSizing: "border-box",
  cursor: "pointer",
  transition: "background-color 160ms ease",
  selectors: {
    "&:last-child": {
      borderBottom: 0,
    },
    "&:hover": {
      backgroundColor: "#f6f8f7",
    },
    "&:focus-visible": {
      outline: "2px solid #8ab6a3",
      outlineOffset: 0,
    },
  },
});

export const statsLabel = style({
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.4,
  color: vars.color.gray600,
});

export const statsValue = style({
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.45,
  color: vars.color.black,
  letterSpacing: 0,
  textAlign: "right",
  wordBreak: "break-word",
});

export const readingStatus =style({
  borderRadius: 99999,
  padding:'4px 10px',
})

export const statusRead = style([
  readingStatus,
  {
    backgroundColor: vars.color.gray600,
    color: "#ffffff",
  }
]);

export const statusDone = style([
  readingStatus,
  {
    color: vars.color.brandText,
    backgroundColor: vars.color.brandBg,
  }
]);

export const statusStop = style([
  readingStatus,
  {
    color: vars.color.negative,
    backgroundColor: vars.color.negativeBg,
  }
]);

export const gradeValue = style([
  statsValue,
  {
    gap: "3px",
  },
]);

export const gradeStar = style({
  width: "18px",
  height: "18px",
  display: "block",
  flexShrink: 0,
  color: "#ffd45c",
});

export const modalOverlay = style({
  position: "fixed",
  inset: 0,
  zIndex: 9998,
  width: "100%",
  height: "100dvh",
  padding: "20px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  backgroundColor: "rgba(0, 0, 0, 0.30)",
  boxSizing: "border-box",
  animation: `${modalFadeIn} 180ms ease-out both`,
});

export const modal = style({
  width: "min(430px, 100%)",
  height: "350px",
  maxHeight: "calc(100dvh - 40px)",
  padding: "22px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "18px",
  backgroundColor: "#ffffff",
  boxShadow: "0 20px 54px rgba(0, 0, 0, 0.16)",
  display: "flex",
  flexDirection: "column",
  overflow: "hidden",
  boxSizing: "border-box",
  animation: `${modalContentFadeIn} 180ms ease-out both`,
  transition: "height 520ms cubic-bezier(0.22, 1, 0.36, 1)",
  "@media": {
    "screen and (max-width: 480px)": {
      height: "330px",
      padding: "20px 18px",
    },
  },
});

export const modalCalendar = style({
  height: "500px",
  "@media": {
    "screen and (max-width: 480px)": {
      height: "500px",
    },
  },
});

export const modalHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "16px",
  overflow: "hidden",
});

export const modalTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "19px",
  lineHeight: 1.35,
  color: vars.color.black,
  letterSpacing: 0,
});

export const closeButton = style({
  width: "32px",
  height: "32px",
  flexShrink: 0,
  padding: 0,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.gray700,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray500,
      backgroundColor: "#f7f7f7",
    },
    "&:focus-visible": {
      outline: "2px solid #8ab6a3",
      outlineOffset: "2px",
    },
  },
});

export const closeIcon = style({
  width: "17px",
  height: "17px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.8,
  strokeLinecap: "round",
});

export const modalBody = style({
  position: "relative",
  flex: 1,
  minHeight: "210px",
  padding: "44px 0 22px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  overflowX: "hidden",
  overflowY: "hidden",
});

export const stepSlideForward = style({
  animation: `${stepSlideForwardKeyframes} 300ms cubic-bezier(0.22, 1, 0.36, 1) both`,
});

export const stepSlideBackward = style({
  animation: `${stepSlideBackwardKeyframes} 300ms cubic-bezier(0.22, 1, 0.36, 1) both`,
});

export const optionGrid = style({
  width: "100%",
  display: "grid",
  gridTemplateColumns: "1fr",
  gap: "10px",
});

export const optionLabel = style({
  minWidth: 0,
});

export const optionInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
});

export const optionButton = style({
  minHeight: "42px",
  padding: "0 12px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "12px",
  backgroundColor: "#ffffff",
  color: vars.color.gray700,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.3,
  textAlign: "center",
  wordBreak: "keep-all",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease, color 160ms ease",
  selectors: {
    [`${optionInput}:checked + &`]: {
      borderColor: "#78a78d",
      backgroundColor: "#edf6f0",
      color: "#2e6546",
    },
    [`${optionInput}:focus-visible + &`]: {
      outline: "2px solid #8ab6a3",
      outlineOffset: "2px",
    },
  },
});

export const publicEditor = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  gap: "16px",
});

export const publicOptionGrid = style({
  display: "grid",
  gridTemplateColumns: "1fr",
  gap: "10px",
});

export const publicOption = style({
  minHeight: "44px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "12px",
  backgroundColor: "#ffffff",
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
});

export const publicOptionActive = style([
  publicOption,
  {
    borderColor: "#78a78d",
    backgroundColor: "#edf6f0",
    color: "#2e6546",
  },
]);

export const publicHelp = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.55,
  color: vars.color.gray600,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const gradeEditor = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "20px",
});

export const gradeHelp = style({
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.4,
  color: vars.color.gray700,
  textAlign: "center",
});

export const periodEditor = style({
  width: "100%",
});

export const modalFooter = style({
  display: "grid",
  gridTemplateColumns: "64px minmax(0, 1fr) 64px",
  alignItems: "center",
  gap: "12px",
});

export const stepButton = style({
  width: "64px",
  height: "40px",
  padding: "0 8px",
  border: 0,
  borderRadius: "8px",
  backgroundColor: "transparent",
  color: vars.color.gray700,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: "#f7f7f7",
      color: vars.color.gray900,
    },
    "&:focus-visible": {
      outline: "2px solid #8ab6a3",
      outlineOffset: "2px",
    },
  },
});

export const confirmButton = style([stepButton]);

export const progressDots = style({
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "7px",
});

export const progressDot = style({
  width: "6px",
  height: "6px",
  borderRadius: "50%",
  backgroundColor: vars.color.gray300,
  transition: "width 160ms ease, background-color 160ms ease",
});

export const progressDotActive = style([
  progressDot,
  {
    width: "18px",
    borderRadius: "999px",
    backgroundColor: "#78a78d",
  },
]);
