/**
 * fileName       : tokens.css.ts
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 디자인 시스템 토큰 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

import { createGlobalTheme } from "@vanilla-extract/css";
export const vars = createGlobalTheme(":root", {
  color: {
    black: "#151515",
    background: "#f0f0f0",
    black025: "#15151550",
    gray100: "#f0f0f0",
    gray200: "#e5e5e5",
    gray300: "#d9d9d9",
    gray400: "#D0D0D0",
    gray500: "#C1C1C1",
  },

  space: {
    sm: "8px",
    md: "16px",
    lg: "24px",
    xl: "32px",
  },

  font: {
    body: `"PretendardRegular", system-ui, sans-serif`,
    heading: `"PretendardBold", system-ui, sans-serif`,
    middle: `"PretendardSemiBold", system-ui, sans-serif`,
  },

  fontSize: {
    caption: "12px",
    body: "14px",
    title: "24px",
    hero: "32px",
  },

  radius: {
    sm: "7px",
    md: "12px",
    xl: "55555px",
  },

  bookHeight: {
    sm: "253px",
    md: "273px",
  },

  headerHeight: "60px",
});
