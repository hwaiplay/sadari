package org.our.sadari.global.scheduler.service;

/**
 * fileName       : TimerDetailDeleteService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 세션 상세 보존기간 정리 업무를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 */
public interface TimerDetailDeleteService {

    /**
     * 보존기간이 지난 완료 타이머 세션 상세를 삭제하고 실행 결과를 기록한다
     *
     * @author SeungHyeon.Kang
     */
    void delExpiredTimer();
}
