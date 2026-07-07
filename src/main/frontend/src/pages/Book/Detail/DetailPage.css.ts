import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const detail = style({
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
  paddingTop: "4px",
  paddingBottom: "10px",
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

export const bookInfoButton = style({
  marginTop: "8px",
  height: "36px",
  padding: "0 16px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const section = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px",
  paddingTop: "2px",
});

export const sectionTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "15px",
  color: vars.color.black,
});

export const statusPill = style({
  width: "fit-content",
  minHeight: "30px",
  padding: "0 12px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: "999px",
  backgroundColor: "#f5f5f5",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
});

export const dateStack = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const dateRow = style({
  display: "grid",
  gridTemplateColumns: "52px 1fr",
  alignItems: "center",
  gap: "14px",
});

export const dateLabel = style({
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: "#777777",
});

export const dateValue = style({
  minHeight: "auto",
  padding: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
});

export const stars = style({
  display: "flex",
  gap: "3px",
  color: "#d3d7dc",
  fontSize: "34px",
  lineHeight: 1,
});

export const starFilled = style({
  color: "#ffd966",
});

export const contentBox = style({
  margin: 0,
  padding: "2px 0 0",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.8,
  color: vars.color.black,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const actions = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "10px",
  paddingTop: "4px",
});

export const actionButton = style({
  height: "44px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "14px",
  cursor: "pointer",
});

export const deleteButton = style([
  actionButton,
  {
    borderColor: "#d84a3a",
    color: "#d84a3a",
  },
]);
