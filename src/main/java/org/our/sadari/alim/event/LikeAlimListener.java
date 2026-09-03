package org.our.sadari.alim.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * fileName       : LikeAlimListener
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 커밋된 좋아요의 알림을 비동기 작업으로 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LikeAlimListener {

    // 좋아요 응답 스레드와 분리하여 알림을 처리할 작업자
    private final LikeAlimWorker likeAlimWorker;

    /**
     * 좋아요 관계가 커밋된 경우에만 알림 작업을 비동기 실행기로 전달함
     * 실행기 포화나 종료 상태에서도 이미 커밋된 좋아요 응답이 실패하지 않도록 예외를 격리함
     *
     * @author HanWon.Jang
     * @param event 커밋이 완료된 좋아요 알림 정보
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeAlim(LikeAlimEvent event) {
        // 잘못된 이벤트는 비동기 실행기에 전달하지 않음
        if (event == null) {
            // 알림 처리 없이 호출을 종료함
            return;
        }

        // 비동기 작업 접수 실패가 HTTP 응답 완료를 방해하지 않도록 분리함
        try {
            // 좋아요 알림 저장과 푸시 처리를 별도 실행 스레드에 위임함
            likeAlimWorker.sendLikeAlim(event);
        }

        // 비동기 실행기에서 작업을 받지 못하면 좋아요는 유지하고 알림만 생략함
        catch (RuntimeException e) {
            log.warn("Like notification dispatch failed. sender={}, target={}", event.getSendUserNumb(), event.getTargetUserNumb(), e);
        }
    }
}
