/**
 * fileName       : responsive.css.ts
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    : 반응형 media query 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */

const breakpoints = {
  mobile: 277,
  tablet: 768,
  desktop: 1024,
};

export const media = {
  tablet: `screen and (min-width: ${breakpoints.tablet}px)`,
  desktop: `screen and (min-width: ${breakpoints.desktop}px)`,
};
