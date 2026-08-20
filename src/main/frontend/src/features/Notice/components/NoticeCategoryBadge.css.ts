import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const badge = style({
  flexShrink: 0,
  padding: "3px 8px",
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.5,
});
