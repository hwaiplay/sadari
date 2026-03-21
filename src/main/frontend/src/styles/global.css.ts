/**
 * fileName       : global.css.ts
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 글로벌 스타일 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

import { globalStyle } from "@vanilla-extract/css";
import { globalFontFace } from "@vanilla-extract/css";

globalFontFace("PretendardBold", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 700,
});

globalFontFace("PretendardSemiBold", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 600,
});

globalFontFace("PretendardRegular", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 400,
});

globalStyle("html, body", {
  margin: 0,
  padding: 0,
  fontFamily: "PretendardRegular",
});

globalStyle("*", {
  margin: 0,
  padding: 0,
});

globalStyle("*, *::before, *::after", {
  boxSizing: "border-box",
});

globalStyle("button", {
  backgroundColor: "#ffffff",
  border: "none",
});
