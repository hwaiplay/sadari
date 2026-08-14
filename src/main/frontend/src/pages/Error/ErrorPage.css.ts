import { vars } from "@/app/styles/tokens.css";
import { globalStyle, style } from "@vanilla-extract/css";

export const notFoundAlert = style({});

globalStyle(`${notFoundAlert} .sadari-swal-button`, {
  background: vars.color.gray900,
  color: "#fff",
  transition: "background 160ms ease"
});

globalStyle(`${notFoundAlert} .sadari-swal-button:hover`, {
  background: vars.color.darkGray,
});

globalStyle(`${notFoundAlert} .sadari-swal-button:focus-visible`, {
  outline: "2px solid #78b991",
  outlineOffset: "3px",
});
