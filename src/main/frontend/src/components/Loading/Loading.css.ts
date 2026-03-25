// src/components/common/Loading.css.ts
import { style, keyframes } from "@vanilla-extract/css";

const spin = keyframes({
  "0%": {
    transform: "rotate(0deg)",
  },
  "100%": {
    transform: "rotate(360deg)",
  },
});

export const container = style({
  height: "100vh",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

export const spinner = style({
  width: "48px",
  height: "48px",
  borderRadius: "50%",
  border: "5px solid rgba(0,0,0,0.1)",
  borderTop: "5px solid #4f46e5", // 포인트 컬러
  animation: `${spin} 0.8s linear infinite`,
});

export const text = style({
  marginTop: "12px",
  fontSize: "14px",
  color: "#555",
});
