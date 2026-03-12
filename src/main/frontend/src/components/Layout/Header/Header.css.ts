import { style } from "@vanilla-extract/css";

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: "68px",
  position: "fixed",
  top: 0,
  left: 0,
  zIndex: 997,
});
