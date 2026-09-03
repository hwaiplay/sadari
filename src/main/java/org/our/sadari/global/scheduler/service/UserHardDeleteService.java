package org.our.sadari.global.scheduler.service;

/**
 * fileName       : UserHardDeleteService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 삭제 유예기간이 끝난 회원의 영구 삭제 업무를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
public interface UserHardDeleteService {

    /**
     * 영구 삭제 예정일이 지난 회원과 연관 데이터를 물리 삭제함
     *
     * @author SeungHyeon.Kang
     */
    void delPendingUsers();
}
