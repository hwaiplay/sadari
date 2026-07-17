import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const content = style({
  maxWidth: "520px",
  width: "100%",
  margin: "0 auto",
  padding: "28px 20px 32px",
  display: "flex",
  flexDirection: "column",
  gap: "22px",
});

export const header = style({
  position: "sticky",
  top: "28px",
  zIndex: 3,
  display: "flex",
  gap: "14px",
  alignItems: "center",
  padding: "0 0 18px",
  borderBottom: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
});

export const coverFrame = style({
  width: "58px",
  aspectRatio: "2 / 3",
  borderRadius: "6px",
  overflow: "hidden",
  flexShrink: 0,
  backgroundColor: "#f2f2f2",
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
  gap: "5px",
});

export const pageTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "16px",
  color: vars.color.black,
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "18px",
  lineHeight: 1.35,
  color: vars.color.black,
  wordBreak: "keep-all",
});

export const meta = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: "#666666",
});

export const authorRatingLine = style({
  display: "flex",
  alignItems: "center",
  gap: "8px",
  maxWidth: "100%",
});

export const ratingSummary = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
  flexShrink: 0,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  lineHeight: 1,
  color: vars.color.black,
});

export const metaSeparator = style({
  flexShrink: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1,
  color: vars.color.gray500,
});

export const ratingStar = style({
  color: "#ffd966",
  fontSize: "15px",
  lineHeight: 1,
});

export const ratingValue = style({
  lineHeight: 1,
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: "14px",
});

export const item = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  padding: "16px 0",
  borderBottom: `1px solid ${vars.color.gray300}`,
});

export const itemTop = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  minWidth: 0,
});

export const itemHeader = style({
  display: "flex",
  alignItems: "center",
  gap: "10px",
  minWidth: 0,
});

export const profileButton = style({
  minWidth: 0,
  padding: 0,
  border: 0,
  backgroundColor: "transparent",
  display: "inline-flex",
  alignItems: "center",
  gap: "10px",
  cursor: "pointer",
});

export const profileImage = style({
  width: "31px",
  height: "31px",
  borderRadius: "50%",
  flexShrink: 0,
  objectFit: "cover",
  backgroundColor: vars.color.gray300,
});

export const writer = style({
  minWidth: 0,
  fontFamily: vars.font.middle,
  fontSize: "16px",
  color: vars.color.black,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const stars = style({
  flexShrink: 0,
  color: "#d3d7dc",
  fontSize: "19px",
  letterSpacing: 0,
});

export const starFilled = style({
  color: "#ffd966",
});

export const likeButton = style({
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "4px",
  minWidth: "48px",
  height: "30px",
  padding: "0 9px",
  border: "1px solid #f0b8c2",
  borderRadius: "999px",
  backgroundColor: "#fff7f9",
  color: "#d84a5f",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  lineHeight: 1,
  cursor: "pointer",
  flexShrink: 0,

  selectors: {
    "&:hover": {
      backgroundColor: "#fff1f4",
      borderColor: "#e98597",
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.55,
    },
  },
});

export const likeIcon = style({
  width: "18px",
  height: "18px",
  flexShrink: 0,
});

export const likeCount = style({
  minWidth: "16px",
  textAlign: "left",
});

export const contentLabel = style({
  fontFamily: vars.font.middle,
  fontSize: "12px",
  color: "#777777",
});

export const reportContentWrap = style({
  maxHeight: "102px",
  overflow: "hidden",
  transition: "max-height 240ms ease",
});

export const reportContentWrapOpen = style([
  reportContentWrap,
  {
    maxHeight: "3000px",
  },
]);

export const reportContent = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.7,
  color: vars.color.black,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const expandButton = style({
  alignSelf: "center",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  width: "34px",
  height: "34px",
  padding: 0,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  cursor: "pointer",
  transition: "background-color 180ms ease, border-color 180ms ease, box-shadow 180ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#ffffff",
      borderColor: "#cfd4da",
      boxShadow: "0 6px 14px rgba(0, 0, 0, 0.08)",
    },
  },
});

export const expandArrow = style({
  width: "20px",
  height: "20px",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  transform: "rotate(0deg)",
  transition: "transform 200ms ease",
});

export const expandArrowOpen = style([
  expandArrow,
  {
    transform: "rotate(180deg)",
  },
]);

export const expandArrowIcon = style({
  width: "18px",
  height: "18px",
  fill: "currentColor",
});

export const empty = style({
  margin: "28px 0 0",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  color: "#777777",
  textAlign: "center",
});
