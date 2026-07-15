/**
 * src/main/frontend/src/app/utils/dateUtil.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
export function padTwoDigits(value: number) {
  return String(value).padStart(2, "0");
}

export function formatDateValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;
}

export function formatYearMonthValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}`;
}

function parseCompactDateParts(value?: string) {
  if (!value) {
    return null;
  }

  const compactDate = value.replace(/\D/g, "");

  if (compactDate.length !== 8) {
    return null;
  }

  const year = Number(compactDate.slice(0, 4));
  const month = Number(compactDate.slice(4, 6));
  const day = Number(compactDate.slice(6, 8));
  const date = new Date(year, month - 1, day);

  if (
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day
  ) {
    return null;
  }

  return { year, month, day, date };
}

function getEnglishOrdinalSuffix(day: number) {
  if (day >= 11 && day <= 13) {
    return "th";
  }

  switch (day % 10) {
    case 1:
      return "st";
    case 2:
      return "nd";
    case 3:
      return "rd";
    default:
      return "th";
  }
}

export function formatCompactDateToKorean(value?: string) {
  if (!value) {
    return "";
  }

  const parsedDate = parseCompactDateParts(value);

  if (!parsedDate) {
    return value;
  }

  return `${parsedDate.year}\uB144${parsedDate.month}\uC6D4${parsedDate.day}\uC77C`;
}

export function formatCompactDateToEnglish(value?: string) {
  if (!value) {
    return "";
  }

  const parsedDate = parseCompactDateParts(value);

  if (!parsedDate) {
    return value;
  }

  const monthName = new Intl.DateTimeFormat("en", { month: "long" }).format(
    parsedDate.date,
  );

  return `${monthName} ${parsedDate.day}${getEnglishOrdinalSuffix(parsedDate.day)}, ${parsedDate.year}`;
}

export function formatCompactDate(value?: string) {
  const locale = navigator.language.toLowerCase();

  if (locale.startsWith("ko")) {
    return formatCompactDateToKorean(value);
  }

  return formatCompactDateToEnglish(value);
}

export function parseLocalDate(value: string) {
  const [year, month, date] = value.split("-").map(Number);
  return new Date(year, month - 1, date);
}

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

export function isSameLocalDate(a: Date, b: Date) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}