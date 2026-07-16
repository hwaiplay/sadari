import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: "100vh",
  padding: "0 0 110px",
  backgroundColor: "#ffffff",
});

export const profileShell = style({
  width: "100%",
  maxWidth: "520px",
  minHeight: "calc(100vh - 110px)",
  margin: "0 auto",
  backgroundColor: "#ffffff",
});

export const cover = style({
  position: "relative",
  height: "260px",
  overflow: "hidden",
  borderRadius: "22px",
  backgroundColor: "#d9e0e7",
  backgroundSize: "cover",
  backgroundPosition: "center",
});

export const coverActionGroup = style({
  position: "absolute",
  right: "14px",
  bottom: "14px",
  zIndex: 2,
  display: "inline-flex",
  alignItems: "center",
  gap: "8px",
});

export const coverProfileEditButton = style({
  minHeight: "30px",
  padding: "0 10px",
  border: "1px solid rgba(255, 255, 255, 0.72)",
  borderRadius: "999px",
  backgroundColor: "rgba(0, 0, 0, 0.48)",
  color: "#ffffff",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  display: "inline-flex",
  alignItems: "center",
  gap: "5px",
  cursor: "pointer",
});

export const coverImageButton = style([
  coverProfileEditButton,
  {
    backgroundColor: "rgba(255, 255, 255, 0.92)",
    color: vars.color.black,
    borderColor: "rgba(255, 255, 255, 0.92)",
  },
]);

export const coverSaveButton = style([
  coverProfileEditButton,
  {
    backgroundColor: vars.color.black,
    borderColor: vars.color.black,
    color: "#ffffff",
    selectors: {
      "&:disabled": {
        cursor: "default",
        opacity: 0.62,
      },
    },
  },
]);

export const actionIcon = style({
  width: "14px",
  height: "14px",
  fill: "currentColor",
  flexShrink: 0,
});

export const coverEmptyText = style({
  position: "absolute",
  left: "50%",
  top: "50%",
  zIndex: 1,
  margin: 0,
  transform: "translate(-50%, -50%)",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.4,
  color: "#7a8490",
  textAlign: "center",
  whiteSpace: "nowrap",
});

export const profileBody = style({
  position: "relative",
  padding: "0 22px 28px",
  display: "flex",
  flexDirection: "column",
  alignItems: "stretch",
});

export const profileHeaderRow = style({
  width: "100%",
  marginTop: "-34px",
  display: "grid",
  gridTemplateColumns: "112px minmax(0, 1fr)",
  alignItems: "start",
  gap: "18px",
});

export const avatarWrap = style({
  position: "relative",
  width: "112px",
  height: "112px",
  margin: 0,
});

export const profileImage = style({
  width: "112px",
  height: "112px",
  borderRadius: "50%",
  objectFit: "cover",
  border: "4px solid #ffffff",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
});

export const avatarCameraButton = style({
  position: "absolute",
  right: "-8px",
  bottom: "8px",
  width: "38px",
  height: "38px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  boxShadow: "0 6px 16px rgba(0, 0, 0, 0.16)",
});

export const cameraIcon = style({
  width: "19px",
  height: "19px",
  fill: "currentColor",
});

export const hiddenInput = style({
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

export const profileText = style({
  width: "100%",
  minWidth: 0,
  paddingTop: "42px",
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  gap: "8px",
  textAlign: "left",
});

export const profileName = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "22px",
  lineHeight: 1.3,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const profileIntro = style({
  margin: 0,
  maxWidth: "100%",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  color: "#666666",
  wordBreak: "break-word",
});

export const profileNameInput = style({
  width: "100%",
  minWidth: 0,
  height: "40px",
  margin: 0,
  padding: "0 12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.3,
  outline: "none",
  boxShadow: "inset 0 1px 0 rgba(255, 255, 255, 0.8)",
  selectors: {
    "&:focus": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
  },
});

export const profileIntroInput = style({
  width: "100%",
  minWidth: 0,
  height: "56px",
  padding: "9px 12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: "#f8f9fa",
  color: "#666666",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  outline: "none",
  resize: "none",
  boxShadow: "inset 0 1px 0 rgba(255, 255, 255, 0.8)",
  selectors: {
    "&:focus": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
  },
});

export const monthlySummary = style({
  width: "calc(100% + 44px)",
  marginTop: "28px",
  marginLeft: "-22px",
  marginRight: "-22px",
  padding: "6px 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
});

export const readingSummaryRow = style({
  minHeight: "66px",
  display: "grid",
  gridTemplateColumns: "minmax(0, 1fr) auto",
  alignItems: "center",
  gap: "12px",
});

export const goalAchievementSummary = style({
  padding: "12px 8px 10px",
});

export const goalAchievementGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
  gap: "8px",
});

