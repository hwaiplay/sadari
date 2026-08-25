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
  selectors: {
    "&:hover": {background: vars.color.gray100},
    '&[data-selected="true"]': {background: "#eef8f2", boxShadow: "0 0 0 1px #34704d"},
    "&:focus-visible": {outline: "2px solid #78b991", outlineOffset: 2},
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
export const deleteButton = style({
  position: "absolute",
  top: 8,
  right: 12,
  padding: "4px 8px",
  border: 0,
  borderRadius: 6,
  background: "transparent",
  color: vars.color.gray600,
  fontSize: 12,
  cursor: "pointer",
  selectors: {"&:hover": {background: vars.color.gray100, color: vars.color.gray900}}
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