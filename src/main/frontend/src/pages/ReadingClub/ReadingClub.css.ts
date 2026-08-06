import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({
  width: "100%",
  maxWidth: "600px",
  margin: "0 auto",
  padding: "18px 0 36px",
  display: "flex",
  flexDirection: "column",
  gap: "22px",
});

export const searchLabel = style({ position: "relative", display: "block", width: "100%" });
export const hiddenLabel = style({ position: "absolute", width: 1, height: 1, overflow: "hidden", clip: "rect(0,0,0,0)" });
export const searchInput = style({
  width: "100%", height: 42, padding: "0 42px 0 16px", boxSizing: "border-box",
  border: `1px solid ${vars.color.gray300}`, borderRadius: 999, background: "#fff",
  fontFamily: vars.font.body, fontSize: 15, outline: "none",
  selectors: { "&:focus": { borderColor: vars.color.black }, "&::placeholder": { color: vars.color.gray500 } },
});
export const searchButton = style({
  position: "absolute", top: "50%", right: 8, transform: "translateY(-50%)", width: 32, height: 32,
  border: 0, borderRadius: "50%", background: "transparent", cursor: "pointer", fontSize: 18,
});

export const section = style({ display: "flex", flexDirection: "column", gap: 14 });
export const sectionTitle = style({ margin: 0, fontFamily: vars.font.semibold, fontSize: 16, color: vars.color.black });
export const list = style({ display: "flex", flexDirection: "column", gap: 12 });
export const card = style({
  padding: "19px 18px", border: `1px solid ${vars.color.gray300}`, borderRadius: 18,
  background: "#fff", display: "flex", flexDirection: "column", gap: 11, cursor: "pointer",
  transition: "border-color 160ms ease, transform 160ms ease",
  selectors: { "&:hover": { borderColor: vars.color.gray500, transform: "translateY(-1px)" } },
});
export const cardTop = style({ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 });
export const cardTitle = style({ margin: 0, fontFamily: vars.font.heading, fontSize: 17, lineHeight: 1.35 });
export const badge = style({ padding: "5px 9px", borderRadius: 999, background: vars.color.gray100, fontFamily: vars.font.semibold, fontSize: 11, whiteSpace: "nowrap" });
export const description = style({ margin: 0, fontFamily: vars.font.body, fontSize: 13, lineHeight: 1.6, color: vars.color.gray600, whiteSpace: "pre-wrap" });
export const meta = style({ display: "flex", flexWrap: "wrap", gap: "7px 12px", fontFamily: vars.font.body, fontSize: 12, color: vars.color.gray500 });
export const chips = style({ display: "flex", flexWrap: "wrap", gap: 7 });
export const chip = style({ padding: "6px 10px", border: `1px solid ${vars.color.gray300}`, borderRadius: 999, background: "#fff", fontFamily: vars.font.body, fontSize: 11, color: vars.color.gray700 });

export const createArea = style({
  minHeight: 94, padding: "20px", border: `1px dashed ${vars.color.gray400}`, borderRadius: 18,
  background: vars.color.gray100, display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16,
});
export const createCopy = style({ display: "flex", flexDirection: "column", gap: 5 });
export const createTitle = style({ margin: 0, fontFamily: vars.font.semibold, fontSize: 15 });
export const createDescription = style({ margin: 0, fontFamily: vars.font.body, fontSize: 12, color: vars.color.gray500 });

export const button = style({
  minHeight: 38, padding: "0 15px", border: `1px solid ${vars.color.gray500}`, borderRadius: 999,
  background: "#fff", color: vars.color.black, fontFamily: vars.font.semibold, fontSize: 13, cursor: "pointer",
  selectors: { "&:hover": { background: vars.color.gray100 }, "&:disabled": { opacity: .5, cursor: "default" } },
});
export const buttonDanger = style([button, { borderColor: "#d7a5a5", color: "#ad4444" }]);
export const actions = style({ display: "flex", flexWrap: "wrap", gap: 8 });

export const empty = style({ padding: "46px 18px", textAlign: "center", fontFamily: vars.font.body, fontSize: 13, color: vars.color.gray500 });
export const loading = style([empty, { padding: "70px 18px" }]);

