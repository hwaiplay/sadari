import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

export const header = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: "100%",
  height: vars.headerHeight,
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  zIndex: 997,
  background:
    "linear-gradient(180deg, #ffffff 0%, rgba(255, 255, 255, 0.98) 60%, rgba(255, 255, 255, 0.86) 84%, rgba(255, 255, 255, 0) 100%)",

  selectors: {
    "&::after": {
      content: "",
      position: "absolute",
      top: "74%",
      left: 0,
      right: 0,
      height: "42px",
      pointerEvents: "none",
      background:
        "linear-gradient(180deg, rgba(255, 255, 255, 0.58) 0%, rgba(255, 255, 255, 0.28) 52%, rgba(255, 255, 255, 0) 100%)",
      backdropFilter: "blur(8px)",
      WebkitBackdropFilter: "blur(8px)",
      maskImage:
        "linear-gradient(180deg, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.48) 58%, rgba(0, 0, 0, 0) 100%)",
      WebkitMaskImage:
        "linear-gradient(180deg, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.48) 58%, rgba(0, 0, 0, 0) 100%)",
    },
    "&._sub": {
      padding: "0 8px 0 5px",
    },
  },
});

export const logo = style({
  margin: "0 auto",
  display: "inline-block",
  position: "relative",
  zIndex: 1,
});

export const backpageBtn = style({
  position: "absolute",
  left: "5px",
  top: "50%",
  transform: "translateY(-50%)",
  width: "40px",
  height: "40px",
  border: 0,
  backgroundColor: "transparent",
  cursor: "pointer",
  zIndex: 1,
});
