/**
 * fileName       : tokens.css.ts
 * author         : HanWon.Jang
 * date           : 2026-03-19
 * description    : 디자인 시스템 토큰 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        HanWon.Jang       주석 추가
 */

import { createGlobalTheme } from "@vanilla-extract/css";
export const vars = createGlobalTheme(":root", {
  color: {
    black: "#151515",
    background: "#ffffff",
    black025: "#15151550",
    gray100: "#F6F8F9",
    gray200: "#E1E3E5",
    gray300: "#D5D8D9",
    gray400: "#CCCFD0",
    gray500: "#BEC0C1",
    gray600: "#7E8587",
    gray700: "#535555",
    gray900: "#293038",
    darkGray: "#171A1F",
    brand: "#9EDFC2",
    brandBg: "#E4F6E9",
    brandText: "#2F8F64",
    negative: "#F4A7AD",
    negativeBg: "#FFF1F3",
    negativeText: "#D84A5F",
    yellow: "#F7D98B",
    yellowBg: "#FFF7E3",
    yellowText: "#FFAF3F",
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
    medium: `"PretendardMedium", system-ui, sans-serif`,
    semibold: `"PretendardSemiBold", system-ui, sans-serif`,
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

  headerHeight: "52px",
  navHeight: "60px"
});
