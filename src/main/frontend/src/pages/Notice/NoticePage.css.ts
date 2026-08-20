import { globalStyle, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "100svh",
  margin: "0 auto",
  padding: "20px 18px 96px",
  boxSizing: "border-box",
  backgroundColor: "#ffffff",
});

export const listPage = style([
  page,
  {
    paddingTop: "10px",
    paddingRight: 0,
    paddingLeft: 0,
  },
]);

export const intro = style({
  marginBottom: "22px",
});

export const description = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.55,
});

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const item = style({
  width: "100%",
  minHeight: "102px",
  padding: "15px 16px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: vars.radius.md,
  backgroundColor: vars.color.background,
  color: vars.color.black,
  textAlign: "left",
  cursor: "pointer",
  transition: "background-color 160ms ease, border-color 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray300,
      backgroundColor: vars.color.gray100,
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
});

export const itemRead = style({
  backgroundColor: "#f3f4f6",
  borderColor: "#e5e7eb",
  opacity: 0.68,
});

export const itemText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
});

export const title = style({
  display: "block",
  minWidth: 0,
  flex: "0 1 auto",
  overflow: "hidden",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.45,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const titleWithUnread = style({
  minWidth: 0,
  flex: 1,
  display: "flex",
  alignItems: "flex-start",
  gap: "5px",
});

export const titleRow = style({
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  gap: "4px",
});

export const pinIcon = style({
  width: "16px",
  height: "16px",
  flexShrink: 0,
  fill: "none",
  stroke: vars.color.brand,
  strokeWidth: 1.8,
  strokeLinecap: "round",
  strokeLinejoin: "round",
});

export const unreadDot = style({
  width: "6px",
  height: "6px",
  flexShrink: 0,
  borderRadius: "50%",
  backgroundColor: "#ef4444",
  marginTop: "2px",
});

export const date = style({
  display: "block",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
  lineHeight: 1.4,
});

export const itemBottom = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-start",
  flexWrap: "wrap",
  gap: "6px",
  marginTop: "10px",
});

export const statusPanel = style({
  minHeight: "260px",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "16px",
});

export const statusText = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const retryButton = style({
  minWidth: "92px",
  height: "38px",
  padding: "0 15px",
  border: `1px solid ${vars.color.gray500}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
    },
    "&:disabled": {
      opacity: 0.5,
      cursor: "default",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "3px",
    },
  },
});

export const moreError = style({
  margin: "12px 0 0",
  color: "#c74747",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const detailHeader = style({
  padding: "4px 0 20px",
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const detailInfo = style({
  display: "flex",
  alignItems: "center",
  flexWrap: "wrap",
  gap: "7px",
  marginTop: "4px",
});

export const detailMeta = style({
  display: "flex",
  alignItems: "center",
  gap: "7px",
});

export const detailTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "24px",
  lineHeight: 1.4,
  overflowWrap: "anywhere",
});

export const content = style({
  padding: "24px 0",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "15px",
  lineHeight: 1.75,
  overflowWrap: "anywhere",
});

globalStyle(`${content} > :first-child`, { marginTop: 0 });
globalStyle(`${content} > :last-child`, { marginBottom: 0 });
globalStyle(`${content} p`, { margin: "0 0 1em" });
globalStyle(`${content} h1`, { margin: "1.4em 0 0.6em", fontFamily: vars.font.heading, fontSize: "22px", lineHeight: 1.4 });
globalStyle(`${content} h2`, { margin: "1.4em 0 0.6em", fontFamily: vars.font.heading, fontSize: "20px", lineHeight: 1.4 });
globalStyle(`${content} h3`, { margin: "1.4em 0 0.6em", fontFamily: vars.font.semibold, fontSize: "18px", lineHeight: 1.45 });
globalStyle(`${content} ul`, { margin: "0 0 1em", paddingLeft: "1.5em" });
globalStyle(`${content} ol`, { margin: "0 0 1em", paddingLeft: "1.5em" });
globalStyle(`${content} a`, { color: vars.color.brand, textDecoration: "underline", textUnderlineOffset: "3px" });
globalStyle(`${content} blockquote`, { margin: "1em 0", padding: "12px 16px", borderLeft: `3px solid ${vars.color.gray400}`, backgroundColor: vars.color.gray100, color: vars.color.gray700 });
globalStyle(`${content} pre`, { maxWidth: "100%", padding: "14px", overflowX: "auto", borderRadius: vars.radius.sm, backgroundColor: vars.color.gray100, boxSizing: "border-box", whiteSpace: "pre-wrap" });
globalStyle(`${content} img`, { display: "block", maxWidth: "100%", height: "auto", margin: "16px auto" });
globalStyle(`${content} table`, { display: "block", width: "100%", maxWidth: "100%", overflowX: "auto", borderCollapse: "collapse" });
globalStyle(`${content} th`, { padding: "7px", border: `1px solid ${vars.color.gray200}` });
globalStyle(`${content} td`, { padding: "7px", border: `1px solid ${vars.color.gray200}` });
globalStyle(`${content} hr`, { margin: "24px 0", border: 0, borderTop: `1px solid ${vars.color.gray200}` });
