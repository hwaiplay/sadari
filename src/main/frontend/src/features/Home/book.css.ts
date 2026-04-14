import { style } from "@vanilla-extract/css";
import { vars } from "../../app/styles/tokens.css";
import { media } from "../../app/styles/responsive.css";

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
  height: "100%",
  borderRadius: vars.radius.sm,

  "@media": {
    [media.desktop]: {
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

// 기울기
export const tilt = style({
  transform: "rotate(8deg) translateY(-1px) translateX(-16px)",

  "@media": {
    [media.tablet]: {
      transform: "rotate(8deg) translateY(-1px) translateX(-26px)",
    },
  },
});
