/**
 * fileName       : Home.css.ts
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 메인 페이지 CSS
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";
import { breakpoints } from "../../styles/breakpoints";

export const emptyHomeContainer = style({
  width: "100%",
  height: "100svh",
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
  height: "calc(33% + 90px)",
  gridTemplateRows: "minmax(0, 1fr)",

  "@media": {
    [`screen and (max-width: 767px)`]: {
      // height: `calc(${vars.bookHeight.sm} + 106px - ${vars.headerHeight} + 30px)`,
      gridTemplateColumns: "repeat(5, 50px)",
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      gridTemplateColumns: "repeat(5, 74px)",
      // height: `calc(${vars.bookHeight.md} + 40px + ${vars.headerHeight})`,
    },
  },
});

export const row6 = style({
  height: "calc(33% + 30px)",
  paddingTop: "30px",
  gridTemplateColumns: "repeat(6, 1fr)",
  gridTemplateRows: "minmax(0, 1fr)",
  justifyContent: "center",
  backgroundImage: 'url("/img/common/background-middle.png")',
  "@media": {
    [`screen and (max-width: 767px)`]: {
      // height: `calc(${vars.bookHeight.sm} + 30px)`,
    },
    [`screen and (min-width: ${breakpoints.tablet}px)`]: {
      // height: `calc(${vars.bookHeight.md} + 40px)`,
    },
  },
});
