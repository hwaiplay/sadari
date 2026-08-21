import { vars } from "@/app/styles/tokens.css";
import { keyframes, style } from "@vanilla-extract/css";

const noticeMarqueeLeft = keyframes({
  "0%": {
    transform: "translateX(0)",
  },
  "100%": {
    transform: "translateX(calc(-50% - 12px))",
  },
});

const noticeMarqueeRevealEnd = keyframes({
  "0%": {
    transform: "translateX(0)",
  },
  "100%": {
    transform: "translateX(min(0px, calc(100cqw - 100%)))",
  },
});

const noticeMarqueeStatic = keyframes({
  "0%": {
    transform: "translateX(0)",
  },
  "100%": {
    transform: "translateX(0)",
  },
});

const noticeSlideUp = keyframes({
  "0%": {
    opacity: 0,
    transform: "translateY(100%)",
  },
  "100%": {
    opacity: 1,
    transform: "translateY(0)",
  },
});

export const slider = style({
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  flex: 1,
  minWidth: 0,
  width: "auto",
  height: "32px",
  margin: 0,
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
  padding: "0 8px 0 0",
  display: "flex",
  alignItems: "center",
  gap: "2px",
  borderRadius: "6px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  textDecoration: "none",
  overflow: "hidden",
  animation: `${noticeSlideUp} 300ms cubic-bezier(0.4, 0, 0.2, 1)`,

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

export const marqueeViewport = style({
  position: "relative",
  zIndex: 0,
  minWidth: 0,
  flex: 1,
  display: "block",
  marginLeft: "-10px",
  padding: "0 12px",
  boxSizing: "border-box",
  overflow: "hidden",
  containerType: "inline-size",
  maskImage:
    "linear-gradient(to right, transparent 0, #000 10px, #000 calc(100% - 12px), transparent 100%)",
  WebkitMaskImage:
    "linear-gradient(to right, transparent 0, #000 10px, #000 calc(100% - 12px), transparent 100%)",
});

export const categoryLayer = style({
  position: "relative",
  zIndex: 1,
  flexShrink: 0,
  display: "inline-flex",
});

export const marqueeTrack = style({
  width: "max-content",
  display: "flex",
  alignItems: "center",
  gap: "24px",
  animationTimingFunction: "linear",
  willChange: "transform",
});

export const singleMarqueeTrack = style({
  animationName: noticeMarqueeLeft,
  animationIterationCount: "infinite",

  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animationName: noticeMarqueeStatic,
      willChange: "auto",
    },
  },
});

export const multipleMarqueeTrack = style({
  animationName: noticeMarqueeRevealEnd,
  animationIterationCount: 1,
  animationFillMode: "forwards",

  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animationName: noticeMarqueeStatic,
      willChange: "auto",
    },
  },
});

export const noticeTitle = style({
  flex: "none",
  display: "block",
  whiteSpace: "nowrap",
});
