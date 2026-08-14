import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

// 지연 조회된 통계가 준비될 때 화면 높이를 유지하며 밝기가 흐르도록 표시한다
const statisticsPulse = keyframes({
  "0%, 100%": {
    opacity: 0.45,
  },
  "50%": {
    opacity: 0.8,
  },
});

export const section = style({
  width: "100%",
  minWidth: 0,
  margin: "24px 0 0",
  padding: "20px",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 22px rgba(0, 0, 0, 0.05)",
});

export const observerOnly = style({
  display: "block",
  width: "100%",
  height: "1px",
});

export const header = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  marginBottom: "24px",
  textAlign: "center",
});

export const title = style({
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  lineHeight: 1.35,
  color: vars.color.black,
});

export const description = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  color: vars.color.gray600,
});

export const content = style({
  display: "flex",
  flexDirection: "column",
  gap: "28px",
});

export const chartBlock = style({
  minWidth: 0,
});

export const chartTitle = style({
  margin: "0 0 14px",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.35,
  color: vars.color.gray900,
});

export const chartHeader = style({
  width: "100%",
  minWidth: 0,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  marginBottom: "14px",
});

export const chartHeaderTitle = style([chartTitle, { margin: 0 }]);

export const chartBasis = style({
  flexShrink: 0,
  fontFamily: vars.font.medium,
  fontSize: "11px",
  lineHeight: 1.3,
  color: vars.color.gray600,
});

export const yearSelect = style({
  zIndex: 2,
  flexShrink: 0,
});

export const yearSelectTrigger = style({
  minWidth: "auto",
  height: "32px",
  padding: 0,
  border: 0,
  borderRadius: 0,
  backgroundColor: "transparent",
  fontFamily: vars.font.medium,
  fontSize: "14px",
  gap: "10px",
});

export const yearOptionList = style({
  minWidth: "112px",
});

export const yearSelectOption = style({
  fontSize: "14px",
});

export const scrollArea = style({
  position: "relative",
  width: "100%",
  minWidth: 0,
});

export const scrollHint = style({
  position: "absolute",
  left: "50%",
  top: "50%",
  zIndex: 2,
  minHeight: "30px",
  padding: "0 10px",
  border: "1px solid rgba(255, 255, 255, 0.72)",
  borderRadius: "999px",
  backgroundColor: "rgba(0, 0, 0, 0.48)",
  color: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.3,
  whiteSpace: "nowrap",
  display: "inline-flex",
  alignItems: "center",
  transform: "translate(-50%, -50%)",
  pointerEvents: "none",
  boxShadow: "0 4px 12px rgba(0, 0, 0, 0.12)",
  opacity: 1,
  transition: "opacity 1400ms ease-out 120ms",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      transition: "none",
    },
  },
});

export const scrollHintDismissed = style({
  opacity: 0,
});

export const horizontalScroll = style({
  width: "100%",
  minWidth: 0,
  overflowX: "auto",
  overflowY: "hidden",
  paddingBottom: "5px",
  scrollbarWidth: "thin",
  scrollbarColor: "transparent transparent",
  outline: "none",
  selectors: {
    "&::-webkit-scrollbar": {
      height: "6px",
    },
    "&::-webkit-scrollbar-track": {
      backgroundColor: "transparent",
    },
    "&::-webkit-scrollbar-thumb": {
      borderRadius: "999px",
      backgroundColor: "transparent",
      transition: "background-color 1100ms ease-out",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
      borderRadius: "6px",
    },
  },
});

export const horizontalScrollActive = style({
  scrollbarColor: `${vars.color.gray300} transparent`,
  selectors: {
    "&::-webkit-scrollbar-thumb": {
      backgroundColor: vars.color.gray300,
      transition: "background-color 140ms ease-out",
    },
  },
});

export const heatmapCalendar = style({
  width: "max-content",
  minWidth: "100%",
  padding: "0 2px 2px",
  boxSizing: "border-box",
});

export const heatmapMonths = style({
  width: "max-content",
  minHeight: "18px",
  display: "grid",
  columnGap: "3px",
  alignItems: "start",
});

export const heatmapMonth = style({
  fontFamily: vars.font.body,
  fontSize: "10px",
  lineHeight: 1,
  color: vars.color.gray600,
  whiteSpace: "nowrap",
});

export const heatmapGrid = style({
  width: "max-content",
  display: "grid",
  gridTemplateRows: "repeat(7, 10px)",
  gridAutoColumns: "10px",
  gridAutoFlow: "column",
  gap: "3px",
});

