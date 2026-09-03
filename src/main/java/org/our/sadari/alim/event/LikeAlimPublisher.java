package org.our.sadari.alim.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * fileName       : LikeAlimPublisher
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 좋아요 트랜잭션과 알림 후처리 사이의 이벤트 경계를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LikeAlimPublisher {

    // Spring 트랜잭션 완료 알림을 전달할 이벤트 발행기
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 신규 좋아요 알림을 현재 트랜잭션의 커밋 이후 처리 대상으로 등록함
     * 이벤트 등록 자체가 실패해도 핵심 좋아요 저장은 계속 확정되도록 예외를 격리함
     *
     * @author HanWon.Jang
     * @param event 좋아요 알림 수신자와 템플릿 정보
     */
    public void setLikeAlim(LikeAlimEvent event) {
        // 잘못된 이벤트는 좋아요 저장 흐름과 알림 시스템 모두에 전달하지 않음
        if (event == null) {
            // 알림 처리 없이 호출을 종료함
            return;
        }

        // 알림 기반 정보가 잘못되어도 좋아요 저장 경로에 예외가 전파되지 않도록 분리함
        try {
            // 현재 좋아요 트랜잭션에 커밋 이후 실행할 알림 이벤트를 등록함
            eventPublisher.publishEvent(event);
        }

        // 이벤트 등록 실패는 좋아요 성공 여부와 분리하고 운영 로그로만 확인함
        catch (RuntimeException e) {
            log.warn("Like notification event publish failed. sender={}, target={}", event.getSendUserNumb(), event.getTargetUserNumb(), e);
        }
    }
}
