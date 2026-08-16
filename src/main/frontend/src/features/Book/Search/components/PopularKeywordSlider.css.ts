import { vars } from "@/app/styles/tokens.css";
import { globalStyle, keyframes, style } from "@vanilla-extract/css";

const keywordSlideUp = keyframes({
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
  flex: "1 1 0",
  minWidth: 0,
  height: "32px",
  marginLeft: "10px",
  display: "flex",
  alignItems: "center",
  boxSizing: "border-box",
});

export const viewport = style({
  position: "relative",
  minWidth: 0,
  height: "32px",
  flex: 1,
  overflow: "hidden",
});

export const keywordButton = style({
  position: "absolute",
  inset: 0,
  height: "32px",
  minHeight: "32px",
  maxHeight: "32px",
  minWidth: 0,
  padding: "0 8px 0 0",
  border: 0,
  borderRadius: "6px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  justifyContent: "flex-start",
  animation: `${keywordSlideUp} 300ms cubic-bezier(0.22, 1, 0.36, 1) both`,

  selectors: {
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.gray100,
    },
    "&:disabled": {
      border: 0,
      backgroundColor: "transparent",
    },
  },

  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});

globalStyle(`${keywordButton} > span`, {
  display: "block",
  width: "100%",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  textAlign: "left",
});
