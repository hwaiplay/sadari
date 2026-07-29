package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.AlimDeleteMapper;
import org.our.sadari.global.scheduler.service.AlimDeleteServiceImpl;

/**
 * fileName       : AlimDeleteServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 스케줄러 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class AlimDeleteServiceImplTest {

    // AlimDelete 데이터 접근 객체
    @Mock
    private AlimDeleteMapper alimDeleteMapper;

    // 스케줄러 로그 안전 처리 객체
    @Mock
    private SchedulerLogSupport schedulerLogSupport;

    // 알림 삭제 스케줄러 단위 테스트 대상
    private AlimDeleteServiceImpl alimDeleteService;

    /**
     * 각 테스트에서 독립적인 서비스 구현체를 생성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 알림 삭제 스케줄러 단위 테스트 대상을 담을 객체를 생성한다
        alimDeleteService = new AlimDeleteServiceImpl(alimDeleteMapper, schedulerLogSupport);
    }

    /**
     * 실제 삭제 건수가 대상 및 성공 건수에 동일하게 기록되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimRecordsDeletedCountAsSuccess() {
        // 성공 실행 로그에 사용할 실행 번호를 설정한다
        when(schedulerLogSupport.setSchedulerLogSafely(any())).thenReturn(1L);
        // Alim 데이터를 DB에서 삭제한다
        when(alimDeleteMapper.delAlim()).thenReturn(12);

        // delAlim 업무 로직을 alimDeleteService에 위임한다
        alimDeleteService.delAlim();

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> captor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogSupport).uptSchedulerLogSafely(captor.capture());

        // 현재 항목의 값을 조회한다
        SchedulerLogDto.SchedulerRunDto log = captor.getValue();
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(Constant.SCHEDULER_CODE_ALIM_DELETE, log.getSchdCode());
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(Constant.SCHEDULER_EXEC_SUCCESS, log.getExecStat());
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(12, log.getTrgtCntt());
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(12, log.getSuccCntt());
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(0, log.getFailCntt());
    }

    /**
     * 삭제 대상이 없을 때 불필요한 스케줄러 로그를 저장하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimSkipsLogWhenNothingWasDeleted() {
        // Alim 데이터를 DB에서 삭제한다
        when(alimDeleteMapper.delAlim()).thenReturn(0);

        // delAlim 업무 로직을 alimDeleteService에 위임한다
        alimDeleteService.delAlim();

        // 처리 건수가 없는 실행은 마스터 로그 수정도 요청하지 않았는지 검증한다
        verify(schedulerLogSupport, never()).uptSchedulerLogSafely(any(SchedulerLogDto.SchedulerRunDto.class));
    }

    /**
     * DELETE 예외가 실패 상세와 마스터 실패 상태에 모두 반영되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimRecordsFailureAndRethrowsDeleteException() {
        // 실패 상세 로그에 연결할 실행 번호를 설정한다
        when(schedulerLogSupport.setSchedulerLogSafely(any())).thenReturn(1L);
        // 스케줄러 실패 상황을 재현할 예외를 담을 객체를 생성한다
        RuntimeException exception = new RuntimeException("delete failed");
        // Alim 데이터를 DB에서 삭제한다
        when(alimDeleteMapper.delAlim()).thenThrow(exception);

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인한다
        assertThrows(RuntimeException.class, alimDeleteService::delAlim);

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(schedulerLogSupport).setSchedulerFailSafely(
                1L
              , Constant.SCHEDULER_FAIL_EXCEPTION
              , null
              , null
              , exception
        );

        ArgumentCaptor<SchedulerLogDto.SchedulerRunDto> captor =
                // 리플렉션 호출 결과의 반환 타입을 지정한다
                ArgumentCaptor.forClass(SchedulerLogDto.SchedulerRunDto.class);
        // 호출 인자를 검증하기 위해 캡처한다
        verify(schedulerLogSupport).uptSchedulerLogSafely(captor.capture());
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(
                Constant.SCHEDULER_EXEC_FAILURE
              , captor.getValue().getExecStat()
        );
        // 실제 처리 결과가 예상값과 일치하는지 검증한다
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getValue().getFailCntt());
    }
}
