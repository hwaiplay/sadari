package org.our.sadari.global.scheduler.service;

/**
 * 삭제 상태인 알림을 주기적으로 물리 삭제하는 스케줄러 업무 계약
 *
 * @author Seunghyeon.Kang
 */
public interface AlimDeleteService {

    /**
     * DELT_YSNO가 Y인 알림을 삭제하고 실행 결과를 스케줄러 로그에 기록
     *
     * @author Seunghyeon.Kang
     */
    void delAlim();
}
