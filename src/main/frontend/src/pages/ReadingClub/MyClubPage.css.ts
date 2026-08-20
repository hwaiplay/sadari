import {vars} from "@/app/styles/tokens.css";
import {destructiveButton} from "@/app/styles/controls.css";
import {style} from "@vanilla-extract/css";

export const page = style({
  position: "relative",
  width: "100%",
  minHeight: `calc(100dvh - ${vars.headerHeight} - ${vars.navHeight} - 16px)`,
  padding: "18px 0 36px",
  boxSizing: "border-box",
});

export const searchTrigger = style({
  width: "100%",
  height: 42,
  padding: "0 16px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 200,
  background: vars.color.background,
  color: vars.color.gray600,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  fontFamily: vars.font.body,
  fontSize: 16,
  letterSpacing: "-0.16px",
  textDecoration: "none",
  cursor: "pointer",
  selectors: {
    "&:hover": {borderColor: vars.color.gray500},
    "&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 2},
  },
});

export const searchIcon = style({
  width: 18,
  height: 18,
  objectFit: "contain"
});

export const invitationSummary = style({
  minHeight: 128,
  marginTop: 18,
  padding: "18px 20px",
  borderRadius: 22,
  background: "#f9f9f9",
  display: "flex",
  flexDirection: "column",
  justifyContent: "space-between",
  boxSizing: "border-box",
});

export const invitationSummaryTop = style({display: "flex", alignItems: "center", gap: 12});
export const invitationIcon = style({width: 26, height: 26, objectFit: "contain", flexShrink: 0});
export const invitationSummaryCopy = style({minWidth: 0, display: "flex", flexDirection: "column", gap: 3});
export const invitationSummaryTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: 1.35,
  letterSpacing: "-0.16px"
});
export const invitationSummaryText = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.medium,
  fontSize: 14,
  lineHeight: 1.35,
  letterSpacing: "-0.14px"
});
export const invitationSummaryAction = style({display: "flex", justifyContent: "flex-end"});

export const quickButton = style({
  minHeight: 26,
  padding: "5px 14px",
  border: 0,
  borderRadius: 200,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.medium,
  fontSize: 14,
  lineHeight: 1,
  letterSpacing: "-0.14px",
  cursor: "pointer",
  selectors: {
    "&:hover": {filter: "brightness(0.98)"},
    "&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 2},
  },
});

export const invitationDetail = style({marginTop: 26, display: "flex", flexDirection: "column", gap: 14});
export const invitationList = style({display: "flex", flexDirection: "column", gap: 10});
export const invitationItem = style({
  padding: "16px 18px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 18,
  background: vars.color.background,
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: 14
});
export const invitationCopy = style({minWidth: 0});
export const invitationName = style({
  margin: 0,
  overflow: "hidden",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 15,
  lineHeight: 1.35,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap"
});
export const invitationSender = style({
  margin: "4px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: 1.4
});
export const invitationActions = style({display: "flex", gap: 6, flexShrink: 0});

const invitationButton = style({
  minHeight: 32,
  padding: "0 11px",
  borderRadius: 200,
  fontFamily: vars.font.medium,
  fontSize: 12,
  cursor: "pointer",
  selectors: {"&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 2}},
});

export const invitationAccept = style([
  invitationButton,
  {
    border: "1px solid #78b991",
    background: "#eef8f2",
    color: "#34704d",
  },
]);
export const invitationDecline = style([
  invitationButton,
  destructiveButton,
]);

export const clubSection = style({marginTop: 26, display: "flex", flexDirection: "column", gap: 14});

export const sectionTitle = style({
  margin: 0,
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: 1.4,
  letterSpacing: "-0.16px"
});

export const clubList = style({display: "flex", flexDirection: "column", gap: 14});

export const clubCard = style({
  width: "100%",
  height: 156,
  padding: 20,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 22,
  background: vars.color.background,
  display: "flex",
  alignItems: "stretch",
  gap: 16,
  boxSizing: "border-box",
  cursor: "pointer",
  transition: "border-color 160ms ease, transform 160ms ease",
  selectors: {
    "&:hover": {borderColor: vars.color.gray500},
    "&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 2},
  },
});

export const clubCover = style({
  width: 79,
  height: "100%",
  flexShrink: 0,
  border: "1px solid #e6e6e6",
  borderRadius: 6,
  background: vars.color.gray100,
  objectFit: "cover",
  boxSizing: "border-box",
});

export const clubInfo = style({minWidth: 0, flex: 1, display: "flex", flexDirection: "column"});
export const clubTop = style({display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8});
export const clubCategory = style({
  minWidth: 0,
  overflow: "hidden",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: 1.35,
  letterSpacing: "-0.12px",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap"
});
export const clubStatus = style({
  padding: "2px 8px",
  borderRadius: 200,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.medium,
  fontSize: 12,
  lineHeight: 1.35,
  letterSpacing: "-0.12px",
  whiteSpace: "nowrap"
});
export const clubName = style({
  margin: "5px 0 0",
  overflow: "hidden",
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 18,
  lineHeight: 1.25,
  letterSpacing: "-0.18px",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap"
});
export const clubMeta = style({
  margin: "4px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.3,
  letterSpacing: "-0.14px"
});
export const progressTrack = style({
  width: "100%",
  height: 10,
  marginTop: "auto",
  borderRadius: 200,
  background: vars.color.gray200,
  overflow: "hidden"
});
export const progressValue = style({display: "block", height: "100%", borderRadius: 200, background: vars.color.brand});
export const progressText = style({
  margin: "5px 0 0",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.25,
  letterSpacing: "-0.14px"
});

export const empty = style({
  margin: 0,
  padding: "46px 18px",
  borderRadius: 22,
  background: vars.color.gray100,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 13,
  textAlign: "center"
});

export const findClub = style({
  width: "100%",
  marginTop: 30,
  padding: 0,
  border: 0,
  background: "transparent",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  textAlign: "left",
  textDecoration: "none",
  cursor: "pointer",
  selectors: {"&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 5, borderRadius: 8}},
});

export const findClubCopy = style({display: "flex", flexDirection: "column", gap: 4});
export const findClubTitle = style({
  color: vars.color.black,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: 1.35
});
export const findClubDescription = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: 1.35,
  letterSpacing: "-0.12px"
});
export const findClubArrow = style({
  color: vars.color.black,
  fontFamily: vars.font.body,
  fontSize: 30,
  fontWeight: 300,
  lineHeight: 1
});

export const createButton = style({
  position: "fixed",
  right: "max(24px, calc((100vw - 600px) / 2 + 24px))",
  bottom: `calc(${vars.headerHeight} + max(${vars.space.sm}, env(safe-area-inset-bottom, 0px)) + 24px)`,
  zIndex: 20,
  width: 62,
  height: 62,
  padding: 7,
  border: 0,
  borderRadius: "50%",
  background: vars.color.gray500,
  boxShadow: "0 0 16px rgba(0, 0, 0, 0.12)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  cursor: "pointer",
  color: "#fff",
  transition: "filter 160ms ease",
  selectors: {
    "&:hover": {filter: "brightness(0.95)"},
    "&:focus-visible": {outline: `2px solid ${vars.color.brand}`, outlineOffset: 3},
  },
});

export const createIcon = style({width: 48, height: 48, filter: "brightness(0) invert(1)"});
