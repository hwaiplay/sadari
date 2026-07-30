import { globalStyle, keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const float = keyframes({
  "0%, 100%": {
    transform: "translate3d(0, 0, 0)",
  },
  "50%": {
    transform: "translate3d(0, -8px, 0)",
  },
});

const pulse = keyframes({
  "0%, 100%": {
    opacity: 0.45,
    transform: "scale(0.92)",
  },
  "50%": {
    opacity: 0.9,
    transform: "scale(1.08)",
  },
});

export const page = style({
  position: "relative",
  display: "grid",
  gridTemplateRows: "auto minmax(0, 1fr) auto",
  width: "min(100%, 600px)",
  minHeight: "100svh",
  margin: "0 auto",
  overflow: "hidden",
  color: vars.color.black,
  background:
    "radial-gradient(circle at 85% 8%, rgba(229, 154, 95, 0.2), transparent 31%), radial-gradient(circle at 10% 76%, rgba(79, 157, 145, 0.16), transparent 32%), #f8f5ef",
});

export const header = style({
  position: "relative",
  zIndex: 3,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  minHeight: "70px",
  padding: "max(18px, env(safe-area-inset-top)) 24px 8px",
});

export const logo = style({
  width: "86px",
  height: "auto",
});

export const progressText = style({
  color: vars.color.gray700,
  fontFamily: vars.font.semibold,
  fontSize: vars.fontSize.caption,
  letterSpacing: "0.08em",
});

export const viewport = style({
  minHeight: 0,
  overflow: "hidden",
  touchAction: "pan-y",
});

export const track = style({
  display: "flex",
  width: "400%",
  height: "100%",
  transition: "transform 460ms cubic-bezier(0.22, 1, 0.36, 1)",
  willChange: "transform",
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      transitionDuration: "1ms",
    },
  },
});

export const slide = style({
  display: "grid",
  gridTemplateRows: "auto minmax(240px, 1fr)",
  alignItems: "center",
  width: "25%",
  minWidth: 0,
  height: "100%",
  padding: "22px 30px 14px",
  overflowY: "auto",
  selectors: {
    '&[aria-hidden="true"]': {
      visibility: "hidden",
      transition: "visibility 0s linear 460ms",
    },
  },
  "@media": {
    "(max-height: 720px)": {
      gridTemplateRows: "auto minmax(205px, 1fr)",
      paddingTop: "8px",
    },
  },
});

export const copy = style({
  position: "relative",
  zIndex: 2,
  maxWidth: "430px",
});

export const eyebrow = style({
  marginBottom: "14px",
  color: "#6d6155",
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  letterSpacing: "0.08em",
});

export const title = style({
  color: "#27231f",
  fontFamily: vars.font.heading,
  fontSize: "clamp(30px, 8vw, 42px)",
  lineHeight: 1.16,
  letterSpacing: "-0.045em",
  whiteSpace: "pre-line",
  wordBreak: "keep-all",
});

export const description = style({
  maxWidth: "420px",
  marginTop: "18px",
  color: "#655f58",
  fontSize: "15px",
  lineHeight: 1.7,
  wordBreak: "keep-all",
});

export const coverVisual = style({
  position: "relative",
  alignSelf: "center",
  width: "min(100%, 370px)",
  height: "270px",
  margin: "16px auto 0",
});

export const coverBookBack = style({
  position: "absolute",
  top: "28px",
  left: "calc(50% - 118px)",
  width: "148px",
  height: "196px",
  border: "1px solid rgba(75, 64, 54, 0.12)",
  borderRadius: "5px 15px 15px 5px",
  background: "linear-gradient(145deg, #d9b44a, #e8c974)",
  boxShadow: "0 22px 40px rgba(108, 83, 45, 0.16)",
  transform: "rotate(-8deg)",
});

