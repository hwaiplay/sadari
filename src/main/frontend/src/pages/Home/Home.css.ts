import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";
import { book } from "../../components/Book/book.css";
import { breakpoints } from "../../styles/breakpoints";

export const homeContainer = style({
  width: "100%",
});

export const bgContainer = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
});

export const rowContainer = style({
  display: "grid",
  alignItems: "flex-end",
  gap: vars.space.sm,
  margin: " 0 auto",
  paddingBottom: vars.space.sm,
  backgroundRepeat: "no-repeat",
  backgroundSize: "cover",
  width: "100%",

  "@media": {
    [`screen and (max-width: 767px)`]: {
      gap: vars.space.sm,
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      gap: "12px",
    },
  },
});

export const row5 = style({
  justifyContent: "flex-end",
  backgroundImage: 'url("/img/common/background-top.png")',
  paddingTop: vars.headerHeight,

  "@media": {
    [`screen and (max-width: 767px)`]: {
      height: `calc(${vars.bookHeight.sm} + 106px - ${vars.headerHeight} + 30px)`,
      gridTemplateColumns: "repeat(5, 1fr)",
      paddingLeft: "calc(16px * 3)",
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      gridTemplateColumns: "repeat(5, 74px)",
      height: `calc(${vars.bookHeight.md} + 40px + ${vars.headerHeight})`,
    },
  },
});

export const row6 = style({
  gridTemplateColumns: "repeat(6, 1fr)",
  justifyContent: "center",
  backgroundImage: 'url("/img/common/background-middle.png")',
  "@media": {
    [`screen and (max-width: 767px)`]: {
      height: `calc(${vars.bookHeight.sm} + 30px)`,
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      height: `calc(${vars.bookHeight.md} + 40px)`,
    },
  },
});
