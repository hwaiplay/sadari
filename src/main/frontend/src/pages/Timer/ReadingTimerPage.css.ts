import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: `calc(100svh - ${vars.headerHeight} - ${vars.navHeight} - max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  padding: "20px 0 32px",
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const intro = style({
  marginBottom: "22px",
  display: "flex",
  alignItems: "center",
  gap: "6px",
});

export const introIcon = style({
  width: "20px",
  height: "20px",
  flexShrink: 0,
});

export const description = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.55,
});

export const card = style({
  width: "100%",
  marginBottom: "10px",
  padding: "16px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: vars.radius.md,
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const cardTitle = style({
  margin: "0 0 14px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
});

export const weekHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  marginBottom: "14px",
});

export const weekCount = style({
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
});

export const weekGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, minmax(0, 1fr))",
  gap: "7px",
});

export const day = style({
  minWidth: 0,
  padding: "10px 2px",
  borderRadius: vars.radius.sm,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  textAlign: "center",
});

export const attendedDay = style({
  backgroundColor: vars.color.brandBg,
  color: vars.color.brandText,
});

export const todayDay = style({
  outline: `2px solid ${vars.color.brand}`,
  outlineOffset: "1px",
});

export const dayName = style({
  display: "block",
  marginBottom: "6px",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
});

export const dayMark = style({
  display: "block",
  fontSize: "17px",
  lineHeight: 1,
});

export const dayMinutes = style({
  display: "block",
  marginTop: "6px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "10px",
});

export const timerCard = style([card, {
  padding: "24px 16px",
  textAlign: "center",
}]);

export const status = style({
  display: "inline-flex",
  padding: "6px 10px",
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
});

export const clock = style({
  margin: "18px 0 8px",
  color: vars.color.black,
  fontFamily: "ui-monospace, SFMono-Regular, Menlo, monospace",
  fontSize: "46px",
  fontWeight: 700,
  letterSpacing: "-2px",
});

export const book = style({
  minHeight: "20px",
  margin: "0 0 20px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
});

export const bookSelect = style({
  display: "flex",
  width: "100%",
  marginBottom: "12px",
});

export const bookSelectTrigger = style({
  width: "100%",
  height: "48px",
  padding: "0 14px 0 15px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  boxShadow: "none",
  transition: "border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray400,
      backgroundColor: "#fbfcfc",
    },
    "&:focus-visible, &[aria-expanded='true']": {
      borderColor: vars.color.brandText,
      backgroundColor: vars.color.background,
      outline: "none",
      boxShadow: `0 0 0 3px ${vars.color.brandBg}`,
    },
  },
});

export const bookOptionList = style({
  top: "calc(100% + 8px)",
  right: "auto",
  left: 0,
  zIndex: 30,
  width: "100%",
  maxHeight: "260px",
  padding: "6px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: vars.color.background,
  boxShadow: "0 12px 30px rgba(21, 21, 21, 0.12)",
  gap: "3px",
  overflowY: "auto",
  boxSizing: "border-box",
});

export const bookOption = style({
  minHeight: "42px",
  padding: "0 12px",
  borderRadius: "8px",
  color: vars.color.gray700,
  fontFamily: vars.font.body,
  fontSize: "14px",
  transition: "background-color 140ms ease, color 140ms ease",
  selectors: {
    "&:hover, &:focus-visible": {
      backgroundColor: vars.color.gray100,
      color: vars.color.black,
      outline: "none",
    },
    "&[aria-selected='true']": {
      backgroundColor: vars.color.brandBg,
      color: vars.color.brandText,
      fontFamily: vars.font.semibold,
    },
  },
});

export const actions = style({
  display: "flex",
  justifyContent: "center",
  gap: "10px",
  flexWrap: "wrap",
});

export const actionButton = style({
  minWidth: "126px",
});

export const empty = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
});

export const recentList = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  margin: 0,
  padding: 0,
  listStyle: "none",
});

export const recentItem = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  paddingTop: "10px",
  borderTop: `1px solid ${vars.color.gray200}`,
});

export const recentBook = style({
  minWidth: 0,
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "13px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const recentBookLink = style({
  display: "inline-block",
  maxWidth: "100%",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontWeight: 700,
  textDecoration: "none",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  verticalAlign: "bottom",
  selectors: {
    "&:hover": {
      textDecoration: "underline",
      textUnderlineOffset: "3px",
    },
    "&:focus-visible": {
      borderRadius: "2px",
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
});

export const recentTime = style({
  flex: "0 0 auto",
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
});