export const coverBookMain = style({
  position: "absolute",
  top: "12px",
  left: "calc(50% - 74px)",
  width: "152px",
  height: "206px",
  padding: "30px 22px",
  border: "1px solid rgba(59, 70, 69, 0.12)",
  borderRadius: "5px 16px 16px 5px",
  background: "linear-gradient(145deg, #4f9d91, #89c8bd)",
  boxShadow: "0 28px 58px rgba(50, 93, 87, 0.25)",
  transform: "rotate(4deg)",
  animation: `${float} 4.8s ease-in-out infinite`,
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});

export const coverBookLine = style({
  display: "block",
  width: "78px",
  height: "8px",
  borderRadius: vars.radius.xl,
  background: "rgba(255, 255, 255, 0.82)",
});

export const coverBookLineShort = style({
  display: "block",
  width: "48px",
  height: "6px",
  marginTop: "10px",
  borderRadius: vars.radius.xl,
  background: "rgba(255, 255, 255, 0.55)",
});

export const paletteCard = style({
  position: "absolute",
  right: "2px",
  bottom: "6px",
  display: "grid",
  gridTemplateColumns: "1fr repeat(4, 22px)",
  alignItems: "center",
  gap: "8px",
  width: "255px",
  minHeight: "58px",
  padding: "12px 14px",
  border: "1px solid rgba(70, 63, 56, 0.12)",
  borderRadius: "17px",
  background: "rgba(255, 255, 255, 0.84)",
  boxShadow: "0 16px 38px rgba(86, 72, 61, 0.12)",
  backdropFilter: "blur(12px)",
});

export const paletteLabel = style({
  color: "#61594f",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
});

const swatch = style({
  width: "22px",
  height: "22px",
  border: "2px solid rgba(255, 255, 255, 0.9)",
  borderRadius: "50%",
  boxShadow: "0 2px 7px rgba(0, 0, 0, 0.1)",
});

export const swatchCoral = style([swatch, { background: "#c96f64" }]);
export const swatchGold = style([swatch, { background: "#d9b44a" }]);
export const swatchTeal = style([swatch, { background: "#4f9d91" }]);
export const swatchNavy = style([swatch, { background: "#4f6380" }]);

export const goalVisual = style({
  position: "relative",
  alignSelf: "center",
  width: "min(100%, 380px)",
  height: "275px",
  margin: "14px auto 0",
});

export const calendarCard = style({
  position: "absolute",
  top: "18px",
  left: "4px",
  width: "250px",
  padding: "22px",
  border: "1px solid rgba(70, 63, 56, 0.1)",
  borderRadius: "24px",
  background: "rgba(255, 255, 255, 0.82)",
  boxShadow: "0 22px 50px rgba(86, 72, 61, 0.12)",
  transform: "rotate(-3deg)",
});

export const calendarHeader = style({
  display: "flex",
  justifyContent: "space-between",
  marginBottom: "20px",
});

globalStyle(`${calendarHeader} span:first-child`, {
  width: "72px",
  height: "9px",
  borderRadius: vars.radius.xl,
  background: "#383530",
});

globalStyle(`${calendarHeader} span:last-child`, {
  width: "34px",
  height: "9px",
  borderRadius: vars.radius.xl,
  background: "#d8d1c8",
});

export const calendarGrid = style({
  display: "grid",
  gridTemplateColumns: "repeat(7, 1fr)",
  gap: "10px",
});

globalStyle(`${calendarGrid} i`, {
  display: "block",
  aspectRatio: "1",
  borderRadius: "7px",
  background: "#eee9e2",
});

export const calendarActiveCoral = style({
  background: "#c96f64 !important",
});

export const calendarActiveTeal = style({
  background: "#4f9d91 !important",
});

export const calendarActiveGold = style({
  background: "#d9b44a !important",
});

export const calendarActiveNavy = style({
  background: "#4f6380 !important",
});

export const goalCard = style({
  position: "absolute",
  right: "0",
  bottom: "14px",
  display: "flex",
  flexDirection: "column",
  width: "178px",
  padding: "18px",
  border: "1px solid rgba(79, 99, 128, 0.16)",
  borderRadius: "20px",
  background: "#4f6380",
  color: "#ffffff",
  boxShadow: "0 20px 40px rgba(79, 99, 128, 0.25)",
  animation: `${float} 5.2s ease-in-out infinite`,
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});

