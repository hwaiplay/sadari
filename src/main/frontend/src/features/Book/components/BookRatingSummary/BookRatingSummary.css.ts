import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const ratingSummary = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
  flexShrink: 0,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1,
  color: vars.color.black,
});

export const ratingStar = style({
  color: "#ffd966",
  fontSize: "16px",
  lineHeight: 1,
});

export const ratingValue = style({
  lineHeight: 1,
});
