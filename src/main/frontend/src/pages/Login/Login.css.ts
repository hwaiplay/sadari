import { keyframes, style } from "@vanilla-extract/css";
import { vars } from "../../app/styles/tokens.css";

const rollUp = keyframes({
  from: {
    transform: "translate3d(0, 0, 0)",
  },
  to: {
    transform: "translate3d(0, -50%, 0)",
  },
});

const rollDown = keyframes({
  from: {
    transform: "translate3d(0, -50%, 0)",
  },
  to: {
    transform: "translate3d(0, 0, 0)",
  },
});

export const loginContainer = style({
  position: "relative",
  maxWidth: "600px",
  width: "100%",
  height: "100svh",
  margin: "0 auto",
  overflow: "hidden"
});

export const background  = style({
  position: "absolute",
  inset: 0,
})

export const background_img_container  = style({
  display: "flex",
  gap: "18px",
  height: "100%",
  paddingInline: "16px",
  overflow: "hidden",
  boxSizing: "border-box",
})

export const background_img_column = style({
  flex: "1 1 0",
  minWidth: 0,
  height: "100%",
  overflow: "hidden",
});

const background_img_track = style({
  display: "flex",
  flexDirection: "column",
  width: "100%",
  willChange: "transform",
});

export const background_img_track_up = style([
  background_img_track,
  {
    animation: `${rollUp} 55s linear infinite`,

    "@media": {
      "(prefers-reduced-motion: reduce)": {
        animation: "none",
      },
    },
  },
]);

export const background_img_track_down = style([
  background_img_track,
  {
    transform: "translate3d(0, -12.5%, 0)",
    animation: `${rollDown} 55s linear infinite`,
    animationDelay: "-41.25s",

    "@media": {
      "(prefers-reduced-motion: reduce)": {
        animation: "none",
      },
    },
  },
]);

export const background_img_track_up_delayed = style([
  background_img_track_up,
  {
    transform: "translate3d(0, -25%, 0)",
    animationDelay: "-27.5s",
  },
]);

export const background_img = style({
  display: "block",
  width: "100%",
  height: "auto",
  flexShrink: 0,
});

export const background_img_overlay = style({
  position: "absolute",
  top: '0',
  left:"0",
  background: "linear-gradient(0deg, rgba(194, 194, 194, 0.8) 0%, rgba(219, 219, 219, 0.6) 71%, rgba(255, 255, 255, 0) 99%)",
  backdropFilter: "blur(6px)",
  width: "100%",
  height: "100%",
  zIndex: "1"
})


export const content = style({
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate3D(-50%,-50%,0)",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: "24px",
  width: "100%",
  zIndex:"2"
});

export const title = style({
  fontSize: "18px",
  fontFamily: vars.font.heading,
  lineHeight: 1.3,
  whiteSpace: "pre-line",
  textAlign: "center",
});

export const loginActions = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  gap: "8px",
});

export const kakaoLoginBtn = style({
  backgroundColor: "#FEE500",
  color: "#000000",
  fontSize: vars.fontSize.body,
  fontFamily: vars.font.semibold,
  width: "300px",
  height: "40px",
  textDecoration: "none",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: vars.radius.sm,
  overflow: "hidden",
  marginTop:"16px"
});

export const privacyPolicyLink = style({
  padding: "8px 4px",
  border: 0,
  backgroundColor: "transparent",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: "14px",
  lineHeight: 1.5,
  textDecoration: "none",
  display: "inline-flex",
  alignItems: "center",
  gap: "2px",
  selectors: {
    "&:hover": {
      color: "#555555",
    },
    "&:focus-visible": {
      outline: "2px solid #78b991",
      outlineOffset: "2px",
    },
  },
});
