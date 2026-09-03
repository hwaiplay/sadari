import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  minHeight: "calc(100svh - 120px)",
  margin: "0 auto",
  padding: "24px 18px 96px",
  boxSizing: "border-box",
  backgroundColor: "#ffffff",
});

export const menu = style({
  width: "100%",
  borderTop: `1px solid ${vars.color.gray200}`,
});

export const menuItem = style({
  width: "100%",
});

export const primaryMenuButton = style({
  width: "100%",
  minHeight: "58px",
  padding: "0 4px",
  border: 0,
  borderBottom: `1px solid ${vars.color.gray200}`,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  fontFamily: vars.font.semibold,
  fontSize: "15px",
  textAlign: "left",
  cursor: "pointer",
});

// 하위 메뉴가 펼쳐진 동안 상위 메뉴 아래 선을 숨기고 하위 메뉴 마지막으로 이동시킴
export const primaryMenuButtonOpen = style({
  borderBottomColor: "transparent",
});

export const chevronIcon = style({
  width: "19px",
  height: "19px",
  flex: "0 0 19px",
  fill: "none",
  stroke: "#777777",
  strokeWidth: 1.8,
  strokeLinecap: "round",
  strokeLinejoin: "round",
  transition: "transform 220ms ease",
});

export const chevronIconOpen = style({
  transform: "rotate(90deg)",
});

export const secondaryMenuWrap = style({
  display: "grid",
  gridTemplateRows: "0fr",
  visibility: "hidden",
  opacity: 0,
  transition:
    "grid-template-rows 260ms cubic-bezier(0.4, 0, 0.2, 1), opacity 180ms ease, visibility 260ms",
});

export const secondaryMenuWrapOpen = style({
  gridTemplateRows: "1fr",
  visibility: "visible",
  opacity: 1,
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const secondaryMenuInner = style({
  minHeight: 0,
  overflow: "hidden",
  display: "flex",
  flexDirection: "column",
  backgroundColor: "#ffffff",
});

export const secondaryMenuButton = style({
  width: "100%",
  height: "48px",
  padding: "0 22px",
  border: 0,
  backgroundColor: "transparent",
  color: "#555555",
  fontFamily: vars.font.body,
  fontSize: "14px",
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:hover": {
      backgroundColor: "#f7f8fa",
    },
    "&:focus-visible": {
      outline: "2px solid #8ab4e8",
      outlineOffset: "-2px",
    },
  },
});

export const withdrawMenuButton = style([
  primaryMenuButton,
  {
    marginTop: "24px",
    color: "#c74747",
    borderTop: `1px solid ${vars.color.gray200}`,
  },
]);

export const statusMessage = style({
  margin: 0,
  padding: "24px 4px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: "14px",
  textAlign: "center",
});
