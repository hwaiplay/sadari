import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: "100svh",
  padding: "48px 20px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  background: "linear-gradient(145deg, #f4f7f5 0%, #ffffff 70%)",
  boxSizing: "border-box",
});

export const panel = style({
  width: "100%",
  maxWidth: "520px",
  padding: "38px 30px 30px",
  border: `1px solid ${vars.color.gray200}`,
  borderRadius: "24px",
  backgroundColor: "#ffffff",
  boxShadow: "0 18px 48px rgba(24, 38, 29, 0.09)",
  textAlign: "center",
  boxSizing: "border-box",
});

export const mark = style({
  width: "52px",
  height: "52px",
  margin: "0 auto 18px",
  display: "grid",
  placeItems: "center",
  borderRadius: "50%",
  backgroundColor: "#f1e8e8",
  color: "#9a4545",
  fontFamily: vars.font.semibold,
  fontSize: "24px",
});

export const eyebrow = style({
  margin: "0 0 8px",
  color: "#69726c",
  fontFamily: vars.font.semibold,
  fontSize: "12px",
});

export const heading = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "24px",
  lineHeight: 1.35,
});

export const description = style({
  margin: "16px auto 24px",
  color: "#666d69",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.45,
  whiteSpace: 'pre-line'
});

export const detailList = style({
  margin: 0,
  padding: "4px 20px",
  borderRadius: "16px",
  backgroundColor: "#f6f8f7",
});

export const detailItem = style({
  padding: "15px 0",
  display: "flex",
  justifyContent: "space-between",
  gap: "16px",
  borderBottom: `1px solid ${vars.color.gray200}`,
  fontSize: "14px",
  lineHeight: 1.5,
  selectors: {
    "&:last-child": {
      borderBottom: 0,
    },
  },
});

export const detailTerm = style({
  flex: "0 0 auto",
  color: "#7b817e",
  fontFamily: vars.font.body,
});

export const detailDescription = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  textAlign: "right",
});

export const note = style({
  margin: "18px 2px 0",
  color: "#858b87",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.6,
  textAlign: "left",
});

export const actions = style({
  marginTop: "26px",
  display: "grid",
  gridTemplateColumns: "1fr",
  gap: "10px",
});

const actionButton = {
  minHeight: "46px",
  borderRadius: "12px",
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  cursor: "pointer",
} as const;

export const logoutButton = style({
  ...actionButton,
  border: `1px solid ${vars.color.black}`,
  backgroundColor: vars.color.black,
  color: "#ffffff",
});
