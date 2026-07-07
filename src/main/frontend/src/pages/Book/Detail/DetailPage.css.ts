import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
  padding: "24px 0 48px",
});

export const detail = style({
  display: "flex",
  flexDirection: "column",
  gap: "24px",
});

export const actions = style({
  display: "flex",
  justifyContent: "flex-end",
  gap: "8px",
});

export const actionButton = style({
  minHeight: "34px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  padding: "0 14px",
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: vars.color.black,
  cursor: "pointer",
});

export const deleteButton = style([
  actionButton,
  {
    color: "#d54848",
  },
]);

export const header = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "18px",
  textAlign: "center",
});

export const coverFrame = style({
  width: "168px",
  borderRadius: "8px",
  overflow: "hidden",
  border: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
  boxShadow: "0 14px 32px rgba(0, 0, 0, 0.1)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  aspectRatio: "3 / 4.2",
  objectFit: "cover",
});

export const title = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1.35,
  color: vars.color.black,
});

export const meta = style({
  margin: "6px 0 0",
  color: "#767676",
  fontSize: "14px",
});

export const panel = style({
  display: "flex",
  flexDirection: "column",
  gap: "18px",
  borderTop: `1px solid ${vars.color.gray200}`,
  paddingTop: "24px",
});

export const sectionTitle = style({
  margin: "0 0 10px",
  fontFamily: vars.font.heading,
  fontSize: "16px",
  color: vars.color.black,
});

export const period = style({
  display: "inline-flex",
  alignItems: "center",
  minHeight: "36px",
  padding: "0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  fontSize: "14px",
});

export const stars = style({
  display: "flex",
  gap: "2px",
  color: "#d3d7dc",
  fontSize: "28px",
  lineHeight: 1,
});

export const starFilled = style({
  color: "#ffd966",
});

export const content = style({
  margin: 0,
  color: "#333333",
  fontSize: "15px",
  lineHeight: 1.75,
  whiteSpace: "pre-wrap",
});

export const bookInfoGrid = style({
  display: "grid",
  gridTemplateColumns: "72px 1fr",
  gap: "10px 14px",
  fontSize: "14px",
});

export const infoLabel = style({
  color: "#767676",
  fontFamily: vars.font.middle,
});

export const infoValue = style({
  margin: 0,
  color: vars.color.black,
});

export const toggleButton = style({
  marginTop: "10px",
  border: 0,
  backgroundColor: "transparent",
  padding: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "14px",
  cursor: "pointer",
});
