package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
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
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Scheduler {

    private final ReportDateOverService reportDateOverService;

    /**
     *
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
