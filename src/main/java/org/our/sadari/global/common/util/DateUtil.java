package org.our.sadari.global.common.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * DateUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public final class DateUtil {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter COMPACT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter KOREAN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu'\uB144'M'\uC6D4'd'\uC77C'").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] ENGLISH_MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private DateUtil() {
        // 아래 처리 단계의 업무 목적을 설명한다.
    }

    /**
     * parseDefaultDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static LocalDate parseDefaultDate(String value) {
        return parseDate(value, DEFAULT_DATE_FORMATTER);
    }

    /**
     * parseCompactDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static LocalDate parseCompactDate(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        String compactDate = value.replaceAll("\\D", "");

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (compactDate.length() != 8) {
            return null;
        }

        return parseDate(compactDate, COMPACT_DATE_FORMATTER);
    }

    /**
     * formatDefaultDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String formatDefaultDate(LocalDate date) {
        return formatDate(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * formatCompactDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String formatCompactDate(LocalDate date) {
        return formatDate(date, COMPACT_DATE_FORMATTER);
    }

    /**
     * formatCompactDateToKorean 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String formatCompactDateToKorean(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(value)) {
            return "";
        }

        LocalDate date = parseCompactDate(value);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (date == null) {
            return value;
        }

        return formatDate(date, KOREAN_DATE_FORMATTER);
    }

    /**
     * formatCompactDateToEnglish 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String formatCompactDateToEnglish(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(value)) {
            return "";
        }

        LocalDate date = parseCompactDate(value);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (date == null) {
            return value;
        }

        return ENGLISH_MONTH_NAMES[date.getMonthValue() - 1]
                + " "
                + date.getDayOfMonth()
                + getEnglishOrdinalSuffix(date.getDayOfMonth())
                + ", "
                + date.getYear();
    }

    /**
     * formatCompactDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @param locale 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String formatCompactDate(String value, Locale locale) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (locale != null && Locale.KOREAN.getLanguage().equals(locale.getLanguage())) {
            return formatCompactDateToKorean(value);
        }

        return formatCompactDateToEnglish(value);
    }

    /**
     * isDefaultDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static boolean isDefaultDate(String value) {
        return parseDefaultDate(value) != null;
    }

    /**
     * validateReportDateRange 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param startDate 처리에 필요한 입력값
     * @param endDate 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static boolean validateReportDateRange(String startDate, String endDate) {
        LocalDate parsedStartDate = parseDefaultDate(startDate);
        LocalDate parsedEndDate = parseDefaultDate(endDate);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.hasEmpty(parsedStartDate, parsedEndDate)) {
            return false;
        }

        return !parsedStartDate.isAfter(parsedEndDate);
    }

    /**
     * getMonthStart 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static LocalDate getMonthStart(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return targetDate.withDayOfMonth(1);
    }

    /**
     * getMonthEnd 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static LocalDate getMonthEnd(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return YearMonth.from(targetDate).atEndOfMonth();
    }

    /**
     * getYearStart 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static LocalDate getYearStart(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return targetDate.withDayOfYear(1);
    }

    /**
     * isDateRangeOverlapped 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param startDate 처리에 필요한 입력값
     * @param endDate 처리에 필요한 입력값
     * @param targetStartDate 처리에 필요한 입력값
     * @param targetEndDate 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static boolean isDateRangeOverlapped(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate targetStartDate,
            LocalDate targetEndDate
    ) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.hasEmpty(startDate, endDate, targetStartDate, targetEndDate)) {
            return false;
        }

        return !endDate.isBefore(targetStartDate) && !startDate.isAfter(targetEndDate);
    }

    /**
     * getEnglishOrdinalSuffix 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param day 처리에 필요한 입력값
     * @return 처리 결과
     */
    private static String getEnglishOrdinalSuffix(int day) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
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

    /**
     * parseDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @param formatter 처리에 필요한 입력값
     * @return 처리 결과
     */
    private static LocalDate parseDate(String value, DateTimeFormatter formatter) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        try {
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * formatDate 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param date 처리에 필요한 입력값
     * @param formatter 처리에 필요한 입력값
     * @return 처리 결과
     */
    private static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (date == null) {
            return "";
        }

        return date.format(formatter);
    }
}
