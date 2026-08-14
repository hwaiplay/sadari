package org.our.sadari.myPage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.myPage.dto.ReadingHeatmapDto;
import org.our.sadari.myPage.dto.ReadingStatisticsDto;
import org.our.sadari.myPage.dto.ReadingStatisticsQueryDto;
import org.our.sadari.myPage.dto.ReadingStatisticsSettingDto;
import org.our.sadari.myPage.mapper.ReadingStatisticsMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : ReadingStatisticsServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 습관 및 연도별 통계의 집계와 계정 및 공개 범위를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    연속 기록과 책별 시간 및 별점과 연도 비교 검증 추가
 * 2026-08-14        SeungHyeon.Kang    소수점 별점의 버림 집계 검증 추가
 * 2026-08-14        SeungHyeon.Kang    올해 상위 도서 세 권 제한과 독후감 번호 검증 추가
 * 2026-08-14        SeungHyeon.Kang    독서 잔디 전용 Mapper 호출 범위 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class ReadingStatisticsServiceImplTest {

    // 마이페이지 독서 통계 데이터 접근 객체 대역
    @Mock
    private ReadingStatisticsMapper readingStatisticsMapper;
    // 테스트 대상 마이페이지 독서 통계 서비스
    private ReadingStatisticsServiceImpl readingStatisticsService;

    /**
     * 서울 시간 2026년 8월 14일로 고정한 독서 통계 서비스를 준비한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // ResultData 메시지 조회에 사용할 테스트 메시지 소스를 초기화한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 공통 결과 코드의 실제 메시지 프로퍼티를 조회 대상으로 설정한다
        messageSource.setBasename("messages");
        // 테스트에서 한글 실패 문구가 손상되지 않도록 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // 공통 결과 객체가 테스트 메시지 소스를 사용하도록 설정한다
        new MessageUtils().setMessageSource(messageSource);

        // 연도별 날짜 경계를 항상 동일하게 검증하도록 서울 고정 시각을 생성한다
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        // 고정된 날짜 경계로 검증할 독서 통계 서비스를 생성한다
        readingStatisticsService = new ReadingStatisticsServiceImpl(readingStatisticsMapper, clock);
    }

    /**
     * 타이머용 잔디 조회가 연도와 일별 시간 Mapper만 사용하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingHeatmapOnlyUsesDailyData() {

        ReadingStatisticsDto.Daily daily = new ReadingStatisticsDto.Daily(LocalDate.of(2026, 8, 14), 2400L);
        // 정상 회원의 본인 잔디 조회를 허용할 계정 설정을 생성한다
        ReadingStatisticsSettingDto setting = getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE);
        // 잔디 조회 전에 정상 계정 상태를 반환하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(setting);
        // 현재 연도만 잔디에서 선택할 수 있도록 설정한다
        when(readingStatisticsMapper.getReadingYearList(1L)).thenReturn(List.of(2026));
        // 현재 날짜의 40분 독서 기록 한 건을 반환하도록 설정한다
        when(readingStatisticsMapper.getDailyTimeList(1L, LocalDate.of(2026, 1, 1)
                                                        , LocalDate.of(2026, 8, 14))).thenReturn(List.of(daily));

        // 전체 통계 대신 타이머 화면의 현재 연도 독서 잔디만 조회한다
        ResultData result = readingStatisticsService.getReadingHeatmap(1L, null);
        ReadingHeatmapDto heatmap = (ReadingHeatmapDto) result.getData();

        // 독서 잔디 전용 응답이 정상 반환됐는지 확인한다
        assertEquals(200, result.getCode());
        // 현재 연도 시작일부터 오늘까지 잔디가 채워졌는지 확인한다
        assertEquals(226, heatmap.getHeatmapList().size());
        // 마지막 날짜의 40분 독서 시간이 유지되는지 확인한다
        assertEquals(2400L, heatmap.getHeatmapList().get(225).getReadSecs());
        // 잔디 전용 조회에서 독서 상태 집계를 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getStatusCountList(any(), any(), any(), any());
        // 잔디 전용 조회에서 연속 독서 날짜를 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getReadingDateList(any());
        // 잔디 전용 조회에서 책별 독서 시간 순위를 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getTopBookTimeList(any(ReadingStatisticsQueryDto.class));
        // 잔디 전용 조회에서 별점 분포를 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getRatingCountList(any());
        // 잔디 전용 조회에서 연도 비교 통계를 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getYearComparison(any(ReadingStatisticsQueryDto.class));
    }

    /**
     * 현재 연도 잔디가 1월 1일부터 오늘까지 빈 날짜를 포함해 채워지는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingStatsFillsCurrentYear() {

        ReadingStatisticsDto.Daily daily = new ReadingStatisticsDto.Daily(LocalDate.of(2026, 8, 14), 1800L);
        ReadingStatisticsDto.Status doneStatus = new ReadingStatisticsDto.Status(Constant.REPORT_STAT_DONE, 3L);
        // 범용 회원 설정 행이 없는 기본값과 같은 정상 회원 설정을 생성한다
        ReadingStatisticsSettingDto setting = getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE);
        // 회원 설정과 계정 상태를 정상 조회하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(setting);
        // 현재 연도와 기록이 있는 과거 연도를 조회 가능 목록으로 설정한다
        when(readingStatisticsMapper.getReadingYearList(1L)).thenReturn(List.of(2026, 2025));
        // 현재 연도 중 오늘 하루의 독서 시간만 조회되도록 설정한다
        when(readingStatisticsMapper.getDailyTimeList(1L, LocalDate.of(2026, 1, 1)
                                                        , LocalDate.of(2026, 8, 14))).thenReturn(List.of(daily));
        // 완독 상태만 존재하고 읽는 중과 중단 상태는 없는 조회 결과를 설정한다
        when(readingStatisticsMapper.getStatusCountList(1L, Constant.REPORT_STAT_READ
                                                        , Constant.REPORT_STAT_DONE
                                                        , Constant.REPORT_STAT_STOP)).thenReturn(List.of(doneStatus));
        // 최근 3일이 이어진 타이머 기록으로 현재 및 최장 연속 기록을 설정한다
        when(readingStatisticsMapper.getReadingDateList(1L)).thenReturn(List.of(LocalDate.of(2026, 8, 12)
                                                                             , LocalDate.of(2026, 8, 13)
                                                                             , LocalDate.of(2026, 8, 14)));
        // 올해 책별 독서 시간 순위가 네 권 조회되어도 화면 응답은 세 권으로 제한되도록 설정한다
        when(readingStatisticsMapper.getTopBookTimeList(any(ReadingStatisticsQueryDto.class)))
                .thenReturn(List.of(new ReadingStatisticsDto.BookTime(31L, 15L, "사다리 독서법", "홍길동", null, 7200L)
                                  , new ReadingStatisticsDto.BookTime(32L, 16L, "두 번째 책", "김사다리", null, 5400L)
                                  , new ReadingStatisticsDto.BookTime(33L, 17L, "세 번째 책", "이독서", null, 3600L)
                                  , new ReadingStatisticsDto.BookTime(34L, 18L, "네 번째 책", "박통계", null, 1800L)));
        // 정수와 반점 별점이 함께 존재하는 별점 분포 원천을 설정한다
        when(readingStatisticsMapper.getRatingCountList(1L))
                .thenReturn(List.of(new ReadingStatisticsDto.Rating(BigDecimal.valueOf(5D), 1L)
                                  , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(4.5D), 2L)
                                  , new ReadingStatisticsDto.Rating(BigDecimal.valueOf(0.5D), 3L)));
        // 현재 연도와 이전 연도 같은 기간의 독서 기록 비교값을 설정한다
        when(readingStatisticsMapper.getYearComparison(any(ReadingStatisticsQueryDto.class)))
                .thenReturn(new ReadingStatisticsDto.YearComparison(2026, 2025, 7200L, 3600L, 3L, 2L, 2L, 1L));

        // 고정 날짜를 기준으로 현재 연도 본인 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getReadingStats(1L, null);
        ReadingStatisticsDto statistics = (ReadingStatisticsDto) result.getData();

        // 정상 통계 응답이 반환됐는지 확인한다
        assertEquals(200, result.getCode());
        // 현재 연도 잔디가 1월 1일부터 오늘까지 226칸인지 확인한다
        assertEquals(226, statistics.getHeatmapList().size());
        // 마지막 날짜의 30분 독서 시간이 유지되는지 확인한다
        assertEquals(1800L, statistics.getHeatmapList().get(225).getReadSecs());
        // 현재 연도와 기록이 있는 과거 연도가 최근 순으로 제공되는지 확인한다
        assertEquals(List.of(2026, 2025), statistics.getAvailableYears());
        // 현재 연도가 선택값으로 반환되는지 확인한다
        assertEquals(2026, statistics.getSelectedYear());
        // 읽는 중 상태가 0건으로 채워졌는지 확인한다
        assertEquals(0L, statistics.getStatusList().get(0).getReptCnt());
        // 완독 상태의 실제 3건이 유지되는지 확인한다
        assertEquals(3L, statistics.getStatusList().get(1).getReptCnt());
        // 오늘까지 이어진 현재 연속 기록이 3일인지 확인한다
        assertEquals(3, statistics.getStreak().getCurrentStreakDays());
        // 전체 기록 중 최장 연속 기록이 3일인지 확인한다
        assertEquals(3, statistics.getStreak().getLongestStreakDays());
        // 올해 가장 오래 읽은 책의 2시간 기록이 유지되는지 확인한다
        assertEquals(7200L, statistics.getTopBookList().get(0).getReadSecs());
        // 올해 가장 오래 읽은 책이 서버 응답에서도 세 권까지만 제공되는지 확인한다
        assertEquals(3, statistics.getTopBookList().size());
        // 첫 번째 책에 독후감 상세 이동 번호가 함께 제공되는지 확인한다
        assertEquals(31L, statistics.getTopBookList().get(0).getReptNumb());
        // 5점 분포가 실제 한 권으로 유지되는지 확인한다
        assertEquals(1L, statistics.getRatingList().get(0).getReptCnt());
        // 4.5점이 소수점 버림된 4점 분포에 두 권으로 포함되는지 확인한다
        assertEquals(2L, statistics.getRatingList().get(1).getReptCnt());
        // 실제 0.5점이 소수점 버림된 0점 분포에 세 권으로 포함되는지 확인한다
        assertEquals(3L, statistics.getRatingList().get(5).getReptCnt());
        // 현재 연도 독서 시간이 전년도 비교값과 함께 유지되는지 확인한다
        assertEquals(7200L, statistics.getYearComparison().getCurrentReadSecs());
    }

    /**
     * 기록이 있는 과거 연도는 12월 31일까지 전체 잔디를 조회하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingStatsFillsPastYear() {

        // 공개 설정과 계정 상태를 정상 조회하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO))
                .thenReturn(getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE));
        // 2025년에 독서 기록이 존재하도록 조회 가능 연도를 설정한다
        when(readingStatisticsMapper.getReadingYearList(1L)).thenReturn(List.of(2025));
        // 과거 연도 전체 기간의 독서 시간이 없도록 설정한다
        when(readingStatisticsMapper.getDailyTimeList(1L, LocalDate.of(2025, 1, 1)
                                                        , LocalDate.of(2025, 12, 31))).thenReturn(List.of());
        // 독서 상태 집계가 없는 빈 서재로 설정한다
        when(readingStatisticsMapper.getStatusCountList(1L, Constant.REPORT_STAT_READ
                                                        , Constant.REPORT_STAT_DONE
                                                        , Constant.REPORT_STAT_STOP)).thenReturn(List.of());

        // 기록이 있는 과거 연도의 본인 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getReadingStats(1L, 2025);
        ReadingStatisticsDto statistics = (ReadingStatisticsDto) result.getData();

        // 과거 연도 조회가 성공했는지 확인한다
        assertEquals(200, result.getCode());
        // 평년인 2025년의 날짜가 365칸으로 모두 채워지는지 확인한다
        assertEquals(365, statistics.getHeatmapList().size());
        // 과거 연도의 마지막 날짜가 12월 31일인지 확인한다
        assertEquals(LocalDate.of(2025, 12, 31), statistics.getHeatmapEnd());
    }

    /**
     * 기록이 없는 임의 과거 연도는 원천 통계를 조회하지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingStatsRejectsUnavailableYear() {

        // 공개 설정과 계정 상태를 정상 조회하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO))
                .thenReturn(getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE));
        // 2025년만 독서 기록이 존재하도록 조회 가능 연도를 설정한다
        when(readingStatisticsMapper.getReadingYearList(1L)).thenReturn(List.of(2025));

        // 기록이 없는 2024년의 본인 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getReadingStats(1L, 2024);

        // 잘못된 연도 요청 실패 응답인지 확인한다
        assertEquals(2009, result.getCode());
        // 조회할 수 없는 연도에는 일별 독서 시간 SQL을 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getDailyTimeList(any(), any(), any());
        // 조회할 수 없는 연도에는 독후감 상태 SQL을 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getStatusCountList(any(), any(), any(), any());
    }

    /**
     * 인증 사용자 번호가 없으면 통계 원천을 조회하지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingStatsRejectsEmptyUser() {

        // 인증 사용자 번호 없이 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getReadingStats(null, null);

        // 잘못된 요청 실패 응답인지 확인한다
        assertEquals(2009, result.getCode());
        // 사용자 번호가 없을 때 연도 목록 SQL을 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getReadingYearList(any());
        // 사용자 번호가 없을 때 일별 독서 시간 SQL을 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getDailyTimeList(any(), any(), any());
    }

    /**
     * 비공개 설정 회원은 다른 사용자에게 통계 원천을 조회하지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getPublicStatsHidesPrivate() {

        // 다른 사용자에게 공개하지 않는 정상 회원 설정을 생성한다
        ReadingStatisticsSettingDto setting = getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE);
        // 공개 통계 조회에서 비공개 회원 설정을 반환하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(setting);

        // 비공개 회원의 다른 사용자용 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getPublicReadingStats(1L, null);

        // 회원 존재 여부를 노출하지 않는 빈 성공 응답인지 확인한다
        assertEquals(200, result.getCode());
        // 비공개 설정에서는 공개할 데이터가 없는지 확인한다
        assertEquals(null, result.getData());
        // 비공개 설정에서는 연도 목록 SQL을 호출하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getReadingYearList(any());
    }

    /**
     * 공개 프로필 통계에는 본인 전용 독후감 상세 번호가 노출되지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getPublicStatsHidesReportLink() {

        // 다른 사용자에게 독서 통계를 공개하는 정상 회원 설정을 생성한다
        ReadingStatisticsSettingDto setting = getSetting(Constant.COMM_YES, Constant.USER_STAT_ACTIVE);
        // 공개 통계 조회에서 공개 회원 설정을 반환하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(setting);
        // 현재 연도만 조회할 수 있도록 저장된 과거 연도는 없는 것으로 설정한다
        when(readingStatisticsMapper.getReadingYearList(1L)).thenReturn(List.of());
        // 공개 응답의 잔디를 빈 기록으로 구성하도록 일별 독서 시간은 없는 것으로 설정한다
        when(readingStatisticsMapper.getDailyTimeList(1L, LocalDate.of(2026, 1, 1)
                                                        , LocalDate.of(2026, 8, 14))).thenReturn(List.of());
        // 공개 응답의 독서 상태를 모두 0건으로 구성하도록 집계 결과는 없는 것으로 설정한다
        when(readingStatisticsMapper.getStatusCountList(1L, Constant.REPORT_STAT_READ
                                                        , Constant.REPORT_STAT_DONE
                                                        , Constant.REPORT_STAT_STOP)).thenReturn(List.of());
        // 공개 응답의 연속 독서일을 0일로 구성하도록 독서 날짜는 없는 것으로 설정한다
        when(readingStatisticsMapper.getReadingDateList(1L)).thenReturn(List.of());
        // 공개 화면에 표시할 올해 가장 오래 읽은 책 한 권과 내부 독후감 번호를 설정한다
        when(readingStatisticsMapper.getTopBookTimeList(any(ReadingStatisticsQueryDto.class)))
                .thenReturn(List.of(new ReadingStatisticsDto.BookTime(31L, 15L, "사다리 독서법", "홍길동", null, 7200L)));
        // 공개 응답의 별점 분포를 모두 0건으로 구성하도록 집계 결과는 없는 것으로 설정한다
        when(readingStatisticsMapper.getRatingCountList(1L)).thenReturn(List.of());

        // 공개 회원의 다른 사용자용 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getPublicReadingStats(1L, null);
        ReadingStatisticsDto statistics = (ReadingStatisticsDto) result.getData();

        // 공개 통계가 정상 응답으로 반환됐는지 확인한다
        assertEquals(200, result.getCode());
        // 올해 가장 오래 읽은 책 정보는 공개 응답에 유지되는지 확인한다
        assertEquals(1, statistics.getTopBookList().size());
        // 본인 전용 독후감 상세 번호는 공개 응답에서 제거됐는지 확인한다
        assertEquals(null, statistics.getTopBookList().get(0).getReptNumb());
    }

    /**
     * 비활성화 회원은 본인 화면에서도 네 가지 추가 통계 원천을 조회하지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReadingStatsHidesWithdrawnAccount() {

        // 비활성화 상태이면서 공개 범위가 비공개인 회원 설정을 생성한다
        ReadingStatisticsSettingDto setting = getSetting(Constant.COMM_NO, Constant.USER_STAT_WITHDRAWN);
        // 본인 통계 조회에서 비활성화 회원 설정을 반환하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(setting);

        // 비활성화 회원의 본인 독서 통계를 조회한다
        ResultData result = readingStatisticsService.getReadingStats(1L, null);

        // 제한 계정의 본인 통계 접근 거절 응답인지 확인한다
        assertEquals(2020, result.getCode());
        // 제한 계정에서는 연속 기록 원천을 조회하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getReadingDateList(any());
        // 제한 계정에서는 올해 책별 독서 시간 원천을 조회하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getTopBookTimeList(any());
        // 제한 계정에서는 별점 분포 원천을 조회하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getRatingCountList(any());
        // 제한 계정에서는 연도 비교 원천을 조회하지 않았는지 확인한다
        verify(readingStatisticsMapper, never()).getYearComparison(any());
    }

    /**
     * 정상 회원의 공개 설정 저장이 범용 회원 설정 행 생성 또는 수정으로 연결되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptReadingStatsUpsertsSetting() {

        // 설정 저장 권한 확인에 사용할 정상 회원 설정을 생성한다
        ReadingStatisticsSettingDto currentSetting = getSetting(Constant.COMM_NO, Constant.USER_STAT_ACTIVE);
        // 저장 전 계정 상태와 현재 설정을 정상 조회하도록 설정한다
        when(readingStatisticsMapper.getReadingStatsSetting(1L, Constant.COMM_NO)).thenReturn(currentSetting);
        // 공개 설정 요청 객체를 생성한다
        ReadingStatisticsSettingDto request = getSetting(Constant.COMM_YES, null);

        // 로그인 회원의 독서 통계 공개 설정을 저장한다
        ResultData result = readingStatisticsService.uptReadingStatsSetting(1L, request);

        // 범용 회원 설정 저장 성공 응답인지 확인한다
        assertEquals(200, result.getCode());
        // 저장된 공개 여부가 응답에 반영됐는지 확인한다
        assertEquals(Constant.COMM_YES, result.getData());
        // 인증 회원 번호가 설정된 요청으로 범용 회원 설정을 저장했는지 확인한다
        verify(readingStatisticsMapper).uptReadingStatsSetting(request);
        // 공개 여부 저장은 독서 시간 원천을 다시 조회하지 않는지 확인한다
        verify(readingStatisticsMapper, never()).getDailyTimeList(any(), any(), any());
    }

    /**
     * 독서 통계 설정 테스트에 사용할 회원 설정 객체를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param publicYsno 공개 여부 코드
     * @param userStat 회원 상태 코드
     * @return 테스트용 회원 설정
     */
    private ReadingStatisticsSettingDto getSetting(String publicYsno, String userStat) {
        // 공개 범위를 담을 테스트 설정 객체를 생성한다
        ReadingStatisticsSettingDto setting = new ReadingStatisticsSettingDto();
        // 다른 사용자 공개 여부를 설정한다
        setting.setPublicYsno(publicYsno);
        // 계정 접근 제한 판정에 사용할 회원 상태를 설정한다
        setting.setUserStat(userStat);
        // 구성된 테스트용 회원 설정을 반환한다
        return setting;
    }
}
