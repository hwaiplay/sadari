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

    void sendPush(Long userNumb, String title, String body, String linkUrlx);
}
