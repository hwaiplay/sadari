/** 모임 채팅 화면 스타일 */
import {vars} from "@/app/styles/tokens.css";
import {style} from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  width: "100%",
  maxWidth: 600,
  minHeight: `calc(100dvh - ${vars.headerHeight} - ${vars.navHeight})`,
  margin: "0 auto",
  padding: "20px 0 24px",
  boxSizing: "border-box",
  flexDirection: "column",
  gap: 16,
});

export const header = style({display: "flex", flexDirection: "column", gap: 4});
export const title = style({margin: 0, color: vars.color.black, fontFamily: vars.font.heading, fontSize: 22});
export const description = style({margin: 0, color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 13});

export const messagePanel = style({
  minHeight: 360,
  maxHeight: "calc(100dvh - 300px)",
  flex: 1,
  overflowY: "auto",
  padding: 16,
  borderRadius: 18,
  background: vars.color.gray100,
});

export const messageList = style({display: "flex", flexDirection: "column", gap: 14, margin: 0, padding: 0, listStyle: "none"});
export const messageRow = style({display: "flex", alignItems: "flex-start", gap: 8});
export const myMessageRow = style([messageRow, {justifyContent: "flex-end"}]);
export const avatar = style({width: 34, height: 34, flexShrink: 0, borderRadius: "50%", objectFit: "cover"});
export const messageContent = style({display: "flex", maxWidth: "78%", flexDirection: "column", gap: 4});
export const sender = style({color: vars.color.gray600, fontFamily: vars.font.medium, fontSize: 12});
export const bubble = style({padding: "10px 12px", borderRadius: "4px 14px 14px", background: vars.color.background, color: vars.color.black, fontFamily: vars.font.body, fontSize: 14, lineHeight: "20px", whiteSpace: "pre-wrap", overflowWrap: "anywhere"});
export const myBubble = style([bubble, {borderRadius: "14px 4px 14px 14px", background: vars.color.brandBg}]);
export const time = style({color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 10});
export const empty = style({margin: "140px 0", color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 14, textAlign: "center"});

export const composer = style({display: "flex", alignItems: "flex-end", gap: 8});
export const input = style({
  minHeight: 52,
  flex: 1,
  padding: "12px 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 14,
  boxSizing: "border-box",
  color: vars.color.black,
  background: vars.color.background,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "20px",
  resize: "none",
  selectors: {"&:focus-visible": {outline: "2px solid #78b991", outlineOffset: 2}},
});
export const srOnly = style({position: "absolute", width: 1, height: 1, padding: 0, margin: -1, overflow: "hidden", clip: "rect(0, 0, 0, 0)", whiteSpace: "nowrap", border: 0});
