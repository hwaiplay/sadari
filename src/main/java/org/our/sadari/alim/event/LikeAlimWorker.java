package org.our.sadari.alim.event;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * fileName       : LikeAlimWorker
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 좋아요 응답과 분리된 스레드에서 알림 저장과 푸시를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 * 2026-08-27        SeungHyeon.Kang    동적 이동 대상 정보 저장 적용
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LikeAlimWorker {

    // 사용자별 알림 저장과 커밋 이후 푸시 발송 서비스
    private final AlimService alimService;
    // 이벤트에 닉네임이 없는 좋아요 등록자의 Redis 정보 조회 서비스
    private final TokenRedisService tokenRedisService;

    /**
     * 커밋된 좋아요의 알림을 별도 스레드에서 저장하고 푸시 발송을 예약한다.
     * 알림 또는 외부 푸시 실패는 기록만 남기고 좋아요 관계에 영향을 주지 않는다.
     *
     * @author HanWon.Jang
     * @param event 좋아요 등록자와 알림 수신자 및 템플릿 정보
     */
    @Async
    public void sendLikeAlim(LikeAlimEvent event) {
        // 필수 알림 식별값이 없으면 잘못된 후처리가 DB에 접근하지 않도록 중단한다
        if (StringUtil.isEmpty(event) || StringUtil.hasEmpty(event.getSendUserNumb(), event.getTargetUserNumb(), event.getTempCode())) {
            // 유효하지 않은 좋아요 알림 후처리를 종료한다
            return;
        }

        // 알림 저장과 푸시 실패가 비동기 실행기의 예외 처리기로 전파되지 않도록 격리한다
        try {
            String sendUserNick = event.getSendUserNick();

            // 독후감과 사진 좋아요처럼 이벤트에 닉네임이 없으면 로그인 Redis 정보에서 조회한다
            if (StringUtil.isEmpty(sendUserNick)) {
                // 알림 템플릿 치환에 사용할 좋아요 등록자 닉네임을 조회한다
                sendUserNick = tokenRedisService.getUserNick(event.getSendUserNumb());
            }

            // 발신자 닉네임이 없으면 미완성 알림을 저장하지 않는다
            if (StringUtil.isEmpty(sendUserNick)) {
                // 닉네임을 확인할 수 없는 좋아요 알림 후처리를 종료한다
                return;
            }

            // 좋아요 알림 템플릿에 등록자 닉네임을 전달할 치환값을 생성한다
            Map<String, Object> replaceMap = new HashMap<>();
            // 템플릿 사용자명에 검증된 좋아요 등록자 닉네임을 설정한다
            replaceMap.put("userName", sendUserNick);

            // 좋아요 트랜잭션과 분리된 새 알림 트랜잭션에서 저장과 푸시 예약을 처리한다
            alimService.sendUserAlim(
                    event.getSendUserNumb()
                  , event.getTargetUserNumb()
                  , Constant.ALIM_SITU_LIKE
                  , event.getTempCode()
                  , event.getTagtType()
                  , event.getTagtNumb()
                  , event.getReplyNumb()
                  , replaceMap
            );
        }

        // 알림 DB와 Redis 및 FCM 실패는 좋아요 완료 결과와 분리하여 운영 로그로 남긴다
        catch (RuntimeException e) {
            log.warn("Like notification processing failed. sender={}, target={}", event.getSendUserNumb(), event.getTargetUserNumb(), e);
        }
    }
}
