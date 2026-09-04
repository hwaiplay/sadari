import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";
import { HEADER_BACKGROUND_COLOR } from "./headerStyle";

const headerContentSlideForwardKeyframes = keyframes({
  "0%": {
    opacity: 0,
    transform: "translateX(28px)",
  },
  "100%": {
    opacity: 1,
    transform: "translateX(0)",
  },
});

const headerContentSlideBackKeyframes = keyframes({
  "0%": {
    opacity: 0,
    transform: "translateX(-28px)",
  },
  "100%": {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const headerShell = style({
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  zIndex: 997,
  width: "100%",
  height: vars.headerHeight,
  backgroundColor: HEADER_BACKGROUND_COLOR,
  transform: "translateY(0)",
  willChange: "transform",
});

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: vars.headerHeight,
  position: "relative",
});

export const headerCenter = style({
  width: "calc(100% - 190px)",
  height: "100%",
  margin: "0 auto",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  overflow: "hidden",
});

export const headerRouteTitle = style({
  position: "absolute",
  left: "16px",
  right: "100px",
  width: "auto",
  margin: 0,
  justifyContent: "flex-start",
});

export const headerRouteTitleWithBack = style({
  left: "52px",
});

export const logo = style({
  margin: 0,
  display: "inline-block",
  position: "relative",
  zIndex: 1,
  paddingTop: "2px"
});

export const routeTitle = style({
  width: "100%",
  minWidth: 0,
  maxWidth: "100%",
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "19px",
  lineHeight: 1.3,
  letterSpacing: 0,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const headerContentSlideForward = style({
  animation: `${headerContentSlideForwardKeyframes} 280ms cubic-bezier(0.22, 1, 0.36, 1) both`,
  willChange: "transform, opacity",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});

export const headerContentSlideBack = style({
  animation: `${headerContentSlideBackKeyframes} 280ms cubic-bezier(0.22, 1, 0.36, 1) both`,
  willChange: "transform, opacity",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
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
  right: "16px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "32px",
  height: "32px",
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
  right: "52px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "32px",
  height: "32px",
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
  width: "10px",
  height: "10px",
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
