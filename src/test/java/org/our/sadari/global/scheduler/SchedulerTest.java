package org.our.sadari.global.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.AlimDeleteService;
import org.our.sadari.global.scheduler.service.ReportDateOverService;

/**
 * fileName       : SchedulerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    // ReportDateOver 업무 처리 서비스
    @Mock
    private ReportDateOverService reportDateOverService;

    // AlimDelete 업무 처리 서비스
    @Mock
    private AlimDeleteService alimDeleteService;

    // 공통코드 캐시 조회 객체
    @Mock
    private CodeUtil codeUtil;

    // 스케줄러 활성화 조건 단위 테스트 대상
    private Scheduler scheduler;

    /**
     * 스케줄 실행 분기만 독립적으로 검증할 수 있도록 Mock 의존성으로 Scheduler를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 스케줄러 활성화 조건 테스트 대상을 담을 객체를 생성한다
        scheduler = new Scheduler(reportDateOverService, alimDeleteService, codeUtil);
    }

    /**
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중이면 실제 알림 서비스를 호출하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendReportDateOverAlimRunsWhenDetailCodeIsEnabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        // 테스트 대상 의존 호출에 반환할 값을 지정한다
        )).thenReturn(true);

        // sendReportDateOverAlim 호출로 검증된 알림 또는 응답을 전송한다
        scheduler.sendReportDateOverAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(reportDateOverService).sendReportDateOverAlim();
    }

    /**
     * 상세코드가 사용 중지 상태이거나 존재하지 않으면 실제 알림 서비스를 호출하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendReportDateOverAlimSkipsWhenDetailCodeIsDisabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        // 테스트 대상 의존 호출에 반환할 값을 지정한다
        )).thenReturn(false);

        // sendReportDateOverAlim 호출로 검증된 알림 또는 응답을 전송한다
        scheduler.sendReportDateOverAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(reportDateOverService, never()).sendReportDateOverAlim();
    }

    /**
     * ALIM_DELETE 상세코드가 사용 중이면 알림 삭제 서비스를 호출하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimRunsWhenDetailCodeIsEnabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_ALIM_DELETE
        // 테스트 대상 의존 호출에 반환할 값을 지정한다
        )).thenReturn(true);

        // delAlim 호출로 삭제 대상 데이터를 정리한다
        scheduler.delAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimDeleteService).delAlim();
    }

    /**
     * ALIM_DELETE 상세코드가 중지 상태이면 알림 삭제 서비스를 호출하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimSkipsWhenDetailCodeIsDisabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_ALIM_DELETE
        // 테스트 대상 의존 호출에 반환할 값을 지정한다
        )).thenReturn(false);

        // delAlim 호출로 삭제 대상 데이터를 정리한다
        scheduler.delAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimDeleteService, never()).delAlim();
    }
}
