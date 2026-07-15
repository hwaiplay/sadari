import api from "@/app/api/axios";
import { message } from "@/app/messages/message";
import { formatYearMonthValue, isSameLocalDate, parseLocalDate } from "@/app/utils/dateUtil";
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

const WEEKDAY_KEYS = [
  "frontend.calendar.week.sun",
  "frontend.calendar.week.mon",
  "frontend.calendar.week.tue",
  "frontend.calendar.week.wed",
  "frontend.calendar.week.thu",
  "frontend.calendar.week.fri",
  "frontend.calendar.week.sat",
];

/**
 * 달력 상단에 표시할 연월 제목을 만듭니다.
 *
 * @author Hanwon.Jang
 * @param date 제목을 만들 기준 날짜
 * @return 화면 표시용 연월 문자열
 */
function formatMonthTitle(date: Date) {
  return message("frontend.calendar.monthLabel", [
    date.getFullYear(),
    date.getMonth() + 1,
  ]);
}

/**
 * yyyy-MM-dd 형식 날짜를 화면 표시용 점 구분 날짜로 변환합니다.
 *
 * @author Hanwon.Jang
 * @param value 변환할 날짜 문자열
 * @return 점 구분 날짜 문자열
 */
function formatDisplayDate(value: string) {
  return value.replaceAll("-", ".");
}

/**
 * 선택한 날짜를 상세 목록 제목으로 표시할 문자열로 변환합니다.
 *
 * @author Hanwon.Jang
 * @param date 선택한 날짜
 * @return 화면 표시용 날짜 문자열
 */
function formatSelectedDate(date: Date) {
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

/**
 * 독후감의 독서 기간에 지정 날짜가 포함되는지 확인합니다.
 *
 * @author Hanwon.Jang
 * @param report 독서 기간을 가진 독후감 데이터
 * @param date 포함 여부를 확인할 날짜
 * @return 해당 날짜가 독서 기간에 포함되면 true
 */
function isReadingOnDate(report: CalendarReport, date: Date) {
  const start = parseLocalDate(report.reportStdt);
  const end = parseLocalDate(report.reportEndt);
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());

  return start <= target && target <= end;
}

/**
 * 독서 시작일과 독후감 번호를 기준으로 달력 목록 정렬 순서를 계산합니다.
 *
 * @author Hanwon.Jang
 * @param a 비교할 첫 번째 독후감
 * @param b 비교할 두 번째 독후감
 * @return 정렬 비교 결과
 */
function compareReports(a: CalendarReport, b: CalendarReport) {
  const startCompare = parseLocalDate(a.reportStdt).getTime() - parseLocalDate(b.reportStdt).getTime();

  if (startCompare !== 0) {
    return startCompare;
  }

  return a.reportNumb - b.reportNumb;
}

/**
 * 월 화면에 표시할 6주치 날짜 배열을 생성합니다.
 *
 * @author Hanwon.Jang
 * @param month 달력을 구성할 기준 월
 * @return 달력에 표시할 42개 날짜 목록
 */
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

/**
 * 지정 날짜에 읽고 있던 독후감 목록을 조회하고 정렬합니다.
 *
 * @author Hanwon.Jang
 * @param reports 월 범위에서 조회한 독후감 목록
 * @param date 목록을 찾을 기준 날짜
 * @return 지정 날짜에 해당하는 독후감 목록
 */
function getReportsOnDate(reports: CalendarReport[], date: Date) {
  return reports.filter((report) => isReadingOnDate(report, date)).sort(compareReports);
}

/**
 * 월별 독서 기간을 달력에 표시하고 선택 날짜의 독후감 목록을 제공합니다.
 *
 * @author Hanwon.Jang
 * @return 독서 달력 페이지 컴포넌트
 */
function ReadingCalendarPage() {
  const navigate = useNavigate();
  const [currentMonth, setCurrentMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState(() => new Date());
  const [reports, setReports] = useState<CalendarReport[]>([]);
  const yearMonth = formatYearMonthValue(currentMonth);
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
            aria-label={message("frontend.calendar.prevMonth")}
            onClick={() => moveMonth(-1)}
          >
            {"<"}
          </button>
          <h1 className={styles.monthTitle}>{formatMonthTitle(currentMonth)}</h1>
          <button
            className={styles.monthButton}
            type="button"
            aria-label={message("frontend.calendar.nextMonth")}
            onClick={() => moveMonth(1)}
          >
            {">"}
          </button>
        </div>

        <section className={styles.calendar} aria-label={message("frontend.calendar.dateSelect")}>
          {WEEKDAY_KEYS.map((weekdayKey) => (
            <div className={styles.weekday} key={weekdayKey}>
              {message(weekdayKey)}
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
                  isSameLocalDate(day, today) && styles.today,
                  isSameLocalDate(day, selectedDate) && styles.selectedDay,
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
