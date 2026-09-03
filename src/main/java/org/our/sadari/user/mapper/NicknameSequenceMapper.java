package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.user.dto.NicknameSequenceDto;

/**
 * fileName       : NicknameSequenceMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 신규 회원 닉네임 번호 발급 데이터 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    순번 식별 조건을 세부코드 조합으로 변경
 */
@Mapper
public interface NicknameSequenceMapper {

    /**
     * 현재 연월의 주어와 서술어 및 동물 코드 조합별 마지막 발급 번호를 증가시킴
     *
     * @author SeungHyeon.Kang
     * @param request 번호를 증가시킬 닉네임 세부코드 조합
     * @return 변경된 닉네임 번호 행 수
     */
    int uptNicknameSequence(NicknameSequenceDto request);

    /**
     * 현재 연월의 주어와 서술어 및 동물 코드 조합별 최초 발급 번호를 등록함
     *
     * @author SeungHyeon.Kang
     * @param request 최초 번호를 등록할 닉네임 세부코드 조합
     * @return 등록된 닉네임 번호 행 수
     */
    int setNicknameSequence(NicknameSequenceDto request);

    /**
     * 현재 연월의 주어와 서술어 및 동물 코드 조합별 마지막 발급 번호를 조회함
     *
     * @author SeungHyeon.Kang
     * @param request 조회할 닉네임 세부코드 조합
     * @return 현재 연월과 마지막 발급 번호
     */
    NicknameSequenceDto getNicknameSequenceDtl(NicknameSequenceDto request);
}