export const heatmapSpacer = style({
  width: "10px",
  height: "10px",
});

export const heatmapCell = style({
  width: "10px",
  height: "10px",
  borderRadius: "2px",
  outline: "none",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "1px",
    },
  },
});

export const heatmapLegendRow = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "8px",
  marginTop: "10px",
  fontFamily: vars.font.body,
  fontSize: "10px",
  lineHeight: 1.3,
  color: vars.color.gray600,
});

export const heatmapHelp = style({
  minWidth: 0,
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  whiteSpace: "nowrap",
  "@media": {
    "screen and (max-width: 390px)": {
      fontSize: "10px",
      letterSpacing: "-0.2px",
    },
  },
});

export const heatmapLegend = style({
  flexShrink: 0,
  display: "flex",
  alignItems: "center",
  gap: "5px",
});

export const legendCell = style({
  width: "10px",
  height: "10px",
  borderRadius: "2px",
});

export const statusLayout = style({
  display: "grid",
  gridTemplateColumns: "148px minmax(0, 1fr)",
  alignItems: "center",
  gap: "24px",
  padding: "4px 8px 2px",
  "@media": {
    "screen and (max-width: 390px)": {
      gridTemplateColumns: "1fr",
      justifyItems: "center",
      gap: "18px",
    },
  },
});

export const donut = style({
  width: "132px",
  height: "132px",
  padding: "18px",
  boxSizing: "border-box",
  borderRadius: "50%",
  display: "grid",
  placeItems: "center",
});

export const donutCenter = style({
  width: "100%",
  height: "100%",
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "4px",
  boxShadow: "0 2px 10px rgba(52, 112, 77, 0.08)",
});

export const donutTotal = style({
  fontFamily: vars.font.semibold,
  fontSize: "20px",
  lineHeight: 1,
  color: vars.color.gray900,
});

export const donutLabel = style({
  fontFamily: vars.font.body,
  fontSize: "10px",
  color: vars.color.gray600,
});

export const statusLegend = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  gap: "12px",
});

export const statusLegendItem = style({
  display: "grid",
  gridTemplateColumns: "10px minmax(0, 1fr) auto",
  alignItems: "center",
  gap: "9px",
});

export const statusDot = style({
  width: "10px",
  height: "10px",
  borderRadius: "50%",
});

export const statusName = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  color: vars.color.gray700,
});

export const statusCount = style({
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  color: vars.color.gray900,
});

export const streakGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: "10px",
});

export const streakCard = style({
  minHeight: "88px",
  padding: "16px",
  boxSizing: "border-box",
  border: "1px solid #d9eee0",
  borderRadius: "16px",
  background: "linear-gradient(145deg, #f8fcf9 0%, #eef8f2 100%)",
  display: "flex",
  flexDirection: "column",
  justifyContent: "space-between",
  gap: "12px",
});

export const streakLabel = style({
  fontFamily: vars.font.medium,
  fontSize: "12px",
  color: vars.color.gray700,
});

export const streakValue = style({
  fontFamily: vars.font.semibold,
  fontSize: "22px",
  lineHeight: 1,
  color: "#34704d",
});

