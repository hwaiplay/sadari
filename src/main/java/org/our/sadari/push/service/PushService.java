package org.our.sadari.push.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.push.dto.PushDto;

/**
 * 푸시 설정 조회, 구독 저장, FCM 발송 기능을 제공하는 Service 계약입니다.
 *
 * @author Seunghyeon.Kang
 */
public interface PushService {

    ResultData getFirebaseWebConfig();

    ResultData setPushSub(Long userNumb, PushDto.PushSubDto req);

    ResultData delPushSub(Long userNumb, PushDto.PushSubDto req);

    /**
     * 알림 수신자의 활성 FCM 토큰으로 알림 내용과 클릭 이동 정보를 발송한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 알림 수신 사용자 번호
     * @param title 푸시 제목
     * @param body 푸시 내용
     * @param linkUrlx 클릭 이동 링크
     * @param alimNumb 클릭 시 읽음 처리할 사용자별 알림 번호
     */
    void sendPush(Long userNumb
                , String title
                , String body
                , String linkUrlx
                , Long alimNumb);
}
