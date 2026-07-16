import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const calendarSlideFromLeftKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(-18px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

const calendarSlideFromRightKeyframes = keyframes({
  from: {
    opacity: 0,
    transform: "translateX(18px)",
  },
  to: {
    opacity: 1,
    transform: "translateX(0)",
  },
});

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const content = style({
  maxWidth: "520px",
  width: "100%",
  margin: "0 auto",
  padding: "18px 16px 44px",
  display: "flex",
  flexDirection: "column",
  gap: "18px",
});

export const toolbar = style({
  display: "grid",
  gridTemplateColumns: "38px 1fr 38px",
  alignItems: "center",
  gap: "10px",
});

export const monthButton = style({
  width: "38px",
  height: "38px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  transition: "background-color 160ms ease, border-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#f8f9fa",
      borderColor: "#cfd4da",
      transform: "translateY(-1px)",
    },
    "&:active": {
      transform: "translateY(0)",
    },
  },
});

export const monthButtonIcon = style({
  width: "20px",
  height: "20px",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const monthTitle = style({
  margin: 0,
  textAlign: "center",
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.3,
  color: vars.color.black,
});

export const calendar = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, minmax(0, 1fr))",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "18px",
  backgroundColor: vars.color.gray200,
  gap: "1px",
  boxShadow: "0 10px 28px rgba(0, 0, 0, 0.06)",
  willChange: "transform, opacity",
});

export const calendarSlideFromLeft = style({
  animation: `${calendarSlideFromLeftKeyframes} 220ms ease-out`,
});

export const calendarSlideFromRight = style({
  animation: `${calendarSlideFromRightKeyframes} 220ms ease-out`,
});

export const weekday = style({
  minHeight: "32px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  backgroundColor: "#fafafa",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  color: "#777777",
});

export const dayCell = style({
  position: "relative",
  minWidth: 0,
  minHeight: "92px",
  padding: "28px 4px 6px",
  border: 0,
  backgroundColor: "#ffffff",
  overflow: "hidden",
  cursor: "pointer",
  textAlign: "left",
  appearance: "none",
});

export const outsideDay = style({
  backgroundColor: "#fafafa",
});

export const today = style({
  boxShadow: "inset 0 0 0 2px #c9b8ff",
});

export const selectedDay = style({
  boxShadow: "inset 0 0 0 2px #8ab6a3",
  backgroundColor: "#fbfffd",
});

export const dayNumber = style({
  position: "absolute",
  top: "6px",
  right: "7px",
  zIndex: 2,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  minWidth: "16px",
  height: "16px",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  lineHeight: 1,
  color: vars.color.black,
});

export const outsideDayNumber = style({
  color: vars.color.gray500,
});

export const dayBooks = style({
  display: "flex",
  flexDirection: "column",
  gap: "3px",
});

export const bookPill = style({
  width: "100%",
  minHeight: "12px",
  borderRadius: "999px",
});

export const moreCount = style({
  marginTop: "1px",
  fontFamily: vars.font.body,
  fontSize: "10px",
  color: "#777777",
});

export const selectedSummary = style({
  margin: 0,
  fontFamily: vars.font.middle,
  fontSize: "14px",
  color: vars.color.black,
});

export const emptyMessage = style({
  margin: "18px 0 0",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  textAlign: "center",
  color: "#777777",
});

export const scheduleList = style({
  display: "flex",
  flexDirection: "column",
  gap: "8px",
});

export const scheduleItem = style({
  width: "100%",
  minHeight: "42px",
  padding: 0,
  border: 0,
  backgroundColor: "transparent",
  display: "grid",
  gridTemplateColumns: "10px 1fr",
  alignItems: "center",
  gap: "10px",
  textAlign: "left",
  cursor: "pointer",
});

export const scheduleColor = style({
  width: "10px",
  height: "28px",
  borderRadius: "999px",
});

export const scheduleText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "2px",
});

export const scheduleTitle = style({
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: vars.color.black,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const scheduleDate = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  color: "#777777",
});
