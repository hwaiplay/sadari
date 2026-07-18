import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const content = style({
  maxWidth: "600px",
  width: "100%",
  margin: "0 auto",
  padding: "18px 20px 44px",
  display: "flex",
  flexDirection: "column",
  gap: "22px",
});

export const searchForm = style({
  display: "grid",
  gridTemplateColumns: "1fr 72px",
  gap: "8px",
});

export const searchInput = style({
  width: "100%",
  height: "42px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  padding: "0 16px",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
  boxSizing: "border-box",
});

export const searchButton = style({
  height: "42px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.black,
  color: "#ffffff",
  fontFamily: vars.font.middle,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      opacity: 0.6,
      cursor: "not-allowed",
    },
  },
});

export const resultList = style({
  display: "flex",
  flexDirection: "column",
  gap: "26px",
});

export const resultCard = style({
  display: "flex",
  flexDirection: "column",
  gap: "14px",
  paddingBottom: "26px",
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const coverArea = style({
  display: "flex",
  justifyContent: "center",
});

export const coverFrame = style({
  width: "112px",
  minHeight: "156px",
  borderRadius: "6px",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray400}`,
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.08)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "17px",
  lineHeight: 1.4,
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
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.7,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const actions = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "10px",
});

export const actionButton = style({
  height: "40px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const primaryButton = style([
  actionButton,
  {
    backgroundColor: vars.color.black,
    color: "#ffffff",
  },
]);

export const loadMoreButton = style({
  width: "100%",
  height: "42px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
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
