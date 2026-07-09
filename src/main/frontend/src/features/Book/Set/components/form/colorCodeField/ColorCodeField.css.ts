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
