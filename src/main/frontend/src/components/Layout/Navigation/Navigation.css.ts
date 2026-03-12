import { style } from "@vanilla-extract/css";

export const navigation = style({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  height: "60px",
  zIndex: 997,
  position: "fixed",
  bottom: 0,
  left: 0,
  margin: 0,
  background: "#e0e0e080",
});

export const navButton = style({
  width: "101px",
});
