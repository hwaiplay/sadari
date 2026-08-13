import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const card = style({
  display: "flex",
  flexDirection: "column",
  gap: 11,
  padding: "19px 18px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 18,
  background: "#fff",
  cursor: "pointer",
  transition: "border-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray500,
      transform: "translateY(-1px)",
    },
  },
});

export const cardTop = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: 12,
});

export const cardTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: 17,
  lineHeight: 1.35,
});

export const badge = style({
  padding: "5px 9px",
  borderRadius: 999,
  background: vars.color.gray100,
  fontFamily: vars.font.semibold,
  fontSize: 11,
  whiteSpace: "nowrap",
});

export const description = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 13,
  lineHeight: 1.6,
  whiteSpace: "pre-wrap",
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

export const meta = style({
  display: "flex",
  flexWrap: "wrap",
  gap: "7px 12px",
  color: vars.color.gray500,
  fontFamily: vars.font.body,
  fontSize: 12,
});
