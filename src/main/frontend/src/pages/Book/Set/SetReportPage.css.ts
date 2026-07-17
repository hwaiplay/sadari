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
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "7px",
  cursor: "pointer",
});

export const formActions = style({
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "10px",
});

export const deleteButton = style([
  saveButton,
  {
    borderColor: "#d84a3a",
    color: "#d84a3a",
  },
]);

export const buttonIcon = style({
  width: "17px",
  height: "17px",
  flexShrink: 0,
});

export const searchBookArea = style({
  minHeight: "360px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: "14px",
  backgroundColor: "rgb(255, 255, 255)",
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
  flexWrap: "wrap",
  gap: "3px",
  color: "#d3d7dc",
  fontSize: "34px",
  lineHeight: 1,
});

export const starLabel = style({
  minWidth: "auto",
  height: "auto",
  border: 0,
  borderRadius: 0,
  backgroundColor: "transparent",
  color: "inherit",
  fontSize: "inherit",
  lineHeight: 1,
  textAlign: "center",
  cursor: "pointer",
  boxSizing: "border-box",
  transition: "color 0.15s ease",

  selectors: {
    "&:hover": {
      color: "#ffd966",
    },
  },
});

export const starActive = style({
  color: "#ffd966",
});

export const textAreaWrap = style({
  position: "relative",
});

export const textArea = style({
  width: "100%",
  minHeight: "300px",
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

export const publicToggleRow = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "14px",
});

export const publicToggleControl = style({
  display: "inline-flex",
  alignItems: "center",
  flexShrink: 0,
  cursor: "pointer",
});

export const publicToggleText = style({
  display: "flex",
  flexDirection: "column",
  gap: "4px",
  minWidth: 0,
});

export const publicToggleState = style({
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: vars.color.black,
});

export const publicToggleHelp = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.45,
  color: "#777777",
});

export const switchTrack = style({
  display: "inline-flex",
  alignItems: "center",
  position: "relative",
  flexShrink: 0,
  width: "52px",
  height: "30px",
  borderRadius: "999px",
  backgroundColor: vars.color.gray400,
  cursor: "pointer",
  transition: "background-color 0.18s ease",

  selectors: {
    [`${hiddenInput}:checked + &`]: {
      backgroundColor: vars.color.black,
    },
  },
});

export const switchThumb = style({
  position: "absolute",
  top: "3px",
  left: "3px",
  width: "24px",
  height: "24px",
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  boxShadow: "0 2px 6px rgba(0, 0, 0, 0.18)",
  transition: "transform 0.18s ease",

  selectors: {
    [`${hiddenInput}:checked + ${switchTrack} &`]: {
      transform: "translateX(22px)",
    },
  },
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
