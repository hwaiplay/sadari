import {vars} from "@/app/styles/tokens.css";
import {style} from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  minHeight: "calc(100dvh - 120px)",
  flexDirection: "column",
  paddingTop: 20,
  paddingBottom: 20,
});

export const voteSummary = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  padding: 20,
  borderRadius: 22,
  background: vars.color.gray100,
});

export const summaryTitle = style({
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: 18,
  lineHeight: "24px"
});

export const deadline = style({
  margin: "4px 0 0",
  color: vars.color.gray600,
  fontSize: 14,
  lineHeight: "20px"
});

export const dDay = style({
  padding: "3px 9px",
  borderRadius: 999,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontSize: 12,
  fontFamily: vars.font.semibold,
});

export const candidateSection = style({marginTop: 28});
export const sectionTitle = style({margin: "0 0 10px", fontFamily: vars.font.semibold, fontSize: 16});
export const candidateList = style({display: "flex", flexDirection: "column", gap: 12});

export const candidateCard = style({
  position: "relative",
  minHeight: 156,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 22,
  background: vars.color.background,
  transition: "background 160ms ease",
  selectors: {
    "&:focus-visible": {outline: `1px solid ${vars.color.brandText}`, outlineOffset: 2},
  },
});

export const candidateSelect = style({
  display: "grid",
  width: "100%",
  minHeight: 154,
  gridTemplateColumns: "76px minmax(0, 1fr) 24px",
  alignItems: "center",
  gap: 16,
  padding: 20,
  border: 0,
  background: "transparent",
  textAlign: "left",
  cursor: "pointer"
});

export const candidateResult = style({
  display: "grid",
  width: "100%",
  minHeight: 154,
  gridTemplateColumns: "90px minmax(0, 1fr) auto",
  alignItems: "center",
  justifyContent: "space-between",
  padding: 20,
  boxSizing: "border-box",
});

export const cover = style({width: 76, height: 112, borderRadius: 6, objectFit: "cover"});

export const bookInformation = style({display: "flex", minWidth: 0, flexDirection: "column", gap: 4});

export const recommender = style({color: vars.color.gray600, fontSize: 12});

export const bookTitle = style({
  overflow: "hidden",
  fontFamily: vars.font.semibold,
  fontSize: 16,
  textOverflow: "ellipsis",
  whiteSpace: "nowrap"
});

export const author = style({color: vars.color.gray600, fontSize: 14});

export const radioIndicator = style({
  width: 22,
  height: 22,
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  selectors: {'[data-selected="true"] &': {border: "7px solid #8edcba"}}
});

export const voteRateRow = style({
  display: "flex",
  alignItems: "center",
  gap: 10,
  marginTop: 8,
});

export const voteRateTrack = style({
  position: "relative",
  height: 8,
  flex: 1,
  overflow: "hidden",
  borderRadius: 999,
  background: vars.color.gray200,
});

export const voteRateFill = style({
  display: "block",
  height: "100%",
  borderRadius: 999,
  background: vars.color.brand,
  transition: "width 240ms ease",
});

export const voteRate = style({
  minWidth: 42,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 14,
  textAlign: "right",
});

export const myVoteBadge = style({
  padding: "4px 8px",
  borderRadius: 999,
  background: vars.color.brandBg,
  color: vars.color.brandText,
  fontFamily: vars.font.semibold,
  fontSize: 12,
  whiteSpace: "nowrap",
  display: "flex",
  justifyContent:"center",
  alignItems:"center",
  gap: 4,
  position: "absolute",
  top: 20,
  right: 20
});

export const cancelRecommendationButton = style({
  position: "absolute",
  right: 14,
  bottom: 12,
  zIndex: 1,
});

export const emptyState = style({
  display: "flex",
  minHeight: 220,
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: 6,
  borderRadius: 22,
  color: vars.color.gray600,
  textAlign: "center"
});

export const emptyTitle = style({
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 18
});

export const emptyDescription = style({margin: 0, fontSize: 14});

export const guide = style({
  marginTop: "auto",
  padding: 16,
  borderRadius: 12,
  background: vars.color.gray100,
  color: vars.color.gray600,
  fontSize: 14,
  lineHeight: "22px"
});

export const guideTitle = style({
  fontFamily: vars.font.semibold,
  fontSize: 14
})

export const guideList = style({
  fontSize: 14,
  marginLeft: 20,
  marginTop: 4
})

export const actions = style({
  display: "grid",
  gap: 8,
  marginTop: 16,
  selectors: {
    '&[data-button-count="one"]': {
      gridTemplateColumns: "1fr"
    },
    '&[data-button-count="two"]': {
      gridTemplateColumns: "1fr 1fr"
    }
  }
});
