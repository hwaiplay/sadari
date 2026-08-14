import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const root = style({
  position: "relative",
  display: "inline-flex",
});

export const trigger = style({
  minWidth: "112px",
  height: "32px",
  padding: "0 12px",
  background: "transparent",
  borderRadius: "99999px",
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  display: "inline-flex",
  justifyContent: 'space-between',
  alignItems: "center",
  gap: "7px",
  cursor: "pointer",
  transition: "background 160ms ease"
});

export const triggerValue = style({
  display: "inline-flex",
  alignItems: "center",
  minWidth: 0,
});

export const arrow = style({
  width: "12px",
  height: "12px",
  fill: "none",
  stroke: vars.color.gray600,
  strokeWidth: 1.5,
  strokeLinecap: "round",
  strokeLinejoin: "round",
  transition: "transform 160ms ease",
});

export const arrowOpen = style({
  transform: "rotate(180deg)",
});

export const optionList = style({
  position: "absolute",
  top: "calc(100% + 4px)",
  right: 0,
  zIndex: 20,
  minWidth: "max(112px, 100%)",
  padding: "5px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "16px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 24px rgba(0, 0, 0, 0.12)",
  display: "flex",
  flexDirection: "column",
  gap: "6px",
  visibility: "hidden",
  opacity: 0,
  pointerEvents: "none",
  transform: "translateY(-8px) scale(0.985)",
  transformOrigin: "top center",
  transition: "opacity 150ms ease, transform 180ms ease, visibility 0s linear 180ms",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      transform: "none",
      transition: "none",
    },
  },
});

export const optionListOpen = style({
  visibility: "visible",
  opacity: 1,
  pointerEvents: "auto",
  transform: "translateY(0) scale(1)",
  transition: "opacity 170ms ease, transform 200ms cubic-bezier(0.22, 1, 0.36, 1), visibility 0s",
});

export const option = style({
  width: "100%",
  minHeight: "34px",
  padding: "0 10px",
  border: 0,
  borderRadius: "10px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  textAlign: "left",
  whiteSpace: "nowrap",
  cursor: "pointer",
  selectors: {
    "&:hover, &:focus-visible": {
      backgroundColor: vars.color.gray100,
      color: vars.color.black,
      outline: "none",
    },
  },
});

export const optionSelected = style({
  backgroundColor: vars.color.gray100,
  fontFamily: vars.font.semibold,
});
