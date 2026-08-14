package org.our.sadari.timer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.timer.config.ReadingTimerProperties;
import org.our.sadari.timer.dto.ReadingTimerDto;
import org.our.sadari.timer.mapper.ReadingTimerMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : ReadingTimerServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 중복 시작과 자정 경계 시간 집계를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    오늘 완료 타이머 조회 범위 검증
 */
@ExtendWith(MockitoExtension.class)
class ReadingTimerServiceImplTest {

    // 독서 타이머 데이터 접근 객체 대역
    @Mock
    private ReadingTimerMapper readingTimerMapper;
    // 테스트 대상 독서 타이머 서비스
    private ReadingTimerServiceImpl readingTimerService;

    /**
     * 서울 시간 2026년 8월 15일 00시 01분으로 고정한 서비스를 준비한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        ReadingTimerProperties properties = new ReadingTimerProperties();
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T15:01:00Z"), ZoneId.of("Asia/Seoul"));
        // 고정 시계를 사용하는 테스트 서비스를 생성한다
        readingTimerService = new ReadingTimerServiceImpl(readingTimerMapper, properties, clock);
        // 요약 조회에 필요한 기본 목록을 빈 목록으로 반환한다
        when(readingTimerMapper.getDailyList(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        // 연결 가능한 도서 목록을 빈 목록으로 반환한다
        when(readingTimerMapper.getReadingBookList(anyLong(), eq(Constant.REPORT_STAT_READ))).thenReturn(List.of());
        // 오늘 완료 타이머 목록을 빈 목록으로 반환한다
        when(readingTimerMapper.getTodayCompletedTimerList(anyLong(), eq(Constant.TIMER_STAT_COMPLETED)
                                                         , any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
    }

    /**
     * 타이머 요약이 서울 기준 오늘 완료한 타이머만 조회하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getTimerSummaryQueriesTodayCompletedTimers() {

        // 고정 시계가 속한 8월 15일의 타이머 요약을 조회한다
        readingTimerService.getTimerSummary(1L);

        // 오늘 00시 이상 내일 00시 미만의 완료 타이머를 조회했는지 확인한다
        verify(readingTimerMapper).getTodayCompletedTimerList(1L, Constant.TIMER_STAT_COMPLETED
                                                            , LocalDateTime.of(2026, 8, 15, 0, 0)
                                                            , LocalDateTime.of(2026, 8, 16, 0, 0));
    }

    /**
     * 완료되지 않은 세션이 있으면 시작 요청을 멱등 처리하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setTimerKeepsExistingActiveSession() {

        ReadingTimerDto activeTimer = createTimer(Constant.TIMER_STAT_PAUSED, LocalDateTime.of(2026, 8, 14, 23, 50));
        // 시작 전과 요약 조회에서 동일한 기존 세션을 반환한다
        when(readingTimerMapper.getActiveTimerDtl(1L, Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_PAUSED)).thenReturn(activeTimer);

        // 동일 사용자의 중복 시작 요청을 실행한다
        ResultData result = readingTimerService.setTimer(1L, new ReadingTimerDto.Request());

        // 중복 요청도 현재 세션을 담은 성공 응답을 반환하는지 확인한다
        assertEquals(200, result.getCode());
        // 새 세션이 등록되지 않았는지 확인한다
        verify(readingTimerMapper, never()).setTimer(any(ReadingTimerDto.class));
    }

    /**
     * 자정을 넘긴 실행 구간이 양쪽 날짜에 나뉘어 집계되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptTimerSplitsRunningSegmentAtMidnight() {

        ReadingTimerDto activeTimer = createTimer(Constant.TIMER_STAT_RUNNING, LocalDateTime.of(2026, 8, 14, 23, 59));
        ReadingTimerDto.Request request = new ReadingTimerDto.Request();
        // 실행 중 세션을 일시정지하도록 요청 상태를 설정한다
        request.setTmrxStat(Constant.TIMER_STAT_PAUSED);
        // 사용자 소유 세션 조회 결과를 설정한다
        when(readingTimerMapper.getTimerDtl(1L, 10L)).thenReturn(activeTimer);
        // 상태 변경 후 요약에는 일시정지 세션을 반환한다
        when(readingTimerMapper.getActiveTimerDtl(1L, Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_PAUSED)).thenReturn(activeTimer);

        // 자정을 1분 지난 시점에 세션을 일시정지한다
        ResultData result = readingTimerService.uptTimer(1L, 10L, request);

        // 상태 변경이 성공했는지 확인한다
        assertEquals(200, result.getCode());
        // 자정 전 60초가 8월 14일 집계에 저장됐는지 확인한다
        verify(readingTimerMapper).setReadingDaily(eq(1L), eq(LocalDate.of(2026, 8, 14)), eq(60L), any(LocalDateTime.class));
        // 자정 후 60초가 8월 15일 집계에 저장됐는지 확인한다
        verify(readingTimerMapper).setReadingDaily(eq(1L), eq(LocalDate.of(2026, 8, 15)), eq(60L), any(LocalDateTime.class));
        // 세션 전체 확정 시간이 120초인지 확인한다
        assertEquals(120L, activeTimer.getReadSecs());
    }

    /**
     * 테스트용 독서 타이머 세션을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param timerStatus 타이머 상태
     * @param lastStart 최근 측정 시작 일시
     * @return 테스트용 독서 타이머 세션
     */
    private ReadingTimerDto createTimer(String timerStatus, LocalDateTime lastStart) {

        ReadingTimerDto timerDto = new ReadingTimerDto();
        // 테스트 세션 번호를 설정한다
        timerDto.setTmrxNumb(10L);
        // 테스트 사용자 번호를 설정한다
        timerDto.setUserNumb(1L);
        // 테스트 타이머 상태를 설정한다
        timerDto.setTmrxStat(timerStatus);
        // 테스트 최근 측정 시작 일시를 설정한다
        timerDto.setLastStrt(lastStart);
        // 테스트 확정 독서 시간을 0초로 설정한다
        timerDto.setReadSecs(0L);
        // 구성한 테스트 세션을 반환한다
        return timerDto;
    }
}
