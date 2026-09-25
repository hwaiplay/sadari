package org.our.sadari.global.scheduler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;
import org.our.sadari.global.common.logging.LogSafe;
import org.our.sadari.global.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.complaint.service.ComplaintEvidenceCleanupService;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.scheduler.service.AlimDeleteService;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.our.sadari.global.scheduler.service.UserHardDeleteService;
import org.our.sadari.global.scheduler.service.UserStatusEventService;
import org.our.sadari.global.scheduler.service.TimerDetailDeleteService;
import org.our.sadari.readingClub.service.OwnerElectionService;
import org.our.sadari.readingClub.service.ReadingClubService;
import org.our.sadari.timer.service.ReadingTimerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

/**
 * fileName       : Scheduler
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무에 필요한 기능을 제공
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 * 2026-07-29        SeungHyeon.Kang    환경별 영구 삭제 유예기간 설명 반영
 * 2026-07-30        SeungHyeon.Kang    회원 상태 Outbox 5분 동기화 추가
 * 2026-08-14        SeungHyeon.Kang    독서 타이머 상세 보존기간 정리 추가
 * 2026-08-20        SeungHyeon.Kang    독서 타이머 목표시간 알림 추가
 * 2026-08-21        SeungHyeon.Kang    독서 타이머 목표시간 자동 완료 추가
 * 2026-09-05        SeungHyeon.Kang    스케줄 진입점·환경설정 통합
 * 2026-09-19        SeungHyeon.Kang         운영 로그 및 안전한 오류 진단
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
    // 모임 독서 회차 확정 업무 서비스
    private final ReadingClubService readingClubService;
    // 모임장 승계 선거 업무 서비스
    private final OwnerElectionService ownerElectionService;
    // 프로필 이미지 임시 파일 업무 서비스
    private final FileService fileService;
    // 보존기간이 지난 신고 증거 정리 업무 서비스
    private final ComplaintEvidenceCleanupService complaintEvidenceCleanupService;

    // TB_CODEXD에서 스케줄러 상세코드의 활성 여부를 조회하는 공통 코드 유틸리티
    private final CodeUtil codeUtil;

    /**
     * 매일 09:00부터 10:00까지 5분 간격으로 실행
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중인 경우에만 실제 알림 스케줄러를 호출
     *
     * @author SeungHyeon.Kang
     */
    @Schedules({@Scheduled(cron = "${scheduler.report-date-over-morning-cron:0 */5 9 * * *}")
              , @Scheduled(cron = "${scheduler.report-date-over-final-cron:0 0 10 * * *}")})
    public void sendReportDateOverAlim() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("sendReportDateOverAlim", () -> {
            /*
             * CodeUtil은 USEE_YSNO가 Y인 상세코드만 반환
             * 코드가 없거나 N이면 운영자가 중지한 스케줄러로 간주하여 업무 호출과 실행 로그 생성을 모두 생략
             */
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_REPORT_DATE_OVER)) {
                // 처리 상태를 정보 로그로 남김
                log.info("목표 독서기간 초과 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_REPORT_DATE_OVER);
                // 매일 09:00부터 10:00까지 5분 간격으로 실행 결과를 반환
                return;
            }

            // sendReportDateOverAlim 업무 로직을 reportDateOverService에 위임
            reportDateOverService.sendReportDateOverAlim();
        });
    }

    /**
     * 매시 0분부터 10분 간격으로 삭제 상태 알림을 물리 삭제
     * SCHD_CODE의 ALIM_DELETE 상세코드가 사용 중인 경우에만 업무와 실행 로그를 생성
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.alim-delete-cron:0 */10 * * * *}")
    public void delAlim() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("delAlim", () -> {
            /*
             * 기존 스케줄러와 동일하게 운영자가 공통코드의 USEE_YSNO만 변경하여 실행을 중지할 수 있도록 처리
             * 사용 중지 상태에서는 업무 서비스에 진입하지 않으므로 불필요한 빈 실행 로그도 생성하지 않음
             */
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_ALIM_DELETE)) {
                // 처리 상태를 정보 로그로 남김
                log.info("알림 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_ALIM_DELETE);
                // 매시 0분부터 10분 간격으로 삭제 상태 알림을 물리 삭제 결과를 반환
                return;
            }

            // delAlim 업무 로직을 alimDeleteService에 위임
            alimDeleteService.delAlim();
        });
    }

    /**
     * 매일 새벽 3시에 환경별 유예기간이 끝난 회원을 영구 삭제
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.user-hard-delete-cron:0 0 3 * * *}")
    public void delPendingUsers() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("delPendingUsers", () -> {

            // 공통코드에서 중지된 영구 삭제 스케줄러는 업무와 실행 로그를 만들지 않음
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE)) {
                // 영구 삭제 스케줄러 중지 상태를 운영 로그에 기록
                log.info("회원 영구 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE);
                // 사용 중지된 스케줄러 처리를 종료
                return;
            }

            // 삭제 유예기간이 끝난 회원의 연관 데이터와 회원 원본을 삭제
            userHardDeleteService.delPendingUsers();
        });
    }

    /**
     * 5분 간격으로 회원 상태 변경 Outbox를 사용자 Redis에 반영
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.user-status-sync-cron:0 */5 * * * *}")
    public void syncUserStatusEvents() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("syncUserStatusEvents", () -> {

            // 공통코드에서 중지된 회원 상태 동기화 스케줄러는 업무와 실행 로그를 만들지 않음
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_STATUS_SYNC)) {
                // 회원 상태 동기화 스케줄러 중지 상태를 운영 로그에 기록
                log.info("회원 상태 동기화 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_STATUS_SYNC);
                // 사용 중지된 스케줄러 처리를 종료
                return;
            }

            // 대기 중인 회원 상태 변경 이벤트를 사용자 Redis에 반영
            userStatusEventService.syncUserStatusEvents();
        });
    }

    /**
     * 10초 간격으로 목표 독서시간이 지난 실행 세션을 완료하고 알림을 발송
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.book-timer-over-cron:*/10 * * * * *}")
    public void sendTimerAlim() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("sendTimerAlim", () -> {

            // 공통코드에서 중지된 독서 타이머 자동 완료 스케줄러는 실행하지 않음
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER)) {
                // 사용 중지 상태를 운영 로그에 남김
                log.info("독서 타이머 목표시간 자동 완료 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                        , Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER);
                // 사용 중지된 스케줄러 처리를 종료
                return;
            }

            // 목표시간이 지난 독서 타이머 자동 완료와 알림 발송을 실행
            readingTimerService.sendTimerAlim();
        });
    }

    /**
     * 매일 새벽 3시 30분에 보존기간이 지난 독서 타이머 세션 상세를 삭제
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.timer-detail-delete-cron:0 30 3 * * *}")
    public void delExpiredTimer() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("delExpiredTimer", () -> {

            // 공통코드에서 중지된 타이머 상세 정리 스케줄러는 실행하지 않음
            if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_TIMER_DETAIL_DELETE)) {
                // 사용 중지 상태를 운영 로그에 남김
                log.info("독서 타이머 상세 정리 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}"
                        , Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_TIMER_DETAIL_DELETE);
                // 사용 중지된 스케줄러 처리를 종료
                return;
            }

            // 보존기간이 지난 완료 세션 상세를 삭제
            timerDetailDeleteService.delExpiredTimer();
        });
    }

    /**
     * 날짜가 바뀐 종료 회차의 목표 결과를 확정
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.round-completion-cron:0 * * * * *}")
    public void completeExpiredRound() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("completeExpiredRound", () -> {
            // 종료된 회차가 상세 화면에 고정 결과로 노출되도록 확정 처리를 위임
            readingClubService.completeExpiredRound();
        });
    }

    /**
     * 상태 전환된 모임의 선거를 생성하고 마감된 선거를 확정
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${scheduler.owner-election-cron:0 * * * * *}")
    public void uptOwnerElection() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("uptOwnerElection", () -> {
            // 누락된 선거를 먼저 생성하여 상태만 남는 모임을 방지
            ownerElectionService.startPendingElection();
            // 마감된 투표의 당선, 결선, 연장 또는 일시중지를 확정
            ownerElectionService.completeDueElection();
        });
    }

    /**
     * 10분마다 30분 보존 시간을 지난 프로필 임시 이미지를 삭제
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(fixedDelayString = "${scheduler.profile-image-draft-cleanup-delay-ms:600000}")
    public void delExpiredProfileDrafts() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("delExpiredProfileDrafts", () -> {
            // 만료된 사용자별 임시 원본과 미리보기 정리를 파일 서비스에 위임
            fileService.delExpiredProfileDrafts();
        });
    }

    /**
     * 최종 처리 뒤 정책 보존기간이 지난 신고 이미지 증거를 정리
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${complaint.evidence.cleanup-cron:0 20 4 * * *}")
    public void delExpiredEvidence() {
        // 주기 작업의 시작·종료·실패를 동일 실행 식별자로 연결
        setTaskLog("delExpiredEvidence", () -> {
            // 신고 상태와 보존기간 검증을 포함한 증거 정리를 업무 서비스에 위임
            complaintEvidenceCleanupService.delExpiredEvidence();
        });
    }

    /**
     * 주기 작업의 실패 전파를 유지하며 실행 단위 진단 문맥 구성
     *
     * @author SeungHyeon.Kang
     * @param taskName 코드에 정의된 작업 이름
     * @param task 실행할 기존 스케줄 작업
     */
    private void setTaskLog(String taskName, Runnable task) {
        // 스케줄 실행 전의 스레드 문맥 보존
        String previousId = MDC.get("jobId");
        // 한 실행의 하위 로그를 묶는 임시 식별자
        MDC.put("jobId", UUID.randomUUID().toString());
        // 주기 실행 소요 시간 기준
        long started = System.nanoTime();
        // 정상 실행과 실패 모두 진단 문맥 해제 보장
        try {
            // 상세 진단 시 실행 시작 확인
            log.debug("event=scheduler_started task={}", taskName);
            // 기존 활성 여부 검사와 업무 처리 실행
            task.run();
            // 고빈도 타이머 폴링은 DEBUG에서만 완료 확인
            if ("sendTimerAlim".equals(taskName)) {
                // 실제 처리 건수는 스케줄러 업무 요약에서 별도 확인
                log.debug("event=scheduler_returned task={} durationMs={}", taskName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            }

            // 일반 주기 작업의 호출 완료를 운영 기본 레벨에서 확인
            else {
                // 내부에서 격리한 부분 실패는 업무 요약 로그와 함께 확인
                log.info("event=scheduler_returned task={} durationMs={}", taskName, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            }
        }

        // 코드 조회와 업무 실행 단계의 미처리 실패 기록
        catch (RuntimeException error) {
            // 원시 예외 메시지 대신 원인과 코드 위치 기록
            log.error("event=scheduler_failed task={} failure={}", taskName, LogSafe.getFailure(error));
            throw error;
        }

        // 스케줄 스레드 재사용 전 기존 문맥 복구
        finally {
            // 기존 작업 식별자가 없는 경우 현재 값 제거
            if (StringUtil.isEmpty(previousId)) {
                // 종료된 실행 문맥 해제
                MDC.remove("jobId");
            }

            // 중첩 실행의 기존 식별자 보존
            else {
                // 상위 작업 문맥 복원
                MDC.put("jobId", previousId);
            }
        }
    }
}
