import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "calc(100svh - 110px)",
  margin: "12px 0",
  padding: "18px 0 110px",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
  display: "flex",
  flexDirection: "column",
  gap: "34px",
});

export const section = style({
  position: "relative",
  padding: "20px 10px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
});

export const title = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.3,
  letterSpacing: 0,
});

export const withdrawalTypeSection = style({
  paddingTop: "20px",
});

export const standaloneTitle = style({
  margin: "0 0 18px",
  padding: "0 0 0 6px",
  textAlign: "left",
});

export const helpButton = style({
  position: "absolute",
  top: "18px",
  right: "14px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  width: "20px",
  height: "20px",
  padding: 0,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: "#6f757b",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1,
  cursor: "pointer",
  transition: "border-color 160ms ease, color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: "#78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const option = style({
  display: "flex",
  alignItems: "center",
  gap: "12px",
  minHeight: "76px",
  marginBottom: "8px",
  padding: "12px 14px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "14px",
  backgroundColor: "#f4f5f6",
  color: "#8b9097",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease, color 160ms ease",
  selectors: {
    "&:last-child": {
      marginBottom: 0,
    },
    "&:hover": {
      borderColor: vars.color.gray300,
      backgroundColor: "#fafafa",
    },
    "&:has(input:checked)": {
      borderColor: "#78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
      boxShadow: "0 5px 14px rgba(74, 143, 101, 0.12)",
    },
    "&:has(input:focus-visible)": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const choiceInput = style({
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

export const optionText = style({
  display: "grid",
  gap: "6px",
});

export const optionTitle = style({
  color: "inherit",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.3,
});

export const optionDescription = style({
  color: "inherit",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.55,
});

export const reasonList = style({
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: "8px",
  "@media": {
    "screen and (max-width: 420px)": {
      gridTemplateColumns: "1fr",
    },
  },
});

export const reason = style({
  display: "flex",
  alignItems: "center",
  gap: "10px",
  minHeight: "44px",
  padding: "0 12px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: "#f4f5f6",
  color: "#8b9097",
  fontFamily: vars.font.body,
  fontSize: "13px",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray300,
      backgroundColor: "#fafafa",
    },
    "&:has(input:checked)": {
      borderColor: "#78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
      boxShadow: "0 5px 14px rgba(74, 143, 101, 0.12)",
    },
    "&:has(input:focus-visible)": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const textareaWrap = style({
  position: "relative",
  marginTop: "14px",
});

export const textarea = style({
  width: "100%",
  minHeight: "120px",
  padding: "14px 14px 32px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "14px",
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  resize: "vertical",
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.55,
  outline: "none",
  boxSizing: "border-box",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:focus": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
    "&::placeholder": {
      color: "#999999",
    },
  },
});

export const byteCounter = style({
  position: "absolute",
  right: "12px",
  bottom: "10px",
  color: "#8b9097",
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1.2,
  pointerEvents: "none",
});

export const withdrawButton = style({
  width: "100%",
  minHeight: "48px",
  margin: "-8px 0 0",
  border: "1px solid #d84b4b",
  borderRadius: "999px",
  background: "#ffffff",
  color: "#c83f3f",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  transition: "background-color 160ms ease, color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#fff5f5",
    },
    "&:disabled": {
      opacity: 0.55,
      cursor: "default",
    },
  },
});

const policyBackdropFadeIn = keyframes({
  from: {
    opacity: 0,
  },
  to: {
    opacity: 1,
  },
});

const policyBackdropFadeOut = keyframes({
  from: {
    opacity: 1,
  },
  to: {
    opacity: 0,
  },
});

const policyModalFadeIn = keyframes({
  from: {
    opacity: 0,
    transform: "translateY(10px) scale(0.98)",
  },
  to: {
    opacity: 1,
    transform: "translateY(0) scale(1)",
  },
});

const policyModalFadeOut = keyframes({
  from: {
    opacity: 1,
    transform: "translateY(0) scale(1)",
  },
  to: {
    opacity: 0,
    transform: "translateY(8px) scale(0.98)",
  },
});

export const policyModalBackdrop = style({
  position: "fixed",
  inset: 0,
  zIndex: 10000,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: "100dvh",
  padding: "20px",
  backgroundColor: "rgba(0, 0, 0, 0.34)",
  boxSizing: "border-box",
  animation: `${policyBackdropFadeIn} 180ms ease-out both`,
});

export const policyModalBackdropClosing = style({
  animation: `${policyBackdropFadeOut} 180ms ease-in both`,
});

export const policyModal = style({
  width: "min(430px, 100%)",
  maxHeight: "calc(100dvh - 40px)",
  padding: "24px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "18px",
  backgroundColor: "#ffffff",
  boxShadow: "0 20px 54px rgba(0, 0, 0, 0.18)",
  overflowY: "auto",
  boxSizing: "border-box",
  animation: `${policyModalFadeIn} 180ms ease-out both`,
});

export const policyModalClosing = style({
  animation: `${policyModalFadeOut} 180ms ease-in both`,
});

export const policyModalHeader = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: "16px",
});

export const policyModalEyebrow = style({
  display: "block",
  marginBottom: "5px",
  color: "#4d8b66",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.3,
});

export const policyModalTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
  letterSpacing: 0,
});

export const policyModalClose = style({
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  flex: "0 0 auto",
  width: "32px",
  height: "32px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f4",
  color: "#555b60",
  fontFamily: vars.font.body,
  fontSize: "23px",
  lineHeight: 1,
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: "#e9ebeb",
      color: vars.color.black,
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const policyModalBody = style({
  display: "grid",
  gap: "12px",
  marginTop: "22px",
});

export const policyItem = style({
  padding: "16px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: "#f8f9f9",
});

export const policyItemHeading = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "10px",
});

export const policyItemTitle = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.35,
});

export const policyRecoverBadge = style({
  flex: "0 0 auto",
  padding: "4px 8px",
  borderRadius: "999px",
  backgroundColor: "#e7f5ec",
  color: "#34704d",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  lineHeight: 1.2,
});

export const policyDeleteBadge = style({
  flex: "0 0 auto",
  padding: "4px 8px",
  borderRadius: "999px",
  backgroundColor: "#fff0f0",
  color: "#b74343",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  lineHeight: 1.2,
});

export const policyList = style({
  display: "grid",
  gap: "7px",
  margin: "12px 0 0",
  paddingLeft: "18px",
  color: "#62676c",
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.55,
});

export const policyModalConfirm = style({
  width: "100%",
  minHeight: "44px",
  marginTop: "18px",
  border: "1px solid #78b991",
  borderRadius: "999px",
  backgroundColor: "#eef8f2",
  color: "#34704d",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  transition: "background-color 160ms ease, color 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#dff1e6",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});
