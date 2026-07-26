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
import org.our.sadari.global.scheduler.service.ReportDateOverService;

/**
 * 상세코드의 사용 여부에 따라 실제 스케줄러 서비스 호출이 제어되는지 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @Mock
    private ReportDateOverService reportDateOverService;

    @Mock
    private CodeUtil codeUtil;

    private Scheduler scheduler;

    /**
     * 스케줄 실행 분기만 독립적으로 검증할 수 있도록 Mock 의존성으로 Scheduler를 구성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        scheduler = new Scheduler(reportDateOverService, codeUtil);
    }

    /**
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중이면 실제 알림 서비스를 호출하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void sendReportDateOverAlimRunsWhenDetailCodeIsEnabled() {
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        )).thenReturn(true);

        scheduler.sendReportDateOverAlim();

        verify(reportDateOverService).sendReportDateOverAlim();
    }

    /**
     * 상세코드가 사용 중지 상태이거나 존재하지 않으면 실제 알림 서비스를 호출하지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void sendReportDateOverAlimSkipsWhenDetailCodeIsDisabled() {
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        )).thenReturn(false);

        scheduler.sendReportDateOverAlim();

        verify(reportDateOverService, never()).sendReportDateOverAlim();
    }
}
