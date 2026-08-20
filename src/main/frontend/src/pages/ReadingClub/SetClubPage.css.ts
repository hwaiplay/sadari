import { selectableOption } from "@/app/styles/controls.css";
import { vars } from "@/app/styles/tokens.css";
import { globalStyle, style } from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  flexDirection: "column",
  gap: 32,
  width: "100%",
  maxWidth: 600,
  margin: "0 auto",
  padding: "20px 0 34px",
});

export const form = style({
  display: "flex",
  flexDirection: "column",
  gap: 26,
  paddingBottom: 68,
});

export const field = style({
  display: "flex",
  flexDirection: "column",
  gap: 8,
});

export const label = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
});

export const input = style({
  width: "100%",
  minHeight: 42,
  padding: "0 16px",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 8,
  background: "#fff",
  fontFamily: vars.font.body,
  fontSize: 14,
  outline: "none",
  selectors: {
    "&::placeholder": { color: vars.color.gray500 },
    "&:focus": { outline: "2px solid #78b991", outlineOffset: 1 },
  },
});

export const textarea = style([
  input,
  {
    minHeight: 128,
    padding: 16,
    resize: "none",
    lineHeight: 1.55,
  },
]);

export const chipsContainer = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 8,
});

export const chips = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 8,
});

export const chip = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: 4,
  padding: "8px 14px",
  borderRadius: 999,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1,
});

export const chipDeleteBtn = style({
  paddingBottom: 1,
  background: "transparent",
  cursor: "pointer",
});

export const button = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: 4,
  width: "fit-content",
  padding: "8px 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 999,
  background: "#fff",
  color: vars.color.gray900,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1,
  cursor: "pointer",
  transition: "background-color 160ms ease",
  selectors: {
    "&:hover": { background: vars.color.gray100 },
    "&:disabled": { opacity: 0.5, cursor: "default" },
  },
});

export const buttonDanger = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: 4,
  padding: "4px 10px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 99999,
  background: "#ffffff",
  color: vars.color.negativeText,
  fontFamily: vars.font.medium,
  fontSize: 14,
  lineHeight: 1,
  cursor: "pointer",
  transition: "background-color 160ms ease",
  selectors: {
    "&:hover": { background: vars.color.negativeBg },
  },
});

export const description = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.6,
  whiteSpace: "pre-wrap",
});

export const optionGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: 12,
});

export const option = style([
  selectableOption,
  {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
    justifyContent: "flex-start",
    gap: 8,
    minHeight: 68,
    padding: 16,
    borderRadius: 8,
    cursor: "pointer",
  },
]);

export const optionTitle = style({
  fontFamily: vars.font.semibold,
  fontSize: 14,
});

export const optionDescription = style({
  fontFamily: vars.font.body,
  fontSize: 12,
});

export const stepper = style({
  display: "grid",
  gridTemplateColumns: "42px minmax(72px, 110px) 42px",
  width: "fit-content",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 8,
});

export const stepperButton = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  border: 0,
  background: vars.color.gray100,
  cursor: "pointer",
});

export const stepperInput = style({
  width: "100%",
  height: 42,
  padding: 0,
  boxSizing: "border-box",
  border: 0,
  borderLeft: `1px solid ${vars.color.gray300}`,
  borderRight: `1px solid ${vars.color.gray300}`,
  appearance: "textfield",
  fontFamily: vars.font.semibold,
  fontSize: 14,
  textAlign: "center",
  outline: "none",
});

globalStyle(`${stepperInput}::-webkit-inner-spin-button, ${stepperInput}::-webkit-outer-spin-button`, {
  margin: 0,
  WebkitAppearance: "none",
});

export const questionRow = style({
  display: "flex",
  flexDirection: "column",
  gap: 8,
  marginBottom: 10,
});

export const questionSubjectContainer = style({
  display: "flex",
  justifyContent: "space-between",
});

export const questionSubjectLabel = style({
  fontFamily: vars.font.semibold,
  fontSize: 16,
});
