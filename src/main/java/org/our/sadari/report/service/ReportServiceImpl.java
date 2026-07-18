package org.our.sadari.report.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.LocaleUtil;
import org.our.sadari.global.common.util.DateUtil;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 독후감 등록, 수정, 삭제와 공개 독후감, 좋아요, 마이페이지 독서 목표 기능을 처리하는 Service 구현체이다.
 * Controller에서 전달받은 요청을 검증하고 Mapper 호출에 필요한 DTO를 구성한다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final BookMapper bookMapper;
    private final CodeUtil codeUtil;
    private static final DateTimeFormatter GOAL_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final WeekFields GOAL_WEEK_FIELDS = WeekFields.ISO;
    private static final int WEEK_GOAL_MAX_UPDATE_COUNT = 1;
    private static final int MONTH_GOAL_MAX_UPDATE_COUNT = 3; // 월간 목표는 한 달 단위로 조정하므로 목표 내리기를 최대 3회까지 허용한다.
    private static final int YEAR_GOAL_MAX_UPDATE_COUNT = 5; // 연간 목표는 장기 목표이므로 목표 내리기를 최대 5회까지 허용한다.
    private static final int WEEK_GOAL_LOCK_REMAINING_DAYS = 3; // 주간 목표는 해당 주가 3일 남은 시점부터 목표 내리기를 잠근다.
    private static final int MONTH_GOAL_LOCK_REMAINING_DAYS = 7; // 월간 목표는 해당 월이 7일 남은 시점부터 목표 내리기를 잠근다.
    private static final String SUMMARY_REPORT_ORDER_END_DATE_DESC = "END_DATE_DESC";
    private static final String SUMMARY_REPORT_ORDER_END_DATE_ASC = "END_DATE_ASC";
    private static final String REPORT_FIELD_STATUS_KEY = "common.report.field.status";
    private static final String REPORT_FIELD_START_DATE_KEY = "common.report.field.startDate";
    private static final String REPORT_FIELD_END_DATE_KEY = "common.report.field.endDate";
    private static final String REPORT_FIELD_GRADE_KEY = "common.report.field.grade";
    private static final String REPORT_FIELD_COLOR_KEY = "common.report.field.color";
    private static final String REPORT_FIELD_CONTENT_KEY = "common.report.field.content";

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회한다.
     * 검색어는 HTML entity를 일반 텍스트로 보정한 뒤 Mapper에 전달한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @return 독후감 목록 조회 결과
     */
    @Override
    public ResultData getBookList(Long userNumb, String bookKeyword, String sortType) {

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookKeyword(StringUtil.normalizePlainText(bookKeyword));
        reportDto.setSortType(normalizeListSortType(sortType));
        reportDto.setReportStat(Constant.REPORT_STAT_READ);

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        return ResultData.success(list);
    }

    /**
     * 마이페이지에 표시할 주간, 월간, 연간 독서량 요약과 목표 달성 정보를 조회한다.
     * 현재 기간과 직전 기간을 같은 기준으로 비교해 증감값과 펼침 목록을 함께 구성한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 독서량 요약, 목표 달성률, 목표 달성 횟수, 기간별 독후감 목록
     */
    @Override
    public ResultData getMonthlyReadingSummary(Long userNumb) {

        // ==========================================
        // 1. 기준 날짜 정의 및 기간별 시작일 계산
        // ==========================================

        // 현재 날짜를 기준으로 설정 (모든 기간 계산의 원천 데이터)
        LocalDate today = LocalDate.now();

        // 현재 주의 시작일(일요일 혹은 월요일, 설정된 GOAL_WEEK_FIELDS 기준) 계산
        LocalDate currentWeekStart = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 1);

        // 현재 주 시작일에서 딱 1주일(7일)을 감산하여 직전 주의 시작일을 계산
        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);

        // 현재 월의 1일을 시작일로 설정
        LocalDate currentMonthStart = today.withDayOfMonth(1);

        // 현재 월 시작일에서 딱 1달을 감산하여 직전 월의 시작일(1일)을 계산
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);

        // 현재 연도의 1월 1일을 시작일로 설정
        LocalDate currentYearStart = today.withDayOfYear(1);

        // 현재 연도 시작일에서 딱 1년을 감산하여 직전 연도의 시작일(1월 1일)을 계산
        LocalDate previousYearStart = currentYearStart.minusYears(1);


        // ==========================================
        // 2. 기간별 집계 요청용 DTO 매개변수 빌드
        // ==========================================

        // [기간 경계 규칙] 이상(>=) ~ 미만(<) 구조를 일관되게 적용하여 데이터의 누락이나 중복 집계를 방지함
        // 예: 이번 주 집계는 '이번 주 시작일(이상)'부터 '다음 주 시작일(미만)' 즉, 이번 주 마지막 날짜의 23시 59분 59초까지 포함하게 됨

        // 이번 주 집계 범위: [currentWeekStart] <= 독서 완료일 < [currentWeekStart + 1주]
        MonthlyReadingSummaryDto currentWeekReq = getSummaryReportReq(
                userNumb, currentWeekStart, currentWeekStart.plusWeeks(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 직전 주 집계 범위: [previousWeekStart] <= 독서 완료일 < [currentWeekStart] (이번 주 시작일 직전까지)
        MonthlyReadingSummaryDto previousWeekReq = getSummaryReportReq(
                userNumb, previousWeekStart, currentWeekStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 이번 달 집계 범위: [currentMonthStart] <= 독서 완료일 < [currentMonthStart + 1달]
        MonthlyReadingSummaryDto currentMonthReq = getSummaryReportReq(
                userNumb, currentMonthStart, currentMonthStart.plusMonths(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 직전 달 집계 범위: [previousMonthStart] <= 독서 완료일 < [currentMonthStart] (이번 달 시작일 직전까지)
        MonthlyReadingSummaryDto previousMonthReq = getSummaryReportReq(
                userNumb, previousMonthStart, currentMonthStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 올해 집계 범위: [currentYearStart] <= 독서 완료일 < [currentYearStart + 1년]
        MonthlyReadingSummaryDto currentYearReq = getSummaryReportReq(
                userNumb, currentYearStart, currentYearStart.plusYears(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 작년 집계 범위: [previousYearStart] <= 독서 완료일 < [currentYearStart] (올해 시작일 직전까지)
        MonthlyReadingSummaryDto previousYearReq = getSummaryReportReq(
                userNumb, previousYearStart, currentYearStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        MonthlyReadingSummaryDto currentReadingReq = getSummaryReportReq(
                userNumb, Constant.REPORT_STAT_READ, SUMMARY_REPORT_ORDER_END_DATE_ASC);


        // ==========================================
        // 3. DB 조회 (완료 상태의 독후감 개수 집계)
        // ==========================================

        // 데이터 정합성을 위해 임시 저장(TEMP) 등이 아닌, 작성이 완전히 완료된(REPORT_STAT = 'DONE') 독후감만 DB에서 카운트함
        int currentWeekCount = reportMapper.getReportCntByPeriod(currentWeekReq);
        int previousWeekCount = reportMapper.getReportCntByPeriod(previousWeekReq);
        int currentMonthCount = reportMapper.getReportCntByPeriod(currentMonthReq);
        int previousMonthCount = reportMapper.getReportCntByPeriod(previousMonthReq);
        int currentYearCount = reportMapper.getReportCntByPeriod(currentYearReq);
        int previousYearCount = reportMapper.getReportCntByPeriod(previousYearReq);


        // ==========================================
        // 4. 기간별 읽기 목표(Reading Goal) 상세 정보 조회
        // ==========================================

        // 사용자가 설정한 목표 권수를 조회하여 달성률을 계산할 수 있도록 함 (목표를 아예 설정하지 않은 상태도 null 처리를 통해 화면에서 인지 가능)
        ReadingGoalDto currentWeekGoal = getReadingGoalDtl(userNumb, currentWeekStart, Constant.GOAL_TYPE_WEEK);
        ReadingGoalDto currentMonthGoal = getReadingGoalDtl(userNumb, currentMonthStart, Constant.GOAL_TYPE_MONTH);
        ReadingGoalDto currentYearGoal = getReadingGoalDtl(userNumb, currentYearStart, Constant.GOAL_TYPE_YEAR);


        // ==========================================
        // 5. 화면 표시용 통합 요약 DTO 구성 및 전후 비교값 산출
        // ==========================================

        // 화면 뷰(UI)에서 현재 값, 이전 값, 그리고 성장세를 나타내는 증감량(Diff)을 한 번에 보여줄 수 있도록 가공하여 바인딩함
        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();

        // 주간 데이터 바인딩 및 차이값(이번 주 완료 건수 - 지난 주 완료 건수) 계산
        summary.setWeekCode(Constant.GOAL_TYPE_WEEK);
        summary.setCurrentWeekCount(currentWeekCount);
        summary.setPreviousWeekCount(previousWeekCount);
        summary.setWeekCountDiff(currentWeekCount - previousWeekCount);

        // 월간 데이터 바인딩: 월 코드명을 영문 3자리 대문자로 변환하여 지정 (예: 'JULY' -> 'JUL')
        summary.setMonthCode(today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH));
        summary.setCurrentMonthCount(currentMonthCount);
        summary.setPreviousMonthCount(previousMonthCount);
        summary.setCountDiff(currentMonthCount - previousMonthCount);

        // 연간 데이터 바인딩: 현재 연도 숫자를 문자열 코드로 변환하여 지정 (예: '2026')
        summary.setYearCode(String.valueOf(today.getYear()));
        summary.setCurrentYearCount(currentYearCount);
        summary.setPreviousYearCount(previousYearCount);
        summary.setYearCountDiff(currentYearCount - previousYearCount);


        // ==========================================
        // 6. 도메인 로직 기반 세부 메타데이터 주입
        // ==========================================

        // 1) 조회한 목표 권수 정보와 달성 여부를 요약 DTO에 바인딩
        applyReadingGoal(summary, currentWeekGoal, currentMonthGoal, currentYearGoal);

        // 2) 목표 수정 제한 정보 계산 및 바인딩 (특정 시점이 지나면 수정 버튼을 비활성화하는 등의 비즈니스 규칙 처리)
        applyReadingGoalUpdateMeta(summary, today, currentWeekGoal, currentMonthGoal, currentYearGoal);

        // 3) 해당 사용자의 역대 누적 전체 목표 달성 횟수(배지나 통계용 데이터) 계산 및 바인딩
        applyReadingGoalAchvCnt(summary, userNumb);


        // ==========================================
        // 7. 상세 목록 매핑 및 결과 반환
        // ==========================================

        // 사용자가 요약 카드 영역을 펼쳤을 때(Accordion 등) 즉시 책 목록을 렌더링할 수 있도록 상세 독후감 리스트도 함께 포함하여 응답함
        summary.setCurrentWeekReports(reportMapper.getSummaryReportList(currentWeekReq));
        summary.setCurrentMonthReports(reportMapper.getSummaryReportList(currentMonthReq));
        summary.setCurrentYearReports(reportMapper.getSummaryReportList(currentYearReq));
        summary.setCurrentReadingReports(reportMapper.getSummaryReportList(currentReadingReq));

        // 가공 완료된 최종 요약 데이터를 성공 상태 포맷으로 감싸서 컨트롤러로 반환
        return ResultData.success(summary);
    }

    /**
     * 목표 기준일과 목표 유형을 DB 조회용 GOAL_DATE 값으로 변환해 현재 목표를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param targetDate 목표 기준일
     * @param goalType 주간, 월간, 연간 목표 유형
     * @return 저장된 목표 정보, 없으면 null
     */
    private ReadingGoalDto getReadingGoalDtl(Long userNumb, LocalDate targetDate, String goalType) {
        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalDate(getGoalDate(targetDate, goalType));
        req.setGoalType(goalType);
        return reportMapper.getReadingGoalDtl(req);
    }

    /**
     * 조회된 목표를 요약 DTO에 반영하고 현재 독서량 기준 달성률을 계산한다.
     * 목표가 없는 유형은 화면에서 목표 설정 버튼을 노출할 수 있도록 설정 여부를 false로 유지한다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param weekGoal 현재 주간 목표
     * @param monthGoal 현재 월간 목표
     * @param yearGoal 현재 연간 목표
     */
    private void applyReadingGoal(MonthlyReadingSummaryDto summary, ReadingGoalDto weekGoal,
                                  ReadingGoalDto monthGoal, ReadingGoalDto yearGoal) {

        // 주간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(weekGoal)) {
            summary.setWeekGoalSet(true);
            summary.setWeekGoalCnt(weekGoal.getGoalCnt());
            summary.setWeekGoalRate(getGoalRate(summary.getCurrentWeekCount(), weekGoal.getGoalCnt()));
        }

        // 월간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(monthGoal)) {
            summary.setMonthGoalSet(true);
            summary.setMonthGoalCnt(monthGoal.getGoalCnt());
            summary.setMonthGoalRate(getGoalRate(summary.getCurrentMonthCount(), monthGoal.getGoalCnt()));
        }

        // 연간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(yearGoal)) {
            summary.setYearGoalSet(true);
            summary.setYearGoalCnt(yearGoal.getGoalCnt());
            summary.setYearGoalRate(getGoalRate(summary.getCurrentYearCount(), yearGoal.getGoalCnt()));
        }
    }

    /**
     * 목표 내리기 잔여 횟수와 기간 마감 여부를 요약 DTO에 반영한다.
     * 프론트 모달은 이 값을 사용해 내리기 버튼 상태와 안내 문구를 결정한다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param today 현재 날짜
     * @param weekGoal 현재 주간 목표
     * @param monthGoal 현재 월간 목표
     * @param yearGoal 현재 연간 목표
     */
    private void applyReadingGoalUpdateMeta(MonthlyReadingSummaryDto summary, LocalDate today,
                                            ReadingGoalDto weekGoal, ReadingGoalDto monthGoal,
                                            ReadingGoalDto yearGoal) {

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

    /**
     * 과거 전체 기간에서 목표를 실제로 달성한 횟수를 주간, 월간, 연간으로 나누어 반영한다.
     * 현재 기간만 보는 독서량 요약과 달리 성공 횟수는 사용자의 전체 이력을 기준으로 집계한다.
     *
     * @author Seunghyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param userNumb 로그인 사용자 번호
     */
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
     * 목표 유형 하나에 대한 전체 달성 횟수를 Mapper를 통해 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param goalType 목표 유형
     * @return 목표 달성 횟수
     */
    private int getReadingGoalAchvCnt(Long userNumb, String goalType) {
        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalType(goalType);
        req.setReportStat(Constant.REPORT_STAT_DONE);
        return reportMapper.getReadingGoalAchvCnt(req);
    }

    /**
     * 완료 독후감 수와 목표 권수를 비교해 화면 표시용 달성률을 계산한다.
     * 100%를 넘는 경우에도 진행 막대는 최대값으로 표시해야 하므로 100으로 제한한다.
     *
     * @author Seunghyeon.Kang
     * @param doneCount 완료 독후감 수
     * @param goalCount 목표 권수
     * @return 0부터 100까지의 달성률
     */
    private int getGoalRate(int doneCount, Integer goalCount) {
        // 목표 권수가 없거나 0 이하이면 달성률 계산이 불가능하므로 0%로 처리한다.
        if (StringUtil.isEmpty(goalCount) || goalCount <= 0) {
            return 0;
        }

        return Math.min(100, (int) Math.round((doneCount * 100.0) / goalCount));
    }

    /**
     * 목표 유형에 따라 TM_GOALXM.GOAL_DATE에 저장할 기준값을 만든다.
     * 주간은 ISO week 기준 YYYYWW, 월간은 YYYYMM, 연간은 YYYY00 형식을 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param targetDate 목표 기준일
     * @param goalType 목표 유형
     * @return 목표 기준값
     */
    private String getGoalDate(LocalDate targetDate, String goalType) {
        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            return getGoalWeekDate(targetDate);
        }

        // 연간 목표는 월 정보가 필요 없으므로 YYYY00 형식으로 저장한다.
        if (Constant.GOAL_TYPE_YEAR.equals(goalType)) {
            return targetDate.getYear() + "00";
        }

        return YearMonth.from(targetDate).format(GOAL_MONTH_FORMATTER);
    }

    /**
     * ISO 주차 기준으로 주간 목표의 GOAL_DATE 값을 생성한다.
     * 연말과 연초가 겹치는 주차를 올바르게 처리하기 위해 week-based-year를 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param targetDate 목표 기준일
     * @return YYYYWW 형식의 주간 목표 기준값
     */
    private String getGoalWeekDate(LocalDate targetDate) {
        int weekYear = targetDate.get(GOAL_WEEK_FIELDS.weekBasedYear());
        int weekNumber = targetDate.get(GOAL_WEEK_FIELDS.weekOfWeekBasedYear());
        return String.format("%04d%02d", weekYear, weekNumber);
    }

            /**
     * 주간, 월간, 연간 독서 목표를 한 번에 저장한다.
     * 목표를 올리는 것은 항상 허용하고, 목표를 낮추는 경우에만 기간과 횟수 제한을 적용한다.
     *
     * @author Seunghyeon.Kang
     * @param readingGoalDto 저장할 주간, 월간, 연간 목표 권수
     * @return 저장 후 갱신된 마이페이지 독서 요약 정보
     */
    private boolean isValidReadingGoal(ReadingGoalDto readingGoalDto) {
        return !(StringUtil.isEmpty(readingGoalDto) || StringUtil.isEmpty(readingGoalDto.getWeekGoalCnt())
                || StringUtil.isEmpty(readingGoalDto.getMonthGoalCnt()) || StringUtil.isEmpty(readingGoalDto.getYearGoalCnt())
                || readingGoalDto.getWeekGoalCnt() <= 0 || readingGoalDto.getMonthGoalCnt() <= 0
                || readingGoalDto.getYearGoalCnt() <= 0);
    }

    @Override
    @Transactional
    public ResultData setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto) {

        // 주간, 월간, 연간 목표 중 하나라도 유효하지 않으면 저장 요청 전체를 거절한다.
        if (!isValidReadingGoal(readingGoalDto)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        LocalDate today = LocalDate.now();
        ResultEnum weekResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_WEEK, readingGoalDto.getWeekGoalCnt());
        // 주간 목표 저장 중 제한 규칙에 걸리면 이후 월간, 연간 저장을 진행하지 않는다.
        if (!StringUtil.isEmpty(weekResult)) {
            return ResultData.fail(weekResult);
        }

        ResultEnum monthResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_MONTH, readingGoalDto.getMonthGoalCnt());
        // 월간 목표 저장 중 제한 규칙에 걸리면 이후 연간 저장을 진행하지 않는다.
        if (!StringUtil.isEmpty(monthResult)) {
            return ResultData.fail(monthResult);
        }

        ResultEnum yearResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_YEAR, readingGoalDto.getYearGoalCnt());
        // 연간 목표 저장 중 제한 규칙에 걸리면 실패 결과를 그대로 반환한다.
        if (!StringUtil.isEmpty(yearResult)) {
            return ResultData.fail(yearResult);
        }

        return getMonthlyReadingSummary(userNumb);
    }

    /**
     * 목표 유형 하나에 대해 현재 목표와 신규 목표를 비교한 뒤 저장한다.
     * 같은 값이면 DB 갱신을 생략하고, 낮추는 값이면 별도 제한 검증을 수행한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @param goalCnt 새 목표 권수
     * @return 저장을 막아야 하는 결과 코드, 정상 저장 가능하면 null
     */
    private ResultEnum setReadingGoalByType(Long userNumb, LocalDate today, String goalType, Integer goalCnt) {
        ReadingGoalDto currentGoal = getReadingGoalDtl(userNumb, today, goalType);

        // 현재 목표와 새 목표가 같으면 수정 횟수를 증가시키지 않기 위해 DB 갱신을 생략한다.
        if (!StringUtil.isEmpty(currentGoal) && goalCnt.equals(currentGoal.getGoalCnt())) {
            return null;
        }

        ResultEnum validateResult = validateReadingGoalDown(currentGoal, today, goalType, goalCnt);
        // 목표 내리기 검증에서 실패 코드가 나오면 해당 코드를 Controller까지 전달한다.
        if (!StringUtil.isEmpty(validateResult)) {
            return validateResult;
        }

        ReadingGoalDto req = new ReadingGoalDto();
        req.setUserNumb(userNumb);
        req.setGoalDate(getGoalDate(today, goalType));
        req.setGoalType(goalType);
        req.setGoalCnt(goalCnt);
        reportMapper.setReadingGoal(req);
        return null;
    }

    /**
     * 목표 권수를 낮추는 요청인지 판단하고 낮추기 제한을 검증한다.
     * 신규 설정 또는 목표 올리기는 제한하지 않고, 낮추기만 횟수와 마감 기간을 적용한다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 저장된 목표
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @param goalCnt 새 목표 권수
     * @return 제한 위반 결과 코드, 통과하면 null
     */
    private ResultEnum validateReadingGoalDown(ReadingGoalDto currentGoal, LocalDate today, String goalType, Integer goalCnt) {
        // 신규 목표 설정이거나 목표를 올리는 요청이면 내리기 제한을 적용하지 않는다.
        if (StringUtil.isEmpty(currentGoal) || currentGoal.getGoalCnt() <= goalCnt) {
            return null;
        }

        // 목표 내리기 허용 횟수를 모두 사용한 경우 더 이상 목표를 낮출 수 없다.
        if (getGoalUpdateLimit(goalType) <= getGoalUpdateCount(currentGoal)) {
            return ResultEnum.COMMON_INVALID_REQUEST;
        }

        // 목표 내리기 가능 기간이 마감된 경우 목표를 낮출 수 없다.
        if (isGoalUpdateLocked(today, goalType)) {
            return ResultEnum.COMMON_INVALID_REQUEST;
        }

        return null;
    }

    /**
     * 목표 유형별 목표 내리기 가능 횟수를 반환한다.
     * 주간 1회, 월간 3회, 연간 5회 제한을 적용한다.
     *
     * @author Seunghyeon.Kang
     * @param goalType 목표 유형
     * @return 목표 내리기 허용 횟수
     */
    private int getGoalUpdateLimit(String goalType) {
        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            return WEEK_GOAL_MAX_UPDATE_COUNT;
        }

        // 월간 목표는 주간보다 넓은 기간을 다루므로 3회까지 목표 내리기를 허용한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            return MONTH_GOAL_MAX_UPDATE_COUNT;
        }

        return YEAR_GOAL_MAX_UPDATE_COUNT;
    }

    /**
     * 현재 목표의 사용 횟수를 기준으로 목표 내리기 잔여 횟수를 계산한다.
     * 목표가 아직 없으면 유형별 전체 허용 횟수를 그대로 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 목표
     * @param goalType 목표 유형
     * @return 목표 내리기 잔여 횟수
     */
    private int getGoalRemainUpdateCount(ReadingGoalDto currentGoal, String goalType) {
        // 저장된 목표가 아직 없으면 유형별 전체 내리기 횟수를 잔여 횟수로 표시한다.
        if (StringUtil.isEmpty(currentGoal)) {
            return getGoalUpdateLimit(goalType);
        }

        return Math.max(0, getGoalUpdateLimit(goalType) - getGoalUpdateCount(currentGoal));
    }

    /**
     * 목표 내리기 사용 횟수가 null이면 0으로 보정한다.
     *
     * @author Seunghyeon.Kang
     * @param currentGoal 현재 목표
     * @return 목표 내리기 사용 횟수
     */
    private int getGoalUpdateCount(ReadingGoalDto currentGoal) {
        return StringUtil.isEmpty(currentGoal.getUpdtCntt()) ? 0 : currentGoal.getUpdtCntt();
    }

    /**
     * 목표 내리기 가능 기간이 지났는지 판단한다.
     * 주간은 해당 주 종료 3일 전부터, 월간은 월 종료 7일 전부터, 연간은 12월 1일부터 내리기를 막는다.
     *
     * @author Seunghyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @return 목표 내리기 마감 여부
     */
    private boolean isGoalUpdateLocked(LocalDate today, String goalType) {
        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            return ChronoUnit.DAYS.between(today, weekLastDay) <= WEEK_GOAL_LOCK_REMAINING_DAYS;
        }

        // 월간 목표는 월 종료 7일 전부터 내리기를 막기 위해 월 마지막 날을 기준으로 계산한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            return ChronoUnit.DAYS.between(today, monthLastDay) <= MONTH_GOAL_LOCK_REMAINING_DAYS;
        }

        return today.getMonthValue() == 12;
    }

    /**
     * 목표 내리기가 가능한 잔여 일수를 계산한다.
     * 기간이 이미 마감되었으면 음수가 내려가지 않도록 0으로 보정한다.
     *
     * @author Seunghyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @return 목표 내리기 가능 잔여 일수
     */
    private int getGoalEditableRemainDays(LocalDate today, String goalType) {
        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, weekLastDay) - WEEK_GOAL_LOCK_REMAINING_DAYS);
        }

        // 월간 목표의 내리기 가능 잔여 일수는 월 마지막 날에서 잠금 기준일을 뺀 값으로 계산한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, monthLastDay) - MONTH_GOAL_LOCK_REMAINING_DAYS);
        }

        LocalDate yearLockDate = LocalDate.of(today.getYear(), 12, 1);
        return Math.max(0, (int) ChronoUnit.DAYS.between(today, yearLockDate));
    }

    /**
     * 완료 독후감 기간 집계에 사용할 요청 DTO를 생성한다.
     * 종료 경계는 중복 집계를 피하기 위해 exclusive 값으로 전달한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param periodStart 기간 시작일
     * @param periodEndExclusive 기간 종료 다음 일자
     * @return 기간 집계 요청 DTO
     */
    private MonthlyReadingSummaryDto getSummaryReportReq(Long userNumb, LocalDate periodStart, LocalDate periodEndExclusive,
                                                         String reportStat, String reportOrderType) {
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        req.setUserNumb(userNumb);
        req.setPeriodStart(periodStart.toString());
        req.setPeriodEndExclusive(periodEndExclusive.toString());
        req.setReportStat(reportStat);
        req.setReportOrderType(reportOrderType);
        return req;
    }

    private MonthlyReadingSummaryDto getSummaryReportReq(Long userNumb, String reportStat, String reportOrderType) {
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        req.setUserNumb(userNumb);
        req.setReportStat(reportStat);
        req.setReportOrderType(reportOrderType);
        return req;
    }

            /**
     * 로그인 사용자가 작성한 독후감 상세 정보와 연결된 도서 정보를 조회한다.
     * 사용자 번호를 조건에 포함해 다른 사용자의 비공개 독후감을 조회하지 못하도록 한다.
     *
     * @author Seunghyeon.Kang
     * @return 독후감 상세 조회 결과
     */
    private String normalizeListSortType(String sortType) {
        // 허용된 정렬값만 Mapper에 전달해 동적 정렬 조건이 임의로 확장되지 않게 한다.
        if (Constant.SORT_START_DATE_DESC.equals(sortType) || Constant.SORT_GRADE_DESC.equals(sortType)) {
            return sortType;
        }

        return Constant.SORT_END_DATE_DESC;
    }
    @Override
    public ResultData getDetail(Long userNumb, Long reportNumb) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        reportDto.setLocale(LocaleUtil.getLocale());
        reportDto.setReportStat(Constant.REPORT_STAT_DONE);

        ReportDto detail = reportMapper.getReportDtl(reportDto);

        // 조회 결과가 없으면 존재하지 않거나 접근할 수 없는 독후감으로 판단한다.
        if (StringUtil.isEmpty(detail)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return ResultData.success(detail);
    }

    /**
     * ISBN 기준으로 공개 독후감 목록을 조회한다.
     * 로그인 사용자의 좋아요 여부를 함께 표시하기 위해 사용자 번호를 Mapper에 전달한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookIsbn 조회할 도서 ISBN
     * @return 공개 독후감 목록 조회 결과
     */
    @Override
    public ResultData getPublicReportsByIsbn(Long userNumb, String bookIsbn) {

        // ISBN이 없으면 도서를 특정할 수 없으므로 공개 독후감 또는 평균 별점을 조회하지 않는다.
        if (StringUtil.isEmpty(bookIsbn)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookIsbn(StringUtil.normalizePlainText(bookIsbn));

        return ResultData.success(reportMapper.getPublicReportList(reportDto));
    }

    /**
     * ISBN 기준 도서 평균 별점을 조회한다.
     * 평균 별점은 공개 여부와 무관하게 전체 독후감을 기준으로 계산한다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점 조회 결과
     */
    @Override
    public ResultData getPublicRatingAverageByIsbn(String bookIsbn) {

        // ISBN이 없으면 도서를 특정할 수 없으므로 공개 독후감 또는 평균 별점을 조회하지 않는다.
        if (StringUtil.isEmpty(bookIsbn)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return ResultData.success(bookMapper.getPublicRatingAverageByIsbn(StringUtil.normalizePlainText(bookIsbn)));
    }

            /**
     * 독후감과 필요한 도서 정보를 등록한다.
     * 도서가 이미 존재하면 기존 도서 번호를 재사용하고, 없으면 도서를 먼저 등록한 뒤 독후감을 저장한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 등록된 독후감 번호
     */
    @Override
    @Transactional
    public ResultData setReportLike(Long userNumb, Long reportNumb) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        // 비공개 또는 존재하지 않는 독후감에는 좋아요를 등록하지 못하도록 차단한다.
        if (reportMapper.getPublicReportLikeTargetCnt(reportDto) == 0) {
            // 요청값이 업무 규칙에 맞지 않음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 이미 좋아요가 있으면 취소하고, 없으면 신규 등록하는 토글 방식으로 처리한다.
        if (reportMapper.dupReportLike(reportDto) > 0) {
            reportMapper.delReportLike(reportDto);
        } else {
            reportMapper.setReportLike(reportDto);
        }

        return ResultData.success(reportMapper.getReportLikeDtl(reportDto));
    }

            /**
     * 독후감과 필요한 도서 정보를 등록한다.
     * 도서가 이미 존재하면 기존 도서 번호를 재사용하고, 없으면 도서를 먼저 등록한 뒤 독후감을 저장한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportDto 등록할 독후감 및 도서 정보
     * @return 등록된 독후감 번호
     */
    @Override
    @Transactional
    public ResultData setReport(Long userNumb, ReportDto reportDto) {

        // 등록 요청의 도서 필수값이 누락되면 도서와 독후감 저장을 모두 중단한다.
        if (hasInvalidBookFields(reportDto)) {
            // 검증 실패 사유를 ResultData로 감싸 Controller까지 전달한다.
            return ResultData.fail(ResultEnum.COMMON_REPORT_BOOK_INVALID);
        }

        reportDto.setUserNumb(userNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, true);

        ReportValidationResult validationResult = validateReport(reportDto, true);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        // ISBN 기준 등록된 도서가 없을 때만 도서 마스터를 신규 생성한다.
        if (bookMapper.dupBook(reportDto) == 0) {
            bookMapper.setBook(reportDto);
        } else {
            reportDto.setBookNumb(bookMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        reportMapper.setReport(reportDto);
        // 독후감 등록 후 PK가 채워지지 않으면 저장 실패로 판단한다.
        if (StringUtil.isEmpty(reportDto.getReportNumb())) {
            // 저장 실패 상황을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        return ResultData.success(reportDto.getReportNumb());
    }

    /**
     * 기존 독후감 정보를 수정한다.
     * URL의 독후감 번호를 DTO에 주입해 클라이언트가 본문 번호를 조작해도 수정 대상이 바뀌지 않도록 한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 번호
     */
    @Override
    public ResultData uptReport(Long userNumb, Long reportNumb, ReportDto reportDto) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, false);

        ReportValidationResult validationResult = validateReport(reportDto, true);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        if (reportMapper.uptReport(reportDto) == 0) {
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        return ResultData.success(reportDto.getReportNumb());
    }

    /**
     * 마이페이지의 현재 읽고 있는 책 목록에서 독서 상태와 별점만 빠르게 수정한다.
     * 전체 독후감 수정 화면으로 이동하지 않아도 완료 여부와 평점만 즉시 반영할 수 있도록 별도 수정 범위를 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportNumb 수정할 독후감 번호
     * @param reportDto 수정할 독서 상태와 별점 정보
     * @return 수정 처리 결과
     */
    @Override
    public ResultData uptReportStatusGrade(Long userNumb, Long reportNumb, ReportDto reportDto) {

        // 대상 독후감 번호가 없으면 수정 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 조회 결과가 없음을 의미하는 공통 응답 메시지: "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        reportDto.setReportGrde(StringUtil.normalizePlainText(reportDto.getReportGrde()));
        reportDto.setReportStat(StringUtil.normalizePlainText(reportDto.getReportStat()));
        reportDto.setReportEndt(LocalDate.now().toString()); // 빠른 완료/중단 처리에서는 사용자가 저장한 시점을 실제 독서 종료일로 기록한다.

        ReportValidationResult validationResult = validateReport(reportDto, false);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        // 사용자 번호를 WHERE 조건에 함께 사용해 다른 사용자의 독후감은 수정되지 않도록 막는다.
        int result = reportMapper.uptReportStatusGrade(reportDto);

        if (result == 0) {
            // 수정 실패를 의미하는 공통 응답 메시지: "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        return ResultData.success(reportDto.getReportNumb());
    }

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     * 사용자 번호와 독후감 번호를 함께 조건으로 사용해 본인 독후감만 삭제되도록 한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 처리 결과
     */
    @Override
    public ResultData delReport(Long userNumb, Long reportNumb) {
        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 대상 데이터가 없음을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        // 삭제 반영 건수가 없으면 본인 독후감이 아니거나 이미 삭제된 데이터로 판단한다.
        if (reportMapper.delReport(reportDto) == 0) {
            // 삭제 실패 상황을 공통 응답 코드로 반환한다.
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        return ResultData.success();
    }
    /**
     * 독후감 등록에 필요한 도서 필수값이 모두 존재하는지 확인한다.
     * 도서 검색 API 응답을 조작해 들어오는 경우에도 백엔드에서 한 번 더 검증한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 및 도서 정보
     * @return 도서 필수값 누락 여부
     */
    private boolean hasInvalidBookFields(ReportDto reportDto) {
        return StringUtil.isEmpty(reportDto) || StringUtil.hasEmpty(
                reportDto.getBookTitl(),
                reportDto.getBookAthr(),
                reportDto.getBookPubl(),
                reportDto.getBookIsbn(),
                reportDto.getBookCvim(),
                reportDto.getBookDesc()
        );
    }

    /**
     * 독후감 등록과 수정에 공통으로 적용되는 업무 검증을 수행한다.
     * 필수값, 공통코드, 날짜 범위, 본문 byte 길이, 공개 여부 코드를 순서대로 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 정보
     * @param isFullScan 독후감 내용을 모두 유효성 검사 할 것인지를 판단
     * @return 검증 실패 결과, 통과하면 null
     */
    private ReportValidationResult validateReport(ReportDto reportDto, boolean isFullScan) {
        List<String> missingFields = new ArrayList<>();

        // 독서 상태는 필수값이며 READ_STAT 공통코드에 등록된 값만 저장한다.
        if (StringUtil.isEmpty(reportDto.getReportStat()) || !codeUtil.existsCode(Constant.CODE_READ_STAT, reportDto.getReportStat())) {
            missingFields.add(MessageUtils.getMessage(REPORT_FIELD_STATUS_KEY));
        }

        // 종료일은 상태와 관계없이 기간 계산에 필요하므로 필수값으로 검증한다.
        if (StringUtil.isEmpty(reportDto.getReportEndt())) {
            missingFields.add(MessageUtils.getMessage(REPORT_FIELD_END_DATE_KEY));
        }

        // 도서 평점의 저장값이 없으면 저장값을 0점으로 보정해 저장값을 숫자로 유지한다.
        if (StringUtil.isEmpty(reportDto.getReportGrde())) {
            reportDto.setReportGrde("0");
        }

        //등록 수정화면에서 행해지는 등록 및 수정은 모든 값을 입력받아야한다.
        if(isFullScan) {
            // 시작일은 상태와 관계없이 기간 계산에 필요하므로 필수값으로 검증한다.
            if (StringUtil.isEmpty(reportDto.getReportStdt())) {
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_START_DATE_KEY));
            }

            // 다 읽었어요 상태의 빈 평점이나 0점부터 5점까지의 정수 범위를 벗어난 값은 저장하지 않는다.
            if (!isValidReportGrade(reportDto.getReportGrde())) {
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_GRADE_KEY));
            }

            // 책장 색상은 필수값이며 BOOK_COLR 공통코드에 등록된 값만 저장한다.
            if (StringUtil.isEmpty(reportDto.getReportColr()) || !codeUtil.existsCode(Constant.CODE_BOOK_COLR, reportDto.getReportColr())) {
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_COLOR_KEY));
            }

            // 독후감 본문이 비어 있으면 저장 대상 내용이 없으므로 필수값 누락으로 처리한다.
            if (StringUtil.isEmpty(reportDto.getReportCntn())) {
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_CONTENT_KEY));
            }

            // 필수값 누락이 하나라도 있으면 누락 항목 목록을 메시지 인자로 반환한다.
            if (!missingFields.isEmpty()) {
                // 검증 실패 사유를 ResultData로 감싸 Controller까지 전달한다.
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_REQUIRED_MISSING, formatMissingFields(missingFields));
            }
            // 시작일이 종료일보다 늦은 데이터는 프론트 조작 여부와 관계없이 저장하지 않는다.
            if (!DateUtil.validateReportDateRange(reportDto.getReportStdt(), reportDto.getReportEndt())) {
                // 사용자에게 프론트와 같은 날짜 범위 오류 메시지를 반환한다.
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_DATE_RANGE_INVALID);
            }

            // Oracle 저장 한도를 넘는 본문은 DB 오류가 나기 전에 업무 검증으로 차단한다.
            if (XssUtil.utf8ByteLength(reportDto.getReportCntn()) > Constant.REPORT_CONTENT_MAX_BYTES) {
                // 최대 허용 byte 수를 메시지 인자로 함께 반환한다.
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, Constant.REPORT_CONTENT_MAX_BYTES);
            }

            // 공개 여부는 Y 또는 N만 허용해 공개 독후감 조회 조건을 안정적으로 유지한다.
            if (!Constant.COMM_YES.equals(reportDto.getPubcYsno()) && !Constant.COMM_NO.equals(reportDto.getPubcYsno())) {
                // 요청값이 업무 규칙에 맞지 않으면 공통 잘못된 요청 응답으로 반환한다.
                return new ReportValidationResult(ResultEnum.COMMON_INVALID_REQUEST);
            }
        }

        return null;
    }

    /**
     * 누락된 필수 항목 목록을 사용자에게 보여줄 수 있는 줄바꿈 문장으로 변환한다.
     *
     * @author Seunghyeon.Kang
     * @param missingFields 누락된 필드 표시명 목록
     * @return 필수값 누락 메시지 인자
     */
    private String formatMissingFields(List<String> missingFields) {
        return "- " + String.join("\n- ", missingFields);
    }

    /**
     * 별점 값이 숫자이며 0점부터 5점 범위 안의 정수인지 확인한다.
     * 0점은 읽고있어요 상태에서 별점을 선택하지 않은 값을 저장하기 위한 내부 보정값으로 허용한다.
     *
     * @author Seunghyeon.Kang
     * @param reportGrde 검증할 별점 문자열
     * @return 유효한 별점 여부
     */
    private boolean isValidReportGrade(String reportGrde) {
        // 별점이 비어 있으면 호출한 검증 흐름에서 상태별 필수 여부를 먼저 판단하도록 false를 반환한다.
        if (StringUtil.isEmpty(reportGrde)) {
            return false;
        }

        try {
            int grade = Integer.parseInt(reportGrde);

            // 평점은 0점부터 5점까지의 정수만 허용하고 소수점 별점은 저장하지 않는다.
            return grade >= 0 && grade <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void setDefaultReportColor(ReportDto reportDto) {
        // 책장 색상은 필수값이며 공통코드에 등록된 색상 코드만 허용한다.
        if (StringUtil.isEmpty(reportDto.getReportColr()) || reportDto.getReportColr().isBlank()) {
            reportDto.setReportColr(codeUtil.getFirstCode(Constant.CODE_BOOK_COLR));
        }
    }

    /**
     * 공개 여부 값이 비어 있으면 비공개로 기본 설정한다.
     * 사용자가 명시적으로 공개를 선택하지 않은 독후감이 외부에 노출되지 않도록 한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 공개 여부 기본값을 반영할 독후감 DTO
     */
    private void setDefaultPublicFlag(ReportDto reportDto) {
        // 공개 여부는 Y 또는 N만 허용해 공개 독후감 조회 조건을 안정적으로 유지한다.
        if (StringUtil.isEmpty(reportDto.getPubcYsno()) || reportDto.getPubcYsno().isBlank()) {
            reportDto.setPubcYsno(Constant.COMM_NO);
        }
    }

    /**
     * 독후감 입력값의 HTML entity와 불필요한 텍스트 표현을 일반 문자열로 정규화한다.
     * 등록 시에는 도서 정보도 함께 정규화하고, 수정 시에는 독후감 필드만 정규화한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 정규화할 독후감 DTO
     * @param includeBookFields 도서 필드 정규화 포함 여부
     */
    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {
        reportDto.setReportStat(StringUtil.normalizePlainText(reportDto.getReportStat()));
        reportDto.setReportStdt(StringUtil.normalizePlainText(reportDto.getReportStdt()));
        reportDto.setReportEndt(StringUtil.normalizePlainText(reportDto.getReportEndt()));
        reportDto.setReportGrde(StringUtil.normalizePlainText(reportDto.getReportGrde()));
        reportDto.setReportColr(StringUtil.normalizePlainText(reportDto.getReportColr()));
        reportDto.setPubcYsno(StringUtil.normalizePlainText(reportDto.getPubcYsno()));
        reportDto.setReportCntn(StringUtil.normalizePlainText(reportDto.getReportCntn()));

        // 등록 요청일 때만 도서 필드를 함께 정규화하고, 수정 요청에서는 독후감 필드만 정규화한다.
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
     * 독후감 검증 실패 결과와 메시지 인자를 함께 전달하기 위한 내부 record이다.
     *
     * @author Seunghyeon.Kang
     * @param resultEnum 실패 결과 코드
     * @param args 메시지 치환 인자
     */
    private record ReportValidationResult(ResultEnum resultEnum, Object... args) {
    }
}
