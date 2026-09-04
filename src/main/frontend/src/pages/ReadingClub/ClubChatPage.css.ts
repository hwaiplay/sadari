/** 모임 채팅 화면 스타일 */
import {vars} from "@/app/styles/tokens.css";
import {style} from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  width: "100%",
  maxWidth: 600,
  minHeight: `calc(100dvh - ${vars.headerHeight} - ${vars.navHeight} - max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
  margin: "0 auto",
  padding: "20px 0 0",
  boxSizing: "border-box",
  flexDirection: "column",
});

export const header = style({display: "flex", flexDirection: "column", gap: 4, marginBottom: 20});
export const title = style({margin: 0, color: vars.color.black, fontFamily: vars.font.heading, fontSize: 20});
export const description = style({margin: 0, color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 14});

export const currentBookCard = style({
  display: "grid",
  gridTemplateColumns: "30px minmax(0, 1fr) 18px",
  minHeight: 58,
  marginBottom: 14,
  padding: "8px 12px",
  boxSizing: "border-box",
  alignItems: "center",
  gap: 10,
  borderRadius: 16,
  background: "#eef8f2",
  color: vars.color.black,
  textDecoration: "none",
  selectors: {
    "&:hover": {background: vars.color.brandBg},
    "&:focus-visible": {outline: "2px solid #78b991", outlineOffset: 2},
  },
});

export const currentBookImage = style({
  width: 30,
  height: 42,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 4,
  objectFit: "cover",
});

export const currentBookInformation = style({display: "flex", minWidth: 0, flexDirection: "column", gap: 3});
export const currentBookLabel = style({color: vars.color.brandText, fontFamily: vars.font.medium, fontSize: 10});
export const currentBookTitle = style({overflow: "hidden", color: vars.color.black, fontFamily: vars.font.semibold, fontSize: 14, lineHeight: "20px", textOverflow: "ellipsis", whiteSpace: "nowrap"});
export const currentBookArrow = style({color: vars.color.black, fontFamily: vars.font.medium, fontSize: 18, textAlign: "right"});

export const messagePanel = style({
  minHeight: 360,
  flex: 1,
  overflowY: "auto",
  padding: "0 0 20px",
  background: vars.color.background,
});

export const dateDivider = style({margin: "4px 0 18px", color: vars.color.gray500, fontFamily: vars.font.body, fontSize: 10, textAlign: "center"});
export const messageList = style({display: "flex", flexDirection: "column", gap: 16, margin: 0, padding: 0, listStyle: "none"});
export const messageRow = style({display: "flex", alignItems: "flex-start", gap: 8});
export const myMessageRow = style([messageRow, {justifyContent: "flex-end"}]);
export const avatar = style({width: 30, height: 30, marginTop: 18, flexShrink: 0, borderRadius: "50%", objectFit: "cover"});
export const messageContent = style({display: "flex", maxWidth: "82%", minWidth: 0, flexDirection: "column", alignItems: "flex-start", gap: 4});
export const myMessageContent = style([messageContent, {alignItems: "flex-end"}]);
export const sender = style({paddingLeft: 2, color: vars.color.gray600, fontFamily: vars.font.medium, fontSize: 10});
export const messageLine = style({display: "flex", maxWidth: "100%", alignItems: "flex-end", gap: 6});
export const myMessageLine = style([messageLine, {flexDirection: "row-reverse"}]);
export const bubble = style({padding: "10px 14px", borderRadius: 14, background: "#f1f1f1", color: vars.color.black, fontFamily: vars.font.body, fontSize: 14, lineHeight: "20px", whiteSpace: "pre-wrap", overflowWrap: "anywhere"});
export const myBubble = style([bubble, {background: "#e6f7ee", color: vars.color.brandText}]);
export const messageMeta = style({display: "flex", marginBottom: 2, flexDirection: "column", alignItems: "flex-start", gap: 1});
export const myMessageMeta = style([messageMeta, {alignItems: "flex-end"}]);
export const unreadCount = style({color: vars.color.brandText, fontFamily: vars.font.semibold, fontSize: 10, lineHeight: "12px", whiteSpace: "nowrap"});
export const time = style({color: vars.color.gray500, fontFamily: vars.font.body, fontSize: 10, lineHeight: "12px", whiteSpace: "nowrap"});
export const empty = style({margin: "140px 0", color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 14, textAlign: "center"});

export const composer = style({display: "flex", margin: "auto -16px 0", padding: "14px 16px", alignItems: "center", gap: 8, borderTop: `1px solid ${vars.color.gray300}`, background: vars.color.background});
export const input = style({
  minHeight: 42,
  maxHeight: 100,
  flex: 1,
  padding: "10px 14px",
  border: 0,
  borderRadius: 999,
  boxSizing: "border-box",
  color: vars.color.black,
  background: vars.color.gray100,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "20px",
  resize: "none",
  selectors: {"&:focus-visible": {outline: "2px solid #78b991", outlineOffset: 2}},
});
export const sendButton = style({
  width: 36,
  minWidth: 36,
  height: 36,
  padding: 0,
  borderColor: vars.color.brandText,
  borderRadius: "50%",
  background: vars.color.brandText,
  color: vars.color.background,
  selectors: {
    "&:hover:not(:disabled)": {background: "#267a56"},
    "&:disabled": {borderColor: vars.color.brandText, background: vars.color.brandText, color: vars.color.background, opacity: 0.45},
  },
});
export const sendIcon = style({fontFamily: vars.font.semibold, fontSize: 16, lineHeight: 1});
export const srOnly = style({position: "absolute", width: 1, height: 1, padding: 0, margin: -1, overflow: "hidden", clip: "rect(0, 0, 0, 0)", whiteSpace: "nowrap", border: 0});
