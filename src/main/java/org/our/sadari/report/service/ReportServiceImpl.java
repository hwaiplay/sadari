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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.BadWordDetectionService;
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
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.mapper.SocialMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReportServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 독후감과 독서 목표 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    // Report 데이터 접근 객체
    private final ReportMapper reportMapper;
    // Social 데이터 접근 객체
    private final SocialMapper socialMapper;
    // Book 데이터 접근 객체
    private final BookMapper bookMapper;
    // 공통코드 캐시 조회 객체
    private final CodeUtil codeUtil;
    // BadWordDetection 업무 처리 서비스
    private final BadWordDetectionService badWordDetectionService;
    private static final DateTimeFormatter GOAL_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    // 목표 주간 FIELDS 설정값
    private static final WeekFields GOAL_WEEK_FIELDS = WeekFields.ISO;
    // 주간 목표 최대 UPDATE 건수 설정값
    private static final int WEEK_GOAL_MAX_UPDATE_COUNT = 1;
    // 월간 목표 최대 UPDATE 건수 설정값
    private static final int MONTH_GOAL_MAX_UPDATE_COUNT = 3; // 월간 목표는 한 달 단위로 조정하므로 목표 내리기를 최대 3회까지 허용한다.
    // 연간 목표 최대 UPDATE 건수 설정값
    private static final int YEAR_GOAL_MAX_UPDATE_COUNT = 5; // 연간 목표는 장기 목표이므로 목표 내리기를 최대 5회까지 허용한다.
    // 주간 목표 LOCK REMAINING 일수 설정값
    private static final int WEEK_GOAL_LOCK_REMAINING_DAYS = 3; // 주간 목표는 해당 주가 3일 남은 시점부터 목표 내리기를 잠근다.
    // 월간 목표 LOCK REMAINING 일수 설정값
    private static final int MONTH_GOAL_LOCK_REMAINING_DAYS = 7; // 월간 목표는 해당 월이 7일 남은 시점부터 목표 내리기를 잠근다.
    // SUMMARY 독후감 ORDER 종료 날짜 내림차순 설정값
    private static final String SUMMARY_REPORT_ORDER_END_DATE_DESC = "END_DATE_DESC";
    // SUMMARY 독후감 ORDER 종료 날짜 ASC 설정값
    private static final String SUMMARY_REPORT_ORDER_END_DATE_ASC = "END_DATE_ASC";
    // 독후감 FIELD 상태 키 설정값
    private static final String REPORT_FIELD_STATUS_KEY = "common.report.field.status";
    // 독후감 FIELD 시작 날짜 키 설정값
    private static final String REPORT_FIELD_START_DATE_KEY = "common.report.field.startDate";
    // 독후감 FIELD 종료 날짜 키 설정값
    private static final String REPORT_FIELD_END_DATE_KEY = "common.report.field.endDate";
    // 독후감 FIELD 평점 키 설정값
    private static final String REPORT_FIELD_GRADE_KEY = "common.report.field.grade";
    // 독후감 FIELD COLOR 키 설정값
    private static final String REPORT_FIELD_COLOR_KEY = "common.report.field.color";
    // 독후감 FIELD 내용 키 설정값
    private static final String REPORT_FIELD_CONTENT_KEY = "common.report.field.content";

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회한다.
     * 검색어는 HTML entity를 일반 텍스트로 보정한 뒤 Mapper에 전달한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @return 독후감 목록 조회 결과
     */
    @Override
    public ResultData getBookList(Long userNumb, String bookKeyword, String sortType) {

        // 독후감 또는 독서 목표 처리 데이터를 담을 객체를 생성한다
        ReportDto reportDto = new ReportDto();
        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // BookKeyword 업무 값을 reportDto DTO에 설정한다
        reportDto.setBookKeyword(StringUtil.normalizePlainText(bookKeyword));
        // SortType 업무 값을 reportDto DTO에 설정한다
        reportDto.setSortType(normalizeListSortType(sortType));
        // ReptStat 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptStat(Constant.REPORT_STAT_READ);

        // ReportList 데이터를 DB에서 조회한다
        List<ReportDto> list = reportMapper.getReportList(reportDto);
        // 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(list);
    }

    /**
     * 마이페이지에 표시할 주간, 월간, 연간 독서량 요약과 목표 달성 정보를 조회한다.
     * 현재 기간과 직전 기간을 같은 기준으로 비교해 증감값과 펼침 목록을 함께 구성한다.
     *
     * @author SeungHyeon.Kang
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
                // 주간 목표 종료일을 시작일에서 일주일 뒤로 계산한다
                userNumb, currentWeekStart, currentWeekStart.plusWeeks(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 직전 주 집계 범위: [previousWeekStart] <= 독서 완료일 < [currentWeekStart] (이번 주 시작일 직전까지)
        MonthlyReadingSummaryDto previousWeekReq = getSummaryReportReq(
                userNumb, previousWeekStart, currentWeekStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 이번 달 집계 범위: [currentMonthStart] <= 독서 완료일 < [currentMonthStart + 1달]
        MonthlyReadingSummaryDto currentMonthReq = getSummaryReportReq(
                // 월간 목표 종료일을 시작일에서 한 달 뒤로 계산한다
                userNumb, currentMonthStart, currentMonthStart.plusMonths(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 직전 달 집계 범위: [previousMonthStart] <= 독서 완료일 < [currentMonthStart] (이번 달 시작일 직전까지)
        MonthlyReadingSummaryDto previousMonthReq = getSummaryReportReq(
                userNumb, previousMonthStart, currentMonthStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 올해 집계 범위: [currentYearStart] <= 독서 완료일 < [currentYearStart + 1년]
        MonthlyReadingSummaryDto currentYearReq = getSummaryReportReq(
                // 연간 목표 종료일을 시작일에서 일 년 뒤로 계산한다
                userNumb, currentYearStart, currentYearStart.plusYears(1), Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // 작년 집계 범위: [previousYearStart] <= 독서 완료일 < [currentYearStart] (올해 시작일 직전까지)
        MonthlyReadingSummaryDto previousYearReq = getSummaryReportReq(
                userNumb, previousYearStart, currentYearStart, Constant.REPORT_STAT_DONE, SUMMARY_REPORT_ORDER_END_DATE_DESC);

        // getSummaryReportReq 조회로 후속 처리에 필요한 데이터를 가져온다
        MonthlyReadingSummaryDto currentReadingReq = getSummaryReportReq(
                userNumb, Constant.REPORT_STAT_READ, SUMMARY_REPORT_ORDER_END_DATE_ASC);


        // ==========================================
        // 3. DB 조회 (완료 상태의 독후감 개수 집계)
        // ==========================================

        // 데이터 정합성을 위해 임시 저장(TEMP) 등이 아닌, 작성이 완전히 완료된(REPORT_STAT = 'DONE') 독후감만 DB에서 카운트함
        int currentWeekCount = reportMapper.getReportCntByPeriod(currentWeekReq);
        // ReportCntByPeriod 데이터를 DB에서 조회한다
        int previousWeekCount = reportMapper.getReportCntByPeriod(previousWeekReq);
        // ReportCntByPeriod 데이터를 DB에서 조회한다
        int currentMonthCount = reportMapper.getReportCntByPeriod(currentMonthReq);
        // ReportCntByPeriod 데이터를 DB에서 조회한다
        int previousMonthCount = reportMapper.getReportCntByPeriod(previousMonthReq);
        // ReportCntByPeriod 데이터를 DB에서 조회한다
        int currentYearCount = reportMapper.getReportCntByPeriod(currentYearReq);
        // ReportCntByPeriod 데이터를 DB에서 조회한다
        int previousYearCount = reportMapper.getReportCntByPeriod(previousYearReq);


        // ==========================================
        // 4. 기간별 읽기 목표(Reading Goal) 상세 정보 조회
        // ==========================================

        // 사용자가 설정한 목표 권수를 조회하여 달성률을 계산할 수 있도록 함 (목표를 아예 설정하지 않은 상태도 null 처리를 통해 화면에서 인지 가능)
        ReadingGoalDto currentWeekGoal = getReadingGoalDtl(userNumb, currentWeekStart, Constant.GOAL_TYPE_WEEK);
        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto currentMonthGoal = getReadingGoalDtl(userNumb, currentMonthStart, Constant.GOAL_TYPE_MONTH);
        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto currentYearGoal = getReadingGoalDtl(userNumb, currentYearStart, Constant.GOAL_TYPE_YEAR);
        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto previousWeekGoal = getReadingGoalDtl(userNumb, previousWeekStart, Constant.GOAL_TYPE_WEEK);
        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto previousMonthGoal = getReadingGoalDtl(userNumb, previousMonthStart, Constant.GOAL_TYPE_MONTH);
        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto previousYearGoal = getReadingGoalDtl(userNumb, previousYearStart, Constant.GOAL_TYPE_YEAR);


        // ==========================================
        // 5. 화면 표시용 통합 요약 DTO 구성 및 전후 비교값 산출
        // ==========================================

        // 화면 뷰(UI)에서 현재 값, 이전 값, 그리고 성장세를 나타내는 증감량(Diff)을 한 번에 보여줄 수 있도록 가공하여 바인딩함
        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();

        // 주간 데이터 바인딩 및 차이값(이번 주 완료 건수 - 지난 주 완료 건수) 계산
        summary.setWeekCode(Constant.GOAL_TYPE_WEEK);
        // CurrentWeekCount 업무 값을 summary DTO에 설정한다
        summary.setCurrentWeekCount(currentWeekCount);
        // PreviousWeekCount 업무 값을 summary DTO에 설정한다
        summary.setPreviousWeekCount(previousWeekCount);
        // WeekCountDiff 업무 값을 summary DTO에 설정한다
        summary.setWeekCountDiff(currentWeekCount - previousWeekCount);

        // 월간 데이터 바인딩: 월 코드명을 영문 3자리 대문자로 변환하여 지정 (예: 'JULY' -> 'JUL')
        summary.setMonthCode(today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH));
        // CurrentMonthCount 업무 값을 summary DTO에 설정한다
        summary.setCurrentMonthCount(currentMonthCount);
        // PreviousMonthCount 업무 값을 summary DTO에 설정한다
        summary.setPreviousMonthCount(previousMonthCount);
        // CountDiff 업무 값을 summary DTO에 설정한다
        summary.setCountDiff(currentMonthCount - previousMonthCount);

        // 연간 데이터 바인딩: 현재 연도 숫자를 문자열 코드로 변환하여 지정 (예: '2026')
        summary.setYearCode(String.valueOf(today.getYear()));
        // CurrentYearCount 업무 값을 summary DTO에 설정한다
        summary.setCurrentYearCount(currentYearCount);
        // PreviousYearCount 업무 값을 summary DTO에 설정한다
        summary.setPreviousYearCount(previousYearCount);
        // YearCountDiff 업무 값을 summary DTO에 설정한다
        summary.setYearCountDiff(currentYearCount - previousYearCount);


        // ==========================================
        // 6. 도메인 로직 기반 세부 메타데이터 주입
        // ==========================================

        // 1) 조회한 목표 권수 정보와 달성 여부를 요약 DTO에 바인딩
        applyReadingGoal(summary, currentWeekGoal, currentMonthGoal, currentYearGoal);
        // 이전 독서 목표를 이번 목표로 복사한다
        applyPreviousReadingGoal(summary, previousWeekGoal, previousMonthGoal, previousYearGoal);

        // 2) 목표 수정 제한 정보 계산 및 바인딩 (특정 시점이 지나면 수정 버튼을 비활성화하는 등의 비즈니스 규칙 처리)
        applyReadingGoalUpdateMeta(summary, today, currentWeekGoal, currentMonthGoal, currentYearGoal);

        // 3) 해당 사용자의 역대 누적 전체 목표 달성 횟수(배지나 통계용 데이터) 계산 및 바인딩
        applyReadingGoalAchvCnt(summary, userNumb);


        // ==========================================
        // 7. 상세 목록 매핑 및 결과 반환
        // ==========================================

        // 사용자가 요약 카드 영역을 펼쳤을 때(Accordion 등) 즉시 책 목록을 렌더링할 수 있도록 상세 독후감 리스트도 함께 포함하여 응답함
        summary.setCurrentWeekReports(reportMapper.getSummaryReportList(currentWeekReq));
        // CurrentMonthReports 업무 값을 summary DTO에 설정한다
        summary.setCurrentMonthReports(reportMapper.getSummaryReportList(currentMonthReq));
        // CurrentYearReports 업무 값을 summary DTO에 설정한다
        summary.setCurrentYearReports(reportMapper.getSummaryReportList(currentYearReq));
        // CurrentReadingReports 업무 값을 summary DTO에 설정한다
        summary.setCurrentReadingReports(reportMapper.getSummaryReportList(currentReadingReq));

        // 마이페이지에 표시할 주간, 월간, 연간 독서량 요약과 목표 달성 정보를 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(summary);
    }

    /**
     * 목표 기준일과 목표 유형을 DB 조회용 GOAL_DATE 값으로 변환해 현재 목표를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param targetDate 목표 기준일
     * @param goalType 주간, 월간, 연간 목표 유형
     * @return 저장된 목표 정보, 없으면 null
     */
    private ReadingGoalDto getReadingGoalDtl(Long userNumb, LocalDate targetDate, String goalType) {

        // 독서 목표 조회 또는 저장 조건을 담을 객체를 생성한다
        ReadingGoalDto req = new ReadingGoalDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // GoalDate 업무 값을 req DTO에 설정한다
        req.setGoalDate(getGoalDate(targetDate, goalType));
        // GoalType 업무 값을 req DTO에 설정한다
        req.setGoalType(goalType);
        // 목표 기준일과 목표 유형을 DB 조회용 GOAL_DATE 값으로 변환해 현재 목표를 조회 결과를 반환한다
        return reportMapper.getReadingGoalDtl(req);
    }

    /**
     * 조회된 목표를 요약 DTO에 반영하고 현재 독서량 기준 달성률을 계산한다.
     * 목표가 없는 유형은 화면에서 목표 설정 버튼을 노출할 수 있도록 설정 여부를 false로 유지한다.
     *
     * @author SeungHyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param weekGoal 현재 주간 목표
     * @param monthGoal 현재 월간 목표
     * @param yearGoal 현재 연간 목표
     */
    private void applyReadingGoal(MonthlyReadingSummaryDto summary, ReadingGoalDto weekGoal, ReadingGoalDto monthGoal
                                , ReadingGoalDto yearGoal) {

        // 주간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(weekGoal)) {

            // WeekGoalSet 업무 값을 summary DTO에 설정한다
            summary.setWeekGoalSet(true);
            // WeekGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setWeekGoalCnt(weekGoal.getGoalCnt());
            // WeekGoalRate 업무 값을 summary DTO에 설정한다
            summary.setWeekGoalRate(getGoalRate(summary.getCurrentWeekCount(), weekGoal.getGoalCnt()));
        }

        // 월간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(monthGoal)) {

            // MonthGoalSet 업무 값을 summary DTO에 설정한다
            summary.setMonthGoalSet(true);
            // MonthGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setMonthGoalCnt(monthGoal.getGoalCnt());
            // MonthGoalRate 업무 값을 summary DTO에 설정한다
            summary.setMonthGoalRate(getGoalRate(summary.getCurrentMonthCount(), monthGoal.getGoalCnt()));
        }

        // 연간 목표가 설정된 경우에만 목표 권수와 달성률을 화면 응답에 포함한다.
        if (!StringUtil.isEmpty(yearGoal)) {

            // YearGoalSet 업무 값을 summary DTO에 설정한다
            summary.setYearGoalSet(true);
            // YearGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setYearGoalCnt(yearGoal.getGoalCnt());
            // YearGoalRate 업무 값을 summary DTO에 설정한다
            summary.setYearGoalRate(getGoalRate(summary.getCurrentYearCount(), yearGoal.getGoalCnt()));
        }
    }

    private void applyPreviousReadingGoal(MonthlyReadingSummaryDto summary, ReadingGoalDto weekGoal, ReadingGoalDto monthGoal
                                        , ReadingGoalDto yearGoal) {

        // weekGoal 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(weekGoal)) {

            // PreviousWeekGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setPreviousWeekGoalCnt(weekGoal.getGoalCnt());
        }

        // monthGoal 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(monthGoal)) {

            // PreviousMonthGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setPreviousMonthGoalCnt(monthGoal.getGoalCnt());
        }

        // yearGoal 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(yearGoal)) {

            // PreviousYearGoalCnt 업무 값을 summary DTO에 설정한다
            summary.setPreviousYearGoalCnt(yearGoal.getGoalCnt());
        }
    }

    /**
     * 목표 내리기 잔여 횟수와 기간 마감 여부를 요약 DTO에 반영한다.
     * 프론트 모달은 이 값을 사용해 내리기 버튼 상태와 안내 문구를 결정한다.
     *
     * @author SeungHyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param today 현재 날짜
     * @param weekGoal 현재 주간 목표
     * @param monthGoal 현재 월간 목표
     * @param yearGoal 현재 연간 목표
     */
    private void applyReadingGoalUpdateMeta(MonthlyReadingSummaryDto summary, LocalDate today, ReadingGoalDto weekGoal
                                          , ReadingGoalDto monthGoal, ReadingGoalDto yearGoal) {

        // WeekGoalRemainUpdateCnt 업무 값을 summary DTO에 설정한다
        summary.setWeekGoalRemainUpdateCnt(getGoalRemainUpdateCount(weekGoal, Constant.GOAL_TYPE_WEEK));
        // MonthGoalRemainUpdateCnt 업무 값을 summary DTO에 설정한다
        summary.setMonthGoalRemainUpdateCnt(getGoalRemainUpdateCount(monthGoal, Constant.GOAL_TYPE_MONTH));
        // YearGoalRemainUpdateCnt 업무 값을 summary DTO에 설정한다
        summary.setYearGoalRemainUpdateCnt(getGoalRemainUpdateCount(yearGoal, Constant.GOAL_TYPE_YEAR));
        // WeekGoalEditableRemainDays 업무 값을 summary DTO에 설정한다
        summary.setWeekGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_WEEK));
        // MonthGoalEditableRemainDays 업무 값을 summary DTO에 설정한다
        summary.setMonthGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_MONTH));
        // YearGoalEditableRemainDays 업무 값을 summary DTO에 설정한다
        summary.setYearGoalEditableRemainDays(getGoalEditableRemainDays(today, Constant.GOAL_TYPE_YEAR));
        // WeekGoalUpdateLocked 업무 값을 summary DTO에 설정한다
        summary.setWeekGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_WEEK));
        // MonthGoalUpdateLocked 업무 값을 summary DTO에 설정한다
        summary.setMonthGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_MONTH));
        // YearGoalUpdateLocked 업무 값을 summary DTO에 설정한다
        summary.setYearGoalUpdateLocked(isGoalUpdateLocked(today, Constant.GOAL_TYPE_YEAR));
    }

    /**
     * 과거 전체 기간에서 목표를 실제로 달성한 횟수를 주간, 월간, 연간으로 나누어 반영한다.
     * 현재 기간만 보는 독서량 요약과 달리 성공 횟수는 사용자의 전체 이력을 기준으로 집계한다.
     *
     * @author SeungHyeon.Kang
     * @param summary 마이페이지 요약 DTO
     * @param userNumb 로그인 사용자 번호
     */
    private void applyReadingGoalAchvCnt(MonthlyReadingSummaryDto summary, Long userNumb) {

        // getReadingGoalAchvCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        int weekGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_WEEK);
        // getReadingGoalAchvCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        int monthGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_MONTH);
        // getReadingGoalAchvCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        int yearGoalAchvCnt = getReadingGoalAchvCnt(userNumb, Constant.GOAL_TYPE_YEAR);

        // WeekGoalAchvCnt 업무 값을 summary DTO에 설정한다
        summary.setWeekGoalAchvCnt(weekGoalAchvCnt);
        // MonthGoalAchvCnt 업무 값을 summary DTO에 설정한다
        summary.setMonthGoalAchvCnt(monthGoalAchvCnt);
        // YearGoalAchvCnt 업무 값을 summary DTO에 설정한다
        summary.setYearGoalAchvCnt(yearGoalAchvCnt);
        // TotalGoalAchvCnt 업무 값을 summary DTO에 설정한다
        summary.setTotalGoalAchvCnt(weekGoalAchvCnt + monthGoalAchvCnt + yearGoalAchvCnt);
    }

    /**
     * 목표 유형 하나에 대한 전체 달성 횟수를 Mapper를 통해 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param goalType 목표 유형
     * @return 목표 달성 횟수
     */
    private int getReadingGoalAchvCnt(Long userNumb, String goalType) {

        // 독서 목표 조회 또는 저장 조건을 담을 객체를 생성한다
        ReadingGoalDto req = new ReadingGoalDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // GoalType 업무 값을 req DTO에 설정한다
        req.setGoalType(goalType);
        // ReptStat 업무 값을 req DTO에 설정한다
        req.setReptStat(Constant.REPORT_STAT_DONE);
        // 목표 유형 하나에 대한 전체 달성 횟수를 Mapper를 통해 조회 결과를 반환한다
        return reportMapper.getReadingGoalAchvCnt(req);
    }

    /**
     * 완료 독후감 수와 목표 권수를 비교해 화면 표시용 달성률을 계산한다.
     * 100%를 넘는 경우에도 진행 막대는 최대값으로 표시해야 하므로 100으로 제한한다.
     *
     * @author SeungHyeon.Kang
     * @param doneCount 완료 독후감 수
     * @param goalCount 목표 권수
     * @return 0부터 100까지의 달성률
     */
    private int getGoalRate(int doneCount, Integer goalCount) {

        // 목표 권수가 없거나 0 이하이면 달성률 계산이 불가능하므로 0%로 처리한다.
        if (StringUtil.isEmpty(goalCount) || goalCount <= 0) {

            // 완료 독후감 수와 목표 권수를 비교해 화면 표시용 달성률을 계산 결과를 반환한다
            return 0;
        }
        // 완료 독후감 수와 목표 권수를 비교해 화면 표시용 달성률을 계산 결과를 반환한다
        return Math.min(100, (int) Math.round((doneCount * 100.0) / goalCount));
    }

    /**
     * 목표 유형에 따라 TM_GOALXM.GOAL_DATE에 저장할 기준값을 만든다.
     * 주간은 ISO week 기준 YYYYWW, 월간은 YYYYMM, 연간은 YYYY00 형식을 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param targetDate 목표 기준일
     * @param goalType 목표 유형
     * @return 목표 기준값
     */
    private String getGoalDate(LocalDate targetDate, String goalType) {

        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {

            // 목표 유형에 따라 TM_GOALXM.GOAL_DATE에 저장할 기준값을 만든다 결과를 반환한다
            return getGoalWeekDate(targetDate);
        }

        // 연간 목표는 월 정보가 필요 없으므로 YYYY00 형식으로 저장한다.
        if (Constant.GOAL_TYPE_YEAR.equals(goalType)) {

            // 목표 유형에 따라 TM_GOALXM.GOAL_DATE에 저장할 기준값을 만든다 결과를 반환한다
            return targetDate.getYear() + "00";
        }
        // 목표 유형에 따라 TM_GOALXM.GOAL_DATE에 저장할 기준값을 만든다 결과를 반환한다
        return YearMonth.from(targetDate).format(GOAL_MONTH_FORMATTER);
    }

    /**
     * ISO 주차 기준으로 주간 목표의 GOAL_DATE 값을 생성한다.
     * 연말과 연초가 겹치는 주차를 올바르게 처리하기 위해 week-based-year를 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param targetDate 목표 기준일
     * @return YYYYWW 형식의 주간 목표 기준값
     */
    private String getGoalWeekDate(LocalDate targetDate) {

        // 지정한 키에 대응하는 값을 조회한다
        int weekYear = targetDate.get(GOAL_WEEK_FIELDS.weekBasedYear());
        // 지정한 키에 대응하는 값을 조회한다
        int weekNumber = targetDate.get(GOAL_WEEK_FIELDS.weekOfWeekBasedYear());
        // ISO 주차 기준으로 주간 목표의 GOAL_DATE 값을 생성 결과를 반환한다
        return String.format("%04d%02d", weekYear, weekNumber);
    }

    /**
     * 주간, 월간, 연간 독서 목표를 한 번에 저장한다.
     * 목표를 올리는 것은 항상 허용하고, 목표를 낮추는 경우에만 기간과 횟수 제한을 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param readingGoalDto 저장할 주간, 월간, 연간 목표 권수
     * @return 저장 후 갱신된 마이페이지 독서 요약 정보
     */
    private boolean isValidReadingGoal(ReadingGoalDto readingGoalDto) {

        // 주간, 월간, 연간 독서 목표를 한 번에 저장 결과를 반환한다
        return !(StringUtil.isEmpty(readingGoalDto) || StringUtil.isEmpty(readingGoalDto.getWeekGoalCnt())
                // 필수 값이 비어 있는지 공통 기준으로 확인한다
                || StringUtil.isEmpty(readingGoalDto.getMonthGoalCnt()) || StringUtil.isEmpty(readingGoalDto.getYearGoalCnt())
                // getWeekGoalCnt 조회로 후속 처리에 필요한 데이터를 가져온다
                || readingGoalDto.getWeekGoalCnt() <= 0 || readingGoalDto.getMonthGoalCnt() <= 0
                // getYearGoalCnt 조회로 후속 처리에 필요한 데이터를 가져온다
                || readingGoalDto.getYearGoalCnt() <= 0);
    }
    /**
     * 로그인 사용자의 독서 목표 권수를 저장한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 사용자 번호
     * @param readingGoalDto 저장할 독서 목표 기간과 권수
     * @return 업무 처리 성공 또는 실패 응답
     */
    @Override
    @Transactional
    public ResultData setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto) {

        // 주간, 월간, 연간 목표 중 하나라도 유효하지 않으면 저장 요청 전체를 거절한다.
        if (!isValidReadingGoal(readingGoalDto)) {

            // "\uC694\uCCAD\uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC544\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 목표 기간 계산의 기준이 되는 오늘 날짜를 조회한다
        LocalDate today = LocalDate.now();
        // getWeekGoalCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        ResultEnum weekResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_WEEK, readingGoalDto.getWeekGoalCnt());
        // 주간 목표 저장 중 제한 규칙에 걸리면 이후 월간, 연간 저장을 진행하지 않는다.
        if (!StringUtil.isEmpty(weekResult)) {

            // 로그인 사용자의 독서 목표 권수를 저장 결과를 반환한다
            return ResultData.fail(weekResult);
        }

        // getMonthGoalCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        ResultEnum monthResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_MONTH, readingGoalDto.getMonthGoalCnt());
        // 월간 목표 저장 중 제한 규칙에 걸리면 이후 연간 저장을 진행하지 않는다.
        if (!StringUtil.isEmpty(monthResult)) {

            // 로그인 사용자의 독서 목표 권수를 저장 결과를 반환한다
            return ResultData.fail(monthResult);
        }

        // getYearGoalCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        ResultEnum yearResult = setReadingGoalByType(userNumb, today, Constant.GOAL_TYPE_YEAR, readingGoalDto.getYearGoalCnt());
        // 연간 목표 저장 중 제한 규칙에 걸리면 실패 결과를 그대로 반환한다.
        if (!StringUtil.isEmpty(yearResult)) {

            // 로그인 사용자의 독서 목표 권수를 저장 결과를 반환한다
            return ResultData.fail(yearResult);
        }
        // 로그인 사용자의 독서 목표 권수를 저장 결과를 반환한다
        return getMonthlyReadingSummary(userNumb);
    }

    /**
     * 직전 기간(지난주, 지난달, 작년)에 설정되어 있던 독서 목표 데이터를 조회하여 현재 기간의 목표로 일괄 복사한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 복사 처리 결과 데이터 (복사된 목표가 없으면 실패 결과, 성공 시 최신 독서 요약 정보 반환)
     */
    @Override
    @Transactional
    public ResultData copyPreviousReadingGoal(Long userNumb) {

        // 1. 현재 시점 및 직전 주간, 월간, 연간의 시작 날짜 기준점을 계산한다.
        LocalDate today = LocalDate.now();                                                      // 실시간 현재 일자 획득
        // 목표 기간 계산에 사용할 기준 요일로 날짜를 조정한다
        LocalDate currentWeekStart = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 1);               // ISO 기준 이번 주 월요일 날짜
        // 지난 주 목표 조회에 사용할 시작일을 계산한다
        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);                           // ISO 기준 지난 주 월요일 날짜
        // 기준 월에서 필요한 일자로 날짜를 조정한다
        LocalDate currentMonthStart = today.withDayOfMonth(1);                                  // 당월 1일 날짜
        // 지난 달 목표 조회에 사용할 시작일을 계산한다
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);                        // 전월 1일 날짜
        // 연간 목표 조회 기준일을 연도의 첫날로 조정한다
        LocalDate currentYearStart = today.withDayOfYear(1);                                    // 금년 1월 1일 날짜
        // 지난해 목표 조회에 사용할 시작일을 계산한다
        LocalDate previousYearStart = currentYearStart.minusYears(1);                           // 전년 1월 1일 날짜

        // 2. 주간, 월간, 연간 목표 순으로 직전 기간의 목표를 복사 처리하고 성공한 총 건수를 누적한다.
        int copiedCount = 0;
        // copyPreviousReadingGoalByType 호출로 이전 목표값을 새 목표에 반영한다
        copiedCount += copyPreviousReadingGoalByType(userNumb, today, currentWeekStart, previousWeekStart, Constant.GOAL_TYPE_WEEK);
        // copyPreviousReadingGoalByType 호출로 이전 목표값을 새 목표에 반영한다
        copiedCount += copyPreviousReadingGoalByType(userNumb, today, currentMonthStart, previousMonthStart, Constant.GOAL_TYPE_MONTH);
        // copyPreviousReadingGoalByType 호출로 이전 목표값을 새 목표에 반영한다
        copiedCount += copyPreviousReadingGoalByType(userNumb, today, currentYearStart, previousYearStart, Constant.GOAL_TYPE_YEAR);

        // 3. 복사된 목표가 단 1건도 없는 경우(이미 목표가 존재하거나 이전 목표 데이터가 없는 경우) 요청 실패로 응답한다.
        if (copiedCount == 0) {

            // "\uC694\uCCAD\uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC544\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 직전 기간(지난주, 지난달, 작년)에 설정되어 있던 독서 목표 데이터를 조회하여 현재 기간의 목표로 일괄 복사한 결과를 반환한다
        return getMonthlyReadingSummary(userNumb);
    }

    /**
     * 목표 타입별로 직전 기간의 목표 데이터를 검증하고 현재 기간의 목표로 단건 복사한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param today 현재 날짜
     * @param currentDate 현재 기간의 시작일
     * @param previousDate 직전 기간의 시작일
     * @param goalType 목표 타입 (WEEK / MONTH / YEAR)
     * @return 목표 복사 성공 여부 (성공: 1, 실패/스킵: 0)
     */
    private int copyPreviousReadingGoalByType(Long userNumb, LocalDate today, LocalDate currentDate
                                            , LocalDate previousDate, String goalType) {

        // 1. 이미 현재 기간에 설정된 목표가 존재하는 경우 덮어쓰지 않고 즉시 스킵한다.
        ReadingGoalDto currentGoal = getReadingGoalDtl(userNumb, currentDate, goalType);
        // currentGoal 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(currentGoal)) {

            // 목표 타입별로 직전 기간의 목표 데이터를 검증하고 현재 기간의 목표로 단건 복사한 결과를 반환한다
            return 0;
        }

        // 2. 직전 기간의 목표 데이터를 조회하여 값이 없거나 0 이하의 유효하지 않은 권수인 경우 스킵한다.
        ReadingGoalDto previousGoal = getReadingGoalDtl(userNumb, previousDate, goalType);
        // previousGoal 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(previousGoal) || StringUtil.isEmpty(previousGoal.getGoalCnt())
                || previousGoal.getGoalCnt() <= 0) {

            // 목표 타입별로 직전 기간의 목표 데이터를 검증하고 현재 기간의 목표로 단건 복사한 결과를 반환한다
            return 0;
        }

        // 3. 직전 기간의 목표 권수를 기반으로 현재 기간의 목표를 새로 등록한다.
        ResultEnum result = setReadingGoalByType(userNumb, today, goalType, previousGoal.getGoalCnt());

        // 목표 타입별로 직전 기간의 목표 데이터를 검증하고 현재 기간의 목표로 단건 복사한 결과를 반환한다
        return StringUtil.isEmpty(result) ? 1 : 0;
    }

    /**
     * 목표 유형 하나에 대해 현재 목표와 신규 목표를 비교한 뒤 저장한다.
     * 같은 값이면 DB 갱신을 생략하고, 낮추는 값이면 별도 제한 검증을 수행한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @param goalCnt 새 목표 권수
     * @return 저장을 막아야 하는 결과 코드, 정상 저장 가능하면 null
     */
    private ResultEnum setReadingGoalByType(Long userNumb, LocalDate today, String goalType
                                          , Integer goalCnt) {

        // getReadingGoalDtl 조회로 후속 처리에 필요한 데이터를 가져온다
        ReadingGoalDto currentGoal = getReadingGoalDtl(userNumb, today, goalType);

        // 현재 목표와 새 목표가 같으면 수정 횟수를 증가시키지 않기 위해 DB 갱신을 생략한다.
        if (!StringUtil.isEmpty(currentGoal) && goalCnt.equals(currentGoal.getGoalCnt())) {

            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // validateReadingGoalDown 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        ResultEnum validateResult = validateReadingGoalDown(currentGoal, today, goalType, goalCnt);
        // 목표 내리기 검증에서 실패 코드가 나오면 해당 코드를 Controller까지 전달한다.
        if (!StringUtil.isEmpty(validateResult)) {

            // 목표 유형 하나에 대해 현재 목표와 신규 목표를 비교한 뒤 저장 결과를 반환한다
            return validateResult;
        }

        // 독서 목표 조회 또는 저장 조건을 담을 객체를 생성한다
        ReadingGoalDto req = new ReadingGoalDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // GoalDate 업무 값을 req DTO에 설정한다
        req.setGoalDate(getGoalDate(today, goalType));
        // GoalType 업무 값을 req DTO에 설정한다
        req.setGoalType(goalType);
        // GoalCnt 업무 값을 req DTO에 설정한다
        req.setGoalCnt(goalCnt);
        // ReadingGoal 업무 값을 reportMapper DTO에 설정한다
        reportMapper.setReadingGoal(req);
        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }

    /**
     * 목표 권수를 낮추는 요청인지 판단하고 낮추기 제한을 검증한다.
     * 신규 설정 또는 목표 올리기는 제한하지 않고, 낮추기만 횟수와 마감 기간을 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param currentGoal 현재 저장된 목표
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @param goalCnt 새 목표 권수
     * @return 제한 위반 결과 코드, 통과하면 null
     */
    private ResultEnum validateReadingGoalDown(ReadingGoalDto currentGoal, LocalDate today, String goalType
                                             , Integer goalCnt) {

        // 신규 목표 설정이거나 목표를 올리는 요청이면 내리기 제한을 적용하지 않는다.
        if (StringUtil.isEmpty(currentGoal) || currentGoal.getGoalCnt() <= goalCnt) {

            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 목표 내리기 허용 횟수를 모두 사용한 경우 더 이상 목표를 낮출 수 없다.
        if (getGoalUpdateLimit(goalType) <= getGoalUpdateCount(currentGoal)) {

            // 목표 권수를 낮추는 요청인지 판단하고 낮추기 제한을 검증 결과를 반환한다
            return ResultEnum.COMMON_INVALID_REQUEST;
        }

        // 목표 내리기 가능 기간이 마감된 경우 목표를 낮출 수 없다.
        if (isGoalUpdateLocked(today, goalType)) {

            // 목표 권수를 낮추는 요청인지 판단하고 낮추기 제한을 검증 결과를 반환한다
            return ResultEnum.COMMON_INVALID_REQUEST;
        }
        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }

    /**
     * 목표 유형별 목표 내리기 가능 횟수를 반환한다.
     * 주간 1회, 월간 3회, 연간 5회 제한을 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param goalType 목표 유형
     * @return 목표 내리기 허용 횟수
     */
    private int getGoalUpdateLimit(String goalType) {

        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {

            // 목표 유형별 목표 내리기 가능 횟수를 반환한다
            return WEEK_GOAL_MAX_UPDATE_COUNT;
        }

        // 월간 목표는 주간보다 넓은 기간을 다루므로 3회까지 목표 내리기를 허용한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {

            // 목표 유형별 목표 내리기 가능 횟수를 반환한다
            return MONTH_GOAL_MAX_UPDATE_COUNT;
        }
        // 목표 유형별 목표 내리기 가능 횟수를 반환한다
        return YEAR_GOAL_MAX_UPDATE_COUNT;
    }

    /**
     * 현재 목표의 사용 횟수를 기준으로 목표 내리기 잔여 횟수를 계산한다.
     * 목표가 아직 없으면 유형별 전체 허용 횟수를 그대로 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param currentGoal 현재 목표
     * @param goalType 목표 유형
     * @return 목표 내리기 잔여 횟수
     */
    private int getGoalRemainUpdateCount(ReadingGoalDto currentGoal, String goalType) {

        // 저장된 목표가 아직 없으면 유형별 전체 내리기 횟수를 잔여 횟수로 표시한다.
        if (StringUtil.isEmpty(currentGoal)) {

            // 현재 목표의 사용 횟수를 기준으로 목표 내리기 잔여 횟수를 계산 결과를 반환한다
            return getGoalUpdateLimit(goalType);
        }
        // 현재 목표의 사용 횟수를 기준으로 목표 내리기 잔여 횟수를 계산 결과를 반환한다
        return Math.max(0, getGoalUpdateLimit(goalType) - getGoalUpdateCount(currentGoal));
    }

    /**
     * 목표 내리기 사용 횟수가 null이면 0으로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param currentGoal 현재 목표
     * @return 목표 내리기 사용 횟수
     */
    private int getGoalUpdateCount(ReadingGoalDto currentGoal) {

        // 목표 내리기 사용 횟수가 null이면 0으로 보정 결과를 반환한다
        return StringUtil.isEmpty(currentGoal.getUpdtCntt()) ? 0 : currentGoal.getUpdtCntt();
    }

    /**
     * 목표 내리기 가능 기간이 지났는지 판단한다.
     * 주간은 해당 주 종료 3일 전부터, 월간은 월 종료 7일 전부터, 연간은 12월 1일부터 내리기를 막는다.
     *
     * @author SeungHyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @return 목표 내리기 마감 여부
     */
    private boolean isGoalUpdateLocked(LocalDate today, String goalType) {

        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {

            // 목표 기간 계산에 사용할 기준 요일로 날짜를 조정한다
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            // 목표 내리기 가능 기간이 지났는지 판단 결과를 반환한다
            return ChronoUnit.DAYS.between(today, weekLastDay) <= WEEK_GOAL_LOCK_REMAINING_DAYS;
        }

        // 월간 목표는 월 종료 7일 전부터 내리기를 막기 위해 월 마지막 날을 기준으로 계산한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {

            // 기준 월에서 필요한 일자로 날짜를 조정한다
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            // 목표 내리기 가능 기간이 지났는지 판단 결과를 반환한다
            return ChronoUnit.DAYS.between(today, monthLastDay) <= MONTH_GOAL_LOCK_REMAINING_DAYS;
        }
        // 목표 내리기 가능 기간이 지났는지 판단 결과를 반환한다
        return today.getMonthValue() == 12;
    }

    /**
     * 목표 내리기가 가능한 잔여 일수를 계산한다.
     * 기간이 이미 마감되었으면 음수가 내려가지 않도록 0으로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param today 현재 날짜
     * @param goalType 목표 유형
     * @return 목표 내리기 가능 잔여 일수
     */
    private int getGoalEditableRemainDays(LocalDate today, String goalType) {

        // 주간 목표는 ISO 주차 기준값을 사용해야 하므로 별도 변환 로직으로 분기한다.
        if (Constant.GOAL_TYPE_WEEK.equals(goalType)) {

            // 목표 기간 계산에 사용할 기준 요일로 날짜를 조정한다
            LocalDate weekLastDay = today.with(GOAL_WEEK_FIELDS.dayOfWeek(), 7);
            // 목표 내리기가 가능한 잔여 일수를 계산 결과를 반환한다
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, weekLastDay) - WEEK_GOAL_LOCK_REMAINING_DAYS);
        }

        // 월간 목표의 내리기 가능 잔여 일수는 월 마지막 날에서 잠금 기준일을 뺀 값으로 계산한다.
        if (Constant.GOAL_TYPE_MONTH.equals(goalType)) {

            // 기준 월에서 필요한 일자로 날짜를 조정한다
            LocalDate monthLastDay = today.withDayOfMonth(today.lengthOfMonth());
            // 목표 내리기가 가능한 잔여 일수를 계산 결과를 반환한다
            return Math.max(0, (int) ChronoUnit.DAYS.between(today, monthLastDay) - MONTH_GOAL_LOCK_REMAINING_DAYS);
        }

        // 필요한 값으로 불변 객체를 생성한다
        LocalDate yearLockDate = LocalDate.of(today.getYear(), 12, 1);
        // 목표 내리기가 가능한 잔여 일수를 계산 결과를 반환한다
        return Math.max(0, (int) ChronoUnit.DAYS.between(today, yearLockDate));
    }

    /**
     * 완료 독후감 기간 집계에 사용할 요청 DTO를 생성한다.
     * 종료 경계는 중복 집계를 피하기 위해 exclusive 값으로 전달한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param periodStart 기간 시작일
     * @param periodEndExclusive 기간 종료 다음 일자
     * @return 기간 집계 요청 DTO
     */
    private MonthlyReadingSummaryDto getSummaryReportReq(Long userNumb, LocalDate periodStart, LocalDate periodEndExclusive
                                                       , String reptStat, String reportOrderType) {

        // 월별 독서 요약 결과를 담을 객체를 생성한다
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // PeriodStart 업무 값을 req DTO에 설정한다
        req.setPeriodStart(periodStart.toString());
        // PeriodEndExclusive 업무 값을 req DTO에 설정한다
        req.setPeriodEndExclusive(periodEndExclusive.toString());
        // ReptStat 업무 값을 req DTO에 설정한다
        req.setReptStat(reptStat);
        // ReportOrderType 업무 값을 req DTO에 설정한다
        req.setReportOrderType(reportOrderType);
        // 완료 독후감 기간 집계에 사용할 요청 DTO를 생성 결과를 반환한다
        return req;
    }

    private MonthlyReadingSummaryDto getSummaryReportReq(Long userNumb, String reptStat, String reportOrderType) {

        // 월별 독서 요약 결과를 담을 객체를 생성한다
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // ReptStat 업무 값을 req DTO에 설정한다
        req.setReptStat(reptStat);
        // ReportOrderType 업무 값을 req DTO에 설정한다
        req.setReportOrderType(reportOrderType);
        // 요약 독후감 req 조회 결과를 반환한다
        return req;
    }

            /**
     * 로그인 사용자가 작성한 독후감 상세 정보와 연결된 도서 정보를 조회한다.
     * 사용자 번호를 조건에 포함해 다른 사용자의 비공개 독후감을 조회하지 못하도록 한다.
     *
     * @author SeungHyeon.Kang
     * @return 독후감 상세 조회 결과
     */
    private String normalizeListSortType(String sortType) {

        // 허용된 정렬값만 Mapper에 전달해 동적 정렬 조건이 임의로 확장되지 않게 한다.
        if (Constant.SORT_START_DATE_DESC.equals(sortType) || Constant.SORT_GRADE_DESC.equals(sortType)) {

            // 로그인 사용자가 작성한 독후감 상세 정보와 연결된 도서 정보를 조회 결과를 반환한다
            return sortType;
        }
        // 로그인 사용자가 작성한 독후감 상세 정보와 연결된 도서 정보를 조회 결과를 반환한다
        return Constant.SORT_END_DATE_DESC;
    }
    /**
     * 독후감 번호로 독후감과 도서 상세 정보를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 사용자 번호
     * @param reptNumb 조회하거나 수정할 독후감 번호
     * @return 업무 처리 성공 또는 실패 응답
     */
    @Override
    public ResultData getDetail(Long userNumb, Long reptNumb) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reptNumb)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 독후감 또는 독서 목표 처리 데이터를 담을 객체를 생성한다
        ReportDto reportDto = new ReportDto();
        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // ReptNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptNumb(reptNumb);
        // Locale 업무 값을 reportDto DTO에 설정한다
        reportDto.setLocale(LocaleUtil.getLocale());
        // ReptStat 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptStat(Constant.REPORT_STAT_DONE);

        // ReportDtl 데이터를 DB에서 조회한다
        ReportDto detail = reportMapper.getReportDtl(reportDto);

        // 조회 결과가 없으면 존재하지 않거나 접근할 수 없는 독후감으로 판단한다.
        if (StringUtil.isEmpty(detail)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }
        // 독후감 번호로 독후감과 도서 상세 정보를 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(detail);
    }

    /**
     * ISBN 기준으로 공개 독후감 목록을 조회한다.
     * 로그인 사용자의 좋아요 여부를 함께 표시하기 위해 사용자 번호를 Mapper에 전달한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookIsbn 조회할 도서 ISBN
     * @return 공개 독후감 목록 조회 결과
     */
    @Override
    public ResultData getPublicReportsByIsbn(Long userNumb, String bookIsbn) {

        // ISBN이 없으면 도서를 특정할 수 없으므로 공개 독후감 또는 평균 별점을 조회하지 않는다.
        if (StringUtil.isEmpty(bookIsbn)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 독후감 또는 독서 목표 처리 데이터를 담을 객체를 생성한다
        ReportDto reportDto = new ReportDto();
        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // BookIsbn 업무 값을 reportDto DTO에 설정한다
        reportDto.setBookIsbn(StringUtil.normalizePlainText(bookIsbn));
        // ISBN 기준으로 공개 독후감 목록을 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(reportMapper.getPublicReportList(reportDto));
    }

    /**
     * ISBN 기준 도서 평균 별점을 조회한다.
     * 평균 별점은 공개 여부와 무관하게 전체 독후감을 기준으로 계산한다.
     *
     * @author SeungHyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점 조회 결과
     */
    @Override
    public ResultData getPublicRatingAverageByIsbn(String bookIsbn) {

        // ISBN이 없으면 도서를 특정할 수 없으므로 공개 독후감 또는 평균 별점을 조회하지 않는다.
        if (StringUtil.isEmpty(bookIsbn)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }
        // ISBN 기준 도서 평균 별점을 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(reportMapper.getPublicRatingAverageByIsbn(StringUtil.normalizePlainText(bookIsbn)));
    }

    /**
     * 독후감과 필요한 도서 정보를 등록한다.
     * 도서가 이미 존재하면 기존 도서 번호를 재사용하고, 없으면 도서를 먼저 등록한 뒤 독후감을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportDto 등록할 독후감 및 도서 정보
     * @return 등록된 독후감 번호
     */
    @Override
    @Transactional
    public ResultData setReport(Long userNumb, ReportDto reportDto) {

        // 등록 요청의 도서 필수값이 누락되면 도서와 독후감 저장을 모두 중단한다.
        if (hasInvalidBookFields(reportDto)) {

            // "\uC120\uD0DD\uD55C \uCC45 \uC815\uBCF4\uAC00 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4. \uB2E4\uB978 \uCC45\uC744 \uC120\uD0DD\uD574\uC8FC\uC138\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_REPORT_BOOK_INVALID);
        }

        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // setDefaultReportColor 호출로 업무 처리에 필요한 값을 설정한다
        setDefaultReportColor(reportDto);
        // setDefaultPublicFlag 호출로 업무 처리에 필요한 값을 설정한다
        setDefaultPublicFlag(reportDto);
        // 독후감 입력값에서 허용하지 않는 스크립트 내용을 제거한다
        sanitizeReport(reportDto, true);

        // validateReport 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        ReportValidationResult validationResult = validateReport(reportDto, true);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {

            // 독후감과 필요한 도서 정보를 등록 과정에서 확인된 검증 실패 응답을 반환한다
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        // ISBN 기준 등록된 도서가 없을 때만 도서 마스터를 신규 생성한다.
        if (bookMapper.dupBook(reportDto) == 0) {

            // Book 업무 값을 bookMapper DTO에 설정한다
            bookMapper.setBook(reportDto);
        }
        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // BookNumb 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookNumb(bookMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        // Report 업무 값을 reportMapper DTO에 설정한다
        reportMapper.setReport(reportDto);
        // 독후감 등록 후 PK가 채워지지 않으면 저장 실패로 판단한다.
        if (StringUtil.isEmpty(reportDto.getReptNumb())) {

            // "\uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.\n\uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }
        // 독후감과 필요한 도서 정보를 등록 결과를 성공 응답으로 반환한다
        return ResultData.success(reportDto.getReptNumb());
    }

    /**
     * 기존 독후감 정보를 수정한다.
     * URL의 독후감 번호를 DTO에 주입해 클라이언트가 본문 번호를 조작해도 수정 대상이 바뀌지 않도록 한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 번호
     */
    @Override
    @Transactional
    public ResultData uptReport(Long userNumb, Long reptNumb, ReportDto reportDto) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reptNumb)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // ReptNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptNumb(reptNumb);
        // setDefaultReportColor 호출로 업무 처리에 필요한 값을 설정한다
        setDefaultReportColor(reportDto);
        // setDefaultPublicFlag 호출로 업무 처리에 필요한 값을 설정한다
        setDefaultPublicFlag(reportDto);
        // 독후감 입력값에서 허용하지 않는 스크립트 내용을 제거한다
        sanitizeReport(reportDto, false);

        // validateReport 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        ReportValidationResult validationResult = validateReport(reportDto, true);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {

            // 기존 독후감 정보를 수정 과정에서 확인된 검증 실패 응답을 반환한다
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (reportMapper.uptReport(reportDto) == 0) {

            // "\uC218\uC815\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.\n\uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }
        // 기존 독후감 정보를 수정 결과를 성공 응답으로 반환한다
        return ResultData.success(reportDto.getReptNumb());
    }

    /**
     * 마이페이지의 현재 읽고 있는 책 목록에서 독서 상태와 별점만 빠르게 수정한다.
     * 전체 독후감 수정 화면으로 이동하지 않아도 완료 여부와 평점만 즉시 반영할 수 있도록 별도 수정 범위를 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 독서 상태와 별점 정보
     * @return 수정 처리 결과
     */
    @Override
    @Transactional
    public ResultData uptReptStatusGrade(Long userNumb, Long reptNumb, ReportDto reportDto) {

        // 대상 독후감 번호가 없으면 수정 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reptNumb)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // ReptNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptNumb(reptNumb);
        // ReptGrde 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptGrde(StringUtil.normalizePlainText(reportDto.getReptGrde()));
        // ReptStat 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptStat(StringUtil.normalizePlainText(reportDto.getReptStat()));
        // ReptEndt 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptEndt(LocalDate.now().toString()); // 빠른 완료/중단 처리에서는 사용자가 저장한 시점을 실제 독서 종료일로 기록한다.

        // validateReport 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        ReportValidationResult validationResult = validateReport(reportDto, false);
        // 업무 검증 실패가 있으면 DB 변경 전에 사용자에게 전달할 실패 결과를 반환한다.
        if (!StringUtil.isEmpty(validationResult)) {

            // 마이페이지의 현재 읽고 있는 책 목록에서 독서 상태와 별점만 빠르게 수정 과정에서 확인된 검증 실패 응답을 반환한다
            return ResultData.fail(validationResult.resultEnum(), validationResult.args());
        }

        // 사용자 번호를 WHERE 조건에 함께 사용해 다른 사용자의 독후감은 수정되지 않도록 막는다.
        int result = reportMapper.uptReptStatusGrade(reportDto);

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (result == 0) {

            // "\uC218\uC815\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.\n\uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }
        // 마이페이지의 현재 읽고 있는 책 목록에서 독서 상태와 별점만 빠르게 수정 결과를 성공 응답으로 반환한다
        return ResultData.success(reportDto.getReptNumb());
    }

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     * 사용자 번호와 독후감 번호를 함께 조건으로 사용해 본인 독후감만 삭제되도록 한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 삭제할 독후감 번호
     * @return 삭제 처리 결과
     */
    @Override
    @Transactional
    public ResultData delReport(Long userNumb, Long reptNumb) {

        // 대상 독후감 번호가 없으면 상세, 수정, 삭제 대상을 특정할 수 없으므로 실패 처리한다.
        if (StringUtil.isEmpty(reptNumb)) {

            // "\uC870\uD68C \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 독후감 또는 독서 목표 처리 데이터를 담을 객체를 생성한다
        ReportDto reportDto = new ReportDto();
        // UserNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setUserNumb(userNumb);
        // ReptNumb 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptNumb(reptNumb);

        // 삭제 반영 건수가 없으면 본인 독후감이 아니거나 이미 삭제된 데이터로 판단한다.
        if (reportMapper.delReport(reportDto) == 0) {

            // "\uC0AD\uC81C\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.\n\uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        // TB_LIKEXX는 TAGT_TYPE 기반 공용 좋아요 테이블이라 DB FK cascade를 걸 수 없다.
        // 독후감 삭제가 성공한 뒤 REPORT 대상 좋아요만 명시적으로 정리해 고아 좋아요 데이터를 남기지 않는다.
        SocialDto.LikeDto likeDto = new SocialDto.LikeDto();
        // TagtType 업무 값을 likeDto DTO에 설정한다
        likeDto.setTagtType(Constant.LIKE_TARGET_REPORT);
        // TagtNumb 업무 값을 likeDto DTO에 설정한다
        likeDto.setTagtNumb(reportDto.getReptNumb());
        // LikeByTarget 데이터를 DB에서 삭제한다
        socialMapper.delLikeByTarget(likeDto);
        // 로그인 사용자의 독후감을 삭제 결과를 성공 응답으로 반환한다
        return ResultData.success();
    }
    /**
     * 독후감 등록에 필요한 도서 필수값이 모두 존재하는지 확인한다.
     * 도서 검색 API 응답을 조작해 들어오는 경우에도 백엔드에서 한 번 더 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 검증할 독후감 및 도서 정보
     * @return 도서 필수값 누락 여부
     */
    private boolean hasInvalidBookFields(ReportDto reportDto) {

        // 독후감 등록에 필요한 도서 필수값이 모두 존재하는지 확인 결과를 반환한다
        return StringUtil.isEmpty(reportDto) || StringUtil.hasEmpty(
                // getBookTitl 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookTitl(),
                // getBookAthr 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookAthr(),
                // getBookPubl 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookPubl(),
                // getBookIsbn 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookIsbn(),
                // getBookCvim 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookCvim(),
                // getBookDesc 조회로 후속 처리에 필요한 데이터를 가져온다
                reportDto.getBookDesc()
        );
    }

    /**
     * 독후감 등록과 수정에 공통으로 적용되는 업무 검증을 수행한다.
     * 필수값, 공통코드, 날짜 범위, 본문 byte 길이, 공개 여부 코드를 순서대로 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 검증할 독후감 정보
     * @param isFullScan 독후감 내용을 모두 유효성 검사 할 것인지를 판단
     * @return 검증 실패 결과, 통과하면 null
     */
    private ReportValidationResult validateReport(ReportDto reportDto, boolean isFullScan) {

        List<String> missingFields = new ArrayList<>();
        // 두 값이 동일한지 안전하게 비교한다
        boolean isReadingStatus = Constant.REPORT_STAT_READ.equals(reportDto.getReptStat());
        // 필수 값이 비어 있는지 공통 기준으로 확인한다
        boolean hasReportContent = !StringUtil.isEmpty(reportDto.getReptCntn());

        // 독서 상태는 필수값이며 READ_STAT 공통코드에 등록된 값만 저장한다.
        if (StringUtil.isEmpty(reportDto.getReptStat()) || !codeUtil.existsCode(Constant.CODE_READ_STAT, reportDto.getReptStat())) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingFields.add(MessageUtils.getMessage(REPORT_FIELD_STATUS_KEY));
        }

        // 종료일은 상태와 관계없이 기간 계산에 필요하므로 필수값으로 검증한다.
        if (StringUtil.isEmpty(reportDto.getReptEndt())) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingFields.add(MessageUtils.getMessage(REPORT_FIELD_END_DATE_KEY));
        }

        // 도서 평점의 저장값이 없으면 저장값을 0점으로 보정해 저장값을 숫자로 유지한다.
        if (StringUtil.isEmpty(reportDto.getReptGrde())) {

            // ReptGrde 업무 값을 reportDto DTO에 설정한다
            reportDto.setReptGrde("0");
        }

        //등록 수정화면에서 행해지는 등록 및 수정은 모든 값을 입력받아야한다.
        if(isFullScan) {

            // 시작일은 상태와 관계없이 기간 계산에 필요하므로 필수값으로 검증한다.
            if (StringUtil.isEmpty(reportDto.getReptStdt())) {

                // 처리한 값을 결과 컬렉션에 추가한다
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_START_DATE_KEY));
            }

            // 다 읽었어요 상태의 빈 평점이나 0점부터 5점까지의 정수 범위를 벗어난 값은 저장하지 않는다.
            if (!isValidReportGrade(reportDto.getReptGrde())) {

                // 처리한 값을 결과 컬렉션에 추가한다
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_GRADE_KEY));
            }

            // 책장 색상은 필수값이며 BOOK_COLR 공통코드에 등록된 값만 저장한다.
            if (StringUtil.isEmpty(reportDto.getReptColr()) || !codeUtil.existsCode(Constant.CODE_BOOK_COLR, reportDto.getReptColr())) {

                // 처리한 값을 결과 컬렉션에 추가한다
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_COLOR_KEY));
            }

            // 읽고 있어요 상태는 사용자가 아직 기록을 남기지 않을 수 있으므로 본문 필수 검증에서 제외한다.
            // 완료/중단 상태는 실제 독후감 기록 저장 단계이므로 기존처럼 본문을 필수값으로 유지한다.
            if (!isReadingStatus && !hasReportContent) {

                // 처리한 값을 결과 컬렉션에 추가한다
                missingFields.add(MessageUtils.getMessage(REPORT_FIELD_CONTENT_KEY));
            }

            // 필수값 누락이 하나라도 있으면 누락 항목 목록을 메시지 인자로 반환한다.
            if (!missingFields.isEmpty()) {

                // 새로 생성한 ReportValidationResult 객체를 반환한다
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_REQUIRED_MISSING, formatMissingFields(missingFields));
            }
            // 시작일이 종료일보다 늦은 데이터는 프론트 조작 여부와 관계없이 저장하지 않는다.
            if (!DateUtil.validateReportDateRange(reportDto.getReptStdt(), reportDto.getReptEndt())) {

                // 새로 생성한 ReportValidationResult 객체를 반환한다
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_DATE_RANGE_INVALID);
            }

            // Oracle 저장 한도를 넘는 본문은 DB 오류가 나기 전에 업무 검증으로 차단한다.
            if (hasReportContent && XssUtil.utf8ByteLength(reportDto.getReptCntn()) > Constant.REPORT_CONTENT_MAX_BYTES) {

                // 새로 생성한 ReportValidationResult 객체를 반환한다
                return new ReportValidationResult(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, Constant.REPORT_CONTENT_MAX_BYTES);
            }

            // 비속어 필터링
            if (hasReportContent) {

                // findBadWord 업무 로직을 badWordDetectionService에 위임한다
                Optional<String> badWord = badWordDetectionService.findBadWord(reportDto.getReptCntn());
                // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
                if (badWord.isPresent()) {

                    // 새로 생성한 ReportValidationResult 객체를 반환한다
                    return new ReportValidationResult(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
                }
            }

            // 공개 여부는 Y 또는 N만 허용해 공개 독후감 조회 조건을 안정적으로 유지한다.
            if (!Constant.COMM_YES.equals(reportDto.getPubcYsno()) && !Constant.COMM_NO.equals(reportDto.getPubcYsno())) {

                // 새로 생성한 ReportValidationResult 객체를 반환한다
                return new ReportValidationResult(ResultEnum.COMMON_INVALID_REQUEST);
            }
        }
        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }

    /**
     * 누락된 필수 항목 목록을 사용자에게 보여줄 수 있는 줄바꿈 문장으로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param missingFields 누락된 필드 표시명 목록
     * @return 필수값 누락 메시지 인자
     */
    private String formatMissingFields(List<String> missingFields) {

        // 누락된 필수 항목 목록을 사용자에게 보여줄 수 있는 줄바꿈 문장으로 변환 결과를 반환한다
        return "- " + String.join("\n- ", missingFields);
    }

    /**
     * 별점 값이 숫자이며 0점부터 5점 범위 안의 정수인지 확인한다.
     * 0점은 읽고있어요 상태에서 별점을 선택하지 않은 값을 저장하기 위한 내부 보정값으로 허용한다.
     *
     * @author SeungHyeon.Kang
     * @param reptGrde 검증할 별점 문자열
     * @return 유효한 별점 여부
     */
    private boolean isValidReportGrade(String reptGrde) {

        // 별점이 비어 있으면 호출한 검증 흐름에서 상태별 필수 여부를 먼저 판단하도록 false를 반환한다.
        if (StringUtil.isEmpty(reptGrde)) {

            // 별점 값이 숫자이며 0점부터 5점 범위 안의 정수인지 확인 판정값을 반환한다
            return false;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // parseInt 호출로 입력값을 필요한 데이터 형식으로 변환한다
            int grade = Integer.parseInt(reptGrde);

            // 별점 값이 숫자이며 0점부터 5점 범위 안의 정수인지 확인 결과를 반환한다
            return grade >= 0 && grade <= 5;
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (NumberFormatException e) {

            // 별점 값이 숫자이며 0점부터 5점 범위 안의 정수인지 확인 판정값을 반환한다
            return false;
        }
    }
    private void setDefaultReportColor(ReportDto reportDto) {

        // 책장 색상은 필수값이며 공통코드에 등록된 색상 코드만 허용한다.
        if (StringUtil.isEmpty(reportDto.getReptColr()) || reportDto.getReptColr().isBlank()) {

            // ReptColr 업무 값을 reportDto DTO에 설정한다
            reportDto.setReptColr(codeUtil.getFirstCode(Constant.CODE_BOOK_COLR));
        }
    }

    /**
     * 공개 여부 값이 비어 있으면 비공개로 기본 설정한다.
     * 사용자가 명시적으로 공개를 선택하지 않은 독후감이 외부에 노출되지 않도록 한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 공개 여부 기본값을 반영할 독후감 DTO
     */
    private void setDefaultPublicFlag(ReportDto reportDto) {

        // 공개 여부는 Y 또는 N만 허용해 공개 독후감 조회 조건을 안정적으로 유지한다.
        if (StringUtil.isEmpty(reportDto.getPubcYsno()) || reportDto.getPubcYsno().isBlank()) {

            // PubcYsno 업무 값을 reportDto DTO에 설정한다
            reportDto.setPubcYsno(Constant.COMM_NO);
        }
    }

    /**
     * 독후감 입력값의 HTML entity와 불필요한 텍스트 표현을 일반 문자열로 정규화한다.
     * 등록 시에는 도서 정보도 함께 정규화하고, 수정 시에는 독후감 필드만 정규화한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 정규화할 독후감 DTO
     * @param includeBookFields 도서 필드 정규화 포함 여부
     */
    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {

        // ReptStat 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptStat(StringUtil.normalizePlainText(reportDto.getReptStat()));
        // ReptStdt 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptStdt(StringUtil.normalizePlainText(reportDto.getReptStdt()));
        // ReptEndt 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptEndt(StringUtil.normalizePlainText(reportDto.getReptEndt()));
        // ReptGrde 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptGrde(StringUtil.normalizePlainText(reportDto.getReptGrde()));
        // ReptColr 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptColr(StringUtil.normalizePlainText(reportDto.getReptColr()));
        // PubcYsno 업무 값을 reportDto DTO에 설정한다
        reportDto.setPubcYsno(StringUtil.normalizePlainText(reportDto.getPubcYsno()));
        // ReptCntn 업무 값을 reportDto DTO에 설정한다
        reportDto.setReptCntn(StringUtil.normalizePlainText(reportDto.getReptCntn()));

        // 등록 요청일 때만 도서 필드를 함께 정규화하고, 수정 요청에서는 독후감 필드만 정규화한다.
        if (includeBookFields) {

            // BookTitl 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookTitl(StringUtil.normalizePlainText(reportDto.getBookTitl()));
            // BookAthr 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookAthr(StringUtil.normalizePlainText(reportDto.getBookAthr()));
            // BookPubl 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookPubl(StringUtil.normalizePlainText(reportDto.getBookPubl()));
            // BookIsbn 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookIsbn(StringUtil.normalizePlainText(reportDto.getBookIsbn()));
            // BookCvim 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookCvim(StringUtil.normalizePlainText(reportDto.getBookCvim()));
            // BookDesc 업무 값을 reportDto DTO에 설정한다
            reportDto.setBookDesc(StringUtil.normalizePlainText(reportDto.getBookDesc()));
        }
    }

    /**
     * 독후감 검증 실패 결과와 메시지 인자를 함께 전달하기 위한 내부 record이다.
     *
     * @author SeungHyeon.Kang
     * @param resultEnum 실패 결과 코드
     * @param args 메시지 치환 인자
     */
    private record ReportValidationResult(ResultEnum resultEnum, Object... args) {

    }
}
