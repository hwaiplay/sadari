/**
 * fileName       : global.css.ts
 * author         : HanWon.Jang
 * date           : 2026-03-19
 * description    : 글로벌 스타일 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        HanWon.Jang       주석 추가
 */

import { globalStyle } from "@vanilla-extract/css";
import { globalFontFace } from "@vanilla-extract/css";

globalFontFace("PretendardExtraBold", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 800,
});

globalFontFace("PretendardBold", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 700,
});

globalFontFace("PretendardSemiBold", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 600,
});

globalFontFace("PretendardMedium", {
  src: 'url("/fonts/PretendardVariable.woff2") format("woff2")',
  fontWeight: 500,
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

globalStyle("body", {
  msOverflowStyle: "none"
});

globalStyle("::-webkit-scrollbar", {
  display: "none",
})

globalStyle("*", {
  margin: 0,
  padding: 0,
});

globalStyle("*, *::before, *::after", {
  boxSizing: "border-box",
});

globalStyle("button", {
  border: "none",
});
