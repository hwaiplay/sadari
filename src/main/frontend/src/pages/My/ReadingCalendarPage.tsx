import api from "@/app/api/axios";
import { Container } from "@/components/Layout/Container/Container";
import { clsx } from "clsx";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./ReadingCalendarPage.css";

type CalendarReport = {
  reportNumb: number;
  bookTitl: string;
  reportStdt: string;
  reportEndt: string;
  reportColr?: string;
};

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

function formatYearMonth(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function formatMonthTitle(date: Date) {
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
}

function formatDisplayDate(value: string) {
  return value.replaceAll("-", ".");
}

function formatSelectedDate(date: Date) {
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function toDate(value: string) {
  const [year, month, date] = value.split("-").map(Number);
  return new Date(year, month - 1, date);
}

function isSameDay(a: Date, b: Date) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

function isReadingOnDate(report: CalendarReport, date: Date) {
  const start = toDate(report.reportStdt);
  const end = toDate(report.reportEndt);
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());

  return start <= target && target <= end;
}

function compareReports(a: CalendarReport, b: CalendarReport) {
  const startCompare = toDate(a.reportStdt).getTime() - toDate(b.reportStdt).getTime();

  if (startCompare !== 0) {
    return startCompare;
  }

  return a.reportNumb - b.reportNumb;
}

function getCalendarDays(month: Date) {
  const firstDay = new Date(month.getFullYear(), month.getMonth(), 1);
  const start = new Date(firstDay);
  start.setDate(firstDay.getDate() - firstDay.getDay());

  return Array.from({ length: 42 }, (_, index) => {
    const day = new Date(start);
    day.setDate(start.getDate() + index);
    return day;
  });
}

function getReportsOnDate(reports: CalendarReport[], date: Date) {
  return reports.filter((report) => isReadingOnDate(report, date)).sort(compareReports);
}

function ReadingCalendarPage() {
  const navigate = useNavigate();
  const [currentMonth, setCurrentMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState(() => new Date());
  const [reports, setReports] = useState<CalendarReport[]>([]);
  const yearMonth = formatYearMonth(currentMonth);
  const days = useMemo(() => getCalendarDays(currentMonth), [currentMonth]);
  const today = useMemo(() => new Date(), []);
  const selectedReports = useMemo(
    () => getReportsOnDate(reports, selectedDate),
    [reports, selectedDate],
  );

  useEffect(() => {
    let ignore = false;

    api
      .get(`/user/reading-calendar?yearMonth=${yearMonth}`)
      .then((response) => {
        if (!ignore && response.data?.code === 200) {
          setReports(response.data.data ?? []);
        }
      })
      .catch(() => {
        if (!ignore) {
          setReports([]);
        }
      });

    return () => {
      ignore = true;
    };
  }, [yearMonth]);

  const moveMonth = (offset: number) => {
    setCurrentMonth((prev) => {
      const next = new Date(prev.getFullYear(), prev.getMonth() + offset, 1);
      setSelectedDate(next);
      return next;
    });
  };

  return (
    <main className={styles.page}>
      <Container className={styles.content}>
        <div className={styles.toolbar}>
          <button
            className={styles.monthButton}
            type="button"
            aria-label="이전 달"
            onClick={() => moveMonth(-1)}
          >
            ‹
          </button>
          <h1 className={styles.monthTitle}>{formatMonthTitle(currentMonth)}</h1>
          <button
            className={styles.monthButton}
            type="button"
            aria-label="다음 달"
            onClick={() => moveMonth(1)}
          >
            ›
          </button>
        </div>

        <section className={styles.calendar} aria-label="월간 독서 시간표">
          {WEEKDAYS.map((weekday) => (
            <div className={styles.weekday} key={weekday}>
              {weekday}
            </div>
          ))}
          {days.map((day) => {
            const dayReports = getReportsOnDate(reports, day);
            const visibleReports = dayReports.slice(0, 3);
            const isOutsideMonth = day.getMonth() !== currentMonth.getMonth();

            return (
              <button
                className={clsx(
                  styles.dayCell,
                  isOutsideMonth && styles.outsideDay,
                  isSameDay(day, today) && styles.today,
                  isSameDay(day, selectedDate) && styles.selectedDay,
                )}
                type="button"
                onClick={() => setSelectedDate(day)}
                key={day.toISOString()}
              >
                <span
                  className={clsx(
                    styles.dayNumber,
                    isOutsideMonth && styles.outsideDayNumber,
                  )}
                >
                  {day.getDate()}
                </span>
                <div className={styles.dayBooks}>
                  {visibleReports.map((report) => {
                    const backgroundColor = report.reportColr || "#e5e5e5";

                    return (
                      <div
                        className={styles.bookPill}
                        style={{ backgroundColor }}
                        title={report.bookTitl}
                        key={`${day.toISOString()}-${report.reportNumb}`}
                      />
                    );
                  })}
                  {dayReports.length > visibleReports.length && (
                    <div className={styles.moreCount}>
                      +{dayReports.length - visibleReports.length}
                    </div>
                  )}
                </div>
              </button>
            );
          })}
        </section>

        <p className={styles.selectedSummary}>
          {formatSelectedDate(selectedDate)}
        </p>

        {selectedReports.length > 0 ? (
          <section className={styles.scheduleList} aria-label="선택한 날짜의 독서 목록">
            {selectedReports.map((report) => {
              const backgroundColor = report.reportColr || "#e5e5e5";

              return (
                <button
                  className={styles.scheduleItem}
                  type="button"
                  onClick={() => navigate(`/book/detail/${report.reportNumb}`)}
                  key={report.reportNumb}
                >
                  <span
                    className={styles.scheduleColor}
                    style={{ backgroundColor }}
                  />
                  <span className={styles.scheduleText}>
                    <strong className={styles.scheduleTitle}>
                      {report.bookTitl}
                    </strong>
                    <span className={styles.scheduleDate}>
                      {formatDisplayDate(report.reportStdt)} -{" "}
                      {formatDisplayDate(report.reportEndt)}
                    </span>
                  </span>
                </button>
              );
            })}
          </section>
        ) : (
          <p className={styles.emptyMessage}>
            선택한 날짜에 읽고 있던 책이 없습니다.
          </p>
        )}
      </Container>
    </main>
  );
}

export default ReadingCalendarPage;
