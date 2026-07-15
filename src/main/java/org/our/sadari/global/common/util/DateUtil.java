package org.our.sadari.global.common.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * 날짜 문자열 변환, 검증, 기간 계산에 사용하는 공통 유틸 클래스입니다.
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
        // 공통 유틸 클래스는 상태를 가지지 않으므로 인스턴스 생성을 막습니다.
    }

    /**
     * yyyy-MM-dd 형태의 날짜 문자열을 LocalDate 객체로 변환합니다.
     * 값이 비어 있거나 실제 날짜로 해석할 수 없으면 null을 반환해 호출부가 기본값 또는 예외 처리를 선택할 수 있게 합니다.
     *
     * @author Seunghyeon.Kang
     * @param value yyyy-MM-dd 형태의 날짜 문자열
     * @return 변환된 LocalDate 객체, 변환할 수 없으면 null
     */
    public static LocalDate parseDefaultDate(String value) {
        return parseDate(value, DEFAULT_DATE_FORMATTER);
    }

    /**
     * yyyyMMdd 형태의 날짜 문자열을 LocalDate 객체로 변환합니다.
     * 외부 도서 API의 출간일처럼 구분자가 없는 날짜를 서버에서 검증하거나 표시 문자열로 바꿀 때 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param value yyyyMMdd 형태이거나 숫자 외 문자가 포함될 수 있는 날짜 문자열
     * @return 변환된 LocalDate 객체, 변환할 수 없으면 null
     */
    public static LocalDate parseCompactDate(String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        String compactDate = value.replaceAll("\\D", "");

        if (compactDate.length() != 8) {
            return null;
        }

        return parseDate(compactDate, COMPACT_DATE_FORMATTER);
    }

    /**
     * LocalDate 객체를 yyyy-MM-dd 형태의 날짜 문자열로 변환합니다.
     * 날짜 객체가 null이면 빈 문자열을 반환해 화면에서 "null" 문구가 노출되지 않게 합니다.
     *
     * @author Seunghyeon.Kang
     * @param date 문자열로 변환할 날짜 객체
     * @return yyyy-MM-dd 형태의 날짜 문자열 또는 빈 문자열
     */
    public static String formatDefaultDate(LocalDate date) {
        return formatDate(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * LocalDate 객체를 yyyyMMdd 형태의 날짜 문자열로 변환합니다.
     * 외부 API 값과 비교하거나 구분자 없는 날짜 key를 만들어야 하는 경우 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param date 문자열로 변환할 날짜 객체
     * @return yyyyMMdd 형태의 날짜 문자열 또는 빈 문자열
     */
    public static String formatCompactDate(LocalDate date) {
        return formatDate(date, COMPACT_DATE_FORMATTER);
    }

    /**
     * yyyyMMdd 형태의 날짜 문자열을 "2017년7월6일" 형태의 한국어 날짜 문자열로 변환합니다.
     * 변환할 수 없는 값은 원본을 그대로 반환해 잘못된 날짜를 정상 날짜처럼 보이게 만들지 않습니다.
     *
     * @author Seunghyeon.Kang
     * @param value yyyyMMdd 형태이거나 숫자 외 문자가 포함될 수 있는 날짜 문자열
     * @return 변환 가능한 경우 한국어 날짜 문자열, 변환할 수 없으면 원본 문자열 또는 빈 문자열
     */
    public static String formatCompactDateToKorean(String value) {
        if (StringUtil.isEmpty(value)) {
            return "";
        }

        LocalDate date = parseCompactDate(value);

        if (date == null) {
            return value;
        }

        return formatDate(date, KOREAN_DATE_FORMATTER);
    }

    /**
     * yyyyMMdd 형태의 날짜 문자열을 "August 27th, 2017" 형태의 영어 날짜 문자열로 변환합니다.
     * 영어 화면이나 영문 알림 문구에서 월 이름, ordinal day, 연도 순서의 자연스러운 날짜 표기가 필요할 때 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param value yyyyMMdd 형태이거나 숫자 외 문자가 포함될 수 있는 날짜 문자열
     * @return 변환 가능한 경우 영어 날짜 문자열, 변환할 수 없으면 원본 문자열 또는 빈 문자열
     */
    public static String formatCompactDateToEnglish(String value) {
        if (StringUtil.isEmpty(value)) {
            return "";
        }

        LocalDate date = parseCompactDate(value);

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
     * yyyyMMdd 형태의 날짜 문자열을 전달받은 locale에 맞는 화면 표시용 날짜 문자열로 변환합니다.
     * 한국어 locale이면 한국어 날짜를 사용하고, 그 외 locale은 메시징 fallback과 맞춰 영어 날짜 형식을 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param value yyyyMMdd 형태이거나 숫자 외 문자가 포함될 수 있는 날짜 문자열
     * @param locale 표시 언어를 결정할 Locale 객체
     * @return locale에 맞는 출간일 표시 문자열
     */
    public static String formatCompactDate(String value, Locale locale) {
        if (locale != null && Locale.KOREAN.getLanguage().equals(locale.getLanguage())) {
            return formatCompactDateToKorean(value);
        }

        return formatCompactDateToEnglish(value);
    }

    /**
     * 전달받은 날짜 문자열이 yyyy-MM-dd 형식의 실제 날짜인지 확인합니다.
     * 단순 정규식이 아니라 LocalDate 파싱을 사용해 형식은 맞지만 존재하지 않는 날짜를 걸러냅니다.
     *
     * @author Seunghyeon.Kang
     * @param value 검증할 날짜 문자열
     * @return yyyy-MM-dd 형식의 실제 날짜이면 true, 아니면 false
     */
    public static boolean isDefaultDate(String value) {
        return parseDefaultDate(value) != null;
    }

    /**
     * 기준 날짜가 속한 달의 첫날을 반환합니다.
     * 기준 날짜가 null이면 현재 날짜를 기준으로 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param date 기준 날짜
     * @return 기준 날짜가 속한 달의 첫날
     */
    public static LocalDate getMonthStart(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return targetDate.withDayOfMonth(1);
    }

    /**
     * 기준 날짜가 속한 달의 마지막 날을 반환합니다.
     * YearMonth를 사용해 윤년 2월처럼 월마다 달라지는 마지막 일을 안전하게 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param date 기준 날짜
     * @return 기준 날짜가 속한 달의 마지막 날
     */
    public static LocalDate getMonthEnd(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return YearMonth.from(targetDate).atEndOfMonth();
    }

    /**
     * 기준 날짜가 속한 해의 첫날을 반환합니다.
     * 기준 날짜가 null이면 현재 날짜를 기준으로 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param date 기준 날짜
     * @return 기준 날짜가 속한 해의 첫날
     */
    public static LocalDate getYearStart(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return targetDate.withDayOfYear(1);
    }

    /**
     * 두 날짜 구간이 하루라도 겹치는지 확인합니다.
     * 날짜 중 하나라도 null이면 올바른 구간으로 볼 수 없으므로 false를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param startDate 첫 번째 날짜 구간의 시작일
     * @param endDate 첫 번째 날짜 구간의 종료일
     * @param targetStartDate 비교할 날짜 구간의 시작일
     * @param targetEndDate 비교할 날짜 구간의 종료일
     * @return 두 날짜 구간이 하루라도 겹치면 true, 아니면 false
     */
    public static boolean isDateRangeOverlapped(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate targetStartDate,
            LocalDate targetEndDate
    ) {
        if (StringUtil.hasEmpty(startDate, endDate, targetStartDate, targetEndDate)) {
            return false;
        }

        return !endDate.isBefore(targetStartDate) && !startDate.isAfter(targetEndDate);
    }

    /**
     * 영어 날짜 표기에 사용하는 일자 suffix를 반환합니다.
     * 11, 12, 13은 끝자리가 1, 2, 3이어도 예외적으로 th를 사용하므로 먼저 예외 구간을 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param day suffix를 붙일 일자
     * @return st, nd, rd, th 중 하나의 suffix
     */
    private static String getEnglishOrdinalSuffix(int day) {
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
     * 지정한 DateTimeFormatter로 날짜 문자열을 LocalDate 객체로 변환합니다.
     * 외부로 공개한 파싱 함수들이 동일하게 예외를 삼키고 null을 반환하도록 처리하는 내부 공통 함수입니다.
     *
     * @author Seunghyeon.Kang
     * @param value 변환할 날짜 문자열
     * @param formatter 날짜 문자열을 해석할 DateTimeFormatter
     * @return 변환된 LocalDate 객체, 변환할 수 없으면 null
     */
    private static LocalDate parseDate(String value, DateTimeFormatter formatter) {
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
     * 지정한 DateTimeFormatter로 LocalDate 객체를 문자열로 변환합니다.
     * 외부로 공개한 포맷 함수들이 null 날짜를 빈 문자열로 다루도록 처리하는 내부 공통 함수입니다.
     *
     * @author Seunghyeon.Kang
     * @param date 변환할 날짜 객체
     * @param formatter 날짜 객체를 문자열로 변환할 DateTimeFormatter
     * @return 변환된 날짜 문자열 또는 빈 문자열
     */
    private static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return "";
        }

        return date.format(formatter);
    }
}