export const form = style({ display: "flex", flexDirection: "column", gap: 28 });
export const field = style({ display: "flex", flexDirection: "column", gap: 10 });
export const label = style({ fontFamily: vars.font.semibold, fontSize: 14, color: vars.color.black });
export const input = style({
  width: "100%", minHeight: 44, padding: "0 14px", boxSizing: "border-box", border: `1px solid ${vars.color.gray300}`,
  borderRadius: 13, background: "#fff", fontFamily: vars.font.body, fontSize: 14, outline: "none",
  selectors: { "&:focus": { borderColor: vars.color.black } },
});
export const textarea = style([input, { minHeight: 128, padding: 14, resize: "vertical", lineHeight: 1.55 }]);
export const optionGrid = style({ display: "grid", gridTemplateColumns: "repeat(2, minmax(0, 1fr))", gap: 10 });
export const option = style({
  minHeight: 70, padding: "13px 14px", border: `1px solid ${vars.color.gray200}`, borderRadius: 14,
  background: "#f7f8f8", display: "flex", flexDirection: "column", justifyContent: "center", gap: 5,
  color: vars.color.gray600, cursor: "pointer",
  selectors: { "&:hover": { borderColor: vars.color.gray300 }, "&[data-selected='true']": { borderColor: vars.color.gray600, background: "#fff", color: vars.color.black } },
});
export const optionTitle = style({ fontFamily: vars.font.semibold, fontSize: 13 });
export const optionDescription = style({ fontFamily: vars.font.body, fontSize: 11, lineHeight: 1.45 });
export const stepper = style({ display: "grid", gridTemplateColumns: "42px minmax(72px, 110px) 42px", width: "fit-content", border: `1px solid ${vars.color.gray300}`, borderRadius: 13, overflow: "hidden" });
export const stepperButton = style({ border: 0, background: vars.color.gray100, fontSize: 19, cursor: "pointer" });
export const stepperInput = style({ width: "100%", height: 42, border: 0, borderLeft: `1px solid ${vars.color.gray300}`, borderRight: `1px solid ${vars.color.gray300}`, textAlign: "center", fontFamily: vars.font.semibold, fontSize: 14, outline: "none" });
export const questionRow = style({ display: "grid", gridTemplateColumns: "minmax(0, 1fr) auto", gap: 8 });

export const overlay = style({ position: "fixed", inset: 0, zIndex: 1300, padding: 16, background: "rgba(0,0,0,.35)", display: "flex", alignItems: "center", justifyContent: "center" });
export const modal = style({ width: "min(520px, 100%)", maxHeight: "calc(100dvh - 40px)", padding: "22px 18px 18px", boxSizing: "border-box", borderRadius: 20, background: "#fff", display: "flex", flexDirection: "column", gap: 18, boxShadow: "0 20px 50px rgba(0,0,0,.22)" });
export const modalHeader = style({ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 });
export const modalTitle = style({ margin: 0, fontFamily: vars.font.heading, fontSize: 19 });
export const modalDescription = style({ margin: "5px 0 0", fontFamily: vars.font.body, fontSize: 12, lineHeight: 1.5, color: vars.color.gray500 });
export const modalBody = style({ overflowY: "auto", display: "flex", flexDirection: "column", gap: 18, paddingRight: 4 });
export const interestGroup = style({ display: "flex", flexDirection: "column", gap: 9 });
export const interestTitle = style({ margin: 0, fontFamily: vars.font.semibold, fontSize: 13 });
export const interestList = style({ display: "flex", flexWrap: "wrap", gap: 8 });
export const interest = style({
  minHeight: 34, padding: "0 12px", border: `1px solid ${vars.color.gray200}`, borderRadius: 999,
  background: "#f7f8f8", color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 12, cursor: "pointer",
  selectors: { "&[data-selected='true']": { borderColor: "#78b991", background: "#eef8f2", color: "#34704d" } },
});
export const modalActions = style({ display: "flex", justifyContent: "flex-end", gap: 8 });

export const detailHeader = style({ padding: "24px 0 5px", display: "flex", flexDirection: "column", gap: 12 });
export const detailTitle = style({ margin: 0, fontFamily: vars.font.heading, fontSize: 24, lineHeight: 1.35 });
export const panel = style({ padding: "20px 18px", border: `1px solid ${vars.color.gray300}`, borderRadius: 18, display: "flex", flexDirection: "column", gap: 15, background: "#fff" });
export const profileRow = style({ display: "grid", gridTemplateColumns: "42px minmax(0,1fr) auto", alignItems: "center", gap: 10 });
export const avatar = style({ width: 42, height: 42, borderRadius: "50%", objectFit: "cover", background: vars.color.gray200 });
export const profileName = style({ margin: 0, fontFamily: vars.font.semibold, fontSize: 13 });
export const profileIntro = style({ margin: "3px 0 0", fontFamily: vars.font.body, fontSize: 11, color: vars.color.gray500 });
export const application = style({ padding: "16px 0", borderBottom: `1px solid ${vars.color.gray200}`, display: "flex", flexDirection: "column", gap: 12, selectors: { "&:last-child": { borderBottom: 0 } } });
export const qa = style({ display: "flex", flexDirection: "column", gap: 5, fontFamily: vars.font.body, fontSize: 12, lineHeight: 1.5 });
