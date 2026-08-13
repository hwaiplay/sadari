import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const selectableOption = style({
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
  color: vars.color.gray600,
  transition: "border-color 160ms ease, background-color 160ms ease, color 160ms ease, box-shadow 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray200,
      color: vars.color.black,
    },
    "&:has(input:checked)": {
      borderColor: vars.color.brandText,
      backgroundColor: vars.color.brandBg,
      color: vars.color.brandText,
      boxShadow: "0 6px 16px rgba(74, 143, 101, 0.12)",
    },
    "&[data-selected='true']": {
      borderColor: vars.color.brandText,
      backgroundColor: vars.color.brandBg,
      color: vars.color.brandText,
      boxShadow: "0 6px 16px rgba(74, 143, 101, 0.12)",
    },
    "&:has(input:focus-visible)": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const destructiveButton = style({
  border: `1px solid ${vars.color.negativeText}`,
  backgroundColor: vars.color.negativeBg,
  color: vars.color.negativeText,
  transition: "background-color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#FFCDD4",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.negative}`,
      outlineOffset: "2px",
    },
    "&:disabled": {
      borderColor: vars.color.gray300,
      backgroundColor: "#ffffff",
      color: vars.color.gray500,
      cursor: "default",
    },
  },
});
