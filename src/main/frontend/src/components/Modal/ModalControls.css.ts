import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const roundClose = style({
  width: "32px",
  height: "32px",
  flex: "0 0 32px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f5",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontSize: "22px",
  lineHeight: 1,
  cursor: "pointer",
  transition: "background-color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.gray200,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.6,
    },
  },
});

export const pairedActions = style({
  display: "grid",
  gridTemplateColumns: "minmax(0, 1fr) minmax(0, 1fr)",
  gap: "8px",
});
