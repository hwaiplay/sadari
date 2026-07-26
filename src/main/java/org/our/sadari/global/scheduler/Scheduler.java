package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

/**
 * 목표 독서기간 초과 알림 작업을 설정된 시간에 시작하는 스케줄러입니다.
 * 실행 시각만 관리하고 실제 대상 조회와 발송은 SchedulerService에 위임합니다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    private final ReportDateOverService reportDateOverService;

    /**
     * 매일 09:00부터 10:00까지 5분 간격으로 실행
     *
     * @author Seunghyeon.Kang
     */
    @Schedules({@Scheduled(cron = "0 */5 9 * * *"), // 오전 9:00 ~ 9:55 (5분 간격)
                @Scheduled(cron = "0 0 10 * * *")})  // 오전 10:00 정각
    public void sendReportDateOverAlim() {
        reportDateOverService.sendReportDateOverAlim();
    }
}