export const goalCaption = style({
  fontSize: "11px",
  opacity: 0.78,
});

export const goalRate = style({
  marginTop: "6px",
  fontFamily: vars.font.heading,
  fontSize: "28px",
});

export const goalBar = style({
  display: "block",
  height: "6px",
  marginTop: "14px",
  overflow: "hidden",
  borderRadius: vars.radius.xl,
  background: "rgba(255, 255, 255, 0.24)",
});

globalStyle(`${goalBar} span`, {
  display: "block",
  width: "67%",
  height: "100%",
  borderRadius: "inherit",
  background: "#f1d48a",
});

export const socialVisual = style({
  position: "relative",
  alignSelf: "center",
  width: "min(100%, 380px)",
  height: "280px",
  margin: "12px auto 0",
});

export const reportCard = style({
  position: "absolute",
  top: "12px",
  left: "calc(50% - 123px)",
  display: "grid",
  gridTemplateColumns: "78px 1fr",
  gap: "18px",
  width: "246px",
  minHeight: "178px",
  padding: "20px",
  border: "1px solid rgba(70, 63, 56, 0.1)",
  borderRadius: "24px",
  background: "rgba(255, 255, 255, 0.86)",
  boxShadow: "0 24px 55px rgba(86, 72, 61, 0.14)",
});

export const reportCover = style({
  display: "block",
  height: "112px",
  borderRadius: "4px 10px 10px 4px",
  background: "linear-gradient(145deg, #c96f64, #e5a49b)",
  boxShadow: "0 12px 24px rgba(151, 78, 68, 0.2)",
});

export const reportLines = style({
  paddingTop: "12px",
});

globalStyle(`${reportLines} i`, {
  display: "block",
  height: "8px",
  marginBottom: "11px",
  borderRadius: vars.radius.xl,
  background: "#ddd7cf",
});

globalStyle(`${reportLines} i:nth-child(2)`, {
  width: "78%",
});

globalStyle(`${reportLines} i:nth-child(3)`, {
  width: "58%",
});

export const reportHeart = style({
  position: "absolute",
  right: "20px",
  bottom: "17px",
  color: "#c96f64",
  fontSize: "25px",
  lineHeight: 1,
});

const profileBubble = style({
  position: "absolute",
  display: "grid",
  placeItems: "center",
  width: "50px",
  height: "50px",
  border: "4px solid #f8f5ef",
  borderRadius: "50%",
  color: "#ffffff",
  fontFamily: vars.font.heading,
  boxShadow: "0 10px 25px rgba(70, 63, 56, 0.15)",
  animation: `${pulse} 3.8s ease-in-out infinite`,
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
    },
  },
});

export const profileBubbleLeft = style([
  profileBubble,
  {
    left: "12px",
    bottom: "42px",
    background: "#4f9d91",
  },
]);

export const profileBubbleRight = style([
  profileBubble,
  {
    right: "16px",
    top: "26px",
    background: "#d9b44a",
    animationDelay: "-1.4s",
  },
]);

export const notificationCard = style({
  position: "absolute",
  right: "15px",
  bottom: "20px",
  display: "flex",
  alignItems: "center",
  gap: "9px",
  padding: "12px 15px",
  border: "1px solid rgba(79, 157, 145, 0.16)",
  borderRadius: vars.radius.xl,
  background: "#ffffff",
  color: "#5f5a53",
  fontFamily: vars.font.semibold,
  fontSize: "11px",
  boxShadow: "0 15px 34px rgba(86, 72, 61, 0.12)",
});

export const notificationDot = style({
  width: "8px",
  height: "8px",
  borderRadius: "50%",
  background: "#c96f64",
  boxShadow: "0 0 0 5px rgba(201, 111, 100, 0.12)",
});

