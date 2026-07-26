package org.our.sadari.global.scheduler.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.our.sadari.global.scheduler.mapper.ReportDateOverMapper;
import org.our.sadari.global.scheduler.service.ReportDateOverServiceImpl;
import org.our.sadari.report.dto.ReportDto;

/**
 * 스케줄러 서비스가 yml에서 전달받은 조회 제한값과 책 제목 치환값을 정확히 사용하는지 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class ReportDateOverServiceImplTest {

    @Mock
    private ReportDateOverMapper reportDateOverMapper;

    @Mock
    private AlimService alimService;

    private ReportDateOverServiceImpl schedulerService;

    /**
     * 운영 yml의 기본값과 같은 100건 제한값으로 테스트 대상 서비스를 구성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        schedulerService = new ReportDateOverServiceImpl(reportDateOverMapper, alimService, 100);
    }

    /**
     * Mapper 조회에 max-size 100을 전달하고 ReportDto의 책 제목을 #{bookTitl} 치환 Map으로 발송하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void sendReportDateOverAlimUsesConfiguredMaxSizeAndReportDto() {
        ReportDto target = createTarget(10L, 31L, "나미야 잡화점의 기적");
        when(reportDateOverMapper.getReportDateOverTargetList(100)).thenReturn(List.of(target));
        when(alimService.sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , any()
        )).thenReturn(ResultData.success());

        schedulerService.sendReportDateOverAlim();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> replaceMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(alimService).sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , replaceMapCaptor.capture()
        );
        assertEquals("나미야 잡화점의 기적", replaceMapCaptor.getValue().get("bookTitl"));
    }

    /**
     * 한 대상의 발송 예외가 발생해도 다음 대상 알림이 계속 처리되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void sendReportDateOverAlimContinuesAfterSingleFailure() {
        ReportDto failedTarget = createTarget(10L, 31L, "첫 번째 책");
        ReportDto nextTarget = createTarget(11L, 32L, "두 번째 책");
        when(reportDateOverMapper.getReportDateOverTargetList(100))
                .thenReturn(List.of(failedTarget, nextTarget));
        when(alimService.sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(10L)
              , any()
        )).thenThrow(new IllegalStateException("temporary failure"));
        when(alimService.sendAlim(
                eq(32L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(11L)
              , any()
        )).thenReturn(ResultData.success());

        assertDoesNotThrow(schedulerService::sendReportDateOverAlim);
        verify(alimService).sendAlim(
                eq(32L)
              , eq(Constant.ALIM_SITU_REPORT)
              , eq(Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER)
              , eq(11L)
              , any()
        );
    }

    /**
     * 테스트에 사용할 목표기간 초과 독후감 DTO를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 알림 수신 사용자 번호
     * @param bookTitl 알림 문구에 표시할 책 제목
     * @return 독후감 도메인의 공용 ReportDto
     */
    private ReportDto createTarget(
            Long reptNumb
          , Long userNumb
          , String bookTitl) {

        ReportDto target = new ReportDto();
        target.setReptNumb(reptNumb);
        target.setUserNumb(userNumb);
        target.setBookTitl(bookTitl);
        return target;
    }
}
