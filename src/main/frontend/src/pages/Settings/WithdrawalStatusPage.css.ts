import { style } from "@vanilla-extract/css";

export const page = style({
  minHeight: "100svh",
  display: "grid",
  placeItems: "center",
  padding: "24px",
  background: "#f5f7fa",
  boxSizing: "border-box",
});

export const panel = style({
  width: "100%",
  maxWidth: "410px",
  padding: "34px 26px",
  border: "1px solid #e2e6eb",
  borderRadius: "8px",
  background: "#ffffff",
  textAlign: "center",
  boxSizing: "border-box",
});

export const heading = style({
  margin: "20px 0 10px",
  fontSize: "21px",
  letterSpacing: 0,
});

export const description = style({
  display: "grid",
  gap: "8px",
  margin: 0,
  color: "#69717d",
  fontSize: "14px",
  lineHeight: 1.6,
});

export const date = style({
  color: "#25282d",
  fontSize: "16px",
});

const markBase = {
  width: "58px",
  height: "58px",
  margin: "0 auto",
  display: "grid",
  placeItems: "center",
  borderRadius: "50%",
  fontSize: "20px",
  fontWeight: 800,
} as const;

export const successMark = style({
  ...markBase,
  background: "#e6f5eb",
  color: "#2e8a4c",
});

export const failMark = style({
  ...markBase,
  background: "#fdecec",
  color: "#c74747",
});

export const pendingMark = style({
  ...markBase,
  background: "#edf3fb",
  color: "#4877ad",
});

export const primaryLink = style({
  width: "100%",
  minHeight: "46px",
  marginTop: "24px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: "6px",
  background: "#3274b9",
  color: "#ffffff",
  fontSize: "15px",
  fontWeight: 700,
  textDecoration: "none",
  boxSizing: "border-box",
});

export const cancelButton = style({
  width: "100%",
  minHeight: "46px",
  marginTop: "24px",
  border: "1px solid #3274b9",
  borderRadius: "6px",
  background: "#ffffff",
  color: "#3274b9",
  fontSize: "15px",
  fontWeight: 700,
  cursor: "pointer",
});
