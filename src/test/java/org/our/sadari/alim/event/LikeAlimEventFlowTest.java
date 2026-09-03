package org.our.sadari.alim.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.springframework.context.ApplicationEventPublisher;

/**
 * fileName       : LikeAlimEventFlowTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 좋아요 저장과 분리된 알림 이벤트 발행 및 비동기 처리 경계를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class LikeAlimEventFlowTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private LikeAlimWorker likeAlimWorker;
    @Mock
    private AlimService alimService;
    @Mock
    private TokenRedisService tokenRedisService;

    private LikeAlimPublisher publisher;
    private LikeAlimListener listener;
    private LikeAlimWorker worker;

    /**
     * 각 테스트에서 사용할 이벤트 발행기와 리스너 및 작업자를 생성함
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 각 테스트가 독립적인 Mock 의존성을 사용하도록 대상 객체를 새로 생성함
        publisher = new LikeAlimPublisher(eventPublisher);
        listener = new LikeAlimListener(likeAlimWorker);
        worker = new LikeAlimWorker(alimService, tokenRedisService);
    }

    /**
     * 유효한 좋아요 알림 이벤트가 Spring 이벤트 발행기로 전달되는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("좋아요 알림 이벤트를 발행한다")
    void publishesEvent() {
        LikeAlimEvent event = createEvent("sender");

        // 좋아요 알림 이벤트를 현재 트랜잭션에 등록함
        publisher.setLikeAlim(event);

        // 동일 이벤트가 한 번만 Spring 이벤트 발행기로 전달되었는지 확인함
        verify(eventPublisher).publishEvent(event);
    }

    /**
     * 이벤트 발행 실패가 좋아요 호출 경로로 전파되지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("이벤트 발행 실패를 좋아요 처리와 격리한다")
    void ignoresPublishFailure() {
        LikeAlimEvent event = createEvent("sender");
        doThrow(new IllegalStateException("publish failed"))
                .when(eventPublisher)
                .publishEvent(event);

        // 알림 이벤트 발행 실패가 호출자에게 전파되지 않는지 확인함
        assertDoesNotThrow(() -> publisher.setLikeAlim(event));
    }

    /**
     * 커밋 이후 리스너가 좋아요 알림을 비동기 작업자에게 위임하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("커밋 이후 좋아요 알림 작업을 위임한다")
    void delegatesToWorker() {
        LikeAlimEvent event = createEvent("sender");

        // 커밋 완료 이벤트를 비동기 알림 작업자에게 전달함
        listener.handleLikeAlim(event);

        // 동일 이벤트가 작업자에게 한 번 전달되었는지 확인함
        verify(likeAlimWorker).sendLikeAlim(event);
    }

    /**
     * 비동기 작업 접수 실패가 커밋된 좋아요 결과로 전파되지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("비동기 작업 접수 실패를 좋아요 응답과 격리한다")
    void ignoresDispatchFailure() {
        LikeAlimEvent event = createEvent("sender");
        doThrow(new IllegalStateException("dispatch failed"))
                .when(likeAlimWorker)
                .sendLikeAlim(event);

        // 작업 접수 실패가 커밋 이후 호출 경계 밖으로 전파되지 않는지 확인함
        assertDoesNotThrow(() -> listener.handleLikeAlim(event));
    }

    /**
     * 이벤트에 포함된 닉네임으로 좋아요 알림 저장을 호출하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("비동기 작업자가 좋아요 알림을 저장한다")
    void usesEventNickname() {
        LikeAlimEvent event = createEvent("sender");

        // 별도 실행 스레드의 실제 작업 로직을 직접 호출함
        worker.sendLikeAlim(event);

        // 수신자와 좋아요 템플릿 및 이동 대상을 알림 서비스에 전달했는지 확인함
        verify(alimService).sendUserAlim(
                1L,
                2L,
                Constant.ALIM_SITU_LIKE,
                "REPORT_LIKE",
                Constant.LIKE_TARGET_REPORT,
                3L,
                null,
                java.util.Map.of("userName", "sender")
        );
        // 이벤트 닉네임이 있으면 Redis를 추가 조회하지 않는지 확인함
        verify(tokenRedisService, never()).getUserNick(1L);
    }

    /**
     * 이벤트에 닉네임이 없으면 Redis 사용자 정보로 알림을 완성하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("닉네임이 없으면 Redis에서 조회한다")
    void loadsNickFromRedis() {
        LikeAlimEvent event = createEvent(null);
        when(tokenRedisService.getUserNick(1L)).thenReturn("redis-sender");

        // 닉네임이 없는 알림 이벤트를 처리함
        worker.sendLikeAlim(event);

        // Redis 닉네임으로 완성한 좋아요 알림이 저장되는지 확인함
        verify(alimService).sendUserAlim(
                1L,
                2L,
                Constant.ALIM_SITU_LIKE,
                "REPORT_LIKE",
                Constant.LIKE_TARGET_REPORT,
                3L,
                null,
                java.util.Map.of("userName", "redis-sender")
        );
    }

    /**
     * 알림 저장 실패가 비동기 작업 경계 밖으로 전파되지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    @DisplayName("알림 저장 실패를 비동기 작업 내부에서 격리한다")
    void ignoresAlimFailure() {
        LikeAlimEvent event = createEvent("sender");
        when(alimService.sendUserAlim(
                eq(1L),
                eq(2L),
                eq(Constant.ALIM_SITU_LIKE),
                eq("REPORT_LIKE"),
                eq(Constant.LIKE_TARGET_REPORT),
                eq(3L),
                eq(null),
                anyMap()
        )).thenThrow(new IllegalStateException("notification failed"));

        // 알림 저장 실패가 비동기 실행기의 예외 처리기로 전파되지 않는지 확인함
        assertDoesNotThrow(() -> worker.sendLikeAlim(event));
    }

    /**
     * 테스트에서 공통으로 사용할 좋아요 알림 이벤트를 생성함
     *
     * @author HanWon.Jang
     * @param sendUserNick 좋아요 등록자 닉네임
     * @return 발신자와 수신자 및 대상이 설정된 이벤트
     */
    private LikeAlimEvent createEvent(String sendUserNick) {
        // 고정된 발신자와 수신자 및 독후감 대상으로 테스트 이벤트를 반환함
        return new LikeAlimEvent(1L, 2L, "REPORT_LIKE", Constant.LIKE_TARGET_REPORT
                               , 3L, null, sendUserNick);
    }
}
