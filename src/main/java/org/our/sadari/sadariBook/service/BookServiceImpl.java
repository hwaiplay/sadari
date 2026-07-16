package org.our.sadari.sadariBook.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.util.LocaleUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.sadariBook.dto.ReadingGoalDto;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

/**
 * 책과 독후감 등록, 조회, 수정, 삭제 업무 로직을 처리하는 서비스 구현체입니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final ReportMapper reportMapper;
    private final CodeUtil codeUtil;
    private static final DateTimeFormatter GOAL_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final WeekFields GOAL_WEEK_FIELDS = WeekFields.ISO;
    private static final int WEEK_GOAL_MAX_UPDATE_COUNT = 1;
    private static final int MONTH_GOAL_MAX_UPDATE_COUNT = 3;
    private static final int YEAR_GOAL_MAX_UPDATE_COUNT = 5;
    private static final int WEEK_GOAL_LOCK_REMAINING_DAYS = 3;
    private static final int MONTH_GOAL_LOCK_REMAINING_DAYS = 7;

    /**
     * 로그인한 회원의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookKeyword 책 제목 또는 작가 검색어
     * @param sortType 목록 정렬 코드
     * @return 독후감 목록
     */
    @Override
    public List<ReportDto> getBookList(Long userNumb, String bookKeyword, String sortType) {

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookKeyword(StringUtil.normalizePlainText(bookKeyword));
        reportDto.setSortType(normalizeListSortType(sortType));

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("Book report list lookup completed. userNumb={}, size={}", userNumb, list.size());
        return list;
    }

    /**
     * 이번 달과 올해의 완료 독서 권수, 비교 증감, 펼침 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 마이페이지 독서 요약 정보
     */
    @Override
    public MonthlyReadingSummaryDto getMonthlyReadingSummary(Long userNumb) {

        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 1);
        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate currentYearStart = today.withDayOfYear(1);
        LocalDate previousYearStart = currentYearStart.minusYears(1);

        // 이번 달은 오늘 기준 월의 1일부터 다음 달 1일 전까지를 집계 범위로 사용합니다.
        // 이번 주는 ISO 기준 월요일부터 다음 주 월요일 전까지를 집계 범위로 사용합니다.
        MonthlyReadingSummaryDto currentWeekReq = getDoneReportCntByPeriodReq(
                userNumb,
                currentWeekStart,
                currentWeekStart.plusWeeks(1)
        );
        // 지난주 비교값은 직전 월요일부터 이번 주 월요일 전까지 같은 방식으로 계산합니다.
        MonthlyReadingSummaryDto previousWeekReq = getDoneReportCntByPeriodReq(
                userNumb,
                previousWeekStart,
                currentWeekStart
        );
        MonthlyReadingSummaryDto currentMonthReq = getDoneReportCntByPeriodReq(
                userNumb,
                currentMonthStart,
                currentMonthStart.plusMonths(1)
        );
        // 지난 달 비교값은 지난 달 1일부터 이번 달 1일 전까지 같은 방식으로 계산합니다.
        MonthlyReadingSummaryDto previousMonthReq = getDoneReportCntByPeriodReq(
                userNumb,
                previousMonthStart,
                currentMonthStart
        );
        // 올해 집계는 1월 1일부터 다음 해 1월 1일 전까지의 완료 독서를 대상으로 합니다.
        MonthlyReadingSummaryDto currentYearReq = getDoneReportCntByPeriodReq(
                userNumb,
                currentYearStart,
                currentYearStart.plusYears(1)
        );
        // 작년 비교값은 작년 1월 1일부터 올해 1월 1일 전까지의 완료 독서를 대상으로 합니다.
        MonthlyReadingSummaryDto previousYearReq = getDoneReportCntByPeriodReq(
                userNumb,
                previousYearStart,
                currentYearStart
        );

        int currentWeekCount = reportMapper.getDoneReportCntByPeriod(currentWeekReq);
        int previousWeekCount = reportMapper.getDoneReportCntByPeriod(previousWeekReq);
        int currentMonthCount = reportMapper.getDoneReportCntByPeriod(currentMonthReq);
        int previousMonthCount = reportMapper.getDoneReportCntByPeriod(previousMonthReq);
        int currentYearCount = reportMapper.getDoneReportCntByPeriod(currentYearReq);
        int previousYearCount = reportMapper.getDoneReportCntByPeriod(previousYearReq);

        ReadingGoalDto currentWeekGoal = getReadingGoalDtl(userNumb, currentWeekStart, Constant.GOAL_TYPE_WEEK);
        ReadingGoalDto currentMonthGoal = getReadingGoalDtl(userNumb, currentMonthStart, Constant.GOAL_TYPE_MONTH);
        ReadingGoalDto currentYearGoal = getReadingGoalDtl(userNumb, currentYearStart, Constant.GOAL_TYPE_YEAR);

        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();
        summary.setWeekCode(getWeekCode(today));
        summary.setCurrentWeekCount(currentWeekCount);
        summary.setPreviousWeekCount(previousWeekCount);
        summary.setWeekCountDiff(currentWeekCount - previousWeekCount);
        // [주석] 화면 대시보드 헤더 및 타이틀 영역에 노출할 영문 3자리 월 코드 획득: "JAN", "FEB", "MAR" 등
        summary.setMonthCode(today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH));

        // 당월 독후감 작성 건수와 전월 작성 건수를 요약 객체에 세팅합니다.
        summary.setCurrentMonthCount(currentMonthCount);
        summary.setPreviousMonthCount(previousMonthCount);

        // 전월 대비 당월의 독후감 작성 건수 증감 수치(Diff)를 연산하여 세팅합니다. (음수일 경우 감소를 의미)
        summary.setCountDiff(currentMonthCount - previousMonthCount);

        // 조회 기준 년도 코드를 문자열로 변환하여 세팅합니다. (예: "2026")
        summary.setYearCode(String.valueOf(today.getYear()));

        // 금년 독후감 작성 건수와 전년 작성 건수를 요약 객체에 세팅합니다.
        summary.setCurrentYearCount(currentYearCount);
        summary.setPreviousYearCount(previousYearCount);

        // 전년 대비 금년의 독후감 작성 건수 증감 수치(Diff)를 연산하여 세팅합니다. (음수일 경우 감소를 의미)
        summary.setYearCountDiff(currentYearCount - previousYearCount);

        //달성률을 계산
        applyReadingGoal(summary, currentWeekGoal, currentMonthGoal, currentYearGoal);
        applyReadingGoalUpdateMeta(summary, today, currentWeekGoal, currentMonthGoal, currentYearGoal);
        applyReadingGoalAchvCnt(summary, userNumb);

        // 메인 대시보드 화면에 리스트 형식으로 노출할 당월 및 금년 완료 상태의 독후감 목록을 조회하여 바인딩합니다.
        summary.setCurrentWeekReports(reportMapper.getDoneReportListByPeriod(currentWeekReq));
        summary.setCurrentMonthReports(reportMapper.getDoneReportListByPeriod(currentMonthReq));
        summary.setCurrentYearReports(reportMapper.getDoneReportListByPeriod(currentYearReq));
        return summary;
    }

    /**
     * 현재 월 또는 현재 연도에 설정된 독서 목표를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param targetDate 목표 기간 계산 기준일
     * @param goalType 목표 구분 타입
     * @return 독서 목표 정보
     */
    private ReadingGoalDto getReadingGoalDtl(Long userNumb, LocalDate targetDate, String goalType) {
        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalDate(getGoalDate(targetDate, goalType));
        req.setGoalType(goalType);
        return reportMapper.getReadingGoalDtl(req);
    }

    /**
     * 조회된 월간/연간 목표를 독서 요약 DTO에 반영하고 달성률을 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 독서 요약 DTO
     * @param monthGoal 이번 달 목표 정보
     * @param yearGoal 올해 목표 정보
     */
    private void applyReadingGoal(
            MonthlyReadingSummaryDto summary,
            ReadingGoalDto weekGoal,
            ReadingGoalDto monthGoal,
            ReadingGoalDto yearGoal
    ) {
        if (!StringUtil.isEmpty(weekGoal)) {
            summary.setWeekGoalSet(true);
            summary.setWeekGoalCnt(weekGoal.getGoalCnt());
            summary.setWeekGoalRate(getGoalRate(summary.getCurrentWeekCount(), weekGoal.getGoalCnt()));
        }

        if (!StringUtil.isEmpty(monthGoal)) {
            summary.setMonthGoalSet(true);
            summary.setMonthGoalCnt(monthGoal.getGoalCnt());
            summary.setMonthGoalRate(getGoalRate(summary.getCurrentMonthCount(), monthGoal.getGoalCnt()));
        }

        if (!StringUtil.isEmpty(yearGoal)) {
            summary.setYearGoalSet(true);
            summary.setYearGoalCnt(yearGoal.getGoalCnt());
            summary.setYearGoalRate(getGoalRate(summary.getCurrentYearCount(), yearGoal.getGoalCnt()));
        }
    }

    /**
     * 현재 달성 권수와 목표 권수를 기준으로 달성률을 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param doneCount 완료 독서 권수
     * @param goalCount 목표 독서 권수
     * @return 0 이상 100 이하의 달성률
     */
    /**
     * 회원이 과거에 설정한 주간, 월간, 연간 목표 중 실제 완료 독서 권수가 목표 권수를 충족한 횟수를 요약 DTO에 반영합니다.
     * 목표 기간별 실적은 mapper의 인라인 집계 뷰에서 계산하므로 별도의 목표 달성 이력 테이블 없이 현재 목표 테이블과 독후감 테이블만 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 독서 요약 DTO
     * @param userNumb 로그인한 회원 번호
     */
    /**
     * 화면에서 목표 저장 전에 수정 가능 여부를 먼저 판단할 수 있도록 기간별 수정 메타 정보를 채웁니다.
     * 최초 설정은 수정 횟수 제한 대상이 아니므로 아직 목표가 없는 경우에는 최대 수정 가능 횟수를 그대로 내려줍니다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 독서 요약 DTO
     * @param today 현재 날짜
     * @param weekGoal 이번 주 목표 정보
     * @param monthGoal 이번 달 목표 정보
     * @param yearGoal 올해 목표 정보
     */
    private void applyReadingGoalUpdateMeta(
            MonthlyReadingSummaryDto summary,
            LocalDate today,
            ReadingGoalDto weekGoal,
            ReadingGoalDto monthGoal,
            ReadingGoalDto yearGoal
    ) {
        summary.setWeekGoalRemainUpdateCnt(getGoalRemainUpdateCount(weekGoal, Constant.GOAL_TYPE_WEEK));
        summary.setMonthGoalRemainUpdateCnt(getGoalRemainUpdateCount(monthGoal, Constant.GOAL_TYPE_MONTH));
        summary.setYearGoalRemainUpdateCnt(getGoalRemainUpdateCount(yearGoal, Constant.GOAL_TYPE_YEAR));
        summary.setWeekGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_WEEK));
        summary.setMonthGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_MONTH));
        summary.setYearGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_YEAR));
        summary.setWeekGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_WEEK));
        summary.setMonthGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_MONTH));
        summary.setYearGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_YEAR));
    }

    private void applyReadingGoalAchvCnt(MonthlyReadingSummaryDto summary, Long userNumb) {
        int weekGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_WEEK);
        int monthGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_MONTH);
        int yearGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_YEAR);

        summary.setWeekGoalAchvCnt(weekGoalAchvCnt);
        summary.setMonthGoalAchvCnt(monthGoalAchvCnt);
        summary.setYearGoalAchvCnt(yearGoalAchvCnt);
        summary.setTotalGoalAchvCnt(weekGoalAchvCnt + monthGoalAchvCnt + yearGoalAchvCnt);
    }

    /**
     * 목표 타입별 달성 횟수 조회 요청 DTO를 구성하고 mapper에 위임합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param goalType 조회할 목표 타입
     * @return 목표 달성 횟수
     */
    private int getReadingGoalAchvCnt(Long userNumb, String goalType) {
        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalType(goalType);
        return reportMapper.getReadingGoalAchvCnt(req);
    }

    private int getGoalRate(int doneCount, Integer goalCount) {
        if (StringUtil.isEmpty(goalCount) || goalCount <= 0) {
            return 0;
        }

        return Math.min(100, (int) Math.round((doneCount * 100.0) / goalCount));
    }

    /**
     * 목표 구분에 따라 TM_GOALXM.GOAL_DATE 값을 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param targetDate 목표 기간 계산 기준일
     * @param goalType 목표 구분 타입
     * @return 월별은 YYYYMM, 연도별은 YYYY00 형식의 목표 기간 값
     */
    private String getGoalDate(LocalDate targetDate, String goalType) {
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            return getGoalWeekDate(targetDate);
        }

        if (Constant.GOAL_TYPE_YEAR.equals(goalType)) {
            return targetDate.getYear() + "00";
        }

        return YearMonth.from(targetDate).format(GOAL_MONTH_FORMATTER);
    }

    /**
     * 주간 목표 조회와 저장에 사용할 ISO 주차 기반 목표 기간 값을 생성합니다.
     * 연말과 연초가 한 주에 걸치는 경우에도 ISO week-based-year를 사용해 같은 주가 같은 키로 저장되게 합니다.
     *
     * @author Seunghyeon.Kang
     * @param targetDate 목표 기간 계산 기준일
     * @return ISO 주차 기준 YYYYWW 형식의 목표 기간 값
     */
    private String getGoalWeekDate(LocalDate targetDate) {
        int weekYear = targetDate.get(GOAL_WEEK_FIELDS.weekBasedYear());
        int weekNumber = targetDate.get(GOAL_WEEK_FIELDS.weekOfWeekBasedYear());
        return String.format("%04d%02d", weekYear, weekNumber);
    }

    /**
     * 마이페이지 달력 아이콘 안에 표시할 주차 코드를 생성합니다.
     * 월간의 JAN, 연간의 2026처럼 주간도 W29 형태로 짧게 표시하여 카드 폭을 유지합니다.
     *
     * @author Seunghyeon.Kang
     * @param targetDate 주차 표시 기준일
     * @return W와 ISO 주차 번호를 조합한 표시 코드
     */
    private String getWeekCode(LocalDate targetDate) {
        return "WEEK";
    }

    /**
     * 마이페이지에서 설정한 현재 월간/연간 독서 목표를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param readingGoalDto 저장할 목표 권수
     * @return 저장 후 다시 조회한 독서 요약 정보
     */
    @Override
    @Transactional
    public MonthlyReadingSummaryDto setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto) {

        validateReadingGoal(readingGoalDto);
        LocalDate today = LocalDate.now();
        setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_WEEK, readingGoalDto.getWeekGoalCnt());
        setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_MONTH, readingGoalDto.getMonthGoalCnt());
        setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_YEAR, readingGoalDto.getYearGoalCnt());
        return getMonthlyReadingSummary(userNumb);
    }

    /**
     * 월간/연간 목표 입력값이 저장 가능한 권수인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param readingGoalDto 검증할 목표 DTO
     */
    private void validateReadingGoal(ReadingGoalDto readingGoalDto) {

        if (StringUtil.isEmpty(readingGoalDto) || StringUtil.isEmpty(readingGoalDto.getWeekGoalCnt())
            || StringUtil.isEmpty(readingGoalDto.getMonthGoalCnt()) || StringUtil.isEmpty(readingGoalDto.getYearGoalCnt())
            || readingGoalDto.getWeekGoalCnt() <= 0 || readingGoalDto.getMonthGoalCnt() <= 0
            || readingGoalDto.getYearGoalCnt() <= 0
        ) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 목표 타입별로 현재 기간의 독서 목표를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param today 현재 날짜
     * @param goalType 목표 타입
     * @param goalCnt 목표 권수
     */
    private void setReadingGoalByType(Long userNumb, LocalDate today, String goalType, Integer goalCnt) {
        ReadingGoalDto currentGoal = getReadingGoalDtl(userNumb, today, goalType);

        if (!StringUtil.isEmpty(currentGoal) && goalCnt.equals(currentGoal.getGoalCnt())) {
            return;
        }

        validateReadingGoalDown(currentGoal, today, goalType, goalCnt);

        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalDate(getGoalDate(today, goalType));
        req.setGoalType(goalType);
        req.setGoalCnt(goalCnt);
        reportMapper.setReadingGoal(req);
    }

    /**
     * 기존 목표를 실제로 수정하려는 경우 목표 타입별 수정 횟수와 수정 가능 기간을 검증합니다.
     * 최초 설정은 수정 횟수에 포함하지 않으며, 이미 같은 값인 경우에는 저장을 건너뛰므로 수정 횟수를 소모하지 않습니다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 기간에 이미 저장된 목표 정보
     * @param today 현재 날짜
     * @param goalType 목표 타입
     */
    private void validateReadingGoalDown(ReadingGoalDto currentGoal, LocalDate today, String goalType, Integer goalCnt) {
        if (StringUtil.isEmpty(currentGoal) || currentGoal.getGoalCnt() <= goalCnt) {
            return;
        }

        if (getGoalUpdateLimit(goalType) <= getGoalUpdateCount(currentGoal)) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        if (isGoalUpdateLocked(today, goalType)) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 목표 타입별 최대 수정 가능 횟수를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param goalType 목표 타입
     * @return 최대 수정 가능 횟수
     */
    private int getGoalUpdateLimit(String goalType) {
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            return WEEK_GOAL_MAX_UPDATE_COUNT;
        }

        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            return MONTH_GOAL_MAX_UPDATE_COUNT;
        }

        return YEAR_GOAL_MAX_UPDATE_COUNT;
    }

    /**
     * 현재 기간 목표를 앞으로 몇 번 더 수정할 수 있는지 계산합니다.
     * 목표가 아직 생성되지 않은 경우에는 최초 설정 화면 안내를 위해 해당 목표 타입의 최대 수정 횟수를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 기간에 저장된 목표 정보
     * @param goalType 목표 타입
     * @return 남은 수정 가능 횟수
     */
    private int getGoalRemainUpdateCount(ReadingGoalDto currentGoal, String goalType) {
        if (StringUtil.isEmpty(currentGoal)) {
            return getGoalUpdateLimit(goalType);
        }

        return Math.max(0, getGoalUpdateLimit(goalType) - getGoalUpdateCount(currentGoal));
    }

    /**
     * 목표 수정 횟수가 null인 기존 데이터를 0회로 보정해 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 기간에 저장된 목표 정보
     * @return 보정된 수정 횟수
     */
    private int getGoalUpdateCount(ReadingGoalDto currentGoal) {
        return StringUtil.isEmpty(currentGoal.getUpdtCntt()) ? 0 : currentGoal.getUpdtCntt();
    }

    /**
     * 목표 타입별 수정 마감 기간에 들어섰는지 확인합니다.
     * 주간은 해당 주 마지막 날까지 3일 이하, 월간은 해당 월 마지막 날까지 7일 이하, 연간은 12월 1일부터 수정할 수 없습니다.
     *
     * @author Seunghyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 타입
     * @return 수정 마감 기간이면 true
     */
    private boolean isGoalUpdateLocked(LocalDate today, String goalType) {
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            return ChronoUnit.DAYS.between(today, weekLastDay) <= WEEK_GOAL_LOCK_REMAINING_DAYS;
        }

        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            return ChronoUnit.DAYS.between(today, monthLastDay) <= MONTH_GOAL_LOCK_REMAINING_DAYS;
        }

        return today.getMonthValue() == 12;
    }

    /**
     * 목표 수정 제한 시점까지 남은 일수를 계산합니다.
     * 반환값이 0이면 현재 날짜 기준으로 이미 수정 제한 기간에 들어왔다는 의미입니다.
     *
     * @author Seunghyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 타입
     * @return 수정 제한 시점까지 남은 일수
     */
    private int getGoalEditableRemainDays(LocalDate today, String goalType) {
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, weekLastDay) - WEEK_GOAL_LOCK_REMAINING_DAYS);
        }

        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, monthLastDay) - MONTH_GOAL_LOCK_REMAINING_DAYS);
        }

        LocalDate yearLockDate = LocalDate.of(today.getYear(), 12, 1);
        return Math.max(0, (int) ChronoUnit.DAYS.between(today, yearLockDate));
    }

    /**
     * 완료 독서 권수와 목록 조회에 공통으로 사용하는 기간 조건 DTO를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param periodStart 조회 시작일
     * @param periodEndExclusive 조회 종료 경계일
     * @param targetDate 집계 기준일
     * @return 기간 조건 DTO
     */
    private MonthlyReadingSummaryDto getDoneReportCntByPeriodReq(
            Long userNumb,
            LocalDate periodStart,
            LocalDate periodEndExclusive
    ) {
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        req.setUserNumb(userNumb);
        req.setPeriodStart(periodStart.toString());
        req.setPeriodEndExclusive(periodEndExclusive.toString());
        return req;
    }


    /**
     * 목록 정렬 코드를 허용된 값으로 정규화합니다.
     *
     * @author Seunghyeon.Kang
     * @param sortType 요청 정렬 코드
     * @return 허용된 정렬 코드, 없거나 잘못된 값이면 종료일 내림차순
     */
    private String normalizeListSortType(String sortType) {
        // 화면에서 허용하는 시작일순과 별점순만 그대로 사용하고 나머지는 기본 정렬로 고정합니다.
        if (Constant.SORT_START_DATE_DESC.equals(sortType) || Constant.SORT_GRADE_DESC.equals(sortType)) {
            return sortType;
        }

        return Constant.SORT_END_DATE_DESC;
    }

    /**
     * 로그인한 회원의 독후감 상세 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 조회할 독후감 번호
     * @return 독후감 상세 정보
     */
    @Override
    public ReportDto getDetail(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        reportDto.setLocale(LocaleUtil.getLocale());

        return reportMapper.getReportDtl(reportDto);
    }

    /**
     * ISBN 기준 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookIsbn 조회할 ISBN
     * @return 공개 독후감 목록
     */
    @Override
    public List<ReportDto> getPublicReportsByIsbn(Long userNumb, String bookIsbn) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookIsbn(StringUtil.normalizePlainText(bookIsbn));

        return reportMapper.getPublicReportList(reportDto);
    }

    /**
     * ISBN 기준 전체 독후감 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 평균 별점을 조회할 ISBN
     * @return 평균 별점
     */
    @Override
    public BigDecimal getPublicRatingAverageByIsbn(String bookIsbn) {
        return reportMapper.getPublicRatingAverageByIsbn(StringUtil.normalizePlainText(bookIsbn));
    }

    /**
     * 독후감 좋아요를 등록하거나 취소합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경 후 좋아요 상태
     */
    @Override
    @Transactional
    public ReportDto setReportLike(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        // 공개 독후감만 좋아요 대상이므로 비공개 또는 존재하지 않는 독후감은 요청 오류로 처리합니다.
        if (reportMapper.getPublicReportLikeTargetCnt(reportDto) == 0) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        // 이미 좋아요를 누른 상태면 취소하고, 아직 누르지 않은 상태면 등록하는 토글 방식입니다.
        if (reportMapper.dupReportLike(reportDto) > 0) {
            reportMapper.delReportLike(reportDto);
        } else {
            reportMapper.setReportLike(reportDto);
        }

        return reportMapper.getReportLikeDtl(reportDto);
    }

    /**
     * 독후감과 책 기본 정보를 함께 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportDto 저장할 독후감 정보
     * @return 저장된 독후감 정보
     */
    @Override
    @Transactional
    public ReportDto setReport(Long userNumb, ReportDto reportDto) {
        reportDto.setUserNumb(userNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, true);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
        validatePublicFlag(reportDto);
        validateReportContentBytes(reportDto);

        // 같은 ISBN 책이 없으면 책 정보를 먼저 등록하고, 이미 있으면 기존 책 번호를 연결합니다.
        if (reportMapper.dupBook(reportDto) == 0) {
            reportMapper.setBook(reportDto);
        } else {
            reportDto.setBookNumb(reportMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        reportMapper.setReport(reportDto);
        return reportDto;
    }

    /**
     * 로그인한 회원의 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 정보
     */
    @Override
    public ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto) {
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, false);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
        validatePublicFlag(reportDto);
        validateReportContentBytes(reportDto);

        reportMapper.uptReport(reportDto);
        return reportDto;
    }

    /**
     * 로그인한 회원의 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 건수
     */
    @Override
    public int delReport(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.delReport(reportDto);
    }

    /**
     * 책장 색상 코드가 비어 있으면 공통코드의 첫 번째 색상으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 보정할 독후감 DTO
     */
    private void setDefaultReportColor(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getReportColr()) || reportDto.getReportColr().isBlank()) {
            reportDto.setReportColr(codeUtil.getFirstCode(Constant.CODE_BOOK_COLR));
        }
    }

    /**
     * 공개 여부가 비어 있으면 비공개 기본값으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 보정할 독후감 DTO
     */
    private void setDefaultPublicFlag(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getPubcYsno()) || reportDto.getPubcYsno().isBlank()) {
            reportDto.setPubcYsno(Constant.COMM_NO);
        }
    }

    /**
     * 독후감 저장 전 HTML entity 변환 없이 원문 기준 문자열을 정리합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 정리할 독후감 DTO
     * @param includeBookFields 책 정보 필드까지 정리할지 여부
     */
    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {
        reportDto.setReportStat(StringUtil.normalizePlainText(reportDto.getReportStat()));
        reportDto.setReportStdt(StringUtil.normalizePlainText(reportDto.getReportStdt()));
        reportDto.setReportEndt(StringUtil.normalizePlainText(reportDto.getReportEndt()));
        reportDto.setReportGrde(StringUtil.normalizePlainText(reportDto.getReportGrde()));
        reportDto.setReportColr(StringUtil.normalizePlainText(reportDto.getReportColr()));
        reportDto.setPubcYsno(StringUtil.normalizePlainText(reportDto.getPubcYsno()));
        reportDto.setReportCntn(StringUtil.normalizePlainText(reportDto.getReportCntn()));

        // 등록은 책 정보까지 함께 들어오고, 수정은 독후감 정보만 수정하므로 플래그로 처리 범위를 분리합니다.
        if (includeBookFields) {
            reportDto.setBookTitl(StringUtil.normalizePlainText(reportDto.getBookTitl()));
            reportDto.setBookAthr(StringUtil.normalizePlainText(reportDto.getBookAthr()));
            reportDto.setBookPubl(StringUtil.normalizePlainText(reportDto.getBookPubl()));
            reportDto.setBookIsbn(StringUtil.normalizePlainText(reportDto.getBookIsbn()));
            reportDto.setBookCvim(StringUtil.normalizePlainText(reportDto.getBookCvim()));
            reportDto.setBookDesc(StringUtil.normalizePlainText(reportDto.getBookDesc()));
        }
    }

    /**
     * 독후감 내용의 UTF-8 byte 길이가 DB 컬럼 한도를 넘지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportContentBytes(ReportDto reportDto) {
        if (XssUtil.utf8ByteLength(reportDto.getReportCntn()) > Constant.REPORT_CONTENT_MAX_BYTES) {
            throw new CustomException(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 독서 상태 코드가 공통코드에 등록된 값인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportStatus(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_READ_STAT, reportDto.getReportStat())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 책장 색상 코드가 공통코드에 등록된 값인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportColor(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_BOOK_COLR, reportDto.getReportColr())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 공개 여부 값이 Y 또는 N인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validatePublicFlag(ReportDto reportDto) {
        if (!Constant.COMM_YES.equals(reportDto.getPubcYsno()) && !Constant.COMM_NO.equals(reportDto.getPubcYsno())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }
}