export const nicknameCard = style({
  alignSelf: "center",
  width: "min(100%, 420px)",
  margin: "26px auto 18px",
  padding: "24px",
  border: "1px solid rgba(70, 63, 56, 0.12)",
  borderRadius: "24px",
  background: "rgba(255, 255, 255, 0.82)",
  boxShadow: "0 24px 60px rgba(86, 72, 61, 0.12)",
  backdropFilter: "blur(14px)",
});

export const nicknameLabel = style({
  display: "block",
  marginBottom: "10px",
  color: "#4f4942",
  fontFamily: vars.font.semibold,
  fontSize: "13px",
});

export const nicknameInputWrap = style({
  position: "relative",
});

export const nicknameInput = style({
  width: "100%",
  height: "56px",
  padding: "0 70px 0 16px",
  border: "1px solid #aaa196",
  borderRadius: "14px",
  outline: "none",
  background: "#ffffff",
  color: "#2f2b27",
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  transition: "border-color 160ms ease, box-shadow 160ms ease",
  selectors: {
    "&:focus": {
      borderColor: "#4f6380",
      boxShadow: "0 0 0 4px rgba(79, 99, 128, 0.12)",
    },
    "&:disabled": {
      cursor: "wait",
      opacity: 0.65,
    },
  },
});

export const nickLength = style({
  position: "absolute",
  top: "50%",
  right: "15px",
  color: vars.color.gray600,
  fontSize: "11px",
  transform: "translateY(-50%)",
});

export const nicknameHint = style({
  marginTop: "10px",
  color: "#7a736b",
  fontSize: "11px",
  lineHeight: 1.5,
  wordBreak: "keep-all",
});

export const startButton = style({
  width: "100%",
  height: "50px",
  marginTop: "20px",
  border: "1px solid #4e5968",
  borderRadius: "14px",
  background: "#ffffff",
  color: "#343c47",
  cursor: "pointer",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  transition: "transform 150ms ease, background 150ms ease, box-shadow 150ms ease",
  selectors: {
    "&:hover:not(:disabled)": {
      background: "#f1f3f5",
      boxShadow: "0 8px 20px rgba(52, 60, 71, 0.1)",
      transform: "translateY(-1px)",
    },
    "&:active:not(:disabled)": {
      transform: "translateY(0)",
    },
    "&:disabled": {
      cursor: "wait",
      opacity: 0.55,
    },
  },
});

export const footer = style({
  position: "relative",
  zIndex: 3,
  padding: "12px 24px max(20px, env(safe-area-inset-bottom))",
  background: "linear-gradient(180deg, rgba(248, 245, 239, 0), #f8f5ef 28%)",
});

export const dots = style({
  display: "flex",
  justifyContent: "center",
  gap: "8px",
  minHeight: "24px",
});

export const dot = style({
  width: "7px",
  height: "7px",
  marginTop: "8px",
  borderRadius: vars.radius.xl,
  background: "#cfc7bd",
  cursor: "pointer",
  transition: "width 220ms ease, background 220ms ease",
});

export const dotActive = style([
  dot,
  {
    width: "24px",
    background: "#4f6380",
  },
]);

export const navigationButtons = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "10px",
  marginTop: "8px",
});

const navigationButton = style({
  height: "44px",
  border: "1px solid #8d857d",
  borderRadius: "13px",
  background: "rgba(255, 255, 255, 0.7)",
  color: "#4f4942",
  cursor: "pointer",
  fontFamily: vars.font.semibold,
  fontSize: "13px",
  transition: "background 150ms ease, opacity 150ms ease",
  selectors: {
    "&:hover:not(:disabled)": {
      background: "#ffffff",
    },
    "&:disabled": {
      cursor: "default",
      opacity: 0.25,
    },
  },
});

export const previousButton = style([navigationButton]);

export const nextButton = style([
  navigationButton,
  {
    borderColor: "#5e6876",
    color: "#343c47",
    background: "#ffffff",
  },
]);
