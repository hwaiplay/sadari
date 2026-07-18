import {style} from "@vanilla-extract/css";
import {vars} from "../../../app/styles/tokens.css";

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
    background: '#fff',
    padding: '0 clamp(16px, 8vw, 50px)',
    boxShadow: "rgba(100, 100, 111, 0.2) 1px 9px 29px 0px",
    boxSizing: "border-box",
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
    width: "min(100%, clamp(320px, 56vw, 520px))",
    margin: "0 auto",
});

export const navLink = style({
    width: "40px",
    height: "40px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    borderRadius: vars.radius.xl,
});

export const navLink__set = style({
    width: '70px',
    height: '70px',
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
    zIndex: 0,
    border: 0,
    backgroundColor: "rgba(0, 0, 0, 0)",
    opacity: 0,
    transition: "opacity 180ms ease, background-color 180ms ease",
});

export const drawerBackdropVisible = style({
    backgroundColor: "rgba(0, 0, 0, 0)",
    opacity: 1,
});

export const drawer = style({
    position: "absolute",
    top: 0,
    right: 0,
    zIndex: 1,
    width: "min(82vw, 320px)",
    height: "100%",
    padding: "28px 20px",
    border: "1px solid #cfd4d9",
    borderRight: 0,
    backgroundColor: "#ffffff",
    boxShadow: "none",
    boxSizing: "border-box",
    transform: "translateX(100%)",
    transition: "transform 220ms ease",
    display: "flex",
    flexDirection: "column",
    gap: "26px",
});

export const drawerOpen = style({
    transform: "translateX(0)",
});

export const drawerProfileSummaryButton = style({
    gridColumn: "1 / -1",
    display: "grid",
    gridTemplateColumns: "58px minmax(0, 1fr)",
    alignItems: "center",
    gap: "14px",
    width: "100%",
    padding: 0,
    border: 0,
    backgroundColor: "transparent",
    textAlign: "left",
    cursor: "pointer",
});

export const drawerHeader = style({
    display: "grid",
    gridTemplateColumns: "58px minmax(0, 1fr)",
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
    lineHeight: 1.45,
    color: "#777777",
    display: "-webkit-box",
    overflow: "hidden",
    textOverflow: "ellipsis",
    WebkitLineClamp: 2,
    WebkitBoxOrient: "vertical",
});

export const drawerActionGroup = style({
    gridColumn: "1 / -1",
    display: "flex",
    justifyContent: "flex-end",
});

export const drawerLogoutButton = style({
    minWidth: "64px",
    height: "30px",
    padding: "0 12px",
    border: "1px solid #d84a4a",
    borderRadius: "999px",
    backgroundColor: "#ffffff",
    color: "#d84a4a",
    fontFamily: vars.font.middle,
    fontSize: "11px",
    cursor: "pointer",
    whiteSpace: "nowrap",
});

export const drawerMenu = style({
    display: "flex",
    flexDirection: "column",
    gap: 0,
});

export const drawerMenuButton = style({
    width: "100%",
    height: "46px",
    padding: "0 2px",
    border: 0,
    borderBottom: `1px solid ${vars.color.gray200}`,
    borderRadius: 0,
    backgroundColor: "transparent",
    color: vars.color.black,
    fontFamily: vars.font.middle,
    fontSize: "14px",
    textAlign: "left",
    cursor: "pointer",
});

export const drawerMenuDisabled = style({
    color: vars.color.gray500,
    backgroundColor: "transparent",
    cursor: "default",
});
