import { message } from "@/app/messages/message";
import { formatDateValue, parseDateValue } from "@/app/utils/dateUtil";
import { useEffect, useMemo, useRef, useState } from "react";
import * as styles from "./CalendarDatePicker.css";

type MonthMoveDirection = "prev" | "next";

type CalendarDatePickerProps = {
  name: string;
  label?: string;
  // 외부 폼 state에서 날짜 값을 직접 제어해야 할 때 사용하는 선택 날짜입니다.
  value?: string;
  defaultValue?: string;
  placeholder?: string;
  // 날짜 선택이 확정되면 부모 폼의 state와 hidden input 값을 함께 동기화합니다.
  onChange?: (value: string) => void;
  // 시작일/종료일 역전처럼 선택 즉시 막아야 하는 검증을 부모 폼에서 실행합니다.
  onBeforeChange?: (value: string) => boolean;
  endName?: string;
  endValue?: string;
  endPlaceholder?: string;
  onRangeChange?: (startValue: string, endValue: string) => void;
};

const WEEK_DAY_KEYS = [
  "frontend.calendar.week.sun",
  "frontend.calendar.week.mon",
  "frontend.calendar.week.tue",
  "frontend.calendar.week.wed",
  "frontend.calendar.week.thu",
  "frontend.calendar.week.fri",
  "frontend.calendar.week.sat",
];

/**
 * 달력 팝오버를 통해 날짜를 선택하고 hidden input으로 값을 전달합니다.
 *
 * @author Hanwon.Jang
 * @param name 폼 전송에 사용할 input 이름
 * @param label 날짜 입력 라벨
 * @param defaultValue 초기 선택 날짜
 * @param placeholder 날짜 미선택 상태에서 보여줄 문구
 * @return 달력 날짜 선택 컴포넌트
 */
