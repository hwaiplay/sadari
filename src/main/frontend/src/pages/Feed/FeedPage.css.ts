import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  margin: "0 auto",
  padding: "20px 0 36px",
  boxSizing: "border-box",
});

export const list = style({ display: "grid", gap: "14px" });

export const card = style({
  overflow: "hidden",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  background: vars.color.background,
  boxSizing: "border-box",
});

export const cardHeader = style({
  display: "flex",
  alignItems: "center",
  gap: "10px",
  padding: "16px 16px 12px",
});

export const avatar = style({ width: "42px", height: "42px", borderRadius: "50%", objectFit: "cover" });
export const authorButton = style({
  display: "flex",
  alignItems: "center",
  gap: "10px",
  padding: 0,
  border: 0,
  background: "transparent",
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:hover": { opacity: 0.78 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 3, borderRadius: 8 },
  },
});
export const authorName = style({ margin: 0, fontFamily: vars.font.semibold, fontSize: "16px", color: vars.color.black });
export const activity = style({ margin: "3px 0 0", fontFamily: vars.font.body, fontSize: "12px", color: vars.color.gray600 });

export const mediaButton = style({
  display: "grid",
  gap: "14px",
  width: "100%",
  padding: "0 16px 16px",
  border: 0,
  background: "transparent",
  textAlign: "left",
  cursor: "pointer",
  boxSizing: "border-box",
  selectors: {
    "&:hover": { background: vars.color.gray100 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: -2 },
  },
});

export const reportMediaButton = style([
  mediaButton,
  {
    gridTemplateColumns: "clamp(104px, 28vw, 144px) minmax(0, 1fr)",
    alignItems: "center",
  },
]);

export const profileMediaButton = style([
  mediaButton,
  {
    gridTemplateColumns: "clamp(132px, 36vw, 180px) minmax(0, 1fr)",
    alignItems: "center",
  },
]);

export const backgroundMediaButton = style([
  mediaButton,
  {
    gridTemplateColumns: "minmax(0, 1fr)",
    gap: "12px",
  },
]);

export const media = style({
  display: "block",
  width: "100%",
  background: vars.color.gray100,
  objectFit: "cover",
});
export const reportMedia = style([media, { aspectRatio: "2 / 3", borderRadius: "12px" }]);
export const profileMedia = style([media, { aspectRatio: "1", maxWidth: "180px", borderRadius: "50%" }]);
export const backgroundMedia = style([media, { aspectRatio: "16 / 9", borderRadius: "12px" }]);
export const mediaInfo = style({ minWidth: 0, alignSelf: "center" });
export const title = style({ margin: 0, fontFamily: vars.font.heading, fontSize: "18px", lineHeight: 1.4, color: vars.color.black, overflowWrap: "anywhere" });
export const metadata = style({ margin: "8px 0 0", fontFamily: vars.font.body, fontSize: "14px", lineHeight: 1.4, color: vars.color.gray600 });
export const rating = style({ margin: "12px 0 0", color: "#e0a600", fontFamily: vars.font.semibold, fontSize: "14px" });
export const content = style({ margin: "0 16px 16px", fontFamily: vars.font.body, fontSize: "16px", lineHeight: 1.55, color: vars.color.gray700, whiteSpace: "pre-wrap", overflowWrap: "anywhere" });

export const actions = style({
  width: "100%",
  minHeight: "24px",
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-end",
  gap: "8px",
  marginTop: "auto",
  padding: "0 16px 16px",
  boxSizing: "border-box",
});
export const actionButton = style({
  minWidth: "32px",
  height: "24px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "4px",
  padding: 0,
  border: 0,
  background: "transparent",
  color: "#ff747c",
  fontFamily: vars.font.body,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:hover": { background: vars.color.gray100 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 1 },
  },
});
export const commentButton = style([actionButton, { color: "#777777" }]);
export const icon = style({ width: "16px", height: "16px", flexShrink: 0 });
export const empty = style({ margin: "72px 20px", fontFamily: vars.font.body, fontSize: "14px", textAlign: "center", lineHeight: 1.7, color: vars.color.gray600, whiteSpace: "pre-line" });
export const error = style({ margin: "56px 20px", fontFamily: vars.font.body, fontSize: "14px", textAlign: "center", color: vars.color.negativeText });
export const retry = style({
  marginTop: "14px",
  padding: "9px 16px",
  border: 0,
  borderRadius: "10px",
  background: vars.color.gray900,
  color: vars.color.background,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  selectors: {
    "&:hover": { background: vars.color.darkGray },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 2 },
  },
});
