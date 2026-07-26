package org.our.sadari.global.scheduler.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.service.SchedulerLogService;

/**
 * 공통 스케줄러 로그 지원 객체가 로그 저장 예외를 격리하고 최종 상태를 올바르게 계산하는지 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class SchedulerLogSupportTest {

    @Mock
    private SchedulerLogService schedulerLogService;

    private SchedulerLogSupport schedulerLogSupport;

    /**
     * 각 테스트가 독립적인 공통 로그 지원 객체를 사용하도록 구성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        schedulerLogSupport = new SchedulerLogSupport(schedulerLogService);
    }

    /**
     * 실행 시작 로그 저장 중 예외가 발생해도 호출 스케줄러에 예외를 전파하지 않고 null을 반환하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setSchedulerLogSafelyReturnsNullWhenLogStorageFails() {
        SchedulerLogDto.SchedulerRunDto schedulerRunDto =
                new SchedulerLogDto.SchedulerRunDto();
        when(schedulerLogService.setSchedulerLog(schedulerRunDto))
                .thenThrow(new IllegalStateException("log storage failure"));

        Long runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);

        assertNull(runxNumb);
    }

    /**
     * 마스터 실행 번호가 없을 때 연결할 수 없는 실패 상세 로그를 저장하지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setSchedulerFailSafelySkipsWhenRunNumberIsMissing() {
        schedulerLogSupport.setSchedulerFailSafely(
                null
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , new IllegalStateException("scheduler failure")
        );

        verify(schedulerLogService, never()).setSchedulerFail(any());
    }

    /**
     * Java 예외 정보를 TL_SCFAIL DTO의 예외 유형 및 오류 내용으로 변환하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setSchedulerFailSafelyMapsExceptionInformation() {
        RuntimeException exception = new IllegalArgumentException("invalid target");

        schedulerLogSupport.setSchedulerFailSafely(
                7L
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , exception
        );

        ArgumentCaptor<SchedulerLogDto.SchedulerFailDto> failCaptor =
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerFailDto.class);
        verify(schedulerLogService).setSchedulerFail(failCaptor.capture());
        assertEquals(7L, failCaptor.getValue().getRunxNumb());
        assertEquals(IllegalArgumentException.class.getName(), failCaptor.getValue().getErroType());
        assertEquals("invalid target", failCaptor.getValue().getErroCntn());
    }

    /**
     * 실행 종료 로그 수정 실패가 실제 스케줄러 업무 흐름으로 전파되지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void uptSchedulerLogSafelySuppressesLogStorageFailure() {
        SchedulerLogDto.SchedulerRunDto schedulerRunDto =
                new SchedulerLogDto.SchedulerRunDto();
        schedulerRunDto.setRunxNumb(9L);
        doThrow(new IllegalStateException("log update failure"))
                .when(schedulerLogService)
                .uptSchedulerLog(schedulerRunDto);

        assertDoesNotThrow(() -> schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto));
    }

    /**
     * 성공 및 실패 건수 조합에 따라 성공, 일부 실패, 실패 상태가 결정되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getSchedulerExecutionStatusUsesSuccessAndFailureCounts() {
        assertEquals(
                Constant.SCHEDULER_EXEC_SUCCESS
              , schedulerLogSupport.getSchedulerExecutionStatus(3, 0)
        );
        assertEquals(
                Constant.SCHEDULER_EXEC_PARTIAL
              , schedulerLogSupport.getSchedulerExecutionStatus(2, 1)
        );
        assertEquals(
                Constant.SCHEDULER_EXEC_FAILURE
              , schedulerLogSupport.getSchedulerExecutionStatus(0, 2)
        );
    }
}
