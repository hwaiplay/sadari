import { createGlobalTheme } from "@vanilla-extract/css";

export const vars = createGlobalTheme(":root", {
  color: {
    black: "#151515",
    background: "#f0f0f0",
  },

  space: {
    xs: "4px",
    sm: "8px",
    md: "16px",
    lg: "24px",
    xl: "32px",
  },

  fontSize: {
    caption: "12px",
    body: "16px",
    title: "24px",
    hero: "32px",
  },

  radius: {
    sm: "10px",
    md: "20px",
    xl: "55555px",
  },
});
