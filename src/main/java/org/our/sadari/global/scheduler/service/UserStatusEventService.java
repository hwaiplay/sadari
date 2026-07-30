package org.our.sadari.global.scheduler.service;

/**
 * fileName       : UserStatusEventService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 회원 상태 변경 Outbox의 사용자 Redis 동기화 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
public interface UserStatusEventService {

    /**
     * 대기 중인 회원 상태 변경 이벤트를 현재 DB 상태로 Redis에 반영한다
     *
     * @author SeungHyeon.Kang
     */
    void syncUserStatusEvents();
}
