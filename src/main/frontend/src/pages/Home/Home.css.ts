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

export const row5Container = style({
  height: "calc(33% + 90px)",
  backgroundImage: 'url("/img/common/background-top.png")',
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
    [media.desktop]: {
      gridTemplateColumns: "repeat(5, 74px)",
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
});
