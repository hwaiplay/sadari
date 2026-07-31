import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";
import { media } from "@/app/styles/responsive.css";

export const page = style({
  minHeight: "calc(100svh - 52px - 52px)",
  backgroundColor: vars.color.background,
});

export const content = style({
  maxWidth: "600px",
  width: "100%",
  margin: "0 auto",
  padding: "18px 0",
  display: "flex",
  flexDirection: "column",
  gap: "22px",
});

export const searchBar = style({
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  gap: "10px",
  width: "100%",
  marginBottom: "12px",

  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg}`,
    },
  },
});

export const searchLabel = style({
  position: "relative",
  display: "block",
  width: "100%",
  minWidth: 0,
  flex: 1,
});

export const hiddenLabel = style({
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

export const searchInput = style({
  width: "100%",
  height: "38px",
  padding: "0 38px 0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "12px",
  outline: "none",

  selectors: {
    "&::placeholder": {
      color: vars.color.gray500,
    },
    "&:focus": {
      borderColor: vars.color.black,
    },
  },
});

export const searchButton = style({
  position: "absolute",
  top: "50%",
  right: "6px",
  width: "32px",
  height: "32px",
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  color: vars.color.black,
  transform: "translateY(-50%)",
  cursor: "pointer",

  selectors: {
    "&:hover": {
      backgroundColor: "#f3f3f3",
    },
    "&:disabled": {
      opacity: 0.6,
      cursor: "not-allowed",
    },
  },
});

export const searchIcon = style({
  width: "22px",
  height: "22px",
  flexShrink: 0,
});

export const resultList = style({
  display: "flex",
  flexDirection: "column",
  gap: "20px",
});

export const resultCard = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "14px",
  padding: "40px 22px 30px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
  boxSizing: "border-box",
});

export const coverArea = style({
  display: "flex",
  justifyContent: "center",
});

export const coverFrame = style({
  width: "126px",
  aspectRatio: "2 / 3",
  borderRadius: "6px",
  overflow: "hidden",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookMeta = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "5px",
  textAlign: "center",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
  color: vars.color.black,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const meta = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
  color: "#666666",
  textAlign: "center",
});

export const description = style({
  width: "100%",
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.7,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const actions = style({
  width: "100%",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  flexWrap: "wrap",
  gap: "8px",
});

export const actionButton = style({
  height: "34px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const primaryButton = style([
  actionButton,
  {
    backgroundColor: "#ffffff",
    color: vars.color.black,
  },
]);

export const loadMoreButton = style({
  width: "100%",
  height: "42px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      opacity: 0.6,
      cursor: "not-allowed",
    },
  },
});

export const emptyMessage = style({
  margin: "40px 0 0",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  color: "#777777",
  textAlign: "center",
});
