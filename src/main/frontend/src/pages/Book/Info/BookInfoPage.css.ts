import { style } from "@vanilla-extract/css";
import { vars } from "@/app/styles/tokens.css";

export const page = style({
  position: "relative",
  width: "100vw",
  marginLeft: "calc(50% - 50vw)",
  minHeight: "100vh",
  backgroundColor: "#ffffff",
  overflow: "hidden",

  selectors: {
    "&::before": {
      content: "",
      position: "absolute",
      top: "-36px",
      right: "-36px",
      left: "-36px",
      height: "760px",
      zIndex: 0,
      backgroundImage: "var(--book-bg-image)",
      backgroundRepeat: "no-repeat",
      backgroundPosition: "center top",
      backgroundSize: "cover",
      filter: "blur(16px)",
      transform: "scale(1.12)",
      opacity: 0.86,
      pointerEvents: "none",
      maskImage:
        "linear-gradient(180deg, #000 0%, rgba(0, 0, 0, 0.88) 34%, rgba(0, 0, 0, 0.28) 70%, rgba(0, 0, 0, 0) 100%)",
      WebkitMaskImage:
        "linear-gradient(180deg, #000 0%, rgba(0, 0, 0, 0.88) 34%, rgba(0, 0, 0, 0.28) 70%, rgba(0, 0, 0, 0) 100%)",
    },
    "&::after": {
      content: "",
      position: "absolute",
      top: 0,
      right: 0,
      left: 0,
      height: "860px",
      zIndex: 0,
      background:
        "linear-gradient(180deg, rgba(255, 255, 255, 0.30) 0%, rgba(255, 255, 255, 0.18) 42%, rgba(255, 255, 255, 0.82) 78%, #ffffff 100%)",
      pointerEvents: "none",
    },
  },
});

export const content = style({
  position: "relative",
  zIndex: 1,
  maxWidth: "600px",
  width: "100%",
  margin: "0 auto",
  padding: "28px 20px 28px",
  display: "flex",
  flexDirection: "column",
  gap: "40px",
});

export const header = style({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  textAlign: "center",
  gap: "14px",
  padding: "48px 24px 42px",
  borderRadius: "14px",
  backgroundColor: "rgb(255, 255, 255)",
  boxShadow: "0 18px 38px rgba(0, 0, 0, 0.18)",
});

export const coverFrame = style({
  width: "126px",
  aspectRatio: "2 / 3",
  borderRadius: "6px",
  overflow: "hidden",
  backgroundColor: "#ffffff",
  boxShadow: "0 10px 24px rgba(0, 0, 0, 0.16)",
});

export const coverImage = style({
  display: "block",
  width: "100%",
  height: "100%",
  objectFit: "cover",
});

export const title = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "20px",
  lineHeight: 1.35,
  color: vars.color.black,
  wordBreak: "keep-all",
});

export const meta = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.45,
  color: "#666666",
});

export const authorRatingLine = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "8px",
  maxWidth: "100%",
});

export const ratingSummary = style({
  display: "inline-flex",
  alignItems: "center",
  gap: "3px",
  flexShrink: 0,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  lineHeight: 1,
  color: vars.color.black,
});

export const metaSeparator = style({
  flexShrink: 0,
  fontFamily: vars.font.body,
  fontSize: "12px",
  lineHeight: 1,
  color: vars.color.gray500,
});

export const ratingStar = style({
  color: "#ffd966",
  fontSize: "15px",
  lineHeight: 1,
});

export const ratingValue = style({
  lineHeight: 1,
});

export const contentPanel = style({
  display: "flex",
  flexDirection: "column",
  gap: "28px",
  minHeight: "auto",
  padding: "28px 22px 26px",
  borderRadius: "14px",
  backgroundColor: "rgba(255, 255, 255, 0.96)",
  boxShadow: "0 -12px 32px rgba(0, 0, 0, 0.14)",
});

export const section = style({
  display: "flex",
  flexDirection: "column",
  gap: "12px",
});

export const sectionTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: "15px",
  color: vars.color.black,
});

export const infoGrid = style({
  display: "grid",
  gridTemplateColumns: "58px 1fr",
  gap: "9px 12px",
});

export const infoLabel = style({
  fontFamily: vars.font.middle,
  fontSize: "13px",
  color: "#777777",
});

export const infoValue = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "13px",
  lineHeight: 1.5,
  color: vars.color.black,
  wordBreak: "break-word",
});

export const description = style({
  margin: 0,
  fontFamily: vars.font.body,
  fontSize: "14px",
  lineHeight: 1.8,
  color: vars.color.black,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});

export const selectButton = style({
  height: "44px",
  border: `1px solid ${vars.color.black}`,
  borderRadius: vars.radius.xl,
  backgroundColor: vars.color.black,
  color: "#ffffff",
  fontFamily: vars.font.heading,
  fontSize: "14px",
  cursor: "pointer",
});

export const bookInfoButton = style({
  height: "34px",
  padding: "0 14px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const bookInfoActionRow = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "8px",
  flexWrap: "wrap",
  width: "100%",
});

export const reportBackButton = style({
  height: "34px",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "5px",
  padding: "0 13px",
  border: `1px solid ${vars.color.gray400}`,
  borderRadius: vars.radius.xl,
  backgroundColor: "#ffffff",
  color: vars.color.black,
  fontFamily: vars.font.middle,
  fontSize: "13px",
  cursor: "pointer",
});

export const reportBackIcon = style({
  width: "15px",
  height: "15px",
  flexShrink: 0,
});
