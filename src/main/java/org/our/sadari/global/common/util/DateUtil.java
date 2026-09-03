package org.our.sadari.global.common.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * fileName       : DateUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-15
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-15        SeungHyeon.Kang    최초 생성
 */
public final class DateUtil {

    // 기본 날짜 FORMATTER 설정값
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            // 날짜 문자열을 해석할 형식 객체를 생성함
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    // 축약 날짜 FORMATTER 설정값
    private static final DateTimeFormatter COMPACT_DATE_FORMATTER =
            // 날짜 문자열을 해석할 형식 객체를 생성함
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    // 한글 날짜 FORMATTER 설정값
    private static final DateTimeFormatter KOREAN_DATE_FORMATTER =
            // 날짜 문자열을 해석할 형식 객체를 생성함
            DateTimeFormatter.ofPattern("uuuu'\uB144'M'\uC6D4'd'\uC77C'").withResolverStyle(ResolverStyle.STRICT);
    // 영문 월간 명칭 설정값
    private static final String[] ENGLISH_MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private DateUtil() {
        // 아래 처리 단계의 업무 목적을 설명함
    }

    /**
     * yyyy-MM-dd 문자열의 날지 파싱함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static LocalDate parseDefaultDate(String value) {
        // yyyy-MM-dd 문자열의 날지 파싱 결과를 반환함
        return parseDate(value, DEFAULT_DATE_FORMATTER);
    }

    /**
     * yyyyMMdd 문자열의 날짜 파싱함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static LocalDate parseCompactDate(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // 조회하거나 생성할 값이 없음을 반환함
            return null;
        }

        // 정규식과 일치하는 문자열을 일괄 치환함
        String compactDate = value.replaceAll("\\D", "");

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기함
        if (compactDate.length() != 8) {
            // 조회하거나 생성할 값이 없음을 반환함
            return null;
        }

        // yyyyMMdd 문자열의 날짜 파싱 결과를 반환함
        return parseDate(compactDate, COMPACT_DATE_FORMATTER);
    }

    /**
     * yyyy-MM-dd 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @return 처리 결과
     */
    public static String formatDefaultDate(LocalDate date) {
        // yyyy-MM-dd 날짜 문자열 변환 결과를 반환함
        return formatDate(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * yyyyMMdd 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @return 처리 결과
     */
    public static String formatCompactDate(LocalDate date) {
        // yyyyMMdd 날짜 문자열 변환 결과를 반환함
        return formatDate(date, COMPACT_DATE_FORMATTER);
    }

    /**
     * 한글 표기 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static String formatCompactDateToKorean(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // 한글 표기 날짜 문자열 변환 결과를 반환함
            return "";
        }

        // yyyyMMdd 형식의 문자열을 날짜 객체로 변환함
        LocalDate date = parseCompactDate(value);

        // date 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(date)) {
            // 한글 표기 날짜 문자열 변환 결과를 반환함
            return value;
        }

        // 한글 표기 날짜 문자열 변환 결과를 반환함
        return formatDate(date, KOREAN_DATE_FORMATTER);
    }

    /**
     * 영문 표기 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static String formatCompactDateEnglish(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // 영문 표기 날짜 문자열 변환 결과를 반환함
            return "";
        }

        // yyyyMMdd 형식의 문자열을 날짜 객체로 변환함
        LocalDate date = parseCompactDate(value);

        // date 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(date)) {
            // 영문 표기 날짜 문자열 변환 결과를 반환함
            return value;
        }

        // 영문 표기 날짜 문자열 변환 결과를 반환함
        return ENGLISH_MONTH_NAMES[date.getMonthValue() - 1]
                + " "
                // getDayOfMonth 조회로 후속 처리에 필요한 데이터를 가져옴
                + date.getDayOfMonth()
                // getDayOfMonth 조회로 후속 처리에 필요한 데이터를 가져옴
                + getEnglishOrdinalSuffix(date.getDayOfMonth())
                + ", "
                // getYear 조회로 후속 처리에 필요한 데이터를 가져옴
                + date.getYear();
    }

    /**
     * yyyyMMdd 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @param locale 메시지와 날짜 표시에 사용할 언어 환경
     * @return 처리 결과
     */
    public static String formatCompactDate(String value, Locale locale) {
        // locale 값이 존재할 때만 관련 업무를 수행하도록 분기함
        if (!StringUtil.isEmpty(locale) && Locale.KOREAN.getLanguage().equals(locale.getLanguage())) {
            // yyyyMMdd 날짜 문자열 변환 결과를 반환함
            return formatCompactDateToKorean(value);
        }

        // yyyyMMdd 날짜 문자열 변환 결과를 반환함
        return formatCompactDateEnglish(value);
    }

    /**
     * yyyy-MM-dd 날짜 형식 유효성 판정함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static boolean isDefaultDate(String value) {
        // yyyy-MM-dd 날짜 형식 유효성 판정 결과를 반환함
        return !StringUtil.isEmpty(parseDefaultDate(value));
    }

    /**
     * 독서 시작일과 종료일 범위 검증함
     *
     * @author SeungHyeon.Kang
     * @param startDate 기간 계산의 시작일
     * @param endDate 기간 계산의 종료일
     * @return 처리 결과
     */
    public static boolean validateReportDateRange(String startDate, String endDate) {
        // 기본 날짜 형식의 문자열을 날짜 객체로 변환함
        LocalDate parsedStartDate = parseDefaultDate(startDate);
        // 기본 날짜 형식의 문자열을 날짜 객체로 변환함
        LocalDate parsedEndDate = parseDefaultDate(endDate);

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기함
        if (StringUtil.hasEmpty(parsedStartDate, parsedEndDate)) {
            // 독서 시작일과 종료일 범위 검증 판정값을 반환함
            return false;
        }

        // 독서 시작일과 종료일 범위 검증 결과를 반환함
        return !parsedStartDate.isAfter(parsedEndDate);
    }

    /**
     * 기준일이 속한 월의 첫 날 계산함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @return 처리 결과
     */
    public static LocalDate getMonthStart(LocalDate date) {
        // 필수 값이 비어 있는지 공통 기준으로 확인함
        LocalDate targetDate = StringUtil.isEmpty(date) ? LocalDate.now() : date;
        // 기준일이 속한 월의 첫 날 계산 결과를 반환함
        return targetDate.withDayOfMonth(1);
    }

    /**
     * 기준일이 속한 월의 마지막 날 계산함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @return 처리 결과
     */
    public static LocalDate getMonthEnd(LocalDate date) {
        // 필수 값이 비어 있는지 공통 기준으로 확인함
        LocalDate targetDate = StringUtil.isEmpty(date) ? LocalDate.now() : date;
        // 기준일이 속한 월의 마지막 날 계산 결과를 반환함
        return YearMonth.from(targetDate).atEndOfMonth();
    }

    /**
     * 기준일이 속한 연도의 첫 날 계산함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @return 처리 결과
     */
    public static LocalDate getYearStart(LocalDate date) {
        // 필수 값이 비어 있는지 공통 기준으로 확인함
        LocalDate targetDate = StringUtil.isEmpty(date) ? LocalDate.now() : date;
        // 기준일이 속한 연도의 첫 날 계산 결과를 반환함
        return targetDate.withDayOfYear(1);
    }

    /**
     * 두 날짜 기간의 겹침 여부 판정함
     *
     * @author SeungHyeon.Kang
     * @param startDate 기간 계산의 시작일
     * @param endDate 기간 계산의 종료일
     * @param targetStartDate 비교 대상 기간의 시작일
     * @param targetEndDate 비교 대상 기간의 종료일
     * @return 처리 결과
     */
    public static boolean isDateRangeOverlapped(LocalDate startDate, LocalDate endDate, LocalDate targetStartDate
                                              , LocalDate targetEndDate) {
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기함
        if (StringUtil.hasEmpty(startDate, endDate, targetStartDate, targetEndDate)) {
            // 두 날짜 기간의 겹침 여부 판정값을 반환함
            return false;
        }

        // 두 날짜 기간의 겹침 여부 판정 결과를 반환함
        return !endDate.isBefore(targetStartDate) && !startDate.isAfter(targetEndDate);
    }

    /**
     * 영문 일자 서수 접미사 조회함
     *
     * @author SeungHyeon.Kang
     * @param day 영문 요일명으로 변환할 요일
     * @return 처리 결과
     */
    private static String getEnglishOrdinalSuffix(int day) {
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기함
        if (day >= 11 && day <= 13) {
            // 영문 일자 서수 접미사 조회 결과를 반환함
            return "th";
        }

        // 입력 코드나 날짜 값별로 서로 다른 업무 규칙을 적용하기 위해 분기함
        switch (day % 10) {
            // 현재 선택된 코드 값에 해당하는 결과를 결정함
            case 1:
                // 영문 일자 서수 접미사 조회 결과를 반환함
                return "st";
            // 현재 선택된 코드 값에 해당하는 결과를 결정함
            case 2:
                // 영문 일자 서수 접미사 조회 결과를 반환함
                return "nd";
            // 현재 선택된 코드 값에 해당하는 결과를 결정함
            case 3:
                // 영문 일자 서수 접미사 조회 결과를 반환함
                return "rd";
            // 현재 선택된 코드 값에 해당하는 결과를 결정함
            default:
                // 영문 일자 서수 접미사 조회 결과를 반환함
                return "th";
        }
    }

    /**
     * 지정 형식 문자열의 날지 파싱함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @param formatter 날짜 파싱과 표시에 사용할 형식
     * @return 처리 결과
     */
    private static LocalDate parseDate(String value, DateTimeFormatter formatter) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // 조회하거나 생성할 값이 없음을 반환함
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록임
        try {
            // 지정 형식 문자열의 날지 파싱 결과를 반환함
            return LocalDate.parse(value, formatter);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환함
        catch (DateTimeParseException e) {
            // 조회하거나 생성할 값이 없음을 반환함
            return null;
        }
    }

    /**
     * 지정 형식 날짜 문자열 변환함
     *
     * @author SeungHyeon.Kang
     * @param date 계산하거나 표시할 날짜
     * @param formatter 날짜 파싱과 표시에 사용할 형식
     * @return 처리 결과
     */
    private static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        // date 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(date)) {
            // 지정 형식 날짜 문자열 변환 결과를 반환함
            return "";
        }

        // 지정 형식 날짜 문자열 변환 결과를 반환함
        return date.format(formatter);
    }
}
