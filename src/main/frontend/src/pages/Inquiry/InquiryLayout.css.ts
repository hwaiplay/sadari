import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const restrictedMain = style({
  minHeight: "100svh",
  padding: "24px 0 max(24px, env(safe-area-inset-bottom, 0px))",
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});
