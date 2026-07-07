import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const colorGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(6, 1fr)",
  gap: "12px 8px",
});

export const colorOption = style({
  display: "flex",
  justifyContent: "center",
});

export const hiddenInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
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

export const customColorButton = style({
  gridColumn: "1 / -1",
  minHeight: "38px",
  marginTop: "2px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "8px",
});

export const customColorPreview = style({
  width: "16px",
  height: "16px",
  borderRadius: vars.radius.xl,
  boxShadow: "inset 0 0 0 1px rgba(0, 0, 0, 0.14)",
});

export const modalBackdrop = style({
  position: "fixed",
  inset: 0,
  zIndex: 1000,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "20px",
  backgroundColor: "rgba(0, 0, 0, 0.35)",
});

export const colorModal = style({
  width: "100%",
  maxWidth: "320px",
  padding: "22px",
  borderRadius: vars.radius.md,
  backgroundColor: "#ffffff",
  boxShadow: "0 18px 42px rgba(0, 0, 0, 0.18)",
  display: "flex",
  flexDirection: "column",
  gap: "18px",
});

export const modalTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "17px",
  color: vars.color.black,
});

export const colorPickerRow = style({
  display: "grid",
  gridTemplateColumns: "64px 1fr",
  alignItems: "center",
  gap: "14px",
});

export const colorPicker = style({
  width: "64px",
  height: "44px",
  padding: 0,
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.sm,
  backgroundColor: "#ffffff",
  cursor: "pointer",
});

export const colorValue = style({
  fontFamily: vars.font.middle,
  fontSize: "14px",
  color: vars.color.black,
});

export const modalActions = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "10px",
});

export const modalButton = style({
  height: "40px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const modalPrimaryButton = style([
  modalButton,
  {
    backgroundColor: vars.color.black,
    color: "#ffffff",
  },
]);
