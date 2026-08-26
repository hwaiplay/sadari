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
  position: "relative",
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
export const authorName = style({ display: "block", margin: 0, fontFamily: vars.font.semibold, fontSize: "16px", color: vars.color.black });
export const activity = style({ display: "block", margin: "3px 0 0", fontFamily: vars.font.body, fontSize: "12px", color: vars.color.gray600 });

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

export const reportMediaRow = style({
  display: "grid",
  gridTemplateColumns: "50px minmax(0, 1fr)",
  alignItems: "center",
  gap: "14px",
  width: "100%",
  padding: "0 16px 16px",
  boxSizing: "border-box",
});

export const reportCoverLink = style({
  display: "block",
  borderRadius: "4px",
  selectors: {
    "&:hover": { opacity: 0.78 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 3 },
  },
});

export const backgroundMediaButton = style([
  mediaButton,
  {
    gridTemplateColumns: "minmax(0, 1fr)",
    gap: "12px",
  },
]);

export const backgroundMediaWrap = style({
  position: "relative",
  display: "block",
  width: "100%",
  aspectRatio: "16 / 9",
  overflow: "hidden",
  borderRadius: "12px",
  background: vars.color.gray100,
});

export const media = style({
  display: "block",
  background: vars.color.gray100,
  objectFit: "cover",
});
export const reportMedia = style([media, { width: "50px", height: "74px", borderRadius: "4px" }]);
export const backgroundMedia = style([media, { width: "100%", height: "100%", borderRadius: "12px" }]);
export const mediaInfo = style({ minWidth: 0, alignSelf: "center", display: "flex", flexDirection: "column", gap: "3px" });
export const bookInfoLink = style({
  display: "block",
  minWidth: 0,
  color: "inherit",
  textDecoration: "none",
  borderRadius: "4px",
  selectors: {
    "&:hover": { background: vars.color.gray100 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 2 },
  },
});
export const title = style({ display: "block", margin: 0, fontFamily: vars.font.semibold, fontSize: "14px", lineHeight: 1.25, color: vars.color.black, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" });
export const authorSearchLink = style({
  display: "block",
  minWidth: 0,
  width: "fit-content",
  maxWidth: "100%",
  borderRadius: "4px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.25,
  overflow: "hidden",
  textDecoration: "none",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  selectors: {
    "&:hover": { background: vars.color.gray100, color: vars.color.black },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 2 },
  },
});
export const ratingStatusRow = style({ minWidth: 0, minHeight: "24px", marginTop: "4px", display: "flex", alignItems: "center", justifyContent: "flex-start", gap: "8px" });
export const rating = style({ display: "inline-flex", alignItems: "center", gap: "3px", color: vars.color.black, fontFamily: vars.font.semibold, fontSize: "14px", lineHeight: 1.45 });
export const ratingIcon = style({ width: "18px", height: "18px", display: "block", flexShrink: 0, color: "#ffd45c" });

export const contentSection = style({
  padding: "0 16px 12px",
});

export const reportContentLink = style({
  display: "block",
  borderRadius: "4px",
  color: "inherit",
  textDecoration: "none",
  selectors: {
    "&:hover": { background: vars.color.gray100 },
    "&:focus-visible": { outline: `2px solid ${vars.color.brand}`, outlineOffset: 2 },
  },
});

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
export const likeActionGroup = style({
  minWidth: "32px",
  height: "24px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "4px",
  color: "#ff747c",
  fontFamily: vars.font.body,
  fontSize: "14px",
});
export const likeIconButton = style([actionButton, { minWidth: "16px", width: "16px" }]);
export const likeCountButton = style({ color: "#ff747c" });
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
