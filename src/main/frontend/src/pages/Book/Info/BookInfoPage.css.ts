import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const content = style({
  maxWidth: "420px",
  width: "100%",
  margin: "0 auto",
  padding: "22px 20px 44px",
  display: "flex",
  flexDirection: "column",
  gap: "26px",
});

export const header = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  textAlign: "center",
  gap: "8px",
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

export const title = style({
  margin: "12px 0 0",
  fontFamily: vars.font.heading,
  fontSize: "22px",
  lineHeight: 1.35,
  color: vars.color.black,
  wordBreak: "keep-all",
});

export const meta = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.45,
  color: "#666666",
});

export const section = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px",
});

export const sectionTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "15px",
  color: vars.color.black,
});

export const infoGrid = style({
  display: "grid",
  gridTemplateColumns: "58px 1fr",
  gap: "9px 12px",
});

export const infoLabel = style({
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: "#777777",
});

export const infoValue = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const description = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.8,
  color: vars.color.black,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const selectButton = style({
  height: "44px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.black,
  color: "#ffffff",
  fontFamily: vars.font.heading,
  fontSize: "14px",
  cursor: "pointer",
});
