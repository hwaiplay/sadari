import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const periodModal = style({
  height: "290px",
  "@media": {
    "screen and (max-width: 480px)": {
      height: "280px",
    },
  },
});

export const periodBody = style({
  minHeight: "110px",
  padding: "18px 0 12px",
});

export const periodEditor = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "14px",
});

export const periodStepper = style({
  width: "220px",
  minHeight: "48px",
  display: "grid",
  gridTemplateColumns: "48px minmax(0, 1fr) 48px",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: "#fafafa",
});

export const periodStepButton = style({
  padding: 0,
  border: 0,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  fontFamily: vars.font.heading,
  fontSize: "18px",
  cursor: "pointer",
  selectors: {
    "&:not(:disabled):hover": {
      backgroundColor: vars.color.gray200,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "-2px",
    },
    "&:disabled": {
      color: vars.color.gray400,
      cursor: "not-allowed",
    },
  },
});

export const periodValue = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1,
});

export const periodGuide = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  textAlign: "center",
});
