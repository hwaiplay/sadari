import { vars } from "@/app/styles/tokens.css.ts";
import { style } from "@vanilla-extract/css";

export const root = style({ position: "relative", display: "inline-flex" });

export const trigger = style({
  width: "18px",
  height: "18px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const triggerIcon = style({ width: "18px", height: "18px", display: "block", transform: "rotate(90deg)" });

export const menu = style({
  position: "absolute",
  top: "calc(100% + 4px)",
  right: 0,
  zIndex: 30,
  minWidth: "112px",
  padding: "5px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "16px",
  backgroundColor: vars.color.background,
  boxShadow: "0 8px 24px rgba(0, 0, 0, 0.12)",
  display: "flex",
  flexDirection: "column",
  gap: "2px",
});

export const menuOption = style({
  width: "100%",
  padding: "9px 12px",
  border: 0,
  borderRadius: "12px",
  backgroundColor: "transparent",
  color: vars.color.gray900,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray500}`,
      outlineOffset: "-2px",
    },
  },
});
