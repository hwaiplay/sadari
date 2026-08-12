package org.our.sadari.push.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.push.dto.PushDto;

/**
 * fileName       : PushService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 */
public interface PushService {
    // getFirebaseWebConfig 조회로 후속 처리에 필요한 데이터를 가져온다
    ResultData getFirebaseWebConfig();

    // setPushSub 호출로 업무 처리에 필요한 값을 설정한다
    ResultData setPushSub(Long userNumb, PushDto.PushSubDto req);

    // delPushSub 호출로 삭제 대상 데이터를 정리한다
    ResultData delPushSub(Long userNumb, PushDto.PushSubDto req);

    /**
     * 전체 기기 로그아웃 시 회원의 모든 푸시 구독을 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 전체 로그아웃 회원 번호
     * @return 구독 비활성화 결과
     */
    ResultData delAllPushSub(Long userNumb);

    /**
     * 알림 수신자의 활성 FCM 토큰으로 알림 내용과 클릭 이동 정보를 발송한다.
     *
     * @author SeungHyeon.Kang
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
