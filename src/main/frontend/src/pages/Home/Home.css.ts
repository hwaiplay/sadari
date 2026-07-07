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
import { vars } from "../../app/styles/tokens.css";
import { media } from "../../app/styles/responsive.css";

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

export const homeContainer = style({
  height: "100svh",
  padding: 0,
  backgroundColor: "#fffaf3",

  "@media": {
    [media.tablet]: {
      minHeight: "970px",
    },
  },
});

export const row5Container = style({
  width: "100%",
  height: "calc(33% + 90px)",
  backgroundColor: "#fffaf3",
  backgroundImage:
    'linear-gradient(rgba(255, 250, 243, 0.42), rgba(255, 246, 232, 0.34)), url("/img/common/background-top.png")',
  backgroundBlendMode: "screen, normal",
  backgroundRepeat: "no-repeat",
  backgroundSize: "cover",
});

export const row = style({
  display: "grid",
  alignItems: "flex-end",
  gap: vars.space.sm,
  margin: " 0 auto",
  paddingBottom: vars.space.sm,
  backgroundRepeat: "no-repeat",
  backgroundSize: "cover",
  width: "100%",
  padding: vars.space.sm,

  "@media": {
    [media.desktop]: {
      gap: "12px",
    },
  },
});

export const row5 = style({
  justifyContent: "flex-end",
  paddingTop: "90px",
  gridTemplateRows: "minmax(0, 1fr)",
  height: "100%",
  gridTemplateColumns: "repeat(5, 50px)",

  "@media": {
    [media.tablet]: {
      gridTemplateColumns: "repeat(5, 84px)",
    },
  },
});

export const row6 = style({
  height: "calc(33% + 30px)",
  paddingTop: "30px",
  gridTemplateColumns: "repeat(6, 1fr)",
  gridTemplateRows: "minmax(0, 1fr)",
  justifyContent: "center",
  backgroundColor: "#fff6e8",
  backgroundImage:
    'linear-gradient(rgba(255, 250, 243, 0.46), rgba(255, 246, 232, 0.36)), url("/img/common/background-middle.png")',
  backgroundBlendMode: "screen, normal",

  // "@media": {
  //   [media.tablet]: {
  //     gridTemplateColumns: "repeat(6, 84px)",
  //   },
  // },
});
