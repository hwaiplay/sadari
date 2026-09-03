package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
 * fileName       : SchedulerLogServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 로직의 동작을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class SchedulerLogServiceImplTest {

    // SchedulerLog 데이터 접근 객체
    @Mock
    private SchedulerLogMapper schedulerLogMapper;

    // 스케줄러 로그 서비스 단위 테스트 대상
    private SchedulerLogServiceImpl schedulerLogService;

    /**
     * Mock Mapper를 사용하는 스케줄러 로그 서비스 구현체를 생성함
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 스케줄러 로그 서비스 단위 테스트 대상을 담을 객체를 생성함
        schedulerLogService = new SchedulerLogServiceImpl(schedulerLogMapper);
    }

    /**
     * 실행 시작 INSERT에서 발급된 실행 번호를 호출부에 반환하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setSchedLogReturnsRun() {
        // 스케줄러 실행 로그를 담을 객체를 생성함
        SchedulerLogDto.SchedulerRunDto runDto = new SchedulerLogDto.SchedulerRunDto();
        // SchdCode 업무 값을 runDto DTO에 설정함
        runDto.setSchdCode(Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
        // MethName 업무 값을 runDto DTO에 설정함
        runDto.setMethName("sendReportDateOverAlim");
        // ExecStat 업무 값을 runDto DTO에 설정함
        runDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        // StrtDate 업무 값을 runDto DTO에 설정함
        runDto.setStrtDate(LocalDateTime.now());
        when(schedulerLogMapper.setSchedulerLog(runDto)).thenAnswer(invocation -> {
            // RunxNumb 업무 값을 runDto DTO에 설정함
            runDto.setRunxNumb(7L);
            // 테스트 콜백에서 준비한 처리 결과를 반환함
            return 1;
        });

        // SchedulerLog 업무 값을 schedulerLogService DTO에 설정함
        Long runxNumb = schedulerLogService.setSchedulerLog(runDto);

        // 실제 처리 결과가 예상값과 일치하는지 검증함
        assertEquals(7L, runxNumb);
        // 의존 객체가 예상한 인자로 호출되었는지 검증함
        verify(schedulerLogMapper).setSchedulerLog(runDto);
    }

    /**
     * 실행 번호가 없는 실패 정보는 복합키를 계산할 수 없으므로 Mapper 호출 전에 차단하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setSchedFailMissingRun() {
        // 스케줄러 실패 상세 정보를 담을 객체를 생성함
        SchedulerLogDto.SchedulerFailDto failDto = new SchedulerLogDto.SchedulerFailDto();
        // FailType 업무 값을 failDto DTO에 설정함
        failDto.setFailType(Constant.SCHEDULER_FAIL_EXCEPTION);

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인함
        assertThrows(
                IllegalArgumentException.class
              , () -> schedulerLogService.setSchedulerFail(failDto)
        );
    }

    /**
     * 실행 종료 UPDATE가 정확히 한 건 반영되지 않으면 로그 유실로 판단하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptSchedLogMissingMaster() {
        // 스케줄러 실행 로그를 담을 객체를 생성함
        SchedulerLogDto.SchedulerRunDto runDto = new SchedulerLogDto.SchedulerRunDto();
        // RunxNumb 업무 값을 runDto DTO에 설정함
        runDto.setRunxNumb(7L);
        // ExecStat 업무 값을 runDto DTO에 설정함
        runDto.setExecStat(Constant.SCHEDULER_EXEC_SUCCESS);
        // TrgtCntt 업무 값을 runDto DTO에 설정함
        runDto.setTrgtCntt(1);
        // SuccCntt 업무 값을 runDto DTO에 설정함
        runDto.setSuccCntt(1);
        // FailCntt 업무 값을 runDto DTO에 설정함
        runDto.setFailCntt(0);
        // ExecMsec 업무 값을 runDto DTO에 설정함
        runDto.setExecMsec(10L);
        // SchedulerLog 데이터를 DB에서 수정함
        when(schedulerLogMapper.uptSchedulerLog(runDto)).thenReturn(0);

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인함
        assertThrows(
                IllegalStateException.class
              , () -> schedulerLogService.uptSchedulerLog(runDto)
        );
    }
}
