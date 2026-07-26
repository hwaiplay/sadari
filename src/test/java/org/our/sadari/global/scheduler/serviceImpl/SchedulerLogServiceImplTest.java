package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.SchedulerLogMapper;
import org.our.sadari.global.scheduler.service.SchedulerLogServiceImpl;

/**
 * 스케줄러 로그 서비스가 실행 로그와 실패 로그의 필수값 및 Mapper 반영 건수를 검증하는지 확인합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class SchedulerLogServiceImplTest {

    @Mock
    private SchedulerLogMapper schedulerLogMapper;

    private SchedulerLogServiceImpl schedulerLogService;

    /**
     * Mock Mapper를 사용하는 스케줄러 로그 서비스 구현체를 생성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        schedulerLogService = new SchedulerLogServiceImpl(schedulerLogMapper);
    }

    /**
     * 실행 시작 INSERT에서 발급된 실행 번호를 호출부에 반환하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setSchedulerLogReturnsGeneratedRunNumber() {
        SchedulerLogDto.SchedulerRunDto runDto = new SchedulerLogDto.SchedulerRunDto();
        runDto.setSchdCode(Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
        runDto.setMethName("sendReportDateOverAlim");
        runDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        when(schedulerLogMapper.setSchedulerLog(runDto)).thenAnswer(invocation -> {
            runDto.setRunxNumb(7L);
            return 1;
        });

        Long runxNumb = schedulerLogService.setSchedulerLog(runDto);

        assertEquals(7L, runxNumb);
        verify(schedulerLogMapper).setSchedulerLog(runDto);
    }

    /**
     * 실행 번호가 없는 실패 정보는 복합키를 계산할 수 없으므로 Mapper 호출 전에 차단하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setSchedulerFailRejectsMissingRunNumber() {
        SchedulerLogDto.SchedulerFailDto failDto = new SchedulerLogDto.SchedulerFailDto();
        failDto.setFailType(Constant.SCHEDULER_FAIL_EXCEPTION);

        assertThrows(
                IllegalArgumentException.class
              , () -> schedulerLogService.setSchedulerFail(failDto)
        );
    }

    /**
     * 실행 종료 UPDATE가 정확히 한 건 반영되지 않으면 로그 유실로 판단하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void uptSchedulerLogRejectsMissingMasterRow() {
        SchedulerLogDto.SchedulerRunDto runDto = new SchedulerLogDto.SchedulerRunDto();
        runDto.setRunxNumb(7L);
        runDto.setExecStat(Constant.SCHEDULER_EXEC_SUCCESS);
        runDto.setTrgtCntt(1);
        runDto.setSuccCntt(1);
        runDto.setFailCntt(0);
        runDto.setExecMsec(10L);
        when(schedulerLogMapper.uptSchedulerLog(runDto)).thenReturn(0);

        assertThrows(
                IllegalStateException.class
              , () -> schedulerLogService.uptSchedulerLog(runDto)
        );
    }
}
