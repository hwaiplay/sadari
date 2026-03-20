import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";

export const loginContainer = style({
  backgroundImage: "url(img/common/background-login.png)",
  backgroundRepeat: "no-repeat",
  backgroundSize: "cover",
  paddingTop: "100px",
  height: "100svh",
});

export const content = style({
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate3D(-50%,-50%,0)",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "45px",
  width: "100%",
});

export const title = style({
  fontSize: "18px",
  fontWeight: 700,
  lineHeight: 1.3,
  whiteSpace: "pre-line",
  textAlign: "center",
});

export const kakaoLoginBtn = style({
  backgroundColor: "#FEE500",
  color: "#000000",
  fontSize: vars.fontSize.body,
  width: "80%",
  height: "40px",
  textDecoration: "none",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: vars.radius.md,
});
