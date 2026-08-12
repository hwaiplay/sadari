package org.our.sadari.user.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : UserSuspensionService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 정지 상태 확인과 기간 만료 복구 업무를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
public interface UserSuspensionService {

    /**
     * 로그인 회원에게 공개할 현재 정지 상태를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 현재 정지 상태
     */
    ResultData getUserSuspension(Long userNumb);

    /**
     * 기간이 끝난 정지 상태를 정지 직전 상태로 복구한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 회원 번호
     * @return 기간 만료로 상태를 변경했으면 true
     */
    boolean uptExpiredSuspension(Long userNumb);

    /**
     * 영구 탈퇴 취소 뒤 적용할 회원 상태를 계산한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 영구 탈퇴를 취소할 회원 번호
     * @return 유효한 정지가 있으면 SUSPENDED, 없으면 ACTIVE
     */
    String getWithdrawalCancelStatus(Long userNumb);
}
