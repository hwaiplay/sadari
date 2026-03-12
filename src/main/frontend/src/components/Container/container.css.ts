import { style } from "@vanilla-extract/css";
import { vars } from "../../styles/tokens.css";
import { media } from "../../styles/responsive.css";

export const container = style({
  width: "100%",
  margin: "0 auto",
  padding: `0 ${vars.space.md}`,

  "@media": {
    [media.tablet]: {
      padding: `0 ${vars.space.lg}`,
    },
  },
});
