/**
 * fileName       : ClubManagementPage.css
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임장 전용 관리 메뉴 화면 스타일을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */
import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  width: "100%",
  maxWidth: 600,
  minHeight: `calc(100vh - ${vars.headerHeight} - ${vars.navHeight})`,
  margin: "0 auto",
  paddingTop: 20,
  boxSizing: "border-box",
  background: vars.color.background,
});

export const menuList = style({
  display: "flex",
  flexDirection: "column",
  gap: '4px'
});

export const menuRow = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  width: "100%",
  minHeight: 44,
  padding: "10px 0",
  border: 0,
  boxSizing: "border-box",
  background: vars.color.background,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: "24px",
  textAlign: "left",
  cursor: "pointer",
  selectors: {
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: 2,
    },
  },
});

export const chevronIcon = style({
  width: 18,
  height: 18,
  objectFit: "contain",
});
