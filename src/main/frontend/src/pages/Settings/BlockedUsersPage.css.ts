import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "calc(100svh - 120px)",
  margin: "0 auto",
  padding: `10px ${vars.space.md} 96px`,
  boxSizing: "border-box",
  backgroundColor: vars.color.background,
});

export const header = style({
  marginBottom: "22px",
});

export const description = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.55,
});

export const list = style({
  margin: 0,
  padding: 0,
  listStyle: "none",
});

export const item = style({
  minHeight: "72px",
  padding: `${vars.space.sm} 0`,
  borderBottom: `1px solid ${vars.color.gray200}`,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: vars.space.md,
});

export const userInfo = style({
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  gap: "12px",
});

export const avatar = style({
  width: "44px",
  height: "44px",
  flex: "0 0 44px",
  borderRadius: vars.radius.xl,
  objectFit: "cover",
  backgroundColor: vars.color.gray100,
});

export const userName = style({
  minWidth: 0,
  overflow: "hidden",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: vars.fontSize.body,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const empty = style({
  margin: `${vars.space.xl} 0 0`,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.body,
  textAlign: "center",
});
