import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const sheetEnter = keyframes({
  from: {
    transform: "translateY(100%)",
  },
  to: {
    transform: "translateY(0)",
  },
});

const statusPill = style({
  flexShrink: 0,
  minHeight: "20px",
  padding: "3px 8px",
  borderRadius: "999px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.medium,
  fontSize: "9px",
  lineHeight: 1,
  whiteSpace: "nowrap",
});

export const page = style({
  width: "100%",
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const content = style({
  maxWidth: "520px",
  width: "100%",
  margin: "0 auto",
  padding: "84px 12px 32px",
  display: "flex",
  flexDirection: "column",
});

export const header = style({
  position: "fixed",
  top: vars.headerHeight,
  left: "50%",
  zIndex: 996,
  width: "min(100%, 520px)",
  height: "84px",
  display: "flex",
  gap: "12px",
  alignItems: "center",
  margin: 0,
  padding: "10px 16px",
  transform: "translateX(-50%)",
  backgroundColor: "#ffffff",
  boxShadow: "0 5px 12px rgba(0, 0, 0, 0.08)",
});

export const coverFrame = style({
  width: "48px",
  height: "64px",
  borderRadius: "3px",
  overflow: "hidden",
  flexShrink: 0,
  backgroundColor: vars.color.gray100,
});

export const coverImage = style({
  width: "100%",
  height: "100%",
  objectFit: "cover",
  display: "block",
});

export const headingArea = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "7px",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "13px",
  lineHeight: 1.45,
  color: vars.color.black,
  display: "-webkit-box",
  overflow: "hidden",
  WebkitLineClamp: 2,
  WebkitBoxOrient: "vertical",
});

export const authorRatingLine = style({
  display: "flex",
  alignItems: "center",
  gap: "7px",
  minWidth: 0,
});

export const meta = style({
  minWidth: 0,
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "10px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const metaSeparator = style({
  flexShrink: 0,
  color: vars.color.gray500,
  fontSize: "9px",
});

export const ratingSummary = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: "10px",
});

export const ratingStar = style({
  color: "#f6c944",
  fontSize: "12px",
  lineHeight: 1,
});

export const filters = style({
  display: "flex",
  alignItems: "center",
  gap: "8px",
  minHeight: "42px",
  padding: "8px 0 4px",
});

export const filterControl = style({
  position: "relative",
  display: "inline-flex",
  alignItems: "center",
  selectors: {
    "&::after": {
      content: "",
      width: "6px",
      height: "6px",
      marginLeft: "-12px",
      borderRight: `1px solid ${vars.color.gray600}`,
      borderBottom: `1px solid ${vars.color.gray600}`,
      transform: "translateY(-2px) rotate(45deg)",
      pointerEvents: "none",
    },
  },
});

export const filterSelect = style({
  minWidth: 0,
  height: "28px",
  padding: "0 20px 0 2px",
  border: 0,
  borderRadius: 0,
  appearance: "none",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "11px",
  cursor: "pointer",
  outline: "none",
});

export const filterDivider = style({
  width: "1px",
  height: "12px",
  backgroundColor: vars.color.gray300,
});

export const visuallyHidden = style({
  position: "absolute",
  width: "1px",
  height: "1px",
  padding: 0,
  margin: "-1px",
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  border: 0,
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  paddingBottom: "8px",
});

export const item = style({
  position: "relative",
  display: "flex",
  flexDirection: "column",
  gap: "8px",
  minHeight: "110px",
  padding: "10px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: "#ffffff",
});

export const itemTop = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "8px",
  minWidth: 0,
});

export const itemHeader = style({
  display: "flex",
  alignItems: "center",
  gap: "7px",
  minWidth: 0,
});

export const profileButton = style({
  minWidth: 0,
  padding: 0,
  border: 0,
  backgroundColor: "transparent",
  display: "inline-flex",
  alignItems: "center",
  gap: "6px",
  cursor: "pointer",
});

export const profileImage = style({
  width: "24px",
  height: "24px",
  flexShrink: 0,
  borderRadius: "50%",
  objectFit: "cover",
  backgroundColor: vars.color.gray300,
});

