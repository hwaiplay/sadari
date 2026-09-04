// src/components/common/Loading.css.ts
import { style, keyframes } from "@vanilla-extract/css";
import {vars} from "@/app/styles/tokens.css.ts";

const spin = keyframes({
  "0%": {
    transform: "rotate(0deg)",
  },
  "100%": {
    transform: "rotate(360deg)",
  },
});

export const container = style({
  height: "100svh",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

export const inlineContainer = style({
  minHeight: "240px",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

export const compactContainer = style({
  width: "100%",
  minHeight: "82px",
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
});

export const inlineCompactContainer = style({
  width: "18px",
  height: "18px",
  display: "inline-flex",
  flexShrink: 0,
  alignItems: "center",
  justifyContent: "center",
});

// 기존 회전 링의 모양과 애니메이션을 유지하면서 모달 안에서만 작게 표시함
export const compactSpinner = style({
  width: "48px",
  height: "48px",
  transform: "scale(0.55)",
});

export const spinner = style({
  width: "48px",
  height: "48px",
  borderRadius: "50%",
  border: "5px solid rgba(0,0,0,0.1)",
  borderTop: `5px solid ${vars.color.brand}`, // 포인트 컬러
  animation: `${spin} 0.8s linear infinite`,
});

export const inlineSpinner = style({
  width: "14px",
  height: "14px",
  boxSizing: "border-box",
  borderRadius: "50%",
  border: "2px solid rgba(0,0,0,0.1)",
  borderTop: `2px solid ${vars.color.brandText}`,
  animation: `${spin} 0.8s linear infinite`,
});

export const text = style({
  marginTop: "12px",
  fontSize: "14px",
  color: "#555",
});
