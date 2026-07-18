import { style } from "@vanilla-extract/css";

export const starGroup = style({
  display: "flex",
  alignItems: "center",
  flexWrap: "wrap",
  gap: "3px",
  width: "fit-content",
  color: "#d3d7dc",
  fontSize: "34px",
  lineHeight: 1,
  touchAction: "none",
  userSelect: "none",
});

export const starGroupDisabled = style({
  pointerEvents: "none",
  opacity: 0.62,
});

export const starLabel = style({
  minWidth: "auto",
  height: "auto",
  border: 0,
  borderRadius: 0,
  backgroundColor: "transparent",
  color: "inherit",
  fontSize: "inherit",
  lineHeight: 1,
  textAlign: "center",
  cursor: "pointer",
  boxSizing: "border-box",
  transition: "color 0.15s ease",

  selectors: {
    "&:hover": {
      color: "#ffd966",
    },
  },
});

export const starActive = style({
  color: "#ffd966",
});

export const hiddenInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
});
