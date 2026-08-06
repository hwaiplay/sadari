import { vars } from "@/app/styles/tokens.css";
import { globalStyle, style } from "@vanilla-extract/css";

export const notFoundAlert = style({});

globalStyle(`${notFoundAlert} .sadari-swal-button`, {
  border: `1px solid ${vars.color.gray700}`,
  backgroundColor: vars.color.background,
  color: vars.color.gray900,
});

globalStyle(`${notFoundAlert} .sadari-swal-button:hover`, {
  backgroundColor: vars.color.gray100,
});

globalStyle(`${notFoundAlert} .sadari-swal-button:focus-visible`, {
  outline: "2px solid #78b991",
  outlineOffset: "3px",
});
