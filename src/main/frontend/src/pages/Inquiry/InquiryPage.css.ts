import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: `calc(100svh - ${vars.headerHeight} - ${vars.navHeight} - max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  padding: "20px 2px 32px",
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const suspendedDetailPage = style({
  paddingBottom: `calc(112px + env(safe-area-inset-bottom, 0px))`,
});

export const intro = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: "16px",
  marginBottom: "22px",
});

export const introText = style({
  minWidth: 0,
});

export const sectionTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.4,
});

export const description = style({
  margin: "5px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.55,
});

export const actionButton = style({
  flexShrink: 0,
  minWidth: "88px",
  height: "40px",
  padding: "0 15px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray100,
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
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

export const itemMetaGroup = style({
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  gap: "6px",
});

export const category = style({
  flexShrink: 0,
  padding: "3px 8px",
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  lineHeight: 1.5,
});

export const state = style({
  minWidth: 0,
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const itemTitle = style({
  display: "block",
  marginTop: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.45,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const itemBottom = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-start",
  flexWrap: "wrap",
  gap: "6px",
  marginTop: "10px",
});

export const meta = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
  lineHeight: 1.4,
});

export const unread = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "5px",
  marginLeft: "auto",
  color: vars.color.negativeText,
  fontFamily: vars.font.semibold,
  fontSize: vars.fontSize.caption,
});

export const unreadDot = style({
  width: "6px",
  height: "6px",
  borderRadius: "50%",
  backgroundColor: vars.color.negativeText,
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
  lineHeight: 1.6,
  textAlign: "center",
});

export const moreButton = style({
  width: "100%",
  height: "44px",
  marginTop: "18px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.md,
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
      color: vars.color.gray600,
      cursor: "default",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
});

export const formIntro = style({
  marginBottom: "26px",
  paddingBottom: "22px",
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const form = style({
  display: "flex",
  flexDirection: "column",
  gap: "22px",
});

export const field = style({
  display: "flex",
  flexDirection: "column",
  gap: "9px",
});

export const label = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.4,
});

export const required = style({
  marginLeft: "3px",
  color: vars.color.negativeText,
});

export const categorySelect = style({
  display: "flex",
  width: "100%",
});

export const categorySelectTrigger = style({
  width: "100%",
  height: "48px",
  padding: "0 14px 0 15px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  boxShadow: "none",
  transition: "border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray400,
      backgroundColor: "#fbfcfc",
    },
    "&:focus-visible, &[aria-expanded='true']": {
      borderColor: vars.color.brandText,
      backgroundColor: vars.color.background,
      outline: "none",
      boxShadow: `0 0 0 3px ${vars.color.brandBg}`,
    },
  },
});

export const fixedCategory = style({
  display: "flex",
  alignItems: "center",
  width: "100%",
  height: "48px",
  padding: "0 15px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  lineHeight: 1.4,
  boxSizing: "border-box",
});

export const categoryOptionList = style({
  top: "calc(100% + 8px)",
  right: "auto",
  left: 0,
  zIndex: 30,
  width: "100%",
  maxHeight: "260px",
  padding: "6px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: vars.color.background,
  boxShadow: "0 12px 30px rgba(21, 21, 21, 0.12)",
  gap: "3px",
  overflowY: "auto",
  boxSizing: "border-box",
});

export const categoryOption = style({
  minHeight: "42px",
  padding: "0 12px",
  borderRadius: "8px",
  color: vars.color.gray700,
  fontFamily: vars.font.body,
  fontSize: "14px",
  transition: "background-color 140ms ease, color 140ms ease",
  selectors: {
    "&:hover, &:focus-visible": {
      backgroundColor: vars.color.gray100,
      color: vars.color.black,
      outline: "none",
    },
    "&[aria-selected='true']": {
      backgroundColor: vars.color.brandBg,
      color: vars.color.brandText,
      fontFamily: vars.font.semibold,
    },
  },
});

export const input = style({
  width: "100%",
  minHeight: "46px",
  padding: "0 13px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  boxSizing: "border-box",
  selectors: {
    "&:focus": {
      borderColor: vars.color.brandText,
      outline: "none",
      boxShadow: `0 0 0 3px ${vars.color.brandBg}`,
    },
    "&::placeholder": {
      color: vars.color.gray500,
    },
  },
});

export const textarea = style({
  width: "100%",
  minHeight: "230px",
  padding: "13px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.65,
  resize: "vertical",
  boxSizing: "border-box",
  selectors: {
    "&:focus": {
      borderColor: vars.color.brandText,
      outline: "none",
      boxShadow: `0 0 0 3px ${vars.color.brandBg}`,
    },
    "&::placeholder": {
      color: vars.color.gray500,
    },
  },
});

export const fieldFooter = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  marginTop: "-3px",
});

export const helper = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
  lineHeight: 1.5,
});

export const count = style({
  flexShrink: 0,
  marginLeft: "auto",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
});

export const submitButton = style({
  width: "100%",
  height: "48px",
  marginTop: "4px",
  border: `1px solid ${vars.color.gray900}`,
  borderRadius: vars.radius.md,
  backgroundColor: vars.color.gray900,
  color: vars.color.background,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray700,
      backgroundColor: vars.color.gray700,
    },
    "&:disabled": {
      borderColor: vars.color.gray200,
      backgroundColor: vars.color.gray100,
      color: vars.color.gray500,
      cursor: "default",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray700}`,
      outlineOffset: "2px",
    },
  },
});

export const suspensionBackBar = style({
  position: "fixed",
  right: "var(--sadari-scrollbar-compensation, 0px)",
  bottom: 0,
  left: 0,
  zIndex: 40,
  padding: `12px 0 max(12px, env(safe-area-inset-bottom, 0px))`,
  backgroundColor: vars.color.background,
  boxShadow: "0 -6px 24px rgb(0 0 0 / 8%)",
  boxSizing: "border-box",
});

export const suspensionBackInner = style({
  width: "100%",
  maxWidth: "600px",
  margin: "0 auto",
  padding: `0 ${vars.space.md}`,
  boxSizing: "border-box",
});

export const error = style({
  margin: "-4px 0 0",
  color: vars.color.negativeText,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
});

export const detailHeader = style({
  padding: "4px 0 21px",
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const detailMeta = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "flex-start",
  flexWrap: "wrap",
  gap: "6px",
  marginTop: "12px",
});

export const detailTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "22px",
  lineHeight: 1.45,
  overflowWrap: "anywhere",
});

export const detailDate = style({
  display: "inline-block",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: vars.fontSize.caption,
});

export const body = style({
  margin: 0,
  padding: "24px 0 30px",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "15px",
  lineHeight: 1.75,
  whiteSpace: "pre-wrap",
  overflowWrap: "anywhere",
});

export const answerSection = style({
  paddingTop: "22px",
  borderTop: `1px solid ${vars.color.gray200}`,
});

export const answerHeading = style({
  margin: "0 0 13px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.4,
});

export const answers = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const answer = style({
  padding: "16px",
  borderLeft: `3px solid ${vars.color.brand}`,
  borderRadius: `0 ${vars.radius.sm} ${vars.radius.sm} 0`,
  backgroundColor: vars.color.brandBg,
});

export const answerMeta = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
});

export const answerAuthor = style({
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
});

export const answerBody = style({
  margin: "10px 0 0",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.7,
  whiteSpace: "pre-wrap",
  overflowWrap: "anywhere",
});
