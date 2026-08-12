package org.our.sadari.push.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.push.dto.PushDto;

/**
 * fileName       : PushMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface PushMapper {
    /**
     * FCM token을 구독 정보로 저장하거나, 이미 있으면 다시 활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호와 FCM token
     * @return 반영 건수
     */
    int setPushSub(PushDto.PushSubDto req);

    /**
     * 같은 브라우저 token이 다른 계정에 연결돼 있으면 이전 계정 구독을 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 FCM token
     * @return 반영 건수
     */
    int uptOtherPushDisabled(PushDto.PushSubDto req);

    /**
     * 사용자가 현재 브라우저의 푸시 구독을 끌 때 해당 token을 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호와 FCM token
     * @return 반영 건수
     */
    int delPushSub(PushDto.PushSubDto req);

    /**
     * 전체 기기 로그아웃 시 회원의 모든 푸시 구독을 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그아웃 회원 번호
     * @return 반영 건수
     */
    int delAllPushSub(@Param("userNumb") Long userNumb);

    /**
     * 알림 수신자의 활성 FCM token 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 알림 수신 사용자 번호
     * @return 활성 구독 목록
     */
    List<PushDto.PushSubDto> getActivePushSubList(@Param("userNumb") Long userNumb);
}
