import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

// 검색 결과에서 현재 검색어와 일치하는 글자에 공통 브랜드 연두색을 적용한다
export const match = style({
  color: vars.color.brand,
  backgroundColor: "transparent",
  font: "inherit",
});
