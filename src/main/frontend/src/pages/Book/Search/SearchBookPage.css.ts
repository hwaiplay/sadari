import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";
import { media } from "@/app/styles/responsive.css";

export const page = style({
  minHeight: "calc(100svh - 52px - 60px)",
  backgroundColor: vars.color.background,
});

export const content = style({
  maxWidth: "600px",
  width: "100%",
  margin: "0 auto",
  padding: "12px 0",
});

export const searchBar = style({
  position: "sticky",
  top: `calc(${vars.headerHeight} - var(--header-scroll-offset, 0px))`,
  zIndex: 996,
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  gap: "10px",
  width: "100%",
  height: vars.headerHeight,
  padding: "5px 0",
  marginBottom: 0,
  willChange: "top",

  "@media": {
    [media.tablet]: {
      padding: `5px ${vars.space.lg}`,
    },
  },
});

export const searchLabel = style({
  position: "relative",
  display: "block",
  width: "100%",
  minWidth: 0,
  flex: 1,
});

export const hiddenLabel = style({
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

export const searchInput = style({
  width: "100%",
  height: "42px",
  padding: "0 38px 0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "16px",
  outline: "none",

  selectors: {
    "&::placeholder": {
      color: vars.color.gray500,
    },
    "&:focus": {
      borderColor: vars.color.black,
    },
  },
});

export const searchButton = style({
  position: "absolute",
  top: "50%",
  right: "10px",
  width: "32px",
  height: "32px",
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  color: vars.color.black,
  transform: "translateY(-50%)",
  cursor: "pointer",

  selectors: {
    "&:hover": {
      backgroundColor: "#f3f3f3",
    },
    "&:disabled": {
      opacity: 0.6,
      cursor: "not-allowed",
    },
  },
});

export const searchIcon = style({
  width: "26px",
  height: "26px",
  flexShrink: 0,
});

export const popularControlBar = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  width: "100%",
  minWidth: 0,
  height: "32px",
  marginTop: "10px",
  padding: 0,
  gap: "8px",
  boxSizing: "border-box",

  "@media": {
    [media.tablet]: {
      padding: `0 calc(${vars.space.lg} - ${vars.space.md})`,
    },
  },
});

export const resultSection = style({
  marginTop: "22px",
});

export const popularPeriodBar = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  flexShrink: 0,
  height: "32px",
});

export const popularPeriodSelect = style({
  zIndex: 2,
});

export const popularPeriodSelectTrigger = style({
  minWidth: "auto",
  height: "32px",
  padding: 0,
  border: 0,
  borderRadius: 0,
  backgroundColor: "transparent",
  fontFamily: vars.font.medium,
  fontSize: "14px",
  gap: "10px",
});

export const popularPeriodOptionList = style({
  minWidth: "128px",
});

export const popularPeriodOption = style({
  fontSize: "14px",
});

export const resultList = style({
  display: "flex",
  flexDirection: "column",
  gap: "20px",
});

export const resultCard = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "14px",
  padding: "40px 22px 30px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
  boxSizing: "border-box",
});

export const popularRank = style({
  alignSelf: "center",
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.4,
  color: vars.color.black,
});

export const coverArea = style({
  display: "flex",
  justifyContent: "center",
});

export const coverFrame = style({
  width: "126px",
  aspectRatio: "2 / 3",
  padding: 0,
  border: 0,
  borderRadius: "6px",
  overflow: "hidden",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
  cursor: "pointer",
  transition: "transform 160ms ease, box-shadow 160ms ease",

  selectors: {
    "&:hover": {
      transform: "translateY(-3px)",
      boxShadow: "0 14px 28px rgba(0, 0, 0, 0.2)",
    },
    "&:active": {
      transform: "translateY(-1px)",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "4px",
    },
  },
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookMeta = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "5px",
  textAlign: "center",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
  color: vars.color.black,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const bookTitleButton = style({
  maxWidth: "100%",
  padding: "2px 6px",
  border: 0,
  borderRadius: "6px",
  backgroundColor: "transparent",
  color: "inherit",
  fontFamily: "inherit",
  fontSize: "inherit",
  lineHeight: "inherit",
  textAlign: "center",
  wordBreak: "inherit",
  cursor: "pointer",
  transition: "background-color 140ms ease, color 140ms ease",

  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
      color: vars.color.brandText,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const meta = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.5,
  color: vars.color.gray600,
  textAlign: "center",
});

export const authorButton = style({
  padding: "2px 4px",
  border: 0,
  borderRadius: "4px",
  backgroundColor: "transparent",
  color: "inherit",
  fontFamily: "inherit",
  fontSize: "inherit",
  lineHeight: "inherit",
  cursor: "pointer",
  transition: "background-color 140ms ease, color 140ms ease",

  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
      color: vars.color.brandText,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const description = style({
  width: "100%",
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.7,
  color: vars.color.black,
  wordBreak: "break-word",
  display: "-webkit-box",
  WebkitBoxOrient: "vertical",
  WebkitLineClamp: 3,
  overflow: "hidden",
});

export const authorRatingLine = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "8px",
  maxWidth: "100%",
});

export const metaSeparator = style({
  flexShrink: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1,
  color: vars.color.gray500,
});

export const actions = style({
  width: "100%",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  flexWrap: "wrap",
  gap: "8px",
});

export const actionButton = style({
  height: "34px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const primaryButton = style([
  actionButton,
  {
    backgroundColor: "#ffffff",
    color: vars.color.black,
  },
]);

export const emptyMessage = style({
  margin: "40px 0 0",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.6,
  color: "#777777",
  textAlign: "center",
});
