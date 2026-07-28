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
 * fileName       : SchedulerLogSupportTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class SchedulerLogSupportTest {

    // SchedulerLog 업무 처리 서비스
    @Mock
    private SchedulerLogService schedulerLogService;

    // 스케줄러 로그 안전 처리 객체
    private SchedulerLogSupport schedulerLogSupport;

    /**
     * 각 테스트가 독립적인 공통 로그 지원 객체를 사용하도록 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 스케줄러 로그 예외 처리 테스트 대상을 담을 객체를 생성한다
        schedulerLogSupport = new SchedulerLogSupport(schedulerLogService);
    }

    /**
     * 실행 시작 로그 저장 중 예외가 발생해도 호출 스케줄러에 예외를 전파하지 않고 null을 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setSchedulerLogSafelyReturnsNullWhenLogStorageFails() {

        SchedulerLogDto.SchedulerRunDto schedulerRunDto =
                // 스케줄러 실행 로그를 담을 객체를 생성한다
                new SchedulerLogDto.SchedulerRunDto();
        // SchedulerLog 업무 값을 schedulerLogService DTO에 설정한다
        when(schedulerLogService.setSchedulerLog(schedulerRunDto))
                .thenThrow(new IllegalStateException("log storage failure"));

        // SchedulerLogSafely 업무 값을 schedulerLogSupport DTO에 설정한다
        Long runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);

        // 실패 로그 저장이 실패해도 예외가 전파되지 않는지 확인한다
        assertNull(runxNumb);
    }

    /**
     * 마스터 실행 번호가 없을 때 연결할 수 없는 실패 상세 로그를 저장하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setSchedulerFailSafelySkipsWhenRunNumberIsMissing() {

        // SchedulerFailSafely 업무 값을 schedulerLogSupport DTO에 설정한다
        schedulerLogSupport.setSchedulerFailSafely(
                null
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , new IllegalStateException("scheduler failure")
        );

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(schedulerLogService, never()).setSchedulerFail(any());
    }

    /**
     * Java 예외 정보를 TL_SCFAIL DTO의 예외 유형 및 오류 내용으로 변환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setSchedulerFailSafelyMapsExceptionInformation() {

        // 잘못된 입력 상황을 재현할 예외를 담을 객체를 생성한다
        RuntimeException exception = new IllegalArgumentException("invalid target");

        // SchedulerFailSafely 업무 값을 schedulerLogSupport DTO에 설정한다
        schedulerLogSupport.setSchedulerFailSafely(
                7L
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , exception
        );

        ArgumentCaptor<SchedulerLogDto.SchedulerFailDto> failCaptor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerFailDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogService).setSchedulerFail(failCaptor.capture());
        // 현재 항목의 값을 조회한다
        assertEquals(7L, failCaptor.getValue().getRunxNumb());
        // getName 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(IllegalArgumentException.class.getName(), failCaptor.getValue().getErroType());
        // 현재 항목의 값을 조회한다
        assertEquals("invalid target", failCaptor.getValue().getErroCntn());
    }

    /**
     * 실행 종료 로그 수정 실패가 실제 스케줄러 업무 흐름으로 전파되지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptSchedulerLogSafelySuppressesLogStorageFailure() {

        SchedulerLogDto.SchedulerRunDto schedulerRunDto =
                // 스케줄러 실행 로그를 담을 객체를 생성한다
                new SchedulerLogDto.SchedulerRunDto();
        // RunxNumb 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setRunxNumb(9L);
        // 스케줄러 비정상 상태를 재현할 예외를 담을 객체를 생성한다
        doThrow(new IllegalStateException("log update failure"))
                .when(schedulerLogService)
                .uptSchedulerLog(schedulerRunDto);

        // uptSchedulerLogSafely 호출로 변경된 업무 상태를 반영한다
        assertDoesNotThrow(() -> schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto));
    }

    /**
     * 성공 및 실패 건수 조합에 따라 성공, 일부 실패, 실패 상태가 결정되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getSchedulerExecutionStatusUsesSuccessAndFailureCounts() {

        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        assertEquals(
                Constant.SCHEDULER_EXEC_SUCCESS
              , schedulerLogSupport.getSchedulerExecutionStatus(3, 0)
        );
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        assertEquals(
                Constant.SCHEDULER_EXEC_PARTIAL
              , schedulerLogSupport.getSchedulerExecutionStatus(2, 1)
        );
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        assertEquals(
                Constant.SCHEDULER_EXEC_FAILURE
              , schedulerLogSupport.getSchedulerExecutionStatus(0, 2)
        );
    }
}
