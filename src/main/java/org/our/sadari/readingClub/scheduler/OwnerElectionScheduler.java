package org.our.sadari.readingClub.scheduler;

import lombok.RequiredArgsConstructor;
import org.our.sadari.readingClub.service.OwnerElectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * fileName       : OwnerElectionScheduler
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 생성과 마감을 주기적으로 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
@Component
@RequiredArgsConstructor
public class OwnerElectionScheduler {

    // 모임장 승계 선거 업무 서비스
    private final OwnerElectionService ownerElectionService;

    /**
     * 상태 전환된 모임의 선거를 생성하고 마감된 선거를 확정함
     *
     * @author HanWon.Jang
     */
    @Scheduled(cron = "${scheduler.round-completion-cron}")
    public void processElection() {
        // 누락된 선거를 먼저 생성하여 상태만 남는 모임을 방지함
        ownerElectionService.startPendingElection();
        // 마감된 투표의 당선, 결선, 연장 또는 일시중지를 확정함
        ownerElectionService.completeDueElection();
    }
}
