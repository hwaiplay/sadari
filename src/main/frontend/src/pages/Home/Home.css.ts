import { style } from "@vanilla-extract/css";
import { vars } from "../../app/styles/tokens.css";
import { media } from "../../app/styles/responsive.css";

export const emptyHomeContainer = style({
  width: "100%",
  minHeight: "100svh",
  padding: 0,
  // backgroundImage: 'url("/img/common/background-empty.png")',
  // backgroundRepeat: "no-repeat",
  // backgroundSize: "cover",
  backgroundColor:'#fff',
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
});

export const emptyTitle = style({
  fontSize: vars.fontSize.title,
  fontFamily: vars.font.heading,
  textAlign: "center",
});

export const homeContainer = style({
  minHeight: "100svh",
  paddingTop: "82px",
  paddingBottom: "96px",
  backgroundColor:'#fff',

  width: "100%",
  margin: "0 auto",
  maxWidth: "600px"
});

export const sortBar = style({
  display: "grid",
  gridTemplateColumns: "minmax(0, 1fr) 118px",
  alignItems: "center",
  gap: "10px",
  padding: `0 ${vars.space.md}`,
  marginBottom: "24px",

  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg}`,
    },
  },
});

export const searchLabel = style({
  position: "relative",
  display: "block",
  minWidth: 0,
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
  height: "32px",
  padding: "0 38px 0 12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "12px",
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
  right: "4px",
  width: "26px",
  height: "26px",
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
  },
});

export const searchIcon = style({
  width: "16px",
  height: "16px",
  flexShrink: 0,
});

export const sortDropdown = style({
  position: "relative",
});

export const sortTrigger = style({
  width: "100%",
  height: "32px",
  padding: "0 10px 0 12px",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "8px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "12px",
  cursor: "pointer",
});

export const sortArrow = style({
  flexShrink: 0,
  fontSize: "9px",
  color: vars.color.gray500,
  lineHeight: 1,
});

export const sortMenu = style({
  position: "absolute",
  top: "38px",
  right: 0,
  zIndex: 5,
  width: "132px",
  padding: "6px",
  display: "flex",
  flexDirection: "column",
  gap: "2px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: "#ffffff",
  boxShadow: "0 12px 28px rgba(0, 0, 0, 0.12)",
});

export const sortMenuItem = style({
  height: "30px",
  padding: "0 10px",
  border: 0,
  borderRadius: "8px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "12px",
  textAlign: "left",
  cursor: "pointer",

  selectors: {
    "&:hover": {
      backgroundColor: "#f5f5f5",
    },
  },
});

export const sortMenuItemActive = style({
  backgroundColor: vars.color.black,
  color: "#ffffff",

  selectors: {
    "&:hover": {
      backgroundColor: vars.color.black,
    },
  },
});

export const monthGroupStack = style({
  display: "flex",
  flexDirection: "column",
  gap: "54px",
});

export const monthGroup = style({
  display: "flex",
  flexDirection: "column",
  gap: "18px",
});

export const monthGroup__inner = style({})

export const monthLabel = style({
  position: "relative",
  width: "fit-content",
  padding: "4px 8px",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1,
  color: vars.color.gray500,
  backgroundColor:'#EBEBEB',
  borderRadius: '999px',
  marginBottom:'8px',
  marginLeft: vars.space.md
});

export const gradeLabel = style({
  color: "#ffd966",
  fontFamily: vars.font.middle,
});

export const emptySearchResult = style({
  padding: "48px 20px",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "12px",
  textAlign: "center",
});

export const emptySearchText = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: vars.color.gray500,
});

export const emptySearchButton = style({
  minHeight: "28px",
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  gap: "4px",
  border: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "12px",
  cursor: "pointer",
});

export const emptySearchButtonIcon = style({
  width: "15px",
  height: "15px",
  flexShrink: 0,
});

export const bookGrid = style({
  display: "flex",
  flexDirection: "column",
  gap: "48px",
  width: "100%",



  "@media": {
    [media.tablet]: {
      gap: "72px",
    },
  },
});

export const bookRow = style({
  display: "grid",
  gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
  columnGap: "16px",
  position: "relative",
  padding: `0 ${vars.space.md} 8px`,


  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg} 8px`,
      columnGap: "42px",
    },
  },
});

export const row5Container = style({
  display: "none",
});

export const row = style({
  display: "none",
});

export const row5 = style({
  display: "none",
});

export const row6 = style({
  display: "none",
});
