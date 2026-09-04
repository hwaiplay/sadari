import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "calc(100svh - 120px)",
  margin: "0 auto",
  padding: "0 0 96px",
  boxSizing: "border-box",
  backgroundColor: vars.color.background,
});

export const searchForm = style({
  position: "sticky",
  top: `calc(${vars.headerHeight} - var(--header-scroll-offset, 0px))`,
  zIndex: 996,
  height: vars.headerHeight,
  padding: "5px 0",
  marginBottom: "12px",
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  willChange: "top",
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
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.gray100,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.5,
    },
  },
});

export const searchIcon = style({
  width: "20px",
  height: "20px",
});

export const list = style({
  margin: `${vars.space.lg} 0 0`,
  padding: 0,
  listStyle: "none",
});

export const item = style({
  minHeight: "72px",
  padding: `${vars.space.sm} 0`,
  borderBottom: `1px solid ${vars.color.gray300}`,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: vars.space.sm,
});

export const profileButton = style({
  minWidth: 0,
  padding: "4px 0",
  border: 0,
  borderRadius: vars.radius.md,
  backgroundColor: "transparent",
  display: "grid",
  gridTemplateColumns: "44px minmax(0, 1fr)",
  alignItems: "center",
  gap: "12px",
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const avatar = style({
  width: "44px",
  height: "44px",
  borderRadius: "50%",
  objectFit: "cover",
  backgroundColor: vars.color.gray100,
});

export const userText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "4px",
});

export const userName = style({
  overflow: "hidden",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.3,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const userIntro = style({
  overflow: "hidden",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.4,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const empty = style({
  margin: `${vars.space.xl} 0 0`,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.body,
  textAlign: "center",
});
