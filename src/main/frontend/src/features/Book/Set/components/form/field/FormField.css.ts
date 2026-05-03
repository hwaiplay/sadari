/**
 * fileName       : FormField.css
 * author         : Hanwon.Jang
 * date           : 2026-03-22
 * description    : 기록하기 폼 각 필드 CSS
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22       Hanwon.Jang       최초 생성
 */

import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const fieldTitle = style({
  fontFamily: vars.font.heading,
  fontSize: "16px",
});
