import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  flexDirection: "column",
  width: "100%",
  maxWidth: 600,
  margin: "0 auto",
  padding: "20px 0 38px",
  boxSizing: "border-box",
});

export const searchForm = style({
  width: "100%",
});

export const searchLabel = style({
  position: "relative",
  display: "block",
  width: "100%",
});

export const hiddenLabel = style({
  position: "absolute",
  width: 1,
  height: 1,
  padding: 0,
  margin: -1,
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  border: 0,
});

export const searchInput = style({
  width: "100%",
  height: 42,
  padding: "0 48px 0 16px",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 200,
  background: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: 16,
  letterSpacing: "-0.16px",
  outline: "none",
  selectors: {
    // "&:focus": {
    //   boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)"
    // },
    "&::placeholder": {
      color: vars.color.gray600,
    },
  },
});

export const searchButton = style({
  position: "absolute",
  top: 8,
  right: 10,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  minWidth: 0,
  width: 26,
  height: 26,
  padding: 0,
  border: 0,
  borderRadius: 999,
  background: "transparent",
});

export const searchIcon = style({
  width: 18,
  height: 18,
});

export const interestSection = style({
  display: "flex",
  flexDirection: "column",
  gap: 12,
  marginTop: 30,
});

export const sectionTitleRow = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: 16,
});

export const sectionTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  fontWeight: 600,
  lineHeight: 1.2,
  letterSpacing: "-0.16px",
});

export const editButton = style({
  display: "flex",
  alignItems: "center",
  gap: 0,
  minWidth: 0,
  height: 28,
  padding: "0 2px 0 8px",
  border: 0,
  background: "transparent",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: 14,
  lineHeight: 1.2,
  letterSpacing: "-0.14px",
});

export const editIcon = style({
  width: 14,
  height: 14,
});

export const interestChips = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 6,
});

export const interestChip = style({
  padding: "6px 18px",
  borderRadius: 200,
  background: vars.color.gray100,
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: 14,
  lineHeight: 1,
  letterSpacing: "-0.14px",
});

export const recommendSection = style({
  display: "flex",
  flexDirection: "column",
  gap: 14,
  marginTop: 30,
});

export const recommendHeader = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  gap: 4,
});

export const sectionDescription = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: 1.2,
  letterSpacing: "-0.12px",
});

export const clubList = style({
  display: "flex",
  flexDirection: "column",
  gap: 14,
});

export const empty = style({
  margin: 0,
  padding: "46px 18px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  textAlign: "center",
});

export const categoryBrowse = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: 8,
  minWidth: 0,
  height: "auto",
  width: "100%",
  marginTop: 30,
  padding: 0,
  border: 0,
  borderRadius: 8,
  background: "transparent",
  textAlign: "left",
  cursor: "pointer",
  transition: "background-color 160ms ease",
});

export const browseIcon = style({
  width: 18,
  height: 18,
});

export const categoryCopy = style({
  display: "flex",
  flexDirection: "column",
  gap: 4,
});

export const categoryTitle = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: 1.2,
});
