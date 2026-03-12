import { style, styleVariants } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";

export const buttonBase = style({
  height: "48px",
  padding: `0 ${vars.space.lg}`,
  borderRadius: vars.radius.xl,

  display: "flex",
  alignItems: "center",
  justifyContent: "center",

  fontSize: vars.fontSize.body,
  fontWeight: 700,
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
});
