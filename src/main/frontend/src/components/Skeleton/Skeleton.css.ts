/**
 * fileName       : Skeleton.css
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 공통 스켈레톤의 배경과 로딩 애니메이션을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang    최초 생성
 */

import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

const shimmer = keyframes({
  "0%": {
    backgroundPosition: "200% 0",
  },
  "100%": {
    backgroundPosition: "-200% 0",
  },
});

export const skeleton = style({
  position: "relative",
  display: "block",
  flexShrink: 0,
  overflow: "hidden",
  boxSizing: "border-box",
  backgroundColor: vars.color.gray100,
  backgroundImage: `linear-gradient(
    90deg,
    ${vars.color.gray200} 0%,
    ${vars.color.gray100} 38%,
    #ffffff 50%,
    ${vars.color.gray100} 62%,
    ${vars.color.gray200} 100%
  )`,
  backgroundSize: "200% 100%",
  animation: `${shimmer} 1.2s linear infinite`,
  "@media": {
    "(prefers-reduced-motion: reduce)": {
      animation: "none",
      backgroundPosition: "50% 0",
    },
  },
});
