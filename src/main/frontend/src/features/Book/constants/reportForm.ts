export const BOOK_COLORS = [
  "#ac8a8a",
  "#c96f64",
  "#d58db3",
  "#8d7cc3",
  "#6aa6d8",
  "#78b87d",
  "#2f3437",
  "#f2c7c3",
  "#f5d7a1",
  "#dce8b4",
  "#bfe6df",
  "#bdddf0",
  "#dcc9e8",
];

export const DEFAULT_REPORT_COLOR = BOOK_COLORS[0];

export const MAX_REPORT_CONTENT_BYTES = 4000;

export function isPresetBookColor(color: string) {
  return BOOK_COLORS.some(
    (presetColor) => presetColor.toLowerCase() === color.toLowerCase(),
  );
}
