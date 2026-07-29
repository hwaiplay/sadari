import { style } from "@vanilla-extract/css";

export const starGroup = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "10px",
  width: "fit-content",
  lineHeight: 1,
});

export const gradeValue = style({
  minHeight: "27px",
  fontFamily: "inherit",
  fontSize: "22px",
  fontWeight: 600,
  lineHeight: 1.2,
  color: "#333333",
});

export const starRow = style({
  display: "flex",
  alignItems: "center",
  gap: "4px",
  width: "fit-content",
  touchAction: "none",
  userSelect: "none",
  cursor: "pointer",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #8ab6a3",
      outlineOffset: "5px",
      borderRadius: "5px",
    },
  },
});

export const starGroupDisabled = style({
  pointerEvents: "none",
  opacity: 0.62,
});

export const star = style({
  position: "relative",
  display: "inline-block",
  width: "44px",
  height: "44px",
});

export const starEmpty = style({
  position: "absolute",
  inset: 0,
  color: "#d9dde1",
});

export const starFill = style({
  position: "absolute",
  top: 0,
  bottom: 0,
  left: 0,
  display: "block",
  width: "var(--rating-fill-width)",
  overflow: "hidden",
  color: "#ffd45c",
  whiteSpace: "nowrap",
  transition: "width 80ms linear",
});

export const starIcon = style({
  display: "block",
  width: "44px",
  height: "44px",
  fill: "currentColor",
  stroke: "currentColor",
  strokeWidth: 1.8,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const hiddenInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
});
