import { message } from "@/app/messages/message";
import { useEffect, useMemo, useState } from "react";
import * as styles from "./CalendarDatePicker.css";

type CalendarDatePickerProps = {
  name: string;
  label: string;
  defaultValue?: string;
  placeholder?: string;
};

const WEEK_DAYS = [
  message("frontend.calendar.week.sun"), // frontend.calendar.week.sun = 일
  message("frontend.calendar.week.mon"), // frontend.calendar.week.mon = 월
  message("frontend.calendar.week.tue"), // frontend.calendar.week.tue = 화
  message("frontend.calendar.week.wed"), // frontend.calendar.week.wed = 수
  message("frontend.calendar.week.thu"), // frontend.calendar.week.thu = 목
  message("frontend.calendar.week.fri"), // frontend.calendar.week.fri = 금
  message("frontend.calendar.week.sat"), // frontend.calendar.week.sat = 토
];

const pad = (value: number) => String(value).padStart(2, "0");

const toDateValue = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const parseDateValue = (value?: string) => {
  if (!value) {
    return new Date();
  }

  const [year, month, day] = value.split("-").map(Number);

  if (!year || !month || !day) {
    return new Date();
  }

  return new Date(year, month - 1, day);
};

function CalendarDatePicker({
  name,
  label,
  defaultValue = "",
  placeholder = message("frontend.calendar.dateSelect"), // frontend.calendar.dateSelect = 날짜 선택
}: CalendarDatePickerProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(defaultValue);
  const [viewDate, setViewDate] = useState(() => parseDateValue(defaultValue));

  useEffect(() => {
    setSelectedDate(defaultValue);
    setViewDate(parseDateValue(defaultValue));
  }, [defaultValue]);

  const todayValue = toDateValue(new Date());
  const viewYear = viewDate.getFullYear();
  const viewMonth = viewDate.getMonth();

  const days = useMemo(() => {
    const firstDay = new Date(viewYear, viewMonth, 1).getDay();
    const lastDate = new Date(viewYear, viewMonth + 1, 0).getDate();

    return [
      ...Array.from({ length: firstDay }, () => null),
      ...Array.from({ length: lastDate }, (_, index) => index + 1),
    ];
  }, [viewMonth, viewYear]);

  const changeMonth = (amount: number) => {
    setViewDate(new Date(viewYear, viewMonth + amount, 1));
  };

  const selectDay = (day: number) => {
    const nextDate = toDateValue(new Date(viewYear, viewMonth, day));

    setSelectedDate(nextDate);
    setIsOpen(false);
  };

  return (
    <div className={styles.wrapper}>
      <label className={styles.label} htmlFor={`${name}Trigger`}>
        {label}
      </label>
      <input type="hidden" name={name} value={selectedDate} />
      <button
        className={styles.trigger}
        id={`${name}Trigger`}
        type="button"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((prev) => !prev)}
      >
        <span className={selectedDate ? "" : styles.placeholder}>
          {selectedDate ? selectedDate.replaceAll("-", ".") : placeholder}
        </span>
        <svg
          className={styles.calendarIcon}
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            d="M7 3v3M17 3v3M4.5 9.5h15M6.5 5h11A2.5 2.5 0 0 1 20 7.5v10A2.5 2.5 0 0 1 17.5 20h-11A2.5 2.5 0 0 1 4 17.5v-10A2.5 2.5 0 0 1 6.5 5Z"
            fill="none"
            stroke="currentColor"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="1.8"
          />
        </svg>
      </button>

      {isOpen && (
        <div className={styles.popover}>
          <div className={styles.header}>
            <button
              className={styles.navButton}
              type="button"
              aria-label={message("frontend.calendar.prevMonth")} // frontend.calendar.prevMonth = 이전 달
              onClick={() => changeMonth(-1)}
            >
              ‹
            </button>
            <strong className={styles.monthLabel}>
              {message("frontend.calendar.monthLabel", [
                viewYear,
                viewMonth + 1,
              ]) /* frontend.calendar.monthLabel = {0}년 {1}월 */}
            </strong>
            <button
              className={styles.navButton}
              type="button"
              aria-label={message("frontend.calendar.nextMonth")} // frontend.calendar.nextMonth = 다음 달
              onClick={() => changeMonth(1)}
            >
              ›
            </button>
          </div>

          <div className={styles.weekGrid}>
            {WEEK_DAYS.map((day) => (
              <span className={styles.weekDay} key={day}>
                {day}
              </span>
            ))}
          </div>

          <div className={styles.dayGrid}>
            {days.map((day, index) => {
              if (!day) {
                return <span className={styles.emptyDay} key={`empty-${index}`} />;
              }

              const dateValue = toDateValue(new Date(viewYear, viewMonth, day));
              const dayClassName = [
                styles.dayButton,
                dateValue === todayValue ? styles.today : "",
                dateValue === selectedDate ? styles.selected : "",
              ]
                .filter(Boolean)
                .join(" ");

              return (
                <button
                  className={dayClassName}
                  key={dateValue}
                  type="button"
                  onClick={() => selectDay(day)}
                >
                  {day}
                </button>
              );
            })}
          </div>

          <div className={styles.footer}>
            <button
              className={styles.closeButton}
              type="button"
              onClick={() => setIsOpen(false)}
            >
              {message("frontend.common.close") /* frontend.common.close = 닫기 */}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CalendarDatePicker;
