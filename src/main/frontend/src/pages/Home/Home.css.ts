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
  backgroundColor:'#f3f3f3',
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
  backgroundColor:'#f3f3f3',

  width: "100%",
  margin: "0 auto",
  maxWidth: "600px"
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
