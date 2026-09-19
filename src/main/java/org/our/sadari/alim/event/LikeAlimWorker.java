package org.our.sadari.alim.event;

import org.our.sadari.global.common.logging.LogSafe;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * fileName       : LikeAlimWorker
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 좋아요 응답과 분리된 스레드에서 알림 저장과 푸시를 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 * 2026-08-27        SeungHyeon.Kang    동적 이동 대상 정보 저장 적용
 * 2026-09-19        SeungHyeon.Kang         운영 로그 및 안전한 오류 진단
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
     * 커밋된 좋아요의 알림을 별도 스레드에서 저장하고 푸시 발송을 예약함
     * 알림 또는 외부 푸시 실패는 기록만 남기고 좋아요 관계에 영향을 주지 않음
     *
     * @author HanWon.Jang
     * @param event 좋아요 등록자와 알림 수신자 및 템플릿 정보
     */
    @Async
    public void sendLikeAlim(LikeAlimEvent event) {
        // 필수 알림 식별값이 없으면 잘못된 후처리가 DB에 접근하지 않도록 중단함
        if (StringUtil.isEmpty(event) || StringUtil.hasEmpty(event.getSendUserNumb(), event.getTargetUserNumb(), event.getTempCode())) {
            // 입력 데이터 없이 비동기 알림 누락 원인 기록
            log.warn("event=like_notification outcome=skipped reason=invalid_event");
            // 유효하지 않은 좋아요 알림 후처리를 종료함
            return;
        }

        // 작업 스레드의 기존 문맥 보존
        String previousId = MDC.get("requestId");
        // 요청 밖에서 생성된 이벤트의 이전 스레드 문맥 혼입 방지
        if (StringUtil.isEmpty(event.getRequestId())) {
            // 연결할 HTTP 요청이 없는 작업 문맥
            MDC.remove("requestId");
        }

        // 원래 HTTP 요청에서 생성한 식별자만 비동기 작업에 연결
        else {
            // 비동기 알림·DB·푸시 로그의 동일 요청 연결
            MDC.put("requestId", event.getRequestId());
        }

        // 알림 저장과 푸시 실패가 비동기 실행기의 예외 처리기로 전파되지 않도록 격리함
        try {
            String sendUserNick = event.getSendUserNick();

            // 독후감과 사진 좋아요처럼 이벤트에 닉네임이 없으면 로그인 Redis 정보에서 조회함
            if (StringUtil.isEmpty(sendUserNick)) {
                // 알림 템플릿 치환에 사용할 좋아요 등록자 닉네임을 조회함
                sendUserNick = tokenRedisService.getUserNick(event.getSendUserNumb());
            }

            // 발신자 닉네임이 없으면 미완성 알림을 저장하지 않음
            if (StringUtil.isEmpty(sendUserNick)) {
                // 개인정보 없이 알림 생략 원인 기록
                log.warn("event=like_notification outcome=skipped reason=missing_sender");
                // 닉네임을 확인할 수 없는 좋아요 알림 후처리를 종료함
                return;
            }

            // 좋아요 알림 템플릿에 등록자 닉네임을 전달할 치환값을 생성함
            Map<String, Object> replaceMap = new HashMap<>();
            // 템플릿 사용자명에 검증된 좋아요 등록자 닉네임을 설정함
            replaceMap.put("userName", sendUserNick);

            // 좋아요 트랜잭션과 분리된 새 알림 트랜잭션에서 저장과 푸시 예약을 처리함
            ResultData result = alimService.sendUserAlim(
                    event.getSendUserNumb()
                  , event.getTargetUserNumb()
                  , Constant.ALIM_SITU_LIKE
                  , event.getTempCode()
                  , event.getTagtType()
                  , event.getTagtNumb()
                  , event.getReplyNumb()
                  , replaceMap
            );
            // 예외 없이 반환된 업무 거절도 운영 로그에 기록
            if (StringUtil.isEmpty(result) || result.getCode() != 200) {
                // 수신자와 알림 본문 없이 저장 실패 기록
                log.warn("event=like_notification outcome=rejected");
            }

            // 성공 응답은 정책상 정상 생략을 포함한 처리 완료
            else {
                // 푸시 전송 결과와 구분되는 업무 처리 완료
                log.info("event=like_notification outcome=completed");
            }
        }

        // 알림 DB와 Redis 및 FCM 실패는 좋아요 완료 결과와 분리하여 운영 로그로 남김
        catch (RuntimeException e) {
            log.warn("Like notification processing failed. sender={}, target={} failure={}"
                   , event.getSendUserNumb(), event.getTargetUserNumb(), LogSafe.getFailure(e));
        }

        // 조기 반환과 오류를 포함한 작업 스레드 문맥 복원
        finally {
            // 기존 식별자가 없던 스레드는 현재 작업 값 제거
            if (StringUtil.isEmpty(previousId)) {
                // 다음 작업으로 요청 식별자 전파 방지
                MDC.remove("requestId");
            }

            // 중첩 실행 이전의 문맥 보존
            else {
                // 이전 작업 문맥 복원
                MDC.put("requestId", previousId);
            }
        }
    }
}
