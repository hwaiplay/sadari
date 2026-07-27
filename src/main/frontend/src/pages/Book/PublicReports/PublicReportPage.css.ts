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
  maxHeight: "20px",
  padding: "5px 7px",
  borderRadius: "999px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.medium,
  fontSize: "10px",
  lineHeight: 1,
  letterSpacing: "-1%",
  whiteSpace: "nowrap",
});

export const page = style({
  width: "100%",
  minHeight: "100svh",
  backgroundColor: "#ffffff",
  paddingBottom: '60px'
});

export const content = style({
  display: "flex",
  flexDirection: "column",
});

export const header = style({
  position: "sticky",
  top: vars.headerHeight,
  zIndex: 996,
  width: "100svw",
  height: "90px",
  marginLeft: "calc(50% - 50svw)",
  backgroundColor: "#ffffff",
  boxShadow: "0px 3px 10px rgba(0, 0, 0, 0.08)",
});

export const headerWrap = style({
  width: "100%",
  maxWidth: "600px",
  height: "100%",
  margin: "0 auto",
  padding: "10px 24px",
  display: "flex",
  gap: "12px",
  alignItems: "center",
});

export const coverFrame = style({
  width: "fit-content",
  height: "100%",
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
  fontSize: "14px",
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
  gap: "2px",
  minWidth: 0,
});

export const meta = style({
  minWidth: 0,
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
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
  fontSize: "12px",
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
  padding: "18px 0",
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: "16px",

});

export const item = style({
  position: "relative",
  display: "flex",
  flexDirection: "column",
  gap: "8px",
  minHeight: "172px",
  padding: "12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
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
    border: `1px solid ${vars.color.brand}`,
    backgroundColor: "#DAEEF8",
    color: vars.color.brand,
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
  color: vars.color.gray900,
  fontFamily: vars.font.medium,
  fontSize: "12px",
});

export const reportContentWrap = style({
  maxHeight: "70px",
  overflow: "clip",
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
  color: "#565656",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: "22px",
  letterSpacing: '-1%',
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const expandButton = style({
  position: "absolute",
  bottom: "14px",
  left: "50%",
  width: "20px",
  height: "20px",
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
  backgroundColor: "#FFFFFF",
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
  backgroundColor: "#FFFFFF",
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
  color: "#FFFFFF",
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