export const topBookList = style({
  margin: 0,
  padding: 0,
  listStyle: "none",
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const topBookItem = style({
  width: "100%",
  minWidth: 0,
  minHeight: "72px",
  padding: "10px 12px",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "14px",
  backgroundColor: "#fbfcfb",
  display: "grid",
  gridTemplateColumns: "34px 40px minmax(0, 1fr) auto",
  alignItems: "center",
  gap: "10px",
  "@media": {
    "screen and (max-width: 390px)": {
      gridTemplateColumns: "28px 36px minmax(0, 1fr)",
      gap: "8px",
    },
  },
});

export const topBookButton = style({
  color: "inherit",
  font: "inherit",
  textAlign: "left",
  cursor: "pointer",
  transition: "border-color 160ms ease, background-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {
      borderColor: "#b9dfc7",
      backgroundColor: "#f6fbf8",
      transform: "translateY(-1px)",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const topBookRank = style({
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  color: "#34704d",
  whiteSpace: "nowrap",
});

export const topBookCover = style({
  width: "40px",
  height: "52px",
  borderRadius: "5px",
  objectFit: "cover",
  backgroundColor: vars.color.gray100,
  boxShadow: "0 2px 7px rgba(0, 0, 0, 0.1)",
  "@media": {
    "screen and (max-width: 390px)": {
      width: "36px",
      height: "48px",
    },
  },
});

export const topBookInfo = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "5px",
});

export const topBookTitle = style({
  overflow: "hidden",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  lineHeight: 1.35,
  color: vars.color.gray900,
  whiteSpace: "nowrap",
  textOverflow: "ellipsis",
});

export const topBookAuthor = style({
  overflow: "hidden",
  fontFamily: vars.font.body,
  fontSize: "10px",
  lineHeight: 1.3,
  color: vars.color.gray600,
  whiteSpace: "nowrap",
  textOverflow: "ellipsis",
});

export const topBookTime = style({
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  color: "#34704d",
  whiteSpace: "nowrap",
  "@media": {
    "screen and (max-width: 390px)": {
      gridColumn: "2 / 4",
      justifySelf: "end",
      marginTop: "-8px",
    },
  },
});

export const emptyState = style({
  minHeight: "84px",
  margin: 0,
  padding: "18px",
  boxSizing: "border-box",
  borderRadius: "14px",
  backgroundColor: "#f7f8f8",
  display: "grid",
  placeItems: "center",
  textAlign: "center",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  color: vars.color.gray600,
});

export const ratingList = style({
  display: "flex",
  flexDirection: "column",
  gap: "9px",
});

export const ratingRow = style({
  display: "grid",
  gridTemplateColumns: "30px minmax(0, 1fr) 38px",
  alignItems: "center",
  gap: "9px",
});

export const ratingGrade = style({
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  color: vars.color.gray700,
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
});

export const ratingStar = style({
  color: "#e4b94f",
  fontSize: "12px",
  lineHeight: 1,
});

export const ratingTrack = style({
  height: "9px",
  overflow: "hidden",
  borderRadius: "999px",
  backgroundColor: "#eef2ef",
});

export const ratingFill = style({
  display: "block",
  minWidth: 0,
  height: "100%",
  borderRadius: "inherit",
  background: "linear-gradient(90deg, #9edfc2 0%, #34704d 100%)",
  transition: "width 220ms ease",
});

export const ratingCount = style({
  textAlign: "right",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  color: vars.color.gray900,
});

export const comparisonTable = style({
  overflow: "hidden",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "14px",
  backgroundColor: "#ffffff",
});

const comparisonGridBase = style({
  display: "grid",
  gridTemplateColumns: "80px minmax(64px, 1fr) minmax(64px, 1fr) minmax(72px, auto)",
  alignItems: "center",
  gap: "8px",
  padding: "10px 12px",
  boxSizing: "border-box",
  "@media": {
    "screen and (max-width: 390px)": {
      gridTemplateColumns: "62px minmax(50px, 1fr) minmax(50px, 1fr) minmax(58px, auto)",
      gap: "5px",
      padding: "9px 8px",
    },
  },
});

export const comparisonHeader = style([comparisonGridBase, {
  backgroundColor: "#f6faf7",
  fontFamily: vars.font.semibold,
  fontSize: "10px",
  color: vars.color.gray600,
  textAlign: "center",
}]);

export const comparisonRow = style([comparisonGridBase, {
  borderTop: `1px solid ${vars.color.gray200}`,
}]);

export const comparisonLabel = style({
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  lineHeight: 1.3,
  color: vars.color.gray700,
});

export const comparisonValue = style({
  textAlign: "center",
  fontFamily: vars.font.medium,
  fontSize: "11px",
  lineHeight: 1.3,
  color: vars.color.gray900,
});

export const comparisonDifference = style({
  justifySelf: "end",
  minHeight: "24px",
  padding: "0 7px",
  borderRadius: "999px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: vars.font.semibold,
  fontSize: "10px",
  lineHeight: 1.2,
  whiteSpace: "nowrap",
});

export const comparisonIncrease = style({
  backgroundColor: "#e8f6ed",
  color: "#34704d",
});

export const comparisonDecrease = style({
  backgroundColor: "#fff0f1",
  color: "#a9555c",
});

export const comparisonSame = style({
  backgroundColor: "#f1f3f2",
  color: vars.color.gray600,
});

export const loading = style({
  minHeight: "330px",
  display: "flex",
  flexDirection: "column",
  gap: "18px",
  animation: `${statisticsPulse} 1.4s ease-in-out infinite`,
});

export const loadingLine = style({
  height: "14px",
  borderRadius: "7px",
  backgroundColor: "#eef8f2",
  selectors: {
    "&:nth-child(2)": {
      width: "72%",
    },
    "&:nth-child(3)": {
      height: "92px",
    },
    "&:nth-child(4)": {
      height: "132px",
    },
  },
});

export const error = style({
  minHeight: "220px",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "14px",
  textAlign: "center",
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: vars.color.gray600,
});

export const retryButton = style({
  minHeight: "36px",
  padding: "0 16px",
  border: `1px solid ${vars.color.gray600}`,
  borderRadius: "10px",
  backgroundColor: "#ffffff",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
  color: vars.color.gray900,
  cursor: "pointer",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const settingButton = style({
  alignSelf: "flex-end",
  marginTop: "10px",
  border: 0,
  backgroundColor: "transparent",
  color: "#8a8a8a",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  display: "inline-flex",
  alignItems: "center",
  gap: "2px",
  transition: "color 160ms ease, opacity 160ms ease",
  selectors: {
    "&:hover": {
      color: "#555555",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "4px",
      borderRadius: "4px",
    },
  },
});

export const modalOverlay = style({
  position: "fixed",
  inset: 0,
  width: "100vw",
  height: "100dvh",
  zIndex: 1200,
  padding: "0 16px",
  boxSizing: "border-box",
  backgroundColor: "rgba(0, 0, 0, 0.34)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  overflow: "hidden",
  overscrollBehavior: "contain",
  animation: `${keyframes({
    from: { opacity: 0 },
    to: { opacity: 1 },
  })} 160ms ease-out`,
});

export const modal = style({
  width: "min(600px, 100%)",
  maxHeight: "calc(100dvh - 48px)",
  overflowY: "auto",
  padding: "20px",
  boxSizing: "border-box",
  borderRadius: "18px",
  backgroundColor: "#ffffff",
  boxShadow: "0 22px 58px rgba(0, 0, 0, 0.24)",
  animation: `${keyframes({
    from: { opacity: 0, transform: "translateY(8px)" },
    to: { opacity: 1, transform: "translateY(0)" },
  })} 180ms ease-out`,
});

export const modalHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
});

export const modalTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "18px",
  lineHeight: 1.35,
  color: vars.color.gray900,
});