export const goalAchievementItem = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "10px",
});

export const goalAchievementLabel = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.2,
  color: "#777777",
});

export const goalAchievementCount = style({
  fontFamily: vars.font.heading,
  fontSize: "18px",
  lineHeight: 1.2,
  color: vars.color.black,
});

export const readingSummaryToggle = style({
  minWidth: 0,
  width: "100%",
  minHeight: "66px",
  padding: 0,
  border: 0,
  backgroundColor: "transparent",
  display: "grid",
  gridTemplateColumns: "38px minmax(0, 1fr) 28px",
  alignItems: "center",
  gap: "12px",
  textAlign: "left",
  cursor: "pointer",
});

export const readingSummaryToggleStatic = style([
  readingSummaryToggle,
  {
    cursor: "default",
    selectors: {
      "&:disabled": {
        opacity: 1,
      },
    },
  },
]);

export const readingSummaryChevron = style({
  width: "28px",
  height: "28px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#f8f9fa",
  color: "#666666",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  transform: "rotate(0deg)",
  transition: "transform 200ms ease, color 180ms ease, background-color 180ms ease, border-color 180ms ease",
});

export const readingSummaryChevronOpen = style([
  readingSummaryChevron,
  {
    color: vars.color.black,
    backgroundColor: "#ffffff",
    borderColor: "#cfd4da",
    transform: "rotate(180deg)",
  },
]);

export const readingSummaryChevronIcon = style({
  width: "17px",
  height: "17px",
  fill: "currentColor",
});

export const readingSummaryDivider = style({
  width: "100%",
  height: "1px",
  backgroundColor: "#eef0f2",
});

export const readingSummaryPanel = style({
  display: "grid",
  gridTemplateRows: "0fr",
  opacity: 0,
  overflow: "hidden",
  transition: "grid-template-rows 220ms ease, opacity 180ms ease",
});

export const readingSummaryPanelOpen = style([
  readingSummaryPanel,
  {
    gridTemplateRows: "1fr",
    opacity: 1,
  },
]);

export const readingSummaryPanelInner = style({
  minHeight: 0,
  display: "flex",
  flexDirection: "column",
  gap: "8px",
  padding: "0 0 12px",
});

export const readingSummaryReport = style({
  width: "100%",
  minHeight: "48px",
  padding: "7px 8px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: "#fafafa",
  display: "grid",
  gridTemplateColumns: "32px minmax(0, 1fr)",
  alignItems: "center",
  gap: "9px",
  textAlign: "left",
  cursor: "pointer",
  transition: "background-color 160ms ease, border-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {
      backgroundColor: "#ffffff",
      borderColor: "#cfd4da",
      transform: "translateY(-1px)",
    },
  },
});

export const readingSummaryCover = style({
  width: "32px",
  height: "42px",
  borderRadius: "4px",
  objectFit: "cover",
  backgroundColor: "#f0f1f2",
});

export const readingSummaryBookText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "3px",
});

export const readingSummaryBookTitle = style({
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  fontFamily: vars.font.middle,
  fontSize: "13px",
  lineHeight: 1.25,
  color: vars.color.black,
});

export const readingSummaryBookMeta = style({
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1.25,
  color: "#777777",
});

export const readingSummaryEmpty = style({
  margin: "0 0 10px",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  color: "#777777",
});

export const monthlyCalendarIcon = style({
  position: "relative",
  width: "38px",
  height: "38px",
  border: `2px solid ${vars.color.black}`,
  borderRadius: "8px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  flexShrink: 0,
});

export const monthlyCalendarRing = style({
  position: "absolute",
  top: "6px",
  left: "7px",
  right: "7px",
  height: "2px",
  backgroundColor: vars.color.black,
  selectors: {
    "&::before": {
      content: "",
      position: "absolute",
      left: "3px",
      top: "-6px",
      width: "3px",
      height: "7px",
      borderRadius: "999px",
      backgroundColor: vars.color.black,
    },
    "&::after": {
      content: "",
      position: "absolute",
      right: "3px",
      top: "-6px",
      width: "3px",
      height: "7px",
      borderRadius: "999px",
      backgroundColor: vars.color.black,
    },
  },
});

export const monthlyCalendarMonth = style({
  marginTop: "7px",
  fontFamily: vars.font.heading,
  fontSize: "9px",
  lineHeight: 1,
  color: vars.color.black,
});

export const monthlySummaryText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "4px",
});

export const monthlySummaryLabel = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.3,
  color: "#777777",
});