export const writer = style({
  minWidth: 0,
  maxWidth: "115px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const statusDone = style([
  statusPill,
  {
    border: "1px solid #70b6ee",
    backgroundColor: "#f4faff",
    color: "#4d9ddb",
  },
]);

export const statusReading = style([
  statusPill,
  {
    border: "1px solid #696969",
    backgroundColor: "#696969",
    color: "#ffffff",
  },
]);

export const statusStopped = style([
  statusPill,
  {
    border: `1px solid ${vars.color.gray400}`,
    backgroundColor: "#f7f7f7",
    color: vars.color.gray600,
  },
]);

export const reportRating = style({
  flexShrink: 0,
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: "10px",
});

export const reportContentWrap = style({
  maxHeight: "70px",
  overflow: "hidden",
  transition: "max-height 220ms ease",
});

export const reportContentWrapOpen = style([
  reportContentWrap,
  {
    maxHeight: "3000px",
  },
]);

export const reportContent = style({
  margin: 0,
  color: "#555555",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.65,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const expandButton = style({
  position: "absolute",
  bottom: "8px",
  left: "50%",
  width: "22px",
  height: "18px",
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  backgroundColor: "transparent",
  color: vars.color.black,
  transform: "translateX(-50%)",
  cursor: "pointer",
});

export const expandArrow = style({
  width: "17px",
  height: "17px",
  fill: "currentColor",
  transition: "transform 180ms ease",
});

export const expandArrowOpen = style([
  expandArrow,
  {
    transform: "rotate(180deg)",
  },
]);

export const itemMetrics = style({
  width: "100%",
  minHeight: "24px",
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  gap: "8px",
  marginTop: "auto",
});

export const metricButton = style({
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
  selectors: {
    "&:disabled": {
      cursor: "default",
      opacity: 0.5,
    },
  },
});

export const metricIcon = style({
  width: "16px",
  height: "16px",
  flexShrink: 0,
});

export const commentButton = style([
  metricButton,
  {
    color: "#777777",
  },
]);

export const commentIcon = style({
  width: "16px",
  height: "16px",
  flexShrink: 0,
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.5,
  strokeLinejoin: "round",
});

export const empty = style({
  margin: "48px 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  textAlign: "center",
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
  height: "min(72svh, 560px)",
  borderRadius: "12px 12px 0 0",
  backgroundColor: "#ffffff",
  display: "flex",
  flexDirection: "column",
  overflow: "hidden",
  animation: `${sheetEnter} 240ms cubic-bezier(0.22, 1, 0.36, 1) both`,
});

export const sheetHandle = style({
  width: "40px",
  height: "4px",
  margin: "10px auto 0",
  borderRadius: "999px",
  backgroundColor: vars.color.gray300,
});

export const commentSheetBody = style({
  minHeight: 0,
  flex: 1,
  padding: "24px 20px",
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
  marginTop: "7px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1.55,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const temporaryCommentList = style({
  width: "100%",
  margin: 0,
  padding: 0,
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  listStyle: "none",
});

export const temporaryComment = style({
  alignSelf: "flex-end",
  maxWidth: "82%",
  padding: "9px 12px",
  borderRadius: "14px 14px 2px 14px",
  backgroundColor: vars.color.gray100,
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.45,
  wordBreak: "break-word",
});

export const commentForm = style({
  flexShrink: 0,
  padding: "10px 16px calc(10px + env(safe-area-inset-bottom))",
  borderTop: `1px solid ${vars.color.gray300}`,
  backgroundColor: vars.color.white,
  display: "flex",
  alignItems: "center",
  gap: "8px",
});

export const commentInput = style({
  minWidth: 0,
  height: "42px",
  flex: 1,
  padding: "0 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "21px",
  backgroundColor: vars.color.white,
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
  height: "42px",
  padding: "0 14px",
  border: 0,
  borderRadius: "21px",
  backgroundColor: vars.color.black,
  color: vars.color.white,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      backgroundColor: vars.color.gray300,
      cursor: "default",
    },
  },
});
