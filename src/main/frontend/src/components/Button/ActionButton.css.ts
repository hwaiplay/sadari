import { vars } from "@/app/styles/tokens.css";
import { style, styleVariants } from "@vanilla-extract/css";

export const button = style({
  minWidth: "76px",
  padding: "0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "8px",
  fontFamily: vars.font.medium,
  fontSize: "14px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "6px",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
    "&:disabled": {
      borderColor: vars.color.gray300,
      background: "#ffffff",
      color: vars.color.gray500,
      cursor: "default",
    },
  },
});

export const variant = styleVariants({
  primary: {
    backgroundColor: vars.color.gray900,
    borderColor: vars.color.gray900,
    color: "#ffffff",
    selectors: {
      "&:hover:not(:disabled)": {
        backgroundColor: vars.color.darkGray
      },
    },
  },
  secondary: {
    backgroundColor: "#ffffff",
    color: vars.color.gray900,
    selectors: {
      "&:hover:not(:disabled)": {
        backgroundColor: vars.color.gray100,
      },
    },
  },
  danger: {
    backgroundColor: vars.color.negativeBg,
    color: vars.color.negativeText,
    selectors: {
      "&:hover:not(:disabled)": {
        backgroundColor: vars.color.negativeHover,
      },
    },
  },
});

export const size = styleVariants({
  sm: {
    minWidth: 0,
    height: "34px",
    padding: "0 10px",
    fontSize: "14px",
  },
  md: {
    height: "42px",
  },
  lg: {
    height: "46px",
  },
});

export const width = styleVariants({
  auto: {},
  full: {
    width: "100%",
  },
  half: {
    width: "50%",
  },
});

export const icon = style({
  width: "17px",
  height: "17px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  flexShrink: 0,
  selectors: {
    "&:empty": {
      display: "none",
    },
  },
});

export const label = style({
  display: "inline-flex",
  alignItems: "center",
  whiteSpace: "nowrap",
  selectors: {
    "&:empty": {
      display: "none",
    },
  },
});
