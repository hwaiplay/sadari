import { style } from "@vanilla-extract/css";
import { vars } from "../../../app/styles/tokens.css";

export const navContainer = style({
  position: "fixed",
  bottom: 0,
  right: 0,
  left: 0,
  zIndex: 997,
  width: "100%",
  height: vars.headerHeight,
  margin: "0 auto",
  display: "flex",
  alignItems: "center",
  background:'#fff',
padding: '0 50px'
});

export const whiteBg = style({
  backgroundColor: "#ffffff",
});

export const navigation = style({
  position: "relative",
  zIndex: 1,
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  width: "100%",
});

export const navLink = style({
  width: "40px",
  height: "40px",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  borderRadius: vars.radius.xl,
});

export  const navLink__set = style({
  width:'70px',
  height:'70px',
  border: '5px solid #fff',
  backgroundColor: vars.color.gray100,
  marginBottom: '35px'
})

export const navIcon = style({
  width: "24px",
  height: "24px",
  display: "block",
});

export const navProfileButton = style({
  width: "40px",
  height: "40px",
  padding: 0,
  border: 0,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
});

export const navProfileImage = style({
  width: "30px",
  height: "30px",
  borderRadius: "50%",
  display: "block",
  objectFit: "cover",
  border: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
});

export const drawerOverlay = style({
  position: "fixed",
  inset: 0,
  zIndex: 1000,
  pointerEvents: "none",
});

export const drawerOverlayVisible = style({
  pointerEvents: "auto",
});

export const drawerBackdrop = style({
  position: "absolute",
  inset: 0,
  border: 0,
  backgroundColor: "rgba(0, 0, 0, 0)",
  opacity: 0,
  transition: "opacity 180ms ease, background-color 180ms ease",
});

export const drawerBackdropVisible = style({
  backgroundColor: "rgba(0, 0, 0, 0.24)",
  opacity: 1,
});

export const drawer = style({
  position: "absolute",
  top: 0,
  right: 0,
  width: "min(82vw, 320px)",
  height: "100%",
  padding: "28px 20px",
  backgroundColor: "#ffffff",
  // boxShadow: "-18px 0 42px rgba(0, 0, 0, 0.16)",
  transform: "translateX(100%)",
  transition: "transform 220ms ease",
  display: "flex",
  flexDirection: "column",
  gap: "26px",
});

export const drawerOpen = style({
  transform: "translateX(0)",
});

export const drawerHeader = style({
  display: "grid",
  gridTemplateColumns: "58px 1fr",
  alignItems: "center",
  gap: "14px",
  paddingBottom: "20px",
  borderBottom: `1px solid ${vars.color.gray200}`,
});

export const drawerProfileImage = style({
  width: "58px",
  height: "58px",
  borderRadius: "50%",
  objectFit: "cover",
  border: `1px solid ${vars.color.gray300}`,
  backgroundColor: "#ffffff",
});

export const drawerProfileMeta = style({
  minWidth: 0,
  display: "flex",
  flexDirection: "column",
  gap: "5px",
});

export const drawerProfileName = style({
  fontFamily: vars.font.heading,
  fontSize: "17px",
  lineHeight: 1.3,
  color: vars.color.black,
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

export const drawerProfileSub = style({
  fontFamily: vars.font.body,
  fontSize: "12px",
  color: "#777777",
});

export const drawerMenu = style({
  display: "flex",
  flexDirection: "column",
  gap: "10px",
});

export const drawerMenuButton = style({
  width: "100%",
  height: "46px",
  padding: "0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: vars.radius.md,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "14px",
  textAlign: "left",
  cursor: "pointer",
});

export const drawerMenuDisabled = style({
  color: vars.color.gray500,
  backgroundColor: "#f7f7f7",
  cursor: "default",
});
