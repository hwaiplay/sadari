/**
 * fileName       : Add.css
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 기록하기 페이지 CSS
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";

export const searchBtn = style({
  width: "112px",
  height: "166px",
  borderRadius: "6px",
  backgroundColor: "#ffffff",
  border: `1px solid ${vars.color.gray500}`,
});

export const searchBtnText = style({
  color: vars.color.gray500,
  fontSize: vars.fontSize.body,
  marginTop: "19px",
});

export const statusContainer = style({
  display: "flex",
  gap: "10px",
});
