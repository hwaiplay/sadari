import { style } from "@vanilla-extract/css";
import { HEADER_BACKGROUND_COLOR } from "@/components/Layout/Header/headerStyle";

export const sentinel = style({
  display: "block",
  width: "100%",
  height: "1px",
  marginBottom: "-1px",
  pointerEvents: "none",
});

export const surface = style({
  backgroundColor: "transparent",
  isolation: "isolate",

  selectors: {
    "&::before": {
      position: "absolute",
      top: "-2px",
      bottom: 0,
      left: "50%",
      zIndex: -1,
      width: "100vw",
      backgroundColor: HEADER_BACKGROUND_COLOR,
      boxShadow: "none",
      content: '""',
      pointerEvents: "none",
      transform: "translateX(-50%)",
      transition: "box-shadow 140ms ease",
    },
  },
});

export const stuck = style({
  selectors: {
    "&::before": {
      boxShadow: "0 6px 14px rgba(0, 0, 0, 0.1)",
    },
  },
});