export const modalClose = style({
  width: "32px",
  height: "32px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "#f3f4f5",
  color: vars.color.black,
  fontSize: "22px",
  lineHeight: 1,
  cursor: "pointer",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
      borderRadius: "8px",
    },
    "&:disabled": {
      opacity: 0.45,
      cursor: "default",
    },
  },
});

export const modalBody = style({
  display: "flex",
  flexDirection: "column",
  gap: "24px",
  marginTop: "24px",
});

export const settingFieldset = style({
  minWidth: 0,
  margin: 0,
  padding: 0,
  border: 0,
});

export const optionGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(86px, 1fr))",
  gap: "8px",
});

export const optionButton = style({
  minHeight: "44px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "12px",
  backgroundColor: "#f7f8f8",
  color: "#7b8187",
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  cursor: "pointer",
  transition: "border-color 150ms ease, background-color 150ms ease, color 150ms ease",
  selectors: {
    "&:hover": {
      borderColor: vars.color.gray300,
      backgroundColor: "#fbfbfb",
    },
    "&[aria-pressed='true']": {
      border: "1px solid #78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
    },
    "&[aria-pressed='true']:hover": {
      border: "1px solid #78b991",
      backgroundColor: "#eef8f2",
      color: "#34704d",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});

export const settingHelp = style({
  margin: "0 0 14px",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.5,
  color: vars.color.gray600,
});

export const modalFooter = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "8px",
  marginTop: "20px",
});

const modalButtonBase = style({
  minWidth: "76px",
  height: "46px",
  borderRadius: "8px",
  padding: "0 14px",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
  transition: "background-color 160ms ease",
});

export const cancelButton = style([modalButtonBase, {
  border: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  selectors: {
    "&:hover": {
      border: `1px solid ${vars.color.gray600}`,
      backgroundColor: vars.color.gray100,
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.6,
    },
  },
}]);

export const saveButton = style([modalButtonBase, {
  border: `1px solid ${vars.color.gray900}`,
  backgroundColor: vars.color.gray900,
  color: "#ffffff",
  selectors: {
    "&:hover": {
      border: `1px solid ${vars.color.darkGray}`,
      backgroundColor: vars.color.darkGray,
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.6,
    },
  },
}]);
