import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";
import { media } from "@/app/styles/responsive.css";

export const reportSetButton = style({
  position: "fixed",
  right: "max(24px, calc((100vw - 600px) / 2 + 24px))",
  bottom: `calc(${vars.headerHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)) + 24px)`,
  zIndex: 998,
  width: "62px",
  height: "62px",
  borderRadius: "50%",
  backgroundColor: vars.color.gray500,
  color: "#ffffff",
  alignItems: "center",
  justifyContent: "center",
  boxShadow: "0 0px 20px rgba(0, 0, 0, 0.1)",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const emptyHomeContainer = style({
  width: "100%",
  minHeight: "calc(100svh - 68px)",
  padding: 0,
  backgroundColor:'#fff',
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
});

export const emptySetReportButton = style({
  background: '#fff',
  border: `1px dashed ${vars.color.gray300}`,
  padding: 20,
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  flexDirection: "column",
  gap: 10,
  borderRadius: 22,
  width: "calc(100% - 32px)",
  textDecoration: "none",
  transition: "background 160ms ease",

  selectors: {
    '&:hover': {
      background: vars.color.gray100,
    }
  }
})

export const emptyPlusCircle = style({
  backgroundColor: vars.color.gray200,
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  borderRadius: 999,
  width: 36,
  height: 36
})

export const emptyTitle = style({
  fontSize: 18,
  fontFamily: vars.font.heading,
  textAlign: "center",
  color: vars.color.black
});

export const emptyDescription = style({
  fontSize: vars.fontSize.body,
  textAlign: "center",
  color: vars.color.gray600
});

export const homeContainer = style({
  minHeight: "100svh",
  paddingTop: "72px",
  paddingBottom: "96px",
  backgroundColor:'#fff',

  width: "100%",
  margin: "0 auto",
  maxWidth: "600px"
});

export const searchBar = style({
  position: "sticky",
  top: vars.headerHeight,
  zIndex: 996,
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  gap: "10px",
  width: "100%",
  height: vars.headerHeight,
  padding: `5px ${vars.space.md}`,
  marginBottom: "12px",
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  isolation: "isolate",
  transition: "top 180ms ease",
  willChange: "top",

  selectors: {
    "&::before": {
      position: "absolute",
      top: 0,
      bottom: 0,
      left: "50%",
      zIndex: -1,
      width: "100vw",
      backgroundColor: "rgba(255, 255, 255, 0.96)",
      content: '""',
      pointerEvents: "none",
      transform: "translateX(-50%)",
    },
  },

  "@media": {
    [media.tablet]: {
      padding: `5px ${vars.space.lg}`,
    },
  },
});

export const searchBarHeaderHidden = style({
  top: 0,
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
  backgroundColor: "#ffffff",
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
  },
});

export const searchIcon = style({
  width: "26px",
  height: "26px",
  flexShrink: 0,
});

export const noticeSortBar = style({
  display: "flex",
  alignItems: "center",
  gap: "12px",
  width: "100%",
  height: "32px",
  padding: `0 ${vars.space.md}`,
  marginBottom: "20px",
  boxSizing: "border-box",

  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg}`,
    },
  },
});

export const sortBar = style({
  display: "flex",
  alignItems: "center",
  flexShrink: 0,
  height: "32px",
  marginLeft: "auto",
});

export const sortSelect = style({
  zIndex: 2,
});

export const sortSelectTrigger = style({
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

export const sortOptionList = style({
  minWidth: "112px",
});

export const sortSelectOption = style({
  fontSize: "14px",
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
  backgroundColor:vars.color.gray100,
  borderRadius: '999px',
  marginBottom:'8px',
  marginLeft: vars.space.md
});

export const gradeLabel = style({
  color: "#ffd966",
  fontFamily: vars.font.semibold,
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
  fontSize: "14px",
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
  fontFamily: vars.font.semibold,
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
