import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const book = style({
  display: "block",
  minWidth: 0,
  textDecoration: "none",
});

export const coverWrap = style({
  position: "relative",
  width: "100%",
  aspectRatio: "2 / 3",
  borderRadius: "6px",
  backgroundColor: "#ffffff",
  overflow: "hidden",
  border: '1px solid',
  borderColor:vars.color.gray200,
  boxShadow: 'rgba(99, 99, 99, 0.2) 0px 2px 8px 0px'
});

export const cover = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const coverFallback = style({
  width: "100%",
  height: "100%",
  padding: "14px 10px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  backgroundColor: "#f7f3ed",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  lineHeight: 1.35,
  textAlign: "center",
  wordBreak: "keep-all",
  overflow: "hidden",
});

export const readingBadge = style({
  position: "absolute",
  top: "7px",
  left: "7px",
  maxWidth: "calc(100% - 14px)",
  padding: "4px 7px",
  borderRadius: vars.radius.xl,
  backgroundColor: "rgba(21, 21, 21, 0.78)",
  color: "#ffffff",
  fontFamily: vars.font.middle,
  fontSize: "11px",
  lineHeight: 1,
  whiteSpace: "nowrap",
  boxShadow: "0 2px 6px rgba(0, 0, 0, 0.18)",
});

export const title = style({
  display: "none",
});

export const author = style({
  display: "none",
});

export const tilt = style({
  transform: "none",
});
