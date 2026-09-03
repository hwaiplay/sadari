package org.our.sadari.user.service;

/**
 * fileName       : NicknameGenerationService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 신규 회원 닉네임 자동 발급 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
public interface NicknameGenerationService {

    /**
     * 공통코드 조합과 현재 연월의 순번으로 중복되지 않는 신규 회원 닉네임을 발급함
     *
     * @author SeungHyeon.Kang
     * @return 신규 회원에게 저장할 자동 발급 닉네임
     */
    String setGeneratedNickname();
}
