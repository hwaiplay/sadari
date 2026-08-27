import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const screen = style({
  position: "fixed",
  inset: 0,
  zIndex: 4000,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  minHeight: "100dvh",
  padding: "24px 16px",
  background: vars.color.background,
});

export const content = style({
  width: "100%",
  maxWidth: "360px",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  textAlign: "center",
});

export const iconWrap = style({
  width: "88px",
  height: "88px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  marginBottom: "24px",
  borderRadius: "50%",
  background: vars.color.gray100,
});

export const icon = style({
  width: "52px",
  height: "52px",
  stroke: vars.color.gray700,
  strokeWidth: 4,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const textGroup = style({
  marginBottom: "32px",
});

export const title = style({
  margin: "0 0 12px",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1.4,
});

export const description = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "16px",
  lineHeight: 1.6,
  whiteSpace: "pre-line",
});

export const action = style({
  width: "100%",
  maxWidth: "240px",
});
