import { vars } from "@/app/styles/tokens.css";
import { globalStyle, style } from "@vanilla-extract/css";

export const page = style({
  display: "flex",
  flexDirection: "column",
  gap: 18,
  width: "100%",
  maxWidth: 600,
  margin: "0 auto",
  padding: "20px 0 104px",
});

export const clubSummary = style({
  position: "relative",
  display: "flex",
  flexDirection: "column",
  gap: 14,
  padding: "20px 60px 20px 20px",
  borderRadius: 20,
  background: vars.color.gray100,
});

export const moreSelect = style({
  position: "absolute",
  top: 14,
  right: 14,
  zIndex: 10,
});

export const moreButton = style({
  width: 36,
  minWidth: 36,
  height: 36,
  padding: 0,
  justifyContent: "center",
  selectors: {
    "&:hover, &:focus-visible": {
      background: vars.color.gray200,
      outline: "none",
    },
  },
});

export const moreIcon = style({
  display: "block",
  width: 20,
  height: 20,
});

export const moreOptionList = style({
  minWidth: 120,
});

export const moreOption = style({
  minHeight: 36,
  fontSize: 14,
});

export const dangerOption = style({
  color: vars.color.negativeText,
  selectors: {
    "&:hover, &:focus-visible": {
      background: vars.color.negativeBg,
      color: vars.color.negativeText,
    },
  },
});

export const chips = style({ display: "flex", flexWrap: "wrap", gap: 6 });

export const chip = style({
  padding: "4px 14px",
  borderRadius: 999,
  background: vars.color.gray900,
  color: "#fff",
  fontFamily: vars.font.body,
  fontSize: 12,
  lineHeight: "18px",
});

export const summaryText = style({
  display: "flex",
  flexDirection: "column",
  gap: 4
});

export const detailTitle = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.heading,
  fontSize: 18,
  lineHeight: "27px",
});

export const meta = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "21px",
});

export const description = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "21px",
  whiteSpace: "pre-wrap",
});

export const section = style({ display: "flex", flexDirection: "column", gap: 12 });

export const sectionTitle = style({
  margin: 0,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 14,
  lineHeight: "21px",
});

export const currentReadingCard = style({
  display: "flex",
  minHeight: 256,
  boxSizing: "border-box",
  flexDirection: "column",
  gap: 18,
  padding: 20,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 22,
  background: "#fff",
});

export const readingCardHeader = style({
  minHeight: 21,
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 14,
  textAlign: "center",
});

export const readingEmpty = style({
  display: "flex",
  flex: 1,
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: 12,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "20px",
});

export const readingBook = style({
  display: "flex",
  flex: 1,
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  gap: 8,
});

export const currentBookImage = style({
  width: 96,
  height: 144,
  borderRadius: 4,
  objectFit: "cover",
});

export const currentBookTitle = style({
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  lineHeight: "24px",
  textAlign: "center",
});

export const currentBookAuthor = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: "20px",
  textAlign: "center",
});

export const memberHeader = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
});

export const chatButton = style({
  padding: "4px 10px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 999,
  background:"#fff",
  color: vars.color.gray900,
  fontFamily: vars.font.medium,
  fontSize: 14,
  cursor: "pointer",
  display: 'flex',
  alignItems: 'center',
  gap: '4px',
});

export const memberSummary = style({ display: "flex", alignItems: "center", gap: 12, minHeight: 38 });

export const memberProfiles = style({
  display: "flex",
  flexWrap: "wrap",
  alignItems: "center",
  gap: 8,
  margin: 0,
  padding: 0,
  listStyle: "none",
});

export const memberProfileItem = style({
  width: 36,
  height: 36,
});

export const memberProfilesOverlapped = style({
  gap: 0,
});

globalStyle(`${memberProfilesOverlapped} > ${memberProfileItem}:not(:first-child)`, {
  marginLeft: "-18px",
});

export const memberProfileImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: "50%",
  background: vars.color.gray200,
  objectFit: "cover",
});

export const memberCountText = style({
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
});

export const clubNavigation = style({
  display: "flex",
  flexDirection: "column",
  gap: '4px',
});

export const navigationRow = style({
  display: "flex",
  width: "100%",
  padding: "10px 0",
  alignItems: "center",
  justifyContent: "space-between",
  background: "transparent",
  color: vars.color.gray900,
  fontFamily: vars.font.semibold,
  fontSize: 16,
  textAlign: "left",
  cursor: "pointer",
});

export const navigationDescription = style({
  display: "block",
  marginTop: 4,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 12,
  fontWeight: 400,
});

export const chevron = style({ color: vars.color.gray500, fontSize: 24, fontWeight: 300 });

export const panel = style({
  display: "flex",
  flexDirection: "column",
  gap: 15,
  padding: 20,
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 18,
  background: "#fff",
});

export const panelDescription = style({
  margin: 0,
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.6,
});

export const management = style({ display: "flex", flexDirection: "column", gap: 18 });
export const field = style({ display: "flex", flexDirection: "column", gap: 8 });
export const label = style({ color: vars.color.gray900, fontFamily: vars.font.semibold, fontSize: 14 });

export const textarea = style({
  width: "100%",
  minHeight: 112,
  padding: 14,
  boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 8,
  background: "#fff",
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.55,
  outline: "none",
  resize: "none",
  selectors: { "&:focus": { borderColor: vars.color.gray900 } },
});

export const profileRow = style({
  display: "grid",
  gridTemplateColumns: "42px minmax(0, 1fr) auto",
  alignItems: "center",
  gap: 10,
});

export const avatar = style({
  width: 42,
  height: 42,
  borderRadius: "50%",
  background: vars.color.gray200,
  objectFit: "cover",
});

export const profileName = style({ display: "block", fontFamily: vars.font.semibold, fontSize: 14 });

export const application = style({
  display: "flex",
  flexDirection: "column",
  gap: 12,
  padding: "16px 0",
  borderBottom: `1px solid ${vars.color.gray300}`,
  selectors: { "&:last-child": { borderBottom: 0 } },
});

export const qa = style({ display: "flex", flexDirection: "column", gap: 5, fontFamily: vars.font.body, fontSize: 12, lineHeight: 1.5 });
export const actions = style({ display: "flex", flexWrap: "wrap", gap: 8 });

export const empty = style({
  margin: 0,
  padding: "32px 18px",
  color: vars.color.gray600,
  fontFamily: vars.font.body,
  fontSize: 14,
  textAlign: "center",
});

export const fixedActionArea = style({
  position: "fixed",
  zIndex: 900,
  right: 16,
  bottom: 80,
  left: 16,
  width: "calc(100% - 32px)",
  maxWidth: 568,
  margin: "0 auto",
  display:"grid",
  gridTemplateColumns: '1fr 1fr',
  gap: '10px'
});
