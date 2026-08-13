import {style} from "@vanilla-extract/css";
import {vars} from "@/app/styles/tokens.css.ts";

export const overlay = style({
  position: "fixed",
  inset: 0,
  zIndex: 1300,
  padding: 16,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  background: "rgba(0,0,0,.35)",
});

export const modal = style({
  width: "min(520px, 100%)",
  maxHeight: "calc(100dvh - 40px)",
  padding: "20px",
  boxSizing: "border-box",
  borderRadius: 20,
  background: "#fff",
  display: "flex",
  flexDirection: "column",
  gap: 18,
  boxShadow: "0 20px 50px rgba(0,0,0,.22)",
});

export const modalHeader = style({
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: 12,
});

export const modalTitle = style({
  margin: 0,
  fontFamily: vars.font.heading,
  fontSize: 22,
});

export const modalDescription = style({
  margin: "10px 0 0",
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1.5,
  color: vars.color.gray600,
  whiteSpace: "pre-line"
});

export const closeButton = style({
  background: 'transparent',
  width: 30,
  height: 30,
  borderRadius: 99999,
  transition: "background-color 160ms ease",
  cursor: "pointer",
  selectors: {
    '&:hover': {
      background: vars.color.gray100
    }
  }
})

export const modalBody = style({
  display: "flex",
  flexDirection: "column",
  gap: 18,
  overflowY: "auto",
  paddingRight: 4,
});

export const interestGroup = style({
  display: "flex",
  flexDirection: "column",
  gap: 10,
});

export const interestTitle = style({
  margin: 0,
  fontFamily: vars.font.semibold,
  fontSize: 14,
});

export const interestList = style({
  display: "flex",
  flexWrap: "wrap",
  gap: 8,
});

export const interest = style({
  padding: "10px 18px",
  border: `1px solid ${vars.color.gray300}`,
  borderRadius: 999,
  background: "#ffffff",
  color: vars.color.gray900,
  fontFamily: vars.font.body,
  fontSize: 14,
  lineHeight: 1,
  cursor: "pointer",
  transition: "background-color 160ms ease",
  selectors: {
    "&:hover":{
      background: vars.color.gray100
    },
    "&[data-selected='true']": {
      borderColor: "#78b991",
      background: "#eef8f2",
      color: "#34704d",
    },
  },
});

export const modalActions = style({
  display: "flex",
  justifyContent: "flex-end",
  gap: 8,
});