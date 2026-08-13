import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const restrictedMain = style({
  minHeight: "100svh",
  paddingTop: vars.headerHeight,
  paddingBottom: `calc(${vars.navHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const navShell = style({
  position: "fixed",
  right: "var(--sadari-scrollbar-compensation, 0px)",
  bottom: 0,
  left: 0,
  zIndex: 997,
  height: `calc(${vars.headerHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  padding: `6px clamp(28px, 10vw, 72px) max(${vars.space.sm}, env(safe-area-inset-bottom, 0px))`,
  backgroundColor: vars.color.background,
  boxShadow: "0 -6px 27px rgb(0 0 0 / 10%)",
  boxSizing: "border-box",
});

export const navigation = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  width: "100%",
  maxWidth: "600px",
  height: "100%",
  margin: "0 auto",
});

export const navLink = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "2px",
  minWidth: "58px",
  padding: 0,
  color: vars.color.gray500,
  textDecoration: "none",
});

export const navLinkActive = style({
  color: vars.color.gray900,
});

export const navIcon = style({
  width: "27px",
  height: "27px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.8,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const navText = style({
  margin: 0,
  color: "currentColor",
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
  lineHeight: 1.2,
});
