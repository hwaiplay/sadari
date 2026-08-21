import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const player = style({
  position: "fixed",
  right: "max(94px, calc((100vw - 600px) / 2 + 94px))",
  bottom: `calc(${vars.headerHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)) + 24px)`,
  left: "max(16px, calc((100vw - 600px) / 2 + 16px))",
  zIndex: 998,
  height: "62px",
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  gap: 0,
  padding: "5px 10px 5px 0",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray500}`,
  borderRadius: "31px",
  backgroundColor: "rgba(255, 255, 255, 0.88)",
  backdropFilter: "blur(14px)",
  WebkitBackdropFilter: "blur(14px)",
  boxShadow: "0 10px 28px rgba(21, 21, 21, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.74)",
  boxSizing: "border-box",
  opacity: 1,
  transform: "translateY(0)",
  transition: "opacity 300ms ease, transform 300ms cubic-bezier(0.4, 0, 1, 1)",
  "@media": {
    "screen and (max-width: 360px)": {
      left: "8px",
      padding: "5px 6px 5px 0",
    },
    "(prefers-reduced-motion: reduce)": {
      transition: "none",
    },
  },
});

export const playerExiting = style({
  zIndex: 996,
  opacity: 0,
  transform: "translateY(calc(100% + 32px))",
  pointerEvents: "none",
});

export const timerPageLink = style({
  minWidth: 0,
  height: "100%",
  flex: "1 1 auto",
  display: "flex",
  alignItems: "center",
  gap: "10px",
  padding: "0 10px 0 20px",
  borderRadius: "25px",
  color: "inherit",
  textDecoration: "none",
  boxSizing: "border-box",
  transition: "background-color 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "-2px",
    },
  },
  "@media": {
    "screen and (max-width: 360px)": {
      gap: "7px",
      padding: "0 7px 0 14px",
    },
  },
});

export const bookCover = style({
  width: "36px",
  height: "50px",
  display: "block",
  flexShrink: 0,
  borderRadius: "4px",
  objectFit: "cover",
  backgroundColor: vars.color.gray100,
  boxShadow: "0 3px 8px rgba(21, 21, 21, 0.16)",
});

export const timerInfo = style({
  minWidth: 0,
  flex: "1 1 auto",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  gap: "4px",
});

export const timerClock = style({
  margin: 0,
  overflow: "hidden",
  color: vars.color.gray900,
  fontFamily: "PretendardExtraBold, system-ui, sans-serif",
  fontSize: "20px",
  fontVariantNumeric: "tabular-nums",
  letterSpacing: "-0.5px",
  lineHeight: 1,
  whiteSpace: "nowrap",
  textOverflow: "ellipsis",
});

export const bookTitle = style({
  margin: 0,
  overflow: "hidden",
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.15,
  whiteSpace: "nowrap",
  textOverflow: "ellipsis",
});

export const playerActions = style({
  display: "flex",
  alignItems: "center",
  gap: "3px",
  flexShrink: 0,
});

export const iconButton = style({
  width: "30px",
  height: "30px",
  minWidth: 0,
  padding: 0,
  borderRadius: "50%",
  color: vars.color.gray700,
  boxSizing: "border-box",
  selectors: {
    "&:disabled": {
      color: vars.color.gray500,
    },
  },
  "@media": {
    "screen and (max-width: 360px)": {
      width: "28px",
      height: "28px",
    },
  },
});

export const controlIcon = style({
  width: "16px",
  height: "16px",
  display: "block",
});
