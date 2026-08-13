import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  flexDirection: "column",
  gap: 32,
  width: "100%",
  maxWidth: 600,
  margin: "0 auto",
  padding: "28px 0 38px",
});

export const searchLabel = style({
  position: "relative",
  display: "block",
  width: "100%",
});

export const hiddenLabel = style({
  position: "absolute",
  width: 1,
  height: 1,
  overflow: "hidden",
  clip: "rect(0,0,0,0)",
});

export const searchInput = style({
  width: "100%",
  height: 42,
  padding: "0 42px 0 16px",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 999,
  background: "#fff",
  fontFamily: vars.font.body,
  fontSize: 15,
  outline: "none",
  selectors: {
    "&:focus": { borderColor: vars.color.black },
    "&::placeholder": { color: vars.color.gray500 },
  },
});

export const searchButton = style({
  position: "absolute",
  top: "50%",
  right: 8,
  width: 32,
  height: 32,
  transform: "translateY(-50%)",
  border: 0,
  borderRadius: "50%",
  background: "transparent",
  cursor: "pointer",
  fontSize: 18,
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: 12,
});

export const chips = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 8,
});

export const chip = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: 4,
  padding: "8px 14px",
  borderRadius: 999,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1,
});

export const empty = style({
  padding: "46px 18px",
  color: vars.color.gray500,
  fontFamily: vars.font.body,
  fontSize: 13,
  textAlign: "center",
});

export const loading = style([
  empty,
  {
    padding: "70px 18px",
  },
]);
