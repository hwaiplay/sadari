import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const coverArea = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "12px",
});

export const coverFrame = style({
  width: "128px",
  minHeight: "178px",
  borderRadius: "6px",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray400}`,
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.08)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookMeta = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "4px",
  textAlign: "center",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "17px",
  lineHeight: 1.4,
  color: vars.color.black,
  wordBreak: "keep-all",
});

export const bookSubInfo = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.45,
  color: "#666666",
});

export const changeButton = style({
  height: "34px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});
