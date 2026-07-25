import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

export const headerShell = style({
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  zIndex: 997,
  width: "100%",
  height: vars.headerHeight,
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.08)",
  transform: "translateY(0)",
  transition: "transform 180ms ease",
  willChange: "transform",
});

export const headerHidden = style({
  transform: "translateY(-100%)",
});

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: vars.headerHeight,
  position: "relative",
});

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

export const hamburgerButton = style({
  position: "absolute",
  right: "5px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "40px",
  height: "40px",
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  zIndex: 2,
});

export const hamburgerIcon = style({
  width: "24px",
  height: "24px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
});

export const headerAlimButton = style({
  position: "absolute",
  right: "49px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "40px",
  height: "40px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  zIndex: 2,
});

export const headerAlimBadge = style({
  position: "absolute",
  top: "5px",
  right: "3px",
  minWidth: "14px",
  height: "14px",
  padding: "0 5px",
  borderRadius: "999px",
  backgroundColor: "#ef4444",
  color: "#ffffff",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.semibold,
  fontSize: "10px",
  lineHeight: 1,
  boxSizing: "border-box",
  pointerEvents: "none",
});

export const headerAlimIcon = style({
  width: "24px",
  height: "24px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});
