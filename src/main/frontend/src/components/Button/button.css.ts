import { style, styleVariants } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";

export const buttonBase = style({
  padding: `0 ${vars.space.md}`,
  borderRadius: vars.radius.xl,

  display: "flex",
  alignItems: "center",
  justifyContent: "center",

  fontFamily: vars.font.heading,
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
});

export const buttonSize = styleVariants({
  sm: {
    height: "32px",
    padding: "0 12px",
    fontSize: "14px",
  },

  md: {
    height: "40px",
    fontSize: "14px",
  },

  lg: {
    height: "48px",
    fontSize: "16px",
  },
});
