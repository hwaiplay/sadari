package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.AlimDeleteMapper;
import org.our.sadari.global.scheduler.service.AlimDeleteServiceImpl;

/**
 * 알림 삭제 스케줄러의 삭제 결과 및 실패 로그 집계 정책을 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class AlimDeleteServiceImplTest {

    @Mock
    private AlimDeleteMapper alimDeleteMapper;

    @Mock
    private SchedulerLogSupport schedulerLogSupport;

    private AlimDeleteServiceImpl alimDeleteService;

    /**
     * 각 테스트에서 독립적인 서비스 구현체를 생성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        alimDeleteService = new AlimDeleteServiceImpl(alimDeleteMapper, schedulerLogSupport);
        when(schedulerLogSupport.setSchedulerLogSafely(any())).thenReturn(1L);
    }

    /**
     * 실제 삭제 건수가 대상 및 성공 건수에 동일하게 기록되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void delAlimRecordsDeletedCountAsSuccess() {
        when(alimDeleteMapper.delAlim()).thenReturn(12);

        alimDeleteService.delAlim();

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> captor =
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        verify(schedulerLogSupport).uptSchedulerLogSafely(captor.capture());

        SchedulerLogDto.SchedulerRunDto log = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(Constant.SCHEDULER_CODE_ALIM_DELETE, log.getSchdCode());
        org.junit.jupiter.api.Assertions.assertEquals(Constant.SCHEDULER_EXEC_SUCCESS, log.getExecStat());
        org.junit.jupiter.api.Assertions.assertEquals(12, log.getTrgtCntt());
        org.junit.jupiter.api.Assertions.assertEquals(12, log.getSuccCntt());
        org.junit.jupiter.api.Assertions.assertEquals(0, log.getFailCntt());
    }

    /**
     * 삭제 대상이 없을 때 정상적인 대상 없음 상태로 기록되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void delAlimRecordsNoDataWhenNothingWasDeleted() {
        when(alimDeleteMapper.delAlim()).thenReturn(0);

        alimDeleteService.delAlim();

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> captor =
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        verify(schedulerLogSupport).uptSchedulerLogSafely(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                Constant.SCHEDULER_EXEC_NO_DATA
              , captor.getValue().getExecStat()
        );
    }

    /**
     * DELETE 예외가 실패 상세와 마스터 실패 상태에 모두 반영되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void delAlimRecordsFailureAndRethrowsDeleteException() {
        RuntimeException exception = new RuntimeException("delete failed");
        when(alimDeleteMapper.delAlim()).thenThrow(exception);

        assertThrows(RuntimeException.class, alimDeleteService::delAlim);

        verify(schedulerLogSupport).setSchedulerFailSafely(
                1L
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , exception
        );

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> captor =
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        verify(schedulerLogSupport).uptSchedulerLogSafely(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                Constant.SCHEDULER_EXEC_FAILURE
              , captor.getValue().getExecStat()
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getValue().getFailCntt());
    }
}
