package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.AlimDeleteService;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.our.sadari.global.scheduler.service.UserHardDeleteService;
import org.our.sadari.global.scheduler.service.UserStatusEventService;
import org.our.sadari.global.scheduler.service.TimerDetailDeleteService;
import org.our.sadari.timer.service.ReadingTimerService;
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
 * 2026-07-29        SeungHyeon.Kang    환경별 영구 삭제 유예기간 설명 반영
 * 2026-07-30        SeungHyeon.Kang    회원 상태 Outbox 5분 동기화 추가
 * 2026-08-14        SeungHyeon.Kang    독서 타이머 상세 보존기간 정리 추가
 * 2026-08-20        SeungHyeon.Kang    독서 타이머 목표시간 알림 추가
 * 2026-08-21        SeungHyeon.Kang    독서 타이머 목표시간 자동 완료 추가
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
    // 영구 삭제 대기 회원 물리 삭제 업무 서비스
    private final UserHardDeleteService userHardDeleteService;
    // 회원 상태 변경 Outbox Redis 동기화 업무 서비스
    private final UserStatusEventService userStatusEventService;
    // 보존기간이 지난 독서 타이머 상세를 정리하는 서비스
    private final TimerDetailDeleteService timerDetailDeleteService;
    // 설정한 독서 타이머 목표시간 자동 완료와 알림 업무 서비스
    private final ReadingTimerService readingTimerService;

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
            log.info("목표 독서기간 초과 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
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
            log.info("알림 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_ALIM_DELETE);
            // 매시 0분부터 10분 간격으로 삭제 상태 알림을 물리 삭제 결과를 반환한다
            return;
        }

        // delAlim 업무 로직을 alimDeleteService에 위임한다
        alimDeleteService.delAlim();
    }

    /**
     * 매일 새벽 3시에 환경별 유예기간이 끝난 회원을 영구 삭제한다.
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "0 0 3 * * *")
    //@Scheduled(cron = "*/10 * * * * *")//테스트
    public void delPendingUsers() {

        // 공통코드에서 중지된 영구 삭제 스케줄러는 업무와 실행 로그를 만들지 않는다
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE)) {
            // 영구 삭제 스케줄러 중지 상태를 운영 로그에 기록한다
            log.info("회원 영구 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE);
            // 사용 중지된 스케줄러 처리를 종료한다
            return;
        }

        // 삭제 유예기간이 끝난 회원의 연관 데이터와 회원 원본을 삭제한다
        userHardDeleteService.delPendingUsers();
    }

    /**
     * 5분 간격으로 회원 상태 변경 Outbox를 사용자 Redis에 반영한다
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "*/30 * * * * *")//테스트
    public void syncUserStatusEvents() {

        // 공통코드에서 중지된 회원 상태 동기화 스케줄러는 업무와 실행 로그를 만들지 않는다
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_STATUS_SYNC)) {
            // 회원 상태 동기화 스케줄러 중지 상태를 운영 로그에 기록한다
            log.info("회원 상태 동기화 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_STATUS_SYNC);
            // 사용 중지된 스케줄러 처리를 종료한다
            return;
        }

        // 대기 중인 회원 상태 변경 이벤트를 사용자 Redis에 반영한다
        userStatusEventService.syncUserStatusEvents();
    }

    /**
     * 10초 간격으로 목표 독서시간이 지난 실행 세션을 완료하고 알림을 발송한다
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "*/10 * * * * *")
    public void sendTimerAlim() {

        // 공통코드에서 중지된 독서 타이머 자동 완료 스케줄러는 실행하지 않는다
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER)) {
            // 사용 중지 상태를 운영 로그에 남긴다
            log.info("독서 타이머 목표시간 자동 완료 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                    , Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER);
            // 사용 중지된 스케줄러 처리를 종료한다
            return;
        }

        // 목표시간이 지난 독서 타이머 자동 완료와 알림 발송을 실행한다
        readingTimerService.sendTimerAlim();
    }

    /**
     * 매일 새벽 3시 30분에 보존기간이 지난 독서 타이머 세션 상세를 삭제한다
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "0 30 3 * * *")
    public void delExpiredTimer() {

        // 공통코드에서 중지된 타이머 상세 정리 스케줄러는 실행하지 않는다
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_TIMER_DETAIL_DELETE)) {
            // 사용 중지 상태를 운영 로그에 남긴다
            log.info("독서 타이머 상세 정리 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                    , Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_TIMER_DETAIL_DELETE);
            // 사용 중지된 스케줄러 처리를 종료한다
            return;
        }

        // 보존기간이 지난 완료 세션 상세를 삭제한다
        timerDetailDeleteService.delExpiredTimer();
    }

}
