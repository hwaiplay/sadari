package org.our.sadari.global.scheduler.service;

/**
 * fileName       : AlimDeleteService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 스케줄러 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
public interface AlimDeleteService {

    /**
     * DELT_YSNO가 Y인 알림을 삭제하고 실행 결과를 스케줄러 로그에 기록
     *
     * @author SeungHyeon.Kang
     */
    void delAlim();
}
