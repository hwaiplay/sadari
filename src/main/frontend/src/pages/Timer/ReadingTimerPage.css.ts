import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: `calc(100svh - ${vars.headerHeight} - ${vars.navHeight} - max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  padding: "20px 0 32px",
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const intro = style({
  marginBottom: "22px",
  display: "flex",
  alignItems: "center",
  gap: "6px",
});

export const introIcon = style({
  width: "20px",
  height: "20px",
  flexShrink: 0,
});

export const description = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.55,
});

export const card = style({
  width: "100%",
  marginBottom: "10px",
  padding: "16px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "22px",
  backgroundColor: vars.color.background,
  boxSizing: "border-box",
});

export const cardTitle = style({
  margin: "0 0 14px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "15px",
});

export const weekHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  marginBottom: "14px",
});

export const weekCount = style({
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
});

export const weekGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, minmax(0, 1fr))",
  gap: "7px",
});

export const day = style({
  minWidth: 0,
  padding: "10px 2px",
  borderRadius: vars.radius.sm,
  backgroundColor: vars.color.gray100,
  color: vars.color.gray700,
  textAlign: "center",
});

export const attendedDay = style({
  backgroundColor: vars.color.brandBg,
  color: vars.color.brandText,
});

export const todayDay = style({
  outline: `2px solid ${vars.color.brand}`,
  outlineOffset: "1px",
});

export const dayName = style({
  display: "block",
  marginBottom: "6px",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
});

export const dayMark = style({
  display: "block",
  fontSize: "17px",
  lineHeight: 1,
});

export const dayMinutes = style({
  display: "block",
  marginTop: "6px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "10px",
});

export const timerCard = style([card, {
  padding: "18px",
  "@media": {
    "screen and (max-width: 340px)": {
      padding: "12px",
    },
  },
}]);

export const timerLayout = style({
  display: "grid",
  gridTemplateColumns: "90px minmax(0, 1fr)",
  alignItems: "stretch",
  gap: "16px",
  minWidth: 0,
  transition: "grid-template-columns 520ms cubic-bezier(0.22, 1, 0.36, 1), gap 520ms cubic-bezier(0.22, 1, 0.36, 1)",
  "@media": {
    "screen and (max-width: 380px)": {
      gap: "12px",
    },
    "screen and (max-width: 340px)": {
      gap: "10px",
    },
    "(prefers-reduced-motion: reduce)": {
      transition: "none",
    },
  },
});

export const timerLayoutWithoutBook = style({
  gridTemplateColumns: "0 minmax(0, 1fr)",
  gap: 0,
  "@media": {
    "screen and (max-width: 380px)": {
      gap: 0,
    },
    "screen and (max-width: 340px)": {
      gap: 0,
    },
  },
});

export const bookCoverColumn = style({
  width: "90px",
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  opacity: 1,
  transition: "width 520ms cubic-bezier(0.22, 1, 0.36, 1), opacity 260ms ease",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      transition: "none",
    },
  },
});

export const bookCoverColumnHidden = style({
  width: 0,
  opacity: 0,
  pointerEvents: "none",
});

export const bookCoverFrame = style({
  position: "relative",
  display: "block",
  width: "90px",
  aspectRatio: "2 / 3",
  padding: 0,
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "6px",
  overflow: "hidden",
  backgroundColor: vars.color.gray100,
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.14)",
  boxSizing: "border-box",
});

export const bookCoverButton = style([bookCoverFrame, {
  color: vars.color.gray700,
  cursor: "pointer",
  transition: "border-color 160ms ease, box-shadow 160ms ease, background-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray400,
      boxShadow: "0 12px 26px rgba(0, 0, 0, 0.18)",
      transform: "translateY(-1px)",
    },
    "&[data-empty='true']": {
      border: 0,
      backgroundColor: "#fbfcfc",
      backgroundImage: `
        repeating-linear-gradient(90deg, ${vars.color.gray500} 0 9px, transparent 9px 16px),
        repeating-linear-gradient(90deg, ${vars.color.gray500} 0 9px, transparent 9px 16px),
        repeating-linear-gradient(180deg, ${vars.color.gray500} 0 9px, transparent 9px 16px),
        repeating-linear-gradient(180deg, ${vars.color.gray500} 0 9px, transparent 9px 16px)
      `,
      backgroundPosition: "top left, bottom left, top left, top right",
      backgroundSize: "100% 2px, 100% 2px, 2px 100%, 2px 100%",
      backgroundRepeat: "no-repeat",
      boxShadow: "none",
    },
    "&[data-empty='true']:hover": {
      borderColor: vars.color.gray500,
      boxShadow: "none",
      transform: "none",
    },
    "&:focus-visible": {
      borderColor: vars.color.brandText,
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "3px",
    },
  },
}]);

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookCoverPlaceholder = style({
  position: "relative",
  display: "flex",
  width: "100%",
  height: "100%",
  padding: "16px 10px",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "8px",
  backgroundColor: "transparent",
  color: vars.color.gray600,
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  lineHeight: 1.35,
  textAlign: "center",
  boxSizing: "border-box",
});

export const bookPlaceholderPlus = style({
  position: "absolute",
  top: "50%",
  left: "50%",
  display: "block",
  color: vars.color.gray700,
  fontFamily: vars.font.body,
  fontSize: "36px",
  fontWeight: 300,
  lineHeight: 1,
  transform: "translate(-50%, -50%)",
});

export const bookPlaceholderText = style({
  position: "absolute",
  right: "8px",
  bottom: "24px",
  left: "8px",
  textAlign: "center",
});

export const coverActionLabel = style({
  position: "absolute",
  right: "8px",
  bottom: "8px",
  left: "8px",
  minHeight: "30px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "0 8px",
  borderRadius: "6px",
  backgroundColor: "rgba(21, 21, 21, 0.72)",
  color: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.2,
  boxSizing: "border-box",
});

export const timerPanel = style({
  height: "156px",
  minWidth: 0,
  flex: "1 1 auto",
  display: "flex",
  flexDirection: "column",
  alignItems: "stretch",
  justifyContent: "center",
  gap: "4px",
  padding: "10px 2px",
  boxSizing: "border-box",
  transition: "padding 520ms cubic-bezier(0.22, 1, 0.36, 1)",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      transition: "none",
    },
  },
});

export const timerPanelWithoutBook = style({
  width: "min(100%, 320px)",
  justifySelf: "center",
  justifyContent: "center",
  gap: "12px",
  padding: "12px 0",
});

export const clock = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: "ui-monospace, SFMono-Regular, Menlo, monospace",
  fontSize: "clamp(40px, 11vw, 52px)",
  fontWeight: 800,
  fontVariantNumeric: "tabular-nums",
  letterSpacing: "-2px",
  lineHeight: 1,
  textAlign: "center",
  whiteSpace: "nowrap",
  "@media": {
    "screen and (max-width: 340px)": {
      fontSize: "clamp(30px, 10vw, 34px)",
      letterSpacing: "-1px",
    },
  },
});

export const book = style({
  minHeight: "30px",
  margin: 0,
  color: vars.color.gray700,
  fontFamily: vars.font.medium,
  fontSize: "13px",
  lineHeight: 1.35,
  textAlign: "center",
  overflow: "hidden",
  display: "-webkit-box",
  WebkitBoxOrient: "vertical",
  WebkitLineClamp: 2,
});

export const actions = style({
  display: "flex",
  flexWrap: "nowrap",
  gap: "7px",
  width: "100%",
  marginTop: 0,
});

export const actionsWithoutBook = style({
  alignSelf: "center",
  width: "min(100%, 300px)",
  marginTop: 0,
});

export const actionButton = style({
  minWidth: 0,
  flex: "1 1 0",
  borderRadius: "10px",
  boxSizing: "border-box",
  "@media": {
    "screen and (max-width: 340px)": {
      padding: "0 6px",
      fontSize: "12px",
    },
  },
});

export const modalOverlay = style({
  position: "fixed",
  inset: 0,
  zIndex: 1300,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "16px",
  backgroundColor: "rgba(21, 21, 21, 0.42)",
  boxSizing: "border-box",
});

export const bookModal = style({
  width: "min(480px, 100%)",
  maxHeight: "calc(100dvh - 32px)",
  display: "flex",
  flexDirection: "column",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "22px",
  overflow: "hidden",
  backgroundColor: vars.color.background,
  boxShadow: "0 24px 64px rgba(21, 21, 21, 0.24)",
});

export const modalHeader = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: "16px",
  padding: "20px 20px 16px",
});

export const modalTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
});

export const modalDescription = style({
  margin: "7px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
});

export const modalClose = style({
  width: "36px",
  height: "36px",
  flex: "0 0 36px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "9px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.background,
  cursor: "pointer",
  boxSizing: "border-box",
  transition: "border-color 150ms ease, background-color 150ms ease",
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

export const modalBody = style({
  minHeight: 0,
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  padding: "16px 20px",
  overflowY: "auto",
});

export const modalBookOption = style({
  width: "100%",
  minHeight: "104px",
  display: "grid",
  gridTemplateColumns: "58px minmax(0, 1fr)",
  alignItems: "center",
  gap: "14px",
  padding: "8px 14px 8px 8px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: "#f7f8f8",
  color: vars.color.gray700,
  textAlign: "left",
  cursor: "pointer",
  boxSizing: "border-box",
  transition: "border-color 150ms ease, background-color 150ms ease, color 150ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray300,
      backgroundColor: "#fbfbfb",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
    "&[data-selected='true']": {
      borderColor: "#78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
    },
  },
});

export const modalBookCover = style({
  width: "58px",
  aspectRatio: "2 / 3",
  display: "block",
  borderRadius: "4px",
  overflow: "hidden",
  backgroundColor: vars.color.gray100,
  boxShadow: "0 5px 12px rgba(21, 21, 21, 0.14)",
});

export const modalBookText = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "7px",
});

export const modalBookTitle = style({
  color: "inherit",
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  lineHeight: 1.45,
  overflow: "hidden",
  display: "-webkit-box",
  WebkitBoxOrient: "vertical",
  WebkitLineClamp: 2,
});

export const modalBookState = style({
  color: vars.color.brandText,
  fontFamily: vars.font.medium,
  fontSize: "12px",
});

export const modalEmpty = style({
  margin: 0,
  padding: "36px 16px",
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const modalFooter = style({
  display: "grid",
  gridTemplateColumns: "minmax(0, 1fr) minmax(0, 1fr)",
  gap: "10px",
  padding: "14px 20px 20px",
});

export const modalFooterButton = style({
  width: "100%",
  minWidth: 0,
  padding: "0 8px",
  fontSize: "13px",
  boxSizing: "border-box",
});

export const empty = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
});

export const heatmapCard = style([card, {
  marginTop: "24px",
  padding: "20px",
}]);

export const heatmapState = style({
  margin: 0,
  padding: "24px 12px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const heatmapError = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "12px",
  padding: "24px 12px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const heatmapRetry = style({
  minHeight: "34px",
  padding: "0 12px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "8px",
  backgroundColor: vars.color.background,
  color: vars.color.gray700,
  fontFamily: vars.font.medium,
  fontSize: "13px",
  cursor: "pointer",
});
