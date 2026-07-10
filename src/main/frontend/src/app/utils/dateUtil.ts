/**
 * Number is normalized to a two digit string.
 * @Author Hanwon.Jang
 * @param value Number to normalize.
 * @return Two digit string padded with zero.
 */
export function padTwoDigits(value: number) {
  return String(value).padStart(2, "0");
}

/**
 * Date object is converted to yyyy-MM-dd format.
 * @Author Hanwon.Jang
 * @param date Date object to convert.
 * @return yyyy-MM-dd formatted date string.
 */
export function formatDateValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;
}

/**
 * Date object is converted to yyyy-MM format.
 * @Author Hanwon.Jang
 * @param date Date object to convert.
 * @return yyyy-MM formatted year-month string.
 */
export function formatYearMonthValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}`;
}

/**
 * yyyy-MM-dd text is parsed as a local Date object.
 * @Author Hanwon.Jang
 * @param value yyyy-MM-dd formatted date string.
 * @return Parsed local Date object.
 */
export function parseLocalDate(value: string) {
  const [year, month, date] = value.split("-").map(Number);
  return new Date(year, month - 1, date);
}

/**
 * yyyy-MM-dd text is parsed and the fallback date is returned when the text is empty or invalid.
 * @Author Hanwon.Jang
 * @param value yyyy-MM-dd formatted date string.
 * @param fallbackDate Date returned when value cannot be parsed.
 * @return Parsed Date object or fallback date.
 */
export function parseDateValue(value?: string, fallbackDate = new Date()) {
  if (!value) {
    return fallbackDate;
  }

  const [year, month, day] = value.split("-").map(Number);

  if (!year || !month || !day) {
    return fallbackDate;
  }

  return new Date(year, month - 1, day);
}

/**
 * Two Date objects are compared by year, month, and date.
 * @Author Hanwon.Jang
 * @param a First date to compare.
 * @param b Second date to compare.
 * @return Whether both values represent the same local date.
 */
export function isSameLocalDate(a: Date, b: Date) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}