export const monthlySummaryCount = style({
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.2,
  color: vars.color.black,
});

const monthlyDiffBase = style({
  minWidth: "38px",
  height: "30px",
  padding: "0 9px",
  border: 0,
  borderRadius: "999px",
  fontFamily: vars.font.heading,
  fontSize: "14px",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const monthlyDiffUp = style([
  monthlyDiffBase,
  {
    backgroundColor: "#e4f6e9",
    color: "#2f8f64",
  },
]);

export const monthlyDiffDown = style([
  monthlyDiffBase,
  {
    backgroundColor: "#fdeaea",
    color: "#c94b4b",
  },
]);

export const monthlyDiffNeutral = style({
  minWidth: "38px",
  height: "30px",
  padding: "0 9px",
  border: 0,
  borderRadius: "999px",
  backgroundColor: "#f3f4f5",
  color: "#777777",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const monthlyDiffTooltipWrap = style({
  position: "relative",
  display: "inline-flex",
  justifyContent: "flex-end",
});

export const monthlyDiffTooltip = style({
  position: "absolute",
  right: 0,
  top: "calc(100% + 9px)",
  zIndex: 5,
  width: "max-content",
  maxWidth: "190px",
  padding: "9px 11px",
  borderRadius: "10px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.45,
  textAlign: "left",
  boxShadow: "0 10px 28px rgba(0, 0, 0, 0.14)",
  border: `1px solid ${vars.color.gray300}`,
  selectors: {
    "&::before": {
      content: "",
      position: "absolute",
      right: "16px",
      top: "-6px",
      width: "10px",
      height: "10px",
      backgroundColor: "#ffffff",
      borderLeft: `1px solid ${vars.color.gray300}`,
      borderTop: `1px solid ${vars.color.gray300}`,
      transform: "rotate(45deg)",
    },
  },
});

export const goalProgressRow = style({
  position: "relative",
  display: "grid",
  gridTemplateColumns: "minmax(0, 1fr) 38px",
  alignItems: "center",
  gap: "8px",
  padding: "0 0 8px 50px",
});

export const goalProgressTarget = style({
  position: "absolute",
  left: 0,
  top: "0",
  width: "42px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1,
  color: "#888888",
  textAlign: "center",
  transform: "translateY(1px)",
});

export const goalProgressTrack = style({
  height: "12px",
  borderRadius: "999px",
  backgroundColor: "#eeeeee",
  overflow: "hidden",
});

export const goalProgressFill = style({
  display: "block",
  height: "100%",
  minWidth: "0%",
  maxWidth: "100%",
  borderRadius: "999px",
  transition: "width 220ms ease, background-color 180ms ease",
});

export const goalProgressMonth = style([
  goalProgressFill,
  {
    backgroundColor: "#ff929c",
  },
]);

export const goalProgressYear = style([
  goalProgressFill,
  {
    backgroundColor: "#8fd7f4",
  },
]);

export const goalProgressRate = style({
  fontFamily: vars.font.heading,
  fontSize: "13px",
  lineHeight: 1,
  textAlign: "center",
  whiteSpace: "nowrap",
  color: "#999999",
  transition: "color 180ms ease",
});

export const goalProgressRateMonth = style([
  goalProgressRate,
  {
    color: "#ff929c",
  },
]);

export const goalProgressRateYear = style([
  goalProgressRate,
  {
    color: "#8fd7f4",
  },
]);

export const goalProgressText = style({
  margin: "-2px 16px 8px 50px",
  fontFamily: vars.font.body,
  fontSize: "11px",
  color: "#888888",
});

export const goalSettingButton = style({
  alignSelf: "flex-end",
  marginTop: "10px",
  padding: "0 4px",
  border: 0,
  backgroundColor: "transparent",
  color: "#8a8a8a",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  lineHeight: 1.4,
  cursor: "pointer",
  display: "inline-flex",
  alignItems: "center",
  gap: "2px",
  transition: "color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover": {
      color: "#555555",
    },
  },
});

export const goalSettingArrow = style({
  fontSize: "17px",
  lineHeight: 1,
});

export const goalModalOverlay = style({
  position: "fixed",
  inset: 0,
  zIndex: 1200,
  padding: "24px",
  backgroundColor: "rgba(0, 0, 0, 0.34)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  animation: `${keyframes({
    from: { opacity: 0 },
    to: { opacity: 1 },
  })} 160ms ease-out`,
});

export const goalModal = style({
  width: "min(520px, 100%)",
  borderRadius: "18px",
  backgroundColor: "#ffffff",
  padding: "22px 20px 18px",
  boxShadow: "0 22px 58px rgba(0, 0, 0, 0.24)",
  animation: `${keyframes({
    from: { opacity: 0, transform: "translateY(8px)" },
    to: { opacity: 1, transform: "translateY(0)" },
  })} 180ms ease-out`,
});

export const goalHelpModal = style([
  goalModal,
  {
    width: "min(440px, 100%)",
  },
]);

export const goalModalHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
});

export const goalModalHeaderActions = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "8px",
});

export const goalHelpButton = style({
  minHeight: "30px",
  padding: "0 10px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: "#777777",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  lineHeight: 1,
  cursor: "pointer",
  transition: "border-color 160ms ease, color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: "#cfd4da",
      backgroundColor: "#f8f9fa",
      color: vars.color.black,
    },
  },
});

export const goalModalTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "18px",
  lineHeight: 1.35,
  color: vars.color.black,
});

export const goalModalClose = style({
  width: "32px",
  height: "32px",
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f5",
  color: vars.color.black,
  fontSize: "22px",
  lineHeight: 1,
  cursor: "pointer",
});

export const goalHelpBody = style({
  marginTop: "18px",
});

export const goalHelpLead = style({
  margin: "0 0 12px",
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.55,
  color: "#555555",
});

export const goalHelpList = style({
  margin: 0,
  paddingLeft: "18px",
  display: "flex",
  flexDirection: "column",
  gap: "9px",
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.55,
  color: vars.color.black,
});

export const goalModalBody = style({
  display: "grid",
  gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
  gap: "10px",
  marginTop: "20px",
});

export const goalInputLabel = style({
  display: "flex",
  flexDirection: "column",
  gap: "7px",
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: vars.color.black,
  textAlign: "center",
});

export const goalStepper = style({
  minHeight: "104px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "10px",
  backgroundColor: "#fafafa",
  overflow: "hidden",
  display: "grid",
  gridTemplateRows: "32px 40px 32px",
  gridTemplateColumns: "1fr",
  alignItems: "stretch",
  transition: "border-color 160ms ease, background-color 160ms ease",
  selectors: {
    "&:focus-within": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
  },
});

export const goalStepperButton = style({
  border: 0,
  backgroundColor: "#f0f1f2",
  color: "#555555",
  fontFamily: vars.font.heading,
  fontSize: "18px",
  lineHeight: 1,
  cursor: "pointer",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  transition: "background-color 160ms ease, color 160ms ease",
  selectors: {
    "&:first-child": {
      gridRow: "3",
    },
    "&:last-child": {
      gridRow: "1",
    },
    "&:hover": {
      backgroundColor: "#e7e9eb",
      color: vars.color.black,
    },
  },
});

export const goalInput = style({
  width: "100%",
  minWidth: 0,
  height: "40px",
  padding: "0 4px",
  border: 0,
  borderTop: `1px solid ${vars.color.gray300}`,
  borderBottom: `1px solid ${vars.color.gray300}`,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "15px",
  textAlign: "center",
  outline: "none",
  selectors: {
    "&::placeholder": {
      color: "#aaaaaa",
    },
  },
});

export const goalLimitInfo = style({
  minHeight: "52px",
  padding: "8px 7px",
  borderRadius: "9px",
  backgroundColor: "#f7f8f8",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "5px",
});

export const goalLimitPill = style({
  maxWidth: "100%",
  padding: "4px 8px",
  borderRadius: "999px",
  backgroundColor: "#edf7f1",
  color: "#3b8f64",
  fontFamily: vars.font.middle,
  fontSize: "11px",
  lineHeight: 1,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const goalLimitMuted = style({
  maxWidth: "100%",
  fontFamily: vars.font.body,
  fontSize: "11px",
  lineHeight: 1.25,
  color: "#777777",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const goalLimitDanger = style([
  goalLimitMuted,
  {
    color: "#c94b4b",
  },
]);

export const goalModalActions = style({
  display: "flex",
  justifyContent: "flex-end",
  gap: "8px",
  marginTop: "20px",
});

const goalModalButtonBase = style({
  minWidth: "76px",
  height: "36px",
  borderRadius: "999px",
  padding: "0 14px",
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const goalModalCancel = style([
  goalModalButtonBase,
  {
    border: `1px solid ${vars.color.gray300}`,
    backgroundColor: "#ffffff",
    color: vars.color.black,
  },
]);

export const goalModalSave = style([
  goalModalButtonBase,
  {
    border: `1px solid ${vars.color.black}`,
    backgroundColor: vars.color.black,
    color: "#ffffff",
    selectors: {
      "&:disabled": {
        cursor: "default",
        opacity: 0.6,
      },
    },
  },
]);
