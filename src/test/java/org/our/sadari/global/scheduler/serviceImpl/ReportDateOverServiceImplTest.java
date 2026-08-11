package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.ReportDateOverMapper;
import org.our.sadari.global.scheduler.service.ReportDateOverServiceImpl;
import org.our.sadari.global.scheduler.service.SchedulerLogService;
import org.our.sadari.report.dto.ReportDto;

/**
 * fileName       : ReportDateOverServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ReportDateOverServiceImplTest {

    // ReportDateOver 데이터 접근 객체
    @Mock
    private ReportDateOverMapper reportDateOverMapper;

    // Alim 업무 처리 서비스
    @Mock
    private AlimService alimService;

    // SchedulerLog 업무 처리 서비스
    @Mock
    private SchedulerLogService schedulerLogService;

    // 목표 독서기간 만료 스케줄러 단위 테스트 대상
    private ReportDateOverServiceImpl schedulerService;

    /**
     * 운영 yml의 기본값과 같은 100건 제한값으로 테스트 대상 서비스를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 스케줄러 로그 예외 처리 테스트 대상을 담을 객체를 생성한다
        SchedulerLogSupport schedulerLogSupport = new SchedulerLogSupport(schedulerLogService);
        // 목표 독서기간 만료 스케줄러 단위 테스트 대상을 담을 객체를 생성한다
        schedulerService = new ReportDateOverServiceImpl(reportDateOverMapper, alimService, schedulerLogSupport, 100);
        // SchedulerLog 업무 값을 schedulerLogService DTO에 설정한다
        lenient().when(schedulerLogService.setSchedulerLog(any())).thenReturn(1L);
    }

    /**
     * Mapper 조회에 max-size 100을 전달하고 ReportDto의 책 제목을 #{bookTitl} 치환 Map으로 발송하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendOverdueUsesConfig() {
        // createTarget 호출로 후속 처리에 필요한 객체를 생성한다
        ReportDto target = createTarget(10L, 31L, "나미야 잡화점의 기적");
        // ReportDateOverTargetList 데이터를 DB에서 조회한다
        when(reportDateOverMapper.getOverdueReportList(100)).thenReturn(List.of(target));
        // sendAlim 업무 로직을 alimService에 위임한다
        when(alimService.sendAlim(
                // 검증할 호출 인자의 동등 조건을 지정한다
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , any()
        // 정상 처리 결과를 공통 성공 응답으로 생성한다
        )).thenReturn(ResultData.success());

        // sendReportDateOverAlim 업무 로직을 schedulerService에 위임한다
        schedulerService.sendReportDateOverAlim();

        @SuppressWarnings("unchecked")
        // 리플렉션 호출 결과의 반환 타입을 지정한다
        ArgumentCaptor<Map<String, Object>> replaceMapCaptor = ArgumentCaptor.forClass(Map.class);
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimService).sendAlim(
                // 검증할 호출 인자의 동등 조건을 지정한다
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , replaceMapCaptor.capture()
        );
        // 현재 항목의 값을 조회한다
        assertEquals("나미야 잡화점의 기적", replaceMapCaptor.getValue().get("bookTitl"));

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> runCaptor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogService).uptSchedulerLog(runCaptor.capture());
        // 현재 항목의 값을 조회한다
        assertEquals(Constant.SCHEDULER_EXEC_SUCCESS, runCaptor.getValue().getExecStat());
        // 현재 항목의 값을 조회한다
        assertEquals(1, runCaptor.getValue().getTrgtCntt());
        // 현재 항목의 값을 조회한다
        assertEquals(1, runCaptor.getValue().getSuccCntt());
        // 현재 항목의 값을 조회한다
        assertEquals(0, runCaptor.getValue().getFailCntt());
    }

    /**
     * 한 대상의 발송 예외가 발생해도 다음 대상 알림이 계속 처리되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendOverdueContinues() {
        // createTarget 호출로 후속 처리에 필요한 객체를 생성한다
        ReportDto failedTarget = createTarget(10L, 31L, "첫 번째 책");
        // createTarget 호출로 후속 처리에 필요한 객체를 생성한다
        ReportDto nextTarget = createTarget(11L, 32L, "두 번째 책");
        // ReportDateOverTargetList 데이터를 DB에서 조회한다
        when(reportDateOverMapper.getOverdueReportList(100))
                .thenReturn(List.of(failedTarget, nextTarget));
        // sendAlim 업무 로직을 alimService에 위임한다
        when(alimService.sendAlim(
                // 검증할 호출 인자의 동등 조건을 지정한다
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , any()
        // 스케줄러 비정상 상태를 재현할 예외를 담을 객체를 생성한다
        )).thenThrow(new IllegalStateException("temporary failure"));
        // sendAlim 업무 로직을 alimService에 위임한다
        when(alimService.sendAlim(
                // 검증할 호출 인자의 동등 조건을 지정한다
                eq(32L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(11L)
              , any()
        // 정상 처리 결과를 공통 성공 응답으로 생성한다
        )).thenReturn(ResultData.success());

        // 스케줄러가 대상 없음 상황을 예외 없이 처리하는지 검증한다
        assertDoesNotThrow(schedulerService::sendReportDateOverAlim);
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimService).sendAlim(
                // 검증할 호출 인자의 동등 조건을 지정한다
                eq(32L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(11L)
              , any()
        );

        ArgumentCaptor<SchedulerLogDto.SchedulerFailDto> failCaptor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerFailDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogService).setSchedulerFail(failCaptor.capture());
        // 현재 항목의 값을 조회한다
        assertEquals(1L, failCaptor.getValue().getRunxNumb());
        // 현재 항목의 값을 조회한다
        assertEquals(Constant.SCHEDULER_FAIL_EXCEPTION, failCaptor.getValue().getFailType());
        // getName 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(IllegalStateException.class.getName(), failCaptor.getValue().getErroType());

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> runCaptor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogService).uptSchedulerLog(runCaptor.capture());
        // 현재 항목의 값을 조회한다
        assertEquals(Constant.SCHEDULER_EXEC_PARTIAL, runCaptor.getValue().getExecStat());
        // 현재 항목의 값을 조회한다
        assertEquals(2, runCaptor.getValue().getTrgtCntt());
        // 현재 항목의 값을 조회한다
        assertEquals(1, runCaptor.getValue().getSuccCntt());
        // 현재 항목의 값을 조회한다
        assertEquals(1, runCaptor.getValue().getFailCntt());
    }

    /**
     * 조회 대상이 없을 때 실행 로그를 등록하거나 수정하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendOverdueSetsNoData() {
        // ReportDateOverTargetList 데이터를 DB에서 조회한다
        when(reportDateOverMapper.getOverdueReportList(100)).thenReturn(List.of());

        // sendReportDateOverAlim 업무 로직을 schedulerService에 위임한다
        schedulerService.sendReportDateOverAlim();

        // 처리 건수가 모두 0이면 마스터 로그를 등록하지 않는지 검증한다
        verify(schedulerLogService, never()).setSchedulerLog(any());
        // 등록된 마스터 로그가 없으므로 종료 로그 수정도 호출하지 않는지 검증한다
        verify(schedulerLogService, never()).uptSchedulerLog(any());
    }

    /**
     * 테스트에 사용할 목표기간 초과 독후감 DTO를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 알림 수신 사용자 번호
     * @param bookTitl 알림 문구에 표시할 책 제목
     * @return 독후감 도메인의 공용 ReportDto
     */
    private ReportDto createTarget(Long reptNumb, Long userNumb, String bookTitl) {
        // 독후감 또는 독서 목표 처리 데이터를 담을 객체를 생성한다
        ReportDto target = new ReportDto();
        // ReptNumb 업무 값을 target DTO에 설정한다
        target.setReptNumb(reptNumb);
        // UserNumb 업무 값을 target DTO에 설정한다
        target.setUserNumb(userNumb);
        // BookTitl 업무 값을 target DTO에 설정한다
        target.setBookTitl(bookTitl);
        // 테스트에 사용할 목표기간 초과 독후감 DTO를 생성한 결과를 반환한다
        return target;
    }
}
