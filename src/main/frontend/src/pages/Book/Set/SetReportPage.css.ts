import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  position: "relative",
  width: "100vw",
  marginLeft: "calc(50% - 50vw)",
  minHeight: "100vh",
  backgroundColor: "#ffffff",
  overflow: "hidden",

  selectors: {
    "&::before": {
      content: "",
      position: "absolute",
      top: "-36px",
      right: "-36px",
      left: "-36px",
      height: "760px",
      zIndex: 0,
      backgroundImage: "var(--book-bg-image)",
      backgroundRepeat: "no-repeat",
      backgroundPosition: "center top",
      backgroundSize: "cover",
      filter: "blur(26px)",
      transform: "scale(1.12)",
      opacity: 0.86,
      pointerEvents: "none",
      maskImage:
        "linear-gradient(180deg, #000 0%, rgba(0, 0, 0, 0.88) 34%, rgba(0, 0, 0, 0.28) 70%, rgba(0, 0, 0, 0) 100%)",
      WebkitMaskImage:
        "linear-gradient(180deg, #000 0%, rgba(0, 0, 0, 0.88) 34%, rgba(0, 0, 0, 0.28) 70%, rgba(0, 0, 0, 0) 100%)",
    },
    "&::after": {
      content: "",
      position: "absolute",
      top: 0,
      right: 0,
      left: 0,
      height: "860px",
      zIndex: 0,
      background:
        "linear-gradient(180deg, rgba(255, 255, 255, 0.30) 0%, rgba(255, 255, 255, 0.18) 42%, rgba(255, 255, 255, 0.82) 78%, #ffffff 100%)",
      pointerEvents: "none",
    },
  },
});

export const form = style({
  position: "relative",
  zIndex: 1,
  maxWidth: "420px",
  width: "100%",
  margin: "0 auto",
  padding: "28px 20px 28px",
  display: "flex",
  flexDirection: "column",
  gap: "28px",
});

export const contentPanel = style({
  display: "flex",
  flexDirection: "column",
  gap: "28px",
  minHeight: "auto",
  padding: "28px 22px 26px",
  borderRadius: "14px",
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  boxShadow: "0 -12px 32px rgba(0, 0, 0, 0.14)",
});

export const saveButton = style({
  width: "100%",
  height: "44px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "14px",
  cursor: "pointer",
});

export const searchBookArea = style({
  minHeight: "360px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: "14px",
  backgroundColor: "rgba(255, 255, 255, 0.94)",
  boxShadow: "0 18px 38px rgba(0, 0, 0, 0.18)",
});

export const fieldStack = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px",
});

export const statusContainer = style({
  display: "flex",
  gap: "10px",
  width: "100%",
});

export const statusOption = style({
  flex: 1,
  minWidth: 0,
});

export const hiddenInput = style({
  position: "absolute",
  opacity: 0,
  pointerEvents: "none",
});

export const statusPill = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  minHeight: "36px",
  padding: "0 12px",
  borderRadius: "999px",
  border: `1px solid ${vars.color.gray400}`,
  backgroundColor: "#f7f7f7",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  whiteSpace: "nowrap",
  cursor: "pointer",

  selectors: {
    [`${hiddenInput}:checked + &`]: {
      backgroundColor: vars.color.black,
      borderColor: vars.color.black,
      color: "#ffffff",
    },
  },
});

export const dateRow = style({
  display: "none",
});

export const inputLabel = style({
  fontFamily: vars.font.body,
  fontSize: "13px",
  color: vars.color.black,
});

export const input = style({
  width: "100%",
  height: "36px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "999px",
  backgroundColor: "#ffffff",
  padding: "0 16px",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
  boxSizing: "border-box",
});

export const starGroup = style({
  display: "flex",
  alignItems: "center",
  gap: "3px",
});

export const starLabel = style({
  color: "#d3d7dc",
  fontSize: "34px",
  lineHeight: 1,
  cursor: "pointer",
});

export const starActive = style({
  color: "#ffd966",
});

export const textAreaWrap = style({
  position: "relative",
});

export const textArea = style({
  width: "100%",
  minHeight: "150px",
  resize: "vertical",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: "16px",
  backgroundColor: "#ffffff",
  padding: "14px 16px",
  fontFamily: vars.font.body,
  fontSize: "14px",
  color: vars.color.black,
  boxSizing: "border-box",
});

export const counter = style({
  position: "absolute",
  top: "-22px",
  right: "4px",
  fontSize: "11px",
  color: vars.color.gray500,
});

export const topBar = style({
  display: "none",
});

export const backButton = style({
  display: "none",
});

export const brand = style({
  display: "none",
});
