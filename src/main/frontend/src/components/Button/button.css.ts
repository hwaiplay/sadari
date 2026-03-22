import { style, styleVariants } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";

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
    background: vars.color.black,
    color: "white",
  },
  secondary: {
    background: vars.color.black025,
    color: "white",
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
