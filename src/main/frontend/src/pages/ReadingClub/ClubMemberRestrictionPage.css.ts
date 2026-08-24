import { vars } from "@/app/styles/tokens.css";
import { style } from "@vanilla-extract/css";

export const page = style({ display: "flex", width: "100%", flexDirection: "column", padding: "10px 0 24px" });
export const description = style({ margin: "0 0 22px", color: vars.color.black, fontFamily: vars.font.semibold, fontSize: 16, lineHeight: 1.55 });
export const list = style({ display: "flex", flexDirection: "column", gap: 10, margin: 0, padding: 0, listStyle: "none" });
export const item = style({ display: "flex", minHeight: 92, alignItems: "center", gap: 12, padding: 16, border: `1px solid ${vars.color.gray300}`, borderRadius: 20, backgroundColor: vars.color.background });
export const avatar = style({ width: 52, height: 52, flexShrink: 0, borderRadius: "50%", objectFit: "cover" });
export const info = style({ display: "flex", minWidth: 0, flex: 1, flexDirection: "column", gap: 4 });
export const name = style({ overflow: "hidden", color: vars.color.black, fontFamily: vars.font.semibold, fontSize: 16, textOverflow: "ellipsis", whiteSpace: "nowrap" });
export const exitDate = style({ color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 12 });
export const restricted = style({ width: "fit-content", color: vars.color.negativeText, fontFamily: vars.font.medium, fontSize: 12 });
export const released = style({ width: "fit-content", color: vars.color.brandText, fontFamily: vars.font.medium, fontSize: 12 });
export const stateMessage = style({ margin: "72px 0 0", color: vars.color.gray600, fontFamily: vars.font.body, fontSize: 14, lineHeight: 1.5, textAlign: "center" });
