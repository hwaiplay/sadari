import { vars } from "@/app/styles/tokens.css.ts";
import { style } from "@vanilla-extract/css";

export const page = style({
  minHeight: `calc(100dvh - ${vars.headerHeight} - ${vars.headerHeight})`,
  padding: "32px 0 24px",
  display: "flex",
  flexDirection: "column",
});

export const reportPage = style([
  page,
  {
    width: "100%",
    maxWidth: "600px",
    minHeight: "calc(100svh - 112px)",
    padding: "20px 0 20px",
    backgroundColor: "#ffffff",
    boxSizing: "border-box",
    gap: "24px",
  },
]);

export const heading = style({
  paddingTop: "26px",
  marginBottom: "12px",
  "@media": {
    "screen and (max-width: 420px)": {
      paddingTop: "22px",
    },
  },
});

export const title = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.3,
  letterSpacing: 0,
});

export const description = style({
  margin: "2px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
});

export const targetCard = style({
  padding: "16px 18px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
});

export const targetMeta = style({
  display: "flex",
  alignItems: "center",
  gap: "8px",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: "14px",
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

export  const reasonArea = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px"
})

export const reasonFieldset = style({
  margin: 0,
  padding: 0,
  border: 0,
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: "12px",
  "@media": {
    "screen and (max-width: 420px)": {
      gridTemplateColumns: "1fr",
    },
  },
});

export const reasonOption = style({
  display: "flex",
  alignItems: "center",
  minHeight: "48px",
  padding: "4px 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease, color 160ms ease, box-shadow 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray200,
      color: vars.color.black,
    },
    "&:has(input:checked)": {
      borderColor: vars.color.brandText,
      backgroundColor: vars.color.brandBg,
      color: vars.color.brandText,
      boxShadow: "0 6px 16px rgba(74, 143, 101, 0.12)",
    },
    "&:has(input:focus-visible)": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
  "@media": {
    "screen and (max-width: 420px)": {
      selectors: {
        "&:last-child": {
          gridColumn: "auto",
        },
      },
    },
  },
});

export const radio = style({
  position: "absolute",
  width: "1px",
  height: "1px",
  padding: 0,
  margin: "-1px",
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  border: 0,
});

export const detailTextarea = style({
  width: "100%",
  minHeight: "126px",
  padding: "16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  resize: "none",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  outline: "none",
  boxSizing: "border-box",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:focus": {
      borderColor: vars.color.gray400,
      backgroundColor: "#ffffff",
    },
    "&::placeholder": {
      color: vars.color.gray600,
    },
  },
});

export const footer = style({
  marginTop: "auto",
});

export const nextButton = style({
  width: "100%",
  minHeight: "48px",
  borderRadius: "8px",
  backgroundColor: vars.color.gray900,
  color: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  transition: "background-color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.darkGray,
    },
    "&:disabled": {
      backgroundColor: '#ffffff',
      border: `1px solid ${vars.color.gray300}`,
      color: vars.color.gray500,
      cursor: "default",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.negativeText}`,
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

export const completeIcon = style({
  marginBottom: '10px'
})

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
