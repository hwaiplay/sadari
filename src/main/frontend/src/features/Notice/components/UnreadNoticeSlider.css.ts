import { vars } from "@/app/styles/tokens.css";
import { media } from "@/app/styles/responsive.css";
import { keyframes, style } from "@vanilla-extract/css";

const noticeSlideLeft = keyframes({
  "0%": {
    opacity: 0,
    transform: "translateX(100%)",
  },
  "100%": {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const slider = style({
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  width: `calc(100% - ${vars.space.md} - ${vars.space.md})`,
  height: "32px",
  margin: `0 ${vars.space.md} 12px`,

  "@media": {
    [media.tablet]: {
      width: `calc(100% - ${vars.space.lg} - ${vars.space.lg})`,
      margin: `0 ${vars.space.lg} 12px`,
    },
  },
});

export const viewport = style({
  position: "relative",
  minWidth: 0,
  height: "32px",
  flex: 1,
  overflow: "hidden",
});

export const noticeLink = style({
  position: "absolute",
  inset: 0,
  height: "32px",
  minWidth: 0,
  padding: "0 8px",
  display: "flex",
  alignItems: "center",
  borderRadius: "6px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  textDecoration: "none",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  animation: `${noticeSlideLeft} 300ms cubic-bezier(0.22, 1, 0.36, 1) both`,

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
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});
