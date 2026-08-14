import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const card = style({
  display: "flex",
  flexDirection: "column",
  width: "100%",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 22,
  background: vars.color.background,
  boxSizing: "border-box",
});

export const summary = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  gap: 10,
  padding: 20,
  background: vars.color.gray100,
});

export const categoryChips = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 6,
  minHeight: 22,
});

export const categoryChip = style({
  padding: "4px 12px",
  borderRadius: 200,
  background: vars.color.gray900,
  color: vars.color.background,
  fontFamily: vars.font.medium,
  fontSize: 12,
  lineHeight: 1.2,
  letterSpacing: "-0.12px",
});

export const clubCopy = style({
  display: "flex",
  flexDirection: "column",
  gap: 4,
});

export const clubTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: 18,
  lineHeight: 1.2,
  letterSpacing: "-0.18px",
});

export const clubMeta = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.2,
  letterSpacing: "-0.14px",
});

export const actionArea = style({
  display: "flex",
  flexDirection: "column",
  gap: 20,
  padding: 20,
});

export const description = style({
  width: "100%",
  minHeight: 17,
  margin: 0,
  overflow: "hidden",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.2,
  letterSpacing: "-0.14px",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const actionButton = style({
  height: 44,
  fontFamily: vars.font.semibold,
  letterSpacing: "-0.14px",
});

export const actionIcon = style({
  width: 16,
  height: 16,
});
