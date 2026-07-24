import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "100svh",
  margin: "0 auto",
  padding: "82px 18px 96px",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
});

export const header = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: "14px",
  marginBottom: "22px",
});

export const title = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1.25,
  color: vars.color.black,
});

export const subtitle = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.45,
  color: "#777777",
});

export const readAllButton = style({
  flex: "0 0 auto",
  minWidth: "76px",
  height: "34px",
  padding: "0 13px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "12px",
  cursor: "pointer",
  whiteSpace: "nowrap",
  selectors: {
    "&:disabled": {
      color: vars.color.gray500,
      cursor: "default",
    },
  },
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const itemButton = style({
  width: "100%",
  minHeight: "78px",
  padding: "14px 15px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: "#ffffff",
  display: "grid",
  gridTemplateColumns: "34px minmax(0, 1fr)",
  alignItems: "center",
  gap: "12px",
  textAlign: "left",
  cursor: "pointer",
  boxSizing: "border-box",
});

export const alimIconWrap = style({
  width: "34px",
  height: "34px",
  borderRadius: "50%",
  backgroundColor: "#eef6ff",
  color: "#3182ce",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
});

export const alimIconWrapLike = style([
  alimIconWrap,
  {
    backgroundColor: "#ffe3ec",
    color: "#e03131",
  },
]);

export const alimIconWrapFollow = style([
  alimIconWrap,
  {
    backgroundColor: "#eafaf1",
    color: "#2f9e44",
  },
]);

export const alimIcon = style({
  width: "17px",
  height: "17px",
  display: "block",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const alimHeartIcon = style([
  alimIcon,
  {
    fill: "currentColor",
    stroke: "currentColor",
  },
]);

export const itemText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "5px",
});

export const itemTitle = style({
  margin: 0,
  fontFamily: vars.font.middle,
  fontSize: "14px",
  lineHeight: 1.35,
  color: vars.color.black,
});

export const itemContent = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.45,
  color: "#555555",
});

export const itemDate = style({
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1.3,
  color: "#9a9a9a",
});

export const empty = style({
  minHeight: "260px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: "#777777",
});

export const scrollTarget = style({
  minHeight: "38px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.body,
  fontSize: "12px",
  color: "#777777",
});
