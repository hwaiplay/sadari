import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  width: "100%",
  minHeight: "100vh",
  padding: "0 0 110px",
  backgroundColor: "#ffffff",
});

export const profileShell = style({
  width: "100%",
  maxWidth: "520px",
  minHeight: "calc(100vh - 110px)",
  margin: "0 auto",
  backgroundColor: "#ffffff",
});

export const cover = style({
  position: "relative",
  height: "260px",
  overflow: "hidden",
  borderRadius: "0 0 22px 22px",
  backgroundColor: "#d9e0e7",
  backgroundSize: "cover",
  backgroundPosition: "center",
});

export const coverActionGroup = style({
  position: "absolute",
  right: "14px",
  bottom: "14px",
  zIndex: 2,
  display: "inline-flex",
  alignItems: "center",
  gap: "8px",
});

export const coverProfileEditButton = style({
  minHeight: "30px",
  padding: "0 10px",
  border: "1px solid rgba(255, 255, 255, 0.72)",
  borderRadius: "999px",
  backgroundColor: "rgba(0, 0, 0, 0.48)",
  color: "#ffffff",
  fontFamily: vars.font.middle,
  fontSize: "12px",
  display: "inline-flex",
  alignItems: "center",
  gap: "5px",
  cursor: "pointer",
});

export const coverImageButton = style([
  coverProfileEditButton,
  {
    backgroundColor: "rgba(255, 255, 255, 0.92)",
    color: vars.color.black,
    borderColor: "rgba(255, 255, 255, 0.92)",
  },
]);

export const coverSaveButton = style([
  coverProfileEditButton,
  {
    backgroundColor: vars.color.black,
    borderColor: vars.color.black,
    color: "#ffffff",
    selectors: {
      "&:disabled": {
        cursor: "default",
        opacity: 0.62,
      },
    },
  },
]);

export const actionIcon = style({
  width: "14px",
  height: "14px",
  fill: "currentColor",
  flexShrink: 0,
});

export const coverEmptyText = style({
  position: "absolute",
  left: "50%",
  top: "50%",
  zIndex: 1,
  margin: 0,
  transform: "translate(-50%, -50%)",
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1.4,
  color: "#7a8490",
  textAlign: "center",
  whiteSpace: "nowrap",
});

export const profileBody = style({
  position: "relative",
  padding: "0 22px 28px",
  display: "flex",
  flexDirection: "column",
  alignItems: "stretch",
});

export const profileHeaderRow = style({
  width: "100%",
  marginTop: "-34px",
  display: "grid",
  gridTemplateColumns: "112px minmax(0, 1fr)",
  alignItems: "start",
  gap: "18px",
});

export const avatarWrap = style({
  position: "relative",
  width: "112px",
  height: "112px",
  margin: 0,
});

export const profileImage = style({
  width: "112px",
  height: "112px",
  borderRadius: "50%",
  objectFit: "cover",
  border: "4px solid #ffffff",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
});

export const avatarCameraButton = style({
  position: "absolute",
  right: "-8px",
  bottom: "8px",
  width: "38px",
  height: "38px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  backgroundColor: "#ffffff",
  color: vars.color.black,
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  boxShadow: "0 6px 16px rgba(0, 0, 0, 0.16)",
});

export const cameraIcon = style({
  width: "19px",
  height: "19px",
  fill: "currentColor",
});

export const hiddenInput = style({
  position: "absolute",
  width: "1px",
  height: "1px",
  padding: 0,
  margin: "-1px",
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  border: 0,
});

export const profileText = style({
  width: "100%",
  minWidth: 0,
  paddingTop: "42px",
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  gap: "8px",
  textAlign: "left",
});

export const profileName = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "22px",
  lineHeight: 1.3,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const profileIntro = style({
  margin: 0,
  maxWidth: "100%",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  color: "#666666",
  wordBreak: "break-word",
});

export const profileNameInput = style({
  width: "100%",
  minWidth: 0,
  height: "40px",
  margin: 0,
  padding: "0 12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: "#f8f9fa",
  color: vars.color.black,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.3,
  outline: "none",
  boxShadow: "inset 0 1px 0 rgba(255, 255, 255, 0.8)",
  selectors: {
    "&:focus": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
  },
});

export const profileIntroInput = style({
  width: "100%",
  minWidth: 0,
  height: "56px",
  padding: "9px 12px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.sm,
  backgroundColor: "#f8f9fa",
  color: "#666666",
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.55,
  outline: "none",
  resize: "none",
  boxShadow: "inset 0 1px 0 rgba(255, 255, 255, 0.8)",
  selectors: {
    "&:focus": {
      borderColor: vars.color.black,
      backgroundColor: "#ffffff",
    },
  },
});
