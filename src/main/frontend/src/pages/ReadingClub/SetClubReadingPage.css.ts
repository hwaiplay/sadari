/**
 * fileName       : SetClubReadingPage.css
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임 독서 등록 화면의 목표 기간과 빈 상태 배치를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 * 2026-08-20        Hanwon.Jang        도서 변경 제한 안내 추가
 */

import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const formTop = style({
  paddingTop: "20px",
});

export const emptyState = style({
  minHeight: "300px",
  padding: "24px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "22px",
  backgroundColor: "#ffffff",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "16px",
  boxSizing: "border-box",
});

export const emptyText = style({
  margin: 0,
  color: vars.color.gray600,
  fontSize: "14px",
  lineHeight: 1.5,
  textAlign: "center",
});

export const bookChangeNotice = style({
  margin: "0 0 16px",
  padding: "12px 14px",
  borderRadius: "12px",
  backgroundColor: vars.color.gray100,
  color: vars.color.negativeText,
  fontSize: "14px",
  lineHeight: 1.5,
});
