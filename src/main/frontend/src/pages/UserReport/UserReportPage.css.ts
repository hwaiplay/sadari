import { vars } from "@/app/styles/tokens.css.ts";
import { style } from "@vanilla-extract/css";

export const page = style({
  minHeight: `calc(100dvh - ${vars.headerHeight} - ${vars.headerHeight})`,
  padding: "32px 0 24px",
  display: "flex",
  flexDirection: "column",
});

export const heading = style({ marginBottom: "28px" });

export const title = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: "18px",
  lineHeight: 1.4,
});

export const description = style({
  margin: "8px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.5,
});

export const targetCard = style({
  marginBottom: "28px",
  padding: "16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "16px",
  backgroundColor: vars.color.background,
});

export const targetMeta = style({
  display: "flex",
  alignItems: "center",
  gap: "8px",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: "13px",
});

export const targetNick = style({ color: vars.color.gray900 });

export const targetContent = style({
  margin: "10px 0 0",
  color: vars.color.gray700,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  display: "-webkit-box",
  WebkitBoxOrient: "vertical",
  WebkitLineClamp: 3,
  overflow: "hidden",
  whiteSpace: "pre-wrap",
  overflowWrap: "anywhere",
});

export const reasonFieldset = style({
  margin: 0,
  padding: 0,
  border: 0,
  display: "flex",
  flexDirection: "column",
});

export const reasonOption = style({
  minHeight: "52px",
  display: "flex",
  alignItems: "center",
  gap: "12px",
  borderBottom: `1px solid ${vars.color.gray200}`,
  color: vars.color.gray900,
  fontFamily: vars.font.body,
  fontSize: "15px",
  cursor: "pointer",
});

export const radio = style({
  width: "18px",
  height: "18px",
  margin: 0,
  accentColor: vars.color.brand,
  flexShrink: 0,
});

export const detailArea = style({ marginTop: "14px" });

export const detailTextarea = style({
  width: "100%",
  minHeight: "120px",
  padding: "14px",
  boxSizing: "border-box",
  resize: "vertical",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "12px",
  color: vars.color.gray900,
  backgroundColor: vars.color.background,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  selectors: {
    "&:focus": {
      borderColor: vars.color.gray700,
      outline: "none",
    },
    "&::placeholder": { color: vars.color.gray500 },
  },
});

export const footer = style({ marginTop: "auto", paddingTop: "32px" });

export const nextButton = style({
  width: "100%",
  height: "48px",
  border: `1px solid ${vars.color.gray700}`,
  borderRadius: "14px",
  backgroundColor: vars.color.background,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      borderColor: vars.color.gray300,
      color: vars.color.gray500,
      cursor: "not-allowed",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray500}`,
      outlineOffset: "2px",
    },
  },
});

export const completePage = style([
  page,
  {
    alignItems: "stretch",
    justifyContent: "center",
    paddingBottom: "64px",
  },
]);

export const completeHeading = style({ textAlign: "center" });

export const completeTitle = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: "22px",
  lineHeight: 1.4,
});

export const completeDescription = style({
  margin: "12px auto 0",
  maxWidth: "360px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  whiteSpace: "pre-line"
});

export const otherOptions = style({ marginTop: "48px" });

export const otherOptionsTitle = style({
  margin: "0 0 10px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
});

export const blockOptionButton = style({
  width: "100%",
  minHeight: "40px",
  backgroundColor: vars.color.background,
  color: vars.color.gray900,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  fontFamily: vars.font.medium,
  fontSize: "14px",
  textAlign: "left",
  cursor: "pointer",
});

export const blockOptionButtonBody = style({
  display: "flex",
  alignItems: "center",
  gap: "8px"
});

export const arrowIcon = style({
  width: "18px",
  height: "18px",
  flexShrink: 0,
  transform: "rotate(-90deg)",
});
