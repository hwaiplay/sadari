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

globalStyle("*, *::before, *::after", {
  boxSizing: "border-box",
});