function CalendarDatePicker({
  name,
  label,
  value,
  defaultValue = "",
  placeholder = message("frontend.calendar.dateSelect"),
  onChange,
  onBeforeChange,
  endName,
  endValue = "",
  endPlaceholder = message("frontend.report.placeholder.endDate"),
  onRangeChange,
}: CalendarDatePickerProps) {
  const wrapperRef = useRef<HTMLDivElement | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(defaultValue);
  const [viewDate, setViewDate] = useState(() => parseDateValue(defaultValue));
  const [monthMoveDirection, setMonthMoveDirection] =
    useState<MonthMoveDirection>("next");
  const currentDateValue = value ?? selectedDate;
  const isRangePicker = Boolean(endName);
  const currentEndDateValue = endValue;

  useEffect(() => {
    setSelectedDate(defaultValue);
    setViewDate(parseDateValue(defaultValue));
  }, [defaultValue]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const handlePointerDown = (event: PointerEvent) => {
      const target = event.target;

      if (
        wrapperRef.current &&
        target instanceof Node &&
        wrapperRef.current.contains(target)
      ) {
        return;
      }

      setIsOpen(false);
    };

    document.addEventListener("pointerdown", handlePointerDown);

    return () => {
      document.removeEventListener("pointerdown", handlePointerDown);
    };
  }, [isOpen]);

  useEffect(() => {
    // 외부 state로 제어하는 날짜가 바뀌면 달력이 해당 월을 바라보도록 동기화합니다.
    if (value === undefined) {
      return;
    }

    setViewDate(parseDateValue(value));
  }, [value]);

  const todayValue = formatDateValue(new Date());
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

  /**
   * 현재 보고 있는 달을 이전 달 또는 다음 달로 이동합니다.
   *
   * @author Hanwon.Jang
   * @param amount 이동할 월 수
   * @return
   */
  const changeMonth = (amount: number) => {
    setMonthMoveDirection(amount < 0 ? "prev" : "next");
    setViewDate(new Date(viewYear, viewMonth + amount, 1));
  };

  /**
   * 선택한 일을 yyyy-MM-dd 값으로 변환해 hidden input 값으로 반영합니다.
   *
   * @author Hanwon.Jang
   * @param day 선택한 일
   * @return
   */
  const selectDay = (day: number) => {
    const nextDate = formatDateValue(new Date(viewYear, viewMonth, day));

    if (isRangePicker) {
      if (!currentDateValue || currentEndDateValue) {
        setSelectedDate(nextDate);
        onChange?.(nextDate);
        onRangeChange?.(nextDate, "");
        return;
      }

      if (new Date(nextDate) < new Date(currentDateValue)) {
        onRangeChange?.(nextDate, currentDateValue);
      } else {
        onRangeChange?.(currentDateValue, nextDate);
      }

      return;
    }

    if (onBeforeChange && !onBeforeChange(nextDate)) {
      return;
    }

    setSelectedDate(nextDate);
    onChange?.(nextDate);
    setIsOpen(false);
  };

  return (
    <div
      className={`${styles.wrapper} ${label ? "" : styles.wrapperNoLabel}`}
      ref={wrapperRef}
    >
      {label && (
        <label className={styles.label} htmlFor={`${name}Trigger`}>
          {label}
        </label>
      )}
      <input type="hidden" name={name} value={currentDateValue} />
      {endName && (
        <input type="hidden" name={endName} value={currentEndDateValue} />
      )}
      <button
        className={styles.trigger}
        id={`${name}Trigger`}
        type="button"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((prev) => !prev)}
      >
        <span
          className={
            currentDateValue || currentEndDateValue ? "" : styles.placeholder
          }
        >
          {isRangePicker
            ? [
                currentDateValue
                  ? currentDateValue.replaceAll("-", ".")
                  : placeholder,
                currentEndDateValue
                  ? currentEndDateValue.replaceAll("-", ".")
                  : endPlaceholder,
              ].join(" ~ ")
            : currentDateValue
              ? currentDateValue.replaceAll("-", ".")
              : placeholder}
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
              aria-label={message("frontend.calendar.prevMonth")}
              onClick={() => changeMonth(-1)}
            >
              <svg
                className={styles.navIcon}
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path d="M15 5 8 12l7 7" />
              </svg>
            </button>
            <strong className={styles.monthLabel}>
              {message("frontend.calendar.monthLabel", [
                viewYear,
                viewMonth + 1,
              ])}
            </strong>
            <button
              className={styles.navButton}
              type="button"
              aria-label={message("frontend.calendar.nextMonth")}
              onClick={() => changeMonth(1)}
            >
              <svg
                className={styles.navIcon}
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path d="m9 5 7 7-7 7" />
              </svg>
            </button>
          </div>

          <div className={styles.weekGrid}>
            {WEEK_DAY_KEYS.map((dayKey) => (
              <span className={styles.weekDay} key={dayKey}>
                {message(dayKey)}
              </span>
            ))}
          </div>

          <div
            className={`${styles.dayGrid} ${
              monthMoveDirection === "prev"
                ? styles.dayGridSlideFromLeft
                : styles.dayGridSlideFromRight
            }`}
            key={`${viewYear}-${viewMonth}`}
          >
            {days.map((day, index) => {
              if (!day) {
                return <span className={styles.emptyDay} key={`empty-${index}`} />;
              }

              const dateValue = formatDateValue(new Date(viewYear, viewMonth, day));
              const isRangeStart = isRangePicker && dateValue === currentDateValue;
              const isRangeEnd = isRangePicker && dateValue === currentEndDateValue;
              const isRangeSameDay = isRangeStart && isRangeEnd;
              const isRangeInner =
                isRangePicker &&
                Boolean(currentDateValue) &&
                Boolean(currentEndDateValue) &&
                new Date(currentDateValue) < new Date(dateValue) &&
                new Date(dateValue) < new Date(currentEndDateValue);
              const dayClassName = [
                styles.dayButton,
                dateValue === todayValue ? styles.today : "",
                isRangeInner ? styles.rangeInner : "",
                isRangeSameDay
                  ? styles.rangeSameDay
                  : isRangeStart
                    ? styles.rangeStart
                    : isRangeEnd
                      ? styles.rangeEnd
                  : dateValue === currentDateValue
                    ? styles.selected
                    : "",
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
              {isRangePicker
                ? "완료"
                : message("frontend.common.close")}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CalendarDatePicker;
