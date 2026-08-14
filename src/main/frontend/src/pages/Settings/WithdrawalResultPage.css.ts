import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "100svh",
  margin: "0 auto",
  padding: "20px 18px 38px",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
  display: "flex",
  flexDirection: "column",
});

export const logoHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  minHeight: "52px",
});

export const logo = style({
  display: "block",
  width: "110px",
  height: "auto",
});

export const content = style({
  width: "100%",
  flex: 1,
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  gap: "32px",
  padding: "36px 0 54px",
  boxSizing: "border-box",
});

export const statusSection = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  textAlign: "center",
});

const markBase = {
  width: "66px",
  height: "66px",
  display: "grid",
  placeItems: "center",
  borderRadius: "50%",
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1,
} as const;

export const successMark = style({
  ...markBase,
  backgroundColor: "#e8f6ed",
  color: "#34704d",
});

export const failMark = style({
  ...markBase,
  backgroundColor: "#fff0f0",
  color: "#bd4343",
});

export const heading = style({
  margin: "24px 0 0",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1.35,
  letterSpacing: 0,
});

export const description = style({
  maxWidth: "420px",
  margin: "12px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.65,
  wordBreak: "keep-all",
});

export const guideSection = style({
  position: "relative",
  width: "100%",
  padding: "20px 18px 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
});

export const sectionTitle = style({
  margin: "0 0 10px",
  padding: "0 0 0 6px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.3,
  letterSpacing: 0,
  textAlign: "left",
});

export const guideList = style({
  margin: 0,
});

export const guideRow = style({
  display: "grid",
  gridTemplateColumns: "76px minmax(0, 1fr)",
  gap: "18px",
  padding: "14px 4px",
  borderBottom: `1px solid ${vars.color.gray200}`,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  selectors: {
    "&:last-child": {
      borderBottom: 0,
    },
  },
});

export const guideLabel = style({
  color: vars.color.gray600,
});

export const guideValue = style({
  minWidth: 0,
  margin: 0,
  color: vars.color.gray900,
  wordBreak: "keep-all",
});

export const successText = style({
  color: "#34704d",
  fontFamily: vars.font.semibold,
});

export const failText = style({
  color: "#bd4343",
  fontFamily: vars.font.semibold,
});

export const primaryLink = style({
  width: "100%",
  minHeight: "48px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  border: `1px solid ${vars.color.gray700}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  textDecoration: "none",
  boxSizing: "border-box",
  transition:
    "background-color 160ms ease, border-color 160ms ease, color 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray700,
      backgroundColor: vars.color.gray100,
      color: vars.color.gray900,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "3px",
    },
  },
});
