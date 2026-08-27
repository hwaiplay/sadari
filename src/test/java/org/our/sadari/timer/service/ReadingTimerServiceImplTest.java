package org.our.sadari.timer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.timer.config.ReadingTimerProperties;
import org.our.sadari.timer.dto.ReadingTimerDto;
import org.our.sadari.timer.mapper.ReadingTimerMapper;
import org.our.sadari.global.common.util.MessageUtils;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 완료 타이머 검증
 * 2026-08-20        SeungHyeon.Kang    목표시간 알림·도서별 누적 페이지 검증
 * 2026-08-21        SeungHyeon.Kang    목표시간 자동 완료와 알림 재시도 검증
 */
@ExtendWith(MockitoExtension.class)
class ReadingTimerServiceImplTest {

    // 독서 타이머 데이터 접근 객체 대역
    @Mock
    private ReadingTimerMapper readingTimerMapper;
    // 알림 저장과 푸시 발송 업무 서비스 대역
    @Mock
    private AlimService alimService;
    // 스케줄러 로그 안전 처리 객체 대역
    @Mock
    private SchedulerLogSupport schedulerLogSupport;
    // 대상 세션별 트랜잭션 관리자 대역
    @Mock
    private PlatformTransactionManager transactionManager;
    // 대상 세션별 트랜잭션 상태 대역
    @Mock
    private TransactionStatus transactionStatus;
    // 테스트 대상 독서 타이머 서비스
    private ReadingTimerServiceImpl readingTimerService;

    /**
     * 서울 시간 2026년 8월 15일 00시 01분으로 고정한 서비스를 준비한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 테스트 실행 환경과 무관하게 한국어 검증 메시지를 사용한다
        LocaleContextHolder.setLocale(Locale.KOREAN);
        ReadingTimerProperties properties = new ReadingTimerProperties();
        StaticMessageSource messageSource = new StaticMessageSource();
        // 목표시간 검증 실패 응답에 사용할 테스트 메시지를 등록한다
        messageSource.addMessage("timer.alert.0004", Locale.KOREAN, "목표시간 오류");
        // 테스트 실행 환경의 기본 영어 로케일에도 같은 검증 메시지를 등록한다
        messageSource.addMessage("timer.alert.0004", Locale.ENGLISH, "Invalid target time");
        // 알림 저장 실패 결과에 사용할 테스트 메시지를 등록한다
        messageSource.addMessage("common.alert.0004", Locale.KOREAN, "조회 결과 없음");
        // 기본 영어 로케일에도 알림 저장 실패 메시지를 등록한다
        messageSource.addMessage("common.alert.0004", Locale.ENGLISH, "No data");
        // 공통 메시지 유틸리티에 테스트 메시지 소스를 설정한다
        new MessageUtils().setMessageSource(messageSource);
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T15:01:00Z"), ZoneId.of("Asia/Seoul"));
        // 고정 시계를 사용하는 테스트 서비스를 생성한다
        readingTimerService = new ReadingTimerServiceImpl(readingTimerMapper, properties, alimService
                                                        , schedulerLogSupport, transactionManager, clock, 100);
        // 건별 알림 트랜잭션 실행에 사용할 상태 객체를 반환한다
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        // 정상 대상 처리의 최종 스케줄러 상태를 성공으로 반환한다
        lenient().when(schedulerLogSupport.getSchedulerExecStatus(anyInt(), anyInt())).thenReturn(Constant.SCHEDULER_EXEC_SUCCESS);
        // 요약 조회에 필요한 기본 목록을 빈 목록으로 반환한다
        lenient().when(readingTimerMapper.getDailyList(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        // 연결 가능한 도서 목록을 빈 목록으로 반환한다
        lenient().when(readingTimerMapper.getReadingBookList(anyLong(), eq(Constant.REPORT_STAT_READ))).thenReturn(List.of());
        // 오늘 완료 타이머 목록을 빈 목록으로 반환한다
        lenient().when(readingTimerMapper.getTodayCompletedTimerList(anyLong(), eq(Constant.TIMER_STAT_COMPLETED)
                                                                   , any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
    }

    /**
     * 테스트에서 지정한 로케일 컨텍스트를 정리한다
     *
     * @author SeungHyeon.Kang
     */
    @AfterEach
    void tearDown() {

        // 다른 테스트에 로케일 설정이 전달되지 않도록 초기화한다
        LocaleContextHolder.resetLocaleContext();
    }

