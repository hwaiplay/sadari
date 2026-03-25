import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  width: "100%",
  height: vars.headerHeight,
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  zIndex: 997,
  background: "transparent",

  selectors: {
    "&._form": {
      padding: "0 8px 0 5px",
      backgroundColor: vars.color.background,
    },
  },
});

export const logo = style({
  margin: "0 auto",
  display: "inline-block",
});

export const saveBtn = style({
  fontSize: "16px",
  fontFamily: vars.font.heading,
  width: "44px",
  height: "44px",
});
