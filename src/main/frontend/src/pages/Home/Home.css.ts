import { style } from "@vanilla-extract/css";
import { vars } from "../../app/styles/tokens.css";
import { media } from "../../app/styles/responsive.css";

export const emptyHomeContainer = style({
  width: "100%",
  minHeight: "100svh",
  padding: 0,
  backgroundImage: 'url("/img/common/background-empty.png")',
  backgroundRepeat: "no-repeat",
  backgroundSize: "cover",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
});

export const emptyTitle = style({
  fontSize: vars.fontSize.title,
  fontFamily: vars.font.heading,
  textAlign: "center",
});

export const homeContainer = style({
  minHeight: "100svh",
  paddingTop: "82px",
  paddingBottom: "96px",
  backgroundColor: "#f3f3f3",
});

export const monthGroupStack = style({
  display: "flex",
  flexDirection: "column",
  gap: "54px",
});

export const monthGroup = style({
  display: "flex",
  flexDirection: "column",
  gap: "18px",
});

export const monthLabel = style({
  position: "relative",
  width: "fit-content",
  paddingLeft: "13px",
  fontFamily: vars.font.middle,
  fontSize: "13px",
  lineHeight: 1,
  color: "#575757",

  selectors: {
    "&::before": {
      content: "",
      position: "absolute",
      left: 0,
      top: "50%",
      width: "5px",
      height: "5px",
      borderRadius: "50%",
      backgroundColor: "#151515",
      transform: "translateY(-50%)",
    },
  },
});

export const bookGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
  columnGap: "28px",
  rowGap: "52px",
  width: "100%",

  "@media": {
    [media.tablet]: {
      columnGap: "42px",
      rowGap: "72px",
    },
  },
});

export const row5Container = style({
  display: "none",
});

export const row = style({
  display: "none",
});

export const row5 = style({
  display: "none",
});

export const row6 = style({
  display: "none",
});