    /**
     * 타이머 요약이 서울 기준 오늘 완료한 타이머만 조회하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getTimerSummaryToday() {

        // 고정 시계가 속한 8월 15일의 타이머 요약을 조회한다
        readingTimerService.getTimerSummary(1L);

        // 오늘 00시 이상 내일 00시 미만의 완료 타이머를 조회했는지 확인한다
        verify(readingTimerMapper).getTodayCompletedTimerList(1L, Constant.TIMER_STAT_COMPLETED
                                                            , LocalDateTime.of(2026, 8, 15, 0, 0)
                                                            , LocalDateTime.of(2026, 8, 16, 0, 0));
    }

    /**
     * 도서별 누적시간 두 번째 페이지가 20건과 다음 페이지 여부만 반환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBookTimePageLimit() {

        List<ReadingTimerDto.BookTime> searchedList = new ArrayList<>();
        // 다음 페이지 판정용 한 건을 포함한 21권의 조회 결과를 구성한다
        for (long bookNumb = 1L; bookNumb <= 21L; bookNumb++) {
            ReadingTimerDto.BookTime bookTime = new ReadingTimerDto.BookTime();
            // 페이지 항목을 구분할 도서 번호를 설정한다
            bookTime.setBookNumb(bookNumb);
            // Mapper 조회 결과에 도서별 누적시간 항목을 추가한다
            searchedList.add(bookTime);
        }
        // 두 번째 페이지의 시작 위치와 다음 페이지 판정 제한 건수에 맞춘 결과를 반환한다
        when(readingTimerMapper.getBookTimeList(1L, Constant.TIMER_STAT_COMPLETED, 20, 21))
                .thenReturn(searchedList);

        // 도서별 누적시간 두 번째 페이지를 조회한다
        ResultData resultData = readingTimerService.getBookTimePage(1L, 2);

        // 최근 기록순 집계 쿼리에 두 번째 페이지 조건을 전달했는지 확인한다
        verify(readingTimerMapper).getBookTimeList(1L, Constant.TIMER_STAT_COMPLETED, 20, 21);
        PageDto<?> pageData = (PageDto<?>) resultData.getData();
        // 화면 표시 목록은 요청한 페이지 크기인 20권으로 제한됐는지 확인한다
        assertEquals(20, pageData.list().size());
        // 요청한 두 번째 페이지 번호가 응답에 유지됐는지 확인한다
        assertEquals(2, pageData.page());
        // 21번째 조회 결과로 다음 페이지 존재 여부를 판정했는지 확인한다
        assertEquals(true, pageData.hasNext());
    }

    /**
     * 완료되지 않은 세션이 있으면 시작 요청을 멱등 처리하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void keepExistingTimerSession() {

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
     * 목표시간을 지정한 새 세션에 알림 예정 일시가 계산되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void scheduleTimerAlarm() {

        ReadingTimerDto.Request request = new ReadingTimerDto.Request();
        // 30분 목표 독서시간을 설정한다
        request.setTargSecs(1800L);
        // 목표시간과 함께 새 독서 타이머를 시작한다
        ResultData result = readingTimerService.setTimer(1L, request);
        // 타이머 시작이 성공했는지 확인한다
        assertEquals(200, result.getCode());

        ArgumentCaptor<ReadingTimerDto> timerCaptor = ArgumentCaptor.forClass(ReadingTimerDto.class);
        // 새 세션 등록값을 캡처한다
        verify(readingTimerMapper).setTimer(timerCaptor.capture());
        // 목표시간이 초 단위로 저장됐는지 확인한다
        assertEquals(1800L, timerCaptor.getValue().getTargSecs());
        // 고정 시작 시각에서 30분 뒤로 알림이 예약됐는지 확인한다
        assertEquals(LocalDateTime.of(2026, 8, 15, 0, 31), timerCaptor.getValue().getAlrmDate());
        // 새 세션에는 발송 일시가 없는지 확인한다
        assertNull(timerCaptor.getValue().getSendDate());
    }

    /**
     * 최대 세션 시간을 넘는 알림 목표시간을 거부하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void rejectTimerOverEightHours() {

        ReadingTimerDto.Request request = new ReadingTimerDto.Request();
        // 8시간을 1초 넘는 목표시간을 설정한다
        request.setTargSecs(28801L);
        // 허용 범위를 벗어난 목표시간으로 타이머 시작을 요청한다
        ResultData result = readingTimerService.setTimer(1L, request);
        // 목표시간 검증 오류 코드가 반환됐는지 확인한다
        assertEquals(2025, result.getCode());
        // 유효하지 않은 세션이 등록되지 않았는지 확인한다
        verify(readingTimerMapper, never()).setTimer(any(ReadingTimerDto.class));
    }

    /**
     * 자정을 넘긴 실행 구간이 양쪽 날짜에 나뉘어 집계되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void splitTimerAtMidnight() {

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
        // 일시정지 시 예약 알림이 해제됐는지 확인한다
        assertNull(activeTimer.getAlrmDate());
    }

    /**
     * 목표시간에 도달한 화면 상태 변경 요청이 목표시각 기준 자동 완료로 전환되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void completeTimerTarget() {

        LocalDateTime targetEndDate = LocalDateTime.of(2026, 8, 15, 0, 1);
        ReadingTimerDto activeTimer = createTimer(Constant.TIMER_STAT_RUNNING, LocalDateTime.of(2026, 8, 15, 0, 0));
        // 1분 목표시간을 설정한다
        activeTimer.setTargSecs(60L);
        // 목표 종료시각을 예약 일시로 설정한다
        activeTimer.setAlrmDate(targetEndDate);
        ReadingTimerDto.Request request = new ReadingTimerDto.Request();
        // 목표시간과 동시에 들어온 일시정지 요청을 설정한다
        request.setTmrxStat(Constant.TIMER_STAT_PAUSED);
        // 사용자 소유 실행 세션을 조회 결과로 설정한다
        when(readingTimerMapper.getTimerDtl(1L, 10L)).thenReturn(activeTimer);

        // 목표시간 도달 시점의 상태 변경 요청을 처리한다
        ResultData result = readingTimerService.uptTimer(1L, 10L, request);

        // 자동 완료 상태 변경이 성공했는지 확인한다
        assertEquals(200, result.getCode());
        // 요청한 일시정지보다 자동 완료 상태가 우선 적용됐는지 확인한다
        assertEquals(Constant.TIMER_STAT_COMPLETED, activeTimer.getTmrxStat());
        // 목표시간인 60초만 확정됐는지 확인한다
        assertEquals(60L, activeTimer.getReadSecs());
        // 목표 종료시각이 완료 일시로 저장됐는지 확인한다
        assertEquals(targetEndDate, activeTimer.getEndxDate());
        // 알림 재시도 전까지 목표 예약 일시가 유지되는지 확인한다
        assertEquals(targetEndDate, activeTimer.getAlrmDate());
        // 목표시간 자동 완료 상태를 저장했는지 검증한다
        verify(readingTimerMapper).uptTimer(activeTimer);
    }

    /**
     * 활성 세션이 없어도 계정 상태 변경 시 대기 중인 목표시간 알림을 취소하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void cancelAlimOnWithdrawal() {

        LocalDateTime withdrawalDate = LocalDateTime.of(2026, 8, 15, 0, 1);
        // 활성 타이머가 없는 계정 상태를 설정한다
        when(readingTimerMapper.getActiveTimerDtl(1L, Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_PAUSED)).thenReturn(null);

        // 계정 비활성화 또는 영구 탈퇴 전 타이머 정리를 실행한다
        readingTimerService.uptTimerWithdrawal(1L);

        // 자동 완료 뒤 알림 재시도 중인 세션까지 예약 취소했는지 검증한다
        verify(readingTimerMapper).uptTimerAlimCancel(1L, withdrawalDate);
    }

    /**
     * 목표시간이 지난 세션을 목표시각에 완료하고 알림 발송 일시를 기록하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void storeTimerAlimDate() {

        LocalDateTime schedulerDate = LocalDateTime.of(2026, 8, 15, 0, 1);
        LocalDateTime targetEndDate = LocalDateTime.of(2026, 8, 15, 0, 0, 55);
        ReadingTimerDto timerDto = new ReadingTimerDto();
        // 테스트 타이머 세션 번호를 설정한다
        timerDto.setTmrxNumb(10L);
        // 테스트 알림 수신자 번호를 설정한다
        timerDto.setUserNumb(31L);
        // 자동 완료 전 실행 중 상태를 설정한다
        timerDto.setTmrxStat(Constant.TIMER_STAT_RUNNING);
        // 1시간 30분 목표시간을 설정한다
        timerDto.setTargSecs(5400L);
        // 목표시간 전체를 측정할 최근 시작 시각을 설정한다
        timerDto.setLastStrt(LocalDateTime.of(2026, 8, 14, 22, 30, 55));
        // 아직 확정하지 않은 독서시간을 0초로 설정한다
        timerDto.setReadSecs(0L);
        // 스케줄러 실행보다 5초 앞선 실제 목표 종료시각을 설정한다
        timerDto.setAlrmDate(targetEndDate);
        // 이번 실행에서 처리할 목표시간 경과 세션을 설정한다
        when(readingTimerMapper.getDueTimerAlimList(Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_COMPLETED
                                                  , Constant.USER_STAT_ACTIVE, schedulerDate, 100)).thenReturn(List.of(10L));
        // 자동 완료 트랜잭션의 실행 세션 잠금 조회 결과를 설정한다
        when(readingTimerMapper.getDueTimerAlimDtl(10L, Constant.TIMER_STAT_RUNNING
                                                 , Constant.USER_STAT_ACTIVE, schedulerDate)).thenReturn(timerDto);
        // 자동 완료 커밋 뒤 알림 트랜잭션의 완료 세션 조회 결과를 설정한다
        when(readingTimerMapper.getTimerAlimDtl(10L, Constant.TIMER_STAT_COMPLETED
                                              , Constant.USER_STAT_ACTIVE, schedulerDate)).thenReturn(timerDto);
        // 공통 알림 저장 성공 결과를 설정한다
        when(alimService.sendAlim(eq(31L), eq(Constant.ALIM_SITU_TIMER)
                                , eq(Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER), eq(Constant.ALIM_TARGET_TIMER)
                                , eq((Long) null), eq((Long) null), any())).thenReturn(ResultData.success());

        // 목표시간이 지난 세션의 알림을 독서 타이머 서비스에서 발송한다
        readingTimerService.sendTimerAlim();

        // BOOK_TIMER_OVER 템플릿에 1시간 30분 치환값을 전달했는지 검증한다
        verify(alimService).sendAlim(eq(31L), eq(Constant.ALIM_SITU_TIMER)
                                  , eq(Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER), eq(Constant.ALIM_TARGET_TIMER)
                                  , eq((Long) null), eq((Long) null)
                                  , argThat(replaceMap -> "1시간 30분".equals(replaceMap.get("timerTime"))));
        // 목표 종료시각까지의 1시간 30분만 세션에 확정됐는지 확인한다
        assertEquals(5400L, timerDto.getReadSecs());
        // 목표시간이 끝난 세션이 완료 상태로 변경됐는지 확인한다
        assertEquals(Constant.TIMER_STAT_COMPLETED, timerDto.getTmrxStat());
        // 스케줄러 지연 시각이 아닌 실제 목표시각이 완료 일시인지 확인한다
        assertEquals(targetEndDate, timerDto.getEndxDate());
        // 완료 세션의 최근 시작 시각이 제거됐는지 확인한다
        assertNull(timerDto.getLastStrt());
        // 자정 전 독서시간이 8월 14일 집계에 저장됐는지 확인한다
        verify(readingTimerMapper).setReadingDaily(31L, LocalDate.of(2026, 8, 14), 5345L, targetEndDate);
        // 자정 후 독서시간이 8월 15일 집계에 저장됐는지 확인한다
        verify(readingTimerMapper).setReadingDaily(31L, LocalDate.of(2026, 8, 15), 55L, targetEndDate);
        // 발송 기준 일시가 세션에 저장됐는지 확인한다
        assertEquals(schedulerDate, timerDto.getSendDate());
        // 발송 뒤 예약 일시가 해제됐는지 확인한다
        assertNull(timerDto.getAlrmDate());
        // 자동 완료 세션의 상태와 확정시간을 먼저 수정했는지 검증한다
        verify(readingTimerMapper).uptTimer(timerDto);
        // 발송 완료 세션을 수정했는지 검증한다
        verify(readingTimerMapper).uptTimerAlimSent(timerDto);
        // 자동 완료와 알림 트랜잭션이 각각 독립적으로 커밋됐는지 검증한다
        verify(transactionManager, times(2)).commit(transactionStatus);
    }

    /**
     * 다른 실행에서 먼저 처리한 세션은 알림을 중복 발송하지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendTimerAlimSkipsStale() {

        LocalDateTime alarmDate = LocalDateTime.of(2026, 8, 15, 0, 1);
        // 이번 실행에서 확인할 목표시간 경과 세션을 설정한다
        when(readingTimerMapper.getDueTimerAlimList(Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_COMPLETED
                                                  , Constant.USER_STAT_ACTIVE, alarmDate, 100)).thenReturn(List.of(10L));
        // 다른 실행에서 먼저 처리한 세션은 잠금 조회에서 반환하지 않는다
        when(readingTimerMapper.getDueTimerAlimDtl(10L, Constant.TIMER_STAT_RUNNING
                                                 , Constant.USER_STAT_ACTIVE, alarmDate)).thenReturn(null);
        // 완료된 알림 재시도 대상도 아닌 세션으로 설정한다
        when(readingTimerMapper.getTimerAlimDtl(10L, Constant.TIMER_STAT_COMPLETED
                                              , Constant.USER_STAT_ACTIVE, alarmDate)).thenReturn(null);

        // 더 이상 대상이 아닌 세션의 목표시간 알림 실행 주기를 처리한다
        readingTimerService.sendTimerAlim();

        // 공통 알림 서비스가 호출되지 않았는지 검증한다
        verify(alimService, never()).sendAlim(any(), any(), any(), any(), any(), any(), any());
        // 세션 발송 일시가 수정되지 않았는지 검증한다
        verify(readingTimerMapper, never()).uptTimerAlimSent(any());
    }

    /**
     * 알림 저장 실패가 먼저 커밋된 목표시간 자동 완료를 되돌리지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void keepTimerCompletion() {

        LocalDateTime alarmDate = LocalDateTime.of(2026, 8, 15, 0, 1);
        ReadingTimerDto timerDto = createTimer(Constant.TIMER_STAT_RUNNING, LocalDateTime.of(2026, 8, 15, 0, 0));
        // 1분 목표시간을 설정한다
        timerDto.setTargSecs(60L);
        // 정확한 목표 종료시각을 예약 일시로 설정한다
        timerDto.setAlrmDate(alarmDate);
        // 자동 완료와 알림 재시도 대상 세션을 설정한다
        when(readingTimerMapper.getDueTimerAlimList(Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_COMPLETED
                                                  , Constant.USER_STAT_ACTIVE, alarmDate, 100)).thenReturn(List.of(10L));
        // 자동 완료할 실행 세션을 반환한다
        when(readingTimerMapper.getDueTimerAlimDtl(10L, Constant.TIMER_STAT_RUNNING
                                                 , Constant.USER_STAT_ACTIVE, alarmDate)).thenReturn(timerDto);
        // 완료 뒤 알림을 발송할 같은 세션을 반환한다
        when(readingTimerMapper.getTimerAlimDtl(10L, Constant.TIMER_STAT_COMPLETED
                                              , Constant.USER_STAT_ACTIVE, alarmDate)).thenReturn(timerDto);
        // 알림 저장 거절 결과를 설정한다
        when(alimService.sendAlim(eq(1L), eq(Constant.ALIM_SITU_TIMER)
                                , eq(Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER), eq(Constant.ALIM_TARGET_TIMER)
                                , eq((Long) null), eq((Long) null), any()))
                .thenReturn(ResultData.fail(ResultEnum.COMMON_NO_DATA));

        // 목표시간 자동 완료와 알림 발송 주기를 실행한다
        readingTimerService.sendTimerAlim();

        // 알림 실패와 관계없이 목표시간까지의 독서시간이 확정됐는지 확인한다
        assertEquals(60L, timerDto.getReadSecs());
        // 먼저 커밋된 세션이 완료 상태를 유지하는지 확인한다
        assertEquals(Constant.TIMER_STAT_COMPLETED, timerDto.getTmrxStat());
        // 다음 스케줄러 주기에 알림을 재시도하도록 예약 일시가 유지되는지 확인한다
        assertEquals(alarmDate, timerDto.getAlrmDate());
        // 자동 완료 상태는 데이터베이스에 저장했는지 검증한다
        verify(readingTimerMapper).uptTimer(timerDto);
        // 실패한 알림의 발송 일시는 저장하지 않았는지 검증한다
        verify(readingTimerMapper, never()).uptTimerAlimSent(any());
        // 자동 완료 트랜잭션만 커밋됐는지 확인한다
        verify(transactionManager).commit(transactionStatus);
        // 알림 트랜잭션은 롤백됐는지 확인한다
        verify(transactionManager).rollback(transactionStatus);
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
