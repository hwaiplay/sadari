import { style } from "@vanilla-extract/css";
import { vars } from "../../../styles/tokens.css";

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: vars.headerHeight,
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  zIndex: 997,
  background: "transparent",
});
