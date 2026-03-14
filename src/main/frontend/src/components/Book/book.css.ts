import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";
import { breakpoints } from "../../styles/breakpoints";

export const book = style({
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
  textOrientation: "mixed",
  fontFamily: vars.font.body,
  padding: `${vars.space.md} 0`,
  fontStyle: "normal",
  textDecoration: "none",
  color: vars.color.black,

  "@media": {
    [`screen and (max-width: 767px)`]: {
      height: vars.bookHeight.sm,
      borderRadius: vars.radius.sm,
      maxWidth: "48px",
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      height: vars.bookHeight.md,
      borderRadius: vars.radius.md,
    },
  },
});

export const title = style({
  fontSize: "1em",
  writingMode: "vertical-rl",
  letterSpacing: "-1px",
  textAlign: "center",
  height: "100%",
  overflow: "hidden",
});

export const author = style({
  fontSize: "11px",
  marginTop: "8px",
  position: "absolute",
});

export const tilt = style({
  transform: "rotate(8deg) translateY(-1px) translateX(-16px)",
});
