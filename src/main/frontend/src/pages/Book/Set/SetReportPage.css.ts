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
  border: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "15px",
  cursor: "pointer",
});

export const coverArea = style({
  display: "flex",
  justifyContent: "center",
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
  display: "grid",
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

export const colorGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(6, 1fr)",
  gap: "10px",
});

export const colorOption = style({
  display: "flex",
  justifyContent: "center",
});

export const colorSwatch = style({
  width: "34px",
  height: "34px",
  borderRadius: "999px",
  border: "2px solid transparent",
  boxShadow: "inset 0 0 0 1px rgba(0, 0, 0, 0.12)",
  cursor: "pointer",

  selectors: {
    [`${hiddenInput}:checked + &`]: {
      borderColor: vars.color.black,
      boxShadow:
        "inset 0 0 0 3px #ffffff, 0 0 0 1px rgba(0, 0, 0, 0.2)",
    },
  },
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
