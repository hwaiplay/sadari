import { vars } from "@/app/styles/tokens.css";
import { style, styleVariants } from "@vanilla-extract/css";

export const buttonBase = style({
  padding: `0 ${vars.space.md}`,
  borderRadius: vars.radius.xl,

  display: "flex",
  alignItems: "center",
  justifyContent: "center",

  fontFamily: vars.font.heading,
  fontSize: vars.fontSize.body,
  lineHeight: 1,

  border: "none",
  cursor: "pointer",

  ":disabled": {
    opacity: 0.5,
    cursor: "not-allowed",
  },
});

export const buttonVariant = styleVariants({
  primary: {
    border: `1px solid ${vars.color.gray700}`,
    background: "#ffffff",
    color: vars.color.gray900,
  },
  secondary: {
    border: `1px solid ${vars.color.gray400}`,
    background: "#ffffff",
    color: vars.color.gray600,
  },
  disable: {
    background: vars.color.gray200,
    border: `1px solid ${vars.color.gray500}`,
    color: vars.color.gray500,
  },
});

export const buttonSize = styleVariants({
  sm: {
    height: "32px",
    padding: "0 12px",
  },

  md: {
    height: "40px",
  },

  lg: {
    height: "48px",
    fontSize: "16px",
  },
});
