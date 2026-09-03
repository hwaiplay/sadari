package org.our.sadari.user.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserWithdrawalDto;

/**
 * fileName       : UserWithdrawalService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 회원 탈퇴 재인증과 상태 변경 업무를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
public interface UserWithdrawalService {

    /**
     * 회원 탈퇴 요청을 검증하고 Kakao 재인증 URL을 발급함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴를 요청한 회원 번호
     * @param request 탈퇴 유형과 사유
     * @return Kakao 재인증 URL
     */
    ResultData setWithdrawalRequest(Long userNumb, UserWithdrawalDto request);

    /**
     * Kakao 재인증 결과를 검증하고 회원 탈퇴를 적용함
     *
     * @author SeungHyeon.Kang
     * @param code Kakao OAuth 인가 코드
     * @param state 탈퇴 요청 일회성 상태값
     * @return 적용된 탈퇴 유형
     */
    ResultData setWithdrawalCallback(String code, String state);

    /**
     * 로그인 회원의 영구 삭제 대기 상태를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 영구 삭제 예정 정보
     */
    ResultData getWithdrawalStatus(Long userNumb);

    /**
     * 영구 삭제 대기 상태를 취소하고 회원을 복구함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 복구할 회원 번호
     * @return 복구 처리 결과
     */
    ResultData uptWithdrawalCancel(Long userNumb);
}
