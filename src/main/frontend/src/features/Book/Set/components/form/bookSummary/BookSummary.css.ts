import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const coverArea = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "14px",
  padding: "48px 24px 42px",
  borderRadius: "14px",
  backgroundColor: "rgba(255, 255, 255, 0.94)",
  boxShadow: "0 18px 38px rgba(0, 0, 0, 0.18)",
});

export const coverFrame = style({
  width: "126px",
  aspectRatio: "2 / 3",
  borderRadius: "6px",
  overflow: "hidden",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const bookMeta = style({
  width: "100%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "5px",
  textAlign: "center",
});

export const bookTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
  color: vars.color.black,
  wordBreak: "keep-all",
});

export const bookSubInfo = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.45,
  color: "#666666",
});

export const buttonGroup = style({
  display: "flex",
  flexWrap: "wrap",
  justifyContent: "center",
  gap: "8px",
});

export const bookInfoButton = style({
  height: "34px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const changeButton = style([
  bookInfoButton,
  {
    borderColor: "rgba(21, 21, 21, 0.22)",
  },
]);
