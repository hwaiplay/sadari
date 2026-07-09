import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

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
  background: "transparent"
});

export  const header_active = style({
  backgroundColor:'#fff'
})

export const logo = style({
  margin: "0 auto",
  display: "inline-block",
  position: "relative",
  zIndex: 1,
});

export const backpageBtn = style({
  position: "absolute",
  left: "5px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "40px",
  height: "40px",
  border: 0,
  backgroundColor: "transparent",
  cursor: "pointer",
  zIndex: 1,
});
