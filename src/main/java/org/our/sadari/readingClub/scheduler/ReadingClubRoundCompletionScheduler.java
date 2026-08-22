package org.our.sadari.readingClub.scheduler;

import lombok.RequiredArgsConstructor;
import org.our.sadari.readingClub.service.ReadingClubService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * fileName       : ReadingClubRoundCompletionScheduler
 * author         : HanWon.Jang
 * date           : 2026-08-22
 * description    : 목표 종료일이 지난 모임 독서 회차의 결과를 주기적으로 확정한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        HanWon.Jang        최초 생성
 */
@Component
@RequiredArgsConstructor
public class ReadingClubRoundCompletionScheduler {

    // 종료된 모임 독서 회차를 확정할 업무 서비스
    private final ReadingClubService readingClubService;

    /**
     * 날짜가 바뀐 종료 회차의 목표 결과를 확정한다.
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    @Scheduled(cron = "0 * * * * *")
    public void completeExpiredReadingRound() {
        // 종료된 회차가 상세 화면에 고정 결과로 노출되도록 확정 처리를 위임한다
        readingClubService.completeExpiredReadingRound();
    }
}
