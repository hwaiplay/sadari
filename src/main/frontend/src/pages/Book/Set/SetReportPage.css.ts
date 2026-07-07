import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  minHeight: "100vh",
  backgroundColor: "#ffffff",
});

export const form = style({
  maxWidth: "420px",
  width: "100%",
  margin: "0 auto",
  padding: "18px 20px 44px",
  display: "flex",
  flexDirection: "column",
  gap: "28px",
});

export const topBar = style({
  display: "grid",
  gridTemplateColumns: "44px 1fr 56px",
  alignItems: "center",
  minHeight: "44px",
});

export const backButton = style({
  width: "36px",
  height: "36px",
  border: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontSize: "30px",
  lineHeight: 1,
  cursor: "pointer",
});

export const brand = style({
  margin: 0,
  textAlign: "center",
  fontFamily: vars.font.heading,
  fontSize: "21px",
  letterSpacing: "0",
});

export const saveButton = style({
  width: "100%",
  height: "44px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "14px",
  cursor: "pointer",
});

export const searchBookArea = style({
  display: "flex",
  justifyContent: "center",
});

export const fieldStack = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px",
});

export const statusContainer = style({
  display: "flex",
  gap: "10px",
  width: "100%",
});

export const statusOption = style({
  flex: 1,
  minWidth: 0,
});

export const hiddenInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
});

export const statusPill = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  minHeight: "36px",
  padding: "0 12px",
  borderRadius: "999px",
  border: `1px solid ${vars.color.gray400}`,
  backgroundColor: "#f7f7f7",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  whiteSpace: "nowrap",
  cursor: "pointer",

  selectors: {
    [`${hiddenInput}:checked + &`]: {
      backgroundColor: vars.color.black,
      borderColor: vars.color.black,
      color: "#ffffff",
    },
  },
});

export const dateRow = style({
  display: "none",
  gridTemplateColumns: "44px 1fr",
  alignItems: "center",
  gap: "10px",
});

export const inputLabel = style({
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: vars.color.black,
});

export const input = style({
  width: "100%",
  height: "36px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  padding: "0 16px",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
  boxSizing: "border-box",
});

export const starGroup = style({
  display: "flex",
  alignItems: "center",
  gap: "3px",
});

export const starLabel = style({
  color: "#d3d7dc",
  fontSize: "34px",
  lineHeight: 1,
  cursor: "pointer",
});

export const starActive = style({
  color: "#ffd966",
});

export const textAreaWrap = style({
  position: "relative",
});

export const textArea = style({
  width: "100%",
  minHeight: "150px",
  resize: "vertical",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "16px",
  backgroundColor: "#ffffff",
  padding: "14px 16px",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
  boxSizing: "border-box",
});

export const counter = style({
  position: "absolute",
  top: "-22px",
  right: "4px",
  fontSize: "11px",
  color: vars.color.gray500,
});
