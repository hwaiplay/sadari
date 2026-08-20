package org.our.sadari.myPage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.myPage.dto.ReadingHeatmapDto;
import org.our.sadari.myPage.dto.ReadingHeatmapRowDto;
import org.our.sadari.myPage.dto.ReadingStatisticsAggregateDto;
import org.our.sadari.myPage.dto.ReadingStatisticsDto;
import org.our.sadari.myPage.dto.ReadingStatisticsQueryDto;
import org.our.sadari.myPage.dto.ReadingStatisticsSettingDto;
import org.our.sadari.myPage.mapper.ReadingStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReadingStatisticsServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 저장된 타이머와 독후감 데이터를 독서 습관 및 연도별 통계로 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 독서 통계 처리
 * 2026-08-15        SeungHyeon.Kang    잔디와 통계 집계 SQL 왕복 통합
 */
@Service
@Transactional(readOnly = true)
public class ReadingStatisticsServiceImpl implements ReadingStatisticsService {

    // 독서 통계의 날짜 경계를 계산할 서울 시간대
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    // 마이페이지 독서 통계 데이터 접근 객체
    private final ReadingStatisticsMapper readingStatisticsMapper;
    // 날짜 경계를 현재 시각 또는 테스트 고정 시각으로 계산할 시계
    private final Clock clock;

    /**
     * 서울 현재 시각을 기준으로 독서 통계 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param readingStatisticsMapper 마이페이지 독서 통계 데이터 접근 객체
     */
    @Autowired
    public ReadingStatisticsServiceImpl(ReadingStatisticsMapper readingStatisticsMapper) {
        // 운영 통계의 날짜 경계를 서울 시간대로 고정한다
        this(readingStatisticsMapper, Clock.system(SEOUL_ZONE));
    }

