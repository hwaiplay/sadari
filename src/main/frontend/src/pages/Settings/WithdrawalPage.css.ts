import { keyframes, style } from "@vanilla-extract/css";
import { destructiveButton, selectableOption } from "@/app/styles/controls.css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "calc(100svh - 112px)",
  padding: "28px 0 20px",
  backgroundColor: "#ffffff",
  boxSizing: "border-box",
  display: "flex",
  flexDirection: "column",
  gap: "34px",
});

export const section = style({
  position: "relative",
  borderRadius: "22px",
  backgroundColor: "#ffffff",
});

export const title = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "17px",
  lineHeight: 1.3,
  letterSpacing: 0,
});

export const withdrawalTypeSection = style({
  paddingTop: "26px",
  "@media": {
    "screen and (max-width: 420px)": {
      paddingTop: "22px",
    },
  },
});

export const standaloneTitle = style({
  margin: "0 0 18px",
  textAlign: "left",
});

export const helpButton = style({
  position: "absolute",
  top: "22px",
  right: "0",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  width: "26px",
  height: "26px",
  padding: 0,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.gray600,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
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
  "@media": {
    "screen and (max-width: 420px)": {
      top: "18px",
      right: "16px",
    },
  },
});

export const optionList = style({
  display: "grid",
  gap: "14px",
});

export const option = style([
  selectableOption,
  {
    display: "flex",
    padding: "16px 18px",
    cursor: "pointer",
  },
]);

export const accountOption = style([
  option,
  {
    alignItems: "flex-start",
    gap: "14px",
  }
])

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
  width: "100%",
  gap: "4px",
});

export const optionHeading = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
});

export const optionTitle = style({
  color: "inherit",
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.35,
});

export const recoverBadge = style({
  flex: "0 0 auto",
  padding: "4px 10px",
  borderRadius: "999px",
  backgroundColor: vars.color.gray600,
  color: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.2,
});

export const deleteBadge = style({
  flex: "0 0 auto",
  padding: "4px 10px",
  borderRadius: "999px",
  backgroundColor: vars.color.negativeBg,
  color: vars.color.negativeText,
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.2,
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
  gap: "12px",
  "@media": {
    "screen and (max-width: 420px)": {
      gridTemplateColumns: "1fr",
    },
  },
});

export const reason = style([
  option,
  {
    alignItems: "center",
    minHeight: "48px",
    padding: "4px 16px",
    fontFamily: vars.font.body,
    fontSize: "14px",
  }
]);

export const textareaWrap = style({
  position: "relative",
  marginTop: "18px",
});

export const textarea = style({
  width: "100%",
  minHeight: "126px",
  padding: "16px 16px 34px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  resize: "none",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  outline: "none",
  boxSizing: "border-box",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:focus": {
      borderColor: vars.color.gray400,
      backgroundColor: "#ffffff",
    },
    "&::placeholder": {
      color: vars.color.gray600,
    },
  },
});

export const byteCounter = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "10px",
  lineHeight: 1.2,
  pointerEvents: "none",
  textAlign: "right",
  width: "100%",
  display: "inline-block"
});

export const withdrawButton = style([
  destructiveButton,
  {
    width: "100%",
    minHeight: "48px",
    margin: "auto 0 0",
    borderRadius: "8px",
    fontFamily: vars.font.semibold,
    fontSize: "14px",
    cursor: "pointer",
  },
]);

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
  backgroundColor: "rgba(0, 0, 0, 0.5)",
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
  backgroundColor: vars.color.gray100,
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.gray200,
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
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
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
  fontSize: "16px",
  lineHeight: 1.35,
});

export const policyList = style({
  display: "grid",
  gap: "7px",
  margin: "12px 0 0",
  paddingLeft: "18px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
});

export const policyModalConfirm = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "fit-content",
  minWidth: "78px",
  minHeight: "38px",
  margin: "18px 0 0 auto",
  padding: "0 18px",
  borderRadius: "8px",
  backgroundColor: vars.color.gray900,
  color: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  boxSizing: "border-box",
  transition: "background-color 160ms ease, border-color 160ms ease, color 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: vars.color.darkGray,
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray700}`,
      outlineOffset: "2px",
    },
  },
});
