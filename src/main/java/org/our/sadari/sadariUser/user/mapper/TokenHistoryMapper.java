package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.sadariUser.user.dto.TokenHistoryDto;

/**
 * packageName    : org.our.sadari.sadariUser.user.mapper
 * fileName       : TokenMapper.java
 * author         : Hanwon.Jang
 * date           : 2026-05-05
 * description    : 토큰 Mapper
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-05       Hanwon.Jang       최초 생성
 */

@Mapper
public interface TokenHistoryMapper {
    /**
     * 기존 토큰 삭제
     * @param userNumb
     * @return 결과 (0/1)
     */
    int deleteToken(@Param("userNumb") Long userNumb);

    /**
     * 새 토큰 저장
     * @param tokenHistoryDto
     * @return 결과 (0/1)
     */
    int setToken(TokenHistoryDto tokenHistoryDto);

    /**
     * 리프레시 토큰 조회
     * @param refreashToken
     * @return
     */
    TokenHistoryDto getRefreshToken(String refreashToken);
}
