import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

export const navContainer = style({
  position: "fixed",
  bottom: 0,
  right: 0,
  left: 0,
  zIndex: 997,
  width: "100%",
  height: vars.headerHeight,
  margin: "0 auto",
  display: "flex",
  alignItems: "center",
  padding: "0 60px",
});

export const whiteBg = style({
  backgroundColor: "#ffffff",
});

export const navigation = style({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  width: "100%",
});

export const navButton = style({
  width: "101px",
});
