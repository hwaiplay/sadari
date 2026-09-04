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

export const policyPage = style({ paddingBottom: "24px" });

export const list = style({ display: "flex", flexDirection: "column" });

export const item = style({ borderBottom: `1px solid ${vars.color.gray300}` });

export const policyItem = style({ width: "100%" });

export const policyTitle = style({
  margin: "0 4px 20px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "18px",
  lineHeight: 1.5,
});

export const button = style({
  width: "100%",
  minHeight: "58px",
  padding: "0 4px",
  border: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:hover": { backgroundColor: vars.color.gray100 },
    "&:focus-visible": { outline: "2px solid #78b991", outlineOffset: "-2px" },
  },
});

export const chevron = style({
  width: "18px",
  height: "18px",
  flexShrink: 0,
  fill: "none",
  stroke: vars.color.gray600,
  strokeWidth: 1.8,
  strokeLinecap: "round",
  strokeLinejoin: "round",
  transition: "transform 180ms ease",
  "@media": {
    "(prefers-reduced-motion: reduce)": { transition: "none" },
  },
});

export const chevronOpen = style({ transform: "rotate(90deg)" });

export const contentWrap = style({
  display: "grid",
  gridTemplateRows: "0fr",
  opacity: 0,
  visibility: "hidden",
  transition: "grid-template-rows 260ms ease, opacity 180ms ease, visibility 0s linear 260ms",
  "@media": {
    "(prefers-reduced-motion: reduce)": { transition: "none" },
  },
});

export const contentWrapOpen = style({
  gridTemplateRows: "1fr",
  opacity: 1,
  visibility: "visible",
  transition: "grid-template-rows 260ms ease, opacity 220ms ease, visibility 0s linear 0s",
});

export const policyContent = style({ width: "100%" });

export const contentClip = style({
  minWidth: 0,
  minHeight: 0,
  overflow: "hidden",
});

export const content = style({
  width: "100%",
  maxWidth: "100%",
  minWidth: 0,
  minHeight: 0,
  padding: "4px 4px 16px",
  boxSizing: "border-box",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.75,
  overflow: "hidden",
  overflowWrap: "anywhere",
  wordBreak: "break-word",
});

export const modifiedDate = style({
  display: "flex",
  justifyContent: "flex-end",
  alignItems: "center",
  gap: "6px",
  padding: "0 4px 24px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
});

export const empty = style({ margin: 0, color: vars.color.gray600 });

export const confirmArea = style({ marginTop: "24px" });

globalStyle(`${content} > :first-child`, { marginTop: 0 });
globalStyle(`${content} > :last-child`, { marginBottom: 0 });
globalStyle(`${content} *`, { maxWidth: "100%", boxSizing: "border-box" });
globalStyle(`${content} ul, ${content} ol`, {
  width: "100%",
  margin: "12px 0",
  padding: "0 0 0 22px",
  boxSizing: "border-box",
});
globalStyle(`${content} li`, {
  maxWidth: "100%",
  paddingLeft: "2px",
  overflowWrap: "anywhere",
  wordBreak: "break-word",
});
globalStyle(`${content} img`, { display: "block", maxWidth: "100%", height: "auto", margin: "16px auto" });
globalStyle(`${content} table`, { display: "block", width: "100%", overflowX: "auto", borderCollapse: "collapse" });
globalStyle(`${content} th`, { padding: "7px", border: `1px solid ${vars.color.gray300}` });
globalStyle(`${content} td`, { padding: "7px", border: `1px solid ${vars.color.gray300}` });
globalStyle(`${content} a`, { color: vars.color.brand, textDecoration: "underline" });
