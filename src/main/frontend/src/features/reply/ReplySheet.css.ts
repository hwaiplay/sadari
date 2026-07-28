import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css.ts";

const sheetEnter = keyframes({
  from: {
    transform: "translateY(100%)",
  },
  to: {
    transform: "translateY(0)",
  },
});

export const sheetLayer = style({
  position: "fixed",
  inset: 0,
  zIndex: 1200,
  display: "flex",
  justifyContent: "center",
  alignItems: "flex-end",
});

export const sheetBackdrop = style({
  position: "absolute",
  inset: 0,
  width: "100%",
  height: "100%",
  border: 0,
  backgroundColor: "rgba(0, 0, 0, 0.48)",
  cursor: "default",
});

export const commentSheet = style({
  position: "relative",
  zIndex: 1,
  width: "min(100%, 600px)",
  height: "85%",
  borderRadius: "22px 22px 0 0",
  backgroundColor: "#ffffff",
  display: "flex",
  flexDirection: "column",
  overflow: "hidden",
  willChange: "transform",
  animation: `${sheetEnter} 240ms cubic-bezier(0.22, 1, 0.36, 1)`,
});

export const sheetHandle = style({
  position: "relative",
  width: "100%",
  height: "60px",
  flexShrink: 0,
  cursor: "grab",
  touchAction: "none",
  userSelect: "none",
  selectors: {
    "&::after": {
      content: "",
      position: "absolute",
      top: "12px",
      left: "50%",
      width: "40px",
      height: "4px",
      borderRadius: "999px",
      backgroundColor: vars.color.gray300,
      transform: "translateX(-50%)",
    },
    "&:active": {
      cursor: "grabbing",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray500}`,
      outlineOffset: "-2px",
    },
  },
});

export const commentSheetBody = style({
  minHeight: 0,
  flex: 1,
  padding: "10px 20px 24px",
  display: "flex",
  flexDirection: "column",
  overflowY: "auto",
});

export const commentEmpty = style({
  flex: 1,
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
});

export const commentEmptyIcon = style({
  width: "42px",
  height: "42px",
  marginBottom: "12px",
  opacity: 0.72,
});

export const commentEmptyTitle = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
});

export const commentEmptyText = style({
  maxWidth: "250px",
  marginTop: "6px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.55,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const replyList = style({
  width: "100%",
  margin: 0,
  padding: 0,
  display: "flex",
  flexDirection: "column",
  gap: "16px",
  listStyle: "none",
});

export const replyItem = style({
  position: "relative",
  display: "flex",
  flexDirection: "column",
  gap: "8px",
  minHeight: "172px",
  backgroundColor: "#ffffff",
});

export const replyItemTop = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "8px",
  minWidth: 0,
});

export const replyItemHeader = style({
  display: "flex",
  alignItems: "center",
  gap: "7px",
  minWidth: 0,
});

export const replyWriterArea = style({
  minWidth: 0,
  display: "inline-flex",
  alignItems: "center",
  gap: "6px",
});

export const replyProfileImage = style({
  width: "24px",
  height: "24px",
  flexShrink: 0,
  borderRadius: "50%",
  objectFit: "cover",
  backgroundColor: vars.color.gray300,
});

export const replyWriter = style({
  minWidth: 0,
  maxWidth: "115px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const replyContentWrap = style({
  maxHeight: "3000px",
  overflow: "clip",
  transition: "max-height 220ms ease",
});

export const replyContent = style({
  margin: 0,
  color: "#565656",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: "22px",
  letterSpacing: "-1%",
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const replyItemMetrics = style({
  width: "100%",
  minHeight: "24px",
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  gap: "8px",
  marginTop: "auto",
});

export const replyMetricButton = style({
  minWidth: "32px",
  height: "24px",
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "4px",
  backgroundColor: "transparent",
  color: "#ff747c",
  fontFamily: vars.font.body,
  fontSize: "14px",
  cursor: "pointer",
});

export const replyAnswerButton = style([
  replyMetricButton,
  {
    color: "#777777",
  },
]);

export const commentForm = style({
  flexShrink: 0,
  padding: "10px 16px calc(10px + env(safe-area-inset-bottom))",
  borderTop: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
  display: "flex",
  alignItems: "center",
  gap: "8px",
});

export const commentInput = style({
  minWidth: 0,
  height: "40px",
  flex: 1,
  padding: "0 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "21px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  outline: "none",
  selectors: {
    "&::placeholder": {
      color: vars.color.gray500,
    },
    "&:focus": {
      borderColor: vars.color.gray600,
    },
  },
});

export const commentSubmitButton = style({
  flexShrink: 0,
  height: "40px",
  padding: "0 24px",
  border: 0,
  borderRadius: "21px",
  backgroundColor: vars.color.black,
  color: "#ffffff",
  fontFamily: vars.font.medium,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      backgroundColor: vars.color.gray400,
      cursor: "default",
    },
  },
});
