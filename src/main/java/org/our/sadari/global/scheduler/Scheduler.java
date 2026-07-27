package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

/**
 * 스케줄러
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    // 목표 독서기간 초과 알림 업무를 수행하는 서비스
    private final ReportDateOverService reportDateOverService;

    // TB_CODEXD에서 스케줄러 상세코드의 활성 여부를 조회하는 공통 코드 유틸리티
    private final CodeUtil codeUtil;

    /**
     * 매일 09:00부터 10:00까지 5분 간격으로 실행
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중인 경우에만 실제 알림 스케줄러를 호출
     *
     * @author Seunghyeon.Kang
     */
    @Schedules({@Scheduled(cron = "0 */5 9 * * *"), // 오전 9:00 ~ 9:55 (5분 간격)
                @Scheduled(cron = "0 0 10 * * *")})  // 오전 10:00 정각
    //@Scheduled(fixedRate = 10000) //테스트
    public void sendReportDateOverAlim() {
        /*
         * CodeUtil은 USEE_YSNO가 Y인 상세코드만 반환한다.
         * 코드가 없거나 N이면 운영자가 중지한 스케줄러로 간주하여 업무 호출과 실행 로그 생성을 모두 생략한다.
         */
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_REPORT_DATE_OVER)) {
            log.info("목표 독서기간 초과 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                    , Constant.CODE_SCHD_CODE
                    , Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
            return;
        }

        reportDateOverService.sendReportDateOverAlim();
    }

}