    /**
     * 로그인 회원이 선택한 연도의 독서 시간 잔디만 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 조회 가능한 연도와 날짜별 독서 시간 잔디
     */
    @Override
    public ResultData getReadingHeatmap(Long userNumb, Integer readYear) {
        // 인증 사용자 번호가 없으면 다른 사용자의 잔디를 추측할 수 없도록 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 본인 잔디 조회 전에 계정 상태를 확인한다
        ReadingStatisticsSettingDto setting = readingStatisticsMapper.getReadingStatsSetting(userNumb
                                                                                             , Constant.COMM_NO);

        // 존재하지 않거나 제한된 계정은 개인 독서 시간을 제공하지 않는다
        if (StringUtil.isEmpty(setting) || !Constant.USER_STAT_ACTIVE.equals(setting.getUserStat())) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 마이페이지와 같은 Mapper 결과로 선택 연도의 잔디를 구성한다
        ReadingHeatmapDto heatmap = getHeatmap(userNumb, readYear, LocalDate.now(clock));

        // 현재 연도 또는 실제 기록이 존재하는 연도만 조회할 수 있다
        if (StringUtil.isEmpty(heatmap)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        // 상태 비율과 책 순위 없이 독서 잔디 데이터만 성공 응답으로 반환한다
        return ResultData.success(heatmap);
    }

    /**
     * 로그인 회원이 선택한 연도의 독서 시간과 전체 독서 상태 분포를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 본인 전용 독서 통계 조회 결과
     */
    @Override
    public ResultData getReadingStats(Long userNumb, Integer readYear) {
        // 인증 사용자 번호가 없으면 다른 사용자의 통계를 추측할 수 없도록 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 본인 통계의 공개 설정을 조회한다
        ReadingStatisticsSettingDto setting = readingStatisticsMapper.getReadingStatsSetting(userNumb
                                                                                             , Constant.COMM_NO);

        // 회원 설정이 없으면 삭제되거나 잘못된 계정의 통계 조회를 중단한다
        if (StringUtil.isEmpty(setting)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 인증 필터 밖에서 호출되더라도 제한 계정의 개인 통계를 제공하지 않는다
        if (!Constant.USER_STAT_ACTIVE.equals(setting.getUserStat())) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 요청 연도에 맞는 본인 독서 통계를 구성한다
        ReadingStatisticsDto statistics = getStatistics(userNumb, setting, readYear, true);

        // 요청할 수 없는 연도는 데이터가 없는 연도까지 임의 조회하지 않도록 중단한다
        if (StringUtil.isEmpty(statistics)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        // 지연 조회된 본인 전용 독서 통계를 성공 응답으로 반환한다
        return ResultData.success(statistics);
    }

    /**
     * 정상 이용 회원이 공개한 독서 통계를 다른 사용자 프로필에 제공한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 공개 통계를 조회할 프로필 회원 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 공개 허용 시 독서 통계, 비공개 또는 제한 계정이면 빈 데이터
     */
    @Override
    public ResultData getPublicReadingStats(Long userNumb, Integer readYear) {
        // 경로의 사용자 번호가 없으면 공개 프로필 통계 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 서버가 공개 여부와 계정 상태를 직접 판단할 회원 설정을 조회한다
        ReadingStatisticsSettingDto setting = readingStatisticsMapper.getReadingStatsSetting(userNumb
                                                                                             , Constant.COMM_NO);

        // 비공개 또는 제한 계정은 설정 존재 여부도 구분되지 않도록 빈 성공 데이터를 반환한다
        if (StringUtil.isEmpty(setting) || !Constant.USER_STAT_ACTIVE.equals(setting.getUserStat())
                || !Constant.COMM_YES.equals(setting.getPublicYsno())) {
            // 공개할 수 없는 회원의 통계 데이터가 없음을 반환한다
            return ResultData.success(null);
        }

        // 요청 연도로 다른 사용자용 독서 통계를 구성한다
        ReadingStatisticsDto statistics = getStatistics(userNumb, setting, readYear, false);

        // 공개 화면에서도 실제 기록이 없고 현재 연도가 아닌 임의 연도는 조회하지 않는다
        if (StringUtil.isEmpty(statistics)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        // 공개를 허용한 회원의 독서 통계를 성공 응답으로 반환한다
        return ResultData.success(statistics);
    }

    /**
     * 로그인 회원의 독서 통계 공개 범위를 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 설정을 변경할 로그인 회원 번호
     * @param setting 선택한 공개 여부
     * @return 저장된 공개 여부 코드
     */
    @Transactional
    @Override
    public ResultData uptReadingStatsSetting(Long userNumb, ReadingStatisticsSettingDto setting) {
        // 회원 번호와 설정이 모두 있어야 다른 계정 설정을 변경하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(setting)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 서버 허용 목록에 없는 공개 코드는 저장하지 않는다
        if (!isAllowedPublic(setting.getPublicYsno())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 회원 설정 행이 없을 때도 계정 상태를 확인하도록 회원과 설정을 함께 조회한다
        ReadingStatisticsSettingDto currentSetting = readingStatisticsMapper.getReadingStatsSetting(userNumb
                                                                                                    , Constant.COMM_NO);

        // 존재하지 않거나 제한된 계정은 범용 회원 설정 행을 생성하거나 변경하지 않는다
        if (StringUtil.isEmpty(currentSetting) || !Constant.USER_STAT_ACTIVE.equals(currentSetting.getUserStat())) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 요청 본문의 사용자 값 대신 인증 사용자 번호를 저장 조건에 설정한다
        setting.setUserNumb(userNumb);
        // 범용 회원 설정 행을 생성하거나 기존 독서 통계 공개 컬럼을 변경한다
        readingStatisticsMapper.uptReadingStatsSetting(setting);

        // 변경된 공개 여부를 화면에서 즉시 반영할 수 있도록 반환한다
        return ResultData.success(setting.getPublicYsno());
    }

    /**
     * 테스트에서 고정 날짜 경계를 사용할 독서 통계 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param readingStatisticsMapper 마이페이지 독서 통계 데이터 접근 객체
     * @param clock 날짜 경계를 고정할 시계
     */
    ReadingStatisticsServiceImpl(ReadingStatisticsMapper readingStatisticsMapper, Clock clock) {

        this.readingStatisticsMapper = readingStatisticsMapper;
        this.clock = clock;
    }

    /**
     * 선택 연도의 날짜별 독서 시간과 전체 독서 상태 통계를 구성한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 통계를 구성할 회원 번호
     * @param setting 검증된 공개 설정
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @param includeReportLink 본인 전용 독후감 상세 번호 포함 여부
     * @return 그래프에 사용할 독서 통계, 조회할 수 없는 연도이면 null
     */
    private ReadingStatisticsDto getStatistics(Long userNumb
                                               , ReadingStatisticsSettingDto setting
                                               , Integer readYear
                                               , boolean includeReportLink) {
        // 서울 기준 오늘을 현재 연도와 조회 종료일 계산에 사용한다
        LocalDate today = LocalDate.now(clock);
        // 마이페이지 전체 통계도 잔디 전용 조회와 같은 연도 및 일별 시간 구성을 재사용한다
        ReadingHeatmapDto heatmap = getHeatmap(userNumb, readYear, today);

        // 요청할 수 없는 연도는 전체 통계도 제공하지 않는다
        if (StringUtil.isEmpty(heatmap)) {
            // 임의 연도 요청은 호출부가 공통 오류로 처리하도록 빈 결과를 반환한다
            return null;
        }
        // 현재 연도와 전년도 같은 기간을 사용하는 추가 통계 조회 조건을 구성한다
        ReadingStatisticsQueryDto statisticsQuery = getStatisticsQuery(userNumb, today);
        // 독서 상태와 연속 기록 및 별점과 연도 비교값을 한 번에 집계한다
        ReadingStatisticsAggregateDto aggregate = readingStatisticsMapper.getStatsAggregate(statisticsQuery);
        // 현재 연도에 완료한 타이머 시간을 도서별로 합산한 상위 세 권을 조회한다
        List<ReadingStatisticsDto.BookTime> topBookList = readingStatisticsMapper.getTopBookTimeList(statisticsQuery);
        // 통합 집계가 비어도 화면 구조를 유지할 기본값 객체를 생성한다
        ReadingStatisticsAggregateDto safeAggregate = StringUtil.isEmpty(aggregate)
                ? new ReadingStatisticsAggregateDto()
                : aggregate;
        // 상태 집계 컬럼을 기존 화면 목록 구조로 변환한다
        List<ReadingStatisticsDto.Status> storedStatusList = List.of(
                new ReadingStatisticsDto.Status(Constant.REPORT_STAT_READ, safeAggregate.getReadCount())
              , new ReadingStatisticsDto.Status(Constant.REPORT_STAT_DONE, safeAggregate.getDoneCount())
              , new ReadingStatisticsDto.Status(Constant.REPORT_STAT_STOP, safeAggregate.getStopCount())
        );
        // 별점 집계 컬럼을 기존 화면 목록 구조로 변환한다
        List<ReadingStatisticsDto.Rating> storedRatingList = List.of(
                new ReadingStatisticsDto.Rating(BigDecimal.ZERO, safeAggregate.getRatingZeroCount())
              , new ReadingStatisticsDto.Rating(BigDecimal.ONE, safeAggregate.getRatingOneCount())
              , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(2L), safeAggregate.getRatingTwoCount())
              , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(3L), safeAggregate.getRatingThreeCount())
              , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(4L), safeAggregate.getRatingFourCount())
              , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(5L), safeAggregate.getRatingFiveCount())
        );
        // 연도 비교 집계 컬럼을 기존 화면 응답 구조로 변환한다
        ReadingStatisticsDto.YearComparison yearComparison = new ReadingStatisticsDto.YearComparison(statisticsQuery.getCurrentYear(), statisticsQuery.getPreviousYear(), safeAggregate.getCurrentReadSecs(), safeAggregate.getPreviousReadSecs(), safeAggregate.getCurrentReadDays(), safeAggregate.getPreviousReadDays(), safeAggregate.getCurrentDoneBooks(), safeAggregate.getPreviousDoneBooks());
        // 읽는 중, 완독, 중단 상태가 0건이어도 모두 표시할 상태 목록을 생성한다
        List<ReadingStatisticsDto.Status> statusList = getStatusList(storedStatusList);
        // SQL이 계산한 현재 및 최장 연속 독서일을 화면 응답으로 변환한다
        ReadingStatisticsDto.Streak streak = new ReadingStatisticsDto.Streak(Math.max(0, safeAggregate.getCurrentStreakDays()), Math.max(0, safeAggregate.getLongestStreakDays()));
        // 책별 시간이 음수가 되거나 목록 수가 세 권을 넘지 않도록 화면 응답을 보정한다
        List<ReadingStatisticsDto.BookTime> safeTopBookList = getTopBookList(topBookList, includeReportLink);
        // 0건인 별점도 막대그래프에 표시하도록 소수점 버림한 1점 단위 전체 목록을 구성한다
        List<ReadingStatisticsDto.Rating> ratingList = getRatingList(storedRatingList);
        // 집계 결과가 없거나 음수인 경우에도 두 연도의 비교 구조를 유지한다
        ReadingStatisticsDto.YearComparison safeYearComparison = getYearComparison(yearComparison, statisticsQuery);
        // 본인 또는 공개 화면에 전달할 독서 통계 응답을 생성한다
        ReadingStatisticsDto response = new ReadingStatisticsDto();
        // 잔디에 포함된 첫 날짜를 설정한다
        response.setHeatmapStart(heatmap.getHeatmapStart());
        // 잔디에 포함된 마지막 날짜를 설정한다
        response.setHeatmapEnd(heatmap.getHeatmapEnd());
        // 선택 연도의 날짜별 독서 시간 목록을 설정한다
        response.setHeatmapList(heatmap.getHeatmapList());
        // 읽는 중, 완독, 중단 상태별 독후감 수 목록을 설정한다
        response.setStatusList(statusList);
        // 현재 및 최장 연속 독서일을 설정한다
        response.setStreak(streak);
        // 현재 연도에 타이머로 오래 읽은 도서 상위 목록을 설정한다
        response.setTopBookList(safeTopBookList);
        // 0점부터 5점까지의 정수 별점별 독후감 수를 설정한다
        response.setRatingList(ratingList);
        // 현재 연도와 전년도 같은 기간의 독서 기록 비교값을 설정한다
        response.setYearComparison(safeYearComparison);
        // 잔디에 표시한 연도를 설정한다
        response.setSelectedYear(heatmap.getSelectedYear());
        // 화면에서 조회할 수 있는 연도 목록을 설정한다
        response.setAvailableYears(heatmap.getAvailableYears());
        // 본인 화면에 현재 공개 상태를 표시할 공개 여부를 설정한다
        response.setPublicYsno(setting.getPublicYsno());
        // 선택 연도의 독서 통계를 반환한다
        return response;
    }

    /**
     * 마이페이지와 타이머 화면이 함께 사용할 선택 연도 독서 잔디를 구성한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 잔디를 구성할 회원 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @param today 서울 시간대 기준 오늘
     * @return 조회 가능한 연도와 날짜별 독서 시간, 조회할 수 없는 연도이면 null
     */
    private ReadingHeatmapDto getHeatmap(Long userNumb, Integer readYear, LocalDate today) {
        // 현재 연도는 기록이 없어도 항상 조회할 수 있도록 기준값으로 보관한다
        int currentYear = today.getYear();
        // 연도가 없으면 현재 연도를 기본 조회 대상으로 사용한다
        int selectedYear = StringUtil.isEmpty(readYear) ? currentYear : readYear;

        // 미래 연도와 LocalDate가 지원하지 않는 연도는 DB 조회 전에 차단한다
        if (selectedYear < 1 || selectedYear > currentYear) {
            // 호출부가 잘못된 연도 요청으로 처리하도록 빈 결과를 반환한다
            return null;
        }

        // 선택 연도의 첫날부터 잔디 원천 데이터를 조회한다
        LocalDate periodStart = LocalDate.of(selectedYear, 1, 1);
        // 현재 연도는 오늘까지, 과거 연도는 마지막 날까지 조회한다
        LocalDate periodEnd = selectedYear == currentYear ? today : LocalDate.of(selectedYear, 12, 31);
        // 조회 가능한 연도와 선택 연도의 일별 독서 시간을 한 SQL에서 조회한다
        List<ReadingHeatmapRowDto> heatmapRowList = readingStatisticsMapper.getHeatmapRowList(userNumb
                                                                                            , periodStart
                                                                                            , periodEnd);
        // 저장된 연도 행을 최근 연도 순서로 보관할 목록을 생성한다
        List<Integer> storedYearList = new ArrayList<>();
        // 선택 연도 일별 시간 행을 잔디 변환용 목록에 보관한다
        List<ReadingStatisticsDto.Daily> storedDailyList = new ArrayList<>();

        // 통합 조회 행을 연도 목록과 일별 독서 시간으로 구분한다
        if (!StringUtil.isEmpty(heatmapRowList)) {
            // 각 행 유형에 맞는 화면 원천 목록을 구성한다
            for (ReadingHeatmapRowDto row : heatmapRowList) {
                // 연도 행은 조회 가능한 연도 목록에 추가한다
                if (ReadingHeatmapRowDto.ROW_TYPE_YEAR.equals(row.getRowType())) {
                    // 값이 있는 기록 연도만 선택 목록 원천에 추가한다
                    if (!StringUtil.isEmpty(row.getReadYear())) {
                        // 기록이 존재하는 연도를 추가한다
                        storedYearList.add(row.getReadYear());
                    }

                    continue;
                }

                // 일별 행은 날짜와 시간을 선택 연도 잔디 원천으로 추가한다
                if (ReadingHeatmapRowDto.ROW_TYPE_DAILY.equals(row.getRowType())
                        && !StringUtil.isEmpty(row.getReadDate())) {
                    // 선택 날짜의 독서 시간을 잔디 원천 목록에 추가한다
                    storedDailyList.add(new ReadingStatisticsDto.Daily(row.getReadDate(), row.getReadSecs()));
                }
            }
        }

        // 화면에 전달할 연도 목록은 현재 연도를 첫 항목으로 보장한다
        List<Integer> availableYears = new ArrayList<>();
        // 현재 연도는 빈 잔디도 조회할 수 있도록 항상 추가한다
        availableYears.add(currentYear);

        // 과거 기록이 존재하는 연도를 중복 없이 화면 목록에 추가한다
        if (!StringUtil.isEmpty(storedYearList)) {
            // 최근 연도부터 반환된 DB 순서를 그대로 유지한다
            for (Integer storedYear : storedYearList) {
                // null과 미래 연도 또는 현재 연도 중복은 연도 선택 목록에서 제외한다
                if (StringUtil.isEmpty(storedYear) || storedYear > currentYear || availableYears.contains(storedYear)) {

                    continue;
                }

                // 독서 기록이 존재하는 과거 연도를 조회 가능 목록에 추가한다
                availableYears.add(storedYear);
            }

        }

        // 현재 연도 또는 실제 기록이 존재하는 연도만 조회할 수 있다
        if (!availableYears.contains(selectedYear)) {
            // 임의 연도 요청은 호출부가 공통 오류로 처리하도록 빈 결과를 반환한다
            return null;
        }

        // 저장된 날짜만 빠르게 찾을 수 있도록 날짜별 독서 시간을 보관한다
        Map<LocalDate, Long> dailyTimeMap = new HashMap<>();

        // 저장된 날짜별 독서 시간을 잔디 검색값에 반영한다
        if (!StringUtil.isEmpty(storedDailyList)) {
            // 조회된 날짜별 독서 시간을 누락 없이 잔디 데이터로 변환한다
            for (ReadingStatisticsDto.Daily daily : storedDailyList) {
                // 날짜가 없는 비정상 집계 행은 그래프 계산에서 제외한다
                if (StringUtil.isEmpty(daily.getReadDate())) {

                    continue;
                }

                // 음수 시간이 화면 통계를 왜곡하지 않도록 최소값을 0초로 보정한다
                long readSecs = Math.max(0L, daily.getReadSecs());
                // 잔디 날짜에 대응하는 확정 독서 시간을 저장한다
                dailyTimeMap.put(daily.getReadDate(), readSecs);
            }

        }

        // 기록이 없는 날짜도 GitHub 형태 잔디의 빈 칸으로 전달할 목록을 생성한다
        List<ReadingStatisticsDto.Daily> heatmapList = getHeatmapList(periodStart, periodEnd, dailyTimeMap);
        // 잔디 전용 응답을 생성한다
        ReadingHeatmapDto heatmap = new ReadingHeatmapDto();
        // 잔디에 포함된 첫 날짜를 설정한다
        heatmap.setHeatmapStart(periodStart);
        // 잔디에 포함된 마지막 날짜를 설정한다
        heatmap.setHeatmapEnd(periodEnd);
        // 선택 연도의 날짜별 독서 시간 목록을 설정한다
        heatmap.setHeatmapList(heatmapList);
        // 잔디에 표시한 연도를 설정한다
        heatmap.setSelectedYear(selectedYear);
        // 화면에서 조회할 수 있는 연도 목록을 설정한다
        heatmap.setAvailableYears(availableYears);
        // 마이페이지와 타이머 화면이 함께 사용할 독서 잔디를 반환한다
        return heatmap;
    }

    /**
     * 기록이 없는 날짜를 0초로 채워 선택 기간의 잔디 목록을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param periodStart 잔디 시작일
     * @param periodEnd 잔디 종료일
     * @param dailyTimeMap 저장된 날짜별 독서 시간
     * @return 빈 날짜를 포함한 선택 기간 독서 시간 목록
     */
    private List<ReadingStatisticsDto.Daily> getHeatmapList(LocalDate periodStart, LocalDate periodEnd
                                                           , Map<LocalDate, Long> dailyTimeMap) {
        // 선택 날짜 범위를 목록의 초기 용량으로 계산한다
        int periodDays = Math.toIntExact(ChronoUnit.DAYS.between(periodStart, periodEnd) + 1L);
        // 날짜별 잔디 항목을 중간 확장 없이 보관할 목록을 생성한다
        List<ReadingStatisticsDto.Daily> heatmapList = new ArrayList<>(periodDays);

        // 시작일부터 오늘까지 모든 날짜를 잔디 한 칸씩 생성한다
        for (LocalDate readDate = periodStart; !readDate.isAfter(periodEnd); readDate = readDate.plusDays(1)) {
            // 저장 기록이 없는 날짜는 0초로 표시한다
            long readSecs = dailyTimeMap.getOrDefault(readDate, 0L);
            // 날짜와 확정 독서 시간을 잔디 목록에 추가한다
            heatmapList.add(new ReadingStatisticsDto.Daily(readDate, readSecs));
        }

        // 선택 기간이 날짜순으로 채워진 잔디 목록을 반환한다
        return heatmapList;
    }

    /**
     * 읽는 중, 완독, 중단 상태가 0건이어도 고정 순서로 상태 목록을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param storedStatusList DB에서 집계한 상태별 독후감 수
     * @return 세 상태를 모두 포함한 독후감 수 목록
     */
    private List<ReadingStatisticsDto.Status> getStatusList(List<ReadingStatisticsDto.Status> storedStatusList) {
        // 세 상태의 기본값과 표시 순서를 함께 보관할 맵을 생성한다
        Map<String, Long> statusCountMap = new LinkedHashMap<>();
        // 읽는 중 상태가 없을 때도 범례가 유지되도록 0건으로 초기화한다
        statusCountMap.put(Constant.REPORT_STAT_READ, 0L);
        // 완독 상태가 없을 때도 범례가 유지되도록 0건으로 초기화한다
        statusCountMap.put(Constant.REPORT_STAT_DONE, 0L);
        // 중단 상태가 없을 때도 범례가 유지되도록 0건으로 초기화한다
        statusCountMap.put(Constant.REPORT_STAT_STOP, 0L);

        // DB에서 조회된 허용 상태의 실제 건수를 기본값에 덮어쓴다
        if (!StringUtil.isEmpty(storedStatusList)) {
            // 세 상태 중 조회된 항목을 누락 없이 반영한다
            for (ReadingStatisticsDto.Status status : storedStatusList) {
                // 허용되지 않은 상태는 본인 통계 범례에 포함하지 않는다
                if (StringUtil.isEmpty(status.getReptStat()) || !statusCountMap.containsKey(status.getReptStat())) {

                    continue;
                }

                // 음수 집계가 표시되지 않도록 상태별 건수를 0 이상으로 보정한다
                statusCountMap.put(status.getReptStat(), Math.max(0L, status.getReptCnt()));
            }

        }

        // 고정된 세 상태를 응답 항목으로 변환할 목록을 생성한다
        List<ReadingStatisticsDto.Status> statusList = new ArrayList<>(statusCountMap.size());

        // 읽는 중, 완독, 중단 순서로 화면 범례 항목을 생성한다
        for (Map.Entry<String, Long> statusEntry : statusCountMap.entrySet()) {
            // 상태 코드와 집계 건수를 화면용 목록에 추가한다
            statusList.add(new ReadingStatisticsDto.Status(statusEntry.getKey(), statusEntry.getValue()));
        }

        // 세 상태를 고정 순서로 포함한 목록을 반환한다
        return statusList;
    }

    /**
     * 추가 독서 통계가 공통으로 사용하는 현재 및 이전 연도의 같은 기간 조건을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 통계를 조회할 회원 번호
     * @param today 서울 시간 기준 오늘
     * @return 책별 독서 시간과 연도 비교 SQL 조건
     */
    private ReadingStatisticsQueryDto getStatisticsQuery(Long userNumb, LocalDate today) {
        // 현재 연도와 전년도 비교 시작일을 계산한다
        LocalDate currentStart = LocalDate.of(today.getYear(), 1, 1);
        LocalDate previousStart = currentStart.minusYears(1L);
        // 오늘까지 포함하기 위해 현재 연도와 전년도 종료 제외일을 각각 계산한다
        LocalDate currentEnd = today.plusDays(1L);
        LocalDate previousEnd = today.minusYears(1L).plusDays(1L);
        // 여러 추가 통계 SQL이 같은 날짜 경계를 사용하도록 조회 조건 객체를 생성한다
        ReadingStatisticsQueryDto query = new ReadingStatisticsQueryDto();
        // 통계를 조회할 회원 번호를 설정한다
        query.setUserNumb(userNumb);
        // 현재 연도 표시값을 설정한다
        query.setCurrentYear(today.getYear());
        // 비교할 이전 연도 표시값을 설정한다
        query.setPreviousYear(today.getYear() - 1);
        // 현재 연도 집계 시작일을 설정한다
        query.setCurrentStart(currentStart);
        // 현재 연도 집계 종료 제외일을 설정한다
        query.setCurrentEnd(currentEnd);
        // 이전 연도 집계 시작일을 설정한다
        query.setPreviousStart(previousStart);
        // 이전 연도 집계 종료 제외일을 설정한다
        query.setPreviousEnd(previousEnd);
        // 현재 연도 완료 타이머 조회 시작 일시를 설정한다
        query.setTimerStart(currentStart.atStartOfDay());
        // 오늘까지 완료된 타이머를 포함할 종료 제외 일시를 설정한다
        query.setTimerEnd(currentEnd.atStartOfDay());
        // 완료 타이머만 책별 독서 시간에 포함하도록 상태를 설정한다
        query.setCompletedStat(Constant.TIMER_STAT_COMPLETED);
        // 완독한 독후감만 연도별 완료 권수에 포함하도록 상태를 설정한다
        query.setDoneStat(Constant.REPORT_STAT_DONE);
        // 연속 독서 기록이 오늘 또는 어제까지 이어졌는지 판정할 기준일을 설정한다
        query.setToday(today);

        // 현재 및 이전 연도의 같은 기간이 구성된 조회 조건을 반환한다
        return query;
    }

    /**
     * 현재 연도 책별 독서 시간을 상위 세 권의 안전한 화면 목록으로 보정한다
     *
     * @author SeungHyeon.Kang
     * @param storedTopBookList DB에서 집계한 책별 타이머 시간 목록
     * @param includeReportLink 본인 전용 독후감 상세 번호 포함 여부
     * @return 유효한 도서만 포함한 상위 세 권 목록
     */
    private List<ReadingStatisticsDto.BookTime> getTopBookList(List<ReadingStatisticsDto.BookTime> storedTopBookList
                                                               , boolean includeReportLink) {
        // 화면에서 순위를 부여할 최대 세 권의 목록을 생성한다
        List<ReadingStatisticsDto.BookTime> topBookList = new ArrayList<>(3);

        // 조회 결과가 있을 때만 유효한 도서를 화면 목록에 추가한다
        if (!StringUtil.isEmpty(storedTopBookList)) {
            // SQL 정렬 순서를 유지하며 최대 세 권까지 검증한다
            for (ReadingStatisticsDto.BookTime bookTime : storedTopBookList) {
                // 독후감 또는 도서를 식별할 수 없는 행은 안전한 상세 이동과 순위 계산에서 제외한다
                if (StringUtil.isEmpty(bookTime) || StringUtil.isEmpty(bookTime.getReptNumb())
                        || StringUtil.isEmpty(bookTime.getBookNumb())) {

                    continue;
                }

                // 음수 독서 시간이 화면에 표시되지 않도록 0초 이상으로 보정한다
                bookTime.setReadSecs(Math.max(0L, bookTime.getReadSecs()));

                // 공개 프로필 응답에는 본인 전용 독후감 상세 번호를 노출하지 않는다
                if (!includeReportLink) {
                    bookTime.setReptNumb(null);
                }

                // 검증한 책별 독서 시간을 현재 연도 순위 목록에 추가한다
                topBookList.add(bookTime);

                // 화면 정책의 상위 세 권이 채워지면 남은 조회 행을 사용하지 않는다
                if (topBookList.size() == 3) {

                    break;
                }
            }

        }

        // 현재 연도에 타이머 기록이 없으면 빈 목록을 포함한 상위 도서 목록을 반환한다
        return topBookList;
    }

    /**
     * 조회된 별점의 소수점을 버리고 0점부터 5점까지 빠짐없는 내림차순 목록으로 구성한다
     *
     * @author SeungHyeon.Kang
     * @param storedRatingList DB에서 집계한 별점별 독후감 수
     * @return 5점부터 0점까지의 1점 단위 별점 분포 목록
     */
    private List<ReadingStatisticsDto.Rating> getRatingList(List<ReadingStatisticsDto.Rating> storedRatingList) {
        // 소수점 별점을 버린 정수 점수와 화면 고정 목록을 비교할 건수 맵을 생성한다
        Map<BigDecimal, Long> ratingCountMap = new HashMap<>();

        // 유효한 원본 양수 별점에서 생성된 집계만 화면 고정 목록에 반영한다
        if (!StringUtil.isEmpty(storedRatingList)) {
            // DB에서 조회된 별점별 건수를 검증한다
            for (ReadingStatisticsDto.Rating rating : storedRatingList) {
                // 별점이 없는 비정상 집계 행은 분포 계산에서 제외한다
                if (StringUtil.isEmpty(rating) || StringUtil.isEmpty(rating.getReptGrde())) {

                    continue;
                }

                // 4.5점이 4점이 되도록 별점의 소수점을 버린 정수 점수를 생성한다
                BigDecimal grade = rating.getReptGrde().setScale(0, RoundingMode.FLOOR);
                // 실제 0.5점이 버림된 0점부터 최대 5점까지의 허용 범위만 포함한다
                if (grade.compareTo(BigDecimal.ZERO) < 0
                        || grade.compareTo(BigDecimal.valueOf(5D)) > 0) {

                    continue;
                }

                // 같은 정수 구간으로 버림된 여러 별점의 기존 건수와 현재 건수를 합산한다
                long ratingCount = ratingCountMap.getOrDefault(grade, 0L) + Math.max(0L, rating.getReptCnt());
                // 합산된 정수 별점별 독후감 수를 화면 고정 목록에 사용할 맵에 저장한다
                ratingCountMap.put(grade, ratingCount);
            }

        }

        // 5점부터 0점까지 여섯 개의 막대 항목을 보관할 목록을 생성한다
        List<ReadingStatisticsDto.Rating> ratingList = new ArrayList<>(6);

        // 1점 단위 별점을 5점부터 0점까지 내림차순으로 생성한다
        for (int wholePoint = 5; wholePoint >= 0; wholePoint--) {
            // 정수 점수를 DB 집계 결과와 같은 BigDecimal 키로 변환한다
            BigDecimal grade = BigDecimal.valueOf(wholePoint);
            // 기록이 없는 별점도 0권 막대로 표시한다
            ratingList.add(new ReadingStatisticsDto.Rating(grade, ratingCountMap.getOrDefault(grade, 0L)));
        }

        // 모든 허용 별점을 포함한 고정 순서 분포 목록을 반환한다
        return ratingList;
    }

    /**
     * 연도 비교 집계값의 연도와 음수 수치를 화면에서 사용할 수 있게 보정한다
     *
     * @author SeungHyeon.Kang
     * @param comparison DB에서 조회한 연도별 비교 수치
     * @param query 현재 및 이전 연도 표시와 기간 조건
     * @return 누락과 음수 값이 보정된 연도 비교 수치
     */
    private ReadingStatisticsDto.YearComparison getYearComparison(ReadingStatisticsDto.YearComparison comparison
                                                                  , ReadingStatisticsQueryDto query) {
        // 집계 결과가 없으면 모든 수치가 0인 비교 객체를 생성한다
        if (StringUtil.isEmpty(comparison)) {
            // 현재 및 이전 연도와 0값을 포함한 비교 결과를 반환한다
            return new ReadingStatisticsDto.YearComparison(query.getCurrentYear(), query.getPreviousYear()
                                                          , 0L, 0L, 0L, 0L, 0L, 0L);
        }

        // SQL 결과와 관계없이 서버가 계산한 현재 연도를 표시 기준으로 설정한다
        comparison.setCurrentYear(query.getCurrentYear());
        // SQL 결과와 관계없이 서버가 계산한 이전 연도를 표시 기준으로 설정한다
        comparison.setPreviousYear(query.getPreviousYear());
        // 현재 연도 독서 시간을 0초 이상으로 보정한다
        comparison.setCurrentReadSecs(Math.max(0L, comparison.getCurrentReadSecs()));
        // 이전 연도 독서 시간을 0초 이상으로 보정한다
        comparison.setPreviousReadSecs(Math.max(0L, comparison.getPreviousReadSecs()));
        // 현재 연도 독서일을 0일 이상으로 보정한다
        comparison.setCurrentReadDays(Math.max(0L, comparison.getCurrentReadDays()));
        // 이전 연도 독서일을 0일 이상으로 보정한다
        comparison.setPreviousReadDays(Math.max(0L, comparison.getPreviousReadDays()));
        // 현재 연도 완독 권수를 0권 이상으로 보정한다
        comparison.setCurrentDoneBooks(Math.max(0L, comparison.getCurrentDoneBooks()));
        // 이전 연도 완독 권수를 0권 이상으로 보정한다
        comparison.setPreviousDoneBooks(Math.max(0L, comparison.getPreviousDoneBooks()));

        // 화면에서 바로 비교할 수 있는 안전한 연도별 수치를 반환한다
        return comparison;
    }

    /**
     * 통계 공개 여부가 공통 Y 또는 N 코드인지 판정한다
     *
     * @author SeungHyeon.Kang
     * @param publicYsno 검증할 공개 여부
     * @return 공개 또는 비공개 코드이면 참
     */
    private boolean isAllowedPublic(String publicYsno) {
        // 공통 공개 여부 코드만 회원 설정에 저장할 수 있다
        return Constant.COMM_YES.equals(publicYsno) || Constant.COMM_NO.equals(publicYsno);
    }
}
