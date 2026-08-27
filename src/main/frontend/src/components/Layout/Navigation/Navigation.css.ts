import {globalStyle, style} from "@vanilla-extract/css";
import {vars} from "@/app/styles/tokens.css";

export const navContainer = style({
    position: "fixed",
    bottom: 0,
    right: "var(--sadari-scrollbar-compensation, 0px)",
    left: 0,
    zIndex: 997,
    width: "auto",
    height: `calc(${vars.headerHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)))`,
    margin: "0 auto",
    display: "flex",
    alignItems: "center",
    background: '#fff',
    padding: `6px clamp(16px, 16px, 50px) max(${vars.space.sm}, env(safe-area-inset-bottom, 0px))`,
    // padding: `6px clamp(16px, 8vw, 50px) max(${vars.space.sm}, env(safe-area-inset-bottom, 0px))`,
    boxShadow: "rgb(0 0 0 / 10%) 0px -6px 27px 0px",
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
    // width: "min(100%, clamp(320px, 56vw, 600px))",
    width: "100%",
    height: "100%",
    margin: "0 auto",
    maxWidth: "600px"
});

export const navLink = style({
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    gap: "2px",
    padding: 0,
    border: 0,
    textDecoration: "none",
    background: "transparent",
    cursor: "pointer",
    minWidth: 52
});

export const navIconWrap = style({
    position: "relative",
    display: "inline-flex",
    width: "28px",
    height: "28px",
    flexShrink: 0,
});

export const timerRunningBadge = style({
    position: "absolute",
    top: "-1px",
    right: "-1px",
    width: "10px",
    height: "10px",
    borderRadius: "999px",
    backgroundColor: "#ef4444",
    boxSizing: "border-box",
    pointerEvents: "none",
});

export const navLinkText = style({
    fontSize: "12px",
    color: vars.color.gray500
})

export const navProfileImage = style({
    width: "30px",
    height: "30px",
    borderRadius: "50%",
    display: "block",
    objectFit: "cover",
    border: `1px solid ${vars.color.gray300}`,
    backgroundColor: "#ffffff",
});

export const navLinkActive = style({});

globalStyle(`${navLinkActive} svg path`, {
    fill: vars.color.gray900,
});

globalStyle(`${navLinkActive} ${navLinkText}`, {
    color: vars.color.gray900,
});

globalStyle(`${navLinkActive} ${navProfileImage}`, {
    borderColor: vars.color.gray900,
});

export const drawerOverlay = style({
    position: "fixed",
    inset: 0,
    zIndex: 1000,
    pointerEvents: "none",

    maxWidth: 600,
    margin: "0 auto",
    overflow: "hidden"
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
    padding: "28px 20px 0 20px",
    backgroundColor: "#ffffff",
    boxSizing: "border-box",
    transform: "translateX(100%)",
    transition: "transform 220ms ease",
    display: "flex",
    flexDirection: "column",
    gap: "26px",
});

export const drawerOpen = style({
    transform: "translateX(0)",
    boxShadow: '-13px 0 32px rgba(0, 0, 0, 0.1)',

});

export const drawerCloseButton = style({
    position: "absolute",
    top: "12px",
    right: "12px",
    zIndex: 2,
    width: "40px",
    height: "40px",
    padding: 0,
    border: 0,
    borderRadius: "50%",
    backgroundColor: "transparent",
    color: "#555555",
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    cursor: "pointer",
    selectors: {
        "&:hover": {
            backgroundColor: vars.color.gray100,
        },
        "&:focus-visible": {
            outline: "2px solid #78b991",
            outlineOffset: "2px",
        },
    },
});

export const drawerCloseIcon = style({
    width: "22px",
    height: "22px",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round",
    strokeLinejoin: "round",
});

export const drawerProfileSummaryButton = style({
    gridColumn: "1 / -1",
    display: "grid",
    gridTemplateColumns: "58px minmax(0, 1fr)",
    alignItems: "center",
    gap: "14px",
    width: "100%",
    padding: "0 36px 0 0",
    border: 0,
    backgroundColor: "transparent",
    boxSizing: "border-box",
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
    alignItems: "center",
    justifyContent: "space-between",
});

export const drawerSettingButton = style({
    width: "30px",
    height: "30px",
    padding: 0,
    border: `1px solid ${vars.color.gray300}`,
    borderRadius: "50%",
    backgroundColor: "#ffffff",
    color: "#555555",
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    cursor: "pointer",
});

export const drawerSettingIcon = style({
    width: "16px",
    height: "16px",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round",
    strokeLinejoin: "round",
});

export const drawerLogoutButton = style({
    minWidth: "64px",
    height: "30px",
    padding: "0 12px",
    border: "1px solid #d84a4a",
    borderRadius: "999px",
    backgroundColor: "#ffffff",
    color: "#d84a4a",
    fontFamily: vars.font.semibold,
    fontSize: "11px",
    cursor: "pointer",
    whiteSpace: "nowrap",
});

export const drawerMenu = style({
    display: "flex",
    flexDirection: "column",
    gap: 0,
});

export const drawerMenuGroup = style({
    width: "100%",
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
    fontFamily: vars.font.semibold,
    fontSize: "14px",
    textAlign: "left",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
});

// 2뎁스가 펼쳐진 동안 1뎁스 하단 선을 숨겨 구분선이 하위 메뉴 마지막으로 이동하게 한다
export const drawerMenuButtonOpen = style({
    borderBottomColor: "transparent",
});

export const drawerMenuChevron = style({
    width: "18px",
    height: "18px",
    flex: "0 0 18px",
    fill: "none",
    stroke: "#777777",
    strokeWidth: 1.8,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    transition: "transform 220ms ease",
});

export const drawerMenuChevronOpen = style({
    transform: "rotate(90deg)",
});

export const drawerSecondaryMenuWrap = style({
    display: "grid",
    gridTemplateRows: "0fr",
    visibility: "hidden",
    opacity: 0,
    transition:
        "grid-template-rows 260ms cubic-bezier(0.4, 0, 0.2, 1), opacity 180ms ease, visibility 260ms",
});

export const drawerSecondaryMenuWrapOpen = style({
    gridTemplateRows: "1fr",
    visibility: "visible",
    opacity: 1,
    borderBottom: `1px solid ${vars.color.gray200}`,
});

export const drawerSecondaryMenuInner = style({
    minHeight: 0,
    overflow: "hidden",
    display: "flex",
    flexDirection: "column",
    backgroundColor: "#ffffff",
});

export const drawerSecondaryMenuButton = style({
    width: "100%",
    height: "42px",
    padding: "0 20px",
    border: 0,
    backgroundColor: "transparent",
    color: vars.color.gray600,
    fontFamily: vars.font.body,
    fontSize: "14px",
    textAlign: "left",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    selectors: {
        "&:hover": {
            backgroundColor: "#f7f8fa",
        },
        "&:focus-visible": {
            outline: "2px solid #8ab4e8",
            outlineOffset: "-2px",
        },
    },
});

export const drawerTertiaryMenuButton = style({
    width: "100%",
    height: "40px",
    padding: "0 20px 0 38px",
    border: 0,
    backgroundColor: "transparent",
    color: "#666666",
    fontFamily: vars.font.body,
    fontSize: "12px",
    textAlign: "left",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    selectors: {
        "&:hover": {
            backgroundColor: "#f7f8fa",
        },
        "&:focus-visible": {
            outline: "2px solid #8ab4e8",
            outlineOffset: "-2px",
        },
    },
});

export const drawerMenuDisabled = style({
    color: vars.color.gray500,
    backgroundColor: "transparent",
    cursor: "default",
});
