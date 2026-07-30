import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const dayGridSlideFromLeftKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(-14px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

const dayGridSlideFromRightKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(14px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const wrapper = style({
  position: "relative",
  display: "grid",
  gridTemplateColumns: "82px minmax(0, 1fr)",
  alignItems: "center",
  gap: "8px",
});

export const wrapperNoLabel = style({
  gridTemplateColumns: "minmax(0, 1fr)",
});

export const label = style({
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: vars.color.black,
  whiteSpace: "nowrap",
});

export const trigger = style({
  width: "100%",
  minHeight: "40px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  padding: "0 14px",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "10px",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  cursor: "pointer",
  boxSizing: "border-box",
});

export const placeholder = style({
  color: vars.color.gray500,
});

export const calendarIcon = style({
  width: "18px",
  height: "18px",
  flexShrink: 0,
});

export const popover = style({
  position: "absolute",
  top: "48px",
  right: 0,
  zIndex: 20,
  width: "292px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "16px",
  backgroundColor: "#ffffff",
  boxShadow: "0 18px 40px rgba(0, 0, 0, 0.14)",
  padding: "14px",
  boxSizing: "border-box",
});

export const inlineCalendar = style({
  width: "100%",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
});

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  marginBottom: "12px",
});

export const monthLabel = style({
  fontFamily: vars.font.heading,
  fontSize: "15px",
  color: vars.color.black,
});

export const navButton = style({
  width: "32px",
  height: "32px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",

  ":hover": {
    borderColor: vars.color.gray500,
    backgroundColor: "#f7f7f7",
  },
});

export const navIcon = style({
  width: "18px",
  height: "18px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const weekGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, 1fr)",
  gap: "4px",
  marginBottom: "6px",
});

export const weekDay = style({
  height: "24px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  color: vars.color.gray500,
  fontFamily: vars.font.body,
  fontSize: "12px",
});

export const dayGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, 1fr)",
  gap: 0,
  overflow: "hidden",
  borderRadius: "10px",
});

export const dayGridSlideFromLeft = style({
  animation: `${dayGridSlideFromLeftKeyframes} 180ms ease-out`,
});

export const dayGridSlideFromRight = style({
  animation: `${dayGridSlideFromRightKeyframes} 180ms ease-out`,
});

export const emptyDay = style({
  height: "34px",
});

export const dayButton = style({
  position: "relative",
  height: "34px",
  border: "1px solid transparent",
  borderRadius: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "13px",
  cursor: "pointer",

  ":hover": {
    borderColor: vars.color.gray400,
    backgroundColor: "#f7f7f7",
  },
});

export const today = style({
  borderColor: "transparent",
  color: "#25624c",
  selectors: {
    "&::after": {
      content: "",
      position: "absolute",
      top: 0,
      left: "50%",
      width: "32px",
      height: "32px",
      border: "1px solid #8ab6a3",
      borderRadius: "50%",
      transform: "translateX(-50%)",
      pointerEvents: "none",
      boxSizing: "border-box",
    },
  },
});

export const selected = style({
  borderRadius: "10px",
  border: `1px solid ${vars.color.gray700}`,
  backgroundColor: "#f3f3f3",
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,

  ":hover": {
    borderColor: vars.color.gray700,
    backgroundColor: vars.color.gray100,
  },
});

export const rangeInner = style({
  borderColor: "#e5f0eb",
  backgroundColor: "#e5f0eb",
  color: "#25624c",
});

export const rangeStart = style({
  borderColor: "#e5f0eb",
  borderTopLeftRadius: "999px",
  borderBottomLeftRadius: "999px",
  backgroundColor: "#e5f0eb",
  color: "#1f5d47",

  ":hover": {
    borderColor: "#e5f0eb",
    backgroundColor: "#e5f0eb",
  },
});

export const rangeEnd = style({
  borderColor: "#e5f0eb",
  borderTopRightRadius: "999px",
  borderBottomRightRadius: "999px",
  backgroundColor: "#e5f0eb",
  color: "#1f5d47",

  ":hover": {
    borderColor: "#e5f0eb",
    backgroundColor: "#e5f0eb",
  },
});

export const rangeSameDay = style({
  width: "34px",
  justifySelf: "center",
  borderColor: "#e5f0eb",
  borderRadius: "50%",
  backgroundColor: "#e5f0eb",
  color: "#1f5d47",

  ":hover": {
    borderColor: "#e5f0eb",
    backgroundColor: "#e5f0eb",
  },
});

export const selectedRange = style({
  minHeight: "40px",
  marginTop: "14px",
  padding: "0 12px",
  borderRadius: "10px",
  backgroundColor: "#f5f7f6",
  color: vars.color.gray700,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  lineHeight: 1.4,
  textAlign: "center",
  wordBreak: "keep-all",
  boxSizing: "border-box",
});

export const footer = style({
  display: "flex",
  justifyContent: "flex-end",
  marginTop: "12px",
});

export const closeButton = style({
  minWidth: "58px",
  height: "30px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "12px",
  cursor: "pointer",
});
