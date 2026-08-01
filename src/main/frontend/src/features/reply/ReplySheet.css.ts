import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css.ts";

const sheetEnter = keyframes({
  from: {
    transform: "translateY(100%)",
  },
  to: {
    transform: "translateY(0)",
  },
});

export const sheetLayer = style({
  position: "fixed",
  inset: 0,
  zIndex: 1200,
  display: "flex",
  justifyContent: "center",
  alignItems: "flex-end",
});

export const sheetBackdrop = style({
  position: "absolute",
  inset: 0,
  width: "100%",
  height: "100%",
  border: 0,
  backgroundColor: "rgba(0, 0, 0, 0.48)",
  cursor: "default",
});

export const commentSheet = style({
  position: "relative",
  zIndex: 1,
  width: "min(100%, 600px)",
  height: "85%",
  borderRadius: "22px 22px 0 0",
  backgroundColor: "#ffffff",
  display: "flex",
  flexDirection: "column",
  overflow: "hidden",
  willChange: "transform",
  animation: `${sheetEnter} 240ms cubic-bezier(0.22, 1, 0.36, 1)`,
});

export const sheetHandle = style({
  position: "relative",
  width: "100%",
  height: "30px",
  flexShrink: 0,
  cursor: "grab",
  touchAction: "none",
  userSelect: "none",
  selectors: {
    "&::after": {
      content: "",
      position: "absolute",
      top: "12px",
      left: "50%",
      width: "40px",
      height: "4px",
      borderRadius: "999px",
      backgroundColor: vars.color.gray300,
      transform: "translateX(-50%)",
    },
    "&:active": {
      cursor: "grabbing",
    },
    "&:focus-visible": {
      outline: `2px solid ${vars.color.gray500}`,
      outlineOffset: "-2px",
    },
  },
});

export const commentSheetBody = style({
  minHeight: 0,
  flex: 1,
  padding: "10px 20px 24px",
  display: "flex",
  flexDirection: "column",
  overflowY: "auto",
});

export const commentEmpty = style({
  flex: 1,
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
});

export const commentEmptyIcon = style({
  width: "42px",
  height: "42px",
  marginBottom: "12px",
  opacity: 0.72,
});

export const commentEmptyTitle = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
});

export const commentEmptyText = style({
  maxWidth: "250px",
  marginTop: "6px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.55,
  textAlign: "center",
  wordBreak: "keep-all",
});

export const replyList = style({
  width: "100%",
  margin: 0,
  padding: 0,
  display: "flex",
  flexDirection: "column",
  gap: "16px",
  listStyle: "none",
});

export const replyThread = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const replyItem = style({
  position: "relative",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "12px",
  minHeight: "92px",
  backgroundColor: "#ffffff",
});

export const childReplyItem = style([
  replyItem,
  {
    width: "calc(100% - 44px)",
    minHeight: "56px",
    marginLeft: "44px",
    alignItems: "flex-start"
  },
]);

export const childReplyList = style({
  display: "flex",
  flexDirection: "column",
  gap: "8px",
});

export const replyItemWrap = style({
  display: "flex",
  gap: "12px",
})

export const replyWriterProfileImgArea = style({
  display: "inline-flex",
});

export const replyProfileLink = style({
  display: "inline-flex",
  flexShrink: 0,
  borderRadius: "50%",
  selectors: {
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
});

export const replyBody = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
});

export const replyTextArea = style({
  display: "flex",
  flexDirection: "column",
  gap: "2px",
});

export const replyWriterRow = style({
  display: "flex",
  alignItems: "center",
  gap: "6px",
});

export const replyProfileImage = style({
  width: "32px",
  height: "32px",
  flexShrink: 0,
  borderRadius: "50%",
  objectFit: "cover",
  backgroundColor: vars.color.gray300,
});

export const replyWriter = style({
  display: "inline-block",
  minWidth: 0,
  maxWidth: "190px",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "16px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
  textDecoration: "none",
  selectors: {
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "2px",
    },
  },
});


export const replyContent = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "16px",
  lineHeight: "1.4",
  letterSpacing: "-1%",
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const replyMentionLink = style({
  color: vars.color.brand,
  textDecoration: "none",
  selectors: {
    "&:focus-visible": {
      outline: `2px solid ${vars.color.brand}`,
      outlineOffset: "1px",
    },
  },
});

export const replyItemMetrics = style({
  width: "100%",
  minHeight: "24px",
  display: "flex",
  flexDirection: "column",
  gap: "6px",
  marginTop: "6px",
});

export const replyDate = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1,
});

export const replyMetricButton = style({
  padding: 0,
  display: "inline-flex",
  alignItems: "center",
  gap: "4px",
  backgroundColor: "transparent",
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: "14px",
  cursor: "pointer",
});

export const replyMoreButton = style([
  replyMetricButton,
  {
    selectors: {
      "&::before":
          {
            content: "''",
            height: "1px",
            width: "30px",
            backgroundColor: vars.color.gray400
          }
    }
  },
]);

export const replyLikeButton = style([
  replyMetricButton,
  {
    color: "#ff747c",
    flexDirection: "column",
  }
])

export const replyItemActions = style({
  flexShrink: 0,
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "6px",
});

export const actionMenuRoot = style({
  position: "relative",
  display: "inline-flex",
});

export const actionMenuTrigger = style({
  width: "24px",
  height: "24px",
  padding: 0,
  border: 0,
  borderRadius: "50%",
  backgroundColor: "transparent",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  selectors: {
    "&:hover, &:focus-visible": {
      backgroundColor: vars.color.gray100,
      outline: "none",
    },
  },
});

export const actionMenuIcon = style({
  width: "20px",
  height: "20px",
  display: "block",
});

export const actionMenu = style({
  position: "absolute",
  top: "calc(100% + 4px)",
  right: 0,
  zIndex: 30,
  minWidth: "112px",
  padding: "5px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "16px",
  backgroundColor: "#ffffff",
  boxShadow: "0 8px 24px rgba(0, 0, 0, 0.12)",
  display: "flex",
  flexDirection: "column",
  gap: "2px",
});

export const actionMenuOption = style({
  width: "100%",
  minHeight: "34px",
  padding: "0 10px",
  border: 0,
  borderRadius: "10px",
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "14px",
  textAlign: "left",
  whiteSpace: "nowrap",
  cursor: "pointer",
  selectors: {
    "&:hover, &:focus-visible": {
      backgroundColor: vars.color.gray100,
      color: vars.color.black,
      outline: "none",
    },
  },
});

export const commentForm = style({
  flexShrink: 0,
  padding: "10px 16px calc(10px + env(safe-area-inset-bottom))",
  borderTop: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
  display: "flex",
  alignItems: "center",
  gap: "8px",
});

export const commentInput = style({
  minWidth: 0,
  height: "40px",
  flex: 1,
  padding: "0 14px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "21px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: "16px",
  outline: "none",
  selectors: {
    "&::placeholder": {
      color: vars.color.gray500,
    },
    "&:focus": {
      borderColor: vars.color.gray600,
    },
  },
});

export const commentSubmitButton = style({
  flexShrink: 0,
  height: "40px",
  padding: "0 24px",
  border: 0,
  borderRadius: "21px",
  backgroundColor: vars.color.black,
  color: "#ffffff",
  fontFamily: vars.font.medium,
  fontSize: "16px",
  cursor: "pointer",
  selectors: {
    "&:disabled": {
      backgroundColor: vars.color.gray400,
      cursor: "default",
    },
  },
});
