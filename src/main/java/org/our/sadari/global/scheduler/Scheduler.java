package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.AlimDeleteService;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

/**
 * fileName       : Scheduler
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무에 필요한 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    // 목표 독서기간 초과 알림 업무를 수행하는 서비스
    private final ReportDateOverService reportDateOverService;

    // 사용자가 삭제 상태로 변경한 알림을 물리 삭제하는 서비스
    private final AlimDeleteService alimDeleteService;

    // TB_CODEXD에서 스케줄러 상세코드의 활성 여부를 조회하는 공통 코드 유틸리티
    private final CodeUtil codeUtil;

    /**
     * 매일 09:00부터 10:00까지 5분 간격으로 실행
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중인 경우에만 실제 알림 스케줄러를 호출
     *
     * @author SeungHyeon.Kang
     */
    @Schedules({@Scheduled(cron = "0 */5 9 * * *"), // 오전 9:00 ~ 9:55 (5분 간격)
                @Scheduled(cron = "0 0 10 * * *")})  // 오전 10:00 정각
    public void sendReportDateOverAlim() {

        /*
         * CodeUtil은 USEE_YSNO가 Y인 상세코드만 반환한다.
         * 코드가 없거나 N이면 운영자가 중지한 스케줄러로 간주하여 업무 호출과 실행 로그 생성을 모두 생략한다.
         */
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_REPORT_DATE_OVER)) {

            // 처리 상태를 정보 로그로 남긴다
            log.info("목표 독서기간 초과 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                    , Constant.CODE_SCHD_CODE
                    , Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
            // 매일 09:00부터 10:00까지 5분 간격으로 실행 결과를 반환한다
            return;
        }

        // sendReportDateOverAlim 업무 로직을 reportDateOverService에 위임한다
        reportDateOverService.sendReportDateOverAlim();
    }

    /**
     * 매시 0분부터 10분 간격으로 삭제 상태 알림을 물리 삭제
     * SCHD_CODE의 ALIM_DELETE 상세코드가 사용 중인 경우에만 업무와 실행 로그를 생성
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void delAlim() {

        /*
         * 기존 스케줄러와 동일하게 운영자가 공통코드의 USEE_YSNO만 변경하여 실행을 중지할 수 있게 한다.
         * 사용 중지 상태에서는 업무 서비스에 진입하지 않으므로 불필요한 빈 실행 로그도 생성하지 않는다.
         */
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_ALIM_DELETE)) {

            // 처리 상태를 정보 로그로 남긴다
            log.info("알림 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                    , Constant.CODE_SCHD_CODE
                    , Constant.SCHEDULER_CODE_ALIM_DELETE);
            // 매시 0분부터 10분 간격으로 삭제 상태 알림을 물리 삭제 결과를 반환한다
            return;
        }

        // delAlim 업무 로직을 alimDeleteService에 위임한다
        alimDeleteService.delAlim();
    }

}
