import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const header = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: 10,
  textAlign: "center",
});

export const closeButton = style({
  position: "absolute",
  zIndex: 2,
  top: 20,
  right: 20,
  width: 32,
  height: 32,
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f5",
  color: vars.color.gray700,
  fontFamily: vars.font.body,
  fontSize: 22,
  lineHeight: "32px",
  cursor: "pointer",
  selectors: {
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.gray200,
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brandText}`,
      outlineOffset: 2,
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.5,
    },
  },
});

export const roundBadge = style({
  padding: "6px 12px",
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: 12,
});

export const title = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: 22,
  lineHeight: 1.3,
});

export const description = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.5,
});

export const deadline = style({
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: 14,
});

export const candidateList = style({
  display: "flex",
  flexDirection: "column",
  gap: 10,
});

export const candidate = style({
  minHeight: 64,
  padding: 12,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 14,
  display: "flex",
  alignItems: "center",
  gap: 12,
  backgroundColor: vars.color.background,
  cursor: "pointer",
  selectors: {
    "&:has(input:checked)": {
      backgroundColor: vars.color.brandBg,
      boxShadow: `0 0 0 2px ${vars.color.brand}`,
    },
    "&:has(input:focus-visible)": {
      outline: `2px solid ${vars.color.brandText}`,
      outlineOffset: 2,
    },
  },
});

export const radio = style({
  width: 20,
  height: 20,
  margin: 0,
  accentColor: vars.color.brandText,
});

export const profile = style({
  width: 40,
  height: 40,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  objectFit: "cover",
});

export const candidateName = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
});

export const guide = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: 1.5,
  textAlign: "center",
});

export const empty = style({
  margin: 0,
  padding: 24,
  borderRadius: 14,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.5,
  textAlign: "center",
});
