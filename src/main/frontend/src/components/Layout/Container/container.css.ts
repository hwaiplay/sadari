import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";
import { media } from "../../../app/styles/responsive.css";

export const container = style({
  maxWidth: "600px",
  width: "100%",
  margin: "0 auto",
  padding: `0 ${vars.space.md}`,

  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg}`,
    },
  },
});
